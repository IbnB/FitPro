package lu.uni.ibrahimtahirou.fitpro.myfragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.RadarChart;
import com.google.android.gms.common.GoogleApiAvailability;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import lu.uni.ibrahimtahirou.fitpro.Constant;
import lu.uni.ibrahimtahirou.fitpro.R;

/**
 * Created by ibrahimtahirou on 9/10/16.
 * credit to Ratan
 */
public class ActivityProfileFragment extends Fragment /*implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>*/ {
    private final int SHOW_PREFERENCES = 0;

    View mFragmentView;
    //TextView textView;
    //Button button3;

    private static final String TAG = "InsideFragment1";
    private static final int REQUEST_FINE_LOCATION = 0;
    private static final int REQUEST_WRITE_STORAGE = 112;
    Button viewAllButton;
    Button clearDBButton;
    ToggleButton toggleButton;

    RadarChart chart;

    FloatingActionButton fabPlus;
    FloatingActionButton fabViewDB;
    FloatingActionButton fabSendEmail;
    FloatingActionButton fabViewActTable;
    Animation animFabOpen, animFabClose, animFabRotateClockwise, animFabRotateAntiClockwise;
    private Boolean isFabOpen = false;


    private BroadcastReceiver receiver;
    private TextView tvActivity;

    private volatile int maxRangeSeekbarValue;
    private volatile int minRangeSeekbarValue;
    public volatile int minSeekValTemp;

    FrameLayout mFrameLayout;
    public static volatile boolean isHandlerRunning = true;
    public static volatile int mRecordingInterval;
    SharedPreferences mSettingsSharedPref;
    public static volatile boolean isDebug = false;
    SharedPreferences sharedPref;
    TextView tvSeekbarMinDate;
    TextView tvSeekbarMaxDate;

    RangeSeekBar mRangeSeekBar;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      /*  //Setting the data recording interval if not in sharedPreference ,then defaul is 60
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String dataRInterval = sharedPref.getString("data_record_pref", "60");
        Log.i("dataRecordInt", dataRInterval);
        mRecordingInterval = Integer.valueOf(dataRInterval.replaceAll("[^0-9]", ""));
*/


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mFragmentView = inflater.inflate(R.layout.activity_profile_layout, null);
        chart = (RadarChart) mFragmentView.findViewById(R.id.chart);


        //Check Google play service availability
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int apiAvailabilityStatusCode = googleApiAvailability.isGooglePlayServicesAvailable(getActivity());
        switch (apiAvailabilityStatusCode) {
            case 0:


                break;
            default:
                //PendingIntent pItentApiConnect = googleApiAvailability.getErrorResolutionPendingIntent(getActivity(), apiAvailabilityStatusCode, MyMainService.REQUEST_CODE_GOOGLE_PLAYER_SERVICE);
                // Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                googleApiAvailability.getErrorDialog(getActivity(), apiAvailabilityStatusCode, 0).show();

        }

        // fab buttons
        fabPlus = (FloatingActionButton) mFragmentView.findViewById(R.id.fab_plus);
        fabViewDB = (FloatingActionButton) mFragmentView.findViewById(R.id.fab_view_db);
        fabSendEmail = (FloatingActionButton) mFragmentView.findViewById(R.id.fab_clear_db);
        fabViewActTable = (FloatingActionButton) mFragmentView.findViewById(R.id.fab_activity_table);

