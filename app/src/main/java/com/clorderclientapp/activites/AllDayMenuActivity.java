package com.clorderclientapp.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.clorderclientapp.adapters.AllDayMenuAdapter;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.adapters.CategoryAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.MenuModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Converter;
import com.clorderclientapp.utils.RecyclerViewClickListener;
import com.clorderclientapp.utils.Utils;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;

import static com.clorderclientapp.activites.JohnniesPizzaScreenActivity.mGoogleApiClient;

public class AllDayMenuActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {

    RecyclerView allDayMenuRecyclerView;

    private AllDayMenuAdapter allDayMenuAdapter;
    private CategoryAdapter categoryAdapter;
    private ImageView backArrow, frontArrow;
    private HttpRequest httpRequest;
    private TextView menuNameTxt, checkOutTxt, itemCount;
    private int currentMenuIndex = 0;
    private int currentCategoryIndex = 0;
    private ImageView cartImg;
    private static final int GRID_COLUMNS_COUNT = 2;
    private LinearLayout cartLayout;
    private TextView subtotalValue;
    public DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private View actionBarCustomView;
    private ImageView mToolBarMenu, mToolBarCart;
    private View toolTop;
    private TextView editProfileTxt, userNameTxt, emailTxt, homeTxt, myOrdersTxt, changePasswordTxt, logoutTxt;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_all_day_menu_screen);
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        initViews();
        initActionBarViews();
        setupToolBar();
//        setupNavigationView();
//        if (Constants.MenuArrayList.size() == 1) {
//            backArrow.setVisibility(View.INVISIBLE);
//            frontArrow.setVisibility(View.INVISIBLE);
//        } else {
//            backArrow.setVisibility(View.INVISIBLE);
//        }
//
        Constants.MenuCategoryArrayList.clear();

        allDayMenuAdapter = new AllDayMenuAdapter(this, Constants.MenuCategoryArrayList);
        allDayMenuRecyclerView.setAdapter(allDayMenuAdapter);
        allDayMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));

