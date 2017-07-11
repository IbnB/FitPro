/*
 *                         SWIPE Android Application
 *                Author: Sébastien FAYE [sebastien.faye@uni.lu]
 *
 * ------------------------------------------------------------------------------
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sébastien FAYE [sebastien.faye@uni.lu], VehicularLab [vehicular-lab@uni.lu]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ------------------------------------------------------------------------------
 */

package lu.uni.ibrahimtahirou.fitpro.detection;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;


/*
    Manage the activity recognition service
    About Android Activity Recognition:
        http://opensignal.com/blog/2013/05/16/getting-started-with-activity-recognition-android-developer-guide/
        https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognition
 */
public class ActivityRecognitionScan implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    //GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener
    //GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>
    private Context context;

    private static PendingIntent callbackIntent;
    //
    private static PendingIntent pIntent;
    private static final String TAG = "ActivityRecognitionScan";
    private static GoogleApiClient myApiClient;

    public ActivityRecognitionScan(Context context) {
        this.context = context;

    }

    public void startActivityRecognitionScan() {
        /*mActivityRecognitionClient = new ActivityRecognitionClient(context, this, this);
        mActivityRecognitionClient.connect();*/

        myApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
        myApiClient.connect();

        Intent intent = new Intent(context, ActivityRecognitionService.class);
        pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (!myApiClient.isConnected()) {
            Toast.makeText(context, "GoogleApiClient not yet connected", Toast.LENGTH_SHORT).show();
        } else {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(myApiClient, 0, pIntent).setResultCallback(this);
        }
    }

    public void stopActivityRecognitionScan() {


        Intent intent = new Intent(context, ActivityRecognitionService.class);
        pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(myApiClient, pIntent).setResultCallback(this);

        ActivityRecognitionService.shouldActivityRecognitionRun = false;
        context.stopService(new Intent(context, ActivityRecognitionService.class));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {



        Log.i(TAG, "Connected1");
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(myApiClient, 0, pIntent).setResultCallback(this);
        Log.i(TAG, "Connected2");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        myApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(context, "Connection Failed", Toast.LENGTH_SHORT).show();

        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.e(TAG, "Successfully added activity detection.");

        } else {
            Log.e(TAG, "Error: " + status.getStatusMessage());
        }
    }


}