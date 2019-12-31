package com.clorderclientapp.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.clorderclientapp.R;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {

    private ImageView backImg;
    private EditText oldPasswordEdit, newPasswordEdit, confirmPasswordEdit;
    private Button saveBtn;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        initViews();
        listeners();
    }

    private void initViews() {
        backImg = findViewById(R.id.backImg);
        oldPasswordEdit = findViewById(R.id.old_password_edit);
        newPasswordEdit = findViewById(R.id.new_password_edit);
        confirmPasswordEdit = findViewById(R.id.confirm_password_edit);
        saveBtn = findViewById(R.id.saveBtn);
    }

    private void listeners() {
        backImg.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.backImg:
                onBackPressed();
                break;
            case R.id.saveBtn:
                if (oldPasswordEdit.getText().length() > 0) {
                    if (Utils.isValidPassword(oldPasswordEdit.getText().toString())) {
                        if (newPasswordEdit.getText().length() > 0) {
                            if (Utils.isValidPassword(newPasswordEdit.getText().toString())) {
                                if (confirmPasswordEdit.getText().length() > 0) {
                                    if ((newPasswordEdit.getText().toString()).equals(confirmPasswordEdit.getText().toString())) {
                                        changeUserPasswordRequest();
                                    } else {
                                        Utils.toastDisplay(this, getString(R.string.new_confirm_password_validation_msg));
                                        confirmPasswordEdit.requestFocus();
                                        InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                    }
                                } else {
                                    Utils.toastDisplay(this, getString(R.string.confirm_password_empty_msg));
                                    confirmPasswordEdit.requestFocus();
                                    InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                }
                            } else {
                                Utils.toastDisplay(this, getString(R.string.new_password_validation_msg));
                                newPasswordEdit.requestFocus();
                                InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            }
                        } else {
                            Utils.toastDisplay(this, getString(R.string.new_password_empty_msg));
                            newPasswordEdit.requestFocus();
                            InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }
                    } else {
                        Utils.toastDisplay(this, getString(R.string.old_password_validation_msg));
                        oldPasswordEdit.requestFocus();
                        InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }

                } else {
                    Utils.toastDisplay(this, getString(R.string.old_password_empty_msg));
                    oldPasswordEdit.requestFocus();
                    InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm2.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }

                break;
        }

    }

    private void changeUserPasswordRequest() {
        if (Utils.isNetworkAvailable(this)) {
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("ClientId", Utils.getClientId(this));
                boolean isUserCredentialsData = sharedPreferences.contains("userCredentials");
                if (isUserCredentialsData) {
                    JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                    requestObject.put("UserEmailId", userCredentials.getString("Email"));
                    requestObject.put("UserId", userCredentials.getString("UserId"));
//                    requestObject.put("Password", userCredentials.getString("Password"));
                }
                requestObject.put("OldPassword", oldPasswordEdit.getText());
                requestObject.put("NewPassword", newPasswordEdit.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utils.startLoadingScreen(this);
            httpRequest.changeUserPassword(this, requestObject, Constants.ChangeUserPassword);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
        }
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.ChangeUserPassword:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if (responseObject.getBoolean("isSuccess")) {
                            Utils.logout(this);
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.change_password_success), Constants.ActionChangePasswordSuccess);
                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), responseObject.getString("status"), Constants.ActionChangePasswordStatus);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), "Unable to process your request.", Constants.ActionChangePasswordStatus);
                }
                break;

        }
    }

    @Override
    public void userAction(int actionType) {

        switch (actionType) {
            case Constants.ActionChangePasswordSuccess:
                startActivity(new Intent(this, JohnniesPizzaScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
        }

    }
}
