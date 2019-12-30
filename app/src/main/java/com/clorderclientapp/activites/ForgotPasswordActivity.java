package com.clorderclientapp.activites;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.clorderclientapp.R;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {
    private Button sendBtn;
    private EditText emailEdit;
    private ImageView backImg;
    private HttpRequest httpRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        httpRequest = new HttpRequest();
        initViews();
        listeners();
    }

    private void initViews() {
        backImg = findViewById(R.id.backImg);
        sendBtn = findViewById(R.id.sendBtn);
        emailEdit = findViewById(R.id.email_edit);
    }

    private void listeners() {
        backImg.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.backImg:
                onBackPressed();
                break;
            case R.id.sendBtn:
                if (emailEdit.getText().length() > 0) {
                    boolean isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailEdit.getText()).matches();
                    if (isEmailValid) {
                        String userName[] = emailEdit.getText().toString().trim().split("@");
//                            .com, .co.in, .net, .org, .edu, .co.nz
                        String domain = userName[1].split("\\.", 2)[1];
                        if (Utils.domainValid(domain)) {
                            verifyCaptcha();
                        } else {
                            Utils.toastDisplay(this, getString(R.string.email_valid_for_order_txt));
                        }
                    } else {
                        Utils.toastDisplay(this, getString(R.string.email_valid_sign_in_msg));
                    }
                } else {
                    Utils.toastDisplay(this, getString(R.string.email_empty_sign_in_msg));
                }
                break;
        }
    }


    private void verifyCaptcha() {
        SafetyNet.getClient(this).verifyWithRecaptcha(getResources().getString(R.string.site_key))
                .addOnSuccessListener(this,
                        new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                // Indicates communication with reCAPTCHA service was
                                // successful.
                                String userResponseToken = response.getTokenResult();
                                if (!userResponseToken.isEmpty()) {
                                    // Validate the user response token using the
                                    // reCAPTCHA siteverify API.
                                    Log.d("userResponseToken", userResponseToken);

                                    if (Utils.isNetworkAvailable(ForgotPasswordActivity.this)) {
                                        resetUserPassword();
                                    } else {
                                        Utils.showPositiveDialog(ForgotPasswordActivity.this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
                                    }


                                }
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.d("Error", "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d("Error", "Error: " + e.getMessage());
                        }
                    }
                });
    }


    private void resetUserPassword() {
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("Clientid", Utils.getClientId(this));
            requestObject.put("UserEmailId", emailEdit.getText().toString());
            Log.d("resetUserPassReq", requestObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.resetUserPassword(this, requestObject, Constants.ResetUserPassword);
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.ResetUserPassword:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    try {
                        JSONObject responseObject = (JSONObject) response;
                        if (responseObject.getBoolean("IsSuccess")) {
                            Utils.showPositiveDialog(this, "Password Reset", responseObject.getString("Status"), Constants.ActionResetUserPassword);
                        } else {
                            Utils.showPositiveDialog(this, "Password Reset", responseObject.getString("Status"), Constants.ActionResetUserPasswordFailed);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.toastDisplay(this, "Network issue");
                }
                break;
        }
    }

    @Override
    public void userAction(int actionType) {
        switch (actionType) {
            case Constants.ActionResetUserPassword:
                startActivity(new Intent(this, JohnniesPizzaScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
        }

    }
}
