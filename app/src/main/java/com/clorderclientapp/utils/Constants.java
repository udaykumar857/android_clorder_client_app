package com.clorderclientapp.utils;

import com.clorderclientapp.modelClasses.CategoryItemModel;
import com.clorderclientapp.modelClasses.CategoryModel;
import com.clorderclientapp.modelClasses.ExistingCardModel;
import com.clorderclientapp.modelClasses.MenuModel;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;

public class Constants {
    public static final int clientId = 5;
    public static final ArrayList<MenuModel> MenuArrayList = new ArrayList<>();
    public static final ArrayList<CategoryModel> MenuCategoryArrayList = new ArrayList<>();
    public static final ArrayList<CategoryItemModel> CategoryItemList = new ArrayList<>();
    public static final ArrayList<String> TimeSlotsTodayList = new ArrayList<>();
    public static boolean isRestaurantClosed = false;
    public static boolean isRestaurantDeliveryClosed = false;
    public static boolean isGuestUserLogin = false;
    public static JSONObject clientSettingsObject;
    public static JSONObject discountObject;
    public static String selectedDate = null, selectedTime = null;
    public static String itemId = "";


    public static final int FetchMenuWithCategories = 1000;
    public static final int FetchClientCategories = 1001;
    public static final int FetchClientItems = 1002;
    public static final int GetModifiersForItem = 1003;
    public static final int GetRestaurantPromotions = 1004;
    public static final int RegisterClorderUser = 1005;
    public static final int LoginUser = 1006;
    public static final int FetchClientOrderHistory = 1007;
    public static final int GetOrderDetails = 1008;
    public static final int FetchTaxForOrder = 1009;
    public static final int FetchDeliveryFees = 1010;
    public static final int FetchDiscount = 1011;
    public static final int PlaceOrder = 1012;
    public static final int UpdateClorderUser = 1013;
    public static final int ClorderGoogleSignup = 1014;
    public static final int ClorderFacebookSignup = 1015;
    public static final int FetchClientSettings = 1016;
    public static final int FetchRestTimeSlots = 1017;
    public static final int GetOrderDetailsForReorder=1018;
    public static final int ConfirmPayPalOrder=1019;


    //Dialog Action Types
    public static final int ActionMenuWithCategoriesFailed = 2000;
    public static final int ActionNetworkFailed = 2001;
    public static final int ActionMenuItemFailed = 2002;
    public static final int ActionCartSubmitSuccess = 2003;
    public static final int ActionCartSubmitFailed = 2004;
    public static final int ActionCardSubmitFailed = 2005;
    public static final int ActionMinDeliveryAmount = 2006;
    public static final int ActionCardSubmit = 2007;
    public static final int ActionRestaurantHours = 2008;
    public static final int ActionReOrderGuestUser = 2009;
    public static final int ActionRestaurantPickupStatus = 2010;
    public static final int ActionRestaurantDeliveryStatus = 2011;
    public static final int ActionCreditCardRepeatedStatus = 2012;
    public static final int ActionDeliveryAddressStatus = 2013;
    public static final int ActionModifiersItemFailed = 2014;
    public static final int ActionRestaurantPromotionsFailed = 2015;
    public static final int ActionCreateClorderUserFailed = 2016;
    public static final int ActionLoginFailed = 2016;
    public static final int ActionClientOrderHistoryFailed = 2017;
    public static final int ActionClientOrderHistoryDetailsFailed = 2018;
    public static final int ActionDeliveryFeeFailed = 2019;
    public static final int ActionDiscountFailed = 2020;
    public static final int ActionPlaceOrderFailed = 2021;
    public static final int ActionUpdateClorderUserFailed = 2022;
    public static final int ActionRestaurantClosed = 2023;
    public static final int ActionSignUpFailed = 2024;
    public static final int ActionApplyCouponFailed = 2025;
    public static final int ActionFetchTaxForOrderFailed = 2026;
    public static final int ActionCouponNotValidOrder = 2027;
    public static final int ActionFetchRestTimeSlotsFailed = 2028;
    public static final int ActionCashLimitMsg = 2029;
    public static final int ActionMenuNotAvailable = 2030;
    public static final int ActionScheduleDeliveryConfirm = 2031;
    public static final int ActionScheduleDeliveryChange = 2032;
    public static final int ActionSchedulePickUpConfirm = 2033;
    public static final int ActionSchedulePickUpChange = 2034;
    public static final int ActionItemModifierFailed = 2035;
    public static final int ActionFetchCouponFailed = 2036;
    public static final int ActionGetOrderDetailsForReorderFailed=2037;
    public static final int ActionPasswordValidationAlert=2038;
}