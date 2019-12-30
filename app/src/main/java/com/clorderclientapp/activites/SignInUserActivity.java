package com.clorderclientapp.activites;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.realm.Realm;

public class SignInUserActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler,
        GoogleApiClient.OnConnectionFailedListener, UserActionInterface {

    private HttpRequest httpRequest;
    private EditText emailEditTxt, passwordEditTxt;
    private TextView signInBtn, guestUserBtn;
    private LoginButton fbLoginBtn;
    private SignInButton googleSignInBtn;
    private SharedPreferences sharedPreferences;
    private TextView createUserTxt;
    private int isReOrder;
    private static final int RC_SIGN_IN = 007;
    private GoogleApiClient mGoogleApiClient;
    CallbackManager callbackManager;
    private Button googleSignOutBtn;
    private ImageView googleImage, fbImage, backImg;
    private TextView forgotPasswordTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication(), getString(R.string.facebook_app_id));

        setContentView(R.layout.activity_signin_order);
        httpRequest = new HttpRequest();
        initViews();
        listeners();
        isReOrder = getIntent().getIntExtra("isReOrder", 0);
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
//        googleSignInBtn.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO);

        String createTxt = "<font color=#000000>New User /</font> <font color=#Fb5b5b>Register</font>";
        createUserTxt.setText(Html.fromHtml(createTxt));

