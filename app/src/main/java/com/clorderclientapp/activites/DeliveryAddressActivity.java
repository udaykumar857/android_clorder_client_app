package com.clorderclientapp.activites;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.AddressModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import io.realm.Realm;

public class DeliveryAddressActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {
    private ImageView backImage;
    private LinearLayout nextStepMenuLayout;
    private int isFromUserCreation = 0;
    private TextView nextScreenTxt, addressType;
    private EditText nameEditTxt, buildingEditTxt, cityEditTxt, zipCodeEditTxt, phoneNumberEditTxt, emailEditTxt, addressEditTxt;
    private SharedPreferences sharedPreferences;
    private HttpRequest httpRequest;
    private int isReOrder = 0;
    private int isReOrderCart = 0;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1000;
    private Spinner addressSpinner;
    private int addressSpinnerPosition = 0;
    ArrayList<AddressModel> addressArrayList;
    private TextView addressTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);
        initViews();
        listeners();
        httpRequest = new HttpRequest();


        sharedPreferences = getSharedPreferences("MySharedPreferences", MODE_PRIVATE);
        if (getIntent() != null) {
            isFromUserCreation = getIntent().getIntExtra("isFromUserCreation", 0);
            emailEditTxt.setText(getIntent().getStringExtra("email"));
            nameEditTxt.setText(getIntent().getStringExtra("name"));
            isReOrder = getIntent().getIntExtra("isReOrder", 0);
            isReOrderCart = getIntent().getIntExtra("isReOrderCart", 0);
        }

        if (isFromUserCreation == 1) {
            emailEditTxt.setEnabled(false);
            nextScreenTxt.setText(getString(R.string.add_card_delivery_txt));
            nextScreenTxt.setGravity(Gravity.END);
        } else if (isFromUserCreation == 0) {
            if (isReOrder == 1) {
                if (isReOrderCart == 1) {
                    emailEditTxt.setEnabled(true);
                    nextScreenTxt.setText(getString(R.string.cart_txt));
                    nextScreenTxt.setGravity(Gravity.CENTER);
                } else {
                    emailEditTxt.setEnabled(false);
                    nextScreenTxt.setText(getString(R.string.re_order_txt));
                    nextScreenTxt.setGravity(Gravity.END);
                }

            } else {
                emailEditTxt.setEnabled(true);
                nextScreenTxt.setText(getString(R.string.menu_txt));
                nextScreenTxt.setGravity(Gravity.CENTER);
            }

        }


        boolean isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", false);
        if (isDeliveryOrder) {
            addressType.setText(getString(R.string.delivery_address_txt));
        } else {
            addressType.setText(getString(R.string.customer_address_txt));
        }

        setData();

    }

    private void initViews() {
        addressType = (TextView) findViewById(R.id.address_type);
        backImage = (ImageView) findViewById(R.id.back_img);
        nextStepMenuLayout = (LinearLayout) findViewById(R.id.next_step_menu_layout);
        nextScreenTxt = (TextView) findViewById(R.id.next_screen_txt);
        nameEditTxt = (EditText) findViewById(R.id.nameEditTxt);
        buildingEditTxt = (EditText) findViewById(R.id.buildingEditTxt);
        cityEditTxt = (EditText) findViewById(R.id.cityEditTxt);
        zipCodeEditTxt = (EditText) findViewById(R.id.zipCodeEditTxt);
        phoneNumberEditTxt = (EditText) findViewById(R.id.phoneNumberEditTxt);
        emailEditTxt = (EditText) findViewById(R.id.emailEditTxt);
        addressEditTxt = (EditText) findViewById(R.id.addressEditTxt);
        addressEditTxt.setInputType(InputType.TYPE_NULL);
        addressTxt = (TextView) findViewById(R.id.addressTxt);
        addressSpinner = (Spinner) findViewById(R.id.addressSpinner);
    }

    private void listeners() {
        backImage.setOnClickListener(this);
        nextStepMenuLayout.setOnClickListener(this);
        addressEditTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d("addressEditTxt", "fire");
                    Utils.hideKeyboard(DeliveryAddressActivity.this, addressEditTxt);
                    addressRequest();
                }
            }
        });

        addressEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("addressOnClick", "fire");
                Utils.hideKeyboard(DeliveryAddressActivity.this, addressEditTxt);
                addressRequest();
            }
        });

        try {
            if (Utils.getClientId(this) == 829 || Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("ShippingOptionId") == 2) {

                //        Westlake - 31303 Agoura Road, Westlake village, CA 91361
                //        Agoura - 29701 Agoura Road, Agoura Hills, CA 91301
                //        Amgen - 1290 Rancho Conego Blvd Newbury Park CA 91320
                //        Simi Valley - 2801 Cochran Stree Simivalley CA 93065
                addressTxt.setVisibility(View.VISIBLE);
                addressSpinner.setVisibility(View.VISIBLE);
                buildingEditTxt.setVisibility(View.GONE);
                cityEditTxt.setVisibility(View.GONE);
                zipCodeEditTxt.setVisibility(View.GONE);
                addressEditTxt.setVisibility(View.GONE);
                try {
                    JSONObject obj = new JSONObject(loadJSONFromAsset());

                    addressArrayList = new ArrayList<>();
                    ArrayList<String> addressNameList = new ArrayList<>();
                    JSONArray m_jArry;
                    if (Constants.addressList != null) {
                        m_jArry = Constants.addressList;
                    } else {
                        m_jArry = obj.getJSONArray("locations");
                    }
                    for (int i = 0; i < m_jArry.length(); i++) {
                        AddressModel addressModel = new AddressModel();
                        addressModel.setName(m_jArry.getJSONObject(i).getString("Address1"));
                        addressModel.setAddress1(m_jArry.getJSONObject(i).getString("Address1"));
                        addressModel.setAddress2(m_jArry.getJSONObject(i).getString("Address2"));
                        addressModel.setCity(m_jArry.getJSONObject(i).getString("City"));
                        addressModel.setZipCode(m_jArry.getJSONObject(i).getString("ZipPostalCode"));
                        addressArrayList.add(addressModel);
                        addressNameList.add(m_jArry.getJSONObject(i).getString("Address1"));
                    }
                    ArrayAdapter<String> orderTimeAdapter = new ArrayAdapter<String>(this,
                            R.layout.spinner_text_view, addressNameList);
                    addressSpinner.setAdapter(orderTimeAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                addressSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        addressSpinnerPosition = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else {
                addressTxt.setVisibility(View.GONE);
                addressSpinner.setVisibility(View.GONE);
                buildingEditTxt.setVisibility(View.VISIBLE);
                cityEditTxt.setVisibility(View.VISIBLE);
                zipCodeEditTxt.setVisibility(View.VISIBLE);
                addressEditTxt.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("grand_bawarchi.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void addressRequest() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setCountry("US")
                    .build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(typeFilter)
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                addressEditTxt.setText("");
                cityEditTxt.setText("");
                zipCodeEditTxt.setText("");
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("TAG", "Place: " + place.getName());
                Log.i("TAG", "Place: " + place.getAddress());
                Log.i("TAG", "Place: " + place.getPhoneNumber());
                Log.i("TAG", "Place: " + place.getPlaceTypes());
                Log.i("TAG", "Place: " + place.getLocale());
                String zip = null;
                String placeName = null;
                String city = null;
                try {
                    String addr = place.getAddress().toString();
                    String placeDetails[] = {};

                    placeName = place.getName().toString();
                    if (addr.contains(",")) {
                        placeDetails = addr.split(",");
                        for (int i = 0; i < placeDetails.length; i++) {
                            Log.i("placeDetails" + i, "" + placeDetails[i]);
                            if (i == 1) {
                                city = placeDetails[i].trim();
                            }
                            if (i == 2) {
                                String zipcode[] = placeDetails[i].trim().split(" ");
                                zip = zipcode[1].trim();
                                Log.i("zipcode" + i, "" + zipcode[1]);
                            }
                        }
                    }
                    Log.d("PlaceString", "" + placeName + "\t" + city + "\t" + zip);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (zip != null) {
                    if (zip.length() > 0) {
                        zipCodeEditTxt.setText(zip);
                    }
                }
                if (city != null) {
                    if (city.length() > 0) {
                        cityEditTxt.setText(city);
                    }
                }
                if (placeName != null) {
                    if (placeName.length() > 0) {
                        addressEditTxt.setText(placeName);
                    }
                }


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("TAG", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }




    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.next_step_menu_layout:
                try {


                    if (nameEditTxt.getText().toString().trim().length() > 0) {
                        if (phoneNumberEditTxt.getText().toString().trim().length() > 0) {
                            String mobileRegex = "^[0-9]{10}$";
                            boolean isPhoneValid = Pattern.matches(mobileRegex, phoneNumberEditTxt.getText());
                            if (isPhoneValid) {
                                if (emailEditTxt.getText().toString().trim().length() > 0) {
                                    boolean isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailEditTxt.getText()).matches();
                                    if (isEmailValid) {
                                        String userName[] = emailEditTxt.getText().toString().trim().split("@");
//                            .com, .co.in, .net, .org, .edu, .co.nz
                                        String domain = userName[1].split("\\.", 2)[1];
                                        if (Utils.domainValid(domain)) {
                                            if (Utils.getClientId(this) == 829 || Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("ShippingOptionId") == 2) {
                                                getData();
                                                //key Board Dismiss
//                                            InputMethodManager imm1 = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                                            imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                                updateClorderUser();
                                            } else {
                                                if (addressEditTxt.getText().toString().trim().length() > 0) {
                                                    if (cityEditTxt.getText().toString().trim().length() > 0) {
                                                        if (zipCodeEditTxt.getText().toString().trim().length() >= 5) {
                                                            getData();
                                                            //key Board Dismiss
                                                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                                            updateClorderUser();
                                                        } else {
                                                            zipCodeEditTxt.requestFocus();
                                                            InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                            imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                                            Utils.toastDisplay(this, getString(R.string.zip_code_empty_txt));
                                                        }
                                                    } else {
                                                        cityEditTxt.requestFocus();
                                                        InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                        imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                                        Utils.toastDisplay(this, getString(R.string.city_empty_txt));
                                                    }
                                                } else {
                                                    addressEditTxt.requestFocus();
                                                    InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                                    Utils.toastDisplay(this, getString(R.string.address_empty_txt));
                                                }
                                            }

                                        } else {
                                            emailEditTxt.requestFocus();
                                            InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                            Utils.toastDisplay(this, getString(R.string.email_valid_for_order_txt));
                                        }

                                    } else {
                                        emailEditTxt.requestFocus();
                                        InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                        Utils.toastDisplay(this, getString(R.string.email_valid_for_order_txt));
                                    }

                                } else {
                                    emailEditTxt.requestFocus();
                                    InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                    Utils.toastDisplay(this, getString(R.string.email_empty_txt));
                                }
                            } else {
                                phoneNumberEditTxt.requestFocus();
                                InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                Utils.toastDisplay(this, getString(R.string.phone_num_valid_msg));
                            }
                        } else {
                            phoneNumberEditTxt.requestFocus();
                            InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            Utils.toastDisplay(this, getString(R.string.phone_num_empty_txt));
                        }

                    } else {
                        nameEditTxt.requestFocus();
                        InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        Utils.toastDisplay(this, getString(R.string.name_empty_txt));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, JohnniesPizzaScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setData() {

        boolean isUserDetails = sharedPreferences.contains("userDetails");
        if (isUserDetails) {
            try {
                JSONObject userDetails = new JSONObject((String) sharedPreferences.getString("userDetails", ""));
                nameEditTxt.setText(userDetails.getString("name"));
                if (Utils.getClientId(this) != 829 || Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("ShippingOptionId") == 2) {
                    addressEditTxt.setText(userDetails.getString("address"));
                    buildingEditTxt.setText(userDetails.getString("buildings"));
                    cityEditTxt.setText(userDetails.getString("city"));
                    zipCodeEditTxt.setText(userDetails.getString("zipCode"));
                }
                phoneNumberEditTxt.setText(userDetails.getString("phoneNumber"));
                emailEditTxt.setText(userDetails.getString("email"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            boolean isLogin = sharedPreferences.contains("userCredentials");
            if (isLogin) {
                try {
                    JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                    emailEditTxt.setText(userCredentials.getString("Email"));
                    nameEditTxt.setText(userCredentials.getString("FullName"));
                    if (!(userCredentials.isNull("UserAddress"))) {

//                    "UserAddress": {
//                        "Address1": "xzzcz",
//                                "Address2": "czxczxc",
//                                "AddressId": 63338,
//                                "City": "www",
//                                "PhoneNumber": "2096848765",
//                                "ZipPostalCode": "08816"
//                    },
                        JSONObject userAddressObject = userCredentials.getJSONObject("UserAddress");
                        if (Utils.getClientId(this) != 829 || Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("ShippingOptionId") == 2) {
                            addressEditTxt.setText(userAddressObject.getString("Address1"));
                            buildingEditTxt.setText(userAddressObject.getString("Address2"));
                            cityEditTxt.setText(userAddressObject.getString("City"));
                            zipCodeEditTxt.setText(userAddressObject.getString("ZipPostalCode"));
                        }
                        phoneNumberEditTxt.setText(userAddressObject.getString("PhoneNumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    private void getData() {
        //storing the delivery address for order submit.
        JSONObject userDetails = new JSONObject();
        try {
            userDetails.put("name", nameEditTxt.getText().toString());
            if (Utils.getClientId(this) == 829 || Constants.clientSettingsObject.getJSONObject("ClientSettings").getInt("ShippingOptionId") == 2) {
                userDetails.put("address", addressArrayList.get(addressSpinnerPosition).getAddress1());
                userDetails.put("buildings", addressArrayList.get(addressSpinnerPosition).getAddress2());
                userDetails.put("city", addressArrayList.get(addressSpinnerPosition).getCity());
                userDetails.put("zipCode", addressArrayList.get(addressSpinnerPosition).getZipCode());
            } else {
                userDetails.put("address", addressEditTxt.getText().toString());
                userDetails.put("buildings", buildingEditTxt.getText().toString());
                userDetails.put("city", cityEditTxt.getText().toString());
                userDetails.put("zipCode", zipCodeEditTxt.getText().toString());
            }

            userDetails.put("phoneNumber", phoneNumberEditTxt.getText().toString());
            userDetails.put("email", emailEditTxt.getText().toString());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userDetails", userDetails.toString());
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateClorderUser() {
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

//1 cash remove
//                4 paypal
//                2

        try {

            if (isFromUserCreation == 1) {
                if (Utils.isNetworkAvailable(this)) {
                    Utils.startLoadingScreen(this);
                    JSONObject requestObject = new JSONObject();
                    requestObject.put("clientId", Utils.getClientId(this));
                    boolean isUserCredentialsData = sharedPreferences.contains("userCredentials");
                    if (isUserCredentialsData) {
                        JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                        requestObject.put("UserId", userCredentials.getString("UserId"));
                        requestObject.put("Email", userCredentials.getString("Email"));
                        requestObject.put("Password", userCredentials.getString("Password"));
                        requestObject.put("FirstName", userCredentials.getString("FirstName"));
                        requestObject.put("LastName", userCredentials.getString("LastName"));
                    }
                    boolean isUserDetailsData = sharedPreferences.contains("userDetails");
                    {
                        if (isUserDetailsData) {
                            JSONObject userDetailsObject = new JSONObject((String) sharedPreferences.getString("userDetails", ""));
                            JSONObject userAddressObject = new JSONObject();
                            userAddressObject.put("Address1", userDetailsObject.getString("address"));
                            userAddressObject.put("Address2", userDetailsObject.getString("buildings"));
                            userAddressObject.put("City", userDetailsObject.getString("city"));
                            userAddressObject.put("PhoneNumber", userDetailsObject.getString("phoneNumber"));
                            userAddressObject.put("ZipPostalCode", userDetailsObject.getString("zipCode"));
                            requestObject.put("UserAddress", userAddressObject);
                        }
                    }
                    Log.d("RequestObject", requestObject.toString());
                    httpRequest.updateClorderUser(this, requestObject, Constants.UpdateClorderUser);
                } else {
                    Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionUpdateClorderUserFailed);
                }

            } else if (isFromUserCreation == 0) {

                if (isReOrder == 1) {

                    if (isReOrderCart == 1) {
                        boolean isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", false);
                        if (isDeliveryOrder) {
                            deliveryFeeRequest();
                        } else {
                            startActivity(new Intent(this, CartActivity.class));
                        }
                    } else {
                        boolean isLogin = sharedPreferences.contains("userCredentials");
                        if (isLogin) {
                            JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                            Intent intent = new Intent(this, OrderHistoryActivity.class);
                            intent.putExtra("userId", userCredentials.getString("UserId"));
                            startActivity(intent);
                        }
                    }

                } else {
                    boolean isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", false);
                    if (isDeliveryOrder) {
                        deliveryFeeRequest();
                    } else {
                        startActivity(new Intent(this, AllDayMenuActivity.class));
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    requestObject.put("clientId", Utils.getClientId(this));
                    JSONObject userDetails = new JSONObject(sharedPreferences.getString("userDetails", ""));
                    requestObject.put("Address1", userDetails.getString("address"));
                    requestObject.put("Address2", userDetails.getString("buildings"));
                    requestObject.put("cityname", userDetails.getString("city"));
                    requestObject.put("zipcode", userDetails.getString("zipCode"));
                    Realm realm = Realm.getDefaultInstance();
                    CartModel cartModel = realm.where(CartModel.class).findFirst();
                    if (cartModel != null) {
                        requestObject.put("SubTotal", 0);
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


    @Override
    public void responseHandler(Object response, int requestType) {
        Utils.cancelLoadingScreen();
        switch (requestType) {
            case Constants.UpdateClorderUser:
                if (response != null) {
                    if (isFromUserCreation == 1) {
                        saveUserDetails(response);
                        Intent intent = new Intent(this, UserCardListActivity.class);
                        intent.putExtra("isFromUserCreation", 1);
                        startActivity(intent);
                    }
                }
                break;

            case Constants.FetchDeliveryFees:
                Realm realm = Realm.getDefaultInstance();
                CartModel cartModel = realm.where(CartModel.class).findFirst();
                if (response != null) {
                    try {
                        JSONObject responseObject = (JSONObject) response;
                        if (responseObject.getBoolean("isSuccess")) {
                            realm.beginTransaction();
                            if (cartModel == null) {
                                cartModel = new CartModel();
                                cartModel.setCartId(1);
                            } else {
                                cartModel.setCartId(cartModel.getCartId());
                            }
                            saveDeliveryFeeDetails(response);
                            try {
                                cartModel.setDeliveryCharge(responseObject.getInt("DeliverCharge"));
                                cartModel.setDeliveryDist(Float.parseFloat(responseObject.getString("DeliveryDist")));
                                realm.copyToRealmOrUpdate(cartModel);
                                realm.commitTransaction();
                                realm.close();
                                if (isReOrderCart == 1) {
                                    startActivity(new Intent(this, CartActivity.class));
                                } else {
                                    startActivity(new Intent(this, AllDayMenuActivity.class));
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), responseObject.getString("status"), Constants.ActionDeliveryAddressStatus);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), "Unable to get data.", Constants.ActionDeliveryAddressStatus);
                }
                break;

        }

    }

    private void saveUserDetails(Object response) {
        //updating login data shared preference... only for create user...
        boolean isUserCredentialsData = sharedPreferences.contains("userCredentials");
        if (isUserCredentialsData) {
            JSONObject responseObject = (JSONObject) response;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userCredentials", responseObject.toString());
            editor.apply();

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


    @Override
    public void userAction(int actionType) {

    }
}
