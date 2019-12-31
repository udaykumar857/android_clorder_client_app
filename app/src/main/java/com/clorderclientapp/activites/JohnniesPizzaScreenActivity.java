package com.clorderclientapp.activites;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.FontTextViewRegularClass;
import com.clorderclientapp.utils.Utils;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;

public class JohnniesPizzaScreenActivity extends AppCompatActivity implements View.OnClickListener,
        ResponseHandler, UserActionInterface, GoogleApiClient.OnConnectionFailedListener {
    private ImageView multionLocationChange;
    private LinearLayout reorderLastOrderLayout;
    private Button pickupBtn, deliveryBtn;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    public static GoogleApiClient mGoogleApiClient;
    private JSONArray restaurantTimingsList;
    int dayOfWeek = 0;
    private TextView orderDateTxt, orTxt;
    private Spinner orderTimeSpinner;
    private boolean isDeliveryOrder;
    private ArrayList<String> orderTimeList;
    private ArrayAdapter orderTimeAdapter;
    private AlertDialog alertDialog;
    private String selectedDate = null;
    private String selectedTime = null;
    private FontTextViewRegularClass scheduleText, registerTxt, loginTxt;
    private Button orderNowBtn, scheduleLaterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_johnnies_pizza_screen);
        httpRequest = new HttpRequest();
        initView();
        listeners();
        if (Constants.MultiLocationList.size() > 1) {
            multionLocationChange.setVisibility(View.VISIBLE);
        } else {
            multionLocationChange.setVisibility(View.INVISIBLE);
        }
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("ClientID", Constants.selectedRestaurantId);
        editor.apply();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        loginStatusMsg();
        if (Utils.isNetworkAvailable(this)) {
            fetchClientSettingsRequest();
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
        }

    }


    private void initView() {
        reorderLastOrderLayout = findViewById(R.id.reorder_lastorder_layout);
        pickupBtn = findViewById(R.id.pickup_btn);
        deliveryBtn = findViewById(R.id.delivery_btn);
        loginTxt = findViewById(R.id.loginTxt);
        registerTxt = findViewById(R.id.registerTxt);
        multionLocationChange = (ImageView) findViewById(R.id.multionLocationChange);
    }


    private void listeners() {
        reorderLastOrderLayout.setOnClickListener(this);
        pickupBtn.setOnClickListener(this);
        deliveryBtn.setOnClickListener(this);
        loginTxt.setOnClickListener(this);
        loginTxt.setOnClickListener(this);
        registerTxt.setOnClickListener(this);
        multionLocationChange.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginStatusMsg();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    private void loginStatusMsg() {
        boolean isLogin = sharedPreferences.contains("userCredentials");
        if (isLogin) {
            loginTxt.setText(String.format("%s", getString(R.string.sign_out_txt)));
        } else {
            loginTxt.setText(String.format("%s", getString(R.string.create_account_text)));
        }
    }

    private void fetchClientSettingsRequest() {
//        {
//            "clientId": 5
//        }
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Utils.getClientId(this));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("FetchClientSettingReq", requestObject.toString());
        httpRequest.fetchClientSettings(this, requestObject, Constants.FetchClientSettings);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        boolean isLogin = sharedPreferences.contains("userCredentials");
        boolean isDeliveryKey = sharedPreferences.contains("isDeliveryOrder");
        switch (id) {
            case R.id.multionLocationChange:
                startActivity(new Intent(JohnniesPizzaScreenActivity.this, MultiLocationActivity.class));
                finish();
                break;
            case R.id.reorder_lastorder_layout:
                if (isLogin) {
                    try {
                        JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                        intent = new Intent(this, OrderHistoryActivity.class);
                        intent.putExtra("userId", userCredentials.getString("UserId"));
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    intent = new Intent(this, SignInUserActivity.class);
                    intent.putExtra("isReOrder", 1);
                    startActivity(intent);
                }
                break;
            case R.id.pickup_btn:
                isDeliveryOrder = false;
                try {
                    //Delivery Type 1-only pickup, 2-only delivery, 4-both
                    int type = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("DeliveryType");
                    if (type == 1 || type == 4) {
                        boolean isRestOpen = Constants.clientSettingsObject.getBoolean("isRestOpen");//To open port this key is used..
                        if (isRestOpen) {
                            boolean restaurantStatus = restaurantStatus(dayOfWeek);//this is used to check whether restaurant open or not...
//                        Realm realm = Realm.getDefaultInstance();
//                        CartModel cartModel = realm.where(CartModel.class).findFirst();
//                        if (cartModel != null) {
                            if (Constants.selectedDate != null && Constants.selectedTime != null) {
                                selectedDate = Constants.selectedDate;
                                selectedTime = Constants.selectedTime;
                            }
//                        }

                            if (!restaurantStatus) {
//                            Utils.showScheduleOrderDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_open_msg), Constants.ActionPickupOrderNow, Constants.ActionPickupScheduleOrder, true);
                                showCustomDialog(this, false);
                            } else {
                                showCustomDialog(this, true);
//                            Utils.showScheduleOrderDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_pickup_status_msg), Constants.ActionPickupOrderNow, Constants.ActionPickupScheduleOrder, false);
                            }
                            pickUpClick();
                        } else {
                            pickUpClick();
                            showCustomDialog(this, true);
//                        Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_pickup_status_msg), Constants.ActionRestaurantPickupStatus);
                        }
                    } else {
                        Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_pickup_status_msg), Constants.ActionRestaurantPickupStatus);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.delivery_btn:
                isDeliveryOrder = true;
                boolean isRestOpen = false;
                try {
                    //Delivery Type 1-only pickup, 2-only delivery, 4-both
                    int type = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("DeliveryType");
                    if (type == 2 || type == 4) {
                        isRestOpen = Constants.clientSettingsObject.getBoolean("isRestOpen");

                        if (isRestOpen) {
                            boolean isDeliveryStatus = restaurantDeliveryStatus(dayOfWeek);
                            boolean restaurantStatus = restaurantStatus(dayOfWeek);//this is used to check whether restaurant open or not...

                            //                        Realm realm = Realm.getDefaultInstance();
//                        CartModel cartModel = realm.where(CartModel.class).findFirst();
//                        if (cartModel != null) {
                            if (Constants.selectedDate != null && Constants.selectedTime != null) {
                                selectedDate = Constants.selectedDate;
                                selectedTime = Constants.selectedTime;
                            }
//                        }


                            if (!restaurantStatus) {
                                if (!isDeliveryStatus) {
                                    showCustomDialog(this, false);
//                                Utils.showScheduleOrderDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_delivery_status_msg), Constants.ActionDeliveryOrderNow, Constants.ActionDeliveryScheduleOrder, true);
                                } else {
                                    showCustomDialog(this, true);
//                                Utils.showScheduleOrderDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_delivery_status_msg), Constants.ActionDeliveryOrderNow, Constants.ActionDeliveryScheduleOrder, false);
                                }

                            } else {
                                showCustomDialog(this, true);
//                            Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_delivery_status_msg), Constants.ActionRestaurantDeliveryStatus);
                            }
                            deliveryClick();

                        } else {
                            deliveryClick();
                            showCustomDialog(this, true);
//                        Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_delivery_status_msg), Constants.ActionRestaurantDeliveryStatus);
                        }
                    } else {
                        Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_delivery_status_msg), Constants.ActionRestaurantDeliveryStatus);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.loginTxt:
                if (isDeliveryKey) {
                    SharedPreferences.Editor pickUpEditor = sharedPreferences.edit();
                    pickUpEditor.putBoolean("isDeliveryOrder", false);
                    pickUpEditor.apply();
                }
                if (isLogin) {
                    loginRegisterClick();
                } else {
                    intent = new Intent(this, SignInUserActivity.class);
                    intent.putExtra("isReOrder", 2);//Click on SignIn btn;
                    startActivity(intent);
                }
                break;
            case R.id.registerTxt:
                if (isDeliveryKey) {
                    SharedPreferences.Editor pickUpEditor = sharedPreferences.edit();
                    pickUpEditor.putBoolean("isDeliveryOrder", false);
                    pickUpEditor.apply();
                }
                if (isLogin) {
                    loginRegisterClick();
                }
                intent = new Intent(this, NewUserSignUpActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void loginRegisterClick() {
        Utils.logout(this);
        loginStatusMsg();
    }

    private void pickUpClick() {
        Intent intent;
        boolean isLogin = sharedPreferences.contains("userCredentials");
        SharedPreferences.Editor pickUpEditor = sharedPreferences.edit();
        pickUpEditor.putBoolean("isDeliveryOrder", false);
        pickUpEditor.apply();
//        if (isLogin) {
//            startActivity(new Intent(this, DeliveryAddressActivity.class));
//        } else {
//            intent = new Intent(this, SignInUserActivity.class);
//            intent.putExtra("isReOrder", 0);
//            startActivity(intent);
//        }
    }

    private void deliveryClick() {
        Intent intent;
        boolean isLogin = sharedPreferences.contains("userCredentials");
        SharedPreferences.Editor deliveryEditor = sharedPreferences.edit();
        deliveryEditor.putBoolean("isDeliveryOrder", true);
        deliveryEditor.apply();
//        if (isLogin) {
//            startActivity(new Intent(this, DeliveryAddressActivity.class));
//        } else {
//            intent = new Intent(this, SignInUserActivity.class);
//            intent.putExtra("isReOrder", 0);
//            startActivity(intent);
//        }
    }


    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {

            case Constants.FetchClientSettings:
                Utils.cancelLoadingScreen();
//            {
//                "RestaurentTimings": [
//                {
//                    "Day": 1,
//                        "LunchStartTime": "11:30:00",
//                        "LunchEndTime": "18:00:00",
//                        "DinnerStartTime": "18:30:00",
//                        "DinnerEndTime": "22:00:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "21:50:00",
//                        "LunchDeliveryStartTime": "11:30:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "21:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "00:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "21:50:00",
//                        "Closed": false,
//                        "DeliveryClosed": true,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 2,
//                        "LunchStartTime": "11:00:00",
//                        "LunchEndTime": "15:00:00",
//                        "DinnerStartTime": "17:00:00",
//                        "DinnerEndTime": "22:00:00",
//                        "LunchEarlyEndTime": "15:00:00",
//                        "DinnerEarlyEndTime": "21:50:00",
//                        "LunchDeliveryStartTime": "11:00:00",
//                        "LunchDeliveryEndTime": "15:00:00",
//                        "DinnerDeliveryStartTime": "17:00:00",
//                        "DinnerDeliveryEndTime": "21:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "00:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "21:50:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 3,
//                        "LunchStartTime": "11:30:00",
//                        "LunchEndTime": "14:30:00",
//                        "DinnerStartTime": "17:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "14:00:00",
//                        "DinnerEarlyEndTime": "23:50:00",
//                        "LunchDeliveryStartTime": "11:30:00",
//                        "LunchDeliveryEndTime": "14:15:00",
//                        "DinnerDeliveryStartTime": "17:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "01:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:50:00",
//                        "Closed": true,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 4,
//                        "LunchStartTime": "11:30:00",
//                        "LunchEndTime": "00:00:00",
//                        "DinnerStartTime": "00:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "23:59:00",
//                        "LunchDeliveryStartTime": "11:30:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "02:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:59:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 5,
//                        "LunchStartTime": "11:30:00",
//                        "LunchEndTime": "00:00:00",
//                        "DinnerStartTime": "00:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "23:59:00",
//                        "LunchDeliveryStartTime": "11:30:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "03:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:59:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 6,
//                        "LunchStartTime": "12:00:00",
//                        "LunchEndTime": "00:00:00",
//                        "DinnerStartTime": "00:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "23:59:00",
//                        "LunchDeliveryStartTime": "12:00:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "04:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:59:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 0,
//                        "LunchStartTime": "12:00:00",
//                        "LunchEndTime": "00:00:00",
//                        "DinnerStartTime": "00:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "23:59:00",
//                        "LunchDeliveryStartTime": "12:00:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "05:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:59:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                }
//                ],
//                "clientId": 5,
//                    "status": "The GetRestaurentHours call is successful",
//                    "isSuccess": true
//            }


                if (response != null) {
                    try {
                        JSONObject responseObject = (JSONObject) response;
                        if (responseObject.getBoolean("isSuccess")) {
                            Constants.clientSettingsObject = (JSONObject) response;
                            if (!responseObject.isNull("DeliveryAddresses")) {
                                Constants.addressList = responseObject.getJSONArray("DeliveryAddresses");
                            }
                            restaurantTimingsList = responseObject.getJSONObject("ClientSettings").getJSONArray("BusinessHours");
                            Calendar calendar = Calendar.getInstance();
//                            calendar.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                            int day = calendar.get(Calendar.DAY_OF_WEEK);
                            long timeGmt = calendar.getTimeInMillis();
                            Log.d("dayOfWeek", "" + day);//Sunday=1,Monday=2,Tueday=3Wednesday=4,Thursday=5,Friday=6,Saturday=7;
                            dayOfWeek = day - 1;
                            Log.d("DayOfWeek", "" + dayOfWeek);
                            int type = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("DeliveryType");
                            if (type == 1 || type == 4) {
                                pickupBtn.setVisibility(View.VISIBLE);
                            } else {
                                pickupBtn.setVisibility(View.GONE);
                            }

                            if (type == 2 || type == 4) {
                                deliveryBtn.setVisibility(View.VISIBLE);
                            } else {
                                deliveryBtn.setVisibility(View.GONE);
                            }
                            restaurantStatus(dayOfWeek);
                            restaurantDeliveryStatus(dayOfWeek);

                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), responseObject.getString("status"), Constants.ActionRestaurantHours);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), "Unable to get data.", Constants.ActionRestaurantHours);
                }
                break;

            case Constants.FetchRestTimeSlots:
                Utils.cancelLoadingScreen();
//            {
//                "isSuccess": true,
//                    "TimeSlots": "12:15 PM|12:15:00 PM-12:30 PM|12:30:00 PM-12:45 PM|12:45:00 PM-1:00 PM|1:00:00 PM-1:15 PM|1:15:00 PM-1:30 PM|1:30:00 PM-1:45 PM|1:45:00 PM-2:00 PM|2:00:00 PM-2:15 PM|2:15:00 PM|5:45 PM|5:45:00 PM-6:00 PM|6:00:00 PM-6:15 PM|6:15:00 PM-6:30 PM|6:30:00 PM-6:45 PM|6:45:00 PM-7:00 PM|7:00:00 PM-7:15 PM|7:15:00 PM-7:30 PM|7:30:00 PM-7:45 PM|7:45:00 PM-8:00 PM|8:00:00 PM-8:15 PM|8:15:00 PM-8:30 PM|8:30:00 PM-8:45 PM|8:45:00 PM-9:00 PM|9:00:00 PM-9:15 PM|9:15:00 PM-9:30 PM|9:30:00 PM-9:45 PM|9:45:00 PM-10:00 PM|10:00:00 PM-10:15 PM|10:15:00 PM-10:30 PM|10:30:00 PM-10:45 PM|10:45:00 PM-11:00 PM|11:00:00 PM-11:15 PM|11:15:00 PM-11:30 PM|11:30:00 PM-11:45 PM|11:45:00 PM|",
//                    "date": "11/30/2016",
//                    "isPickup": false,
//                    "clientId": 5
//            }


//            {
//                "isSuccess": true,
//                    "TimeSlots": "Closed|",
//                    "date": "12/05/2016",
//                    "isPickup": false,
//                    "clientId": 5
//            }

                if (response != null) {
                    try {
                        JSONObject responseObject = (JSONObject) response;
                        if (responseObject.getBoolean("isSuccess")) {
                            if (responseObject.getString("TimeSlots").equals("Closed|")) {
                                scheduleText.setVisibility(View.VISIBLE);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                String nowDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
                                if (nowDate.equals(responseObject.getString("date"))) {
                                    orderNowBtn.setVisibility(View.GONE);
                                    orTxt.setVisibility(View.GONE);
                                    Constants.TimeSlotsTodayList.clear();
                                    Constants.TimeSlotsTodayList.add(getString(R.string.closed_txt));
                                }
                                orderTimeList.clear();
                                orderTimeList.add(getString(R.string.closed_txt));
                                orderTimeAdapter.notifyDataSetChanged();
                                orderTimeSpinner.setSelection(0);
                            } else {
                                orderTimeList.clear();
                                String timeDiv[] = responseObject.getString("TimeSlots").split("\\|");
                                for (int i = 0; i < timeDiv.length; i++) {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                    String todayDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
                                    if (timeDiv[i].contains("-")) {
                                        if (responseObject.getString("date").equals(todayDate)) {
                                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
//                                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT"));
                                            String nowTime = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis());

                                            if (timeInMinutes(timeDiv[i].split("-")[1]) > (timeInMinutes(nowTime))) {
                                                orderTimeList.add(timeDiv[i].split("-")[1]);
                                            }
                                        } else {
                                            orderTimeList.add(timeDiv[i].split("-")[1]);
                                        }

                                    } else {
                                        if (responseObject.getString("date").equals(todayDate)) {
                                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
//                                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                                            String nowTime = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis());
//                                            12:00 AM   2:00:00 AM
                                            String timeSplit[] = timeDiv[i].split(" ");
                                            if (timeSplit[0].split(":").length == 2) {
                                                if (timeInMinutes(timeDiv[i]) > (timeInMinutes(nowTime))) {
                                                    orderTimeList.add(timeDiv[i]);
                                                }
                                            }
                                        } else {
//                                            12:00 AM   2:00:00 AM
                                            String timeSplit[] = timeDiv[i].split(" ");
                                            if (timeSplit[0].split(":").length == 2) {
                                                orderTimeList.add(timeDiv[i]);
                                            }
                                        }

                                    }


                                }
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                                String nowDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
                                if (nowDate.equals(responseObject.getString("date"))) {
                                    orderNowBtn.setVisibility(View.VISIBLE);
                                    orTxt.setVisibility(View.VISIBLE);
                                    Constants.TimeSlotsTodayList.clear();
                                    if (orderTimeList.size() == 0) {
                                        Constants.TimeSlotsTodayList.add(getString(R.string.closed_txt));
                                    } else {
                                        Constants.TimeSlotsTodayList.addAll(orderTimeList);
                                    }

                                }

                                if (Constants.TimeSlotsTodayList.get(0).equals("Closed")) {
                                    orderNowBtn.setVisibility(View.GONE);
                                    orTxt.setVisibility(View.GONE);
                                }

                                if (orderTimeList.size() > 0) {
                                    scheduleText.setVisibility(View.GONE);
                                } else {
                                    orderTimeList.add(getString(R.string.closed_txt));
                                }
                                selectedDate = responseObject.getString("date");
                                orderTimeAdapter.notifyDataSetChanged();
                                if (selectedTime != null) {
                                    int position = 0;

                                    for (int i = 0; i < orderTimeList.size(); i++) {
                                        if (orderTimeList.get(i).equals(selectedTime)) {
                                            position = i;
                                            break;
                                        }

                                    }
                                    orderTimeSpinner.setSelection(position);
                                } else {
                                    orderTimeSpinner.setSelection(0);
                                }
                            }

                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), "Unable to get data.", Constants.ActionFetchRestTimeSlotsFailed);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), "Unable to get data.", Constants.ActionFetchRestTimeSlotsFailed);
                }


                break;
        }
    }


    private boolean restaurantStatus(int day) {
        boolean restaurantStatus = false;
        try {
            for (int i = 0; i < restaurantTimingsList.length(); i++)
                // In Api "Day": 0-Sunday, "Day": 1-Monday, "Day": 2-Tuesday, "Day": 3-Wednesday, "Day": 4-Thursday, "Day": 5-Friday, "Day": 6-Saturday,
                if (day == restaurantTimingsList.getJSONObject(i).getInt("Day")) {
//                    timingsUpdate(restaurantTimingsList.getJSONObject(i));
                    JSONObject updateObject = restaurantTimingsList.getJSONObject(i);

                    if (updateObject.getBoolean("Closed")) {
                        Constants.isRestaurantClosed = true;
                        restaurantStatus = true;
                        break;
                    } else {
                        Constants.isRestaurantClosed = false;
                    }
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurantStatus;
    }


    private boolean restaurantDeliveryStatus(int day) {
        boolean restaurantStatus = false;
        try {
            for (int i = 0; i < restaurantTimingsList.length(); i++)
                // In Api "Day": 0-Sunday, "Day": 1-Monday, "Day": 2-Tuesday, "Day": 3-Wednesday, "Day": 4-Thursday, "Day": 5-Friday, "Day": 6-Saturday,
                if (day == restaurantTimingsList.getJSONObject(i).getInt("Day")) {
//                    timingsUpdate(restaurantTimingsList.getJSONObject(i));
                    JSONObject updateObject = restaurantTimingsList.getJSONObject(i);

                    if (updateObject.getBoolean("DeliveryClosed")) {
                        Constants.isRestaurantDeliveryClosed = true;
                        restaurantStatus = true;
                        break;
                    } else {
                        Constants.isRestaurantDeliveryClosed = false;
                    }

                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurantStatus;
    }

    private void showCustomDialog(Context mContext, boolean isFutureOrder) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setCancelable(false);
        LayoutInflater layoutInflaterView = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflaterView.inflate(R.layout.activity_order_timings, null);
        dialogBuilder.setView(view);
        orderDateTxt = view.findViewById(R.id.order_date_txt);
        orderTimeSpinner = view.findViewById(R.id.order_time_spinner);
        scheduleText = view.findViewById(R.id.schedule_text);
        orderNowBtn = view.findViewById(R.id.order_now);
        scheduleLaterBtn = view.findViewById(R.id.schedule_later);
        orTxt = view.findViewById(R.id.or_txt);
        ImageView scheduleCloseBtn = (ImageView) view.findViewById(R.id.schedule_close_btn);
        if (isFutureOrder) {
            orderNowBtn.setVisibility(View.GONE);
            orTxt.setVisibility(View.GONE);
            scheduleText.setVisibility(View.VISIBLE);
        } else {
            orTxt.setVisibility(View.VISIBLE);
            orderNowBtn.setVisibility(View.VISIBLE);
        }
        orderTimeList = new ArrayList<>();
        orderTimeAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_text_view, orderTimeList);
        orderTimeSpinner.setAdapter(orderTimeAdapter);
        orderNowBtn.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {

                                               Calendar calendar = Calendar.getInstance();
//                                               calendar.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                                               int day = calendar.get(Calendar.DAY_OF_WEEK);
                                               if (scheduleOrder(day - 1)) {
                                                   alertDialog.dismiss();
                                                   Constants.selectedDate = selectedDate;
                                                   Constants.selectedTime = "ASAP";

                                                   boolean isLogin = sharedPreferences.contains("userCredentials");
                                                   if (isLogin) {
                                                       startActivity(new Intent(JohnniesPizzaScreenActivity.this, DeliveryAddressActivity.class));
                                                   } else {
                                                       Intent intent = new Intent(JohnniesPizzaScreenActivity.this, SignInUserActivity.class);
                                                       intent.putExtra("isReOrder", 0);
                                                       startActivity(intent);
                                                   }
                                               } else {
                                                   Utils.toastDisplay(JohnniesPizzaScreenActivity.this, "Presently restaurant is closed.Please schedule the order later.");
                                               }

                                               //write the code above...
//
//                                               boolean isAllowedToOrder = false;
//                                               if (!Constants.TimeSlotsTodayList.get(0).equals("Closed")) {
//                                                   SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
//                                                   simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
//                                                   String nowTime = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
//
//                                                   if (Constants.TimeSlotsTodayList.size() > 1) {
//                                                       ArrayList<String> todayList = new ArrayList<String>();
//                                                       int count = Constants.TimeSlotsTodayList.size();
//                                                       for (int i = 0; i < Constants.TimeSlotsTodayList.size(); i++) {
//
//                                                           if (timeInMinutes(Constants.TimeSlotsTodayList.get(i)) - timeInMinutes(Constants.TimeSlotsTodayList.get(i + 1)) == 15) {
//                                                               todayList.add(Constants.TimeSlotsTodayList.get(i));
//                                                               if (count == (i + 1)) {
//                                                                   todayList.add(Constants.TimeSlotsTodayList.get(i + 1));
//                                                                   break;
//                                                               }
//                                                           } else {
//                                                               for (int j = 0; j < todayList.size(); j++) {
//                                                                   if (timeInMinutes(todayList.get(j)) >= timeInMinutes(nowTime) && timeInMinutes(todayList.get(j)) <= timeInMinutes(nowTime)) {
//                                                                       isAllowedToOrder = true;
//                                                                       break;
//                                                                   } else {
//                                                                       isAllowedToOrder = false;
//                                                                   }
//                                                               }
//                                                           }
//                                                       }
//                                                   } else {
//                                                       if (timeInMinutes(Constants.TimeSlotsTodayList.get(0)) < timeInMinutes(nowTime)) {
//                                                           isAllowedToOrder = true;
//                                                       } else {
//                                                           isAllowedToOrder = false;
//                                                       }
//                                                   }
//
//                                                   if (isAllowedToOrder) {
//                                                       alertDialog.dismiss();
//                                                       Constants.selectedDate = selectedDate;
//                                                       Constants.selectedTime = "ASAP";
//                                                       boolean isLogin = sharedPreferences.contains("userCredentials");
//                                                       if (isLogin) {
//                                                           startActivity(new Intent(JohnniesPizzaScreenActivity.this, DeliveryAddressActivity.class));
//                                                       } else {
//                                                           Intent intent = new Intent(JohnniesPizzaScreenActivity.this, SignInUserActivity.class);
//                                                           intent.putExtra("isReOrder", 0);
//                                                           startActivity(intent);
//                                                       }
//                                                   }
//
//
//                                               } else
//
//                                               {
//                                                   scheduleText.setVisibility(View.VISIBLE);
//                                                   Utils.toastDisplay(JohnniesPizzaScreenActivity.this, "Please select other timings.");
//                                               }
//----------------------------------------------------------------------------------------------------

//                if (orderTimeList.get(orderTimeSpinner.getSelectedItemPosition()).equals("Closed")) {
//                    scheduleText.setVisibility(View.VISIBLE);
//                    Utils.toastDisplay(JohnniesPizzaScreenActivity.this, "Please select other timings.");
//
//                } else {
//                    alertDialog.dismiss();


//                    Constants.selectedDate = selectedDate;
//                    Constants.selectedTime = "ASAP";
//
//                    boolean isLogin = sharedPreferences.contains("userCredentials");
//                    if (isLogin) {
//                        startActivity(new Intent(JohnniesPizzaScreenActivity.this, DeliveryAddressActivity.class));
//                    } else {
//                        Intent intent = new Intent(JohnniesPizzaScreenActivity.this, SignInUserActivity.class);
//                        intent.putExtra("isReOrder", 0);
//                        startActivity(intent);
//                    }
//                }


                                           }
                                       }

        );

        scheduleLaterBtn.setOnClickListener(new View.OnClickListener()

                                            {
                                                @Override
                                                public void onClick(View v) {
                                                    if (orderTimeList.get(orderTimeSpinner.getSelectedItemPosition()).equals("Closed")) {
                                                        scheduleText.setVisibility(View.VISIBLE);
                                                        Utils.toastDisplay(JohnniesPizzaScreenActivity.this, "Please select other timings.");
                                                    } else {
                                                        alertDialog.dismiss();
//                    Realm realm = Realm.getDefaultInstance();
//                    try {
//                        realm.beginTransaction();
//                        CartModel cartModel = realm.where(CartModel.class).findFirst();
//                        if (cartModel == null) {
//                            cartModel = new CartModel();
//                            cartModel.setCartId(1);
//                        } else {
//                            cartModel.setCartId(cartModel.getCartId());
//                        }
//                        cartModel.setOrderPlacedDate(selectedDate);
//                        cartModel.setOrderPlacedTime(orderTimeList.get(orderTimeSpinner.getSelectedItemPosition()));
//                        realm.copyToRealmOrUpdate(cartModel);
//                        realm.commitTransaction();
//                        realm.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        realm.cancelTransaction();
//                    }
                                                        Constants.selectedDate = selectedDate;
                                                        Constants.selectedTime = orderTimeList.get(orderTimeSpinner.getSelectedItemPosition());
                                                        boolean isLogin = sharedPreferences.contains("userCredentials");
                                                        if (isLogin) {
                                                            startActivity(new Intent(JohnniesPizzaScreenActivity.this, DeliveryAddressActivity.class));
                                                        } else {
                                                            Intent intent = new Intent(JohnniesPizzaScreenActivity.this, SignInUserActivity.class);
                                                            intent.putExtra("isReOrder", 0);
                                                            startActivity(intent);
                                                        }
                                                    }
                                                }
                                            }

        );

        scheduleCloseBtn.setOnClickListener(new View.OnClickListener()

                                            {
                                                @Override
                                                public void onClick(View v) {
                                                    alertDialog.dismiss();
                                                }
                                            }

        );

        orderDateTxt.setOnClickListener(new View.OnClickListener()

                                        {
                                            @Override
                                            public void onClick(View v) {
                                                datePick();
                                            }
                                        }

        );
        orderDateTxt.setText(getString(R.string.today_txt));
//        scheduleOrder(dayOfWeek);
        alertDialog = dialogBuilder.create();
        alertDialog.show();

        if (selectedDate != null && (selectedTime != null) && (!selectedTime.equals("ASAP"))) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Integer.parseInt(selectedDate.split("-")[0]), (Integer.parseInt(selectedDate.split("-")[1]) - 1), Integer.parseInt(selectedDate.split("-")[2]));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d", Locale.getDefault());

            String userSelectedDate = simpleDateFormat.format(new Date(calendar.getTimeInMillis()));

            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));

            if (selectedDate.equals(simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis()))) {
                orderDateTxt.setText(getString(R.string.today_txt));
            } else {
                orderDateTxt.setText(String.format("%s", userSelectedDate));
            }
            fetchRestTimeSlotsRequest(selectedDate);
        } else {
            orderDateTxt.setText(getString(R.string.today_txt));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
            fetchRestTimeSlotsRequest(simpleDateFormat.format(Calendar.getInstance().getTimeInMillis()));
        }


    }

    private void fetchRestTimeSlotsRequest(String selectedDate) {
//        {
//                "clientId": "5",
//                "Date":"11/30/2016",
//                "isPickup":false
//        }
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Utils.getClientId(this));
            requestObject.put("Date", selectedDate);
            requestObject.put("isPickup", !isDeliveryOrder);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("fetchRestTimeSlotsReq", requestObject.toString());
        httpRequest.fetchRestTimeSlots(this, requestObject, Constants.FetchRestTimeSlots);
    }


    private void datePick() {

        if (selectedDate == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
            selectedDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
        }
//        Calendar cal1 = Calendar.getInstance();
//        cal1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Constants.timeZone));
//        cal1.set(Calendar.YEAR, Integer.parseInt(simpleDateFormat.format(new Date()).split("-")[0]));
//        cal1.set(Calendar.MONTH, (Integer.parseInt(simpleDateFormat.format(new Date()).split("-")[1]) - 1));
//        cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(simpleDateFormat.format(new Date()).split("-")[2]));

        String cal[] = selectedDate.split("-");
        Log.d("CurrentDatePicker", selectedDate);
        int year = Integer.parseInt(cal[0]);
        int month = Integer.parseInt(cal[1]) - 1;
        int day = Integer.parseInt(cal[2]);
        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                Log.d("selectedDate", "" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + "-" + DateUtils.formatDateRange(JohnniesPizzaScreenActivity.this, calendar.getTimeInMillis(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY));

                String weekDay = DateUtils.formatDateRange(JohnniesPizzaScreenActivity.this, calendar.getTimeInMillis(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY);

                String date = year + "-" + (((monthOfYear + 1) < 10) ? ("0" + (monthOfYear + 1)) : (monthOfYear + 1)) + "-" + ((dayOfMonth < 10) ? ("0" + dayOfMonth) : dayOfMonth);
                selectedDate = date;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d", Locale.getDefault());
                String userSelectedDate = simpleDateFormat.format(calendar.getTimeInMillis());
                Log.d("Formated Date", "" + userSelectedDate);
                int day = 0;
                switch (weekDay) {
                    case "Sunday":
                        day = 0;
                        break;
                    case "Monday":
                        day = 1;
                        break;
                    case "Tuesday":
                        day = 2;
                        break;
                    case "Wednesday":
                        day = 3;
                        break;
                    case "Thursday":
                        day = 4;
                        break;
                    case "Friday":
                        day = 5;
                        break;
                    case "Saturday":
                        day = 6;
                        break;
                }
                SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("Constants.timeZone));
                String todayDate = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis());

                if (date.equals(todayDate)) {
                    orderDateTxt.setText(getString(R.string.today_txt));
                    fetchRestTimeSlotsRequest(todayDate);
                } else {
                    orderDateTxt.setText(String.format("%s", userSelectedDate));
                    fetchRestTimeSlotsRequest(date);
                }

            }
        }, year, month, day);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd Z", Locale.getDefault());
        Calendar mCalendar = Calendar.getInstance();
