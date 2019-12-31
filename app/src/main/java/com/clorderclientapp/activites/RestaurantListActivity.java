package com.clorderclientapp.activites;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.Services.LocationService;
import com.clorderclientapp.adapters.RestaurantListAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.CuisineModel;
import com.clorderclientapp.modelClasses.RestaurantModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.RecyclerViewClickListener;
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

public class RestaurantListActivity extends AppCompatActivity implements ResponseHandler, UserActionInterface, View.OnClickListener {

    private HttpRequest httpRequest;
    private RecyclerView mRecyclerView;

    public static ArrayList<RestaurantModel> restaurantList;
    private ArrayList<RestaurantModel> filterList;
    private TextView noRestaurant;
    private RestaurantListAdapter restaurantListAdapter;
    private ImageView backImg;
    private ImageView map, filterImg;
    private EditText searchRest;
    private AlertDialog alertDialog;
    private LatLng mLatLng;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1000;
    private static final String[] LOCATION_PERMISSION = new String[]
            {Manifest.permission.ACCESS_FINE_LOCATION};
    private final int LOCATION_PERMISSION_REQUEST = 4001;
    private TextView enterAddress;
    private ArrayList<CuisineModel> cuisineArrayList;
    Spinner cuisineSpinner;
    private boolean isFilterOpen = false;
    private String address;
    private int distanceSpinnerPosition = 0, cuisinePosition = 0, priceSpinnerPosition = 0;
    private float ratingStar = 0;
    List<String> distanceList;
    List<String> priceList;
    private int isDeliveryAvaliable = 0;
    RatingBar ratingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        distanceList = Arrays.asList(getResources().getStringArray(R.array.distanceList));
        priceList = Arrays.asList(getResources().getStringArray(R.array.priceList));
        httpRequest = new HttpRequest();
        cuisineArrayList = new ArrayList<>();
        initViews();
        listeners();
        if (getIntent() != null) {
            address = getIntent().getStringExtra("address");
            double latitude = getIntent().getDoubleExtra("latitude", 0.00);
            double longitude = getIntent().getDoubleExtra("longitude", 0.00);
            mLatLng = new LatLng(latitude, longitude);
        }
        getRestaurantList();
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        noRestaurant = (TextView) findViewById(R.id.noRestaurant);
        backImg = (ImageView) findViewById(R.id.back_img);
        searchRest = (EditText) findViewById(R.id.searchRest);
        filterImg = (ImageView) findViewById(R.id.filterImg);
        map = (ImageView) findViewById(R.id.map);
    }

    private void listeners() {
        restaurantList = new ArrayList<>();
        filterList = new ArrayList<>();
        restaurantListAdapter = new RestaurantListAdapter(this, filterList);
        mRecyclerView.setAdapter(restaurantListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this,
                new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Constants.clientId = restaurantList.get(position).getRestaurantID();
                        startActivity(new Intent(RestaurantListActivity.this, MultiLocationActivity.class));
                    }
                }));
        searchRest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    filter(s.toString());
                } else {
                    filterList.clear();
                    filterList.addAll(restaurantList);
                    restaurantListAdapter.notifyDataSetChanged();
                }
            }
        });
        backImg.setOnClickListener(this);
        filterImg.setOnClickListener(this);
        map.setOnClickListener(this);
    }


    private void filter(String text) {
        //new array list that will hold the filtered data
        ArrayList<RestaurantModel> filterNames = new ArrayList<>();

        for (int i = 0; i < restaurantList.size(); i++) {

            if (restaurantList.get(i).getTitle().toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filterNames.add(restaurantList.get(i));
            }
        }
        filterList.clear();
        filterList.addAll(filterNames);
        restaurantListAdapter.notifyDataSetChanged();
    }


    private void getRestaurantList() {
        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
            JSONObject itemsObj = new JSONObject();
            JSONObject requestObject = new JSONObject();

            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM", Locale.getDefault());
                String todayDate = simpleDateFormat.format(new Date());
                Log.d("todaysDate", todayDate);
                requestObject.put("ExclusiveStartKey", JSONObject.NULL);
                requestObject.put("Latitude", mLatLng.latitude);
                requestObject.put("Longitude", mLatLng.longitude);
                requestObject.put("Address", address);
                requestObject.put("Date", todayDate.split(" ")[0]);
                requestObject.put("Time", todayDate.split(" ")[1]);
                requestObject.put("Distance", Integer.parseInt(distanceList.get(distanceSpinnerPosition)));
                requestObject.put("PriceForTwo", priceSpinnerPosition == 0 ? 0 : Integer.parseInt(priceList.get(priceSpinnerPosition).replace("$", "")));
                requestObject.put("Cuisine", cuisinePosition == 0 ? JSONObject.NULL : cuisineArrayList.get(cuisinePosition).getCname());
                requestObject.put("Parking", JSONObject.NULL);
                requestObject.put("TakeOut", JSONObject.NULL);
                requestObject.put("Buffet", JSONObject.NULL);
                requestObject.put("KidsFriendly", JSONObject.NULL);
                requestObject.put("OutdoorSeating", JSONObject.NULL);
                requestObject.put("Rating", ratingStar);
                requestObject.put("DeliveryAvail", isDeliveryAvaliable);
                requestObject.put("OpenNow", 0);
                itemsObj.put("Items", requestObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("restaurantDataRequest", itemsObj.toString());
            httpRequest.restaurantData(this, itemsObj, Constants.RestaurantData);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionDeliveryFeeFailed);
        }
    }

    private void getRestaurantCuisineList() {
        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
            JSONObject itemsObj = new JSONObject();
            JSONObject requestObject = new JSONObject();

            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM", Locale.getDefault());
                String todayDate = simpleDateFormat.format(new Date());
                Log.d("todaysDate", todayDate);
                requestObject.put("ExclusiveStartKey", JSONObject.NULL);
                requestObject.put("Latitude", mLatLng.latitude);
                requestObject.put("Longitude", mLatLng.longitude);
                requestObject.put("Address", address);
                requestObject.put("Date", todayDate.split(" ")[0]);
                requestObject.put("Time", todayDate.split(" ")[1]);
                requestObject.put("Distance", 5);
                requestObject.put("PriceForTwo", 0);
                requestObject.put("Cuisine", JSONObject.NULL);
                requestObject.put("Parking", JSONObject.NULL);
                requestObject.put("TakeOut", JSONObject.NULL);
                requestObject.put("Buffet", JSONObject.NULL);
                requestObject.put("KidsFriendly", JSONObject.NULL);
                requestObject.put("OutdoorSeating", JSONObject.NULL);
                requestObject.put("Rating", 0);
                requestObject.put("DeliveryAvail", 0);
                requestObject.put("OpenNow", 0);
                requestObject.put("OnlyCuisine", 1);// To get only cuisine.
                itemsObj.put("Items", requestObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("RestaurantCuisineReq", itemsObj.toString());
            httpRequest.restaurantCuisineData(this, itemsObj, Constants.RestaurantCuisineData);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionDeliveryFeeFailed);
        }
    }

    private void getCuisineRequest() {
        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
            httpRequest.getCuisineData(this, Constants.CuisineData);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionDeliveryFeeFailed);
        }
    }

    @Override
    public void responseHandler(Object response, int requestType) {

        switch (requestType) {
            case Constants.RestaurantData:
                Utils.cancelLoadingScreen();
                restaurantList.clear();
                filterList.clear();
                if (response != null) {
                    restaurantList.addAll((ArrayList<RestaurantModel>) response);
                    filterList.addAll((ArrayList<RestaurantModel>) response);
                    restaurantListAdapter.notifyDataSetChanged();
                    noRestaurant.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    noRestaurant.setVisibility(View.VISIBLE);
                }
                getRestaurantCuisineList();

                break;

            case Constants.RestaurantCuisineData:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    cuisineArrayList.clear();
                    cuisineArrayList.addAll((ArrayList<CuisineModel>) response);
                }

                if (isFilterOpen) {
                    setCuisineList();
                }
                break;

            case Constants.CuisineData:
                Utils.cancelLoadingScreen();
                if (response != null) {
                }
                break;
        }

    }

    @Override
    public void userAction(int actionType) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("updateLocation");
        registerReceiver(locationUpdateReceiver, filter);
        Utils.showNoLocationDialog(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(locationUpdateReceiver);
    }

    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("LocationUpdateReceiver", "\t" + "onReceive()");
            Utils.cancelLoadingScreen();
            if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
                double latitude, longitude;
                latitude = intent.getDoubleExtra("latitude", 0);
                longitude = intent.getDoubleExtra("longitude", 0);
                Log.d("Latitude", "\t" + latitude);
                Log.d("Longitude", "\t" + longitude);
                mLatLng = new LatLng(latitude, longitude);
                String addressData = reverseGeoCoding(mLatLng);
                if (addressData != null) {
                    enterAddress.setText("");
                    enterAddress.setText(addressData);
                    enterAddress.setTextColor(Color.parseColor("#000000"));
                    address = addressData;
                    getRestaurantList();
                } else {
                    Utils.toastDisplay(RestaurantListActivity.this, "Unable to fetch current location");
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.filterImg:
                isFilterOpen = true;
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                showFilterDialog();
//                getCuisineRequest();

                break;
            case R.id.map:
                if (restaurantList.size() > 0) {
                    startActivity(new Intent(this, MapActivity.class));
                } else {
                    Utils.toastDisplay(this, "No Restaurants In Your Area");
                }
                break;
        }
    }

    private void showFilterDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View alertView = LayoutInflater.from(this).
                inflate(R.layout.layout_filter_restaurant, null, false);

        ImageView filterCloseBtn = (ImageView) alertView.findViewById(R.id.filter_close_btn);
        enterAddress = (TextView) alertView.findViewById(R.id.enterAddress);
        ImageView gpsPoint = (ImageView) alertView.findViewById(R.id.gpsPoint);
        Spinner distanceSpinner = (Spinner) alertView.findViewById(R.id.distanceSpinner);
        cuisineSpinner = (Spinner) alertView.findViewById(R.id.cuisineSpinner);
        Spinner priceSpinner = (Spinner) alertView.findViewById(R.id.priceSpinner);
        ratingBar = (RatingBar) alertView.findViewById(R.id.ratingBar);
        CheckBox deliveryCheck = (CheckBox) alertView.findViewById(R.id.deliveryCheck);
        Typeface font = Typeface.createFromAsset(getAssets(), "Lora-Regular.ttf");
        Button submitBtn = (Button) alertView.findViewById(R.id.submitBtn);
        enterAddress.setText("");
        enterAddress.setText(address);
        enterAddress.setTextColor(Color.parseColor("#000000"));
        ArrayAdapter distanceAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_text_view, distanceList);

        ArrayAdapter priceAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_text_view, priceList);

        distanceSpinner.setAdapter(distanceAdapter);
        priceSpinner.setAdapter(priceAdapter);
        distanceSpinner.setSelection(distanceSpinnerPosition);
        priceSpinner.setSelection(priceSpinnerPosition);
        ratingBar.setRating(ratingStar);


        if (isDeliveryAvaliable == 0) {
            deliveryCheck.setChecked(false);
        } else {
            deliveryCheck.setChecked(true);
        }
        deliveryCheck.setTypeface(font);
        setCuisineList();

        filterCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFilterOpen = false;
                alertDialog.dismiss();
            }
        });

        enterAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addressRequest();
            }
        });
        gpsPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(RestaurantListActivity.this, LOCATION_PERMISSION, LOCATION_PERMISSION_REQUEST);
            }
        });
        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                distanceSpinnerPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cuisineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cuisinePosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        priceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                priceSpinnerPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        deliveryCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isDeliveryAvaliable = 1;
                } else {
                    isDeliveryAvaliable = 0;
                }
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFilterOpen = false;
                ratingStar = ratingBar.getRating();
                alertDialog.dismiss();
                getRestaurantList();
            }
        });
        builder.setView(alertView);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        if (alertDialog != null) {
            alertDialog.show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    private void setCuisineList() {

        int allCnt = 0;

//            cuisineArrayList = new ArrayList<>();
        CuisineModel cuisineModel = new CuisineModel();
        cuisineModel.setCname("All");

//            if (restaurantList.size() > 0) {
//                cuisineArrayList.addAll(restaurantList.get(0).getCuisineModelArrayList());
//            }
        for (int j = 0; j < cuisineArrayList.size(); j++) {
            allCnt += cuisineArrayList.get(j).getCount();
        }
        cuisineModel.setCount(allCnt);
        cuisineArrayList.add(0, cuisineModel);

        List<String> list = new ArrayList<String>();
        for (int k = 0; k < cuisineArrayList.size(); k++) {
            list.add(cuisineArrayList.get(k).getCname() + "(" + cuisineArrayList.get(k).getCount() + ")");
        }
        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_text_view, list);
        cuisineSpinner.setAdapter(cuisineAdapter);
    }


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
                address = "" + place.getAddress();
                enterAddress.setText(place.getAddress());
                enterAddress.setTextColor(Color.parseColor("#000000"));
                getRestaurantList();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("TAG", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private String reverseGeoCoding(LatLng mLatLng) {
        String parsedAddress = null;
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
            parsedAddress = TextUtils.join(System.getProperty("line.separator"), addressFragments);
        }

        return parsedAddress;
    }
}
