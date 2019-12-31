package com.clorderclientapp.activites;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartItemModel;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.RealmModels.OptionsModifiersModel;
import com.clorderclientapp.adapters.ExistingCardsAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.HandleInterface;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.ExistingCardModel;
import com.clorderclientapp.modelClasses.ItemModifiersModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;

public class UserCardListActivity extends AppCompatActivity implements View.OnClickListener, HandleInterface, ResponseHandler, UserActionInterface {
    private Button addACardBtn;
    private TextView skipBtn, payPalTxt;
    private ImageView accountUpdateBack;
    private ListView cardListView;
    private ExistingCardsAdapter existingCardsAdapter;
    private SharedPreferences sharedPreferences;
    private HttpRequest httpRequest;
    private int isFromUserCreation = 0;
    public static ArrayList<ExistingCardModel> existingCardList;
    private int deletedCardPosition = 0;
    private static final int ADD_CARD = 3000;
    private LinearLayout addCardListLayout;
    private boolean isDeliveryOrder = false;
    private String billingZipCode = "";
    private String creditCardCSC = "";
    private String creditCardExpired = "";
    private String creditCardNumber = "";
    private String creditCardType = "";
    private int cardSelectedPosition = 0;
    private int paymentType = 1;
    private int isFreeItemCoupon = 0;
    private String pstCurrentTime;
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
    private static final int REQUEST_CODE_PAYMENT = 1;
    private String orderId = "0";
    private String payPalResponse = "";

    // note that these credentials will differ between live & sandbox environments.
    //Sandbox
//    private static final String CONFIG_CLIENT_ID = "AT6cGxBwRvDtcg5ZovY3buRT28DbZP7MBRaHEON8YZvUUd1FSz6ZHNQ1ejvc";
//    Ad832inWYflRVVajE2fEdOkud078w0xcXnYRto2eMY_O_aDW1OGZ8v30LIn4A_oshtdqtfBoYhMsqGl2  ---> Saurab Credentials
    //    AT6cGxBwRvDtcg5ZovY3buRT28DbZP7MBRaHEON8YZvUUd1FSz6ZHNQ1ejvc  ---> Clorder SandBox
    //    AVHn_BDAMoZIsKAAo7GjwIOM-7e1Gnc2XacJhYL4a5tvPeBuXbCqBC2AWGO3 ---->Clorder Live

