package com.clorderclientapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.clorderclientapp.R;
import com.clorderclientapp.modelClasses.RestaurantModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.RestaurantListViewHolder> {
    private Context mContext;
    private ArrayList<RestaurantModel> restaurantArrayList;


    public RestaurantListAdapter(Context context, ArrayList<RestaurantModel> restaurantModelArrayList) {
        restaurantArrayList = restaurantModelArrayList;
        mContext = context;
    }

    @Override
    public RestaurantListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.layout_search_restaurant_single_row, parent, false);
        return new RestaurantListAdapter.RestaurantListViewHolder(row);
    }

    @Override
    public void onBindViewHolder(RestaurantListViewHolder holder, int position) {

        Uri uri = Uri.parse(restaurantArrayList.get(position).getImageUrl());

        showImage(holder, uri);

        holder.cuisineName.setText(restaurantArrayList.get(position).getCuisineName());
        holder.restaurantName.setText(restaurantArrayList.get(position).getTitle());
        holder.restAddress.setText(restaurantArrayList.get(position).getAddress());
        String distanceString = "<b>" + "Distance(Miles):" + "</b> " + String.format(Locale.getDefault(), "%.2f", Float.parseFloat(restaurantArrayList.get(position).getDistance()));
        holder.distance.setText(Html.fromHtml(distanceString));
        String deliveryString = "<b>" + "Delivery:" + "</b> ";
        if (restaurantArrayList.get(position).getLunchDelivery().equals("Available") || restaurantArrayList.get(position).getDinnerDelivery().equals("Available")) {
            deliveryString = deliveryString + "Available";
        } else {
            deliveryString = deliveryString + "Not Available";
        }
        holder.deliveryStatus.setText(Html.fromHtml(deliveryString));

        String restStatus="Open";
        if (restaurantArrayList.get(position).getLunchStatus().equals("Open") || restaurantArrayList.get(position).getDinnerStatus().equals("Open")) {
            restStatus="Open";

        } else {
            restStatus="Currently Not Available-Order In Advance";
        }

        holder.restaurantStatus.setText(restStatus);

        JSONArray timingArray = restaurantArrayList.get(position).getTimingsArray();
        String todayLunchStartTime = null, todayDinnerEndTime = null;
        if (timingArray.length() > 0) {
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK)) - 1;
            Log.d("dayOfWeek",""+dayOfWeek);
            try {
                todayLunchStartTime=timingArray.getJSONObject(dayOfWeek).getJSONObject("Lunch").getString("start");
                todayDinnerEndTime=timingArray.getJSONObject(dayOfWeek).getJSONObject("Dinner").getString("end");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(restStatus.equals("Open")){
            holder.restTime.setText(String.valueOf("Closes At "+todayDinnerEndTime));
        }else{
            holder.restTime.setText(String.valueOf("Opens At "+todayLunchStartTime));
        }



    }

    private void showImage(RestaurantListViewHolder holder, Uri imageUri) {

        Glide.with(mContext).load(imageUri).asBitmap().listener(new RequestListener<Uri, Bitmap>() {
            @Override
            public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                return false;
            }
        }).placeholder(R.mipmap.restaurant_128).into(holder.restImg);
    }

    @Override
    public int getItemCount() {
        return restaurantArrayList.size();
    }

    class RestaurantListViewHolder extends RecyclerView.ViewHolder {

        private TextView cuisineName, restaurantName, restAddress, distance, deliveryStatus, restaurantStatus, restTime;
        private ImageView restImg;

        public RestaurantListViewHolder(View itemView) {
            super(itemView);
            restImg = (ImageView) itemView.findViewById(R.id.restImg);
            cuisineName = (TextView) itemView.findViewById(R.id.cuisineName);
            restaurantName = (TextView) itemView.findViewById(R.id.restaurantName);
            restAddress = (TextView) itemView.findViewById(R.id.restAddress);
            distance = (TextView) itemView.findViewById(R.id.distance);
            deliveryStatus = (TextView) itemView.findViewById(R.id.deliveryStatus);
            restaurantStatus = (TextView) itemView.findViewById(R.id.restaurantStatus);
            restTime = (TextView) itemView.findViewById(R.id.restTime);

        }
    }


}
