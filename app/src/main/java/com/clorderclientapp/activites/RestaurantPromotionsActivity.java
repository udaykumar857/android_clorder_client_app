package com.clorderclientapp.activites;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.adapters.RestaurantPromotionsAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.HandleInterface;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.RestaurantPromotionsModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;
import com.clorderclientapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestaurantPromotionsActivity extends AppCompatActivity implements ResponseHandler, View.OnClickListener, HandleInterface, UserActionInterface {
    HttpRequest httpRequest;
    RecyclerView promotionsrecyclerView;
    ArrayList<RestaurantPromotionsModel> restaurantPromotionsList;
    RestaurantPromotionsAdapter restaurantPromotionsAdapter;
    ImageView promotionsBack;
    private TextView noCouponsTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_promotions);
        httpRequest = new HttpRequest();
        initView();
        setAdapter();
        listeners();
        if (Utils.isNetworkAvailable(this)) {
            GetRestaurantPromotions();
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionRestaurantPromotionsFailed);
        }
    }

    private void initView() {
        promotionsrecyclerView = (RecyclerView) findViewById(R.id.coupon_list);
        restaurantPromotionsList = new ArrayList<>();
        promotionsBack = (ImageView) findViewById(R.id.promotions_back);
        noCouponsTxt = findViewById(R.id.no_coupons_txt);
    }

    private void listeners() {
        promotionsBack.setOnClickListener(this);
    }

    private void setAdapter() {
        restaurantPromotionsAdapter = new RestaurantPromotionsAdapter(this, restaurantPromotionsList);
        promotionsrecyclerView.setAdapter(restaurantPromotionsAdapter);
        promotionsrecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    private void GetRestaurantPromotions() {
        Utils.startLoadingScreen(this);

        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Utils.getClientId(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.getRestaurantPromotions(this, requestObject, Constants.GetRestaurantPromotions);
    }

    @Override
    public void responseHandler(Object response, int requestType) {

        switch (requestType) {
            case Constants.GetRestaurantPromotions:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    restaurantPromotionsList.clear();
                    restaurantPromotionsList.addAll((ArrayList<RestaurantPromotionsModel>) response);
                    restaurantPromotionsAdapter.notifyDataSetChanged();
                    if (restaurantPromotionsList.size() > 0) {
                        promotionsrecyclerView.setVisibility(View.VISIBLE);
                        noCouponsTxt.setVisibility(View.GONE);
                    }else{
                        promotionsrecyclerView.setVisibility(View.GONE);
                        noCouponsTxt.setVisibility(View.VISIBLE);
                    }
                }
                break;

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promotions_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void handleClick(View view, int position) {
        Intent promotionsIntent = new Intent(this, CartActivity.class);
        promotionsIntent.putExtra("validForOrderType", restaurantPromotionsList.get(position).getValidForOrderType());
        promotionsIntent.putExtra("couponCode", restaurantPromotionsList.get(position).getCouponTitle());
        promotionsIntent.putExtra("discountType", restaurantPromotionsList.get(position).getDiscountType());
        setResult(RESULT_OK, promotionsIntent);
        finish();
    }

    @Override
    public void userAction(int actionType) {

    }
}
