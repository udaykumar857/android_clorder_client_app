package com.clorderclientapp.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class LocationService extends Service implements LocationListener {
    private static final String TAG = "LocationService";

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 5 * 60 * 1000;
    private Handler mHandler;
    private LocationManager mLocationManager;

    private Runnable gpsRunnable = new Runnable() {
        @Override
        public void run() {
            getLocationFromGPS();
        }
    };

    private Runnable networkRunnable = new Runnable() {
        @Override
        public void run() {
            getLocationFromNetwork();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "\t" + "onCreate()");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "\t" + "onStartCommand()");
        if (mHandler != null) {
            mHandler.removeCallbacks(gpsRunnable);
            mHandler.removeCallbacks(networkRunnable);
        }
        getLocation();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkEnabled) {
            getLocationFromNetwork();
        } else if (isGPSEnabled) {
            getLocationFromGPS();
        } else {
            Log.d("Location ->", "Turned OFF");
        }
    }

    private void getLocationFromGPS() {
        Log.d("GPS ->", "Enabled");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission  ->  ", "Granted");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES, 0, this);
            Location mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mLocation != null) {
                broadCastLocation(mLocation);
            } else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                getLocationFromNetwork();
            } else {
                Log.d("GPS Location ->  ", "Null");
                /* If "GPS Only" option is selected
                 *  Then retry getting the location
                 * */
                mHandler.postDelayed(gpsRunnable, 5000);
            }
        } else {
            Log.d("Permission  ->  ", "Denied");
        }
    }

    private void getLocationFromNetwork() {
        Log.d("Network ->", "Enabled");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission  ->  ", "Granted");
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES, 0, this);
            Location mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (mLocation != null) {
                broadCastLocation(mLocation);
            } else {
                Log.d("Network Location ->  ", "Null");
                /* If "Power Saving" option is selected
                 *  Then retry getting the location
                 * */
                mHandler.postDelayed(networkRunnable, 5000);
            }
        } else {
            Log.d("Permission  ->  ", "Denied");
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void broadCastLocation(Location location) {
        Intent intent = new Intent("updateLocation");
        intent.putExtra("latitude", location.getLatitude());
        intent.putExtra("longitude", location.getLongitude());
        sendBroadcast(intent);
        stopSelf();
    }

    private void removeLocationUpdates() {
        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Removing", "LocationUpdates");
                mLocationManager.removeUpdates(this);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        removeLocationUpdates();
    }
}
