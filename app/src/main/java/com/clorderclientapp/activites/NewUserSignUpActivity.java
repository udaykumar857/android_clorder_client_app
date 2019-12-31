package com.clorderclientapp.activites;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.clorderclientapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewUserSignUpActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {
    private Button createAccountBtn, cancelBtn;
    private TextView existingCustomerTxt;
    private EditText nameEditTxt, emailEditTxt, passwordEditTxt;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_sign_up);
        initViews();
        listeners();
        httpRequest = new HttpRequest();

        if (getIntent() != null) {
            emailEditTxt.setText(getIntent().getStringExtra("email"));
            passwordEditTxt.setText(getIntent().getStringExtra("password"));
        }
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);

    }

    private void initViews() {
        nameEditTxt = (EditText) findViewById(R.id.name_edit);
        emailEditTxt = (EditText) findViewById(R.id.email_edit);
        passwordEditTxt = (EditText) findViewById(R.id.password_edit);
        createAccountBtn = (Button) findViewById(R.id.create_account_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        existingCustomerTxt = (TextView) findViewById(R.id.existing_customer_txt);
    }

    private void listeners() {
        createAccountBtn.setOnClickListener(this);
        existingCustomerTxt.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

    }

    private boolean domainValid(String domainString) {
        boolean isDomain = false;
        String domainList[] = {"com", "co.in", "net", "org", "edu", "co.nz"};
        for (int i = 0; i < domainList.length; i++) {
            if (domainString.equals(domainList[i])) {
                isDomain = true;
                break;
            }

        }
        return isDomain;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.create_account_btn:
                if (nameEditTxt.getText().length() > 0) {
                    if (emailEditTxt.getText().length() > 0) {
                        boolean isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailEditTxt.getText()).matches();
                        if (isEmailValid) {
                            String userName[] = emailEditTxt.getText().toString().trim().split("@");
//                            .com, .co.in, .net, .org, .edu, .co.nz
                            String domain = userName[1].split("\\.", 2)[1];
                            if (domainValid(domain)) {

                                if (passwordEditTxt.getText().length() > 0) {
                                    if (isValidPassword(passwordEditTxt.getText().toString())) {
                                        if (Utils.isNetworkAvailable(this)) {
                                            createClorderUser();
                                        } else {
                                            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionCreateClorderUserFailed);
                                        }
                                    } else {
                                        Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.password_validation_msg), Constants.ActionPasswordValidationAlert);
                                    }

                                } else {
                                    Utils.toastDisplay(this, getString(R.string.password_empty_msg));
                                }
                            } else {
                                Utils.toastDisplay(this, getString(R.string.email_valid_msg));
                            }


                        } else {
                            Utils.toastDisplay(this, getString(R.string.email_valid_msg));
                        }

                    } else {
                        Utils.toastDisplay(this, getString(R.string.email_empty_msg));
                    }

                } else {
                    Utils.toastDisplay(this, getString(R.string.name_empty_msg));
                }

                break;
            case R.id.existing_customer_txt:
                onBackPressed();
                break;
            case R.id.cancel_btn:
                onBackPressed();
                break;
        }
        //key Board Dismiss
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void createClorderUser() {
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Constants.clientId);
            requestObject.put("Email", emailEditTxt.getText().toString());
            requestObject.put("Password", passwordEditTxt.getText().toString());
            requestObject.put("FullName", nameEditTxt.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.registerClorderUser(this, requestObject, Constants.RegisterClorderUser);
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.RegisterClorderUser:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if (!(responseObject.getString("UserId")).equals("0")) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            {
//                                "clientId": 5,
//                                    "UserId": 93045,
//                                    "Email": "udaykumar29",
//                                    "Password": "123456",
//                                    "isFirstTimeUser": true,
//                                    "lastOrderDays": 3650,
//                                    "status": "User created",
//                                    "FirstName": "uday",
//                                    "FullName": "uday kumar",
//                                    "LastName": "kumar",
//                                    "UserAddress": null,
//                                    "PaymentInformation": null
//                            }
                            JSONObject userCredentials = (JSONObject) response;
                            editor.putString("userCredentials", userCredentials.toString());
//                                0:clorderSignIn/SignUp
//                                1:Facebook Login
//                                2:Google Login
                            editor.putInt("isFromLogin", 1);
                            editor.apply();
                            Intent intent = new Intent(this, DeliveryAddressActivity.class);
                            intent.putExtra("isFromUserCreation", 1);
                            intent.putExtra("email", emailEditTxt.getText().toString());
                            intent.putExtra("name", nameEditTxt.getText().toString());
                            startActivity(intent);
                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.alert_txt), String.format("%s", responseObject.getString("status")), Constants.ActionSignUpFailed);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public static boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,15}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    @Override
    public void userAction(int actionType) {

    }
}
