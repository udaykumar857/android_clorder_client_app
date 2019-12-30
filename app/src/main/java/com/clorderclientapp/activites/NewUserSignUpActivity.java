package com.clorderclientapp.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

public class NewUserSignUpActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {
    private Button createAccountBtn, cancelBtn;
    private TextView existingCustomerTxt;
    private EditText nameEditTxt, emailEditTxt, passwordEditTxt;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    private String email, password;
    private ImageView backImg;
    private TextView termsPolicyTxt;

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
        String existingTxt = "<font color=#000000>Existing Customer /</font> <font color=#Fb5b5b>Login Here</font>";
        existingCustomerTxt.setText(Html.fromHtml(existingTxt));
    }

    private void initViews() {
        backImg = (ImageView) findViewById(R.id.back_img);
        nameEditTxt = (EditText) findViewById(R.id.name_edit);
        emailEditTxt = (EditText) findViewById(R.id.email_edit);
        passwordEditTxt = (EditText) findViewById(R.id.password_edit);
        createAccountBtn = (Button) findViewById(R.id.create_account_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        existingCustomerTxt = (TextView) findViewById(R.id.existing_customer_txt);
        termsPolicyTxt=findViewById(R.id.termsPolicyTxt);
    }

    private void listeners() {
        backImg.setOnClickListener(this);
        createAccountBtn.setOnClickListener(this);
        existingCustomerTxt.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        SpannableString SpanString = new SpannableString(
                "Terms of Use and Privacy Policy");
//        Terms of Use and Privacy Policy

        ClickableSpan teremsAndCondition = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

//                Utils.displayToast("Clickable span terms and codition",
//                        SignUp.this);

//                Intent mIntent = new Intent(SignUp.this, CommonWebView.class);
//                mIntent.putExtra("isTermsAndCondition", true);
//                startActivity(mIntent);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.clorder.com/terms-conditions.html"));
                startActivity(intent);

            }
        };

        ClickableSpan privacy = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

//                Utils.displayToast("Clickable span terms and codition",
//                        SignUp.this);
//
//                Intent mIntent = new Intent(SignUp.this, CommonWebView.class);
//                mIntent.putExtra("isPrivacyPolicy", true);
//                startActivity(mIntent);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.clorder.com/privacy-policy.html"));
                startActivity(intent);

            }
        };

        SpanString.setSpan(teremsAndCondition, 0, 12, 0);
        SpanString.setSpan(privacy, 17, 31, 0);
        SpanString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colr_red)), 0, 12, 0);
        SpanString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colr_red)), 17, 31, 0);
        SpanString.setSpan(new UnderlineSpan(), 0, 12, 0);
        SpanString.setSpan(new UnderlineSpan(), 17, 31, 0);

        termsPolicyTxt.setMovementMethod(LinkMovementMethod.getInstance());
        termsPolicyTxt.setText(SpanString, TextView.BufferType.SPANNABLE);
        termsPolicyTxt.setSelected(true);

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
                                    if (Utils.isValidPassword(passwordEditTxt.getText().toString())) {
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
                Intent intent=new Intent(this, SignInUserActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.cancel_btn:
                onBackPressed();
                break;
            case R.id.back_img:
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
            requestObject.put("clientId", Utils.getClientId(this));
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



    @Override
    public void userAction(int actionType) {

    }
}
