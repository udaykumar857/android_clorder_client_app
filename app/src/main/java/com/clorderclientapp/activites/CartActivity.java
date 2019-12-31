package com.clorderclientapp.activites;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartItemModel;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.adapters.CartDetailsAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.HandleInterface;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmList;

public class CartActivity extends AppCompatActivity implements View.OnClickListener, HandleInterface,
        AdapterView.OnItemClickListener, ResponseHandler, UserActionInterface {

    private ListView cartDetailsListView;
    private CartDetailsAdapter cartDetailsAdapter;
    private Button addItemBtn, addBtn;
    private ImageView cartBackArrow, pickUpTickImg, deliveryTickImg;
    private TextView viewCouponsTxt, subtotalText, checkOutTxt, cartEmptyMsgTxt, taxAmountTxt, deliveryChargeAmountTxt, discountAmountTxt, applyCouponTxt;
    private LinearLayout cartDetailsLayout, orderTypeLayout, pickUpLayout, deliveryLayout;
    private EditText couponCode, cartSpecialNotes;
    public static final int VIEW_COUPON = 2001;
    private Spinner tipSpinner;
    private TextView totalCartAmount, tipValue;
    private RealmList<CartItemModel> cartItemList;
    private AlertDialog tipAlertDialog;
    private EditText tipDialogValue;
    private Button okBtn, cancelBtn;
    private float tipOthersTotal = 0.0f;
    private ScrollView scrollView;
    private SharedPreferences sharedPreferences;
    private HttpRequest httpRequest;
    private boolean isDeliveryOrder = true;
    private boolean isCouponApplied = true;
    private String deliveryStatusMsg = "";
    private boolean isDeliveryAddressValid = false;
    boolean isTaxRequest = false;
    private float defaultTipAmount = 0;
    private int shippingOptionsId = 0;//In the ClientSeeting Api if the ShippingOptionsId =4 then only the delivery cost is dependent on the subtotal
    private int validForOrderType = 0;//Coupon applied for pickup/delivery
    private int discountType = 0;//CouponType
    private String selectedDate = null;
    private String selectedTime = null;
    private TextView orderDateTxt, orTxt;
    ;
    private Spinner orderTimeSpinner;
    private ArrayList<String> orderTimeList;
    private ArrayAdapter orderTimeAdapter;
    private AlertDialog alertDialog;
    private TextView scheduleText;
    private Button orderNowBtn, scheduleLaterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_cart_activity);
        initView();

        try {
            if (Constants.clientSettingsObject.getJSONObject("ClientSettings").has("EnableTip")) {
                if (Constants.clientSettingsObject.getJSONObject("ClientSettings").getBoolean("EnableTip")) {
                    int defaultTip = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("DefaultTip");
                    if (defaultTip == 0) {
                        tipSpinner.setSelection(1);
                    } else {
                        defaultTipAmount = defaultTip;
                    }

                }
            }
            shippingOptionsId = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("ShippingOptionId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listeners();
        setData();
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);

        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        if (cartModel != null) {
            isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", false);
            if (isDeliveryOrder) {
                pickUpTickImg.setVisibility(View.INVISIBLE);
                deliveryTickImg.setVisibility(View.VISIBLE);
            } else {
                pickUpTickImg.setVisibility(View.VISIBLE);
                deliveryTickImg.setVisibility(View.INVISIBLE);
            }
            Log.d("CartModel", cartModel.toString());
            cartItemList = cartModel.getCartItemList();
            Log.d("cartItemList size", "" + cartItemList.size());
            cartDetailsAdapter = new CartDetailsAdapter(this, cartItemList);
            cartDetailsListView.setAdapter(cartDetailsAdapter);
            if (cartItemList.size() == 0) {
                cartDetailsLayout.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                checkOutTxt.setVisibility(View.GONE);
                orderTypeLayout.setVisibility(View.GONE);//Due to more Nested Layouts, Layout is outside the scroll view
                cartEmptyMsgTxt.setVisibility(View.VISIBLE);
                addBtn.setVisibility(View.VISIBLE);
            } else {
//                isCouponApplied = false;
                if (shippingOptionsId == 4) {
                    deliveryFeeRequest();
                }
                if (cartModel.getDiscountCode() != null) {
                    couponCode.setText(cartModel.getDiscountCode());
                    validForOrderType = cartModel.getValidForOrderType();
                    discountType = cartModel.getDiscountType();
                    fetchDiscountRequest();
                } else {
                    taxForOrderRequest(true);
                }

            }


        } else {
            cartDetailsLayout.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
            checkOutTxt.setVisibility(View.GONE);
            orderTypeLayout.setVisibility(View.GONE);//Due to more Nested Layouts, Layout is outside the scroll view
            cartEmptyMsgTxt.setVisibility(View.VISIBLE);
            addBtn.setVisibility(View.VISIBLE);
        }

    }

    private void initView() {
        cartDetailsListView = (ListView) findViewById(R.id.cart_detail_list);
        addItemBtn = (Button) findViewById(R.id.add_item_btn);
        cartBackArrow = (ImageView) findViewById(R.id.cart_back);
        viewCouponsTxt = (TextView) findViewById(R.id.view_coupons_txt);
        cartDetailsLayout = (LinearLayout) findViewById(R.id.cart_details_layout);
        subtotalText = (TextView) findViewById(R.id.subtotal_text);
        couponCode = (EditText) findViewById(R.id.coupon_code);
        tipSpinner = (Spinner) findViewById(R.id.tip_spinner);
        totalCartAmount = (TextView) findViewById(R.id.total_cart_amount);
        cartSpecialNotes = (EditText) findViewById(R.id.cart_special_notes);
        tipValue = (TextView) findViewById(R.id.tip_value);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        checkOutTxt = (TextView) findViewById(R.id.check_out_text);
        cartEmptyMsgTxt = (TextView) findViewById(R.id.cart_empty_msg_txt);
        pickUpLayout = (LinearLayout) findViewById(R.id.pickup_layout);
        deliveryLayout = (LinearLayout) findViewById(R.id.delivery_layout);
        orderTypeLayout = (LinearLayout) findViewById(R.id.order_type_layout);
        pickUpTickImg = (ImageView) findViewById(R.id.pick_up_tick_img);
        deliveryTickImg = (ImageView) findViewById(R.id.delivery_tick_img);
        taxAmountTxt = (TextView) findViewById(R.id.tax_amount_txt);
        deliveryChargeAmountTxt = (TextView) findViewById(R.id.delivery_charge_txt);
        discountAmountTxt = (TextView) findViewById(R.id.promotion_txt);
        applyCouponTxt = (TextView) findViewById(R.id.apply_coupon_txt);
        addBtn = (Button) findViewById(R.id.add_btn);
    }


    private void setData() {
        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        if (cartModel != null) {
            int position = cartModel.getTipPosition();
            if (position == 5) {
                tipValue.setText(String.format(Locale.getDefault(), "%f", cartModel.getTipAmount()));
                tipOthersTotal = cartModel.getTipAmount();
            }
            tipSpinner.setSelection(position);
        }
    }

    private void listeners() {
        addItemBtn.setOnClickListener(this);
        cartBackArrow.setOnClickListener(this);
        viewCouponsTxt.setOnClickListener(this);
        cartDetailsListView.setOnItemClickListener(this);
        tipValue.setOnClickListener(this);
        checkOutTxt.setOnClickListener(this);
        pickUpLayout.setOnClickListener(this);
        deliveryLayout.setOnClickListener(this);
        applyCouponTxt.setOnClickListener(this);
        addBtn.setOnClickListener(this);

        tipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position <= 4) {
                    tipValue.setEnabled(false);
                    cartDetailsUpdate(cartItemList);
                } else {
                    tipValue.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onBackPressed() {


        startActivity(new Intent(this, AllDayMenuActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Realm.getDefaultInstance().close();
    }

    @Override
    public void onClick(View v) {
        //Delivery Type 1-only pickup, 2-only delivery, 4-both
        int type = 0;
        try {
            type = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("DeliveryType");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (v.getId()) {

            case R.id.pickup_layout:
                if (type == 1 || type == 4) {
                    pickUpTickImg.setVisibility(View.VISIBLE);
                    deliveryTickImg.setVisibility(View.INVISIBLE);
                    Constants.selectedDate = null;
                    Constants.selectedTime = null;
                    SharedPreferences.Editor pickUpEditor = sharedPreferences.edit();
                    pickUpEditor.putBoolean("isDeliveryOrder", false);
                    pickUpEditor.apply();
//                isCouponApplied = false;

                    cartDetailsUpdate(cartItemList);
                    Realm realm = Realm.getDefaultInstance();
                    CartModel cartModel = realm.where(CartModel.class).findFirst();
                    if (cartModel.getDiscountCode() != null) {
                        fetchDiscountRequest();
                    }
                } else {
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_pickup_status_msg), Constants.ActionRestaurantPickupStatus);
                }
//                else {
//                    couponCode.setText("");
//                }
                break;
            case R.id.delivery_layout:

                if (type == 2 || type == 4) {
                    pickUpTickImg.setVisibility(View.INVISIBLE);
                    deliveryTickImg.setVisibility(View.VISIBLE);
                    Constants.selectedDate = null;
                    Constants.selectedTime = null;
                    SharedPreferences.Editor deliveryEditor = sharedPreferences.edit();
                    deliveryEditor.putBoolean("isDeliveryOrder", true);
                    deliveryEditor.apply();
//                isCouponApplied = false;
                    cartDetailsUpdate(cartItemList);
                    deliveryFeeRequest();
                    Realm realm1 = Realm.getDefaultInstance();
                    CartModel cartModel2 = realm1.where(CartModel.class).findFirst();
                    if (cartModel2.getDiscountCode() != null) {
                        fetchDiscountRequest();
                    }

                } else {
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_delivery_status_msg), Constants.ActionRestaurantDeliveryStatus);
                }

//                else {
//                    couponCode.setText("");
//                }
                break;
            case R.id.add_item_btn:
                onBackPressed();
                break;
            case R.id.cart_back:
                onBackPressed();
                break;
            case R.id.view_coupons_txt:
                startActivityForResult(new Intent(this, RestaurantPromotionsActivity.class), VIEW_COUPON);
                break;
            case R.id.apply_coupon_txt:
                if (couponCode.getText().toString().trim().length() > 0) {
                    fetchDiscountRequest();
                } else {
                    Utils.toastDisplay(this, getString(R.string.discount_msg));
                }
                break;
            case R.id.tip_value:
                tipDialog();
                break;
            case R.id.check_out_text:
                try {
                    boolean restaurantStatus = restaurantStatus();//this is used to check whether restaurant open or not...
//                        if (!restaurantStatus) {
                    if (isTaxRequest) {
                        if (isCouponApplied) {

                            if (sharedPreferences.getBoolean("isDeliveryOrder", false)) {
                                if (type == 2 || type == 4) {
//                                    if (!Constants.isRestaurantDeliveryClosed) {
                                    Float subtotal = Float.parseFloat(subtotalText.getText().toString().replace("$", ""));
                                    try {
                                        String value = String.valueOf(Constants.clientSettingsObject.getJSONObject("ClientSettings").getDouble("MinDelivery"));
                                        Float MinDelivery = Float.parseFloat(value);

                                        if (subtotal >= MinDelivery) {

                                            cartDetailsUpdate(cartItemList);
                                            if (Constants.selectedDate != null && Constants.selectedTime != null && !(Constants.isGuestUserLogin)) {
                                                selectedDate = Constants.selectedDate;
                                                selectedTime = Constants.selectedTime;
                                                if (!selectedTime.equals("ASAP")) {
                                                    Utils.showScheduleOrderDialog(this, getString(R.string.confirm_order_time), getString(R.string.your_order_schedule) + " " + selectedDate + " " + selectedTime, Constants.ActionScheduleDeliveryConfirm, Constants.ActionScheduleDeliveryChange);
                                                } else {
                                                    Utils.showScheduleOrderDialog(this, getString(R.string.confirm_order_time), " " + getString(R.string.your_order_schedule) + " ASAP", Constants.ActionScheduleDeliveryConfirm, Constants.ActionScheduleDeliveryChange);
                                                }

                                            } else {
                                                changeDeliverySchedule();
//                                            Utils.showScheduleOrderDialog(this, getString(R.string.confirm_order_time), getString(R.string.your_order_schedule) + "ASAP", Constants.ActionScheduleConfirm, Constants.ActionScheduleChange);
                                            }
//                                            }

//                                            else {
//                                                boolean restaurantStatus = restaurantStatus();
//                                                boolean isDeliveryStatus = restaurantDeliveryStatus();
//                                                if (!restaurantStatus) {
//                                                    if (!isDeliveryStatus) {
//                                                        showCustomDialog(this, false);
//                                                    } else {
//                                                        showCustomDialog(this, true);
//                                                    }
//                                                } else {
//                                                    showCustomDialog(this, true);
//                                                }
//                                            }

//
                                        } else {
                                            Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.minimum_delivery_amount_msg) + value, Constants.ActionMinDeliveryAmount);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_delivery_status_msg), Constants.ActionRestaurantDeliveryStatus);
                                }
//                                    } else {
//                                        Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_delivery_status_msg), Constants.ActionRestaurantDeliveryStatus);
//                                    }
                            } else {
                                if (type == 1 || type == 4) {
                                    cartDetailsUpdate(cartItemList);
//                                    if (!Constants.isRestaurantClosed) {
                                    if (Constants.selectedDate != null && Constants.selectedTime != null && !(Constants.isGuestUserLogin)) {
                                        selectedDate = Constants.selectedDate;
                                        selectedTime = Constants.selectedTime;
                                        if (!selectedTime.equals("ASAP")) {
                                            Utils.showScheduleOrderDialog(this, getString(R.string.confirm_order_time), getString(R.string.your_order_schedule) + " " + selectedDate + " " + selectedTime, Constants.ActionSchedulePickUpConfirm, Constants.ActionSchedulePickUpChange);
                                        } else {
                                            Utils.showScheduleOrderDialog(this, getString(R.string.confirm_order_time), getString(R.string.your_order_schedule) + " ASAP", Constants.ActionSchedulePickUpConfirm, Constants.ActionSchedulePickUpChange);
                                        }
                                    } else {
                                        changePickUpSchedule();
                                    }
//                                    startActivity(new Intent(this, UserCardListActivity.class));
//                                    } else {
//                                        Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_pickup_status_msg), Constants.ActionRestaurantPickupStatus);
//                                    }

                                } else {
                                    Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_pickup_status_msg), Constants.ActionRestaurantPickupStatus);
                                }
                            }
                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.coupon_msg_txt), Constants.ActionApplyCouponFailed);
                        }
                    } else {
                        if (shippingOptionsId == 4) {
                            deliveryFeeRequest();
                        } else {
                            taxForOrderRequest(true);
                        }

                    }