//        format.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
//        format.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
//        TimeZone timeZone=TimeZone.getTimeZone("America/Los_Angeles");
//        Log.d("GetTimeZone",""+TimeZone.getTimeZone("America/Los_Angeles").getID());

        int dayOfMonth = Integer.parseInt((format.format(mCalendar.getTimeInMillis())).split(" ")[0].split("-")[2]);
        Log.d("dayOfMonth", "" + dayOfMonth);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mCalendar.set(Calendar.HOUR_OF_DAY, mCalendar.getMinimum(Calendar.HOUR_OF_DAY));
        mCalendar.set(Calendar.MINUTE, mCalendar.getMinimum(Calendar.MINUTE));
        mCalendar.set(Calendar.SECOND, mCalendar.getMinimum(Calendar.SECOND));
        mCalendar.set(Calendar.MILLISECOND, mCalendar.getMinimum(Calendar.MILLISECOND));
        dpd.getDatePicker().setMinDate(mCalendar.getTimeInMillis());
        Log.d("Minimum", "\t" + format.format(mCalendar.getTimeInMillis()));
        mCalendar.add(Calendar.DAY_OF_MONTH, 15);
        dpd.getDatePicker().setMaxDate(mCalendar.getTimeInMillis());
        Log.d("Maximum", "\t" + format.format(mCalendar.getTimeInMillis()));
        dpd.show();
    }


    private boolean scheduleOrder(int day) {
        boolean isAvailable = false;
        try {
            for (int i = 0; i < restaurantTimingsList.length(); i++)
                // In Api "Day": 0-Sunday, "Day": 1-Monday, "Day": 2-Tuesday, "Day": 3-Wednesday, "Day": 4-Thursday, "Day": 5-Friday, "Day": 6-Saturday,
                if (day == restaurantTimingsList.getJSONObject(i).getInt("Day")) {
                    isAvailable = timingsUpdate(restaurantTimingsList.getJSONObject(i));
                    break;
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isAvailable;
    }


    private boolean timingsUpdate(JSONObject updateObject) {
//            {
//                "Day": 1,
//                    "LunchStartTime": "11:30:00",
//                    "LunchEndTime": "18:00:00",
//                    "DinnerStartTime": "18:30:00",
//                    "DinnerEndTime": "22:00:00",
//                    "LunchEarlyEndTime": "00:00:00",
//                    "DinnerEarlyEndTime": "21:50:00",
//                    "LunchDeliveryStartTime": "11:30:00",
//                    "LunchDeliveryEndTime": "00:00:00",
//                    "DinnerDeliveryStartTime": "00:00:00",
//                    "DinnerDeliveryEndTime": "21:45:00",
//                    "BreakfastStartTime": "00:00:00",
//                    "BreakfastEndTime": "00:00:00",
//                    "BreakfastDeliveryStartTime": "00:00:00",
//                    "BreakfastDeliveryEndTime": "00:00:00",
//                    "ReservationCloseTime": "00:00:00",
//                    "ReservationEndTime": "21:50:00",
//                    "Closed": false,
//                    "DeliveryClosed": true,
//                    "ReservationClosed": false,
//                    "MorningOrderEndTime": "00:00:00",
//                    "NightOrderEndTime": "00:00:00",
//                    "DeliveryMorningOrderEndTime": "00:00:00",
//                    "DeliveryNightOrderEndTime": "00:00:00"
//            }

        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
//        simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
        String nowTime[] = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis()).split(":");
        int nowMinutes = Integer.parseInt(nowTime[0]) * 60 + Integer.parseInt(nowTime[1]);
        boolean isTimingPres = false;
        int offset = 0;
        try {

            if (isDeliveryOrder) {
                offset = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("DelTimeOffset");
                if (updateObject.getBoolean("DeliveryClosed") || updateObject.getBoolean("Closed")) {
//                    orderTimeList.clear();
//                    orderTimeList.add(getString(R.string.closed_txt));
//                    orderTimeAdapter.notifyDataSetChanged();
//                    orderTimeSpinner.setSelection(0);

                    Utils.toastDisplay(this, "Presently restaurant is closed.Please schedule the order later.");
                } else {


                    if ((!updateObject.getString("BreakfastDeliveryStartTime").equals("00:00:00")) ||
                            (!updateObject.getString("BreakfastDeliveryEndTime").equals("00:00:00"))) {
                        String timingStart[] = updateObject.getString("BreakfastDeliveryStartTime").split(":");
                        String timingEnd[] = updateObject.getString("BreakfastDeliveryEndTime").split(":");
                        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]) + offset;

                        if (updateObject.getString("BreakfastDeliveryEndTime").equals("00:00:00")) {
                            timingEnd = updateObject.getString("LunchDeliveryEndTime").split(":");
                        }


                        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);

                        if (nowMinutes >= startMinutes && (nowMinutes <= endMinutes)) {
                            isTimingPres = true;
                        }

//                        for (int i = startMinutes; i < endMinutes; i += 15) {
//                            boolean isTimingPresent = false;
//                            for (int j = 0; j < orderTimeList.size(); j++) {
//                                if (orderTimeList.contains(timeFormatting(i))) {
//                                    isTimingPresent = true;
//                                }
//                            }
//                            if (!isTimingPresent) {
//                                orderTimeList.add(timeFormatting(i));
//                            }
//                        }
                    }

                    if ((!updateObject.getString("LunchDeliveryStartTime").equals("00:00:00")) ||
                            (!updateObject.getString("LunchDeliveryEndTime").equals("00:00:00"))) {
                        String timingStart[] = updateObject.getString("LunchDeliveryStartTime").split(":");
                        String timingEnd[] = updateObject.getString("LunchDeliveryEndTime").split(":");


                        if (updateObject.getString("LunchDeliveryStartTime").equals("00:00:00")) {
                            timingStart = updateObject.getString("DinnerDeliveryStartTime").split(":");
                        }
                        if (updateObject.getString("LunchDeliveryEndTime").equals("00:00:00")) {
                            timingEnd = updateObject.getString("DinnerDeliveryEndTime").split(":");
                        }

                        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]) + offset;
                        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);

                        if (nowMinutes >= startMinutes && (nowMinutes <= endMinutes)) {
                            isTimingPres = true;
                        }

