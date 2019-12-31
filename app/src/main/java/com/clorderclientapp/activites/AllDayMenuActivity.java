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
import com.clorderclientapp.adapters.AllDayMenuAdapter;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.MenuModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.RecyclerViewClickListener;
import com.clorderclientapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;

public class AllDayMenuActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {

    RecyclerView allDayMenuRecyclerView;

    private AllDayMenuAdapter allDayMenuAdapter;
    private ImageView backArrow, frontArrow;
    private HttpRequest httpRequest;
    private TextView menuNameTxt, checkOutTxt;
    private int currentMenuIndex = 0;

    TextView subtotalValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_all_day_menu_screen);
        httpRequest = new HttpRequest();
        initViews();


//        if (Constants.MenuArrayList.size() == 1) {
//            backArrow.setVisibility(View.INVISIBLE);
//            frontArrow.setVisibility(View.INVISIBLE);
//        } else {
//            backArrow.setVisibility(View.INVISIBLE);
//        }
        Constants.MenuCategoryArrayList.clear();
        allDayMenuAdapter = new AllDayMenuAdapter(this, Constants.MenuCategoryArrayList);
        allDayMenuRecyclerView.setAdapter(allDayMenuAdapter);
        allDayMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (Utils.isNetworkAvailable(this)) {
            clientMenuAndCategoryRequest();
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
        }

        listeners();


    }

    private void initViews() {
        allDayMenuRecyclerView = (RecyclerView) findViewById(R.id.all_day_menu_list);
        menuNameTxt = (TextView) findViewById(R.id.menu_name_txt);
        backArrow = (ImageView) findViewById(R.id.back_arrow);
        frontArrow = (ImageView) findViewById(R.id.front_arrow);
        subtotalValue = (TextView) findViewById(R.id.subtotal_text);
        checkOutTxt = (TextView) findViewById(R.id.check_out_text);
    }

    private void listeners() {
        allDayMenuRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this,
                new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
//                        "startTime": "00:00",
//                         "endTime": "23:59",
                        String timingStart[] = Constants.MenuArrayList.get(currentMenuIndex).menuStartTime.split(":");
                        String timingEnd[] = Constants.MenuArrayList.get(currentMenuIndex).menuEndTime.split(":");
                        int startMinutes = Integer.parseInt(timingStart[0]) * 60 + Integer.parseInt(timingStart[1]);
                        int endMinutes = Integer.parseInt(timingEnd[0]) * 60 + Integer.parseInt(timingEnd[1]);

                        int nowMinutes = 0;
                        if(Constants.selectedDate==null && Constants.selectedTime==null){
                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            simpleDateFormat1.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                            Constants.selectedDate = simpleDateFormat1.format(Calendar.getInstance().getTimeInMillis());
                            Constants.selectedTime = "ASAP";
                        }
                        if (!Constants.selectedTime.equals("ASAP")) {
                            nowMinutes = timeInMinutes(Constants.selectedTime);
                        } else {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8"));
                            String nowTime[] = (simpleDateFormat.format(Calendar.getInstance().getTimeInMillis())).split(":");
                            nowMinutes = Integer.parseInt(nowTime[0]) * 60 + Integer.parseInt(nowTime[1]);
                        }
                        
                        if ((nowMinutes >= startMinutes) && (nowMinutes <= endMinutes)) {
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
        frontArrow.setOnClickListener(this);
        backArrow.setOnClickListener(this);
        checkOutTxt.setOnClickListener(this);
    }

    private void clientMenuAndCategoryRequest() {
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Constants.clientId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpRequest.fetchMenuWithCategories(this, requestObject, Constants.FetchMenuWithCategories);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.check_out_text:
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
        }

    }

    private void categoryList(int position) {
        Constants.MenuCategoryArrayList.clear();
        Constants.MenuCategoryArrayList.addAll(Constants.MenuArrayList.get(position).getCategoryArrayList());
        allDayMenuAdapter.notifyDataSetChanged();
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
                } else {
                    Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.unable_to_get_menus_msg), Constants.ActionMenuWithCategoriesFailed);
                }
                break;
        }

    }

    @Override
    public void userAction(int actionType) {

    }
}
