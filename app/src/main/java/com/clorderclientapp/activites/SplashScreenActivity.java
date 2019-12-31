package com.clorderclientapp.activites;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.MultiLocationModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

public class SplashScreenActivity extends AppCompatActivity implements ResponseHandler, UserActionInterface {
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_layout);
        Utils.initializeSSLContext(this);
        resetOrderTimings();
        Realm realm = Realm.getDefaultInstance();
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
                startActivity(new Intent(SplashScreenActivity.this, FindRestaurantActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);
    }


    private void resetOrderTimings() {
        Constants.selectedTime = null;
        Constants.selectedDate = null;
    }


    @Override
    public void responseHandler(Object response, int requestType) {

    }

    @Override
    public void userAction(int actionType) {

    }
}

