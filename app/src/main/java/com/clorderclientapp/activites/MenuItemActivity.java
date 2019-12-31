package com.clorderclientapp.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.adapters.MenuItemAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.CategoryItemModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.RecyclerViewClickListener;
import com.clorderclientapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;


public class MenuItemActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {

    RecyclerView menuItemRecyclerView;
    MenuItemAdapter menuItemAdapter;

    HttpRequest httpRequest;
    TextView menuItemTitleTxt, subtotalValue, checkOutText;

    int currentCategoryIndex = 0;
    ImageView frontArrow, backArrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_item_layout);
        httpRequest = new HttpRequest();

        initView();
        currentCategoryIndex = getIntent().getIntExtra("categoryPosition", 0);

//        if (currentCategoryIndex == 0) {
//            backArrow.setVisibility(View.INVISIBLE);
//        } else if (currentCategoryIndex == Constants.MenuCategoryArrayList.size() - 1) {
//            frontArrow.setVisibility(View.INVISIBLE);
//        }

        menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
        clientItemsRequest();
        menuItemAdapter = new MenuItemAdapter(this, Constants.CategoryItemList);
        menuItemRecyclerView.setAdapter(menuItemAdapter);
        menuItemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listeners();

    }


    private void initView() {
        menuItemTitleTxt = (TextView) findViewById(R.id.menu_item_title_txt);
        menuItemRecyclerView = (RecyclerView) findViewById(R.id.menu_item_list);

        frontArrow = (ImageView) findViewById(R.id.front_arrow);
        backArrow = (ImageView) findViewById(R.id.back_arrow);
        subtotalValue = (TextView) findViewById(R.id.subtotal_text);
        checkOutText = (TextView) findViewById(R.id.check_out_text);
    }


    private void listeners() {
        frontArrow.setOnClickListener(this);
        backArrow.setOnClickListener(this);
        checkOutText.setOnClickListener(this);
        menuItemRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this, new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent itemIntent = new Intent(MenuItemActivity.this, ItemSelectionActivity.class);
                itemIntent.putExtra("position", position);
                itemIntent.putExtra("modifierSelection", 0);
                startActivity(itemIntent);
            }
        }));

    }

    private void clientItemsRequest() {
        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("clientId", Constants.clientId);
                requestObject.put("categoryId", Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpRequest.fetchClientItems(this, requestObject, Constants.FetchClientItems);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        if (cartModel != null) {
            if (cartModel.getCartItemList().size() > 0) {
                subtotalValue.setText(String.format("$%s", Utils.roundFloatString(cartModel.getSubtotal(), 2) + " SUBTOTAL"));
            } else {
                subtotalValue.setText(String.format("$0%s", " SUBTOTAL"));
            }
        } else {
            subtotalValue.setText(String.format("$0%s", " SUBTOTAL"));
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Realm.getDefaultInstance().close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.back_arrow:
//                currentCategoryIndex -= 1;
//                if (currentCategoryIndex > 0) {
//                    frontArrow.setVisibility(View.VISIBLE);
//                    backArrow.setVisibility(View.VISIBLE);
//                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
//                    clientItemsRequest();
//                } else if (currentCategoryIndex == 0) {
//                    backArrow.setVisibility(View.INVISIBLE);
//                    frontArrow.setVisibility(View.VISIBLE);
//                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
//                    clientItemsRequest();
//                }
//                break;
//            case R.id.front_arrow:
//                currentCategoryIndex += 1;
//                if (currentCategoryIndex < Constants.MenuCategoryArrayList.size() - 1) {
//                    backArrow.setVisibility(View.VISIBLE);
//                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
//                    clientItemsRequest();
//                } else if (currentCategoryIndex == Constants.MenuCategoryArrayList.size() - 1) {
//                    frontArrow.setVisibility(View.INVISIBLE);
//                    backArrow.setVisibility(View.VISIBLE);
//                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
//                    clientItemsRequest();
//                }
//
//                break;
            case R.id.back_arrow:
                menuItemRecyclerView.setVisibility(View.INVISIBLE);
                currentCategoryIndex -= 1;
                if (currentCategoryIndex > 0) {
                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
                    clientItemsRequest();
                } else if (currentCategoryIndex == 0) {
                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
                    clientItemsRequest();
                } else {
                    currentCategoryIndex = Constants.MenuCategoryArrayList.size() - 1;
                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
                    clientItemsRequest();
                }
                break;
            case R.id.front_arrow:
                menuItemRecyclerView.setVisibility(View.INVISIBLE);
                currentCategoryIndex += 1;
                if (currentCategoryIndex < Constants.MenuCategoryArrayList.size() - 1) {
                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
                    clientItemsRequest();
                } else if (currentCategoryIndex == Constants.MenuCategoryArrayList.size() - 1) {
                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
                    clientItemsRequest();
                } else {
                    currentCategoryIndex = 0;
                    menuItemTitleTxt.setText(Constants.MenuCategoryArrayList.get(currentCategoryIndex).getCategoryTitle());
                    clientItemsRequest();
                }

                break;

            case R.id.check_out_text:
                startActivity(new Intent(this, CartActivity.class));
                break;

        }
    }

    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {
            case Constants.FetchClientItems:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    Constants.CategoryItemList.clear();
                    Constants.CategoryItemList.addAll((ArrayList<CategoryItemModel>) response);
                    menuItemAdapter.notifyDataSetChanged();
                    menuItemRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    Utils.showPositiveDialog(this, "Message", getString(R.string.unable_to_fetch_menu_items), Constants.ActionMenuItemFailed);
                }
                if (Constants.CategoryItemList.size() == 0) {
                    backArrow.setVisibility(View.INVISIBLE);
                    frontArrow.setVisibility(View.INVISIBLE);
                }
                break;
        }

    }

    @Override
    public void userAction(int actionType) {

    }
}