        //fab animation
        animFabOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        animFabClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);
        animFabRotateClockwise = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_clockwise);
        animFabRotateAntiClockwise = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_anticlockwise);


        mSettingsSharedPref = getActivity().getSharedPreferences("checkbox_preference1", 0);

        fabPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isFabOpen) {

                    fabViewDB.startAnimation(animFabClose);
                    fabViewDB.setClickable(false);

                    fabViewActTable.startAnimation(animFabClose);
                    fabViewActTable.setClickable(false);

                    fabSendEmail.startAnimation(animFabClose);
                    fabSendEmail.setClickable(false);

                    fabPlus.startAnimation(animFabRotateAntiClockwise);

                    isFabOpen = false;
                } else {
                    fabViewDB.startAnimation(animFabOpen);
                    fabViewDB.setClickable(true);

                    fabSendEmail.startAnimation(animFabOpen);
                    fabSendEmail.setClickable(true);
                    fabViewActTable.startAnimation(animFabOpen);
                    fabViewActTable.setClickable(true);

                    fabPlus.startAnimation(animFabRotateClockwise);
                    isFabOpen = true;

                }

            }
        });

        fabViewDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myToast("view ALL DB");

            }
        });

        fabViewActTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myToast("view ALL DB");

            }
        });
        fabSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                switch (getExternalStorageState()) {
                    case NOT_AVAILABLE:
                        myToast("External Storage not Available");

                    case WRITEABLE:
                        myToast("External Storage Available");


                        break;
                    case READ_ONLY:
                        myToast("Read Only Storage Available");
                        break;
                }


            }
        });


        //********End Fab******


        //************start RangeSeekBar****************

        tvSeekbarMinDate = (TextView) mFragmentView.findViewById(R.id.tvSeekBarVal);
        tvSeekbarMaxDate = (TextView) mFragmentView.findViewById(R.id.tvSeekbarMaxDate);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isServiceON = sharedPref.getBoolean("startStopService", true);
        Log.i("Wi-Fi IsON-->", String.valueOf(isServiceON));
        if (isServiceON == false) {
            AlertDialog.Builder altdial = new AlertDialog.Builder(getActivity());
            altdial.setMessage("Scanning Service off please enabled it").setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do somthing

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                        }
                    });

            AlertDialog alert = altdial.create();
            alert.setTitle("Start Scanning Service");
            alert.show();

        }

        //**************

        //starting wifi scan service
        //startWifiSanService();


        mFrameLayout = (FrameLayout) mFragmentView.findViewById(R.id.fragment1_layout);


        tvActivity = (TextView) mFragmentView.findViewById(R.id.tvActivity);
        viewAllButton = (Button) mFragmentView.findViewById(R.id.viewAllButton);
        toggleButton = (ToggleButton) mFragmentView.findViewById(R.id.toggleButton);

        clearDBButton = (Button) mFragmentView.findViewById(R.id.clearDBbutton);


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //receiving activity detection result
                if (Constant.CUSTOM_ACTION_ACTIVITY_DATA.equals(intent.getAction())) {


                } else {
                    tvActivity.setText("Activity detection Service stopped");
                }
            }


        };

        //intent filter for registering the reciever
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.CUSTOM_ACTION_ACTIVITY_DATA);
        //filter.addAction(Constant.ACTION_SCAN_WIFI);
        filter.addAction(Constant.ACTION_SETTINGS_DATA);
        getActivity().registerReceiver(receiver, filter);


        readAndShowValues();
        return mFragmentView;

    }//[End fragment onCreateView()]


    //method to convert milliseconds to HH:mm:ss:SSS format
    //@TargetApi(Build.VERSION_CODES.N)
    public String millsToDateFormat(long mills) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {

            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(mills),
                    TimeUnit.MILLISECONDS.toMinutes(mills) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(mills) % TimeUnit.MINUTES.toSeconds(1));


            return hms;
        } else {
            Date date = new Date(mills + 7200000);
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String dateFormatted = formatter.format(date);
            return dateFormatted;

        }

    }


    /* method to create   alert dialogbox to show the db content on screen*/
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


    @Override
    public void onStart() {
        super.onStart();
        //myApiClient.connect();
    }


    private boolean mayRequestLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar snackbar = Snackbar.make(mFrameLayout, "Please Enable Location Service", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                        }
                    });
            snackbar.getView();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The requested permission is granted.
                    Log.i("Permission", "Granted");
                } else {
                    // The user denied the requested permission.
                    Log.i("Permission", "Denied");
                    try {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features what required the permission
                    Log.i(" Write Permission", "External Write to SD permission Granted");
                } else {
                    Toast.makeText(getActivity(), "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                }
            }

        }
    }


    private boolean mayRequestWriteToSDPermision() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            /*Snackbar snackbar = Snackbar.make(mFrameLayout, "Please Grant Permission to Export activity Data", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                        }
                    });*/

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Permission to access the SD-CARD is required for this app to export your activity Data.")
                    .setTitle("Permission required");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    Log.i(TAG, "Clicked");
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
        return false;
    }


    //*************Before integration*****
    /* read values from shared preferences, display values in text view*/
    public void readAndShowValues() {
        // store settings
        String string = "";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());


        // first checkbox
        boolean checked1 = sharedPref.getBoolean("checkbox_preference1", false);
        if (checked1) {
            string += "checkbox 1 checked\n";
        } else {
            string += "checkbox 1 not checked\n";
        }

        // second checkbox
        boolean checked2 = sharedPref.getBoolean("checkbox_preference2", false);
        if (checked2) {
            string += "checkbox 2 checked\n";
        } else {
            string += "checkbox 2 not checked\n";
        }

        // switch preference
        boolean swtichPref = sharedPref.getBoolean("startStopService", true);
        if (swtichPref) {
            string += "switch is ON\n";
        } else {
            string += "switch is OFF\n";
        }

        // list, get country
        String country = sharedPref.getString("activity_scan_Interval_pref", "");
        string += "List : " + country + "\n";

        // Wifi scan interval, get country
        String wifiScanInval = sharedPref.getString("wifi_scan_intval_pref", "");
        string += "Wifi Scan Intval : " + wifiScanInval + "\n";

        // Data recording  interval, get country
        String dataRcrdingIntval = sharedPref.getString("data_record_pref", "");
        string += "Data Recording Intval: " + dataRcrdingIntval + "\n";
        // edit text
        String text = sharedPref.getString("edittext_preference", "");
        string += "EditText : " + text;

        // show values
        Log.i("SettingsData", string);
        // textView.setText(string);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        readAndShowValues();
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);

    }

    @SuppressWarnings("resource")


    //because the External Storage might not be available you will need to determine the state of it
    // before you perform any operation, otherwise your app will crash...
    //http://stackoverflow.com/questions/26579869/how-can-i-let-users-access-the-internal-storage-directory-of-my-app
    public enum StorageState {
        NOT_AVAILABLE, WRITEABLE, READ_ONLY
    }

    public static StorageState getExternalStorageState() {
        StorageState result = StorageState.NOT_AVAILABLE;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return StorageState.WRITEABLE;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return StorageState.READ_ONLY;
        }

        return result;
    }


    public void myToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

}//end main class
