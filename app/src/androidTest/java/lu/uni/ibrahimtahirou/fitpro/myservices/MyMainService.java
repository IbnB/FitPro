package lu.uni.ibrahimtahirou.fitpro.myservices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import lu.uni.ibrahimtahirou.fitpro.constants.Constants;
import lu.uni.ibrahimtahirou.fitpro.detection.ActivityRecognitionService;
import lu.uni.ibrahimtahirou.fitpro.detection.MyDetectedActivity;
import lu.uni.ibrahimtahirou.fitpro.detection.ProbableActivityDetector;


//mport lu.uni.ibrahimtahirou.fitpro.myfragments.ActivityProfileFragment;

public class MyMainService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    public static final int REQUEST_CODE_GOOGLE_PLAYER_SERVICE = 101;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    public static final int NOTIFICATION_ID_STILL = 4543;
    public static final int NOTIFICATION_ID_VEHICLE = 3504;
    public static final int NOTIFICATION_ID_MOTION = 7045;
    // private Context context;
    private static final String TAG = "MyMainService Class";
    private static final int REQUEST_FINE_LOCATION = 0;
    public static volatile boolean isHandlerRunning;
    public static volatile int mActivityScanInterval = 0;
    public static volatile boolean isMainServiceRunning;
    final Handler mHandler = new Handler();
    private final String KEY_STILL_NUM_NOTIF = "numOfSentNotification";
    private final String KEY_STILL_TSTAMP = "timeStamp";
    private final String KEY_TRANSPORT_NUM_NOTIF = "TransportNumNotif";
    private final String KEY_TRANSPORT_TSTAMP = "TransportTimeStamp";
    private final String KEY_MOTION_NUM_NOTIF = "MotionNumNotif";
    private final String KEY_MOTION_TSTAMP = "MotionTimeStamp";
    MyDetectedActivity detectedActivity;
    ProbableActivityDetector probableActivityDetector;

    SharedPreferences sharedPref;
    SharedPreferences notificationSharedPref;
    SharedPreferences.Editor notificationSharedPrefEditor;
    SharedPreferences sharedPrefRxNotif;
    private Context mContext;
    private Thread mThread;
    private Runnable mRunnable;
    private PowerManager.WakeLock mWakeLock;
    private GoogleApiClient myApiClient;
    private PendingIntent pIntent;
    private BroadcastReceiver receiver;
    private BroadcastReceiver activityReceiver;
    private List<MyDetectedActivity> myDetectedActivityList;
    private List<ArrayList> wifiIdList;
    private ArrayList<String> mTempWifiList;
    //private List<List<String>> myDetectedWifiList;
    private long dayCount;
    private long intervalIdMax;
    private volatile int mRecordingInterval = 60;
    private HashMap<String, Integer> surveyData;
    private ArrayList<String> surveyActivity;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNM;


    public MyMainService() {

    }

    //method to format to string the wifiIdList to be added to DB
    public static String formatWifiIdListToString(ArrayList<String> myWifiList) {
        //converting the arrayList to string
        String myFormatedString = myWifiList.toString();
        // removing the opening and closing bracket
        myFormatedString = myFormatedString.replaceAll("^\\[|\\]$", "");

        return myFormatedString;

    }



    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        surveyData = new HashMap<>();
        surveyData.put("Still", 0);
        surveyData.put("InVehicle", 0);
        surveyData.put("Moving", 0);

        surveyActivity = new ArrayList<>();

        //Setting the data recording interval if not in sharedPreference ,then defaul is 60
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);


        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


       /* //getting the activity scan interval from settingsSharedPref
        String strActScanInterval = sharedPref.getString("activity_scan_Interval_pref", "0");
        Log.i("ActScan", strActScanInterval);
        mActivityScanInterval = Integer.valueOf(strActScanInterval.replaceAll("[^0-9]", ""));*/


        notificationSharedPref = mContext.getSharedPreferences("myNotificationSharedPref", Context.MODE_PRIVATE);
        notificationSharedPrefEditor = notificationSharedPref.edit();
        notificationSharedPrefEditor.putInt(KEY_STILL_NUM_NOTIF, 0);
        notificationSharedPrefEditor.putLong(KEY_STILL_TSTAMP, 0);
        notificationSharedPrefEditor.putInt(KEY_TRANSPORT_NUM_NOTIF, 0);
        notificationSharedPrefEditor.putLong(KEY_TRANSPORT_TSTAMP, 0);
        notificationSharedPrefEditor.putInt(KEY_MOTION_NUM_NOTIF, 0);
        notificationSharedPrefEditor.putLong(KEY_MOTION_TSTAMP, 0);
        notificationSharedPrefEditor.apply();

        myDetectedActivityList = new ArrayList<MyDetectedActivity>();
        mTempWifiList = new ArrayList<String>();
        detectedActivity = new MyDetectedActivity();
        //entry = new ActivityTable();
        //Log.i("QUERY2", IntervalRepository.createTable());
        //Log.i("QUERY3", ActivityRepository.createTable());
        //IntervalRepository.insertDefaultRow();


        myApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();


        //Receveirs

        activityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //receiving activity detection result
                if (Constants.CUSTOM_ACTION_ACTIVITY_DATA.equals(intent.getAction())) {
                    //Log.i("actisHandlerRunnig", String.valueOf(isHandlerRunning));

                    String s = "";
                    s += "Activity: " + intent.getStringExtra("Activity") + " " + "|Confidence: " + intent.getExtras().getInt("Confidence") + "%" + " |Time: " + convertMillsToHhMmSs(intent.getExtras().getLong("TimeStamp")) + "\n";
                    Log.i(TAG, s);


                    //add detected activity to a temporal list
                    myDetectedActivityList.add(new MyDetectedActivity(intent.getStringExtra("Activity"), intent.getExtras().getInt("Confidence"), intent.getExtras().getLong("TimeStamp")));


                    // Add detected activity to db
                   /* if (OldActivityRepo.insertActivity(intent.getStringExtra("Activity"), String.valueOf(intent.getExtras().getInt("Confidence")), String.valueOf(intent.getExtras().getLong("TimeStamp")))) {
                        //Toast.makeText(getApplicationContext(), "Activity added to db", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.i(TAG, "Activity NOT added to db");
                    }
*/
                    //requestActivityUpdates();
                }


            }
        };

        IntentFilter actfilter = new IntentFilter();
        actfilter.addAction(Constants.CUSTOM_ACTION_ACTIVITY_DATA);

        //LocalBroadcastManager.getInstance(mContext).registerReceiver(activityReceiver, actfilter);
        registerReceiver(activityReceiver, actfilter);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //receiving activity detection result
              /*  if (Constants.CUSTOM_ACTION_ACTIVITY_DATA.equals(intent.getAction())) {
                    Log.i("actisHandlerRunnig", String.valueOf(isHandlerRunning));

                    String s = "";
                    s += "Activity: " + intent.getStringExtra("Activity") + " " + "|Confidence: " + intent.getExtras().getInt("Confidence") + "%" + " |Time: " + millsToDateFormat(intent.getExtras().getLong("TimeStamp")) + "\n";
                    Log.i(TAG,s);

                    //add detected activity to a temporal list
                    myDetectedActivityList.add(new MyDetectedActivity(intent.getStringExtra("Activity"), intent.getExtras().getInt("Confidence"), intent.getExtras().getLong("TimeStamp")));


                    // Add detected activity to db
                    if (OldActivityRepo.insertActivity(intent.getStringExtra("Activity"), String.valueOf(intent.getExtras().getInt("Confidence")), String.valueOf(intent.getExtras().getLong("TimeStamp")))) {
                        //Toast.makeText(getApplicationContext(), "Activity added to db", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.i(TAG,"Activity NOT added to db");
                    }
                }*/



                //Retrieve wifi scan result broadcasted by wifiScan service
              /*  if (Constants.ACTION_SETTINGS_DATA.equals(intent.getAction())) {
                    Log.i("SettingsDa MainSer--> ", String.valueOf(intent.getStringArrayListExtra("settings")));
                    mRecordingInterval = Integer.valueOf(intent.getStringArrayListExtra("settings").get(2));
                    mActivityScanInterval = Integer.valueOf(intent.getStringArrayListExtra("settings").get(5));
                }
*/

            }
        };

        IntentFilter filter = new IntentFilter();
        //filter.addAction(Constants.CUSTOM_ACTION_ACTIVITY_DATA);
        //filter.addAction(Constants.ACTION_SETTINGS_DATA);
        //LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, filter);
        registerReceiver(receiver, filter);


        //**************
        // Get the Wake Lock
       /* PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLockTag");*/

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }


    // **** My methods*********

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Intent intentLocation = new Intent(MyMainService.this, MyLocationService.class);
        //startService(intentLocation);


        //start Activity recognition service
        Intent scan = new Intent(MyMainService.this, ActivityRecognitionService.class);
        startService(scan);
        requestActivityUpdates();

        //preference_key_isMainServiceON

        /*SharedPreferences mSharedPref = mContext.getSharedPreferences(String.valueOf(R.string.preference_key_isMainServiceON), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.clear();
        isMainServiceRunning = true;
        editor.putBoolean("IsMainServiceOn", isMainServiceRunning);
        editor.commit();

        //get boolean value for sending notification or not
        sharedPrefRxNotif = PreferenceManager.getDefaultSharedPreferences(mContext);

       if (!isAlarmWorking(mContext)){
           scheduleAlarm2(mContext); //schedule alarm
       }*/
        myApiClient.connect();






       /* SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean isServiceON = sharedPref.getBoolean("startStopService", true);
        Log.i("Wi-Fi IsON-->", String.valueOf(isServiceON));*/


        //myPeriodicCycle();


        return Service.START_STICKY;
    }

    public static String convertMillsToHhMmSs(long mills) {

        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("Europe/Luxembourg"));
        calendar.setTimeInMillis(mills);
        DateTime jodaTime = new DateTime(mills,
                DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Luxembourg")));
        org.joda.time.format.DateTimeFormatter parser1 = DateTimeFormat.forPattern("h:mm:ss aa");
        // System.out.println("Get Time : "+parser1.print(jodaTime));


        return parser1.print(jodaTime);
    }
    /*public void myPeriodicCycle() {
        Log.i("actisHandlerRun?Default", String.valueOf(isHandlerRunning));
        if (!isHandlerRunning) {
            isHandlerRunning = true;
            mWakeLock.acquire();
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    while (isHandlerRunning) {
                        Log.i("actisHandlerRunningIn", String.valueOf(isHandlerRunning));

                        long timestamp = 0;
                        String mostProbableActivity;
                        Log.i("dGMy Activity list --> ", myDetectedActivityList.toString());
                        //determining the most probable activity within a time window
                        if ((myDetectedActivityList != null) *//*&& (myDetectedActivityList.isEmpty()==false)*//*) {
                            //probableActivityDetector.getMostProbableActivity(myDetectedActivityList);
                            //TODO: Check the below line
                            //String mostProbableActivity = probableActivityDetector.getMostProbableActivity(myDetectedActivityList).get(0);
                            //Log.i("MyProbale ActivityOld: ", mostProbableActivity);
                            //TODO: workaround
                            // String mostProbableActivity = OldActivityRepo.myMostProbAct();
                            mostProbableActivity = OldActivityRepo.myMostProbActBasedOnMaxActCount();
                            Log.i("MyProbActivity new --> ", mostProbableActivity);
                            surveyActivity.add(mostProbableActivity);


                            try {
                                timestamp = myDetectedActivityList.get(myDetectedActivityList.size() - 1).getActivityTimestamp();
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                            Log.i("MyProbable  TStamp -->", String.valueOf(timestamp));


                            // TODO:insert entry  into activityTable
                            *//* query = SELECT * FROM ACTIVITY_TABLE WHERE INTERVAL_ID = IntervalTable.getIntID(timestamp) AND ACTIVITY= mostProbaleActivty and WFIF_ID= wfilist(a)
                            *  if (query==null) then activityTable.insert(mostProbaleActivty, getIntID(timestamp), wifiList.toString, count=1)else
                            *  increment counter value in the row returned y the query
                            *  *//*

                            if (ActivityRepository.mySelectQuery(mostProbableActivity, timestamp, mTempWifiList).get(0) == -1) { //if query return null
                                entry.setActivityType(mostProbableActivity);
                                entry.setIntervalID(IntervalRepository.getIntID(timestamp));
                                entry.setWifiList(formatWifiIdListToString(mTempWifiList));
                                //entry.setCount(1);
                                entry.setCount(mRecordingInterval);
                                // entry.setDataRecordingInterval(mRecordingInterval);
                                ActivityRepository.insert(entry);
                            } else {
                                ActivityRepository.updateCount(ActivityRepository.mySelectQuery(mostProbableActivity,
                                                                                                timestamp,
                                                                                                mTempWifiList).get(1) + mRecordingInterval,
                                                                                                ActivityRepository.mySelectQuery(mostProbableActivity,
                                                                                                timestamp, mTempWifiList).get(0));
                            }

                            intervalIdMax = IntervalRepository.getLastIntervalId();

                            *//*Udating Node table*//*
                           *//* if (mostProbableActivity.equals("")) {
                                NodeRepository.updateNodeTable(mTempWifiList, ActivityType.STILL, (int) intervalIdMax);
                            } else {
                                NodeRepository.updateNodeTable(mTempWifiList, mostProbableActivity, (int) intervalIdMax);

                            }
                            *//**//*Updating Edge table*//**//*
                            EdgeRepository.updateEdgeTable(mTempWifiList, (int) intervalIdMax);*//*


                            myDetectedActivityList.clear();
                            mTempWifiList.clear();
                        } else {

                            try {
                                requestActivityUpdates();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }



                        // survey data
                        if (surveyActivity.size() == 5) {
                            for (String activity : surveyActivity) {
                                switch (activity) {
                                    case ActivityType.STILL:
                                    case "":
                                        surveyData.put("Still", surveyData.get("Still") + 1);
                                        break;
                                    case ActivityType.IN_VEHICLE:
                                        surveyData.put("InVehicle", surveyData.get("InVehicle") + 1);
                                        break;
                                    case ActivityType.WALKING:
                                    case ActivityType.RUNNING:
                                    case ActivityType.ON_FOOT:
                                    case ActivityType.ON_BICYCLE:
                                        surveyData.put("Moving", surveyData.get("Moving") + 1);
                                        break;

                                }


                                    switch (getMapKeyWithHighestValue(surveyData)) {
                                        case "Still":
                                            // send notification if not asked before
                                            if (surveyData.get("Still") >= 4)
                                                sendNotification(NOTIFICATION_ID_STILL, "Still");
                                            break;
                                        case "InVehicle":
                                            // send notification if not asked before
                                            if (surveyData.get("InVehicle") >= 4)
                                                sendNotification(NOTIFICATION_ID_VEHICLE, "Vehicle");
                                            break;
                                        case "Moving":
                                            // send notification if not asked before
                                            if (surveyData.get("Moving") >= 4)
                                                sendNotification(NOTIFICATION_ID_MOTION, "Motion");

                                            break;

                                    }//end switch



                            }//end for

                            surveyActivity.clear();
                            surveyData.put("Still", 0);
                            surveyData.put("InVehicle", 0);
                            surveyData.put("Moving", 0);

                        }// end if  survey data


                        Log.i("OldActTable", oldActivityRepo.getAllActivities().toString());
                        oldActivityRepo.deleteAllTableEntries();

                        //end
                        Log.i("RecordINTVAL", String.valueOf(mRecordingInterval));
                        try {
                            Thread.sleep(mRecordingInterval * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }
            };
            mThread = new Thread(mRunnable);
            mThread.start();
        }//end if

    }//end myperiodicCycle*/

    //method to stop bg service
    public void stopBgService() {
        stopService(new Intent(MyMainService.this, ActivityRecognitionService.class));
    }

    //method to request activity update/ start detection
    public void requestActivityUpdates() {
        Log.i("ActScanIntRU", String.valueOf(mActivityScanInterval));

        //sharedPref.getString()
        Intent intent = new Intent(MyMainService.this, ActivityRecognitionService.class);
        pIntent = PendingIntent.getService(MyMainService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (myApiClient.isConnected()) {
            try {
                ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(myApiClient, mActivityScanInterval, pIntent).setResultCallback(this);
            } catch (Exception e) {
                Log.e(TAG, "Failed to connect to Activity Recognition API");
                e.printStackTrace();
            }
        }
    }

    //method to stop detection
    public void removeActivityUpdates() {
        Intent intent = new Intent(MyMainService.this, ActivityRecognitionService.class);
        pIntent = PendingIntent.getService(MyMainService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (myApiClient.isConnected()) {
            try {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(myApiClient, pIntent).setResultCallback(this);
            } catch (Exception e) {
                Log.e(TAG, "Failed to connect to Activity Recognition API");
                e.printStackTrace();
            }
        }
        Log.i(TAG, "Activity detection stopped");

        stopBgService();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected1");
        Intent intent = new Intent(MyMainService.this, ActivityRecognitionService.class);
        pIntent = PendingIntent.getService(MyMainService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(myApiClient, mActivityScanInterval, pIntent).setResultCallback(this);
        Log.i(TAG, "Connected2");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        myApiClient.connect();
        if (myApiClient.isConnected()) {
            requestActivityUpdates();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e(TAG, "Connection Failed");

        Log.e(TAG, "Connection to Google Activity Recgnition API failed with Error code : " + connectionResult.getErrorCode() + "\n" + connectionResult.getErrorMessage());


    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, "Successfully added activity detection.");

        } else {
            Log.e(TAG, "Error: " + status.getStatusMessage());
        }
    }

    @Override
    public boolean stopService(Intent name) {
       /* SharedPreferences mSharedPref = mContext.getSharedPreferences(String.valueOf(R.string.preference_key_isMainServiceON), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.clear();
        editor.putBoolean("IsMainServiceOn", false);
        editor.commit();

        //cancelAlarm();
        mHandler.removeMessages(0);
        // removeActivityUpdates();
        try {
            // unregisterReceiver(receiver);
            // unregisterReceiver(activityReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error with unregistering receivers!");
            e.printStackTrace();
        }
        Log.i(TAG, "MyMainService Stopped");*/
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        isMainServiceRunning = false;

     /*   // releasing the wakelock
        if (mWakeLock.isHeld())
            mWakeLock.release();*/


        try {
            // removeActivityUpdates();
            //LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
            //LocalBroadcastManager.getInstance(mContext).unregisterReceiver(activityReceiver);
            unregisterReceiver(receiver);
            unregisterReceiver(activityReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error with unregistering receivers2!");
            e.printStackTrace();
        }


        Log.i(TAG, "MyMainService stopping..");

        super.onDestroy();

    }





    /**
     * Show a notification while this service is running.
     */
   /* private void showNotification(int notification_id, String name) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Please complete survey";
        CharSequence title = "Survey Questionnaires";

        // The PendingIntent to launch our activity if the user selects this notification
        Intent mIntent = null;
        switch (name) {
            case "Still":
                mIntent = new Intent(this, StillNotificationActivity.class);
                text = "Please complete Place Survey";
                title = "Place Survey";
                break;
            case "Vehicle":
                mIntent = new Intent(this, VehicleNotificationActivity.class);
                text = "Please complete Transport mode  Survey";
                title = "Transportation mode Survey";
                break;
            case "Motion":
                mIntent = new Intent(this, MovementNotificationActivity.class);
                title = "Motion Activity Survey";
                text = "Please complete Motion Activity Survey";
                break;
        }


        mIntent.putExtra("Time", System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        inboxStyle.setBigContentTitle("Enter Content Text");
        inboxStyle.addLine("hi events ");
        // Set the info for the views that show in the notification panel.
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.cast_ic_notification_0)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(title)  // the label of the entry
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setFullScreenIntent(contentIntent, true)
                    //.setStyle(inboxStyle)
                    .build();
        }
        //todo remember to comment if doesnt solve notification clearing
        //notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        // Send the notification.
        mNM.notify(notification_id, notification);


    }

    private void sendNotification(int notification_id, String surveyType) {
        String keyNotif = "";
        String keyTimeStamp = "";

        switch (surveyType) {
            case "Still":
                keyNotif = KEY_STILL_NUM_NOTIF;
                keyTimeStamp = KEY_STILL_TSTAMP;
                break;
            case "Vehicle":
                keyNotif = KEY_TRANSPORT_NUM_NOTIF;
                keyTimeStamp = KEY_TRANSPORT_TSTAMP;
                break;
            case "Motion":
                keyNotif = KEY_MOTION_NUM_NOTIF;
                keyTimeStamp = KEY_MOTION_TSTAMP;
                break;

        }

        int numOfSentNotification = notificationSharedPref.getInt(keyNotif, 0);

        long currentTstamp = System.currentTimeMillis();
        //long timeStamp = notificationSharedPref.getLong(keyTimeStamp, 0);
        long timeStamp = notificationSharedPref.getLong(keyTimeStamp, currentTstamp - 300 * 1000);
        Log.i("Record notfi", "Tstamp: " + String.valueOf(timeStamp) + " |NotifType: " + keyNotif + " |CurrTime-LastNotifTime: " + (currentTstamp - timeStamp) + " |NumNotif " + numOfSentNotification);

        if (numOfSentNotification == 0 || (numOfSentNotification > 1 && ((currentTstamp - timeStamp) > *//*90 * 1000*//* 330*1000))) {

            boolean shouldRxNotification = sharedPrefRxNotif.getBoolean("checkbox_preference2", true);
            //Log.i("Notif", String.valueOf(shouldRxNotification));
            if (shouldRxNotification == true) { // condition to check if notification is enabled in Settings
                showNotification(notification_id, surveyType);
            }
            notificationSharedPrefEditor.putInt(keyNotif, numOfSentNotification + 1);
            notificationSharedPrefEditor.putLong(keyTimeStamp, currentTstamp);
            notificationSharedPrefEditor.apply();

        } else {
            notificationSharedPrefEditor.putInt(keyNotif, numOfSentNotification + 1);
            notificationSharedPrefEditor.putLong(keyTimeStamp, currentTstamp);
            notificationSharedPrefEditor.apply();
        }
    }

*/


}
