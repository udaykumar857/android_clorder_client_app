package com.clorderclientapp.activites;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.clorderclientapp.R;
import com.clorderclientapp.adapters.CustomInfoWindowAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.clorderclientapp.activites.RestaurantListActivity.restaurantList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private GoogleMap mMap;
    public static ArrayList<LatLng> mapList;
    private int i = 0;
    private ImageView backImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initViews();
        listeners();

    }

    private void initViews() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        backImg = (ImageView) findViewById(R.id.back_img);
    }

    private void listeners() {
        backImg.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        setMap();
    }

    private void setMap() {

        for (i = 0; i < restaurantList.size(); i++) {
            Log.d("Index", "" + i);
            mMap.addMarker(new MarkerOptions().position(restaurantList.get(i).getmLatLngRestaurant()).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_rest1)).anchor(0.5f, 1));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantList.get(i).getmLatLngRestaurant(), 15));
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this, restaurantList.get(i)));
//            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//                @Override
//                public View getInfoWindow(Marker marker) {
//                    Log.d("Index",""+i);
//                    View mapTitle = getLayoutInflater().inflate(R.layout.layout_map_title, null);
//                    TextView restaurantName= (TextView) mapTitle.findViewById(R.id.restaurantName);
//                    TextView deliveryStatus= (TextView) mapTitle.findViewById(R.id.deliveryStatus);
//                    TextView restaurantStatus= (TextView) mapTitle.findViewById(R.id.restaurantStatus);
//                    TextView restTime= (TextView) mapTitle.findViewById(R.id.restTime);
//                    restaurantName.setText(restaurantList.get(i).getTitle());
//                    String deliveryString = "<b>" + "Delivery:" + "</b> ";
//                    if (restaurantList.get(i).getLunchDelivery().equals("Available") || restaurantList.get(i).getDinnerDelivery().equals("Available")) {
//                        deliveryString = deliveryString + "Available";
//                    } else {
//                        deliveryString = deliveryString + "Not Available";
//                    }
//                    deliveryStatus.setText(Html.fromHtml(deliveryString));
//                    String restStatus="Open";
//                    if (restaurantList.get(i).getLunchStatus().equals("Open") || restaurantList.get(i).getDinnerStatus().equals("Open")) {
//                        restStatus="Open";
//
//                    } else {
//                        restStatus="Currently Not Available-Order In Advance";
//                    }
//                    restaurantStatus.setText(Html.fromHtml(restStatus));
//
//                    JSONArray timingArray = restaurantList.get(i).getTimingsArray();
//                    String todayLunchStartTime = null, todayDinnerEndTime = null;
//                    if (timingArray.length() > 0) {
//                        Calendar calendar = Calendar.getInstance();
//                        int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK)) - 1;
//                        try {
//                            todayLunchStartTime=timingArray.getJSONObject(dayOfWeek).getJSONObject("Lunch").getString("start");
//                            todayDinnerEndTime=timingArray.getJSONObject(dayOfWeek).getJSONObject("Dinner").getString("end");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    if(restStatus.equals("Open")){
//                        restTime.setText(String.valueOf("Closes At "+todayDinnerEndTime));
//                    }else{
//                        restTime.setText(String.valueOf("Opens At "+todayLunchStartTime));
//                    }
//
//                    return mapTitle;
//                }
//
//                @Override
//                public View getInfoContents(Marker marker) {
//                    return null;
//                }
//            });
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
        }
    }
}
