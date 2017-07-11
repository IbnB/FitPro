package lu.uni.ibrahimtahirou.fitpro.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import lu.uni.ibrahimtahirou.fitpro.R;
import lu.uni.ibrahimtahirou.fitpro.constants.Constants;
import lu.uni.ibrahimtahirou.fitpro.database.Database;
import lu.uni.ibrahimtahirou.fitpro.detection.ActivityRecognitionService;
import lu.uni.ibrahimtahirou.fitpro.models.RouteModel;
import lu.uni.ibrahimtahirou.fitpro.utils.DataParser;
import lu.uni.ibrahimtahirou.fitpro.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static lu.uni.ibrahimtahirou.fitpro.myservices.MyMainService.convertMillsToHhMmSs;

public class RouteActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker startingLocationMarker, endingLocationMarker;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;

    private CheckBox cbRun, cbWalk, cbBike,cbRest;
    private EditText etRun, etWalk, etBike, etRest, etRouteName;
    private FloatingActionButton fcbDone;
    private int choice;
    private String routeId = "";
    private boolean canInsertRecord = false;

    private ArrayList<Polyline> polyline = new ArrayList<>();

    private final String EXERCISE_RUN = "Run", EXERCISE_WALK = "Walk", EXERCISE_BIKE = "Bike";
    private String distance = "", duration = "";

    private LinearLayout llExercises;
    private TextView tvExercises;
    public static final int ANIMATE_SPEED_TURN = 3000;
    private PolylineOptions lineOptions;
    private int countCheck = 0;
    private boolean shoudDrawLine = true;
    private LatLng walkLatLong;
    private LatLng runLatLong;
    private LatLng bikeLatLong;
    private boolean isFirstCallMade = false;
    private boolean isSecondCallMade = false;
    private String exerciseRunDuration = "0 min";
    private String exerciseWalkDuration = "0 min";
    private String exerciseBikeDuration = "0 min";
    private String exerciseRestDuration = "0 min";
    private String url;
    private String routeName;
    private int backgroundThreadCount=0;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean shouldzoomToCurrentLocation=true;
    private TextView tvActivity;

    private BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Toast.makeText(RouteActivity.this, location.getLatitude() + "" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView tvToolBar = (TextView) toolbar.findViewById(R.id.toolbar_title);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            choice = bundle.getInt(Constants.CHOICE);

            if (choice == Constants.Route.ADD) {
                Utils.Msg.showToastLengthLong(this, "Click on map to choose start and end point.");
                tvToolBar.setText("Add Route");
            } else if (choice == Constants.Route.DETAIL) {
                routeId = bundle.getString(Constants.Route.ID);
                tvToolBar.setText("Route Detail");
            }
        }
        setSupportActionBar(toolbar);

        setBasicViews();

        tvActivity = (TextView) findViewById(R.id.tvActivity);


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //receiving activity detection result
                if (Constants.CUSTOM_ACTION_ACTIVITY_DATA.equals(intent.getAction())) {
                    if (ActivityRecognitionService.shouldActivityRecognitionRun) {
                        // Log.i("actisHandlerRunnig2", String.valueOf(isHandlerRunning));

                        String s = "";
                        s += "Activity: " + intent.getStringExtra("Activity") + " " + "|Confidence: " + intent.getExtras().getInt("Confidence") + "%" + " |Time: " + convertMillsToHhMmSs(intent.getExtras().getLong("TimeStamp")) + "\n";
                        tvActivity.setText(s);

                    } else {
                        tvActivity.setText("Activity detection Service stopped");
                    }
                }
            }
        };

        //intent filter for registering the reciever
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CUSTOM_ACTION_ACTIVITY_DATA);
        //filter.addAction(Constant.ACTION_SCAN_WIFI);
        //filter.addAction(Constants.ACTION_SETTINGS_DATA);
        registerReceiver(receiver, filter);
    }

    private void setBasicViews() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        fcbDone = (FloatingActionButton) findViewById(R.id.fcbDone);

        cbRun = (CheckBox) findViewById(R.id.cbRun);
        cbWalk = (CheckBox) findViewById(R.id.cbWalk);
        cbBike = (CheckBox) findViewById(R.id.cbBike);
        cbRest=(CheckBox) findViewById(R.id.cbRest);

        etRun = (EditText) findViewById(R.id.etRun);
        etWalk = (EditText) findViewById(R.id.etWalk);
        etBike = (EditText) findViewById(R.id.etBike);
        etRest = (EditText) findViewById(R.id.etRest);
        etRouteName = (EditText) findViewById(R.id.etRouteName);

        llExercises = (LinearLayout) findViewById(R.id.llExercies);
        tvExercises = (TextView) findViewById(R.id.tvExercises);

    }


    /**
     * Handle on click listener
     *
     * @param view
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fcbDone:
//                animateMarker();
                Toast.makeText(RouteActivity.this, "Done", Toast.LENGTH_SHORT).show();

                if(choice==Constants.Route.DETAIL)
                {
                    animateMarker();
//                    makeAlert("ogya show");
                }
                else
                {
                    performValidation();
                }
                break;
        }
    }

    private void performValidation() {


        String exerciseRun = "", exerciseWalk = "", exerciseBike = "";


        String exercise = "";
        String exerciseDuration = "";

        /**
         * Check if Activity Run is selected by user then validate time
         * otherwise no worries
         */
        if (cbRun.isChecked()) {
            exerciseRun = EXERCISE_RUN;
            canInsertRecord = true;
//            }
        } else {
            exerciseRun = "";
            exerciseRunDuration = "0 mins";
        }

        /**
         * Check if Activity Walk is selected by user then validate time
         * otherwise no worries
         */
        if (cbWalk.isChecked()) {
            exerciseWalk = EXERCISE_WALK;
            canInsertRecord = true;
//            }
        } else {
            exerciseWalk = "";
            exerciseWalkDuration = "0 mins";
        }

        /**
         * Check if Activity Bike is selected by user then validate time
         * otherwise no worries
         */
        if (cbBike.isChecked()) {
            exerciseBike = EXERCISE_BIKE;
            canInsertRecord = true;
        } else {
            exerciseBike = "";
            exerciseBikeDuration = "0 mins";
        }

        if (cbRest.isChecked()) {
            //exerciseBike = EXERCISE_BIKE;
            //canInsertRecord = true;
            /*Random rand = new Random();
            int  n = rand.nextInt(5) + 1;*/
        } else {
            exerciseBike = "";
            exerciseRestDuration = "0 mins";
        }
        routeName = etRouteName.getText().toString();
        if (routeName.equals("")) {
            Utils.Msg.showToastLengthShort(this, getString(R.string.please_enter_route_name));
        } else {


            if (canInsertRecord) {

                if (countCheck == 2) {
                    if (cbWalk.isChecked() && cbRun.isChecked()) {
                        walkLatLong = getDividedLatLong(true);
                        url = getUrl(startingLocationMarker.getPosition(), walkLatLong, Constants.WALKING);
                        FetchUrl fetchUrl = new FetchUrl();
                        fetchUrl.execute(url);
                        isFirstCallMade = true;

                    } else if (cbWalk.isChecked() && cbBike.isChecked()) {
                        walkLatLong = getDividedLatLong(true);
                        startingLocationMarker.setPosition(walkLatLong);
                        url = getUrl(startingLocationMarker.getPosition(), walkLatLong, Constants.WALKING);
                        FetchUrl fetchUrl = new FetchUrl();
                        fetchUrl.execute(url);
                        isFirstCallMade = true;

                    } else if (cbRun.isChecked() && cbBike.isChecked()) {
                        runLatLong = getDividedLatLong(true);
                        url = getUrl(startingLocationMarker.getPosition(), runLatLong, Constants.TRANSIT);
                        FetchUrl fetchUrl = new FetchUrl();
                        fetchUrl.execute(url);
                        isFirstCallMade = true;
                    }
                    backgroundThreadCount=2;
                } else if (countCheck == 3) {

                    walkLatLong = getDividedLatLong(false);
                    url = getUrl(startingLocationMarker.getPosition(), walkLatLong, Constants.WALKING);
                    FetchUrl fetchUrl = new FetchUrl();
                    fetchUrl.execute(url);
                    isFirstCallMade = true;
                    backgroundThreadCount=3;
                }
                else {
                    if (cbWalk.isChecked()) {

                        url = getUrl(startingLocationMarker.getPosition(), endingLocationMarker.getPosition(), Constants.WALKING);
                        FetchUrl fetchUrl = new FetchUrl();
                        fetchUrl.execute(url);
                    } else if (cbRun.isChecked()) {
                        url = getUrl(startingLocationMarker.getPosition(), endingLocationMarker.getPosition(), Constants.TRANSIT);
                        FetchUrl fetchUrl = new FetchUrl();
                        fetchUrl.execute(url);
                    } else {
                        url = getUrl(startingLocationMarker.getPosition(), endingLocationMarker.getPosition(), Constants.DRIVING);
                        FetchUrl fetchUrl = new FetchUrl();
                        fetchUrl.execute(url);
                    }
                    backgroundThreadCount=1;
                }

            } else {
                Utils.Msg.showToastLengthShort(this, getString(R.string.please_enter_exercise_duration));
            }
        }

    }

    void performApiCall(LatLng origin, LatLng end, String mode) {
        url = getUrl(origin, end, mode);
        FetchUrl fetchUrl = new FetchUrl();
        fetchUrl.execute(url);
    }

    private void animateMarker() {

        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(new LatLng(startingLocationMarker.getPosition().latitude, startingLocationMarker.getPosition().longitude))
                        .bearing(45)
                        .tilt(90)
                        .zoom(mMap.getCameraPosition().zoom)
                        .build();

        mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                ANIMATE_SPEED_TURN,
                new GoogleMap.CancelableCallback() {

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onCancel() {
                    }
                }
        );

    }

    /**
     * Get Location from lat lng
     *
     * @param latLng
     * @return
     */

    private Location convertLatLngToLocation(LatLng latLng) {
        Location location = new Location("someLoc");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    private float bearingBetweenLatLngs(LatLng beginLatLng, LatLng endLatLng) {
        Location beginLocation = convertLatLngToLocation(beginLatLng);
        Location endLocation = convertLatLngToLocation(endLatLng);
        return beginLocation.bearingTo(endLocation);
    }

    /**
     * Handle checkbox state
     *
     * @param compoundButton
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {


        switch (compoundButton.getId()) {
            case R.id.cbRun:
                if (isChecked) {
//                    etRun.setVisibility(View.VISIBLE);
                    countCheck++;
                } else {
//                    etRun.setText("");
//                    etRun.setVisibility(View.GONE);
                    if (countCheck > 0) {
                        countCheck--;
                    }
                }

                break;
            case R.id.cbWalk:
                if (isChecked) {
//                    etWalk.setVisibility(View.VISIBLE);
                    countCheck++;
                } else {
//                    etWalk.setVisibility(View.GONE);
//                    etWalk.setText("");
                    if (countCheck > 0) {
                        countCheck--;
                    }
                }
                break;
            case R.id.cbBike:
                if (isChecked) {
//                    etBike.setVisibility(View.VISIBLE);
                    countCheck++;
                } else {
//                    etBike.setVisibility(View.GONE);
//                    etBike.setText("");
                    if (countCheck > 0) {
                        countCheck--;
                    }
                }
                break;
        }
    }


    /**
     * Manipulates the map once available.;
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                if (choice == Constants.Route.ADD) {
                    mMap.setMyLocationEnabled(true);
                }

            }
        } else {
            buildGoogleApiClient();
            if (choice == Constants.Route.ADD) {
                mMap.setMyLocationEnabled(true);
            }
        }

        handleRouteMode();

        if (choice == Constants.Route.ADD) {
            handleStartAndEndPositionMarkers();
        }
    }

    private void handleStartAndEndPositionMarkers() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (startingLocationMarker == null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Start Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    startingLocationMarker = mMap.addMarker(markerOptions);
                    startingLocationMarker.setDraggable(true);

                } else if (endingLocationMarker == null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("End Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    endingLocationMarker = mMap.addMarker(markerOptions);
                    endingLocationMarker.setDraggable(true);


                    drawRouteLine();

                } else {
                    Utils.Msg.showToastLengthShort(RouteActivity.this, "Drag markers");
                }
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
//                if (marker.getTitle().equals(startingLocationMarker.getTitle())) {
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
//                } else if (marker.getTitle().equals(endingLocationMarker.getTitle())) {
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
//
//                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));


                if (marker.getTitle().equals(startingLocationMarker.getTitle())) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                } else if (marker.getTitle().equals(endingLocationMarker.getTitle())) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }
                drawRouteLine();

            }
        });


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(0.1F); //added
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(choice==Constants.Route.DETAIL)
        {
            if (startingLocationMarker != null) {
                startingLocationMarker.setPosition(latLng);
            }

            if(walkLatLong!=null)
            {
                if(startingLocationMarker.getPosition()==latLng)
                {
                    makeAlert(getString(R.string.walking_msg));
                }
            }
            if (runLatLong!=null)
            {
                if(runLatLong==latLng)
                {
                    makeAlert(getString(R.string.bike_msg));
                }
            }
            if (bikeLatLong!=null)
            {
                if(bikeLatLong==latLng)
                {
                    makeAlert(getString(R.string.bike_msg));
                }
            }
            if(latLng==endingLocationMarker.getPosition())
            {
                makeAlert(getString(R.string.finished_msg));
            }

        }
//        if (startingLocationMarker != null) {


//        //Place current location marker
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        startingLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        
        //// TODO: 7/8/2017 have to place a condition to avoid zoom evertime 
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
//        }

//
//        //Place current location marker
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        //move map camera
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
//
//        Toast.makeText(this, latLng.toString(), Toast.LENGTH_SHORT).show();
//        if (startingLocationMarker != null && endingLocationMarker != null) {
//            startingLocationMarker.setPosition(latLng);
//
//        }


//        //stop location updates
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//        }


    }

    private void makeAlert(final String message)
    {
        AlertDialog.Builder dialog= new AlertDialog.Builder(this)
                .setTitle("Alert !")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if(message.equals(getString(R.string.finished_msg)))
                        {
                            dialogInterface.dismiss();
                            finishActivity();
                        }
                        dialogInterface.dismiss();
                    }
                });
        dialog.show();

    }

   private void finishActivity()
    {
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        registerReceiver(receiver, new IntentFilter(Constants.CUSTOM_ACTION_ACTIVITY_DATA));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }



    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Utils.Msg.showToastLengthShort(this, "Permission is required for maps.");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    private void handleRouteMode() {
        if (choice == Constants.Route.ADD) {
            /**
             * Set all input fields to gone, so we will enable only selected field.
             */
            etWalk.setVisibility(View.GONE);
            etRun.setVisibility(View.GONE);
            etBike.setVisibility(View.GONE);
            fcbDone.setVisibility(View.GONE);


            /**
             * Set click listeners
             */
            cbRun.setOnCheckedChangeListener(this);
            cbWalk.setOnCheckedChangeListener(this);
            cbBike.setOnCheckedChangeListener(this);
            fcbDone.setOnClickListener(this);
        } else if (choice == Constants.Route.DETAIL) {

            RouteModel routeModel = Database.Route.getRecordById(this, routeId);
            llExercises.setVisibility(View.GONE);
            fcbDone.setVisibility(View.VISIBLE);
            fcbDone.setOnClickListener(this);
            etRouteName.setKeyListener(null);
            etRouteName.setFocusable(false);
            etRouteName.setFocusableInTouchMode(true);
            etRouteName.setText("" + routeModel.getRouteName());

            setExerciseReminderLatLng(routeModel);

            /**
             * Add starting marker based on previously saved points
             */
            String[] startPosition = routeModel.getRouteStartingPoint().split(",");
            LatLng startLatLng = new LatLng(Double.parseDouble(startPosition[0]), Double.parseDouble(startPosition[1]));
            MarkerOptions startMarkerOptions = new MarkerOptions();
            startMarkerOptions.position(startLatLng);
            startMarkerOptions.title("Start Position");
            startMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            startingLocationMarker = mMap.addMarker(startMarkerOptions);
            startingLocationMarker.setDraggable(false);


            /**
             * Add ending marker based on previously saved points
             */
            String[] endPosition = routeModel.getRouteEndingPoint().split(",");
            LatLng endLatLng = new LatLng(Double.parseDouble(endPosition[0]), Double.parseDouble(endPosition[1]));
            MarkerOptions endMarkerOptions = new MarkerOptions();
            endMarkerOptions.position(endLatLng);
            endMarkerOptions.title("End Position");
            endMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            endingLocationMarker = mMap.addMarker(endMarkerOptions);
            endingLocationMarker.setDraggable(false);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

            String[] exercise = routeModel.getRouteExercise().split(",");
            String[] exerciseDuration = routeModel.getRouteExerciseDuration().split(",");

            /**
             * may be user has not selected all three exercises so possibility is here we can face
             * indexOutOfBoundException.
             *
             * so don't worry just call drawRouteLine in case of Exception. Lolx ;)
             */
            try {
                String txtToDisplay = "" + exercise[0] + " - " + exerciseDuration[0] + ""
                        + "\n" + exercise[1] + " - " + exerciseDuration[1] + ""
                        + "\n" + exercise[2] + " - " + exerciseDuration[2] + "";
                /**
                 * Possible, that user has not selected any exercise so string will contains comma :D :P
                 */
                tvExercises.setText(txtToDisplay.replaceAll(",", " "));
                drawRouteLine();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                drawRouteLine();
            }


        }
    }

    private void setExerciseReminderLatLng(RouteModel routeModel)
    {
        String walk,run,bike;
        walk=routeModel.getWalkingLatLng();
        run=routeModel.getRunLatLng();
        bike=routeModel.getBikeLatLng();
        String split[]=null;
        try
        {
            if(!walk.isEmpty()&&walk.contains(","))
            {
                split=walk.split(",");
                walkLatLong=new LatLng(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
            }
            if(!run.isEmpty()&&run.contains(","))
            {
                split=run.split(",");
                runLatLong=new LatLng(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
            }
            if(!bike.isEmpty()&&bike.contains(","))
            {
                split=bike.split(",");
                bikeLatLong=new LatLng(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    private void drawRouteLine() {

        /**
         * Refer this link for more information
         * https://www.androidtutorialpoint.com/intermediate/google-maps-draw-path-two-points-using-google-directions-google-map-android-api-v2/
         */


        /**
         * Remove all lines from map and clear list
         */
        for (Polyline line : polyline) {
            line.remove();
        }
        polyline.clear();


        LatLng origin = startingLocationMarker.getPosition();
        LatLng dest = endingLocationMarker.getPosition();

        // Getting URL to the Google Directions API
        String url = getUrl(origin, dest);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String getUrl(LatLng origin, LatLng dest, String mode) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + "mode=" + mode + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);

                distance = parser.getRouteDistance();
                duration = parser.getRouteDuration();
                setDuration(parser.getTravelMode(), duration);

                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

//            if (shoudDrawLine ) { // avoid every time polyline draw

            ArrayList<LatLng> points;
            lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                polyline.add(mMap.addPolyline(lineOptions));
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
//                shoudDrawLine = false;
//            }

            if (choice == Constants.Route.ADD && fcbDone.getVisibility() == View.GONE) {
                fcbDone.setVisibility(View.VISIBLE);
            }

            if (countCheck == 2) {
                if (cbWalk.isChecked() && cbRun.isChecked()) {
                    if (isFirstCallMade) {
                        performApiCall(walkLatLong, endingLocationMarker.getPosition(), Constants.TRANSIT);
                        isFirstCallMade = false;
                    }


                } else if (cbWalk.isChecked() && cbBike.isChecked()) {

                    if (isFirstCallMade) {
                        performApiCall(walkLatLong, endingLocationMarker.getPosition(), Constants.DRIVING);
                        isFirstCallMade = false;

                    }

                } else if (cbRun.isChecked() && cbBike.isChecked()) {
                    if (isFirstCallMade) {
                        performApiCall(runLatLong, endingLocationMarker.getPosition(), Constants.DRIVING);
                        isFirstCallMade = false;

                    }

                }

            }
            if (countCheck == 3) {
                if (isFirstCallMade) {
                    runLatLong = getDividedLatLong(true);
                    url = getUrl(walkLatLong, runLatLong, Constants.TRANSIT);
                    FetchUrl fetchUrl = new FetchUrl();
                    fetchUrl.execute(url);
                    isFirstCallMade = false;
                    isSecondCallMade = true;
                } else if (isSecondCallMade) {
                    performApiCall(runLatLong, endingLocationMarker.getPosition(), Constants.DRIVING);
                    isSecondCallMade = false;

                }

            }

            if (canInsertRecord &&backgroundThreadCount==1 ) {
                insertInDataBase();
            }
            backgroundThreadCount--;
        }
    }

    /**
     * after making all the api calls just save it into  db
     */
    private void insertInDataBase() {
//        String exerciseRun = "", exerciseWalk = "", exerciseBike = "";
        String exercise = "";
        String exerciseDuration = "";

        exercise = EXERCISE_WALK + "," + EXERCISE_RUN + "," + EXERCISE_BIKE;
        exerciseDuration = exerciseWalkDuration + "," + exerciseRunDuration + "," + exerciseBikeDuration;


        String startPoint = startingLocationMarker.getPosition().latitude + "," + startingLocationMarker.getPosition().longitude;
        String endPoint = endingLocationMarker.getPosition().latitude + "," + endingLocationMarker.getPosition().longitude;


        try {
            duration = String.valueOf(Integer.parseInt(exerciseWalkDuration.split(" ")[0])
                    + Integer.parseInt(exerciseRunDuration.split(" ")[0])
                    + Integer.parseInt(exerciseBikeDuration.split(" ")[0]));
        } catch (Exception nex) {
            nex.printStackTrace();
        }

        if (Database.Route.insert(getApplicationContext(), new RouteModel.RouteBuilder()
                .routeName(routeName)
                .routeDistance(distance + " meters")
                .routeTime(duration + " mins")
                .routeExercise(exercise)
                .routeExerciseDuration(exerciseDuration)
                .routeStartingPoint("" + startPoint)
                .routeEndingPoint("" + endPoint)
                .walkingDuration(exerciseWalkDuration)
                .walkingLatLng(String.valueOf(walkLatLong))
                .runLatLng(String.valueOf(runLatLong))
                .runDuration(exerciseRunDuration)
                .bikeLatLng(String.valueOf(bikeLatLong))
                .bikeDuration(exerciseBikeDuration)
                .build())) {
            Utils.Msg.showToastLengthShort(getApplicationContext(), "Route added successfully.");
            finish();
        } else {
            Utils.Msg.showToastLengthShort(getApplicationContext(), "Unable to add route.");
        }


    }

    /**
     * set the each travel duration for the exercise plan
     *
     * @param travelMode
     * @param duration
     */
    private void setDuration(String travelMode, String duration) {

        if (canInsertRecord) {
            if (url.contains(Constants.WALKING)) {
                exerciseWalkDuration = duration;
            } else if (url.contains(Constants.DRIVING)) {
                exerciseBikeDuration = duration;
            } else if (url.contains(Constants.TRANSIT)) {
                exerciseRunDuration = duration;
            }
        }

    }

    /**
     * To get the divided point on polyline as a lat long
     *
     * @param isMid
     * @return
     */
    private LatLng getDividedLatLong(boolean isMid) {
        double lat = 0, lng = 0;

        int n = (isMid) ? lineOptions.getPoints().size() : lineOptions.getPoints().size() / 2;
        for (int i = 0; i < n; i++) {
            lat += lineOptions.getPoints().get(i).latitude;
            lng += lineOptions.getPoints().get(i).longitude;
        }

        return new LatLng(lat / n, lng / n);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
           unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
