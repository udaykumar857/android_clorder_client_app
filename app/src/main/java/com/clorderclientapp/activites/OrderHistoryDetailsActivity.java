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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.clorderclientapp.RealmModels.CartItemModel;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.RealmModels.OptionsModifiersModel;
import com.clorderclientapp.adapters.OrderDetailsAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.ItemDetailsModel;
import com.clorderclientapp.modelClasses.ItemModifiersModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.clorderclientapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;

public class OrderHistoryDetailsActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {

    private TextView orderIdTxt, placedOnTxt, orderDateDetailTxt, orderTypeTxt, paymentModeTxt,
            subtotal, discount, tax, deliveryFee, detailTip, donation, orderTotalAmount, footerText, specialNotesMsg;
    private ListView orderDetailsList;
    private OrderDetailsAdapter orderDetailsAdapter;
    private ArrayList<ItemDetailsModel> itemDetailsList;
    private HttpRequest httpRequest;
    private String orderId;
    private ImageView backImg;
    private float totalItemCost = 0.0f;
    private RealmList<ItemModifiersModel> itemModifiersList;
    private String userSelectedModifierOptions = "";
    private float modifierTotalAmount = 0.0f;
    private int itemQty = 1, minQty = 1;
    private float itemPrice = 0.0f;
    private Button pickupBtn, deliveryBtn;
    private boolean isDeliveryOrder;
    int dayOfWeek = 0;
    private String selectedDate = null;
    private String selectedTime = null;
    JSONArray restaurantTimingsList;
    private SharedPreferences sharedPreferences;
    private TextView orderDateTxt, orTxt;
    private Spinner orderTimeSpinner;
    private TextView scheduleText;
    private ArrayList<String> orderTimeList;
    private Button orderNowBtn, scheduleLaterBtn;
    private ArrayAdapter orderTimeAdapter;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history_details);
        initViews();
        listeners();
        itemDetailsList = new ArrayList<>();
        orderDetailsAdapter = new OrderDetailsAdapter(this, itemDetailsList);
        orderDetailsList.setAdapter(orderDetailsAdapter);
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        try {
            restaurantTimingsList = Constants.clientSettingsObject.getJSONObject("ClientSettings").getJSONArray("BusinessHours");
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (getIntent() != null) {
            orderId = getIntent().getExtras().getString("orderId", "0");
        }
        httpRequest = new HttpRequest();
        if (Utils.isNetworkAvailable(this)) {
            orderDetailRequest();
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionClientOrderHistoryDetailsFailed);
        }
    }

    private void initViews() {
        orderIdTxt = (TextView) findViewById(R.id.order_id_txt);
        placedOnTxt = (TextView) findViewById(R.id.placed_on_txt);
        orderDateDetailTxt = (TextView) findViewById(R.id.order_date_txt);
        orderTypeTxt = (TextView) findViewById(R.id.order_type_txt);
        paymentModeTxt = (TextView) findViewById(R.id.payment_mode_txt);
        subtotal = (TextView) findViewById(R.id.subtotal);
        discount = (TextView) findViewById(R.id.discount);
        tax = (TextView) findViewById(R.id.tax);
        deliveryFee = (TextView) findViewById(R.id.delivery_fee);
        detailTip = (TextView) findViewById(R.id.detail_tip);
        orderTotalAmount = (TextView) findViewById(R.id.order_total_amount);
//        donation = (TextView) findViewById(R.id.donation);
        orderDetailsList = (ListView) findViewById(R.id.order_details_list);
        specialNotesMsg = (TextView) findViewById(R.id.special_notes_msg);
//        footerText = (TextView) findViewById(R.id.orderNowBtn);
        backImg = (ImageView) findViewById(R.id.back_img);
        pickupBtn = (Button) findViewById(R.id.pickup_btn);
        deliveryBtn = (Button) findViewById(R.id.delivery_btn);
    }

    private void listeners() {
//        footerText.setOnClickListener(this);
        pickupBtn.setOnClickListener(this);
        deliveryBtn.setOnClickListener(this);
        backImg.setOnClickListener(this);
    }


    private void orderDetailRequest() {
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Utils.getClientId(this));
            requestObject.put("orderId", orderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        httpRequest.getOrderDetails(this, requestObject, Constants.GetOrderDetails);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.orderNowBtn:
//                if (Utils.isNetworkAvailable(this)) {
//                    getOrderDetailsForReorderRequest();
//                } else {
//                    Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionGetOrderDetailsForReorderFailed);
//                }
//                break;

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

            case R.id.back_img:
                onBackPressed();
                break;
        }

    }


    private void getOrderDetailsForReorderRequest() {

//        {
//                "clientId":5,
//                "OrderID":242193
//
//        }

        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Utils.getClientId(this));
            requestObject.put("OrderID", orderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("getOrderDetailsForReReq", requestObject.toString());
        httpRequest.getOrderDetailsForReorder(this, requestObject, Constants.GetOrderDetailsForReorder);
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.GetOrderDetails:
                if (response != null) {
                    try {
//                        "clientId": 5,
//                                "orderId": 121794,
//                                "statusMessage": "Order details processed successfully.",
//                                "requestStatus": true,
//                                "FacebookUrl": null,
//                                "ClientName": "Braintree Restaurant Demo",
//                                "SubTotal": "17.98",
//                                "Tax": "1.64",
//                                "ShippingCost": "0.00",
//                                "DiscountAmount": "0.00",
//                                "Total": "19.62",
//                                "SecurityCode": "3103",
//                                "OrderType": "PickUp",
//                                "SpecialNotes": "",
//                                "TipAmount": "",
//                                "OrderDate": "2016-08-18T23:37:00",
//                                "CreatedDate": "2016-08-18T23:36:58",
//                                "OrderTime": "Thursday 8/18/2016 11:37 PM",
//                                "IsFutureOrder": false,
//                                "IsASAPOrder": true,
//                                "CustomerName": "qq ",
//                                "CustomerPhoneNumber": "2096848765",
//                                "Email": "qq@qq.com",
//                                "CustomerAddress1": "xzzcz",
//                                "CustomerAddress2": "czxczxc",
//                                "CustomerCity": "www",
//                                "CustomerState": "CA",
//                                "CustomerZipCode": "08816",
//                                "CustomerCountry": "United States",
//                                "IsPrePaid": false,
                        JSONObject responseObject = new JSONObject((String) response);
                        orderIdTxt.setText(String.format("%s", "Order ID: " + responseObject.getString("orderId")));
//                                "CreatedDate": "2016-08-18T23:36:58",
                        String placedDate;
                        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Date parsed = null;
                        try {
                            placedDate = (responseObject.getString("CreatedDate")).replace("T", " ");
                            parsed = sourceFormat.parse(placedDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Log.d("CreatedDate", "" + parsed);
                        TimeZone tz = TimeZone.getTimeZone(Constants.timeZone);
                        SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
                        destFormat.setTimeZone(tz);

                        placedDate = destFormat.format(parsed);


//                        String createdDate[] = (responseObject.getString("CreatedDate")).split("T");
//                        String timingCreate[] = createdDate[1].split(":");
//                        String periodCreate;
//                        Integer hoursCreate = Integer.valueOf(timingCreate[0]);
//                        if (hoursCreate > 12) {
//                            periodCreate = "PM";
//                            hoursCreate = hoursCreate - 12;
//                        } else {
//                            periodCreate = "AM";
//                        }
//                        String orderTime[] = responseObject.getString("OrderTime").split(" ");
//                        placedOnTxt.setText(String.format("%s", "Placed On: " + createdDate[0] + " " + ((hoursCreate < 10) ? ("0" + hoursCreate) : hoursCreate) + ":" + timingCreate[1] + " " + periodCreate));

                        placedOnTxt.setText(String.format("%s", "Placed On: " + placedDate.replace("am", "AM").replace("pm", "PM")));

                        if (responseObject.getBoolean("IsASAPOrder")) {
                            orderDateDetailTxt.setText(String.format("%s", "Order Date: ASAP"));
                        } else {
//                            "OrderDate": "2016-08-18T23:37:00"
                            String orderDate;
                            SimpleDateFormat sourceFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            sourceFormat1.setTimeZone(TimeZone.getTimeZone("GMT"));
                            Date parsed1 = null;
                            try {
                                orderDate = (responseObject.getString("OrderDate")).replace("T", " ");
                                parsed1 = sourceFormat.parse(orderDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Log.d("Orderdate", "" + parsed1);
                            TimeZone tz1 = TimeZone.getTimeZone(Constants.timeZone);
                            SimpleDateFormat destFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
                            destFormat1.setTimeZone(tz1);

                            orderDate = destFormat.format(parsed1);

//                            String orderDate[] = (responseObject.getString("OrderDate")).split("T");
//                            String timing[] = orderDate[1].split(":");
//                            String period;
//                            Integer hours = Integer.valueOf(timing[0]);
//                            if (hours > 12) {
//                                period = "PM";
//                                hours = hours - 12;
//                            } else {
//                                period = "AM";
//                            }
                            orderDateDetailTxt.setText(String.format("%s", "Order Date: " + orderDate.replace("am", "AM").replace("pm", "PM")));
                        }
                        orderTypeTxt.setText(String.format("%s", "Order Type: " + responseObject.getString("OrderType")));
                        paymentModeTxt.setText(String.format("%s", "Payment Mode: " + responseObject.getString("paymentType")));

                        //OrderItems.............
                        JSONArray selectedItemsArray = responseObject.getJSONArray("SelectedItems");
                        itemDetailsList.clear();
                        for (int i = 0; i < selectedItemsArray.length(); i++) {
                            ItemDetailsModel itemsDetails = new ItemDetailsModel();
                            JSONObject itemObject = selectedItemsArray.getJSONObject(i);
                            itemsDetails.setItemName(itemObject.getString("Title"));
                            Integer quantity = itemObject.getInt("Quantity");
                            itemsDetails.setItemQuantity(quantity + "X");
                            itemsDetails.setTotalPrice(itemObject.getString("TotalPrice"));
                            // set visibility for notes and make it with and insert it into database
                            String note = itemObject.getString("Notes");
                            if (note.equals("")) {
                                itemsDetails.setIsNoteVisible("0");
                            } else {
                                itemsDetails.setIsNoteVisible("1");
                            }
                            itemsDetails.setNote(itemObject.getString("Notes"));

                            String makeItWith = itemObject.getString("Options");

                            if (makeItWith.equals("")) {
                                itemsDetails.setIsMakeItWithVisible("0"); // false
                            } else {
                                itemsDetails.setIsMakeItWithVisible("1"); // true
                            }
                            itemsDetails.setOptions(itemObject.getString("Options"));
                            itemDetailsList.add(itemsDetails);
                        }
                        orderDetailsAdapter.notifyDataSetChanged();

                        subtotal.setText(String.format("%s", "$" + responseObject.getString("SubTotal")));
                        String discounts = responseObject.getString("DiscountAmount");
                        if (discounts.equals("")) {
                            discount.setText(String.format("%s", "$0.00"));
                        } else {
                            discount.setText(String.format("%s", "$" + discounts));
                        }

                        String taxAmt = responseObject.getString("Tax");
                        if (taxAmt.equals("")) {
                            tax.setText(String.format("%s", "$0.00"));
                        } else {
                            tax.setText(String.format("%s", "$" + taxAmt));
                        }

                        String detailTips = responseObject.getString("TipAmount");
                        if (detailTips.equals("")) {
                            detailTip.setText(String.format("%s", "$0.00"));
                        } else {
                            detailTip.setText(String.format("%s", "$" + detailTips));
                        }

                        String totals = responseObject.getString("Total");
                        if (totals.equals("")) {
                            orderTotalAmount.setText(String.format("%s", "$0.00"));
                        } else {
                            orderTotalAmount.setText(String.format("%s", "$" + totals));
                        }

                        String deliveryFees = responseObject.getString("ShippingCost");
                        if (deliveryFees.equals("")) {
                            deliveryFee.setText(String.format("%s", "$0.00"));
                        } else {
                            deliveryFee.setText(String.format("%s", "$" + deliveryFees));
                        }

//                        String donationPay = responseObject.getString("Donation");
//                        if (donationPay.equals("")) {
//                            donation.setText(String.format("%s", "$0.00"));
//                        } else {
//                            donation.setText(String.format("%s", "$" + donationPay));
//                        }

                        String specialNotes = responseObject.getString("SpecialNotes");
                        if (specialNotes.equals("") || specialNotes.equals("null")) {
                            specialNotesMsg.setText("--");
                        } else {
                            specialNotesMsg.setText(specialNotes);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Utils.cancelLoadingScreen();
                break;

            case Constants.GetOrderDetailsForReorder:
                if (response != null) {
                    try {
                        Realm deleteRealm = Realm.getDefaultInstance();
                        try {
                            deleteRealm.beginTransaction();
                            deleteRealm.delete(CartModel.class);
                            deleteRealm.commitTransaction();
                        } catch (Exception e) {
                            e.printStackTrace();
                            deleteRealm.cancelTransaction();
                        }

                        JSONObject responseObject = changeSelectedOptions(response);// Json Parsing for changing user selected options
                        if (responseObject != null) {
//                        JSONObject responseObject = new JSONObject((String) response);
                            JSONArray selectedArray = responseObject.getJSONArray("SelectedItems");
                            for (int i = 0; i < selectedArray.length(); i++) {
                                JSONObject selectedItemObject = selectedArray.getJSONObject(i);
                                Realm realm = Realm.getDefaultInstance();
                                CartModel cartModel = realm.where(CartModel.class).findFirst();
                                realm.beginTransaction();
                                if (cartModel == null) {
                                    cartModel = new CartModel();
                                    cartModel.setCartId(1);
                                } else {
                                    cartModel.setCartId(cartModel.getCartId());
                                }
                                CartItemModel cartItemModel = null;

                                cartItemModel = new CartItemModel();
                                cartItemModel.setCategoryId(selectedItemObject.getInt("CategoryId"));
                                cartItemModel.setItemDesc(selectedItemObject.getString("Description"));
                                cartItemModel.setItemId(selectedItemObject.getInt("ItemId"));
                                cartItemModel.setItemTitle(selectedItemObject.getString("Title"));
                                cartItemModel.setItemMinQuantity(selectedItemObject.getInt("MinQuantity"));
                                cartItemModel.setItemOrderQuantity(selectedItemObject.getInt("quatity"));
                                cartItemModel.setItemPrice(selectedItemObject.getString("Price"));
                                itemQty = selectedItemObject.getInt("quatity");
                                minQty = selectedItemObject.getInt("MinQuantity");
                                itemPrice = Float.parseFloat(selectedItemObject.getString("Price"));
                                itemModifiersList = new RealmList<>();
                                itemModifiersList.addAll(getModifierList(selectedItemObject));
                                getTotalModifiersAmount();
                                totalItemCost = (itemQty) * itemPrice + modifierTotalAmount;
                                cartItemModel.setTotalItemPrice(totalItemCost);
                                cartItemModel.setUserSelectedModifierOptions(setUserOptionsModifiers());

                                RealmList<CartItemModel> cartItemList;
                                if (cartModel.getCartItemList() == null) {
                                    cartItemList = new RealmList<>();
                                    cartItemModel.setSerialId(1);
                                    for (int p = 0; p < itemModifiersList.size(); p++) {
                                        itemModifiersList.get(p).setModifierSerialId(100 + p);
                                        for (int k = 0; k < itemModifiersList.get(p).getOptionsModifiersList().size(); k++) {
                                            //(id+(index*10000))
                                            itemModifiersList.get(p).getOptionsModifiersList().get(k).setModifierSerialId(100 + p);
                                            itemModifiersList.get(p).getOptionsModifiersList().get(k).setOptionSerialId(itemModifiersList.get(p).getOptionsModifiersList().get(k).getOptionId());
                                        }
                                    }
                                    cartItemModel.setItemModifiersList(itemModifiersList);
                                    cartItemList.add(cartItemModel);
                                    cartModel.setCartItemList(cartItemList);
                                } else {
                                    cartItemList = cartModel.getCartItemList();
                                    int serialId = 1;
                                    if (cartItemList.size() > 0) {
                                        serialId = cartItemList.get(cartItemList.size() - 1).getSerialId() + 1;
                                    }
                                    cartItemModel.setSerialId(serialId);
                                    for (int p = 0; p < itemModifiersList.size(); p++) {
                                        itemModifiersList.get(p).setModifierSerialId((serialId * 1000) + p);
                                        for (int k = 0; k < itemModifiersList.get(p).getOptionsModifiersList().size(); k++) {
                                            itemModifiersList.get(p).getOptionsModifiersList().get(k).setModifierSerialId((serialId * 1000) + p);
                                            itemModifiersList.get(p).getOptionsModifiersList().get(k).setOptionSerialId((serialId * itemModifiersList.get(p).getOptionsModifiersList().get(k).getOptionId()));
                                        }
                                    }

                                    cartItemModel.setItemModifiersList(itemModifiersList);
                                    cartItemList.add(cartItemModel);
                                }
                                realm.copyToRealmOrUpdate(cartModel);
                                realm.commitTransaction();
                            }
                            getSubTotalValue();

                            boolean isLogin = sharedPreferences.contains("userCredentials");
                            if (isLogin) {
                                Intent deliveryAddressIntent = new Intent(OrderHistoryDetailsActivity.this, DeliveryAddressActivity.class);
                                deliveryAddressIntent.putExtra("isReOrder", 1);
                                deliveryAddressIntent.putExtra("isReOrderCart", 1);
                                startActivity(deliveryAddressIntent);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                Utils.cancelLoadingScreen();
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
                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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
                                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                                    String todayDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
                                    if (timeDiv[i].contains("-")) {
                                        if (responseObject.getString("date").equals(todayDate)) {
                                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
                                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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
                                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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
                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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

    private RealmList<ItemModifiersModel> getModifierList(JSONObject responseData) {
        RealmList<ItemModifiersModel> itemModifiersModelArrayList = new RealmList<>();
        try {
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return itemModifiersModelArrayList;
    }

    private void getTotalModifiersAmount() {

        for (int i = 0; i < itemModifiersList.size(); i++) {
            RealmList<OptionsModifiersModel> optionsModifiersModelArrayList = itemModifiersList.get(i).getOptionsModifiersList();
            for (int j = 0; j < optionsModifiersModelArrayList.size(); j++) {
                if (optionsModifiersModelArrayList.get(j).isOptionsDefault()) {
                    if (!(optionsModifiersModelArrayList.get(j).getPrice().equals("null"))) {
                        modifierTotalAmount += (Float.parseFloat(optionsModifiersModelArrayList.get(j).getPrice()));
                    }
                }
            }
        }
    }

    private String setUserOptionsModifiers() {

        StringBuilder stringBuilder = new StringBuilder();
        int modifierCount = 0, optionsCount = 0;
        for (int i = 0; i < itemModifiersList.size(); i++) {
            RealmList<OptionsModifiersModel> optionsModifiersModelRealmList = itemModifiersList.get(i).getOptionsModifiersList();
            optionsCount = 0;
            if (itemModifiersList.get(i).getIsModifierSelected()) {
                modifierCount++;
                if (modifierCount == 1) {
                    stringBuilder.append("(");
                }
                stringBuilder.append((itemModifiersList.get(i).getModifierName())).append(":").append(" ");
                for (int j = 0; j < optionsModifiersModelRealmList.size(); j++) {
                    if (optionsModifiersModelRealmList.get(j).isOptionsDefault()) {
                        optionsCount++;
                        if (optionsCount > 1) {
                            stringBuilder.append(",").append(" ");
                        }
                        stringBuilder.append(optionsModifiersModelRealmList.get(j).getTitle());
                    }

                }
                stringBuilder.append("\n");
            }

        }
        if (modifierCount >= 1) {
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.append(")");
        }
        if (stringBuilder.length() > 0) {
//            makeSelectionTxt.setText(stringBuilder.toString());
            userSelectedModifierOptions = stringBuilder.toString();
            return userSelectedModifierOptions;
        } else {
            return "";
        }
    }

    private void getSubTotalValue() {
        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        if (cartModel != null) {
            float subtotalValue = 0.0f;
            realm.beginTransaction();
            for (int i = 0; i < cartModel.getCartItemList().size(); i++) {
                subtotalValue += cartModel.getCartItemList().get(i).getTotalItemPrice();
            }
            cartModel.setSubtotal(Utils.roundUpFloatValue(subtotalValue, 2));
            realm.copyToRealmOrUpdate(cartModel);
            realm.commitTransaction();
            realm.close();
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

    private JSONObject changeSelectedOptions(Object response) {
        JSONObject responseObject = null;
        try {
            responseObject = new JSONObject((String) response);
            JSONArray selectedArray = responseObject.getJSONArray("SelectedItems");
            for (int i = 0; i < selectedArray.length(); i++) {
                JSONObject selectedItemObject = selectedArray.getJSONObject(i);
                JSONObject selectedOptionsObject = selectedItemObject.getJSONObject("SelectedOptions");
                JSONArray clientFieldsArray = selectedItemObject.getJSONArray("clientFields");
                for (int k = 0; k < clientFieldsArray.length(); k++) {
                    String selectedOptions = selectedOptionsObject.getString(clientFieldsArray.getJSONObject(k).getString("ID"));
                    if (selectedOptions.contains(",")) {
                        String selected[] = selectedOptions.split(",");
//                    "2972": "11228",
//                    "2976": "",
                        JSONArray optionsArray = clientFieldsArray.getJSONObject(k).getJSONArray("Options");
                        for (int p = 0; p < optionsArray.length(); p++) {
                            responseObject.getJSONArray("SelectedItems").getJSONObject(i).getJSONArray("clientFields").getJSONObject(k).getJSONArray("Options").getJSONObject(p).put("Default", false);
                            for (int q = 0; q < selected.length; q++) {
                                if (selected[q].equals(optionsArray.getJSONObject(p).getString("OptionId"))) {
                                    responseObject.getJSONArray("SelectedItems").getJSONObject(i).getJSONArray("clientFields").getJSONObject(k).getJSONArray("Options").getJSONObject(p).put("Default", true);
                                }
                            }
                        }

                    } else {
                        //                    "2972": "11228",
//                    "2976": "",
                        String selected = selectedOptions;

                        if (!(selected.equals(""))) {
                            JSONArray optionsArray = clientFieldsArray.getJSONObject(k).getJSONArray("Options");
                            for (int p = 0; p < optionsArray.length(); p++) {
                                responseObject.getJSONArray("SelectedItems").getJSONObject(i).getJSONArray("clientFields").getJSONObject(k).getJSONArray("Options").getJSONObject(p).put("Default", false);
                                if (selected.equals(optionsArray.getJSONObject(p).getString("OptionId"))) {
                                    responseObject.getJSONArray("SelectedItems").getJSONObject(i).getJSONArray("clientFields").getJSONObject(k).getJSONArray("Options").getJSONObject(p).put("Default", true);
                                }
                            }

                        }


                    }
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseObject;
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
                                               calendar.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                                               int day = calendar.get(Calendar.DAY_OF_WEEK);
                                               if (scheduleOrder(day - 1)) {
                                                   alertDialog.dismiss();
                                                   Constants.selectedDate = selectedDate;
                                                   Constants.selectedTime = "ASAP";

                                                   if (Utils.isNetworkAvailable(OrderHistoryDetailsActivity.this)) {
                                                       getOrderDetailsForReorderRequest();
                                                   } else {
                                                       Utils.showPositiveDialog(OrderHistoryDetailsActivity.this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionGetOrderDetailsForReorderFailed);
                                                   }


                                               } else {
                                                   Utils.toastDisplay(OrderHistoryDetailsActivity.this, "Presently restaurant is closed.Please schedule the order later.");
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
                                                        Utils.toastDisplay(OrderHistoryDetailsActivity.this, "Please select other timings.");
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

                                                        if (Utils.isNetworkAvailable(OrderHistoryDetailsActivity.this)) {
                                                            getOrderDetailsForReorderRequest();
                                                        } else {
                                                            Utils.showPositiveDialog(OrderHistoryDetailsActivity.this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionGetOrderDetailsForReorderFailed);
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
        orderDateTxt.setText(

                getString(R.string.today_txt)

        );
//        scheduleOrder(dayOfWeek);
        alertDialog = dialogBuilder.create();
        alertDialog.show();

        if (selectedDate != null && (selectedTime != null) && (!selectedTime.equals("ASAP"))) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Integer.parseInt(selectedDate.split("-")[0]), (Integer.parseInt(selectedDate.split("-")[1]) - 1), Integer.parseInt(selectedDate.split("-")[2]));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d", Locale.getDefault());
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
            String userSelectedDate = simpleDateFormat.format(new Date(calendar.getTimeInMillis()));

            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));

            if (selectedDate.equals(simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis()))) {
                orderDateTxt.setText(getString(R.string.today_txt));
            } else {
                orderDateTxt.setText(String.format("%s", userSelectedDate));
            }
            fetchRestTimeSlotsRequest(selectedDate);
        } else {
            orderDateTxt.setText(getString(R.string.today_txt));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
            fetchRestTimeSlotsRequest(simpleDateFormat.format(Calendar.getInstance().getTimeInMillis()));
        }


    }

    private void datePick() {

        if (selectedDate == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
            selectedDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
        }
//        Calendar cal1 = Calendar.getInstance();
//        cal1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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
//                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                Log.d("selectedDate", "" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + "-" + DateUtils.formatDateRange(OrderHistoryDetailsActivity.this, calendar.getTimeInMillis(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY));

                String weekDay = DateUtils.formatDateRange(OrderHistoryDetailsActivity.this, calendar.getTimeInMillis(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY);

                String date = year + "-" + (((monthOfYear + 1) < 10) ? ("0" + (monthOfYear + 1)) : (monthOfYear + 1)) + "-" + ((dayOfMonth < 10) ? ("0" + dayOfMonth) : dayOfMonth);
                selectedDate = date;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM d", Locale.getDefault());
//                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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
                simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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
        format.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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
//        simpleDateFormat1.forma
//        simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
//        calendar.set(Calendar.YEAR, Integer.parseInt(simpleDateFormat1.format(new Date()).split("-")[0]));
//        calendar.set(Calendar.MONTH, (Integer.parseInt(simpleDateFormat1.format(new Date()).split("-")[1]) - 1));
//        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(simpleDateFormat1.format(new Date()).split("-")[2]));

//        calendar.set(Calendar.YEAR, 2016);
//        calendar.set(Calendar.MONTH, 11);
//        calendar.set(Calendar.DAY_OF_MONTH, 7);
//        Log.d("DateObject", "" + calendar.getTime());
//        dpd.getDatePicker().setMinDate(calendar.getTimeInMillis());
//        dpd.getDatePicker().setMinDate((System.currentTimeMillis() - 10000));
//        dpd.getDatePicker().setMaxDate(cal1.getTimeInMillis());
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

    private void pickUpClick() {
        SharedPreferences.Editor pickUpEditor = sharedPreferences.edit();
        pickUpEditor.putBoolean("isDeliveryOrder", false);
        pickUpEditor.apply();
    }

    private void deliveryClick() {
        SharedPreferences.Editor deliveryEditor = sharedPreferences.edit();
        deliveryEditor.putBoolean("isDeliveryOrder", true);
        deliveryEditor.apply();
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
        simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
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

//    private void selectOrderTypeDialog() {
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//        dialogBuilder.setCancelable(false);
//        LayoutInflater layoutInflaterView = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//        View view = layoutInflaterView.inflate(R.layout.select_order_type_dialog, null);
//        dialogBuilder.setView(view);
//        AlertDialog orderTypeDialog = dialogBuilder.create();
//        orderTypeDialog.show();
//    }

    @Override
    public void userAction(int actionType) {

    }
}