//                        } else {
//                            Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_status_msg), Constants.ActionRestaurantClosed);
//                        }


//                    else {
//
//                        Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.restaurant_status_msg), Constants.ActionRestaurantClosed);
//
//                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;
            case R.id.add_btn:
                onBackPressed();
                break;
        }

    }


    private void showCustomDialog(Context mContext, boolean isFutureOrder) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setCancelable(false);
        LayoutInflater layoutInflaterView = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflaterView.inflate(R.layout.activity_order_timings, null);
        dialogBuilder.setView(view);
        orderDateTxt = (TextView) view.findViewById(R.id.order_date_txt);
        orderTimeSpinner = (Spinner) view.findViewById(R.id.order_time_spinner);
        scheduleText = (TextView) view.findViewById(R.id.schedule_text);
        orderNowBtn = (Button) view.findViewById(R.id.order_now);
        scheduleLaterBtn = (Button) view.findViewById(R.id.schedule_later);
        orTxt = (TextView) view.findViewById(R.id.or_txt);
        TextView scheduleTitle = (TextView) view.findViewById(R.id.schedule_title);
        ImageView scheduleCloseBtn = (ImageView) view.findViewById(R.id.schedule_close_btn);
        scheduleTitle.setText(getString(R.string.confirm_schedule_txt));
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
                calendar.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                if (scheduleOrder(day - 1)) {
                    alertDialog.dismiss();
                    Constants.selectedDate = selectedDate;
                    Constants.selectedTime = "ASAP";

                    startActivity(new Intent(CartActivity.this, UserCardListActivity.class));
                } else {
                    Utils.toastDisplay(CartActivity.this, "Presently restaurant is closed.Please schedule the order later.");
                }