//                        for (int i = startMinutes; i < endMinutes; i += 15) {
//                            boolean isTimingPresent = false;
//                            for (int j = 0; j < orderTimeList.size(); j++) {
//                                if (orderTimeList.contains(timeFormatting(i))) {
//                                    isTimingPresent = true;
//                                }
//                            }
//                            if (!isTimingPresent) {
//                                orderTimeList.add(timeFormatting(i));
//                            }
//                        }
                    }


                    if ((!updateObject.getString("DinnerDeliveryStartTime").equals("00:00:00")) ||
                            (!updateObject.getString("DinnerDeliveryEndTime").equals("00:00:00"))) {

                        String timingStart[] = updateObject.getString("DinnerDeliveryStartTime").split(":");
                        String timingEnd[] = updateObject.getString("DinnerDeliveryEndTime").split(":");


                        if (updateObject.getString("DinnerDeliveryStartTime").equals("00:00:00")) {
                            timingStart = updateObject.getString("LunchDeliveryStartTime").split(":");
                        }

                        if (updateObject.getString("DinnerDeliveryEndTime").equals("00:00:00")) {
                            timingEnd = updateObject.getString("LunchDeliveryEndTime").split(":");
                        }


                        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]) + offset;
                        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);

                        if (nowMinutes >= startMinutes && (nowMinutes <= endMinutes)) {
                            isTimingPres = true;
                        }

