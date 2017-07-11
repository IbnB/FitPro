package lu.uni.ibrahimtahirou.fitpro.detection;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ActivityRecognitionService extends IntentService {
    private Context mContext;
    private String TAG = this.getClass().getSimpleName();
    public static volatile boolean shouldActivityRecognitionRun = true;


    public ActivityRecognitionService() {
        super("My Activity Recognition Service");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            DetectedActivity mMostProbableActivity = result.getMostProbableActivity();
            if (mMostProbableActivity.getType() == DetectedActivity.ON_FOOT) {
                DetectedActivity preciseActivity = walkingOrRunning(result.getProbableActivities());
                if (preciseActivity != null)
                    mMostProbableActivity = preciseActivity;

            }

            Log.i(TAG, getType(mMostProbableActivity.getType()) + "\t" + mMostProbableActivity.getConfidence());
    
    
                /*Intent i = new Intent("lu.uni.ibrahimtahirou.fitpro.ACTIVITY_RECOGNITION_DATA");
                i.putExtra("Activity", getType(result.getMostProbableActivity().getType()));
                i.putExtra("Confidence", result.getMostProbableActivity().getConfidence());
                i.putExtra("TimeStamp", result.getTime());*/
            Intent i = new Intent("lu.uni.ibrahimtahirou.fitpro.ACTIVITY_RECOGNITION_DATA");
            i.putExtra("Activity", getType(mMostProbableActivity.getType()));
            i.putExtra("Confidence", mMostProbableActivity.getConfidence());
            i.putExtra("TimeStamp", result.getTime());

            // LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
            sendBroadcast(i);
            Log.i(TAG, "Broadcast Sent");
        } else {
            Log.i(TAG, "Activity Recoginition has no result");

        }

        if (shouldActivityRecognitionRun == false) {

            stopSelf();
            return;
        }


    }

    //http://stackoverflow.com/questions/24818517/activity-recognition-api?rq=1
    private DetectedActivity walkingOrRunning(List<DetectedActivity> probableActivities) {
        DetectedActivity myActivity = null;

        int confidence = 0;
        for (DetectedActivity activity : probableActivities) {
            if (activity.getType() != DetectedActivity.RUNNING && activity.getType() != DetectedActivity.WALKING)
                continue;

            if (activity.getConfidence() >= confidence) {
                confidence = activity.getConfidence();
                myActivity = activity;
            }
        }


        return myActivity;
    }


    private String getType(int type) {

        if (type == DetectedActivity.IN_VEHICLE)
            return ActivityType.IN_VEHICLE;
        else if (type == DetectedActivity.ON_BICYCLE)
            return ActivityType.ON_BICYCLE;
        else if (type == DetectedActivity.RUNNING)
            return ActivityType.RUNNING;
        else if (type == DetectedActivity.WALKING)
            return ActivityType.WALKING;
        else if (type == DetectedActivity.STILL)
            return ActivityType.STILL; //The device is still (not moving).
        else if (type == DetectedActivity.ON_FOOT)
            return ActivityType.ON_FOOT; //The device is on a user who is walking or running.
        else if (type == DetectedActivity.UNKNOWN)
            return ActivityType.UNKNOWN;
        else if (type == DetectedActivity.TILTING)
            return ActivityType.TILTING; //The device angle relative to gravity changed significantly.
        else
            return "Unknown";
    }

    public String getMapKeyWithHighestValue(HashMap<String, Integer> map) {
        String keyWithHighestVal = "";

        // getting the max value in the Hashmap
        int maxValueInMap = (Collections.max(map.values()));

        //iterate through the map to get the key that corresponds to the max value in the Hashmap
        for (Map.Entry<String, Integer> entry : map.entrySet()) {  // Iterate through hashmap
            if (entry.getValue() == maxValueInMap) {

                keyWithHighestVal = entry.getKey();     // this is the key which has the max value
            }

        }
        return keyWithHighestVal;
    }


    @Override
    public boolean stopService(Intent name) {
        stopSelf();
        return super.stopService(name);
    }
}//[End ActivityRecognitionService Class]