//        allDayMenuRecyclerView.setHasFixedSize(true);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, GRID_COLUMNS_COUNT,
//                RecyclerView.VERTICAL, false);
//        categoryAdapter = new CategoryAdapter(this, Constants.MenuCategoryArrayList);
//        allDayMenuRecyclerView.setAdapter(categoryAdapter);
//        allDayMenuRecyclerView.setLayoutManager(gridLayoutManager);


        if (Utils.isNetworkAvailable(this)) {
            clientMenuAndCategoryRequest();
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
        }

        listeners();


    }

    private void initViews() {
        cartImg = (ImageView) findViewById(R.id.cartImg);
        allDayMenuRecyclerView = (RecyclerView) findViewById(R.id.all_day_menu_list);
        menuNameTxt = (TextView) findViewById(R.id.menu_name_txt);
        backArrow = (ImageView) findViewById(R.id.back_arrow);
        frontArrow = (ImageView) findViewById(R.id.front_arrow);
        subtotalValue = (TextView) findViewById(R.id.subtotal_text);
        checkOutTxt = (TextView) findViewById(R.id.check_out_text);
        cartLayout = findViewById(R.id.cart_layout);
        itemCount = findViewById(R.id.item_count);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.mainDrawer);
        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mNavigationView.setItemIconTintList(null);
        editProfileTxt = findViewById(R.id.editProfileTxt);
        userNameTxt = findViewById(R.id.userNameTxt);
        emailTxt = findViewById(R.id.emailTxt);
        homeTxt = findViewById(R.id.home_txt);
        myOrdersTxt = findViewById(R.id.my_orders_txt);
        changePasswordTxt = findViewById(R.id.change_password_txt);
        logoutTxt = findViewById(R.id.logout_txt);
        actionBarCustomView = LayoutInflater.from(this).inflate(R.layout.layout_toolbar, null, false);
    }


    private void initActionBarViews() {
        mToolBarMenu = (ImageView) actionBarCustomView.findViewById(R.id.mToolBarMenu);
        mToolBarCart = (ImageView) actionBarCustomView.findViewById(R.id.mToolBarCart);
        mToolBarMenu.setOnClickListener(this);
        mToolBarCart.setOnClickListener(this);
        editProfileTxt.setOnClickListener(this);
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

    private void setupNavigationView() {
        if (!Constants.isGuestUserLogin) {

            boolean isUserDetails = sharedPreferences.contains("userDetails");
            if (isUserDetails) {
                try {
                    JSONObject userCredentials = new JSONObject((String) sharedPreferences.getString("userCredentials", ""));
                    userNameTxt.setText(userCredentials.getString("FullName"));
                    emailTxt.setText(userCredentials.getString("Email"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            mToolBarMenu.setVisibility(View.INVISIBLE);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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

    private void listeners() {
        allDayMenuRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this,
                new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
//                        Constants.MenuCategoryArrayList.get(currentCategoryIndex).setExpanded(false);
//                        allDayMenuAdapter.notifyDataSetChanged();
//                        currentCategoryIndex = position;

                        //                        "startTime": "00:00",
//                         "endTime": "23:59",
                        String timingStart[] = Constants.MenuArrayList.get(currentMenuIndex).menuStartTime.split(":");
                        String timingEnd[] = Constants.MenuArrayList.get(currentMenuIndex).menuEndTime.split(":");
                        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]);
                        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);

                        int nowMinutes = 0;
                        if (Constants.selectedDate == null && Constants.selectedTime == null) {
                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                            Constants.selectedDate = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis());
                            Constants.selectedTime = "ASAP";
                        }
                        if (!Constants.selectedTime.equals("ASAP")) {
                            nowMinutes = timeInMinutes(Constants.selectedTime);
                        } else {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
                            String nowTime[] = (simpleDateFormat.format(Calendar.getInstance().getTimeInMillis())).split(":");
                            nowMinutes = Integer.parseInt(nowTime[0]) * 60 + Integer.parseInt(nowTime[1]);
                        }

                        if ((nowMinutes >= startMinutes) && (nowMinutes <= endMinutes)) {


//                            clientItemsRequest(position);
                            int categoryId = Constants.MenuCategoryArrayList.get(position).getCategoryId();
                            String categoryTitle = Constants.MenuCategoryArrayList.get(position).getCategoryTitle();
                            Intent menuItemIntent = new Intent(AllDayMenuActivity.this, MenuItemActivity.class);
                            menuItemIntent.putExtra("categoryId", categoryId);
                            menuItemIntent.putExtra("categoryTitle", categoryTitle);
                            menuItemIntent.putExtra("categoryPosition", position);
                            startActivity(menuItemIntent);
                        } else {
                            Utils.showPositiveDialog(AllDayMenuActivity.this, getString(R.string.message_txt), getString(R.string.menu_item_not_available), Constants.ActionMenuNotAvailable);
                        }

                    }
                }));
//        cartImg.setOnClickListener(this);
        frontArrow.setOnClickListener(this);
        backArrow.setOnClickListener(this);
