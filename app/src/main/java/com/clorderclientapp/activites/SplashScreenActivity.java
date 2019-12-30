package com.clorderclientapp.activites;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

public class SplashScreenActivity extends AppCompatActivity implements ResponseHandler, UserActionInterface {
    private static int SPLASH_TIME_OUT = 3000;
    private HttpRequest httpRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_layout);
        Utils.initializeSSLContext(this);
        resetOrderTimings();
        Realm realm = Realm.getDefaultInstance();
        httpRequest = new HttpRequest();
        try {
            realm.beginTransaction();
            realm.delete(CartModel.class);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
//                startActivity(new Intent(SplashScreenActivity.this, JohnniesPizzaScreenActivity.class));
//                finish();
                getLocations();
            }
        }, SPLASH_TIME_OUT);
    }


    private void resetOrderTimings() {
        Constants.selectedTime = null;
        Constants.selectedDate = null;
    }


    private void getLocations() {
        if (Utils.isNetworkAvailable(this)) {
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("clientId", Constants.clientId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Utils.startLoadingScreen(this);
            httpRequest.fetchClientChildLocations(this, requestObject, Constants.FetchClientChildLocations);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
        }

    }


    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.FetchClientChildLocations:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    try {
                        Constants.MultiLocationList.clear();
                        JSONObject locObj = (JSONObject) response;
                        if (locObj.getBoolean("isSuccess")) {
                            JSONArray locationArray = locObj.getJSONArray("ChildLocations");
                            Log.d("LocationList", locationArray.toString());
                            if (locationArray.length() == 1) {
                                startActivity(new Intent(this, JohnniesPizzaScreenActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(SplashScreenActivity.this, MultiLocationActivity.class));
                                finish();
                            }
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

        switch (actionType) {
            case Constants.ActionNetworkFailed:
                finish();
                break;
        }

    }
}

