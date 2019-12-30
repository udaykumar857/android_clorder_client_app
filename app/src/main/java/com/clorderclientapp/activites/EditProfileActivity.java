package com.clorderclientapp.activites;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler ,UserActionInterface{
    private TextView editImgTxt;
    private EditText nameEdit, emailEdit;
    private Button saveBtn;
    private ImageView editBack;
    private SharedPreferences sharedPreferences;
    private HttpRequest httpRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        initViews();
        listeners();
        boolean isUserDetails = sharedPreferences.contains("userDetails");
        if (isUserDetails) {
            try {
                JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                nameEdit.setText(userCredentials.getString("FullName"));
                emailEdit.setText(userCredentials.getString("Email"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() {
        editBack = findViewById(R.id.edit_back);
        editImgTxt = findViewById(R.id.editImgTxt);
        nameEdit = findViewById(R.id.name_edit);
        emailEdit = findViewById(R.id.email_edit);
        saveBtn = findViewById(R.id.saveBtn);
    }

    private void listeners() {
        editBack.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_back:
                onBackPressed();
                break;

            case R.id.saveBtn:
//                onBackPressed();
                updateClorderUser();
                break;
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
            if (Utils.isNetworkAvailable(this)) {
                Utils.startLoadingScreen(this);
                JSONObject requestObject = new JSONObject();
                requestObject.put("clientId", Utils.getClientId(this));
                boolean isUserCredentialsData = sharedPreferences.contains("userCredentials");
                if (isUserCredentialsData) {
                    JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                    requestObject.put("UserId", userCredentials.getString("UserId"));
                    requestObject.put("Email", emailEdit.getText().toString());
                    requestObject.put("FirstName", nameEdit.getText().toString());
                    requestObject.put("Password", userCredentials.getString("Password"));
                    requestObject.put("FullName", nameEdit.getText().toString());
//                    requestObject.put("LastName", userCredentials.getString("LastName"));
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.UpdateClorderUser:
                Utils.cancelLoadingScreen();
                saveUserDetails(response);
                onBackPressed();
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

    @Override
    public void userAction(int actionType) {
    }
}