//        SpannableString ss = new SpannableString("New User / Register");
//        ClickableSpan clickableSpan = new ClickableSpan() {
//            @Override
//            public void onClick(View textView) {
//                Intent intent = new Intent(SignInUserActivity.this, NewUserSignUpActivity.class);
//                intent.putExtra("email", emailEditTxt.getText().toString());
//                intent.putExtra("password", passwordEditTxt.getText().toString());
//                startActivity(intent);
//            }
//
//            @Override
//            public void updateDrawState(TextPaint ds) {
//                super.updateDrawState(ds);
//                ds.setUnderlineText(false);
//            }
//        };
//        ss.setSpan(clickableSpan, 0, 19, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//        createUserTxt.setText(ss);
//        createUserTxt.setLinkTextColor(Color.parseColor("#fb5b5b"));
//        createUserTxt.setMovementMethod(LinkMovementMethod.getInstance());
//        createUserTxt.setHighlightColor(Color.parseColor("#1242aa"));
//        fbLoginBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//        fbLoginBtn.setText("");
//        fbLoginBtn.setBackgroundResource(R.mipmap.facebook);
    }

    private void initViews() {
        backImg = (ImageView) findViewById(R.id.back_img);
        googleImage = (ImageView) findViewById(R.id.g_image);
        fbImage = (ImageView) findViewById(R.id.fb_image);
        emailEditTxt = (EditText) findViewById(R.id.email_edit);
        passwordEditTxt = (EditText) findViewById(R.id.password_edit);
        signInBtn = (TextView) findViewById(R.id.sign_in_btn);
        guestUserBtn = (TextView) findViewById(R.id.guest_user_btn);
        forgotPasswordTxt = findViewById(R.id.forgotPasswordTxt);
        fbLoginBtn = (LoginButton) findViewById(R.id.fb_login_btn);
//        googleSignInBtn = (SignInButton) findViewById(R.id.google_sign_in_btn);
        createUserTxt = (TextView) findViewById(R.id.create_user_txt);
        googleSignOutBtn = (Button) findViewById(R.id.google_sign_out_btn);
    }

    private void listeners() {
        callbackManager = CallbackManager.Factory.create();
        backImg.setOnClickListener(this);
        fbLoginBtn.setReadPermissions("email");
        fbLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.d("loginResult", "Success");

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("LoginActivity", response.toString());
                        try {
                            Log.d("Fb response", "" + object.getString("email") + "/t" + object.getString("name"));
                            clorderGoogleSignUpRequest(object.getString("email"), object.getString("name"), false);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location,name"); // Parámetros que pedimos a facebook
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("loginResult", "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("loginResult", error.getMessage() + "/t" + "" + error.getLocalizedMessage());
                Utils.showPositiveDialog(SignInUserActivity.this, "Alert", getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
            }
        });
        signInBtn.setOnClickListener(this);
        guestUserBtn.setOnClickListener(this);
        fbImage.setOnClickListener(this);
        fbLoginBtn.setOnClickListener(this);
        googleImage.setOnClickListener(this);
        googleSignOutBtn.setOnClickListener(this);
        createUserTxt.setOnClickListener(this);
        forgotPasswordTxt.setOnClickListener(this);
    }


    private void loginUserRequest() {
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Utils.getClientId(this));
            requestObject.put("Email", emailEditTxt.getText().toString());
            requestObject.put("Password", passwordEditTxt.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.loginUser(this, requestObject, Constants.LoginUser);
    }

    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d("TAG", "Got cached sign-in");
            GoogleSignInResult result = opr.get();

        } else {
            Utils.startLoadingScreen(this);
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    Utils.cancelLoadingScreen();
                    Log.d("status", "" + googleSignInResult.getStatus());
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d("status", "" + result.getStatus());
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("TAG", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                String personName = acct.getDisplayName();
                String email = acct.getEmail();
                Log.e("TAG", "Name: " + personName + ", email: " + email);
                clorderGoogleSignUpRequest(email, personName, true);
            }
            updateUI(true);

        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }


    private void clorderGoogleSignUpRequest(String email, String name, boolean isFromGoogleSignIn) {
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
//            {
//                    "clientId":5,
//                    "Email":"mobigo1234@gmail.com",
//                    "FirstName":"mobigo 123"
//            }
            requestObject.put("clientId", Utils.getClientId(this));
            requestObject.put("Email", email);
            requestObject.put("FullName", name);
            Log.d("clorderGoogleSignUpReq", " " + requestObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isFromGoogleSignIn) {
            httpRequest.clorderGoogleSignUp(this, requestObject, Constants.ClorderGoogleSignup);
        } else {
            httpRequest.clorderGoogleSignUp(this, requestObject, Constants.ClorderFacebookSignup);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.create_user_txt:
                Intent intent = new Intent(SignInUserActivity.this, NewUserSignUpActivity.class);
                intent.putExtra("email", emailEditTxt.getText().toString());
                intent.putExtra("password", passwordEditTxt.getText().toString());
                startActivity(intent);
                finish();
                break;
            case R.id.sign_in_btn:
                Constants.isGuestUserLogin = false;
                if (emailEditTxt.getText().length() > 0) {
                    boolean isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailEditTxt.getText()).matches();
                    if (isEmailValid) {
                        if (passwordEditTxt.getText().length() > 0) {
                            if (Utils.isNetworkAvailable(this)) {
                                loginUserRequest();
                            } else {
                                Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionLoginFailed);
                            }

                        } else {
                            Utils.toastDisplay(this, getString(R.string.password_empty_sign_in_msg));
                        }
                    } else {
                        Utils.toastDisplay(this, getString(R.string.email_valid_sign_in_msg));
                    }

                } else {
                    Utils.toastDisplay(this, getString(R.string.email_empty_sign_in_msg));
                }

                break;
            case R.id.guest_user_btn:
