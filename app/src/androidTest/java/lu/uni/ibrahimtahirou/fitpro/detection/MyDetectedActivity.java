package lu.uni.ibrahimtahirou.fitpro.detection;

/**
 * Created by ibrahimtahirou on 8/24/16.
 */
public class MyDetectedActivity {

    private String activityName;
    private int activityConfidence;
    private Long activityTimestamp;

    public MyDetectedActivity() {
    }

    public MyDetectedActivity(String activityName, int activityConfidence, Long activityTimestamp) {
        this.activityName = activityName;
        this.activityConfidence = activityConfidence;
        this.activityTimestamp = activityTimestamp;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public int getActivityConfidence() {
        return activityConfidence;
    }

    public void setActivityConfidence(int activityConfidence) {
        this.activityConfidence = activityConfidence;
    }

    public Long getActivityTimestamp() {
        return activityTimestamp;
    }

    public void setActivityTimestamp(Long activityTimestamp) {
        this.activityTimestamp = activityTimestamp;
    }

    @Override
    public String toString() {
        return "MyDetectedActivity{" +
                "activityName='" + activityName + '\'' +
                ", activityConfidence=" + activityConfidence +
                ", activityTimestamp=" + activityTimestamp +
                '}';
    }
}
