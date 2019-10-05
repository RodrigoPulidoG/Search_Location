package repos.rodpro.com.searchlocationapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrinterId;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "SEARCH_LOC_MAIN";
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 200;
    private static final long UPDATE_INTERVAL = 4*1000;
    private static final long FAST_INTERVAL = 2*1000;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();
        requestLocationPermission();
    }

    private void setUpViews() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        /*if (isLocationPermissionGranted()){
            startLocationUpdates();
        }else {
            requestLocationPermission();
        }*/
    }

    private void requestLocationPermission() {
        Log.d(TAG, "requestLocationPermission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        Log.d(TAG,"startLocationUpdates");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FAST_INTERVAL);

        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                Log.d(TAG,"startLocationUpdates, LocationResult: " +locationResult);
                if (locationResult!=null){
                    showLocation(locationResult);
                }
            }
        },getMainLooper());
    }

    private void showLocation(LocationResult locationResult) {
        TextView tvMessage = findViewById(R.id.update_message);
        tvMessage.setText("The location will be updated every "+ UPDATE_INTERVAL/1000 + " seconds.");
        TextView tvLocationLat = findViewById(R.id.current_location_lat);
        TextView tvLocationLon = findViewById(R.id.current_location_lon);
        double lat = locationResult.getLastLocation().getLatitude();
        double lon = locationResult.getLastLocation().getLongitude();
        tvLocationLat.setText(String.valueOf(lat));
        tvLocationLon.setText(String.valueOf(lon));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.w(TAG,"onRequestPermissionsResult");
        switch (requestCode){
            case PERMISSION_LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationUpdates();
                }else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                        requestLocationPermission();
                    }else {
                        showPermissionsDialog();
                    }
                }
                break;
        }
    }

    private void showPermissionsDialog() {
        new AlertDialog.Builder(MainActivity.this).
                setMessage("Permissions necessary to use tha App").
                setPositiveButton("Go!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        }).create().show();
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

}
