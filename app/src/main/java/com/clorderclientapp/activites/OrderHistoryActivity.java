package com.clorderclientapp.activites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.adapters.OrderHistoryAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.OrderHistoryModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.RecyclerViewClickListener;
import com.clorderclientapp.utils.Utils;
import com.clorderclientapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderHistoryActivity extends AppCompatActivity implements ResponseHandler, View.OnClickListener, UserActionInterface {
    private RecyclerView orderHistoryRecyclerView;
    private OrderHistoryAdapter orderHistoryAdapter;
    private ArrayList<OrderHistoryModel> orderHistoryList;
    private String userId = "0";
    private HttpRequest httpRequest;
    private ImageView historyBack;
    private TextView noPastOrdersTxt;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        initViews();
        listeners();
        sharedPreferences = getSharedPreferences("MySharedPreferences", MODE_PRIVATE);
        boolean isLogin = sharedPreferences.contains("userCredentials");

        if (isLogin) {
            try {
                JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                userId = userCredentials.getString("UserId");
                httpRequest = new HttpRequest();
                orderHistoryList = new ArrayList<>();
                recyclerViewInit();
                if (Utils.isNetworkAvailable(this)) {
                    fetchClientOrderHistory();
                } else {
                    Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionClientOrderHistoryFailed);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() {
        orderHistoryRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        historyBack = (ImageView) findViewById(R.id.history_back);
        noPastOrdersTxt = (TextView) findViewById(R.id.no_past_orders_txt);
    }

    private void listeners() {
        historyBack.setOnClickListener(this);
        orderHistoryRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this,
                new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(OrderHistoryActivity.this, OrderHistoryDetailsActivity.class);
                        intent.putExtra("orderId", orderHistoryList.get(position).getOrderId());
                        startActivity(intent);
                    }
                }));
    }

    private void recyclerViewInit() {
        orderHistoryAdapter = new OrderHistoryAdapter(this, orderHistoryList);
        orderHistoryRecyclerView.setAdapter(orderHistoryAdapter);
        orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    private void fetchClientOrderHistory() {
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Constants.clientId);
            requestObject.put("userId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("fetchClientOrderHistory", "" + requestObject.toString());
        httpRequest.fetchClientOrderHistory(this, requestObject, Constants.FetchClientOrderHistory);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.history_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, JohnniesPizzaScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.FetchClientOrderHistory:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    ArrayList<OrderHistoryModel> orderHistoryArrayList = (ArrayList<OrderHistoryModel>) response;
//                    {
//                        "clientId": 5,
//                            "userId": 92967,
//                            "ClientOrders": [
//                        {
//                            "orderid": 121794,
//                                "OrderTotal": "19.6200",
//                                "OrderDate": "8/19/2016 6:36:58 AM"
//                        },
//                        {
//                            "orderid": 121793,
//                                "OrderTotal": "29.2700",
//                                "OrderDate": "8/19/2016 6:33:41 AM"
//                        },
//                        {
//                            "orderid": 121747,
//                                "OrderTotal": "26.0300",
//                                "OrderDate": "7/30/2016 9:28:27 PM"
//                        }
//                        ],
//                        "ordersCount": 3,
//                            "status": "The call to fetchClientOrderHistory is successful."
//                    }
                    if (orderHistoryArrayList.size() > 0) {
                        orderHistoryRecyclerView.setVisibility(View.VISIBLE);
                        orderHistoryList.clear();
                        orderHistoryList.addAll(orderHistoryArrayList);
                        orderHistoryAdapter.notifyDataSetChanged();
                    } else {
                        orderHistoryRecyclerView.setVisibility(View.GONE);
                        noPastOrdersTxt.setVisibility(View.VISIBLE);
                    }


                }

                break;
        }

    }


    @Override
    public void userAction(int actionType) {

    }
}
