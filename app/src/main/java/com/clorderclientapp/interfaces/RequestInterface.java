package com.clorderclientapp.interfaces;


import android.app.Activity;

import org.json.JSONObject;

public interface RequestInterface {

    public void restaurantData(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void restaurantCuisineData(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void getCuisineData(Activity mActivity, int requestNumber);

    public void fetchClientChildLocations(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchMenuWithCategories(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void registerClorderUser(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void loginUser(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void changeUserPassword(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void resetUserPassword(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchClientOrderHistory(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void getOrderDetails(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchClientCategoriesRequest(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchClientItems(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void getModifiersForItem(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void getRestaurantPromotions(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchTaxForOrder(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchDeliveryFees(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchDiscount(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void placeOrder(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void updateClorderUser(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void clorderGoogleSignUp(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchClientSettings(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void fetchRestTimeSlots(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void getOrderDetailsForReorder(Activity mActivity, JSONObject requestObject, int requestNumber);

    public void confirmOrderPostPayment(Activity mActivity, JSONObject requestObject, int requestNumber);


}