//                        for (int i = startMinutes; i < endMinutes; i += 15) {
//                            boolean isTimingPresent = false;
//                            for (int j = 0; j < orderTimeList.size(); j++) {
//                                if (orderTimeList.contains(timeFormatting(i))) {
//                                    isTimingPresent = true;
//                                }
//                            }
//                            if (!isTimingPresent) {
//                                orderTimeList.add(timeFormatting(i));
//                            }
//                        }

                    }


                    orderTimeAdapter.notifyDataSetChanged();
                    orderTimeSpinner.setSelection(0);
                }
            } else {
                offset = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("PickupTimeOffset");
                if (updateObject.getBoolean("Closed")) {
//                    orderTimeList.clear();
//                    orderTimeList.add(getString(R.string.closed_txt));
//                    orderTimeAdapter.notifyDataSetChanged();
//                    orderTimeSpinner.setSelection(0);

                    Utils.toastDisplay(this, "Presently restaurant is closed.Please schedule the order later.");
                } else {

                    String endTimings = "23:55:00";

                    if ((!updateObject.getString("BreakfastStartTime").equals("00:00:00")) ||
                            (!updateObject.getString("BreakfastEndTime").equals("00:00:00"))) {
                        String timingStart[] = updateObject.getString("BreakfastStartTime").split(":");
                        String timingEnd[] = updateObject.getString("BreakfastEndTime").split(":");

                        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]) + offset;

                        if (updateObject.getString("BreakfastEndTime").equals("00:00:00")) {
                            timingEnd = updateObject.getString("LunchEndTime").split(":");
                        }
                        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);
                        if (nowMinutes >= startMinutes && (nowMinutes <= endMinutes)) {
                            isTimingPres = true;
                        }
