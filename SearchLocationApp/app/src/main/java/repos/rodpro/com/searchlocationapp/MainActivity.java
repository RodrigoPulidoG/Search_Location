package repos.rodpro.com.searchlocationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "SEARCH_LOC_MAIN";
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 200;
    private static final long UPDATE_INTERVAL = 4*1000;
    private static final long FAST_INTERVAL = 2*1000;
    private static final long UPDATE_LOCATION_TIME = 3*1000;
    private static final long UPDATE_LOCATION_DISTANCE = 5;
    private static LocationManager locationManager;
    private Location currentLocationFused;
    private Location currentLocationProvider;
    private String provider;
    private Criteria criteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setLocationManager();
        requestLocationPermission();
    }

    private void setLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
    }


    @Override
    protected void onStart() {
        super.onStart();
        /*if (isLocationPermissionGranted()){
            checkProviderLocation();
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
            checkProviderLocation();
        }
    }

    private void checkProviderLocation() {
        Log.d(TAG, "checkProviderLocation");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        if (isLocationEnabled()){
            provider = locationManager.getBestProvider(criteria, true);
            currentLocationFused = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, "checkProviderLocation, Provider: " + provider);
            if (currentLocationProvider == null){
                locationManager.requestLocationUpdates(provider, UPDATE_LOCATION_TIME ,UPDATE_LOCATION_DISTANCE, locationListener);
                Log.d(TAG, "checkProviderLocation, Location Listener");
            }else {
                showProviderLocation();
            }
        }else {
            alertLocationInactive();
        }
    }

    private void alertLocationInactive() {
        new AlertDialog.Builder(MainActivity.this).
                setMessage("Allow access to device location.").
                setPositiveButton("Go!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        }).create().show();
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
                    showFusedLocation(locationResult);
                }
            }
        },getMainLooper());
    }

    private void showFusedLocation(LocationResult locationResult) {
        TextView tvMessage = findViewById(R.id.update_message);
        tvMessage.setText("The location will be updated every "+ UPDATE_INTERVAL/1000 + " seconds.");
        TextView tvLocationLat = findViewById(R.id.current_location_lat);
        TextView tvLocationLon = findViewById(R.id.current_location_lon);
        double lat = locationResult.getLastLocation().getLatitude();
        double lon = locationResult.getLastLocation().getLongitude();
        tvLocationLat.setText(String.valueOf(lat));
        tvLocationLon.setText(String.valueOf(lon));
    }

    private void showProviderLocation() {
        Log.d(TAG,"showProviderLocation, Current location: " + currentLocationProvider);
        TextView tvProviderMessage = findViewById(R.id.update_message_provider);
        tvProviderMessage.setText("The location will be updated minimum every "+ UPDATE_LOCATION_TIME/1000
                + " seconds and minimum each " + UPDATE_LOCATION_DISTANCE + " mts.");
        TextView tvLocationProviderLat = findViewById(R.id.current_location_lat_gps);
        TextView tvLocationProviderLon = findViewById(R.id.current_location_lon_gps);
        if (currentLocationProvider!=null){
            double lat = currentLocationProvider.getLatitude();
            double lon = currentLocationProvider.getLongitude();
            tvLocationProviderLat.setText(String.valueOf(lat));
            tvLocationProviderLon.setText(String.valueOf(lon));
        }else {
            checkProviderLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.w(TAG,"onRequestPermissionsResult");
        switch (requestCode){
            case PERMISSION_LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationUpdates();
                    checkProviderLocation();
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

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "LocationListener, my location: " + location);
            currentLocationProvider = location;
            if (location != null){
                showProviderLocation();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.e(TAG, "onStatusChanged, Status: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "onProviderEnabled, GPS Enabled: " + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.e(TAG, "locationListenerNormal: Provider de GPS disabled!!");
            alertLocationInactive();
        }
    };

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


}
