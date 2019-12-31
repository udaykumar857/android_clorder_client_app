package com.clorderclientapp.activites;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.ExistingCardModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PaymentAddCardActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {

    private ImageView accountUpdateBack;
    private Button addCardBtn;
    private ExistingCardModel existingCardModel;
    private int cardObjectPosition = 0;
    private EditText nameOnCardEdit, cardNumberEdit, cvvEdit, billingZipCodeEdit;
    private Spinner monthSpinner, yearSpinner;
    private boolean isNewCard = false;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    private ArrayList<String> monthList;
    private ArrayList<String> yearList;
    private TextView paymentsTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_add_card);
        initViews();
        listeners();
        httpRequest = new HttpRequest();
//        monthList=new ArrayList<>();
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        if (getIntent() != null) {
            isNewCard = getIntent().getBooleanExtra("isNewCard", false);
            if (!isNewCard) {
                addCardBtn.setText(getResources().getString(R.string.update_txt));
                paymentsTxt.setText(getResources().getString(R.string.payment_update_card_txt));
                existingCardModel = getIntent().getParcelableExtra("cardObject");
                setData();
            }
        }
    }

    private void initViews() {
        accountUpdateBack = (ImageView) findViewById(R.id.account_update_back);
        nameOnCardEdit = (EditText) findViewById(R.id.name_on_card_edit);
        cardNumberEdit = (EditText) findViewById(R.id.card_number_edit);
        cvvEdit = (EditText) findViewById(R.id.cvv_edit);
        billingZipCodeEdit = (EditText) findViewById(R.id.billing_zip_code_edit);
        monthSpinner = (Spinner) findViewById(R.id.month_spinner);
        yearSpinner = (Spinner) findViewById(R.id.year_spinner);
        addCardBtn = (Button) findViewById(R.id.add_card_btn);
        paymentsTxt = findViewById(R.id.payments_txt);
    }

    private void listeners() {
        yearList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        int currentYear = Integer.parseInt(simpleDateFormat.format(new Date()));
        for (int i = 0; i < 21; i++) {
            yearList.add(String.valueOf(currentYear + i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this,
                R.layout.card_spinner_text_view, yearList);
        yearSpinner.setAdapter(yearAdapter);
        monthList = new ArrayList<>();
        String mList[] = getResources().getStringArray(R.array.expiry_month);
        for (int j = 0; j < mList.length; j++) {
            monthList.add(mList[j]);
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(this,
                R.layout.card_spinner_text_view, monthList);
        monthSpinner.setAdapter(monthAdapter);

        accountUpdateBack.setOnClickListener(this);
        addCardBtn.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.account_update_back:
                onBackPressed();
                break;
            case R.id.add_card_btn:
                if (nameOnCardEdit.getText().toString().trim().length() > 0) {
                    if (cardNumberEdit.getText().toString().trim().length() > 0) {
                        if (cardNumberEdit.getText().length() >= 12) {
                            if (cvvEdit.getText().toString().trim().length() > 0) {
                                if (cvvEdit.getText().toString().trim().length() >= 3) {
                                    if (billingZipCodeEdit.getText().toString().trim().length() >= 5) {
                                        Log.d("ValidCard", "" + Utils.Check(cardNumberEdit.getText().toString()));
                                        if (Utils.Check(cardNumberEdit.getText().toString().trim())) {
                                            boolean isCardRepeated = false;
                                            if (isNewCard) {
                                                for (int i = 0; i < UserCardListActivity.existingCardList.size(); i++) {
                                                    if (UserCardListActivity.existingCardList.get(i).getCreditCardNumber().equals(cardNumberEdit.getText().toString())) {
                                                        isCardRepeated = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!isCardRepeated) {
                                                updateClorderUser();
                                            } else {
                                                Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.repeated_card_msg), Constants.ActionCreditCardRepeatedStatus);
                                            }

                                        } else {
                                            Utils.toastDisplay(this, getString(R.string.card_num_valid_msg));
                                        }

                                    } else {
                                        Utils.toastDisplay(this, getString(R.string.zip_code_card_empty_msg));
                                    }
                                } else {
                                    Utils.toastDisplay(this, getString(R.string.cvv_num_wrong_msg));
                                }
                            } else {
                                Utils.toastDisplay(this, getString(R.string.cvv_card_empty_msg));
                            }
                        } else {
                            Utils.toastDisplay(this, getString(R.string.card_num_wrong_msg));
                        }
                    } else {
                        Utils.toastDisplay(this, getString(R.string.card_num_empty_msg));
                    }

                } else {
                    Utils.toastDisplay(this, getString(R.string.name_on_card_empty_msg));
                }

                break;
        }
    }

    private void setData() {
        nameOnCardEdit.setText(existingCardModel.getCreditCardName());
        cardNumberEdit.setText(existingCardModel.getCreditCardNumber());
        cvvEdit.setText(existingCardModel.getCreditCardCSC());
        billingZipCodeEdit.setText(existingCardModel.getBillingZipCode());
//     "2016-08-18T23:37:00"
        String orderDate[] = (existingCardModel.getCreditCardExpired()).split("T");
        String date[] = orderDate[0].split("-");
        String year = date[0];
        String month = date[1];

//        String years[] = getResources().getStringArray(R.array.expiry_year);
        for (int i = 0; i < yearList.size(); i++) {
            if (yearList.get(i).equals(year)) {
                yearSpinner.setSelection(i);
                break;
            }
        }
        int months = Integer.parseInt(month);
        monthSpinner.setSelection(months - 1);


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
        if (!Constants.isGuestUserLogin) {
            if (Utils.isNetworkAvailable(this)) {
                Utils.startLoadingScreen(this);
                JSONObject requestObject = new JSONObject();
                try {
                    requestObject.put("clientId", Utils.getClientId(this));
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
                    creditCardObject.put("BillingZipCode", billingZipCodeEdit.getText().toString());
                    if (isNewCard) {
                        creditCardObject.put("CardId", "0");
                    } else {
                        creditCardObject.put("CardId", existingCardModel.getCardId());
                    }
                    creditCardObject.put("CreditCardCSC", cvvEdit.getText().toString());
                    creditCardObject.put("CreditCardExpired", String.format("%s", yearSpinner.getSelectedItem() + "-" + (monthSpinner.getSelectedItemPosition() + 1) + "-" + "01"));
                    creditCardObject.put("CreditCardName", nameOnCardEdit.getText().toString());
                    creditCardObject.put("CreditCardNumber", cardNumberEdit.getText().toString());

                    String ccNum = cardNumberEdit.getText().toString();
                    ArrayList<String> creditCardTypeList = Utils.getCreditCardTypeList();
                    for (int i = 0; i < creditCardTypeList.size(); i++) {
                        if (ccNum.matches(creditCardTypeList.get(i))) {
                            switch (i) {

//                    public enum CreditCardTypeEnum
//                    {
//                        [Name("American Express")]
//                        AmericanExpress = 1,
//
//                            [Name("Discover")]
//                        Discover = 2,
//
//                            [Name("Master Card")]
//                        MasterCard = 4,
//
//                            [Name("Visa")]
//                        VISA = 8,
//
//                            [Name("Diner Club")]
//                        DinerClub = 16,
//
//                            [Name("JCB International")]
//                        JCB = 32
//
//                    }

                                case 0:
//                            VISA = 8
                                    creditCardObject.put("CreditCardType", 8);
                                    break;
                                case 1:
//                            MasterCard = 4
                                    creditCardObject.put("CreditCardType", 4);
                                    break;
                                case 2:
                                    creditCardObject.put("CreditCardType", 1);
                                    break;
                                case 3:
//                            DinerClub = 16
                                    creditCardObject.put("CreditCardType", 16);
                                    break;
                                case 4:
//                            Discover = 2
                                    creditCardObject.put("CreditCardType", 2);
                                    break;
                                case 5:
//                            JCB = 32
                                    creditCardObject.put("CreditCardType", 32);
                                    break;
                                default:
//                            MasterCard = 4
                                    creditCardObject.put("CreditCardType", 4);
                                    break;
                            }
                            break;
                        }
                    }
                    creditCardObject.put("IsDeleted", false);
                    paymentInformationArray.put(creditCardObject);
                    requestObject.put("PaymentInformation", paymentInformationArray);

                    Log.d("RequestObject", requestObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                httpRequest.updateClorderUser(this, requestObject, Constants.UpdateClorderUser);
            } else {
                Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
            }
        } else {
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

            JSONArray paymentInformationArray = new JSONArray();
            JSONObject paymentObject = new JSONObject();
            try {
                paymentObject.put("BillingZipCode", billingZipCodeEdit.getText().toString());
                paymentObject.put("CardId", "0");

                paymentObject.put("CreditCardCSC", cvvEdit.getText().toString());
                paymentObject.put("CreditCardExpired", String.format("%s", yearSpinner.getSelectedItem() + "-" + (monthSpinner.getSelectedItemPosition() + 1) + "-" + "01"));
                paymentObject.put("CreditCardName", nameOnCardEdit.getText().toString());
                paymentObject.put("CreditCardNumber", cardNumberEdit.getText().toString());
                String ccNum = cardNumberEdit.getText().toString();
                ArrayList<String> creditCardTypeList = Utils.getCreditCardTypeList();
                for (int i = 0; i < creditCardTypeList.size(); i++) {
                    if (ccNum.matches(creditCardTypeList.get(i))) {
                        switch (i) {

//                    public enum CreditCardTypeEnum
//                    {
//                        [Name("American Express")]
//                        AmericanExpress = 1,
//
//                            [Name("Discover")]
//                        Discover = 2,
//
//                            [Name("Master Card")]
//                        MasterCard = 4,
//
//                            [Name("Visa")]
//                        VISA = 8,
//
//                            [Name("Diner Club")]
//                        DinerClub = 16,
//
//                            [Name("JCB International")]
//                        JCB = 32
//
//                    }

                            case 0:
//                            VISA = 8
                                paymentObject.put("CreditCardType", 8);
                                break;
                            case 1:
//                            MasterCard = 4
                                paymentObject.put("CreditCardType", 4);
                                break;
                            case 2:
                                paymentObject.put("CreditCardType", 1);
                                break;
                            case 3:
//                            DinerClub = 16
                                paymentObject.put("CreditCardType", 16);
                                break;
                            case 4:
//                            Discover = 2
                                paymentObject.put("CreditCardType", 2);
                                break;
                            case 5:
//                            JCB = 32
                                paymentObject.put("CreditCardType", 32);
                                break;
                            default:
//                            MasterCard = 4
                                paymentObject.put("CreditCardType", 4);
                                break;
                        }
                        break;
                    }
                }
                paymentObject.put("IsDeleted", false);
                paymentInformationArray.put(paymentObject);
                Intent intent = new Intent(this, UserCardListActivity.class);
                intent.putExtra("paymentInformationArray", paymentInformationArray.toString());
                setResult(RESULT_OK, intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void responseHandler(Object response, int requestType) {

        switch (requestType) {
            case Constants.UpdateClorderUser:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if (responseObject.getBoolean("isSuccess")) {
                            //Update the Login Response by adding cards
                            updateLoginData(responseObject);
                            Intent intent = new Intent(this, UserCardListActivity.class);
                            intent.putExtra("paymentInformationArray", responseObject.getJSONArray("PaymentInformation").toString());
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Utils.showPositiveDialog(this, "Message", getString(R.string.card_details_wrong_msg), Constants.ActionCardSubmitFailed);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.showPositiveDialog(this, "Message", getString(R.string.un_able_to_add_card_msg), Constants.ActionCardSubmitFailed);
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

    @Override
    public void userAction(int actionType) {

    }
}