//                        for (int i = startMinutes; i < endMinutes; i += 15) {
//                            boolean isTimingPresent = false;
//                            for (int j = 0; j < orderTimeList.size(); j++) {
//                                if (orderTimeList.contains(timeFormatting(i))) {
//                                    isTimingPresent = true;
//                                }
//                            }
//                            if (!isTimingPresent) {
//                                orderTimeList.add(timeFormatting(i));
//                            }
//                        }
                    }

                    if ((!updateObject.getString("LunchStartTime").equals("00:00:00")) ||
                            (!updateObject.getString("LunchEndTime").equals("00:00:00"))) {
                        String timingStart[] = updateObject.getString("LunchStartTime").split(":");
                        String timingEnd[] = updateObject.getString("LunchEndTime").split(":");

                        if (updateObject.getString("LunchStartTime").equals("00:00:00")) {
                            timingStart = updateObject.getString("DinnerStartTime").split(":");
                        }
                        if (updateObject.getString("LunchEndTime").equals("00:00:00")) {
                            timingEnd = updateObject.getString("DinnerEndTime").split(":");
                        }
                        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]) + offset;
                        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);

                        if (nowMinutes >= startMinutes && (nowMinutes <= endMinutes)) {
                            isTimingPres = true;
                        }
//                        for (int i = startMinutes; i < endMinutes; i += 15) {
//                            boolean isTimingPresent = false;
//                            for (int j = 0; j < orderTimeList.size(); j++) {
//                                if (orderTimeList.contains(timeFormatting(i))) {
//                                    isTimingPresent = true;
//                                }
//                            }
//                            if (!isTimingPresent) {
//                                orderTimeList.add(timeFormatting(i));
//                            }
//                        }
                    }

                    if ((!updateObject.getString("DinnerStartTime").equals("00:00:00")) ||
                            (!updateObject.getString("DinnerEndTime").equals("00:00:00"))) {

                        String timingStart[] = updateObject.getString("DinnerStartTime").split(":");
                        String timingEnd[] = updateObject.getString("DinnerEndTime").split(":");

                        if (updateObject.getString("DinnerStartTime").equals("00:00:00")) {
                            timingStart = updateObject.getString("LunchStartTime").split(":");
                        }

                        if (updateObject.getString("DinnerEndTime").equals("00:00:00")) {
                            timingEnd = updateObject.getString("LunchEndTime").split(":");
                        }
                        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]) + offset;
                        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);

                        if (nowMinutes >= startMinutes && (nowMinutes <= endMinutes)) {
                            isTimingPres = true;
                        }