    //Live
    private static final String CONFIG_CLIENT_ID = "AVHn_BDAMoZIsKAAo7GjwI0M-7e1Gnc2XacJhYL4a5tvPeBuXbCqBC2AWGO3";


    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID);
    private static final String TAG = "paymentExample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_card_list);
        initViews();
        listeners();
        httpRequest = new HttpRequest();
        if (getIntent() != null) {
            isFromUserCreation = getIntent().getIntExtra("isFromUserCreation", 0);
        }
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        if (isFromUserCreation == 1) {
            skipBtn.setText(getString(R.string.account_update_skip));
        } else {
            payPalTxt.setVisibility(View.VISIBLE);
            skipBtn.setText(getString(R.string.account_update_pay_cash));
        }
        if (!Constants.isGuestUserLogin || existingCardList == null) {
            existingCardList = new ArrayList<>();
        }

        existingCardsAdapter = new ExistingCardsAdapter(this, existingCardList);
        cardListView.setAdapter(existingCardsAdapter);

        if (!existingCardList.isEmpty()) {
            addCardListLayout.setVisibility(View.VISIBLE);
        }
        if (!Constants.isGuestUserLogin) {//Need to take data from shared preferences if not guest user.....
            getCardListData();
        }
    }

    private void initViews() {
        addACardBtn = (Button) findViewById(R.id.add_a_card_btn);
        skipBtn = (TextView) findViewById(R.id.skip_btn);
        accountUpdateBack = (ImageView) findViewById(R.id.account_update_back);
        cardListView = (ListView) findViewById(R.id.card_list);
        addCardListLayout = (LinearLayout) findViewById(R.id.add_card_list_layout);
        payPalTxt = (TextView) findViewById(R.id.pay_pal_txt);
    }

    private void listeners() {
        addACardBtn.setOnClickListener(this);
        skipBtn.setOnClickListener(this);
        payPalTxt.setOnClickListener(this);
        accountUpdateBack.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.add_a_card_btn:
                Intent intent = new Intent(this, PaymentAddCardActivity.class);
                intent.putExtra("isNewCard", true);
                startActivityForResult(intent, ADD_CARD);
                break;
            case R.id.skip_btn:
                if (isFromUserCreation == 1) {
                    startActivity(new Intent(this, JohnniesPizzaScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } else {
                    try {
                        int maxCashValue = Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("maxCashValue");
                        Realm realm = Realm.getDefaultInstance();
                        CartModel cartModel = realm.where(CartModel.class).findFirst();
                        if (cartModel.getSubtotal() <= maxCashValue) {
                            paymentType = 1;
                            cartSubmit();
                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.cash_amount_max_limit) + maxCashValue + ".", Constants.ActionCashLimitMsg);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.account_update_back:
                onBackPressed();
                break;
            case R.id.pay_pal_txt:
                paymentType = 4;
                cartSubmit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ADD_CARD:
                if (resultCode == RESULT_OK) {
                    try {
                        existingCardList.clear();
                        JSONArray paymentInformation = new JSONArray((String) data.getExtras().getString("paymentInformationArray"));
                        for (int i = 0; i < paymentInformation.length(); i++) {
                            ExistingCardModel existingCardModel = new ExistingCardModel();
                            existingCardModel.setBillingZipCode(paymentInformation.getJSONObject(i).getString("BillingZipCode"));
                            existingCardModel.setCardId(paymentInformation.getJSONObject(i).getString("CardId"));
                            existingCardModel.setCreditCardCSC(paymentInformation.getJSONObject(i).getString("CreditCardCSC"));
                            existingCardModel.setCreditCardExpired(paymentInformation.getJSONObject(i).getString("CreditCardExpired"));
                            existingCardModel.setCreditCardName(paymentInformation.getJSONObject(i).getString("CreditCardName"));
                            existingCardModel.setCreditCardNumber(paymentInformation.getJSONObject(i).getString("CreditCardNumber"));
                            existingCardModel.setCreditCardType(paymentInformation.getJSONObject(i).getString("CreditCardType"));
                            existingCardModel.setDeleted(paymentInformation.getJSONObject(i).getBoolean("IsDeleted"));
                            existingCardList.add(existingCardModel);
                        }

                        if (existingCardList.size() > 0) {
                            addCardListLayout.setVisibility(View.VISIBLE);
                            existingCardsAdapter.notifyDataSetChanged();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case REQUEST_CODE_PAYMENT:
                if (resultCode == Activity.RESULT_OK) {
                    PaymentConfirmation confirm =
                            data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                    if (confirm != null) {
                        try {
                            Log.i(TAG, confirm.toJSONObject().toString(4));
                            Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));

                            payPalResponse = confirm.toJSONObject().getJSONObject("response").toString();
                            Log.d("Paypal Response", "" + payPalResponse);

                            confirmPayPalOrder();

                            /**
                             *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                             * or consent completion.
                             * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                             * for more details.
                             *
                             * For sample mobile backend interactions, see
                             * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                             */
//                        displayResultText("PaymentConfirmation info received from PayPal");


                        } catch (JSONException e) {
                            Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i(TAG, "The user canceled.");
                } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                    Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
                }
                break;
        }

    }

    private void getCardListData() {
//        "PaymentInformation": [
//        {
//            "BillingZipCode": "84095",
//                "CardId": 52722,
//                "CreditCardCSC": "2222",
//                "CreditCardExpired": "2017-01-02T00:00:00",
//                "CreditCardName": "Demo222",
//                "CreditCardNumber": "4444333322221111",
//                "CreditCardType": 8,
//                "IsDeleted": false
//        },
//        {
//            "BillingZipCode": "84095",
//                "CardId": 52723,
//                "CreditCardCSC": "2223",
//                "CreditCardExpired": "2017-01-02T00:00:00",
//                "CreditCardName": "Demo333",
//                "CreditCardNumber": "4444333322222222",
//                "CreditCardType": 8,
//                "IsDeleted": false
//        }
//        ]
        boolean isLogin = sharedPreferences.contains("userCredentials");
        if (isLogin) {
            try {
                JSONObject userCredentials = new JSONObject(sharedPreferences.getString("userCredentials", ""));
                if (!(userCredentials.isNull("PaymentInformation"))) {
                    JSONArray paymentInformationArray = userCredentials.getJSONArray("PaymentInformation");
                    for (int i = 0; i < paymentInformationArray.length(); i++) {
                        ExistingCardModel existingCardModel = new ExistingCardModel();
                        existingCardModel.setBillingZipCode(paymentInformationArray.getJSONObject(i).getString("BillingZipCode"));
                        existingCardModel.setCardId(paymentInformationArray.getJSONObject(i).getString("CardId"));
                        existingCardModel.setCreditCardCSC(paymentInformationArray.getJSONObject(i).getString("CreditCardCSC"));
                        existingCardModel.setCreditCardExpired(paymentInformationArray.getJSONObject(i).getString("CreditCardExpired"));
                        existingCardModel.setCreditCardName(paymentInformationArray.getJSONObject(i).getString("CreditCardName"));
                        existingCardModel.setCreditCardNumber(paymentInformationArray.getJSONObject(i).getString("CreditCardNumber"));
                        existingCardModel.setCreditCardType(paymentInformationArray.getJSONObject(i).getString("CreditCardType"));
                        existingCardModel.setDeleted(paymentInformationArray.getJSONObject(i).getBoolean("IsDeleted"));
                        if (!paymentInformationArray.getJSONObject(i).getBoolean("IsDeleted")) {
                            existingCardList.add(existingCardModel);
                        }

                    }
                    if (existingCardList.size() > 0) {
                        addCardListLayout.setVisibility(View.VISIBLE);
                        existingCardsAdapter.notifyDataSetChanged();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public void handleClick(View view, int position) {

        switch (view.getId()) {
            case R.id.editBtn:
                Intent intent = new Intent(this, PaymentAddCardActivity.class);
                intent.putExtra("cardObject", existingCardList.get(position));
                intent.putExtra("isNewCard", false);
                startActivityForResult(intent, ADD_CARD);
                break;
            case R.id.deleteBtn:
                if (!Constants.isGuestUserLogin) {
                    deletedCardPosition = position;
                    if (Utils.isNetworkAvailable(this)) {
                        updateClorderUser(position);
                    } else {
                        Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionUpdateClorderUserFailed);
                    }

                } else {
                    existingCardList.remove(position);
                    if (existingCardList.size() == 0) {
                        addCardListLayout.setVisibility(View.GONE);
                    }
                    existingCardsAdapter.notifyDataSetChanged();
                }
                break;

            case R.id.card_layout:
                if (isFromUserCreation != 1) {
                    paymentType = 2;
                    Utils.showDialog(this, getString(R.string.message_txt), getString(R.string.card_selection_msg) + existingCardList.get(position).getCreditCardNumber(), Constants.ActionCardSubmit);
                    cardSelectedPosition = position;
                }

                break;
        }

    }

    private void updateClorderUser(int position) {
//        {
//            "clientId": 5,
//                "UserId": 92973,                               ------------------------- Mandatory
//            "Email": "qqq@qqq.com",              ------------------------- Mandatory
//            "Password": "Qwerty1234",
//                "FirstName": "qqq",             ------------------------- Mandatory
//            "LastName": "qwerr",             ------------------------- Mandatory
//            "UserAddress": {
//            "Address1": "xcxcx",             ------------------------- Mandatory
//            "Address2": "yyyy",             ------------------------- Mandatory
//            "City": "South Jordan",             ------------------------- Mandatory
//            "PhoneNumber": "2096848765",              ------------------------- Mandatory
//            "ZipPostalCode": "84095"             ------------------------- Mandatory
//        },
//            "PaymentInformation": [
//            {
//                "BillingZipCode": "84095",
//                    "CardId": 52722, ------------------------------------------ New card this will be 0
//                "CreditCardCSC": "2222",
//                    "CreditCardExpired": "2017-01-02",
//                    "CreditCardName": "Demo222",
//                    "CreditCardNumber": "4444333322221111",
//                    "CreditCardType": 8,
//                    "IsDeleted": false  ---------------------------------  If an existing card needs to be removed then send this as true
//            }
//            ]
//        }

        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Constants.clientId);
            boolean isUserCredentialsData = sharedPreferences.contains("userCredentials");
            if (isUserCredentialsData) {
                JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                requestObject.put("UserId", userCredentials.getString("UserId"));
                requestObject.put("Email", userCredentials.getString("Email"));
                requestObject.put("Password", userCredentials.getString("Password"));
                requestObject.put("FirstName", userCredentials.getString("FirstName"));
                requestObject.put("LastName", userCredentials.getString("LastName"));
                requestObject.put("UserAddress", userCredentials.getJSONObject("UserAddress"));
            }
            JSONArray paymentInformationArray = new JSONArray();
            JSONObject creditCardObject = new JSONObject();
            creditCardObject.put("BillingZipCode", existingCardList.get(position).getBillingZipCode());
            creditCardObject.put("CardId", existingCardList.get(position).getCardId());
            creditCardObject.put("CreditCardCSC", existingCardList.get(position).getCreditCardCSC());
            creditCardObject.put("CreditCardExpired", existingCardList.get(position).getCreditCardExpired());
            creditCardObject.put("CreditCardName", existingCardList.get(position).getCreditCardName());
            creditCardObject.put("CreditCardNumber", existingCardList.get(position).getCreditCardNumber());
            creditCardObject.put("CreditCardType", existingCardList.get(position).getCreditCardType());
            creditCardObject.put("IsDeleted", true);
            paymentInformationArray.put(creditCardObject);
            requestObject.put("PaymentInformation", paymentInformationArray);

            Log.d("RequestObject", requestObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.updateClorderUser(this, requestObject, Constants.UpdateClorderUser);
    }


    private void cartSubmit() {
        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
//        {
//            "IpAddress": null, ----------------------------------- IP address of the device
//            "OrderId": 0,		-----------------------------------  0 for new order, Orderid for updated order
//            "ClientId": 0, -----------------------------------  ClientId of the restaurent
//            "MemberId": 0,	----------------------------------- UserId of logged in user, 0 for guest
//            "DeliveryPreference": 0,	-----------------------------------  1 - Pickup 2- Delivery
//            "OverallNotes": null, ----------------------------------- special notes of the order
//            "DeliveryDist": 0,----------------------------------- distance of the address from restaurent
//            "UserDataInfo": {
//            "Email": null,-----------------------------------  Email address of user
//            "FirstName": null, ----------------------------------- First name
//            "LastName": null, ----------------------------------- last name
//            "UserAddress": {
//                "Address1": null,	----------------------------------- address1
//                "Address2": null,----------------------------------- address 2
//                "City": null, -----------------------------------  city
//                "PhoneNumber": null, ----------------------------------- phone number
//                "ZipPostalCode": null ----------------------------------- zipcode
//            },
//        },
//            "PaymentInfo": { -----------------------------------  required if credit card
//            "BillingZipCode": null, ----------------------------------- billing zip code required if credit card
//            "CreditCardCSC": null,
//                    "CreditCardExpired": "0001-01-01T00:00:00",
//                    "CreditCardNumber": null,
//                    "CreditCardType": 0,
//        },
//            "CartInfo": {
//            "AddfreeItemCouponApplied": false,
//                    "InstanceIdFreeItemAdded": "00000000-0000-0000-0000-000000000000",
//                    "FreeAddedItemId": 0,
//                    "CartItems": [
//            {
//                "CategoryId": null,
//                    "ItemId": 0,
//                    "Notes": null,
//                    "Price": 0,
//                    "Title": null,
//        "CartItemInstanceId" : "ed8aa535-d507-40ac-9403-baa1f854adc0",-----------------Not required to send
//                "Quantity" : 2
//                    "SelectedOptions": {
//                         "6": "216",
//                        "32": "523"
//            }
//            }
//            ],
//            "TaxCost": 0,
//                    "TipAmount": 0,
//                    "ShippingCost": 0,
//                    "DiscountCode": null,
//                    "DiscountAmount": 0,
//                    "SubTotal": 0,
//                    "Total": 0,
//                    "TipType": 0
//        }
//        }

            JSONObject cartObject = new JSONObject();
            try {
                Realm realm = Realm.getDefaultInstance();
                CartModel cartModel = realm.where(CartModel.class).findFirst();
                //Cart Object
                cartObject.put("IpAddress", "");
                cartObject.put("OrderId", 0);
                cartObject.put("ClientId", Constants.clientId);
                cartObject.put("PaymentType", paymentType);

                boolean isUserCredentials = sharedPreferences.contains("userCredentials");
                if (isUserCredentials) {
                    JSONObject userCredentials = new JSONObject(sharedPreferences.getString("userCredentials", ""));
                    cartObject.put("MemberId", userCredentials.getString("UserId"));
                } else {
                    cartObject.put("MemberId", 0);
                }


                String orderDate;

                if (Constants.selectedTime.equals("ASAP")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    orderDate = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());

                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
//                    simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                    Constants.selectedTime = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis());
                } else {
//                    "OrderDate":"2016-12-08 9:45 AM"

                    orderDate = Constants.selectedDate + " " + Constants.selectedTime;

                    Log.d("OrderTimeOriginal", "" + orderDate);
                    SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa",Locale.getDefault());

//                    sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));

                    Date parsed = null;
                    try {
                        parsed = sourceFormat.parse(orderDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.d("OrderTime8", "" + parsed);
                    TimeZone tz = TimeZone.getTimeZone("GMT");
                    SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
                    destFormat.setTimeZone(tz);

                    orderDate = destFormat.format(parsed);

                    Log.d("OrderTime", orderDate);

                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aaa", Locale.getDefault());
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                String nowTime = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());

                SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                String nowDate = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis());

                if (nowDate.equals(Constants.selectedDate)) {
                    if ((timeInMinutes(Constants.selectedTime) - timeInMinutes(nowTime)) > 45) {
                        cartObject.put("isFutureOrder", true);
                    } else {
                        cartObject.put("isFutureOrder", false);
                    }
                } else {
                    cartObject.put("isFutureOrder", true);
                }

                cartObject.put("OrderDate", orderDate);

                isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", false);
                if (isDeliveryOrder) {
                    cartObject.put("DeliveryPreference", "2");//1-pickup, 2-delivery
                } else {
                    cartObject.put("DeliveryPreference", "1");
                }
                cartObject.put("OverallNotes", cartModel.getSpecialNotes());
                if (isDeliveryOrder) {
                    cartObject.put("DeliveryDist", "" + cartModel.getDeliveryDist());
                } else {
                    cartObject.put("DeliveryDist", 0);
                }

                //userDataInfo.....
                JSONObject userDataInfo = new JSONObject();
                boolean isAddressDetails = sharedPreferences.contains("userDetails");
                if (isAddressDetails) {
                    JSONObject userDetails = new JSONObject(sharedPreferences.getString("userDetails", ""));
                    userDataInfo.put("FirstName", userDetails.getString("name"));
                    userDataInfo.put("LastName", "");
                    userDataInfo.put("Email", userDetails.getString("email"));
                    //userAddress.....
                    JSONObject userAddress = new JSONObject();
                    userAddress.put("Address1", userDetails.getString("address"));
                    userAddress.put("Address2", userDetails.getString("buildings"));
                    userAddress.put("City", userDetails.getString("city"));
                    userAddress.put("PhoneNumber", userDetails.getString("phoneNumber"));
                    userAddress.put("ZipPostalCode", userDetails.getString("zipCode"));
                    userDataInfo.put("UserAddress", userAddress);
                }
                cartObject.put("UserDataInfo", userDataInfo);

                //PaymentInfo.....
                JSONObject paymentInfo = new JSONObject();
                paymentInfo.put("BillingZipCode", billingZipCode);
                paymentInfo.put("CreditCardCSC", creditCardCSC);
                paymentInfo.put("CreditCardExpired", creditCardExpired);
                paymentInfo.put("CreditCardNumber", creditCardNumber);
                paymentInfo.put("CreditCardType", creditCardType);
                cartObject.put("PaymentInfo", paymentInfo);

                //CartInfo.....
                JSONObject cartInfo = new JSONObject();
                cartInfo.put("AddfreeItemCouponApplied", false);
                cartInfo.put("InstanceIdFreeItemAdded", "00000000-0000-0000-0000-000000000000");
                cartInfo.put("FreeAddedItemId", 0);

                //CartItems.....
                RealmList<CartItemModel> cartItemList = cartModel.getCartItemList();
                JSONArray cartItemsArray = new JSONArray();
                for (int i = 0; i < cartItemList.size(); i++) {
                    JSONObject cartItemObject = new JSONObject();
                    cartItemObject.put("CategoryId", cartItemList.get(i).getCategoryId());
                    cartItemObject.put("ItemId", cartItemList.get(i).getItemId());
                    cartItemObject.put("Notes", cartItemList.get(i).getSpecialNotes());
                    cartItemObject.put("Price", cartItemList.get(i).getItemPrice());
                    cartItemObject.put("Title", cartItemList.get(i).getItemTitle());
                    cartItemObject.put("Quantity", cartItemList.get(i).getItemOrderQuantity());
                    //SelectedOptions......
                    RealmList<ItemModifiersModel> modifiersList = cartItemList.get(i).getItemModifiersList();
                    JSONObject selectedOptions = new JSONObject();
                    for (int j = 0; j < modifiersList.size(); j++) {
                        RealmList<OptionsModifiersModel> optionsModifiersList = modifiersList.get(j).getOptionsModifiersList();
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int k = 0; k < optionsModifiersList.size(); k++) {
                            if (optionsModifiersList.get(k).isOptionsDefault()) {
                                stringBuilder.append(optionsModifiersList.get(k).getOptionId());
                                stringBuilder.append(",");
                            }
                        }
                        String modifierSelectedData = stringBuilder.toString();
                        if (stringBuilder.length() > 0) {
                            modifierSelectedData = stringBuilder.substring(0, stringBuilder.length() - 1);
                        }
                        selectedOptions.put("" + modifiersList.get(j).getId(), modifierSelectedData);
                    }
                    cartItemObject.put("SelectedOptions", selectedOptions);

                    cartItemsArray.put(cartItemObject);
                }
                cartInfo.put("CartItems", cartItemsArray);

                cartInfo.put("TaxCost", "" + cartModel.getTax());
                cartInfo.put("TipAmount", "" + Utils.roundUpFloatValue(cartModel.getTipAmount(), 2));
                if (isDeliveryOrder) {
                    cartInfo.put("ShippingCost", cartModel.getDeliveryCharge());
                } else {
                    cartInfo.put("ShippingCost", 0);
                }

                cartInfo.put("DiscountCode", cartModel.getDiscountCode());
                cartInfo.put("DiscountAmount", Utils.roundUpFloatValue(cartModel.getDiscountAmount(), 2));
                cartInfo.put("SubTotal", "" + Utils.roundUpFloatValue(cartModel.getSubtotal(), 2));
                cartInfo.put("Total", "" + Utils.roundUpFloatValue(cartModel.getOrderTotal(), 2));
                cartInfo.put("TipType", 0);
                cartObject.put("CartInfo", cartInfo);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("cartObject", cartObject.toString());
            httpRequest.placeOrder(this, cartObject, Constants.PlaceOrder);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionPlaceOrderFailed);
        }
    }


    private void confirmPayPalOrder() {

        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("ClientId", Constants.clientId);
            requestObject.put("OrderId", orderId);
            requestObject.put("PaypalResponse", payPalResponse);
            Log.d("Confirm Paypal Req", "" + requestObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        httpRequest.confirmPayPalOrder(this, requestObject, Constants.ConfirmPayPalOrder);
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        Utils.cancelLoadingScreen();
        switch (requestType) {
            case Constants.UpdateClorderUser:
                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        JSONObject paymentInformation = responseObject.getJSONArray("PaymentInformation").getJSONObject(0);
                        if (!(paymentInformation.getString("CardId").equals("0"))) {
                            updateLoginData(responseObject);
                            existingCardList.remove(deletedCardPosition);
                            if (existingCardList.size() == 0) {
                                addCardListLayout.setVisibility(View.GONE);
                            }
                            existingCardsAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constants.PlaceOrder:
                if (response != null) {
                    Log.d("Place Order response", "" + response.toString());
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if (responseObject.getBoolean("isSuccess")) {
                            Realm realm = Realm.getDefaultInstance();
                            CartModel cartModel = realm.where(CartModel.class).findFirst();
                            realm.beginTransaction();
                            cartModel.setOrderId(responseObject.getInt("OrderId"));
                            orderId = responseObject.getString("OrderId");
                            switch (paymentType) {
                                case 1:
                                    cartModel.setPaymentType("Cash Payment");
                                    break;
                                case 2:
                                    cartModel.setPaymentType("Credit Card");
                                    break;
                                case 4:
                                    cartModel.setPaymentType("PayPal Prepaid");
                                    break;
                                default:
                                    cartModel.setPaymentType("Cash Payment");
                                    break;
                            }
//                            "OrderDate": "2016-09-27T07:59:52"
                            if (cartModel.getDiscountCode() != null) {
                                if (cartModel.getDiscountCode().equals(responseObject.getJSONObject("CartInfo").getString("DiscountCode"))) {
                                    isFreeItemCoupon = 1;
                                } else {
                                    isFreeItemCoupon = 0;
                                }
                            }
                            String orderTimeStamp[] = responseObject.getString("OrderDate").split("T");
                            cartModel.setOrderPlacedDate(orderTimeStamp[0]);
                            cartModel.setOrderPlacedTime(orderTimeStamp[1]);
                            cartModel.setFutureOrder(responseObject.getBoolean("isFutureOrder"));
                            realm.copyToRealmOrUpdate(cartModel);
                            realm.commitTransaction();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa", Locale.getDefault());
                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                            pstCurrentTime = simpleDateFormat.format(Calendar.getInstance().getTimeInMillis());
                            if (paymentType == 4) {
                                Intent intent1 = new Intent(this, PayPalService.class);
                                intent1.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                                startService(intent1);
                                getThingToBuy();
                            } else {
                                Utils.showPositiveDialog(this, "OrderSuccess", getString(R.string.order_successfully_place_msg) + responseObject.getString("OrderId"), Constants.ActionCartSubmitSuccess);
                            }

                        } else {
                            Utils.showPositiveDialog(this, "OrderFailed", responseObject.getString("statusMessage"), Constants.ActionCartSubmitFailed);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.showPositiveDialog(this, "Alert", getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
                }
                break;

            case Constants.ConfirmPayPalOrder:
                if (response != null) {
                    Log.d("ConfirmPayPalOrder", "" + response.toString());
                    try {
                        JSONObject responseObject = new JSONObject((String) response);
                        if (responseObject.getBoolean("isSuccess")) {
                            Utils.showPositiveDialog(this, "OrderSuccess", getString(R.string.order_successfully_place_msg) + orderId, Constants.ActionCartSubmitSuccess);
                        } else {
                            Utils.showPositiveDialog(this, "OrderFailed", responseObject.getString("statusMessage"), Constants.ActionCartSubmitFailed);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
        }
    }


    private void updateLoginData(JSONObject responseObject) {
        boolean isUserCredentialsData = sharedPreferences.contains("userCredentials");
        if (isUserCredentialsData) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userCredentials", responseObject.toString());
            editor.apply();
        }
    }

//    public void onBuyPressed(View pressed) {
//        /*
//         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
//         * Change PAYMENT_INTENT_SALE to
//         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
//         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
//         *     later via calls from your server.
//         *
//         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
//         */
//        PayPalPayment thingToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
//
//        /*
//         * See getStuffToBuy(..) for examples of some available payment options.
//         */
//
//        Intent intent = new Intent(SampleActivity.this, PaymentActivity.class);
//
//        // send the same configuration for restart resiliency
//        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
//
//        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
//
//        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
//    }


    private void getThingToBuy() {

        PayPalPayment thingToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    private PayPalPayment getStuffToBuy(String paymentIntent) {
        //--- include an item list, payment amount details

        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();

        PayPalPayment payment = new PayPalPayment(new BigDecimal(cartModel.getOrderTotal()), "USD", getString(R.string.app_name) + "-Food Order", paymentIntent);

        //--- set other optional fields like invoice_number, custom field, and soft_descriptor
//        payment.custom("This is text that will be associated with the payment that the app can use.");

        return payment;
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
            case Constants.ActionCartSubmitSuccess:
                Intent intent = new Intent(this, OrderConfirmationActivity.class);
                intent.putExtra("PlacedTime", pstCurrentTime);
                intent.putExtra("isFreeItemCoupon", isFreeItemCoupon);
                startActivity(intent);
                break;
            case Constants.ActionCartSubmitFailed:
                break;
            case Constants.ActionNetworkFailed:
                break;
            case Constants.ActionCardSubmit:
                billingZipCode = existingCardList.get(cardSelectedPosition).getBillingZipCode();
                creditCardCSC = existingCardList.get(cardSelectedPosition).getCreditCardCSC();
                creditCardExpired = existingCardList.get(cardSelectedPosition).getCreditCardExpired();
                creditCardNumber = existingCardList.get(cardSelectedPosition).getCreditCardNumber();
                creditCardType = existingCardList.get(cardSelectedPosition).getCreditCardType();
                cartSubmit();
                break;
        }
    }


}