//                boolean isDeliveryKey = sharedPreferences.contains("isDeliveryOrder");
//                if (isDeliveryKey) {
//                    SharedPreferences.Editor pickUpEditor = sharedPreferences.edit();
//                    pickUpEditor.putBoolean("isDeliveryOrder", false);
//                    pickUpEditor.apply();
//                }
                if (isReOrder == 1) {
                    Constants.isGuestUserLogin = false;
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.guest_user_re_order_msg), Constants.ActionReOrderGuestUser);
                } else {
                    Constants.isGuestUserLogin = true;
                    Intent intent1 = new Intent(this, DeliveryAddressActivity.class);
                    intent1.putExtra("isFromUserCreation", 0);
                    startActivity(intent1);
                }


                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.beginTransaction();
                    realm.delete(CartModel.class);
                    realm.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                    realm.cancelTransaction();
                }
                boolean isUserDetails = sharedPreferences.contains("userDetails");
                if (isUserDetails) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("userDetails");
                    editor.apply();
                }

                break;
            case R.id.fb_login_btn:
                Constants.isGuestUserLogin = false;
                // Initialize the SDK before executing any other operations,
                PackageInfo info;
                try {
                    info = getPackageManager().getPackageInfo("com.clorderclientapp", PackageManager.GET_SIGNATURES);
                    for (Signature signature : info.signatures) {
                        MessageDigest md;
                        md = MessageDigest.getInstance("SHA");
                        md.update(signature.toByteArray());
                        String something = new String(Base64.encode(md.digest(), 0));
//                        String something = new String(Base64.encodeBytes(md.digest()));
                        Log.e("hash key", something);
                    }
                } catch (PackageManager.NameNotFoundException e1) {
                    Log.e("name not found", e1.toString());
                } catch (NoSuchAlgorithmException e) {
                    Log.e("no such an algorithm", e.toString());
                } catch (Exception e) {
                    Log.e("exception", e.toString());
                }
                break;
//            case R.id.google_sign_in_btn:
//                Constants.isGuestUserLogin = false;
//                if (Utils.isNetworkAvailable(this)) {
//                    signIn();
//                } else {
//                    Utils.showPositiveDialog(this, "Alert", getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
//                }
//                break;
            case R.id.fb_image:
                Constants.isGuestUserLogin = false;
//                String url="https://www.facebook.com/connect/login_success.html";
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
                fbLoginBtn.performClick();
                break;
            case R.id.g_image:
                Constants.isGuestUserLogin = false;
                if (Utils.isNetworkAvailable(this)) {
                    signIn();
                } else {
                    Utils.showPositiveDialog(this, "Alert", getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
                }
                break;
            case R.id.google_sign_out_btn:
                Constants.isGuestUserLogin = false;
                signOut();
                break;

            case R.id.forgotPasswordTxt:
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;

        }
        //key Board Dismiss
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }


    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.LoginUser:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if ((responseObject.getBoolean("isSuccess"))) {
                            if (!(responseObject.getString("UserId")).equals("0")) {


//                                {
//                                    "clientId": 5,
//                                        "UserId": 92973,
//                                        "Email": "qqq@qqq.com",
//                                        "Password": "Qwerty1234",
//                                        "isFirstTimeUser": true,
//                                        "lastOrderDays": 0,
//                                        "status": "The LoginUser call is successful",
//                                        "FirstName": "qqq",
//                                        "FullName": "qqq qwerty",
//                                        "LastName": "qwerty",
//                                        "UserAddress": {
//                                    "Address1": "poooooo",
//                                            "Address2": "ooooooo",
//                                            "AddressId": 63340,
//                                            "City": "South Jordan",
//                                            "PhoneNumber": "2096848765",
//                                            "ZipPostalCode": "84095"
//                                },
//                                    "PaymentInformation": [
//                                    {
//                                        "BillingZipCode": "25255",
//                                            "CardId": 52890,
//                                            "CreditCardCSC": "858",
//                                            "CreditCardExpired": "2018-01-01T00:00:00",
//                                            "CreditCardName": "Four",
//                                            "CreditCardNumber": "550000002552",
//                                            "CreditCardType": 8,
//                                            "IsDeleted": false
//                                    },
//                                    {
//                                        "BillingZipCode": "25255",
//                                            "CardId": 52892,
//                                            "CreditCardCSC": "858",
//                                            "CreditCardExpired": "2018-03-01T00:00:00",
//                                            "CreditCardName": "Four",
//                                            "CreditCardNumber": "200000000",
//                                            "CreditCardType": 8,
//                                            "IsDeleted": false
//                                    },
//                                    {
//                                        "BillingZipCode": "848",
//                                            "CardId": 52894,
//                                            "CreditCardCSC": "4545",
//                                            "CreditCardExpired": "2018-01-01T00:00:00",
//                                            "CreditCardName": "No",
//                                            "CreditCardNumber": "111222",
//                                            "CreditCardType": 8,
//                                            "IsDeleted": false
//                                    },
//                                    {
//                                        "BillingZipCode": "84095",
//                                            "CardId": 52898,
//                                            "CreditCardCSC": "2222",
//                                            "CreditCardExpired": "2017-01-02T00:00:00",
//                                            "CreditCardName": "Demo222",
//                                            "CreditCardNumber": "4444333322221111",
//                                            "CreditCardType": 8,
//                                            "IsDeleted": false
//                                    }
//                                    ]
//                                }
                                Realm realm = Realm.getDefaultInstance();
                                try {
                                    realm.beginTransaction();
                                    realm.delete(CartModel.class);
                                    realm.commitTransaction();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    realm.cancelTransaction();
                                }
                                //Clearing previous Data of delivery address....
                                boolean isUserDetails = sharedPreferences.contains("userDetails");
                                if (isUserDetails) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.remove("userDetails");
                                    editor.apply();
                                }

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                JSONObject userCredentials = (JSONObject) response;
                                editor.putString("userCredentials", userCredentials.toString());
//                                0:clorderSignIn/SignUp
//                                1:Facebook Login
//                                2:Google Login
                                editor.putInt("isFromLogin", 0);
                                editor.apply();
                                if (isReOrder == 1) {
                                    Intent intent = new Intent(this, OrderHistoryActivity.class);
                                    intent.putExtra("userId", ((JSONObject) response).getString("UserId"));
                                    startActivity(intent);
                                } else if (isReOrder == 0) {
                                    Intent intent = new Intent(this, DeliveryAddressActivity.class);
                                    intent.putExtra("isFromUserCreation", 0);
                                    startActivity(intent);
                                } else {
                                    onBackPressed();
                                }

                            } else {
                                Utils.toastDisplay(this, getString(R.string.user_credentials_wrong_msg));
                            }
                        } else {
                            Utils.toastDisplay(this, getString(R.string.user_credentials_wrong_msg));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.toastDisplay(this, "Network issue");
                }
                break;
            case Constants.ClorderGoogleSignup:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if (!(responseObject.getString("UserId")).equals("0")) {

                            Realm realm = Realm.getDefaultInstance();
                            try {
                                realm.beginTransaction();
                                realm.delete(CartModel.class);
                                realm.commitTransaction();
                            } catch (Exception e) {
                                e.printStackTrace();
                                realm.cancelTransaction();
                            }

                            //Clearing previous Data of delivery address....
                            boolean isUserDetails = sharedPreferences.contains("userDetails");
                            if (isUserDetails) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("userDetails");
                                editor.apply();
                            }


                            SharedPreferences.Editor editor = sharedPreferences.edit();
