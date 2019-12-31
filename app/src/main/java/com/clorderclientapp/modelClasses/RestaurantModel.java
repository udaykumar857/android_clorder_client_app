package com.clorderclientapp.modelClasses;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;

import java.util.ArrayList;

public class RestaurantModel {

    private String title;
    private String address;
    private String CuisineName;
    private String distance;
    private LatLng mLatLngRestaurant;
    private String imageUrl;
    private int restaurantID;
    private String lunchStatus;
    private String dinnerStatus;
    private String LunchDelivery;
    private String DinnerDelivery;
    private JSONArray timingsArray;
    private ArrayList<CuisineModel> cuisineModelArrayList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCuisineName() {
        return CuisineName;
    }

    public void setCuisineName(String cuisineName) {
        CuisineName = cuisineName;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }


    public LatLng getmLatLngRestaurant() {
        return mLatLngRestaurant;
    }

    public void setmLatLngRestaurant(LatLng mLatLngRestaurant) {
        this.mLatLngRestaurant = mLatLngRestaurant;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(int restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getLunchStatus() {
        return lunchStatus;
    }

    public void setLunchStatus(String lunchStatus) {
        this.lunchStatus = lunchStatus;
    }

    public String getDinnerStatus() {
        return dinnerStatus;
    }

    public void setDinnerStatus(String dinnerStatus) {
        this.dinnerStatus = dinnerStatus;
    }

    public String getLunchDelivery() {
        return LunchDelivery;
    }

    public void setLunchDelivery(String lunchDelivery) {
        LunchDelivery = lunchDelivery;
    }

    public String getDinnerDelivery() {
        return DinnerDelivery;
    }

    public void setDinnerDelivery(String dinnerDelivery) {
        DinnerDelivery = dinnerDelivery;
    }

    public JSONArray getTimingsArray() {
        return timingsArray;
    }

    public void setTimingsArray(JSONArray timingsArray) {
        this.timingsArray = timingsArray;
    }

    public ArrayList<CuisineModel> getCuisineModelArrayList() {
        return cuisineModelArrayList;
    }

    public void setCuisineModelArrayList(ArrayList<CuisineModel> cuisineModelArrayList) {
        this.cuisineModelArrayList = cuisineModelArrayList;
    }
}
