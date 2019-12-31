package com.clorderclientapp.activites;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.adapters.MultiLocationAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.MultiLocationModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.RecyclerViewClickListener;
import com.clorderclientapp.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class MultiLocationActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface, OnMapReadyCallback {

    private RecyclerView mRecyclerView;
    private MultiLocationAdapter multiLocationAdapter;
    private ArrayList<MultiLocationModel> multiLocationList;
    private ImageView backImg;
    private HttpRequest httpRequest;
    private TextView noLocationsTxt;
    private RelativeLayout titleLayout;
    private LinearLayout restListLayout;
    private MapView mapView;
    private GoogleMap mGoogleMap;
    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_location);
        httpRequest = new HttpRequest();
        initViews();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        multiLocationAdapter = new MultiLocationAdapter(this, Constants.MultiLocationList);
        mRecyclerView.setAdapter(multiLocationAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listeners();
        getLocations();
    }


    private void initViews() {
        titleLayout = findViewById(R.id.titleLayout);
        backImg = findViewById(R.id.backImg);
        mapView = (MapView) findViewById(R.id.mapView);
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        noLocationsTxt = findViewById(R.id.no_locations_txt);
        restListLayout = findViewById(R.id.restListLayout);
        multiLocationAdapter = new MultiLocationAdapter(this, Constants.MultiLocationList);
    }

    private void listeners() {
        backImg.setOnClickListener(this);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this,
                new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Realm realm = Realm.getDefaultInstance();
                        try {
                            realm.beginTransaction();
                            realm.delete(CartModel.class);
                            realm.commitTransaction();
                        } catch (Exception e) {
                            e.printStackTrace();
                            realm.cancelTransaction();
                        }
                        Constants.selectedRestaurantId = Constants.MultiLocationList.get(position).getId();
                        startActivity(new Intent(MultiLocationActivity.this, JohnniesPizzaScreenActivity.class));
                        finish();
                    }
                }));
    }

    private void getLocations() {
        if (Utils.isNetworkAvailable(this)) {
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("clientId", Constants.clientId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utils.startLoadingScreen(this);
            httpRequest.fetchClientChildLocations(this, requestObject, Constants.FetchClientChildLocations);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
        }

//        String locationData = "{\n" +
//                "  \"locations\":[\n" +
//                "    {\n" +
//                "      \"address\":\"2000 Avenue of the Stars suite 20, Los Angeles, CA 90067\",\n" +
//                "     \"id\":5,\n" +
//                "     \"restaurantName\":\"Demo Restaurant\"\n" +
//                "      \n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"address\":\"11819 Wilshire Blvd, Los Angeles, CA 90025\",\n" +
//                "     \"id\":494,\n" +
//                "     \"restaurantName\":\"Yen Sushi\"\n" +
//                "      \n" +
//                "    }\n" +
//                "    ]\n" +
//                "}";

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backImg:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    private void initCamera() {
//        Log.d("Location", "" + location);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        int widthpix = metrics.widthPixels;
//        int heightpix = metrics.heightPixels;
//        Log.d("widthpix,heightpix", "" + widthpix + "/t" + heightpix);

        for (int i = 0; i < Constants.MultiLocationList.size(); i++) {
            int num = i + 1;
            Bitmap exp = Utils.drawMultilineTextToBitmap(this, R.mipmap.map_marker_60, "" + num);
            Log.d("Map Sequence Num", "" + i + 1);
            Log.d("Map LatLng", "" + Constants.MultiLocationList.get(i).getmLatLng());
            mGoogleMap.addMarker(new MarkerOptions().position(Constants.MultiLocationList.get(i).getmLatLng()).icon(BitmapDescriptorFactory.fromBitmap(exp)).anchor(0.5f, 1).title(Constants.MultiLocationList.get(i).getAddress()));
//            mGoogleMap.addMarker(new MarkerOptions().position(mapLocationsList.get(i)));
            builder.include(Constants.MultiLocationList.get(i).getmLatLng());
        }

        LatLngBounds latLngBounds;
        if (Constants.MultiLocationList.size() > 0) {
            latLngBounds = builder.build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 8));
        }
        mGoogleMap.setMapType(MAP_TYPES[1]);
        mGoogleMap.setTrafficEnabled(false);
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.FetchClientChildLocations:
                if (response != null) {
                    try {
                        Constants.MultiLocationList.clear();
                        JSONObject locObj = (JSONObject) response;
                        if (locObj.getBoolean("isSuccess")) {
                            JSONArray locationArray = locObj.getJSONArray("ChildLocations");
                            Log.d("LocationList", locationArray.toString());
                            if (locationArray.length() > 1) {
                                for (int i = 0; i < locationArray.length(); i++) {
                                    MultiLocationModel multiLocationModel = new MultiLocationModel();
                                    multiLocationModel.setId(locationArray.getJSONObject(i).getInt("ClientId"));
                                    multiLocationModel.setAddress(locationArray.getJSONObject(i).getString("ClientAddress"));
                                    multiLocationModel.setRestaurantName(locationArray.getJSONObject(i).getString("ClientName"));
                                    String latitude = locationArray.getJSONObject(i).getString("ClientLatititude");
                                    String longitude = locationArray.getJSONObject(i).getString("ClientLongitude");
                                    Geocoder coder = new Geocoder(this);
                                    String strAddress = locationArray.getJSONObject(i).getString("ClientAddress");
                                    List<Address> address;
                                    try {
                                        address = coder.getFromLocationName(strAddress, 5);
                                        if (address != null) {
                                            Address location = address.get(0);
                                            latitude = String.valueOf(location.getLatitude());
                                            longitude = String.valueOf(location.getLongitude());

                                        } else {
                                            latitude = "";
                                            longitude = "";
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        try {
                                            address = coder.getFromLocationName(strAddress, 5);
                                            if (address != null) {
                                                Address location = address.get(0);
                                                latitude = String.valueOf(location.getLatitude());
                                                longitude = String.valueOf(location.getLongitude());
                                            } else {
                                                latitude = "";
                                                longitude = "";
                                            }

                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    if (!latitude.equals("") && !longitude.equals("")) {
                                        multiLocationModel.setmLatLng(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)));
                                        Constants.MultiLocationList.add(multiLocationModel);
                                    }
                                }
                                multiLocationAdapter.notifyDataSetChanged();
                                titleLayout.setVisibility(View.VISIBLE);
                                restListLayout.setVisibility(View.VISIBLE);
                                noLocationsTxt.setVisibility(View.GONE);
                                initCamera();
                                Utils.cancelLoadingScreen();
                            } else {
                                Utils.cancelLoadingScreen();
                                startActivity(new Intent(this, JohnniesPizzaScreenActivity.class));
                                finish();
                            }
                        } else {
                            Utils.cancelLoadingScreen();
                            titleLayout.setVisibility(View.VISIBLE);
                            noLocationsTxt.setVisibility(View.VISIBLE);
                            restListLayout.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.cancelLoadingScreen();
                    }
                } else {
                    Utils.cancelLoadingScreen();
                    titleLayout.setVisibility(View.VISIBLE);
                    noLocationsTxt.setVisibility(View.VISIBLE);
                    restListLayout.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void userAction(int actionType) {

    }


}
