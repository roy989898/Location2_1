package pom2.poly.com.location2_1;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {


    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;
    private Button removeButton;
    private Button requestButton;
    private TextView detected_activity_textView;
    private ActivityDetectionBroadcastReciver mActivityDetectionBroadcastReciver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        removeButton = (Button) findViewById(R.id.remove_activity_update_button);
        requestButton = (Button) findViewById(R.id.request_activity_update_button);
        detected_activity_textView = (TextView) findViewById(R.id.detected_activity_textView);
        mActivityDetectionBroadcastReciver = new ActivityDetectionBroadcastReciver();
        mGoogleApiClient = buildGoogleAPIClient();

    }

    private GoogleApiClient buildGoogleAPIClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
        return mGoogleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }



    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {


    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.BRODCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mActivityDetectionBroadcastReciver, intentFilter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityDetectionBroadcastReciver);
        super.onPause();

    }

    public void requestActivityUpdate(View view) {
        if (mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 1000, getActivityDetectionPendinhIntent()).setResultCallback(this);
            requestButton.setEnabled(false);
            requestButton.setEnabled(true);


        } else {
            Toast.makeText(this, "not yet Connect", Toast.LENGTH_SHORT).show();
        }





    }

    private PendingIntent getActivityDetectionPendinhIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


    }

    public void removetActivityUpdate(View view) {

        if (mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getActivityDetectionPendinhIntent()).setResultCallback(this);
            requestButton.setEnabled(true);
            requestButton.setEnabled(false);
        } else {
            Toast.makeText(this, "not yet Connect", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
        super.onStop();
    }

    private String getActivityString(int code) {
        switch (code) {
            case DetectedActivity.IN_VEHICLE:

                return "IN_VEHICLE";

            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";

            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";

            case DetectedActivity.RUNNING:
                return "RUNNING";

            case DetectedActivity.STILL:
                return "STILL";

            case DetectedActivity.TILTING:
                return "TILTING";

            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";

            case DetectedActivity.WALKING:
                return "WALKING";

        }
        return "";
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.e("onResult", "success add activity detection");
        } else {
            Log.e("onResult", "Error adding or remove activity detection ");
        }
    }

    protected class ActivityDetectionBroadcastReciver extends BroadcastReceiver {
        private static final String TAG = "ActivityDetectionBroadcastReciver";

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> activityLiss = intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
            String startStatus = "";
            for (DetectedActivity acticity : activityLiss) {
                startStatus = startStatus + getActivityString(acticity.getType()) + " " + acticity.getConfidence() + " %" + "%\n";
            }
            detected_activity_textView.setText(startStatus);

        }
    }
}
