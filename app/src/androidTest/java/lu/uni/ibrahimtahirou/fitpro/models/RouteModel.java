package lu.uni.ibrahimtahirou.fitpro.models;


public class RouteModel {

    private String routeId = "";
    private String routeName = "";
    private String routeDistance = "";
    private String routeTimeDuration = "";
    private String routeExercise = "";
    private String routeExerciseDuration = "";
    private String routeStartingPoint = "";
    private String routeEndingPoint = "";
    private String walkingLatLng="";
    private String runLatLng="";
    private String bikeLatLng="";
    private String walkingDuration="";
    private String runDuration="";
    private String bikeDuration="";


    public String getWalkingDuration() {
        return walkingDuration;
    }

    public String getRunDuration() {
        return runDuration;
    }

    public String getBikeDuration() {
        return bikeDuration;
    }

    public String getWalkingLatLng() {
        return walkingLatLng;
    }

    public String getRunLatLng() {
        return runLatLng;
    }

    public String getBikeLatLng() {
        return bikeLatLng;
    }

    public String getRouteStartingPoint() {
        return routeStartingPoint;
    }

    public String getRouteEndingPoint() {
        return routeEndingPoint;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getRouteDistance() {
        return routeDistance;
    }

    public String getRouteTimeDuration() {
        return routeTimeDuration;
    }

    public String getRouteExercise() {
        return routeExercise;
    }

    public String getRouteExerciseDuration() {
        return routeExerciseDuration;
    }

    /**
     * RouteBuilder Constructor
     *
     * @param routeBuilder
     */
    private RouteModel(RouteBuilder routeBuilder) {
        routeId = routeBuilder.routeId;
        routeName = routeBuilder.routeName;
        routeDistance = routeBuilder.routeDistance;
        routeTimeDuration = routeBuilder.routeTimeDuration;
        routeExercise = routeBuilder.routeExercise;
        routeExerciseDuration = routeBuilder.routeExerciseDuration;
        routeStartingPoint = routeBuilder.routeStartingPoint;
        routeEndingPoint = routeBuilder.routeEndingPoint;
        walkingLatLng=routeBuilder.walkingLatLng;
        runLatLng=routeBuilder.runLatLng;
        bikeLatLng=routeBuilder.bikeLatLng;


    }


    /**
     * Builder class
     */
    public static class RouteBuilder {
        private String routeId = "";
        private String routeName = "";
        private String routeDistance = "";
        private String routeTimeDuration = "";
        private String routeExercise = "";
        private String routeExerciseDuration = "";
        private String routeStartingPoint = "";
        private String routeEndingPoint = "";
        private String walkingLatLng="";
        private String runLatLng="";
        private String bikeLatLng="";
        private String walkingDuration="";
        private String runDuration="";
        private String bikeDuration="";

        public RouteBuilder routeId(String routeId) {
            this.routeId = routeId;
            return this;
        }
        public RouteBuilder routeName(String routeName) {
            this.routeName = routeName;
            return this;
        }

        public RouteBuilder routeDistance(String routeDistance) {
            this.routeDistance = routeDistance;
            return this;
        }

        public RouteBuilder routeTime(String routeTime) {
            this.routeTimeDuration = routeTime;
            return this;
        }

        public RouteBuilder routeExercise(String routeExercise) {
            this.routeExercise = routeExercise;
            return this;
        }

        public RouteBuilder routeExerciseDuration(String routeExerciseDuration) {
            this.routeExerciseDuration = routeExerciseDuration;
            return this;
        }

        public RouteBuilder routeStartingPoint(String routeStartingPoint) {
            this.routeStartingPoint = routeStartingPoint;
            return this;
        }

        public RouteBuilder routeEndingPoint(String routeEndingPoint) {
            this.routeEndingPoint = routeEndingPoint;
            return this;
        }

        public RouteBuilder walkingLatLng(String walkingLatlng) {
            this.walkingLatLng = walkingLatlng;
            return this;
        }

        public RouteBuilder runLatLng(String runLatLng) {
            this.runLatLng = runLatLng;
            return this;
        }
        public RouteBuilder bikeLatLng(String bikeLatLng) {
            this.bikeLatLng = bikeLatLng;
            return this;
        }

        public RouteBuilder walkingDuration(String walkingDuration) {
            this.walkingDuration = walkingDuration;
            return this;
        }

        public RouteBuilder runDuration(String runDuration) {
            this.runDuration = runDuration;
            return this;
        }

        public RouteBuilder bikeDuration(String bikeDuration) {
            this.bikeDuration = bikeDuration;
            return this;
        }
        public RouteModel build() {
            return new RouteModel(this);
        }
    }


}
