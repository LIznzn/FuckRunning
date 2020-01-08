package com.acgmiao.dev.fuckrunning.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.acgmiao.dev.fuckrunning.R;
import com.acgmiao.dev.fuckrunning.util.PermissionUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import static com.acgmiao.dev.fuckrunning.util.CoordTrans.GPS2GCJ;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener {

    private static final long LOCATION_REFRESH_TIME_INTERVAL = 1000;

    private static final float LOCATION_MIN_DISTANCE = 1.0f;

    private static final long BAD_ACCURACY_UPDATE_TIME = 2 * 60 * 1000;

    private GoogleMap mMap;

    private LocationManager mLocationManager;

    private boolean mIsAddListener;

    private boolean mIsPermissionGranted;

    private BroadcastReceiver broadcastReceiver;

    private boolean mIsResume;

    private Location mLastKnownLocation;

    private long mLastUpdateLocationTime;

    private LocationSource.OnLocationChangedListener myLocationListener = null;

    private int MARKER_NUMBER_CONTER = 0;

    private Marker[] mMakerArray = new Marker[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    if (mIsPermissionGranted && mIsResume) {
                        boolean isGPSOn = isGPSOn();
                        if (isGPSOn) {
                            startGetLocation();
                        } else {
                            stopGetLocation();
                        }
                    }
                }
            }
        };
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResume = true;
        registerBroadcastReceiver();
        if (mIsPermissionGranted) {
            startGetLocation();
        } else {
            if (checkLocationPermission()) {
                mIsPermissionGranted = true;
                startGetLocation();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResume = true;
        unregisterBroadcastReceiver();
        if (mIsPermissionGranted) {
            stopGetLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean isGotLocationPermission = checkLocationPermission();
        if (isGotLocationPermission) {
            // goto next method
            mIsPermissionGranted = true;
            startGetLocation();
        } else {
            // need to get location permission
            requestLocationPermission();
        }

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapClick(LatLng point) {
        addMarker(point);
        Toast.makeText(this, "tapped, point=" + point, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Toast.makeText(this, "long pressed, point=" + point, Toast.LENGTH_SHORT).show();
    }

    public void addMarker(LatLng point) {
        if(MARKER_NUMBER_CONTER<5) {//最大5个点
            mMakerArray[MARKER_NUMBER_CONTER++] = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .draggable(true)
                    .alpha(0.7f)
                    .title(String.valueOf(MARKER_NUMBER_CONTER)));
        } else {
            Toast.makeText(this, "数组越界，不加Marker", Toast.LENGTH_SHORT).show();
        }
    }


    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        unregisterReceiver(broadcastReceiver);
    }

    private void initLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @SuppressLint("MissingPermission")
    private void startGetLocation() {
        initLocationManager();
        if (!mIsAddListener) {
            List<String> providers = mLocationManager.getProviders(true);
            for (int i = 0; i < providers.size(); i++) {
                String provider = providers.get(i);
                Log.e("test", "startGetLocation:" + provider);
                mLocationManager.requestLocationUpdates(
                        provider, LOCATION_REFRESH_TIME_INTERVAL, LOCATION_MIN_DISTANCE, this);
            }
            mIsAddListener = true;
        }
    }

    private void stopGetLocation() {
        initLocationManager();
        if (mIsAddListener) {
            Log.e("test", "stopGetLocation");
            mLocationManager.removeUpdates(this);
            mIsAddListener = false;
            //mMap.setLocationSource(this);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        long nowTime = System.currentTimeMillis();
        boolean isUpdateMap = false;
        double lot = location.getLongitude();
        double lat = location.getLatitude();
        LatLng mgLatLng = GPS2GCJ(new LatLng(lat, lot));
        location.setLatitude(mgLatLng.latitude);
        location.setLongitude(mgLatLng.longitude);
        mMap.setLocationSource(new MyLocationSource());
        if (mLastKnownLocation == null) {//第一次定到位
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mgLatLng, 15));
        }
        if (mLastKnownLocation == null || location.getAccuracy() <= mLastKnownLocation.getAccuracy()) {
            mLastKnownLocation = location;
            mLastUpdateLocationTime = nowTime;
            if (!mMap.isMyLocationEnabled()) {
                mMap.setMyLocationEnabled(true);
            }
            isUpdateMap = true;
        }
        // force update location
        if(mLastUpdateLocationTime > 0 && nowTime - mLastUpdateLocationTime >= BAD_ACCURACY_UPDATE_TIME) {
            if(location.getAccuracy() >= mLastKnownLocation.getAccuracy()) {
                mLastKnownLocation = location;
                mLastUpdateLocationTime = nowTime;
                isUpdateMap = true;
            }
        }
        if(isUpdateMap){
            myLocationListener.onLocationChanged(mLastKnownLocation);
            Log.e("test","onLocationChanged Performed");
        }
        Log.e("test", "onLocationChanged:" + location.toString() + ",Provider:" + location.getProvider());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.e("test", "onStatusChanged:" + s + "," + i);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.e("test", "onProviderEnabled:" + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.e("test", "onProviderDisabled:" + s);
    }

    private boolean isGPSOn() {
        initLocationManager();
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean checkLocationPermission() {
        boolean locationPermission = false;
        int permissionStatus = ContextCompat.
                checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
        }
        return locationPermission;
    }

    private void requestLocationPermission() {
        PermissionUtils.requestPermission(this,
                PermissionUtils.LOCATION_PERMISSION_REQUEST_CODE,
                android.Manifest.permission.ACCESS_FINE_LOCATION, false);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocationApplication button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode != PermissionUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            mIsPermissionGranted = true;
            initLocationManager();
            startGetLocation();
        } else {
            mIsPermissionGranted = false;
            Log.e("test", "not get location permission");
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

//    private void showMissingPermissionError() {
//        PermissionUtils.PermissionDeniedDialog.newInstance(false).show(getSupportFragmentManager(), "dialog");
//    }

    private class MyLocationSource implements LocationSource {
        @Override
        public void activate(OnLocationChangedListener listener) {
            myLocationListener = listener;
        }

        @Override
        public void deactivate() {
            myLocationListener = null;
        }
    }
}