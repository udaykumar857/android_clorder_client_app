package com.clorderclientapp.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.adapters.MenuItemAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.CategoryItemModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Converter;
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
    TextView menuItemTitleTxt, subtotalValue, checkOutText, itemCount;

    int currentCategoryIndex = 0;
    ImageView frontArrow, backArrow;
    ImageView backImg, cartImg, mToolBarBack;
    private LinearLayout cartLayout;
    private TextView noOrdersTxt;
    private Toolbar mToolbar;
    private View actionBarCustomView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_item_layout);
        httpRequest = new HttpRequest();

        initView();
        initActionBarViews();
        setupToolBar();
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
        menuItemRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listeners();

    }


    private void initView() {
//        backImg = (ImageView) findViewById(R.id.backImg);
        menuItemTitleTxt = (TextView) findViewById(R.id.menu_item_title_txt);
        menuItemRecyclerView = (RecyclerView) findViewById(R.id.menu_item_list);
//        cartImg = (ImageView) findViewById(R.id.cartImg);
        frontArrow = (ImageView) findViewById(R.id.front_arrow);
        backArrow = (ImageView) findViewById(R.id.back_arrow);
        subtotalValue = (TextView) findViewById(R.id.subtotal_text);
        checkOutText = (TextView) findViewById(R.id.check_out_text);
        cartLayout = findViewById(R.id.cart_layout);
        itemCount = findViewById(R.id.item_count);
        noOrdersTxt = findViewById(R.id.no_orders_txt);
        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        actionBarCustomView = LayoutInflater.from(this).inflate(R.layout.layout_menu_item_toolbar, null, false);
    }

    private void initActionBarViews() {
        mToolBarBack = (ImageView) actionBarCustomView.findViewById(R.id.backImg);
        mToolBarBack.setOnClickListener(this);
    }


    private void listeners() {
//        backImg.setOnClickListener(this);
//        cartImg.setOnClickListener(this);
        frontArrow.setOnClickListener(this);
        backArrow.setOnClickListener(this);
//        checkOutText.setOnClickListener(this);
        cartLayout.setOnClickListener(this);
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

    private void setupToolBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            ActionBar.LayoutParams params =
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
            actionBar.setCustomView(actionBarCustomView, params);
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    private void clientItemsRequest() {
        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("clientId", Utils.getClientId(this));
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
        invalidateOptionsMenu();
        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        if (cartModel != null) {
            if (cartModel.getCartItemList().size() > 0) {
                subtotalValue.setText(String.format("$%s", Utils.roundFloatString(cartModel.getSubtotal(), 2)));
            } else {
                subtotalValue.setText("$0");
            }
            itemCount.setText(String.format("%s", cartModel.getCartItemList().size() + " Item(s)"));
        } else {
            subtotalValue.setText("$0");
            itemCount.setText("0 Item(s)");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.cart_action);
        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        int cartItemCnt = 0;
        if (cartModel != null) {
            if (cartModel.getCartItemList().size() > 0) {
                cartItemCnt = cartModel.getCartItemList().size();
            }
        }
        menuItem.setIcon(Converter.convertLayoutToImage(this, cartItemCnt, R.mipmap.ic_shopping_cart_white_24dp));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.cart_action:
                startActivity(new Intent(this, CartActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();
        Realm.getDefaultInstance().close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backImg:
                onBackPressed();
                break;

            case R.id.cartImg:
                startActivity(new Intent(this, CartActivity.class));
                break;
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

            case R.id.cart_layout:
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
                    noOrdersTxt.setVisibility(View.GONE);
                } else {
                    menuItemRecyclerView.setVisibility(View.GONE);
                    noOrdersTxt.setVisibility(View.VISIBLE);
//                    Utils.showPositiveDialog(this, "Message", getString(R.string.unable_to_fetch_menu_items), Constants.ActionMenuItemFailed);
                }
                if (Constants.MenuCategoryArrayList.size() <= 1) {
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