//                if (orderTimeList.get(orderTimeSpinner.getSelectedItemPosition()).equals("Closed")) {
//                    scheduleText.setVisibility(View.VISIBLE);
//                    Utils.toastDisplay(CartActivity.this, "Please select other timings.");
//                } else {
//                    alertDialog.dismiss();
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
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        realm.cancelTransaction();
//                    }

//                    Constants.selectedDate = selectedDate;
//                    Constants.selectedTime = "ASAP";
//                    startActivity(new Intent(CartActivity.this, UserCardListActivity.class));
//                }
            }
        });

        scheduleLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderTimeList.get(orderTimeSpinner.getSelectedItemPosition()).equals("Closed")) {
                    scheduleText.setVisibility(View.VISIBLE);
                    Utils.toastDisplay(CartActivity.this, "Please select other timings.");
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
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        realm.cancelTransaction();
//                    }

                    Constants.selectedDate = selectedDate;
                    Constants.selectedTime = orderTimeList.get(orderTimeSpinner.getSelectedItemPosition());

                    startActivity(new Intent(CartActivity.this, UserCardListActivity.class));
                }
            }
        });

        scheduleCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        orderDateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePick();
            }
        });

//        scheduleOrder(dayOfWeek);
        alertDialog = dialogBuilder.create();
        alertDialog.show();


        if (selectedDate != null && (selectedTime != null) && (!selectedTime.equals("ASAP"))) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Integer.parseInt(selectedDate.split("-")[0]), (Integer.parseInt(selectedDate.split("-")[1]) - 1), Integer.parseInt(selectedDate.split("-")[2]));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d", Locale.getDefault());
            String userSelectedDate = simpleDateFormat.format(new Date(calendar.getTimeInMillis()));


            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            if (selectedDate.equals(simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis()))) {
                orderDateTxt.setText(getString(R.string.today_txt));
            } else {
                orderDateTxt.setText(String.format("%s", userSelectedDate));
            }

            fetchRestTimeSlotsRequest(selectedDate);

        } else {
            orderDateTxt.setText(getString(R.string.today_txt));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            fetchRestTimeSlotsRequest(simpleDateFormat.format(Calendar.getInstance().getTimeInMillis()));
        }


    }

    private void datePick() {

        if (selectedDate == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            selectedDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
        }


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
//                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                Log.d("selectedDate", "" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + "-" + DateUtils.formatDateRange(CartActivity.this, calendar.getTimeInMillis(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY));

                String weekDay = DateUtils.formatDateRange(CartActivity.this, calendar.getTimeInMillis(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY);

                String date = year + "-" + (((monthOfYear + 1) < 10) ? ("0" + (monthOfYear + 1)) : (monthOfYear + 1)) + "-" + ((dayOfMonth < 10) ? ("0" + dayOfMonth) : dayOfMonth);
                selectedDate = date;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d", Locale.getDefault());
//                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
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
                simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
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
//        Log.d("CurrentCal1Millis1", "" + cal1.getTimeInMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd Z", Locale.getDefault());
        Calendar mCalendar = Calendar.getInstance();
        format.setTimeZone(TimeZone.getTimeZone("GMT-8"));
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
//        Log.d("CurrentMillis", "" + (System.currentTimeMillis() - 10000));

        dpd.show();
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
            requestObject.put("clientId", Constants.clientId);
            requestObject.put("Date", selectedDate);
            requestObject.put("isPickup", !isDeliveryOrder);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("fetchRestTimeSlotsReq", requestObject.toString());
        httpRequest.fetchRestTimeSlots(this, requestObject, Constants.FetchRestTimeSlots);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VIEW_COUPON:
                    String coupon = data.getExtras().getString("couponCode");
                    couponCode.setText(coupon);
                    validForOrderType = data.getExtras().getInt("validForOrderType");
                    discountType = data.getExtras().getInt("discountType");
                    break;
            }
        }

    }

    @Override
    public void handleClick(View view, int position) {
        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        realm.beginTransaction();
        cartItemList = cartModel.getCartItemList();
        cartItemList.remove(position);

//        cartModel.setCartItemList(cartItemList);

        if (cartItemList.size() == 0) {
            cartModel.setCartItemList(null);
            cartModel.setDiscountCode(null);
            cartDetailsLayout.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
            checkOutTxt.setVisibility(View.GONE);
            orderTypeLayout.setVisibility(View.GONE);//Due to more Nested Layouts, Layout is outside the scroll view
            cartEmptyMsgTxt.setVisibility(View.VISIBLE);
            addBtn.setVisibility(View.VISIBLE);
        }
        realm.copyToRealmOrUpdate(cartModel);
        realm.commitTransaction();
        cartDetailsAdapter.notifyDataSetChanged();
//        isCouponApplied = false;
        cartDetailsUpdate(cartItemList);

        if (cartItemList.size() > 0) {
            if (shippingOptionsId == 4) {
                deliveryFeeRequest();
            } else {
                taxForOrderRequest(true);
            }
            if (cartModel.getDiscountCode() != null) {
                fetchDiscountRequest();
            }

//            else {
//                couponCode.setText("");
//            }
        }
    }


    private void cartDetailsUpdate(RealmList<CartItemModel> cartItemModelArrayList) {
        if (cartItemModelArrayList.size() > 0) {
            Realm realm = Realm.getDefaultInstance();
            CartModel cartModel = realm.where(CartModel.class).findFirst();
            realm.beginTransaction();
            cartDetailsLayout.setVisibility(View.VISIBLE);

            //Subtotal Calculations done only in cart.
            float subtotalValue = 0.0f;
            for (int i = 0; i < cartItemModelArrayList.size(); i++) {
                subtotalValue += cartItemModelArrayList.get(i).getTotalItemPrice();
                for (int j = 0; j < cartItemModelArrayList.get(i).getItemModifiersList().size(); j++) {
                    Log.d("Items Modifiers List", cartItemModelArrayList.get(i).getItemModifiersList().get(j).toString());
                    for (int k = 0; k < cartItemModelArrayList.get(i).getItemModifiersList().get(j).getOptionsModifiersList().size(); k++) {
                        Log.d("options Modifiers List", cartItemModelArrayList.get(i).getItemModifiersList().get(j).getOptionsModifiersList().get(k).toString());
                    }
                }
            }
            subtotalValue = Utils.roundUpFloatValue(subtotalValue, 2);
            cartModel.setSubtotal(subtotalValue);
            subtotalText.setText(String.format("%s", "$" + Utils.roundFloatString(subtotalValue, 2)));
            float taxAmount = 0.0f;
            taxAmountTxt.setText(String.format("%s", "$" + Utils.roundFloatString(cartModel.getTax(), 2)));
            taxAmount = cartModel.getTax();
            int deliveryCharge = 0;
            isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", false);
            if (isDeliveryOrder) {
                deliveryChargeAmountTxt.setText(String.format("%s", "$" + Utils.roundFloatString(cartModel.getDeliveryCharge(), 2)));
            } else {
                deliveryChargeAmountTxt.setText(String.format("%s", "$0.00"));
            }
            deliveryCharge = cartModel.getDeliveryCharge();

            Log.d("spinner Position update", "" + tipSpinner.getSelectedItemPosition());
            int tipSpinnerPosition = tipSpinner.getSelectedItemPosition();
            cartModel.setTipPosition(tipSpinnerPosition);
            String tip[] = getResources().getStringArray(R.array.tip_array);
            float totalAmount = 0.0f;
            if (tipSpinnerPosition > 0 && tipSpinnerPosition < 5) {
                int tipPercent = Integer.parseInt(tip[tipSpinnerPosition].replace("%", ""));
                totalAmount = (((subtotalValue * tipPercent) / 100) + subtotalValue);
                tipValue.setText(String.valueOf(Utils.roundFloatString(((subtotalValue * tipPercent) / 100), 2)));
                cartModel.setTipAmount(Utils.roundUpFloatValue(((subtotalValue * tipPercent) / 100), 2));
            } else if (tipSpinnerPosition == 0) {
                totalAmount = subtotalValue + defaultTipAmount;
                tipValue.setText(Utils.roundFloatString(defaultTipAmount, 2));
                cartModel.setTipAmount(defaultTipAmount);
            } else {
                tipValue.setText("");
                totalAmount = tipOthersTotal + subtotalValue;
                tipValue.setText(String.valueOf(tipOthersTotal));
                cartModel.setTipAmount(tipOthersTotal);
            }
            if (isDeliveryOrder) {
                totalAmount = totalAmount + deliveryCharge + taxAmount;
            } else {
                totalAmount = totalAmount + taxAmount;
            }
            float discountAmount = 0.0f;
            if (isCouponApplied) {
                discountAmount = cartModel.getDiscountAmount();
                totalAmount = totalAmount - discountAmount;
            } else {
                discountAmount = cartModel.getDiscountAmount();
                totalAmount = totalAmount + discountAmount;
                cartModel.setDiscountAmount(0.0f);
                isCouponApplied = true;
                cartModel.setDiscountCode(null);
                discountAmount = 0.0f;
            }
            discountAmountTxt.setText(String.format("%s", "$" + Utils.roundFloatString(discountAmount, 2)));
            totalCartAmount.setText(String.format("%s", "$" + Utils.roundFloatString(totalAmount, 2)));
            cartModel.setOrderTotal(Utils.roundUpFloatValue(totalAmount, 2));
            cartModel.setSpecialNotes(cartSpecialNotes.getText().toString());
            realm.copyToRealmOrUpdate(cartModel);
            realm.commitTransaction();
            realm.close();
        } else {
            cartDetailsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent itemSelectionIntent = new Intent(this, ItemSelectionActivity.class);
        itemSelectionIntent.putExtra("modifierSelection", 1);
        itemSelectionIntent.putExtra("position", position);
        startActivity(itemSelectionIntent);
        finish();
    }


    private float tipDialog() {
        AlertDialog.Builder tipBuilder = new AlertDialog.Builder(this);
        tipBuilder.setCancelable(false);
        LayoutInflater layoutInflaterTip = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View tipView = layoutInflaterTip.inflate(R.layout.tip_value_dialog, null);
        okBtn = (Button) tipView.findViewById(R.id.tip_ok_button);
        cancelBtn = (Button) tipView.findViewById(R.id.tip_cancel_button);
        tipDialogValue = (EditText) tipView.findViewById(R.id.tip_value);
        tipDialogValue.append("0");
        tipBuilder.setView(tipView);
        tipAlertDialog = tipBuilder.create();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipDialogValue.getText().toString().trim().length() > 0) {
                    tipOthersTotal = Float.parseFloat(tipDialogValue.getText().toString());
                    tipAlertDialog.dismiss();
                    cartDetailsUpdate(cartItemList);
                } else {
                    Utils.toastDisplay(CartActivity.this, "Please enter the tip.");
                }

            }
        });
        tipDialogValue.requestFocus();
        InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipAlertDialog.dismiss();
                cartDetailsUpdate(cartItemList);
            }
        });
        tipAlertDialog.show();
        tipDialogValue.addTextChangedListener(myTextWatcher);
        return tipOthersTotal;
    }

    private final TextWatcher myTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                Log.d("onText : -> ", s.toString());
                tipDialogValue.removeTextChangedListener(this);
                String data = s.toString();
                Log.d("Length", "\t" + data.length());
                if (data.contains(".")) {
                    data = data.replaceAll("\\.", "");
                    tipDialogValue.setText("");
                    tipDialogValue.append(data);
                }
                if (data.length() >= 3) {
                    tipDialogValue.setText("");
                    data = new StringBuilder(data).insert(data.length() - 2, ".").toString();
                    tipDialogValue.append(data);
                }
                tipDialogValue.addTextChangedListener(this);
            } else {
                Log.d("Value", "Empty");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    private boolean tipValueValidation(String tipAmount) {
        Pattern pattern = Pattern.compile("^[1-9]\\d*(\\.\\d+)?$");
        Matcher matcher = pattern.matcher(tipAmount);
        float tipCost = 0.0f;
        if (matcher.matches()) {
            tipCost = Float.valueOf(tipAmount.toString());
            return true;
        }
        return false;
    }


    private void fetchDiscountRequest() {
//        {
//            "clientId":5,
//                "SubTotal":23.85,
//                "CouponCode":25,
//                "orderType":1,------Order Type -- 1 - Pickup 2 - Delivery -- Mandatory
//                "MemberId":92973
//        }
        int isCouponApplicable;

        isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", false);
        if (isDeliveryOrder) {
            isCouponApplicable = 2;//delivery
        } else {
            isCouponApplicable = 1;//pickup
        }
        if (isCouponApplicable == validForOrderType || validForOrderType == 4) {
            if (Utils.isNetworkAvailable(this)) {
                Utils.startLoadingScreen(this);
                JSONObject requestObject = new JSONObject();
                try {
                    requestObject.put("clientId", Constants.clientId);
                    Realm realm = Realm.getDefaultInstance();
                    CartModel cartModel = realm.where(CartModel.class).findFirst();
                    requestObject.put("SubTotal", "" + cartModel.getSubtotal());
                    requestObject.put("CouponType", discountType);
                    boolean orderType = sharedPreferences.getBoolean("isDeliveryOrder", false);
                    if (orderType) {
                        requestObject.put("orderType", 2);
                    } else {
                        requestObject.put("orderType", 1);
                    }
                    boolean isUserCredentials = sharedPreferences.contains("userCredentials");
                    if (isUserCredentials) {
                        JSONObject userCredentials = new JSONObject(sharedPreferences.getString("userCredentials", ""));
                        requestObject.put("MemberId", userCredentials.getString("UserId"));
                    } else {
                        requestObject.put("MemberId", 0);
                    }
                    requestObject.put("CouponCode", couponCode.getText().toString());
                    Log.d("fetchDiscountRequest", requestObject.toString());
                    httpRequest.fetchDiscount(this, requestObject, Constants.FetchDiscount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                isCouponApplied = false;//If Network fails to get Coupon details
                Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionDiscountFailed);
            }
        } else {
            taxForOrderRequest(true);
            if (isCouponApplicable == 1) {
                isCouponApplied = false;
                cartDetailsUpdate(cartItemList);
                Utils.showPositiveDialog(this, getString(R.string.message_txt), String.format("%s", getString(R.string.coupon_not_valid) + "PickUp orders."), Constants.ActionCouponNotValidOrder);
            } else {
                isCouponApplied = false;
                cartDetailsUpdate(cartItemList);
                Utils.showPositiveDialog(this, getString(R.string.message_txt), String.format("%s", getString(R.string.coupon_not_valid) + "Delivery orders."), Constants.ActionCouponNotValidOrder);
            }
        }
    }

    private boolean restaurantStatus() {
        boolean restaurantStatus = false;
        JSONArray restaurantTimingsList;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int day = dayOfWeek - 1;//android Calender will be start with sun and by response it start with Mon

        try {
            restaurantTimingsList = Constants.clientSettingsObject.getJSONObject("ClientSettings").getJSONArray("BusinessHours");
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

    private boolean restaurantDeliveryStatus() {
        boolean restaurantStatus = false;
        JSONArray restaurantTimingsList;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int day = dayOfWeek - 1;//android Calender will be start with sun and by response it start with Mon
        try {
            restaurantTimingsList = Constants.clientSettingsObject.getJSONObject("ClientSettings").getJSONArray("BusinessHours");
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

    private void deliveryFeeRequest() {
//        {
//            "clientId":5,
//                "Address1":"35 N Lake Ave #120",
//                "Address2":"Pasadena",
//                "cityname":"CA",
//                "zipcode":"91101",
//                "SubTotal":"15.75"
//
//        }
        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
            boolean isAddressDetails = sharedPreferences.contains("userDetails");
            JSONObject requestObject = new JSONObject();
            try {
                if (isAddressDetails) {
                    requestObject.put("clientId", Constants.clientId);
                    JSONObject userDetails = new JSONObject(sharedPreferences.getString("userDetails", ""));
                    requestObject.put("Address1", userDetails.getString("address"));
                    requestObject.put("Address2", userDetails.getString("buildings"));
                    requestObject.put("cityname", userDetails.getString("city"));
                    requestObject.put("zipcode", userDetails.getString("zipCode"));
                    Realm realm = Realm.getDefaultInstance();
                    CartModel cartModel = realm.where(CartModel.class).findFirst();
                    if (cartModel != null) {
                        if (shippingOptionsId == 4) {
                            requestObject.put("SubTotal", "" + Utils.roundUpFloatValue(cartModel.getSubtotal(), 2));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("fetchDeliveryFeeRequest", requestObject.toString());
            httpRequest.fetchDeliveryFees(this, requestObject, Constants.FetchDeliveryFees);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionDeliveryFeeFailed);
        }

    }


    private void taxForOrderRequest(boolean isLoadingState) {
//        {
//            "clientId":5,
//                "SubTotal":23.50
//        }
        if (Utils.isNetworkAvailable(this)) {

            if (isLoadingState) {
                Utils.startLoadingScreen(this);
            }

            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("clientId", Constants.clientId);

                Realm realm = Realm.getDefaultInstance();
                CartModel cartModel = realm.where(CartModel.class).findFirst();
                if (cartModel != null) {
                    requestObject.put("SubTotal", "" + Utils.roundUpFloatValue(cartModel.getSubtotal(), 2));
                    requestObject.put("DiscountAmount", "" + Utils.roundUpFloatValue(cartModel.getDiscountAmount(), 2));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("taxForOrderRequest", requestObject.toString());
            httpRequest.fetchTaxForOrder(this, requestObject, Constants.FetchTaxForOrder);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionFetchTaxForOrderFailed);
        }
    }


    private void getRestaurantHoursRequest() {
//        {
//            "clientId": 5
//        }
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Constants.clientId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("RestaurantHoursRequest", requestObject.toString());
        httpRequest.fetchClientSettings(this, requestObject, Constants.FetchClientSettings);
    }


    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {

            case Constants.FetchDeliveryFees:
                Utils.cancelLoadingScreen();
                if (shippingOptionsId == 4) {
                    taxForOrderRequest(true);
                }
                Realm realm = Realm.getDefaultInstance();
                CartModel cartModel = realm.where(CartModel.class).findFirst();
                if (cartModel != null) {
                    if (response != null) {
                        saveDeliveryFeeDetails(response);
                        JSONObject responseObject = (JSONObject) response;
                        realm.beginTransaction();
                        try {
                            cartModel.setDeliveryCharge(responseObject.getInt("DeliverCharge"));
                            cartModel.setDeliveryDist(Float.parseFloat(responseObject.getString("DeliveryDist")));
                            realm.copyToRealmOrUpdate(cartModel);
                            realm.commitTransaction();
                            realm.close();
                            isDeliveryAddressValid = responseObject.getBoolean("isSuccess");
                            deliveryStatusMsg = responseObject.getString("status");
                            if (!isDeliveryAddressValid) {
                                Utils.showPositiveDialog(this, getString(R.string.message_txt), deliveryStatusMsg, Constants.ActionDeliveryAddressStatus);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        cartDetailsUpdate(cartItemList);
                    } else {
                        Utils.showPositiveDialog(this, getString(R.string.message_txt), "Unable to get delivery fee.", Constants.ActionDeliveryAddressStatus);
                    }
                }
                break;

            case Constants.FetchTaxForOrder:
                Realm realm1 = Realm.getDefaultInstance();
                realm1.beginTransaction();
                CartModel cartModel1 = realm1.where(CartModel.class).findFirst();
                if (cartModel1 != null) {
                    if (response != null) {
                        JSONObject responseObject = (JSONObject) response;
                        try {
                            cartModel1.setTax(Utils.roundUpFloatValue(Float.parseFloat(responseObject.getString("TaxAmount")), 2));
                            realm1.copyToRealmOrUpdate(cartModel1);
                            realm1.commitTransaction();
                            realm1.close();
                            isTaxRequest = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    cartDetailsUpdate(cartItemList);
                }
                Utils.cancelLoadingScreen();
                break;

            case Constants.FetchDiscount:

                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;

//                    {
//                        "clientId": 5,
//                            "status": "The call to FetchDiscount success",
//                            "SubTotal": 25,
//                            "CouponCode": "SuperFree",
//                            "MemberId": 120678,
//                            "ItemIdofFreeItem": 73054,
//                            "DiscountAmount": 5.25,
//                            "orderType": 1,
//                            "isSuccess": true,
//                            "CouponType": 128
//                    }
                    try {
                        if (responseObject.getBoolean("isSuccess")) {
                            Realm realm2 = Realm.getDefaultInstance();
                            CartModel cartModel2 = realm2.where(CartModel.class).findFirst();
                            if (cartModel2 != null) {

                                realm2.beginTransaction();
                                cartModel2.setDiscountAmount(Float.parseFloat(responseObject.getString("DiscountAmount")));
                                cartModel2.setDiscountCode(responseObject.getString("CouponCode"));
                                cartModel2.setValidForOrderType(validForOrderType);
                                cartModel2.setDiscountType(responseObject.getInt("CouponType"));
                                realm2.copyToRealmOrUpdate(cartModel2);
                                realm2.commitTransaction();
                                realm2.close();
                                isCouponApplied = true;
                                Constants.discountObject = responseObject;
                                cartDetailsUpdate(cartItemList);
                            }

                            Utils.cancelLoadingScreen();
                            taxForOrderRequest(true); //Every time if discount amount is present we need to call tax api because some restaurant base on discount for tax Amount.By keeping discount amount in tax api.
                        } else {
                            couponCode.setText(""); //If coupon failed remove the coupon Text from UI.
                            Utils.cancelLoadingScreen();
                            taxForOrderRequest(false);//Showing dialog here so isLoading is false here.
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), responseObject.getString("status"), Constants.ActionFetchCouponFailed);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                break;

            case Constants.FetchClientSettings:
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
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if (responseObject.getBoolean("isSuccess")) {
                            scheduleOrderDialog(responseObject);
                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), responseObject.getString("status"), Constants.ActionRestaurantHours);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
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
                                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                                    String todayDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
                                    if (timeDiv[i].contains("-")) {
                                        if (responseObject.getString("date").equals(todayDate)) {
                                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
                                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
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
                                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
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
                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
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


    private void saveDeliveryFeeDetails(Object response) {

//        {
//            "clientId": 5,
//                "status": "The call to FetchDeliveryFees is successful.",
//                "DeliveryDist": 0.1,
//                "DeliverCharge": 3,
//                "Address1": "35 N Lake Ave #120",
//                "Address2": "Pasadena",
//                "cityname": "CA",
//                "zipcode": "91101",
//                "SubTotal": 500
//        }

//        boolean isDeliveryFeeDetailsData = sharedPreferences.contains("deliveryFeeDetails");
        JSONObject responseObject = (JSONObject) response;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("deliveryFeeDetails", responseObject.toString());
        editor.apply();
    }

    private void scheduleOrderDialog(JSONObject responseObject) {

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        try {
            JSONArray restaurantTimingsList = responseObject.getJSONArray("RestaurentTimings");
            JSONObject todayTimingsObject = restaurantTimingsList.getJSONObject(day);


        } catch (JSONException e) {
            e.printStackTrace();
        }

//        switch (day) {
//            case 1:
//                break;
//            case 2:
//                break;
//            case 3:
//                break;
//            case 4:
//                break;
//            case 5:
//                break;
//            case 6:
//                break;
//            case 0:
//                break;
//        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_order_timings, null);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialogView);
        alertDialog.show();

//        Spinner orderDateSpinner = (Spinner) dialogView.findViewById(R.id.order_date_spinner);
        Spinner orderTimeSpinner = (Spinner) dialogView.findViewById(R.id.order_time_spinner);


        ArrayList<String> orderDateList = new ArrayList<>();
        ArrayList<String> orderTimeList = new ArrayList<>();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        String dayOfWeek = simpleDateFormat.format(new Date());
        Log.d("dayOfWeek", "" + dayOfWeek);
        try {
            JSONArray restaurantTimingsList = responseObject.getJSONArray("RestaurentTimings");
            for (int i = 0; i < restaurantTimingsList.length(); i++)

                switch (dayOfWeek) {
                    case "Monday":// "Day": 1
                        break;
                    case "Tuesday":// "Day": 2
                        break;
                    case "Wednesday":// "Day": 3
                        break;
                }


        } catch (JSONException e) {
            e.printStackTrace();
        }


//        Cursor reasonCursor = dataBaseHandler.getReasons(orderType);
//        sampleSpinnerList.add(getResources().getString(R.string.select_reason));
//        for (int i = 0; i < reasonCursor.getCount(); i++) {
//            reasonList.add(reasonCursor.getString(reasonCursor.getColumnIndex(DataBaseHandler.REASON_ID)));
//            sampleSpinnerList.add(reasonCursor.getString(reasonCursor.getColumnIndex(DataBaseHandler.REASON_NAME)));
//            reasonCursor.moveToNext();
//        }
//        reasonCursor.close();

        ArrayAdapter orderDateAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_text_view, orderDateList);
//        orderDateSpinner.setAdapter(orderDateAdapter);

        ArrayAdapter orderTimeAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_text_view, orderTimeList);
        orderTimeSpinner.setAdapter(orderTimeAdapter);
    }


    private void changeDeliverySchedule() {
        boolean restaurantStatus = restaurantStatus();
        boolean isDeliveryStatus = restaurantDeliveryStatus();
        boolean isRestOpen = false;
        try {
            isRestOpen = Constants.clientSettingsObject.getBoolean("isRestOpen");

            if (isRestOpen) {//this key is for when holiday for restaurant(Special)
                if (!restaurantStatus) {//When restaurant is closed on particular day .(both pick up and delivery)
                    if (!isDeliveryStatus) {//when only delivery is closed on particular day.
                        showCustomDialog(this, false);
                    } else {
                        showCustomDialog(this, true);
                    }
                } else {
                    showCustomDialog(this, true);
                }
            } else {
                showCustomDialog(this, true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void changePickUpSchedule() {
        boolean isRestOpen = false;
        selectedDate = Constants.selectedDate;
        selectedTime = Constants.selectedTime;
        boolean restaurantStatus = restaurantStatus();
        try {
            isRestOpen = Constants.clientSettingsObject.getBoolean("isRestOpen");
            if (isRestOpen) {//this key is for when holiday for restaurant(Special)
                if (!restaurantStatus) {
                    showCustomDialog(this, false);
                } else {
                    showCustomDialog(this, true);
                }
            } else {
                showCustomDialog(this, true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean scheduleOrder(int day) {
        boolean isAvailable = false;
        try {
            JSONArray restaurantTimingsList = Constants.clientSettingsObject.getJSONObject("ClientSettings").getJSONArray("BusinessHours");
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
        simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
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
                if (updateObject.getBoolean("Closed")) {
//                    orderTimeList.clear();
//                    orderTimeList.add(getString(R.string.closed_txt));
//                    orderTimeAdapter.notifyDataSetChanged();
//                    orderTimeSpinner.setSelection(0);


                } else {
                    offset = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("PickupTimeOffset");
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


    @Override
    public void userAction(int actionType) {
        switch (actionType) {
            case Constants.ActionDeliveryAddressStatus:
                SharedPreferences.Editor pickUpEditor = sharedPreferences.edit();
                pickUpEditor.putBoolean("isDeliveryOrder", false);
                pickUpEditor.apply();
                pickUpTickImg.setVisibility(View.VISIBLE);
                deliveryTickImg.setVisibility(View.INVISIBLE);
                cartDetailsUpdate(cartItemList);
                break;
            case Constants.ActionScheduleDeliveryConfirm:
                startActivity(new Intent(CartActivity.this, UserCardListActivity.class));
                break;
            case Constants.ActionScheduleDeliveryChange:
                changeDeliverySchedule();
                break;
            case Constants.ActionSchedulePickUpConfirm:
                startActivity(new Intent(CartActivity.this, UserCardListActivity.class));
                break;
            case Constants.ActionSchedulePickUpChange:
                changePickUpSchedule();
                break;

        }
    }
}
