package com.clorderclientapp.httpClient;

public class URL {

        private static final String BASE_URL = "http://devapi.clorder.com/ClorderMobile/";
//    private static final String BASE_URL = "https://api.clorder.com/ClorderMobile/";

    public static final String REGISTER_CLORDER_USER = BASE_URL + "registerClorderUser";
    public static final String LOGIN_USER = BASE_URL + "LoginUser";
    public static final String FETCH_CLIENT_ORDER_HISTORY = BASE_URL + "fetchClientOrderHistory";
    public static final String GET_ORDER_DETAILS = BASE_URL + "GetOrderDetails";
    public static final String FETCH_CLIENT_CATEGORIES = BASE_URL + "FetchClientCategories";
    public static final String FETCH_CLIENT_ITEMS = BASE_URL + "FetchClientItems";
    public static final String GET_MODIFIERS_FOR_ITEM = BASE_URL + "getModifiersForItem";
    public static final String GET_RESTAURANT_PROMOTIONS = BASE_URL + "GetRestaurentPromotions";
    public static final String FETCH_MENU_WITH_CATEGORIES = BASE_URL + "FetchMenuWithCategories";
    public static final String FETCH_TAX_FOR_ORDER = BASE_URL + "FetchTaxForOrder";
    public static final String FETCH_DELIVERY_FEES = BASE_URL + "FetchDeliveryFees";
    public static final String FETCH_DISCOUNT = BASE_URL + "fetchDiscount";
    public static final String PLACE_ORDER = BASE_URL + "PlaceOrder";
    public static final String UPDATE_CLORDER_USER = BASE_URL + "UpdateClorderUser";
    public static final String CLORDER_GOOGLE_SIGN_UP = BASE_URL + "ClorderGoogleSignup";
    public static final String FETCH_CLIENT_SETTINGS = BASE_URL + "FetchClientSettings";
    public static final String FETCH_REST_TIME_SLOTS = BASE_URL + "FetchRestTimeSlots";
    public static final String GET_ORDER_DETAILS_FOR_REORDER = BASE_URL + "GetOrderDetailsForReorder";
    public static final String CONFIRM_PAY_PAL_ORDER=BASE_URL+"ConfirmPaypalOrder";
}