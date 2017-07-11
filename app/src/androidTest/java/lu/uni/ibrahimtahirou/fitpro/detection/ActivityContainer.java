package lu.uni.ibrahimtahirou.fitpro.detection;

/**
 * Created by ibrahimtahirou on 8/24/16.
 */


public class ActivityContainer {
    MyActivity myActivity;

    public ActivityContainer() {
        myActivity = new MyActivity();
    }

    @Override
    public String toString() {
        return "ActivityContainer [myActivity=" + myActivity + "]";
    }

    public class MyActivity {

        private String name;
        private int count;
        private int confidence;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getConfidence() {
            return confidence;
        }

        public void setConfidence(int confidence) {
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return "MyActivity [name=" + name + ", count=" + count + ", confidence=" + confidence + "]";
        }


    }







        /*public static void main(String... args) {

            ActivityContainer container = new ActivityContainer();
            container.myActivity.setName("Still");
            container.myActivity.setCount(2);
            container.myActivity.setConfidence(75);
            System.out.println(container);
            container.myActivity.setName("Running");
            container.myActivity.setCount(4);
            container.myActivity.setConfidence(95);
            System.out.println(container);

        }*/

}



