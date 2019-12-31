package com.clorderclientapp.httpClient;

public class URL {

//        private static final String BASE_URL = "http://devapi.clorder.com/";
    private static final String BASE_URL = "https://api.clorder.com/";

    private static final String AWS_BASE_URL = "https://xnabrco3j1.execute-api.us-east-2.amazonaws.com/prod/";

    public static final String RESTAURANT_DATA = AWS_BASE_URL + "api/";
    public static final String RESTAURANT_CUISINE_DATA = AWS_BASE_URL + "api/";
    public static final String CUISINE_DATA = AWS_BASE_URL + "api?cuisine=";

    public static final String FETCH_CLIENT_CHILD_LOCATIONS = BASE_URL + "ClorderMobile/FetchClientChildLocations";
    public static final String REGISTER_CLORDER_USER = BASE_URL + "ClorderMobile/registerClorderUser";
    public static final String LOGIN_USER = BASE_URL + "ClorderMobile/LoginUser";
    public static final String FETCH_CLIENT_ORDER_HISTORY = BASE_URL + "ClorderMobile/fetchClientOrderHistory";
    public static final String GET_ORDER_DETAILS = BASE_URL + "ClorderMobile/GetOrderDetails";
    public static final String FETCH_CLIENT_CATEGORIES = BASE_URL + "ClorderMobile/FetchClientCategories";
    public static final String FETCH_CLIENT_ITEMS = BASE_URL + "ClorderMobile/FetchClientItems";
    public static final String GET_MODIFIERS_FOR_ITEM = BASE_URL + "ClorderMobile/getModifiersForItem";
    public static final String GET_RESTAURANT_PROMOTIONS = BASE_URL + "ClorderMobile/GetRestaurentPromotions";
    public static final String FETCH_MENU_WITH_CATEGORIES = BASE_URL + "ClorderMobile/FetchMenuWithCategories";
    public static final String FETCH_TAX_FOR_ORDER = BASE_URL + "ClorderMobile/FetchTaxForOrder";
    public static final String FETCH_DELIVERY_FEES = BASE_URL + "ClorderMobile/FetchDeliveryFees";
    public static final String FETCH_DISCOUNT = BASE_URL + "ClorderMobile/fetchDiscount";
    public static final String PLACE_ORDER = BASE_URL + "ClorderMobile/PlaceOrder";
    public static final String UPDATE_CLORDER_USER = BASE_URL + "ClorderMobile/UpdateClorderUser";
    public static final String CLORDER_GOOGLE_SIGN_UP = BASE_URL + "ClorderMobile/ClorderGoogleSignup";
    public static final String FETCH_CLIENT_SETTINGS = BASE_URL + "ClorderMobile/FetchClientSettings";
    public static final String FETCH_REST_TIME_SLOTS = BASE_URL + "ClorderMobile/FetchRestTimeSlots";
    public static final String GET_ORDER_DETAILS_FOR_REORDER = BASE_URL + "ClorderMobile/GetOrderDetailsForReorder";
    public static final String CONFIRM_ORDER_POST_PAYMENT = BASE_URL + "ClorderMobile/ConfirmOrderPostPayment";

    public static final String RESET_USER_PASSWORD = BASE_URL + "ClorderUserAdmin/ResetUserPassword";
    public static final String CHANGE_USER_PASSWORD = BASE_URL + "ClorderUserAdmin/ChangeUserPassword";
}