//            {
//                "clientId": 5,
//                    "UserId": 93083,
//                    "Email": "mobigo1234@gmail.com",
//                    "Password": null,
//                    "isFirstTimeUser": false,
//                    "lastOrderDays": 0,
//                    "status": "Google user successfully created in Clorder.",
//                    "FirstName": "mobigo 123",
//                    "FullName": null,
//                    "LastName": null,
//                    "UserAddress": null,
//                    "PaymentInformation": null
//            }
                            JSONObject userCredentials = (JSONObject) response;
                            editor.putString("userCredentials", userCredentials.toString());
//                                0:clorderSignIn/SignUp
//                                1:Facebook Login
//                                2:Google Login
                            editor.putInt("isFromLogin", 2);
                            editor.apply();


                            if (userCredentials.isNull("UserAddress")) {
                                Intent intent = new Intent(this, DeliveryAddressActivity.class);
                                intent.putExtra("isFromUserCreation", 1);
                                intent.putExtra("isReOrder", isReOrder);
                                intent.putExtra("email", userCredentials.getString("Email"));
                                intent.putExtra("name", userCredentials.getString("FirstName"));
                                startActivity(intent);
                            } else if (isReOrder == 0) {
                                Intent intent = new Intent(this, DeliveryAddressActivity.class);
                                intent.putExtra("isFromUserCreation", 0);
                                intent.putExtra("isReOrder", isReOrder);
                                intent.putExtra("email", userCredentials.getString("Email"));
                                intent.putExtra("name", userCredentials.getString("FirstName"));
                                startActivity(intent);
                            } else if (isReOrder == 1) {
                                Intent intent = new Intent(this, OrderHistoryActivity.class);
                                intent.putExtra("isFromUserCreation", 0);
                                intent.putExtra("isReOrder", isReOrder);
                                intent.putExtra("email", userCredentials.getString("Email"));
                                intent.putExtra("name", userCredentials.getString("FirstName"));
                                startActivity(intent);
                            } else {
                                onBackPressed();
                            }

                        } else {
                            Utils.toastDisplay(this, String.format("%s", String.format("%s", responseObject.getString("status"))));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.toastDisplay(this, "Network issue");
                }
                break;
            case Constants.ClorderFacebookSignup:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if (!(responseObject.getString("UserId")).equals("0")) {

                            //Clearing previous Data of delivery address....
                            boolean isUserDetails = sharedPreferences.contains("userDetails");
                            if (isUserDetails) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("userDetails");
                                editor.apply();
                            }


                            SharedPreferences.Editor editor = sharedPreferences.edit();
