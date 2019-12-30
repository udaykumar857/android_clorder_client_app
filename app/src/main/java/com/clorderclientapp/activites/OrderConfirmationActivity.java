package com.clorderclientapp.activites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartItemModel;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.RealmModels.OptionsModifiersModel;
import com.clorderclientapp.adapters.OrderConfirmationListAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.ItemModifiersModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;


public class OrderConfirmationActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, ResponseHandler, UserActionInterface {
    private ListView finalOrderList;
    private TextView orderNumTxt, orderPlacedDateTxt, orderPlacedTimeTxt, orderTimeTxt, orderTypeTxt, customerNameTxt, customerPhoneNumTxt,
            paymentType, subtotal, tax, discount, delivery, tip, total, specialInstructionsTxt;
    private ImageView openMapImg, doneImg, orderConfirmBack;
    private SharedPreferences sharedPreferences;
    private OrderConfirmationListAdapter orderConfirmationListAdapter;
    private RealmList<CartItemModel> cartItemList;
    private MapView mapView;
    private GoogleMap mGoogleMap;
    private LatLng orderLatLng;
    private String placedTime = "";
    private int isFreeItemCoupon = 0;
    private HttpRequest httpRequest;
    private TextView orderConfirmTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_confirmation_layout);
        sharedPreferences = getSharedPreferences("MySharedPreferences", MODE_PRIVATE);
        cartItemList = new RealmList<>();
        httpRequest = new HttpRequest();
        if (getIntent() != null) {
            placedTime = getIntent().getStringExtra("PlacedTime");
            isFreeItemCoupon = getIntent().getIntExtra("isFreeItemCoupon", 0);

        }
        initViews();
        try {
            orderConfirmTxt.setText(getString(R.string.order_confirm_msg)+
                    " "+Constants.clientSettingsObject.getJSONObject("ClientSettings").getString("ClientName")
                    + " "+Constants.clientSettingsObject.getJSONObject("ClientSettings").getString("PhoneNumber")
            +". "+getString(R.string.order_confirm_payment_msg));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listeners();


        getOrderDetails();

        if (isFreeItemCoupon == 1) {

            try {
                if (Constants.discountObject.getInt("ItemIdofFreeItem") != 0) {
                    getModifiersForItemRequest();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        try {
            orderLatLng = new LatLng(Constants.clientSettingsObject.getJSONObject("ClientSettings").getDouble("clientLat"), Constants.clientSettingsObject.getJSONObject("ClientSettings").getDouble("clientLon"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Constants.selectedTime = null;
        Constants.selectedDate = null;//Reset the date and time after Order Submission.

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void getModifiersForItemRequest() {

        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("clientId", Utils.getClientId(this));
                requestObject.put("ItemId", Constants.discountObject.getString("ItemIdofFreeItem"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpRequest.getModifiersForItem(this, requestObject, Constants.GetModifiersForItem);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionItemModifierFailed);
        }

    }


    private void initViews() {
        finalOrderList = (ListView) findViewById(R.id.final_order_list);
        orderNumTxt = (TextView) findViewById(R.id.order_num_txt);
        orderConfirmBack = (ImageView) findViewById(R.id.order_confirm_back);
        orderPlacedDateTxt = (TextView) findViewById(R.id.order_placed_date_txt);
        orderPlacedTimeTxt = (TextView) findViewById(R.id.order_placed_time_txt);
        orderConfirmTxt = (TextView) findViewById(R.id.order_confirm_txt);
        orderTimeTxt = (TextView) findViewById(R.id.order_time);
        orderTypeTxt = (TextView) findViewById(R.id.order_type_txt);
        customerNameTxt = (TextView) findViewById(R.id.customer_name_txt);
        customerPhoneNumTxt = (TextView) findViewById(R.id.customer_phone_num_txt);
        specialInstructionsTxt = (TextView) findViewById(R.id.special_instructions_txt);
        paymentType = (TextView) findViewById(R.id.payment_type);
        subtotal = (TextView) findViewById(R.id.subtotal);
        tax = (TextView) findViewById(R.id.tax);
        discount = (TextView) findViewById(R.id.discount);
        delivery = (TextView) findViewById(R.id.delivery);
        tip = (TextView) findViewById(R.id.tip);
        total = (TextView) findViewById(R.id.total);
        openMapImg = (ImageView) findViewById(R.id.open_map_img);
        doneImg = (ImageView) findViewById(R.id.done_img);
        mapView = (MapView) findViewById(R.id.mapView);
    }

    private void listeners() {
        openMapImg.setOnClickListener(this);
        doneImg.setOnClickListener(this);
        orderConfirmBack.setOnClickListener(this);
        orderConfirmationListAdapter = new OrderConfirmationListAdapter(this, cartItemList);
        finalOrderList.setAdapter(orderConfirmationListAdapter);
    }

    @Override
    public void onBackPressed() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(CartModel.class);
        realm.commitTransaction();
        startActivity(new Intent(this, JohnniesPizzaScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_map_img:
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + orderLatLng.latitude + "," + orderLatLng.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
                break;
            case R.id.done_img:
                onBackPressed();
                break;
            case R.id.order_confirm_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void getOrderDetails() {
        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        orderNumTxt.setText(String.format("%s", cartModel.getOrderId()));

//        orderPlacedDateTxt.setText(String.format("%s", cartModel.getOrderPlacedDate()));
//        orderPlacedTimeTxt.setText(String.format("%s", " " + cartModel.getOrderPlacedTime()));

        orderPlacedDateTxt.setText(placedTime.split(" ")[0]);
        orderPlacedTimeTxt.setText(" " + placedTime.split(" ")[1] + " " + placedTime.split(" ")[2].replace("am", "AM").replace("pm","PM"));

        if (cartModel.isFutureOrder) {
            String orderDate;
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsed = null;
            try {
                orderDate = cartModel.getOrderPlacedDate() + " " + cartModel.getOrderPlacedTime();
                parsed = sourceFormat.parse(orderDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("OrderTime8", "" + parsed);
            TimeZone tz = TimeZone.getTimeZone(Constants.timeZone);
            SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
            destFormat.setTimeZone(tz);

            orderDate = destFormat.format(parsed);


            orderTimeTxt.setText(orderDate.replace("am", "AM").replace("pm","PM") + "(" + getString(R.string.future_order_txt) + ")");
        } else {
            orderTimeTxt.setText(getString(R.string.asap_order_txt));
        }

        boolean isAddressDetails = sharedPreferences.contains("userDetails");
        if (isAddressDetails) {
            JSONObject userDetails = null;
            try {
                userDetails = new JSONObject((String) sharedPreferences.getString("userDetails", ""));
                customerNameTxt.setText(userDetails.getString("name"));
                customerPhoneNumTxt.setText(userDetails.getString("phoneNumber"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        boolean isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", false);
        if (isDeliveryOrder) {
            orderTypeTxt.setText(getString(R.string.delivery_confirmation_txt));
            delivery.setText(String.format("%s", "$" + Utils.roundFloatString(cartModel.getDeliveryCharge(), 2)));
        } else {
            orderTypeTxt.setText(getString(R.string.pickup_confirmation_txt));
        }
        if (cartModel.getSpecialNotes().equals("")) {
            specialInstructionsTxt.setText("---");
        } else {
            specialInstructionsTxt.setText(cartModel.getSpecialNotes());
        }
        paymentType.setText(cartModel.getPaymentType());
        subtotal.setText(String.format("%s", "$" + Utils.roundFloatString(cartModel.getSubtotal(), 2)));
        tax.setText(String.format("%s", "$" + Utils.roundFloatString(cartModel.getTax(), 2)));
        tip.setText(String.format("%s", "$" + Utils.roundFloatString(cartModel.getTipAmount(), 2)));
        total.setText(String.format("%s", "$" + Utils.roundFloatString(cartModel.getOrderTotal(), 2)));
        discount.setText(String.format("%s", "$" + Utils.roundFloatString(cartModel.getDiscountAmount(), 2)));

        cartItemList.clear();
        cartItemList.addAll(cartModel.getCartItemList());
        orderConfirmationListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.addMarker(new MarkerOptions().position(orderLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).anchor(0.5f, 1));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(orderLatLng, 15));
    }

    private String setUserOptionsModifiers(RealmList<ItemModifiersModel> itemModifiersList) {
        String userSelectedModifierOptions = "";
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

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.GetModifiersForItem:
                Utils.cancelLoadingScreen();
                try {
                    if (response != null) {
                        RealmList<ItemModifiersModel> itemModifiersList = new RealmList<>();
                        itemModifiersList.addAll((RealmList<ItemModifiersModel>) response);
                        String selectedOptions = setUserOptionsModifiers(itemModifiersList);
                        CartItemModel cartItemModel = new CartItemModel();
                        cartItemModel.setItemOrderQuantity(1);
                        cartItemModel.setItemTitle(Constants.itemId);
                        cartItemModel.setTotalItemPrice(Float.parseFloat(Constants.discountObject.getString("DiscountAmount")));
                        cartItemModel.setUserSelectedModifierOptions(selectedOptions);
                        cartItemList.add(cartItemModel);
                        orderConfirmationListAdapter.notifyDataSetChanged();

                    } else {
                        Utils.showPositiveDialog(this, "Alert", getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;
        }

    }

    @Override
    public void userAction(int actionType) {
        switch (actionType) {
            case Constants.ActionItemModifierFailed:
                getModifiersForItemRequest();
                break;
        }

    }
}