//        checkOutTxt.setOnClickListener(this);
        homeTxt.setOnClickListener(this);
        myOrdersTxt.setOnClickListener(this);
        changePasswordTxt.setOnClickListener(this);
        logoutTxt.setOnClickListener(this);
        cartLayout.setOnClickListener(this);
    }

    private void clientMenuAndCategoryRequest() {
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Utils.getClientId(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.fetchMenuWithCategories(this, requestObject, Constants.FetchMenuWithCategories);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setupNavigationView();
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
    protected void onStop() {
        super.onStop();
        Realm.getDefaultInstance().close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.mToolBarMenu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.mToolBarCart:
                startActivity(new Intent(this, CartActivity.class));
                break;
            case R.id.cart_layout:
                startActivity(new Intent(this, CartActivity.class));
                break;
//            case R.id.back_arrow:
//                currentMenuIndex -= 1;
//
//                if (currentMenuIndex > 0) {
//                    frontArrow.setVisibility(View.VISIBLE);
//                    backArrow.setVisibility(View.VISIBLE);
//                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
//
//                } else if (currentMenuIndex == 0) {
//                    backArrow.setVisibility(View.INVISIBLE);
//                    frontArrow.setVisibility(View.VISIBLE);
//                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
//                }
//
//                break;
//            case R.id.front_arrow:
//                currentMenuIndex += 1;
//                if (currentMenuIndex < Constants.MenuArrayList.size()-1) {
//                    backArrow.setVisibility(View.VISIBLE);
//                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
//                } else if (currentMenuIndex == Constants.MenuArrayList.size() - 1) {
//                    frontArrow.setVisibility(View.INVISIBLE);
//                    backArrow.setVisibility(View.VISIBLE);
//                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
//                }
//                break;


            case R.id.back_arrow:
//                currentCategoryIndex = 0;
                currentMenuIndex -= 1;
                if (currentMenuIndex > 0) {
                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
                    categoryList(currentMenuIndex);

                } else if (currentMenuIndex == 0) {
                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
                    categoryList(currentMenuIndex);
                } else {
                    currentMenuIndex = Constants.MenuArrayList.size() - 1;
                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
                    categoryList(currentMenuIndex);
                }

                break;
            case R.id.front_arrow:
//                currentCategoryIndex = 0;
                currentMenuIndex += 1;
                if (currentMenuIndex < Constants.MenuArrayList.size() - 1) {
                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
                    categoryList(currentMenuIndex);
                } else if (currentMenuIndex == Constants.MenuArrayList.size() - 1) {
                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
                    categoryList(currentMenuIndex);
                } else {
                    currentMenuIndex = 0;
                    menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
//                    clientCategoriesRequest();
                    categoryList(currentMenuIndex);
                }
                break;

            case R.id.home_txt:
                mDrawerLayout.closeDrawers();
                startActivity(new Intent(this, JohnniesPizzaScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case R.id.editProfileTxt:
                mDrawerLayout.closeDrawers();
                startActivity(new Intent(this, EditProfileActivity.class));
                break;
            case R.id.my_orders_txt:
                mDrawerLayout.closeDrawers();
                Intent intent = new Intent(this, OrderHistoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.change_password_txt:
                mDrawerLayout.closeDrawers();
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;
            case R.id.logout_txt:
                mDrawerLayout.closeDrawers();
                Utils.showDialog(this, getResources().getString(R.string.logout_txt), getResources().getString(R.string.logout_msg), Constants.ActionLogout);
                break;
        }

    }

    private void categoryList(int position) {
        Constants.MenuCategoryArrayList.clear();
        Constants.MenuCategoryArrayList.addAll(Constants.MenuArrayList.get(position).getCategoryArrayList());
        allDayMenuAdapter.notifyDataSetChanged();
//        categoryAdapter.notifyDataSetChanged();
    }

    private int timeInMinutes(String time) {
//        12:00 AM
//        9:15 PM

        int minutes = 0;

        String timeSpilt[] = time.split(" ");
        if (timeSpilt[1].equals("PM") && (Integer.parseInt(timeSpilt[0].split(":")[0]) < 12)) {
            minutes = ((Integer.parseInt(timeSpilt[0].split(":")[0]) + 12) * 60) + Integer.parseInt(timeSpilt[0].split(":")[1]);

        } else if (timeSpilt[1].equals("PM") && (Integer.parseInt(timeSpilt[0].split(":")[0]) == 12)) {
            minutes = ((Integer.parseInt(timeSpilt[0].split(":")[0])) * 60) + Integer.parseInt(timeSpilt[0].split(":")[1]);

        } else if (timeSpilt[1].equals("AM") && (Integer.parseInt(timeSpilt[0].split(":")[0]) < 12)) {
            minutes = ((Integer.parseInt(timeSpilt[0].split(":")[0])) * 60) + Integer.parseInt(timeSpilt[0].split(":")[1]);
        } else if (timeSpilt[1].equals("AM") && (Integer.parseInt(timeSpilt[0].split(":")[0]) == 12)) {
            minutes = ((Integer.parseInt(timeSpilt[0].split(":")[0]) - 12) * 60) + Integer.parseInt(timeSpilt[0].split(":")[1]);
        }


        return minutes;
    }


    @Override
    public void responseHandler(Object response, int requestType) {

        switch (requestType) {
            case Constants.FetchMenuWithCategories:
                Utils.cancelLoadingScreen();
                if (response != null) {
                    Constants.MenuArrayList.clear();
                    Constants.MenuArrayList.addAll((ArrayList<MenuModel>) response);
                    if (Constants.MenuArrayList.size() > 0) {
                        menuNameTxt.setText(Constants.MenuArrayList.get(currentMenuIndex).getMenuTitle());
                        categoryList(currentMenuIndex);
                    }
                    if (Constants.MenuArrayList.size() == 1) {
                        backArrow.setVisibility(View.INVISIBLE);
                        frontArrow.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.unable_to_get_menus_msg), Constants.ActionMenuWithCategoriesFailed);
                }
                break;
//            case Constants.FetchClientItems:
//                Utils.cancelLoadingScreen();
//                if (response != null) {
//                    Constants.MenuCategoryArrayList.get(currentCategoryIndex).setExpanded(true);
//                    Constants.CategoryItemList.clear();
//                    Constants.CategoryItemList.addAll((ArrayList<CategoryItemModel>) response);
////                    allDayMenuAdapter.notifyDataSetChanged();
//                } else {
//                    Utils.showPositiveDialog(this, "Message", getString(R.string.unable_to_fetch_menu_items), Constants.ActionMenuItemFailed);
//                }
//                if (Constants.CategoryItemList.size() == 0) {
//                    backArrow.setVisibility(View.INVISIBLE);
//                    frontArrow.setVisibility(View.INVISIBLE);
//                }
//                break;
        }
    }

    private void clientItemsRequest(int currentCategoryIndex) {
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
    public void userAction(int actionType) {

        switch (actionType) {
            case Constants.ActionLogout:
                logoutClick();
                Intent intent = new Intent(this, JohnniesPizzaScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }

    }

//    @Override
//    public void onParentClick(int position) {
//        Constants.MenuCategoryArrayList.get(currentCategoryIndex).setExpanded(false);
////        allDayMenuAdapter.notifyDataSetChanged();
//        currentCategoryIndex = position;
//
//        //                        "startTime": "00:00",
////                         "endTime": "23:59",
//        String timingStart[] = Constants.MenuArrayList.get(currentMenuIndex).menuStartTime.split(":");
//        String timingEnd[] = Constants.MenuArrayList.get(currentMenuIndex).menuEndTime.split(":");
//        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]);
//        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);
//
//        int nowMinutes = 0;
//        if (Constants.selectedDate == null && Constants.selectedTime == null) {
//            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
//            Constants.selectedDate = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis());
//            Constants.selectedTime = "ASAP";
//        }
//        if (!Constants.selectedTime.equals("ASAP")) {
//            nowMinutes = timeInMinutes(Constants.selectedTime);
//        } else {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(Constants.timeZone));
//            String nowTime[] = (simpleDateFormat.format(Calendar.getInstance().getTimeInMillis())).split(":");
//            nowMinutes = Integer.parseInt(nowTime[0]) * 60 + Integer.parseInt(nowTime[1]);
//        }
//
//        if ((nowMinutes >= startMinutes) && (nowMinutes <= endMinutes)) {
//
//
//            clientItemsRequest(position);
////            int categoryId = Constants.MenuCategoryArrayList.get(position).getCategoryId();
////            String categoryTitle = Constants.MenuCategoryArrayList.get(position).getCategoryTitle();
////            Intent menuItemIntent = new Intent(AllDayMenuActivity.this, MenuItemActivity.class);
////            menuItemIntent.putExtra("categoryId", categoryId);
////            menuItemIntent.putExtra("categoryTitle", categoryTitle);
////            menuItemIntent.putExtra("categoryPosition", position);
////            startActivity(menuItemIntent);
//        } else {
//            Utils.showPositiveDialog(AllDayMenuActivity.this, getString(R.string.message_txt), getString(R.string.menu_item_not_available), Constants.ActionMenuNotAvailable);
//        }
//
//    }

//    @Override
//    public void onChildClick(int parentPosition, int childPosition) {
//        Intent itemIntent = new Intent(this, ItemSelectionActivity.class);
//        itemIntent.putExtra("position", childPosition);
//        itemIntent.putExtra("modifierSelection", 0);
//        startActivity(itemIntent);
//    }

    private void logoutClick() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userCredentials");
        editor.apply();
//                                0:clorderSignIn/SignUp
//                                1:Facebook Login
//                                2:Google Login

        int loginFrom = sharedPreferences.getInt("isFromLogin", 0);

        switch (loginFrom) {
            case 0:
                break;
            case 1:
                //Facebook signout
                FacebookSdk.sdkInitialize(getApplicationContext());
                LoginManager.getInstance().logOut();
                break;
            case 2:
                //Google Signout
                signOutGoogle();
                if (mGoogleApiClient.isConnected()) {
                    revokeAccess();
                }
                break;
        }
        Utils.resetOrderTimings();

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
            SharedPreferences.Editor editor1 = sharedPreferences.edit();
            editor1.remove("userDetails");
            editor1.apply();
        }
    }

    private void signOutGoogle() {

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
    }


}