//                        for (int i = startMinutes; i < endMinutes; i += 15) {
//                            boolean isTimingPresent = false;
//                            for (int j = 0; j < orderTimeList.size(); j++) {
//                                if (orderTimeList.contains(timeFormatting(i))) {
//                                    isTimingPresent = true;
//                                }
//                            }
//                            if (!isTimingPresent) {
//                                orderTimeList.add(timeFormatting(i));
//                            }
//                        }

                    }


//                    orderTimeAdapter.notifyDataSetChanged();
//                    orderTimeSpinner.setSelection(0);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return isTimingPres;
    }


    private String timeFormat(String time) {
        String updateTimings = "";
        String timing[] = time.split(":");
        String period;
        Integer hours = Integer.valueOf(timing[0]);
        if (hours > 12) {
            period = "PM";
            hours = hours - 12;
        } else if (hours == 12) {
            period = "PM";
        } else if (hours == 0) {
            hours = 12;
            period = "AM";
        } else {
            period = "AM";
        }
        updateTimings = ((hours < 10) ? ("0" + hours) : hours) + ":" + timing[1] + " " + period;
        return updateTimings;
    }


    private int timeInMinutes(String time) {
//        12:00 AM
//        9:15 PM

        int minutes = 0;

        String timeSpilt[] = time.split(" ");
        if (timeSpilt[1].equals("PM") && (Integer.parseInt(timeSpilt[0].split(":")[0]) < 12)) {
            minutes = ((Integer.parseInt(timeSpilt[0].split(":")[0]) + 12) * 60) + Integer.parseInt(timeSpilt[0].split(":")[1]);

        } else if (timeSpilt[1].equals("PM") && (Integer.parseInt(timeSpilt[0].split(":")[0]) == 12)) {
            minutes = ((Integer.parseInt(timeSpilt[0].split(":")[0])) * 60) + Integer.parseInt(timeSpilt[0].split(":")[1]);

        } else if (timeSpilt[1].equals("AM") && (Integer.parseInt(timeSpilt[0].split(":")[0]) < 12)) {
            minutes = ((Integer.parseInt(timeSpilt[0].split(":")[0])) * 60) + Integer.parseInt(timeSpilt[0].split(":")[1]);
        } else if (timeSpilt[1].equals("AM") && (Integer.parseInt(timeSpilt[0].split(":")[0]) == 12)) {
            minutes = ((Integer.parseInt(timeSpilt[0].split(":")[0]) - 12) * 60) + Integer.parseInt(timeSpilt[0].split(":")[1]);
        }


        return minutes;
    }


    private String timeFormatting(int minutes) {
        String updateTimings = "";
        String period;
        int hours = minutes / 60; //since both are ints, you get an int
        int minute = minutes % 60;
        String time = String.format("%d:%02d", hours, minute);
        if (hours > 12) {
            period = "PM";
            hours = hours - 12;
        } else if (hours == 12) {
            period = "PM";
        } else if (hours == 0) {
            hours = 12;
            period = "AM";
        } else {
            period = "AM";
        }
        updateTimings = ((hours < 10) ? ("0" + hours) : hours) + ":" + ((minute < 10) ? ("0" + minute) : minute) + " " + period;
        return updateTimings;
    }

    @Override
    public void userAction(int actionType) {
        switch (actionType) {
            case Constants.ActionRestaurantHours:
            case Constants.ActionNetworkFailed:
                finish();
                break;
            case Constants.ActionRestaurantPickupStatus:
//                pickUpClick();
                break;
            case Constants.ActionRestaurantDeliveryStatus:
//                deliveryClick();
                break;

//            case Constants.ActionPickupOrderNow:
//                showCustomDialog(this, "", "", 1, false);
//                break;
//
//            case Constants.ActionPickupScheduleOrder:
//                showCustomDialog(this, "", "", 1, true);
//                break;
//
//            case Constants.ActionDeliveryOrderNow:
//                showCustomDialog(this, "", "", 1, false);
//                break;
//            case Constants.ActionDeliveryScheduleOrder:
//                showCustomDialog(this, "", "", 1, true);
//                break;
        }


    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
