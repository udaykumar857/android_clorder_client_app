package com.clorderclientapp.activites;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.Services.LocationService;
import com.clorderclientapp.utils.CheckPermission;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FindRestaurantActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView facebookImg, twitterImg, googlePlusImg, instagramImg;
    private TextView enterAddress;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1000;
    private Button findRestaurantBtn;
    private LatLng mLatLng;
    private String addressData;
    private ImageView gpsPoint;
    private static final String[] LOCATION_PERMISSION = new String[]
            {Manifest.permission.ACCESS_FINE_LOCATION};
    private final int LOCATION_PERMISSION_REQUEST = 4001;
    private CheckPermission checker;
    private double latitude = 0, longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_restaurant);
        checker = new CheckPermission(this);
        initViews();
        listeners();
    }

    private void initViews() {
        enterAddress = (TextView) findViewById(R.id.enterAddress);
        findRestaurantBtn = (Button) findViewById(R.id.findRestaurantBtn);
        facebookImg = (ImageView) findViewById(R.id.facebookImg);
        twitterImg = (ImageView) findViewById(R.id.twitterImg);
        googlePlusImg = (ImageView) findViewById(R.id.googlePlusImg);
        instagramImg = (ImageView) findViewById(R.id.instagramImg);
        gpsPoint = (ImageView) findViewById(R.id.gpsPoint);
    }

    private void listeners() {
        enterAddress.setOnClickListener(this);
        findRestaurantBtn.setOnClickListener(this);
        facebookImg.setOnClickListener(this);
        twitterImg.setOnClickListener(this);
        googlePlusImg.setOnClickListener(this);
        instagramImg.setOnClickListener(this);
        gpsPoint.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        switch (v.getId()) {
            case R.id.enterAddress:
                addressRequest();
                break;
            case R.id.findRestaurantBtn:
                if (enterAddress != null && (enterAddress.getText().length() > 0)&&(!enterAddress.getText().equals("Enter Your Address"))) {
                    Intent restaurantIntent = new Intent(this, RestaurantListActivity.class);
                    restaurantIntent.putExtra("latitude", mLatLng.latitude);
                    restaurantIntent.putExtra("longitude", mLatLng.longitude);
                    restaurantIntent.putExtra("address", addressData);
                    startActivity(restaurantIntent);
                } else {
                    Utils.toastDisplay(this, "Please enter your address.");
                }
                break;
            case R.id.gpsPoint:

                ActivityCompat.requestPermissions(this, LOCATION_PERMISSION, LOCATION_PERMISSION_REQUEST);

                break;
            case R.id.facebookImg:
                intent.setData(Uri.parse("https://www.facebook.com/orderfoodonlinenow"));
                startActivity(intent);
                break;
            case R.id.twitterImg:
                intent.setData(Uri.parse("https://twitter.com/Orderfoodonlin1?lang=en"));
                startActivity(intent);
                break;
            case R.id.googlePlusImg:
                intent.setData(Uri.parse("https://plus.google.com/101315232992416963740"));
                startActivity(intent);
                break;
            case R.id.instagramImg:
                intent.setData(Uri.parse("https://www.instagram.com/OrderFoodOnline/"));
                startActivity(intent);
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("updateLocation");
        registerReceiver(locationUpdateReceiver, filter);
        Utils.showNoLocationDialog(this);
//        if (!checker.lacksPermissions(LOCATION_PERMISSION[0])) {
//
//        } else {
//            // Permission is not granted. Ask user to grant them manually
//            Utils.showMissingPermissionDialog(this,null,
//                    getString(R.string.missing_permission_title),
//                    getString(R.string.location_permission_missing_message),Constants.ActionLocationPermission);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(locationUpdateReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("Results", "\t" + Arrays.toString(grantResults) + "\t" + Arrays.toString(permissions) + "\t" + requestCode);
        boolean isAllPermissionsAllowed = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == -1) {
                isAllPermissionsAllowed = false;
            }
        }

        if (isAllPermissionsAllowed) {
            switch (requestCode) {
                case LOCATION_PERMISSION_REQUEST:
                    Utils.startLoadingScreen(this);
                    startService(new Intent(this, LocationService.class));
                    break;
            }
        }

        boolean shouldCheckForPermission = !(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_DENIED);
        Log.d("shouldCheckForPerm", "" + shouldCheckForPermission);
        if (shouldCheckForPermission || !isAllPermissionsAllowed) {
            checkIfPermissionIsCompletelyDenied(requestCode);
        }
    }

    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("LocationUpdateReceiver", "\t" + "onReceive()");
            Utils.cancelLoadingScreen();
            if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
                latitude = intent.getDoubleExtra("latitude", 0);
                longitude = intent.getDoubleExtra("longitude", 0);
                Log.d("Latitude", "\t" + latitude);
                Log.d("Longitude", "\t" + longitude);
                mLatLng=new LatLng(latitude, longitude);
                String addressData = reverseGeoCoding(mLatLng);
                if(addressData !=null){
                    enterAddress.setText("");
                    enterAddress.setText(addressData);
                    enterAddress.setTextColor(Color.parseColor("#000000"));
                }else{
                    Utils.toastDisplay(FindRestaurantActivity.this,"Unable to fetch current location");
                }
            }
        }
    };

    private void checkIfPermissionIsCompletelyDenied(int requestCode) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!shouldShowRequestPermissionRationale(LOCATION_PERMISSION[0])) {
                        Utils.showMissingPermissionDialog(this, null, getString(R.string.missing_permission_title),
                                getString(R.string.location_permission_missing_message), Constants.ActionLocationPermission);
                    }
                }
                break;
        }
    }

    private void addressRequest() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setCountry("US")
                    .build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(typeFilter)
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    private String reverseGeoCoding(LatLng mLatLng) {
        String parsedAddress=null;
        List<Address> addressList = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                addressList = geocoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if (addressList != null) {
            Log.d("addressList", addressList.toString());
            Address address = addressList.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();
            addressFragments.add(address.getAddressLine(0));
            Log.d("addressList", addressFragments.toString() + TextUtils.join(System.getProperty("line.separator"), addressFragments));
            parsedAddress=TextUtils.join(System.getProperty("line.separator"), addressFragments);
        }

        return parsedAddress;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                enterAddress.setText("");
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("TAG", "Place: " + place.getName());
                Log.i("TAG", "Place: " + place.getAddress());
                Log.i("TAG", "Place: " + place.getPhoneNumber());
                Log.i("TAG", "Place: " + place.getPlaceTypes());
                Log.i("TAG", "Place: " + place.getLocale() + "" + place.getLatLng());
                mLatLng = place.getLatLng();
                addressData = "" + place.getAddress();
                enterAddress.setText(place.getAddress());
                enterAddress.setTextColor(Color.parseColor("#000000"));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("TAG", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