//            {
//                "clientId": 5,
//                    "UserId": 93083,
//                    "Email": "mobigo1234@gmail.com",
//                    "Password": null,
//                    "isFirstTimeUser": false,
//                    "lastOrderDays": 0,
//                    "status": "Google user successfully created in Clorder.",
//                    "FirstName": "mobigo 123",
//                    "FullName": null,
//                    "LastName": null,
//                    "UserAddress": null,
//                    "PaymentInformation": null
//            }

                            Realm realm = Realm.getDefaultInstance();
                            try {
                                realm.beginTransaction();
                                realm.delete(CartModel.class);
                                realm.commitTransaction();
                            } catch (Exception e) {
                                e.printStackTrace();
                                realm.cancelTransaction();
                            }


                            JSONObject userCredentials = (JSONObject) response;
                            editor.putString("userCredentials", userCredentials.toString());
//                                0:clorderSignIn/SignUp
//                                1:Facebook Login
//                                2:Google Login
                            editor.putInt("isFromLogin", 1);
                            editor.apply();

                            if (userCredentials.isNull("UserAddress")) {//UserAddress is null
                                Intent intent = new Intent(this, DeliveryAddressActivity.class);
                                intent.putExtra("isFromUserCreation", 1);
                                intent.putExtra("isReOrder", isReOrder);
                                intent.putExtra("email", userCredentials.getString("Email"));
                                intent.putExtra("name", userCredentials.getString("FullName"));
                                startActivity(intent);
                            } else if (isReOrder == 0) {
                                Intent intent = new Intent(this, DeliveryAddressActivity.class);
                                intent.putExtra("isFromUserCreation", 0);
                                intent.putExtra("isReOrder", isReOrder);
                                intent.putExtra("email", userCredentials.getString("Email"));
                                intent.putExtra("name", userCredentials.getString("FirstName"));
                                startActivity(intent);
                            } else if (isReOrder == 1) {
                                Intent intent = new Intent(this, OrderHistoryActivity.class);
                                intent.putExtra("isFromUserCreation", 0);
                                intent.putExtra("isReOrder", isReOrder);
                                intent.putExtra("email", userCredentials.getString("Email"));
                                intent.putExtra("name", userCredentials.getString("FirstName"));
                                startActivity(intent);
                            } else {
                                onBackPressed();
                            }

                        } else {
                            Utils.toastDisplay(this, String.format("%s", String.format("%s", responseObject.getString("status"))));
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

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //gmail
        Log.d("Connection Result", "" + connectionResult.getErrorMessage());
    }

    @Override
    public void userAction(int actionType) {

    }

    private void updateUI(boolean isUIUpdateState) {
        if (isUIUpdateState) {
            googleImage.setVisibility(View.GONE);
            googleSignOutBtn.setVisibility(View.VISIBLE);
        } else {
            googleImage.setVisibility(View.VISIBLE);
            googleSignOutBtn.setVisibility(View.GONE);
        }

    }



}
