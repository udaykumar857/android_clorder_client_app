package com.clorderclientapp.httpClient;


import android.app.Activity;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.clorderclientapp.RealmModels.OptionsModifiersModel;
import com.clorderclientapp.interfaces.RequestInterface;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.modelClasses.CategoryItemModel;
import com.clorderclientapp.modelClasses.CategoryModel;
import com.clorderclientapp.modelClasses.CuisineModel;
import com.clorderclientapp.modelClasses.ItemModifiersModel;
import com.clorderclientapp.modelClasses.MenuModel;
import com.clorderclientapp.modelClasses.OrderHistoryModel;
import com.clorderclientapp.modelClasses.RestaurantModel;
import com.clorderclientapp.modelClasses.RestaurantPromotionsModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.VolleySingletonClass;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import io.realm.RealmList;

public class HttpRequest implements RequestInterface {

    private ResponseHandler mCallBack;

    @Override
    public void restaurantData(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.RESTAURANT_DATA, requestObject, new VolleyRequest.onSuccess() {
            @Override
            public void onResponse(int statusCode, Object response) {
                try {
                    JSONArray responseData = new JSONArray(response.toString());
                    Log.d("restaurantData", "" + responseData.toString());
                    ArrayList<RestaurantModel> restaurantModelArrayList = new ArrayList<>();
                    if (responseData.length() > 1) {
                        ArrayList<CuisineModel> cuisineModelArrayList = new ArrayList<>();
                        for (int i = 0; i < responseData.length(); i++) {
                            if (responseData.getJSONObject(i).has("Cuisines")) {
                                JSONArray cuisineArray = responseData.getJSONObject(i).getJSONArray("Cuisines");
                                for (int j = 0; j < cuisineArray.length(); j++) {
                                    CuisineModel cuisineModel = new CuisineModel();
                                    cuisineModel.setCname(cuisineArray.getJSONObject(j).getString("Cname"));
                                    cuisineModel.setCount(cuisineArray.getJSONObject(j).getInt("Count"));
                                    cuisineModelArrayList.add(cuisineModel);
                                }
                            } else {
                                RestaurantModel restaurantModel = new RestaurantModel();
                                restaurantModel.setTitle(responseData.getJSONObject(i).getString("Title"));
                                restaurantModel.setAddress(responseData.getJSONObject(i).getString("Address"));
                                restaurantModel.setCuisineName(responseData.getJSONObject(i).getString("CuisineName"));
                                restaurantModel.setDistance(responseData.getJSONObject(i).getString("Distance"));
                                restaurantModel.setImageUrl(responseData.getJSONObject(i).getString("ImageUrl"));
                                restaurantModel.setRestaurantID(Integer.parseInt(responseData.getJSONObject(i).getString("RestaurantID")));
                                restaurantModel.setmLatLngRestaurant(new LatLng(responseData.getJSONObject(i).getDouble("Lat"), responseData.getJSONObject(i).getDouble("Long")));
                                restaurantModel.setLunchStatus(responseData.getJSONObject(i).getString("Lunch"));
                                restaurantModel.setDinnerStatus(responseData.getJSONObject(i).getString("Dinner"));
                                restaurantModel.setLunchDelivery(responseData.getJSONObject(i).getString("LunchDelivery"));
                                restaurantModel.setDinnerDelivery(responseData.getJSONObject(i).getString("DinnerDelivery"));
                                restaurantModel.setTimingsArray(responseData.getJSONObject(i).getJSONArray("Timing"));
                                restaurantModel.setCuisineModelArrayList(cuisineModelArrayList);
                                restaurantModelArrayList.add(restaurantModel);
                            }
                        }
                        mCallBack.responseHandler(restaurantModelArrayList, requestNumber);
                    } else {
                        mCallBack.responseHandler(null, requestNumber);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);

    }

    @Override
    public void restaurantCuisineData(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.RESTAURANT_CUISINE_DATA, requestObject, new VolleyRequest.onSuccess() {
            @Override
            public void onResponse(int statusCode, Object response) {
                try {
                    JSONObject responseData = new JSONObject(response.toString());
                    Log.d("restaurantCuisineData", "" + responseData.toString());
                    if (responseData.length() > 0) {
                        ArrayList<CuisineModel> cuisineModelArrayList = new ArrayList<>();
                        if (responseData.has("Cuisines")) {
                            JSONArray cuisineArray = responseData.getJSONArray("Cuisines");
                            for (int j = 0; j < cuisineArray.length(); j++) {
                                CuisineModel cuisineModel = new CuisineModel();
                                cuisineModel.setCname(cuisineArray.getJSONObject(j).getString("Cname"));
                                cuisineModel.setCount(cuisineArray.getJSONObject(j).getInt("Count"));
                                cuisineModelArrayList.add(cuisineModel);
                            }
                        }
                        mCallBack.responseHandler(cuisineModelArrayList, requestNumber);
                    } else {
                        mCallBack.responseHandler(null, requestNumber);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void getCuisineData(Activity mActivity, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(URL.CUISINE_DATA, new VolleyRequest.onSuccess() {
            @Override
            public void onResponse(int statusCode, Object response) {
                JSONArray responseData = null;
                try {
                    responseData = new JSONArray(response.toString());
                    Log.d("getCuisineData", "" + responseData.toString());
                    ArrayList<CuisineModel> cuisineArrayList = new ArrayList<>();
                    if (responseData.length() > 1) {
                        for (int j = 0; j < responseData.length(); j++) {
                            CuisineModel cuisineModel = new CuisineModel();
                            cuisineModel.setCname(String.valueOf(responseData.get(j)));
                            cuisineArrayList.add(cuisineModel);
                        }
                        mCallBack.responseHandler(cuisineArrayList, requestNumber);
                    } else {
                        mCallBack.responseHandler(null, requestNumber);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }


    @Override
    public void fetchClientChildLocations(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_CLIENT_CHILD_LOCATIONS, requestObject, new VolleyRequest.onSuccess() {
            @Override
            public void onResponse(int statusCode, Object response) {
                try {
                    JSONObject responseData = new JSONObject(response.toString());
                    Log.d("fetchClientChildLoca", "" + responseData.toString());
                    if (responseData.has("status")) {
                        mCallBack.responseHandler(responseData, requestNumber);
                    } else {
                        mCallBack.responseHandler(null, requestNumber);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void fetchMenuWithCategories(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_MENU_WITH_CATEGORIES, requestObject, new VolleyRequest.onSuccess() {
            @Override
            public void onResponse(int statusCode, Object response) {
                try {
                    JSONObject responseData = new JSONObject(response.toString());
                    Log.d("fetchMenuWithCategories", "" + responseData.toString());
                    if (!responseData.isNull("clientMenuCategories")) {
                        JSONArray clientMenuArray = responseData.getJSONArray("clientMenuCategories");
                        ArrayList<MenuModel> menuModelArrayList = new ArrayList<>();
                        for (int i = 0; i < clientMenuArray.length(); i++) {
                            MenuModel menuModel = new MenuModel();
                            menuModel.setMenuTypeId(clientMenuArray.getJSONObject(i).getInt("TypeId"));
                            menuModel.setMenuTitle(clientMenuArray.getJSONObject(i).getString("Title"));
                            menuModel.setClientId(clientMenuArray.getJSONObject(i).getInt("ClientId"));
                            menuModel.setMenuIsDefault(clientMenuArray.getJSONObject(i).getBoolean("IsDefault"));
                            menuModel.setMenuOrderNo(clientMenuArray.getJSONObject(i).getInt("OrderNo"));
                            menuModel.setMenuIsVisible(clientMenuArray.getJSONObject(i).getBoolean("IsVisible"));
                            menuModel.setMenuStartTime(clientMenuArray.getJSONObject(i).getString("startTime"));
                            menuModel.setMenuEndTime(clientMenuArray.getJSONObject(i).getString("endTime"));
                            if (!(clientMenuArray.getJSONObject(i).isNull("Categories"))) {
                                JSONArray clientCategoriesArray = clientMenuArray.getJSONObject(i).getJSONArray("Categories");
                                ArrayList<CategoryModel> categoryModelArrayList = new ArrayList<>();
                                for (int j = 0; j < clientCategoriesArray.length(); j++) {
                                    CategoryModel categoryModel = new CategoryModel();
                                    categoryModel.setCategoryDesc(clientCategoriesArray.getJSONObject(j).getString("CategoryDescr"));
                                    categoryModel.setCategoryId(clientCategoriesArray.getJSONObject(j).getInt("CategoryId"));
                                    categoryModel.setCategoryTitle(clientCategoriesArray.getJSONObject(j).getString("CategoryTitle"));
                                    categoryModel.setCategoryMode(clientCategoriesArray.getJSONObject(j).getInt("Mode"));
                                    if (clientCategoriesArray.getJSONObject(j).has("CategoryImageURL")) {
                                        categoryModel.setImageUrl(clientCategoriesArray.getJSONObject(j).getString("CategoryImageURL"));
                                    } else {
                                        categoryModel.setImageUrl("https://s3.amazonaws.com/Clorder/Client/chickenplate-wb.png");
                                    }
                                    categoryModel.setExpanded(false);
                                    if (clientCategoriesArray.getJSONObject(j).has("Items")) {
                                        categoryModel.setItemsCnt(clientCategoriesArray.getJSONObject(j).getJSONArray("Items").length());
                                    }
                                    categoryModelArrayList.add(categoryModel);
                                }
                                menuModel.setCategoryArrayList(categoryModelArrayList);
                            }
                            menuModelArrayList.add(menuModel);
                        }
                        mCallBack.responseHandler(menuModelArrayList, requestNumber);

                    } else {
                        mCallBack.responseHandler(null, requestNumber);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void registerClorderUser(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.REGISTER_CLORDER_USER, requestObject, new VolleyRequest.onSuccess() {
            @Override
            public void onResponse(int statusCode, Object response) {
                try {
                    JSONObject responseData = new JSONObject(response.toString());
                    Log.d("registerClorderUser", "" + responseData.toString());
                    if (responseData.has("status")) {
                        mCallBack.responseHandler(responseData, requestNumber);
                    } else {
                        mCallBack.responseHandler(null, requestNumber);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void loginUser(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.LOGIN_USER, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("LoginUser", "" + responseData.toString());
                            if (responseData.has("status")) {
                                mCallBack.responseHandler(responseData, requestNumber);
                            } else {
                                mCallBack.responseHandler(null, requestNumber);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void changeUserPassword(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.CHANGE_USER_PASSWORD, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("changeUserPassword", "" + responseData.toString());
                            if (responseData.has("status")) {
                                mCallBack.responseHandler(responseData, requestNumber);
                            } else {
                                mCallBack.responseHandler(null, requestNumber);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void resetUserPassword(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.RESET_USER_PASSWORD, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("resetUserPassword", "" + responseData.toString());
                            mCallBack.responseHandler(responseData, requestNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void fetchClientOrderHistory(final Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_CLIENT_ORDER_HISTORY, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("fetchClientOrderHistory", "" + responseData.toString());

                            if (!responseData.isNull("ClientOrders")) {
                                ArrayList<OrderHistoryModel> orderHistoryList = new ArrayList<>();
                                JSONArray clientOrdersArray = responseData.getJSONArray("ClientOrders");
                                for (int i = 0; i < clientOrdersArray.length(); i++) {
                                    OrderHistoryModel orderHistoryModel = new OrderHistoryModel();

//                                    "OrderDate":"12/9/2016 7:21:41 AM"
                                    String orderDate = clientOrdersArray.getJSONObject(i).getString("OrderDate");
                                    Log.d("OrderDate", orderDate);
                                    SimpleDateFormat sourceFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa");
                                    sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                    Date parsed = null;
                                    try {
                                        parsed = sourceFormat.parse(orderDate);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d("OrderTime8", "" + parsed);
                                    TimeZone tz = TimeZone.getTimeZone(Constants.timeZone);
                                    SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
                                    destFormat.setTimeZone(tz);

                                    orderDate = destFormat.format(parsed);


                                    orderHistoryModel.setOrderDate(orderDate);
                                    orderHistoryModel.setOrderId(clientOrdersArray.getJSONObject(i).getString("orderid"));
                                    orderHistoryModel.setClientName(Constants.clientSettingsObject.getJSONObject("ClientSettings").getString("ClientName"));
                                    orderHistoryModel.setOrderTotal(clientOrdersArray.getJSONObject(i).getString("OrderTotal"));
                                    orderHistoryList.add(orderHistoryModel);
                                }

                                mCallBack.responseHandler(orderHistoryList, requestNumber);
                            } else {
                                mCallBack.responseHandler(null, requestNumber);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void getOrderDetails(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.GET_ORDER_DETAILS, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("getOrderDetails", "" + responseData.toString());
                            mCallBack.responseHandler(response, requestNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }


    @Override
    public void fetchClientCategoriesRequest(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;

        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_CLIENT_CATEGORIES, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("fetchClientCategories", "" + responseData.toString());
                            if (!responseData.isNull("clientCategories")) {
                                JSONArray clientCategoriesArray = responseData.getJSONArray("clientCategories");
                                ArrayList<CategoryModel> categoryModelArrayList = new ArrayList<>();
                                for (int i = 0; i < clientCategoriesArray.length(); i++) {
                                    CategoryModel categoryModel = new CategoryModel();
                                    categoryModel.setCategoryDesc(clientCategoriesArray.getJSONObject(i).getString("CategoryDescr"));
                                    categoryModel.setCategoryId(clientCategoriesArray.getJSONObject(i).getInt("CategoryId"));
                                    categoryModel.setCategoryTitle(clientCategoriesArray.getJSONObject(i).getString("CategoryTitle"));
                                    categoryModel.setCategoryMode(clientCategoriesArray.getJSONObject(i).getInt("Mode"));
                                    categoryModelArrayList.add(categoryModel);
                                }
                                mCallBack.responseHandler(categoryModelArrayList, requestNumber);
                            } else {
                                mCallBack.responseHandler(null, requestNumber);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new VolleyRequest.onFailure() {
                    @Override
                    public void onError(int statusCode, Object errorResponse) {
                        Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                        mCallBack.responseHandler(null, requestNumber);
                    }
                });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void fetchClientItems(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_CLIENT_ITEMS, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        JSONObject responseData = null;
                        try {
                            responseData = new JSONObject(response.toString());
                            Log.d("FetchClientItems", "" + responseData.toString());
                            ArrayList<CategoryItemModel> categoryItemModelArrayList = new ArrayList<>();
                            if (!responseData.isNull("clientItems")) {
                                JSONArray clientItemsArray = responseData.getJSONArray("clientItems");
                                for (int i = 0; i < clientItemsArray.length(); i++) {
                                    CategoryItemModel categoryItemModel = new CategoryItemModel();
                                    categoryItemModel.setCategoryId(clientItemsArray.getJSONObject(i).getInt("CategoryId"));
                                    categoryItemModel.setItemDesc(clientItemsArray.getJSONObject(i).getString("Description"));
                                    categoryItemModel.setItemImageUrl(clientItemsArray.getJSONObject(i).getString("ImageUrl"));
                                    categoryItemModel.setItemIsAvailable(clientItemsArray.getJSONObject(i).getBoolean("IsAvailable"));
                                    categoryItemModel.setItemIsDiscount(clientItemsArray.getJSONObject(i).getBoolean("IsDiscount"));
                                    categoryItemModel.setItemIsTaxable(clientItemsArray.getJSONObject(i).getBoolean("IsTaxable"));
                                    categoryItemModel.setItemCode(clientItemsArray.getJSONObject(i).getString("ItemCode"));
                                    categoryItemModel.setItemFilters(clientItemsArray.getJSONObject(i).getString("ItemFilters"));
                                    categoryItemModel.setItemId(clientItemsArray.getJSONObject(i).getInt("ItemId"));
                                    categoryItemModel.setItemTitleCode(clientItemsArray.getJSONObject(i).getString("ItemTitleCode"));
                                    categoryItemModel.setItemMinQuantity(clientItemsArray.getJSONObject(i).getInt("MinQuantity"));
                                    categoryItemModel.setItemMode(clientItemsArray.getJSONObject(i).getInt("Mode"));
                                    categoryItemModel.setItemNotes(clientItemsArray.getJSONObject(i).getString("Notes"));
                                    categoryItemModel.setItemOrderNo(clientItemsArray.getJSONObject(i).getInt("OrderNo"));
                                    categoryItemModel.setItemPrice(clientItemsArray.getJSONObject(i).getString("Price"));
                                    categoryItemModel.setItemTitle(clientItemsArray.getJSONObject(i).getString("Title"));
                                    categoryItemModel.setItemVisible(clientItemsArray.getJSONObject(i).getBoolean("Visible"));
                                    categoryItemModelArrayList.add(categoryItemModel);
                                }
                                mCallBack.responseHandler(categoryItemModelArrayList, requestNumber);
                            } else {
                                mCallBack.responseHandler(null, requestNumber);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void getModifiersForItem(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.GET_MODIFIERS_FOR_ITEM, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        JSONObject responseData = null;
                        try {
                            responseData = new JSONObject(response.toString());
                            Log.d("getModifiersForItem", "" + responseData.toString());
                            Constants.itemId = responseData.getString("ItemName");
                            RealmList<ItemModifiersModel> itemModifiersModelArrayList = new RealmList<>();
                            if (responseData.getJSONArray("clientFields").length() != 0) {

                                //            "Active": true,
//                "ID": 2,
//                "IsPriceField": false,
//                "MaxSelection": 0,
//                "Name": "How Spicy?",
//            "Price": null,
//                "Type": 0
                                for (int i = 0; i < responseData.getJSONArray("clientFields").length(); i++) {
                                    ItemModifiersModel itemModifiersModel = new ItemModifiersModel();
                                    itemModifiersModel.setModifierActive(responseData.getJSONArray("clientFields").getJSONObject(i).getBoolean("Active"));
//                            itemModifiersModel.setDisplayPrice(responseData.getJSONArray("clientFields").getJSONObject(i).getBoolean("DisplayPrice"));
                                    itemModifiersModel.setId(responseData.getJSONArray("clientFields").getJSONObject(i).getInt("ID"));
                                    itemModifiersModel.setPriceField(responseData.getJSONArray("clientFields").getJSONObject(i).getBoolean("IsPriceField"));
                                    itemModifiersModel.setMaxSelection(responseData.getJSONArray("clientFields").getJSONObject(i).getInt("MaxSelection"));
                                    itemModifiersModel.setModifierName(responseData.getJSONArray("clientFields").getJSONObject(i).getString("Name"));
                                    itemModifiersModel.setPrice(responseData.getJSONArray("clientFields").getJSONObject(i).getString("Price"));
                                    itemModifiersModel.setModifierType(responseData.getJSONArray("clientFields").getJSONObject(i).getInt("Type"));

                                    RealmList<OptionsModifiersModel> optionsModifiersModelArrayList = new RealmList<>();
                                    for (int k = 0; k < responseData.getJSONArray("clientFields").getJSONObject(i).getJSONArray("Options").length(); k++) {

                                        //    {
//        "Default": false,
//            "FieldId": 2,
//            "MaxSelectionPerField": 0,
//            "OptionId": 5,
//            "Price": null,
//            "Title": "Mild Spicy"
//    },
                                        JSONObject optionsResponseObject = responseData.getJSONArray("clientFields").getJSONObject(i).getJSONArray("Options").getJSONObject(k);
                                        OptionsModifiersModel optionsModifiersModel = new OptionsModifiersModel();
                                        optionsModifiersModel.setOptionsDefault(optionsResponseObject.getBoolean("Default"));
                                        //In case User doesn't select any modifiers...........
                                        boolean isOptionsSelected = optionsResponseObject.getBoolean("Default");
                                        if (isOptionsSelected) {
                                            itemModifiersModel.setIsModifierSelected(true);
                                        }
                                        optionsModifiersModel.setFieldId(optionsResponseObject.getInt("FieldId"));
                                        optionsModifiersModel.setMaxSelectionPerField(optionsResponseObject.getInt("MaxSelectionPerField"));
                                        optionsModifiersModel.setOptionId(optionsResponseObject.getInt("OptionId"));
                                        optionsModifiersModel.setPrice(optionsResponseObject.getString("Price"));
                                        optionsModifiersModel.setTitle(optionsResponseObject.getString("Title"));

                                        optionsModifiersModelArrayList.add(optionsModifiersModel);
                                    }
                                    itemModifiersModel.setOptionsModifiersList(optionsModifiersModelArrayList);
                                    itemModifiersModelArrayList.add(itemModifiersModel);
                                }


                            }
                            mCallBack.responseHandler(itemModifiersModelArrayList, requestNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void getRestaurantPromotions(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.GET_RESTAURANT_PROMOTIONS, requestObject, new VolleyRequest.onSuccess() {
            @Override
            public void onResponse(int statusCode, Object response) {
                JSONObject responseData = null;

                try {
                    responseData = new JSONObject(response.toString());
                    Log.d("getRestaurantPromotions", "" + responseData.toString());
                    ArrayList<RestaurantPromotionsModel> restaurantPromotionsList = new ArrayList<>();
                    JSONArray restaurantPromotionsArray = responseData.getJSONArray("RestaurentPromotions");
                    if (restaurantPromotionsArray.length() != 0) {
                        for (int i = 0; i < restaurantPromotionsArray.length(); i++) {
                            RestaurantPromotionsModel restaurantPromotionsModel = new RestaurantPromotionsModel();

//    "CouponId": 1315,
//            "Title": "1",
//            "Discount": 0,
//            "GratuityDiscount": 0,
//            "Active": true,
//            "CreateDate": 0,
//            "DiscountType": 1,
//            "TotalAmount": 0,
//            "ValidFor": 4,
//            "ValidForOrderType": 1,
//            "MinItemsForDiscount": 0,
//            "ItemsDiscountOn": 0,
//            "IsPublic": true,
//            "DateExpire": "2016-12-31T00:00:00",
//            "Description": "",
//            "ItemIdofFreeItem": 0,
//            "DiscountTotalAmount": 0,
//            "DiscountAmount": 0,
//            "QuantityofFreeItem": 0,
//            "MaxDiscount": 0

                            restaurantPromotionsModel.setCouponId(restaurantPromotionsArray.getJSONObject(i).getInt("CouponId"));
                            restaurantPromotionsModel.setCouponTitle(restaurantPromotionsArray.getJSONObject(i).getString("Title"));
                            restaurantPromotionsModel.setDiscount(restaurantPromotionsArray.getJSONObject(i).getInt("Discount"));
                            restaurantPromotionsModel.setGratuityDiscount(restaurantPromotionsArray.getJSONObject(i).getInt("GratuityDiscount"));
                            restaurantPromotionsModel.setActive(restaurantPromotionsArray.getJSONObject(i).getBoolean("Active"));
                            restaurantPromotionsModel.setCreateDate(restaurantPromotionsArray.getJSONObject(i).getString("CreateDate"));
                            restaurantPromotionsModel.setDiscountType(restaurantPromotionsArray.getJSONObject(i).getInt("DiscountType"));
                            restaurantPromotionsModel.setTotalAmount(restaurantPromotionsArray.getJSONObject(i).getInt("TotalAmount"));
                            restaurantPromotionsModel.setValidFor(restaurantPromotionsArray.getJSONObject(i).getInt("ValidFor"));
                            restaurantPromotionsModel.setValidForOrderType(restaurantPromotionsArray.getJSONObject(i).getInt("ValidForOrderType"));
                            restaurantPromotionsModel.setMinItemsForDiscount(restaurantPromotionsArray.getJSONObject(i).getInt("MinItemsForDiscount"));
                            restaurantPromotionsModel.setItemsDiscountOn(restaurantPromotionsArray.getJSONObject(i).getInt("ItemsDiscountOn"));
                            restaurantPromotionsModel.setPublic(restaurantPromotionsArray.getJSONObject(i).getBoolean("IsPublic"));
                            restaurantPromotionsModel.setDateExpire(restaurantPromotionsArray.getJSONObject(i).getString("DateExpire"));
                            restaurantPromotionsModel.setCouponDesc(restaurantPromotionsArray.getJSONObject(i).getString("Description"));
                            restaurantPromotionsModel.setItemIdofFreeItem(restaurantPromotionsArray.getJSONObject(i).getInt("ItemIdofFreeItem"));
                            restaurantPromotionsModel.setDiscountTotalAmount(restaurantPromotionsArray.getJSONObject(i).getInt("DiscountTotalAmount"));
                            restaurantPromotionsModel.setDiscountAmount(restaurantPromotionsArray.getJSONObject(i).getInt("DiscountAmount"));
                            restaurantPromotionsModel.setQuantityofFreeItem(restaurantPromotionsArray.getJSONObject(i).getInt("QuantityofFreeItem"));
                            restaurantPromotionsModel.setMaxDiscount(restaurantPromotionsArray.getJSONObject(i).getInt("MaxDiscount"));
                            restaurantPromotionsList.add(restaurantPromotionsModel);

                        }
                    }
                    mCallBack.responseHandler(restaurantPromotionsList, requestNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void fetchDiscount(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_DISCOUNT, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("fetchDiscount", "" + responseData.toString());

                            mCallBack.responseHandler(responseData, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void fetchTaxForOrder(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_TAX_FOR_ORDER, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("fetchTaxForOrder", "" + responseData.toString());

                            mCallBack.responseHandler(responseData, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void fetchDeliveryFees(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_DELIVERY_FEES, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("fetchDeliveryFees", "" + responseData.toString());

                            mCallBack.responseHandler(responseData, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void placeOrder(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.PLACE_ORDER, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("placeOrder", "" + responseData.toString());
                            mCallBack.responseHandler(responseData, requestNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void updateClorderUser(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.UPDATE_CLORDER_USER, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("updateClorderUser", "" + responseData.toString());

                            mCallBack.responseHandler(responseData, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void clorderGoogleSignUp(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.CLORDER_GOOGLE_SIGN_UP, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("clorderGoogleSignUp", "" + responseData.toString());

                            mCallBack.responseHandler(responseData, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void fetchClientSettings(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_CLIENT_SETTINGS, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("getRestaurentHours", "" + responseData.toString());

                            mCallBack.responseHandler(responseData, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void fetchRestTimeSlots(Activity mActivity, JSONObject requestObject, final int requestNumber) {
        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.FETCH_REST_TIME_SLOTS, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("fetchRestTimeSlots", "" + responseData.toString());

                            mCallBack.responseHandler(responseData, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);
    }

    @Override
    public void getOrderDetailsForReorder(Activity mActivity, JSONObject requestObject, final int requestNumber) {

        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.GET_ORDER_DETAILS_FOR_REORDER, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("getOrderDetailsReorder", "" + responseData.toString());

                            mCallBack.responseHandler(response, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);

    }

    @Override
    public void confirmOrderPostPayment(Activity mActivity, JSONObject requestObject, final int requestNumber) {

        mCallBack = (ResponseHandler) mActivity;
        VolleyRequest volleyRequest = new VolleyRequest(Request.Method.POST, URL.CONFIRM_ORDER_POST_PAYMENT, requestObject,
                new VolleyRequest.onSuccess() {
                    @Override
                    public void onResponse(int statusCode, Object response) {
                        try {
                            JSONObject responseData = new JSONObject(response.toString());
                            Log.d("confirmPayPalOrder", "" + responseData.toString());

                            mCallBack.responseHandler(response, requestNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new VolleyRequest.onFailure() {
            @Override
            public void onError(int statusCode, Object errorResponse) {
                Log.d("Fail", "\t" + statusCode + "Error : " + errorResponse.toString());
                mCallBack.responseHandler(null, requestNumber);
            }
        });
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingletonClass.getInstance(mActivity).addToRequestQueue(volleyRequest);

    }
}
