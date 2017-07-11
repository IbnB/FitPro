package lu.uni.ibrahimtahirou.fitpro.detection;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ibrahimtahirou on 8/18/16.
 */
public class ProbableActivityDetector {


    public static List<String> getMostProbableActivity(List<MyDetectedActivity> myDetectedActivityList) {


        List<String> result = new ArrayList<>();
        int stillCount = 0;
        int onFootCount = 0;
        int inVehicleCount = 0;
        int runningCount = 0;
        int onBicycleCount = 0;
        int unknownCount = 0;
        int tiltingCount = 0;
        int walkingCount = 0;

        int stillConfidence = 0;
        int onFootConfidence = 0;
        int inVehicleConfidence = 0;
        int runningConfidence = 0;
        int onBicycleConfidence = 0;
        int unknownConfidence = 0;
        int tiltingConfidence = 0;
        int walkingConfidence = 0;


        for (MyDetectedActivity activity : myDetectedActivityList) {
            switch (activity.getActivityName()) {
                case ActivityType.STILL:
                    stillCount++;
                    stillConfidence += activity.getActivityConfidence();
                    //int stillConfidenceAvg = stillConfidence/stillCount;
                    break;
                case ActivityType.ON_FOOT:
                    onFootCount++;
                    onFootConfidence += activity.getActivityConfidence();
                    break;
                case ActivityType.WALKING:
                    walkingCount++;
                    walkingConfidence += activity.getActivityConfidence();
                case ActivityType.RUNNING:
                    runningCount++;
                    runningConfidence += activity.getActivityConfidence();
                case ActivityType.IN_VEHICLE:
                    inVehicleCount++;
                    inVehicleConfidence += activity.getActivityConfidence();
                    break;
                case ActivityType.ON_BICYCLE:
                    onBicycleCount++;
                    onBicycleConfidence += activity.getActivityConfidence();
                case ActivityType.TILTING:
                    tiltingCount++;
                    tiltingConfidence += activity.getActivityConfidence();
                    break;
                case ActivityType.UNKNOWN:
                    unknownCount++;
                    unknownConfidence += activity.getActivityConfidence();
            }
        }

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put(ActivityType.STILL, stillCount);
        map.put(ActivityType.ON_FOOT, onFootCount);
        map.put(ActivityType.RUNNING, runningCount);
        map.put(ActivityType.IN_VEHICLE, inVehicleCount);
        map.put(ActivityType.ON_BICYCLE, onBicycleCount);
        map.put(ActivityType.WALKING, walkingCount);
        map.put(ActivityType.UNKNOWN, unknownCount);
        map.put(ActivityType.TILTING, tiltingCount);

        Log.i("Map", map.toString());
        int maxValueInMap = (Collections.max(map.values()));// This will return max value in the Hashmap
        for (Map.Entry<String, Integer> entry : map.entrySet()) {  // Itrate through hashmap
            if (entry.getValue() == maxValueInMap) {
                Log.i("ProbActDetectorClass", entry.getKey());
                result.add(entry.getKey());     // add the key with max value to the list


            }

            // TODO: 8/18/16  : Remember to handle the situation where there is tie between activity counts

        }

        return result;
    }

}