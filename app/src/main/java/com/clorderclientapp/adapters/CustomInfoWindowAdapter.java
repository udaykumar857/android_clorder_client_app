package com.clorderclientapp.adapters;

import android.app.Activity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.modelClasses.RestaurantModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;

import static com.clorderclientapp.activites.RestaurantListActivity.restaurantList;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private RestaurantModel mRestaurantModel;
    private Activity mActivity;


    public CustomInfoWindowAdapter(Activity activity,RestaurantModel restaurantModel) {
        mRestaurantModel=restaurantModel;
        mActivity=activity;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Log.d("MarkerPosition",""+marker.getId());
        int clickPosition=Integer.parseInt(marker.getId().replace("m",""));
        View mapTitle = mActivity.getLayoutInflater().inflate(R.layout.layout_map_title, null);
        TextView restaurantName= (TextView) mapTitle.findViewById(R.id.restaurantName);
        TextView deliveryStatus= (TextView) mapTitle.findViewById(R.id.deliveryStatus);
        TextView restaurantStatus= (TextView) mapTitle.findViewById(R.id.restaurantStatus);
        TextView restTime= (TextView) mapTitle.findViewById(R.id.restTime);
        restaurantName.setText(restaurantList.get(clickPosition).getTitle());
        String deliveryString = "<b>" + "Delivery:" + "</b> ";
        if (restaurantList.get(clickPosition).getLunchDelivery().equals("Available") || restaurantList.get(clickPosition).getDinnerDelivery().equals("Available")) {
            deliveryString = deliveryString + "Available";
        } else {
            deliveryString = deliveryString + "Not Available";
        }
        deliveryStatus.setText(Html.fromHtml(deliveryString));
        String restStatus="Open";
        if (restaurantList.get(clickPosition).getLunchStatus().equals("Open") || restaurantList.get(clickPosition).getDinnerStatus().equals("Open")) {
            restStatus="Open";

        } else {
            restStatus="Currently Not Available-Order In Advance";
        }
        restaurantStatus.setText(Html.fromHtml(restStatus));

        JSONArray timingArray = restaurantList.get(clickPosition).getTimingsArray();
        String todayLunchStartTime = null, todayDinnerEndTime = null;
        if (timingArray.length() > 0) {
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK)) - 1;
            try {
                todayLunchStartTime=timingArray.getJSONObject(dayOfWeek).getJSONObject("Lunch").getString("start");
                todayDinnerEndTime=timingArray.getJSONObject(dayOfWeek).getJSONObject("Dinner").getString("end");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(restStatus.equals("Open")){
            restTime.setText(String.valueOf("Closes At "+todayDinnerEndTime));
        }else{
            restTime.setText(String.valueOf("Opens At "+todayLunchStartTime));
        }

        return mapTitle;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Log.d("getInfoContents","fire"+marker);
        return null;
    }
}
