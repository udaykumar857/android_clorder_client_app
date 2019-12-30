package com.clorderclientapp.activites;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class OrderTimingsActivity extends AppCompatActivity implements ResponseHandler, UserActionInterface, View.OnClickListener {
    private Spinner orderTimeSpinner;
    private TextView orderDateTxt;
    private ArrayList<String> orderDateList, orderTimeList;
    private HttpRequest httpRequest;
    private ArrayAdapter orderTimeAdapter;
    private boolean isDeliveryOrder = true;
    private SharedPreferences sharedPreferences;
    private JSONArray restaurantTimingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_timings);
        initViews();
        listeners();
        orderDateList = new ArrayList<>();
        orderTimeList = new ArrayList<>();
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);

        isDeliveryOrder = sharedPreferences.getBoolean("isDeliveryOrder", true);

//        ArrayAdapter orderDateAdapter = new ArrayAdapter<String>(this,
//                R.layout.spinner_text_view, orderDateList);
//        orderDateSpinner.setAdapter(orderDateAdapter);


        orderTimeAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_text_view, orderTimeList);
        orderTimeSpinner.setAdapter(orderTimeAdapter);

        if (Utils.isNetworkAvailable(this)) {
            getRestaurantHoursRequest();
        } else {
            Utils.showPositiveDialog(this, getString(R.string.message_txt), getString(R.string.request_fail_msg), Constants.ActionNetworkFailed);
        }

    }

    private void initViews() {
        orderDateTxt = (TextView) findViewById(R.id.order_date_txt);
//        orderTimeSpinner = (Spinner) findViewById(R.id.order_time_txt);

    }

    private void listeners() {
        orderDateTxt.setOnClickListener(this);
    }

    private void getRestaurantHoursRequest() {
//        {
//            "clientId": 5
//        }
        Utils.startLoadingScreen(this);
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("clientId", Utils.getClientId(this));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("FetchClientSettingReq", requestObject.toString());
        httpRequest.fetchClientSettings(this, requestObject, Constants.FetchClientSettings);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order_date_txt:
                datePick();
                break;
        }
    }


    @Override
    public void responseHandler(Object response, int requestType) {
        switch (requestType) {

            case Constants.FetchClientSettings:
                Utils.cancelLoadingScreen();
//            {
//                "RestaurentTimings": [
//                {
//                    "Day": 1,
//                        "LunchStartTime": "11:30:00",
//                        "LunchEndTime": "18:00:00",
//                        "DinnerStartTime": "18:30:00",
//                        "DinnerEndTime": "22:00:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "21:50:00",
//                        "LunchDeliveryStartTime": "11:30:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "21:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "00:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "21:50:00",
//                        "Closed": false,
//                        "DeliveryClosed": true,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 2,
//                        "LunchStartTime": "11:00:00",
//                        "LunchEndTime": "15:00:00",
//                        "DinnerStartTime": "17:00:00",
//                        "DinnerEndTime": "22:00:00",
//                        "LunchEarlyEndTime": "15:00:00",
//                        "DinnerEarlyEndTime": "21:50:00",
//                        "LunchDeliveryStartTime": "11:00:00",
//                        "LunchDeliveryEndTime": "15:00:00",
//                        "DinnerDeliveryStartTime": "17:00:00",
//                        "DinnerDeliveryEndTime": "21:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "00:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "21:50:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 3,
//                        "LunchStartTime": "11:30:00",
//                        "LunchEndTime": "14:30:00",
//                        "DinnerStartTime": "17:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "14:00:00",
//                        "DinnerEarlyEndTime": "23:50:00",
//                        "LunchDeliveryStartTime": "11:30:00",
//                        "LunchDeliveryEndTime": "14:15:00",
//                        "DinnerDeliveryStartTime": "17:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "01:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:50:00",
//                        "Closed": true,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 4,
//                        "LunchStartTime": "11:30:00",
//                        "LunchEndTime": "00:00:00",
//                        "DinnerStartTime": "00:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "23:59:00",
//                        "LunchDeliveryStartTime": "11:30:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "02:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:59:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 5,
//                        "LunchStartTime": "11:30:00",
//                        "LunchEndTime": "00:00:00",
//                        "DinnerStartTime": "00:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "23:59:00",
//                        "LunchDeliveryStartTime": "11:30:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "03:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:59:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 6,
//                        "LunchStartTime": "12:00:00",
//                        "LunchEndTime": "00:00:00",
//                        "DinnerStartTime": "00:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "23:59:00",
//                        "LunchDeliveryStartTime": "12:00:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "04:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:59:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                },
//                {
//                    "Day": 0,
//                        "LunchStartTime": "12:00:00",
//                        "LunchEndTime": "00:00:00",
//                        "DinnerStartTime": "00:00:00",
//                        "DinnerEndTime": "23:59:00",
//                        "LunchEarlyEndTime": "00:00:00",
//                        "DinnerEarlyEndTime": "23:59:00",
//                        "LunchDeliveryStartTime": "12:00:00",
//                        "LunchDeliveryEndTime": "00:00:00",
//                        "DinnerDeliveryStartTime": "00:00:00",
//                        "DinnerDeliveryEndTime": "23:45:00",
//                        "BreakfastStartTime": "00:00:00",
//                        "BreakfastEndTime": "05:00:00",
//                        "BreakfastDeliveryStartTime": "00:00:00",
//                        "BreakfastDeliveryEndTime": "00:00:00",
//                        "ReservationCloseTime": "00:00:00",
//                        "ReservationEndTime": "23:59:00",
//                        "Closed": false,
//                        "DeliveryClosed": false,
//                        "ReservationClosed": false,
//                        "MorningOrderEndTime": "00:00:00",
//                        "NightOrderEndTime": "00:00:00",
//                        "DeliveryMorningOrderEndTime": "00:00:00",
//                        "DeliveryNightOrderEndTime": "00:00:00"
//                }
//                ],
//                "clientId": 5,
//                    "status": "The GetRestaurentHours call is successful",
//                    "isSuccess": true
//            }
                if (response != null) {
                    JSONObject responseObject = (JSONObject) response;
                    try {
                        if (responseObject.getBoolean("isSuccess")) {
                            restaurantTimingsList = responseObject.getJSONObject("ClientSettings").getJSONArray("BusinessHours");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeZone(TimeZone.getTimeZone("GMT-5"));
                            int day = calendar.get(Calendar.DAY_OF_WEEK);
                            long timeGmt = calendar.getTimeInMillis();
                            Log.d("dayOfWeek", "" + day);//Sunday=1,Monday=2,Tueday=3Wednesday=4,Thursday=5,Friday=6,Saturday=7;
                            Log.d("Gmt-5", "" + timeGmt);//US/Eastern Time Zone ---- Richmond, VA 23219, USA
                            orderDateTxt.setText(getString(R.string.today_txt));
                            scheduleOrder(day - 1);
                        } else {
                            Utils.showPositiveDialog(this, getString(R.string.message_txt), responseObject.getString("status"), Constants.ActionRestaurantHours);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    private void scheduleOrder(int day) {
        try {
            for (int i = 0; i < restaurantTimingsList.length(); i++)
                // In Api "Day": 0-Sunday, "Day": 1-Monday, "Day": 2-Tuesday, "Day": 3-Wednesday, "Day": 4-Thursday, "Day": 5-Friday, "Day": 6-Saturday,
                if (day == restaurantTimingsList.getJSONObject(i).getInt("Day")) {
                    timingsUpdate(restaurantTimingsList.getJSONObject(i));
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void timingsUpdate(JSONObject updateObject) {
//            {
//                "Day": 1,
//                    "LunchStartTime": "11:30:00",
//                    "LunchEndTime": "18:00:00",
//                    "DinnerStartTime": "18:30:00",
//                    "DinnerEndTime": "22:00:00",
//                    "LunchEarlyEndTime": "00:00:00",
//                    "DinnerEarlyEndTime": "21:50:00",
//                    "LunchDeliveryStartTime": "11:30:00",
//                    "LunchDeliveryEndTime": "00:00:00",
//                    "DinnerDeliveryStartTime": "00:00:00",
//                    "DinnerDeliveryEndTime": "21:45:00",
//                    "BreakfastStartTime": "00:00:00",
//                    "BreakfastEndTime": "00:00:00",
//                    "BreakfastDeliveryStartTime": "00:00:00",
//                    "BreakfastDeliveryEndTime": "00:00:00",
//                    "ReservationCloseTime": "00:00:00",
//                    "ReservationEndTime": "21:50:00",
//                    "Closed": false,
//                    "DeliveryClosed": true,
//                    "ReservationClosed": false,
//                    "MorningOrderEndTime": "00:00:00",
//                    "NightOrderEndTime": "00:00:00",
//                    "DeliveryMorningOrderEndTime": "00:00:00",
//                    "DeliveryNightOrderEndTime": "00:00:00"
//            }
        try {
            if (isDeliveryOrder) {
                if (updateObject.getBoolean("DeliveryClosed")) {
                    orderTimeList.clear();
                    orderTimeList.add(getString(R.string.closed_txt));
                    orderTimeAdapter.notifyDataSetChanged();
                    orderTimeSpinner.setSelection(0);
                } else {
                    orderTimeList.clear();
                    orderTimeList.add(timeFormat(updateObject.getString("BreakfastDeliveryStartTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("BreakfastDeliveryEndTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("LunchDeliveryStartTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("LunchDeliveryEndTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("DinnerDeliveryStartTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("DinnerDeliveryEndTime")));
                    orderTimeAdapter.notifyDataSetChanged();
                    orderTimeSpinner.setSelection(0);
                }
            } else {
                if (updateObject.getBoolean("Closed")) {
                    orderTimeList.clear();
                    orderTimeList.add(getString(R.string.closed_txt));
                    orderTimeAdapter.notifyDataSetChanged();
                    orderTimeSpinner.setSelection(0);
                } else {
                    orderTimeList.clear();
                    if ((!updateObject.getString("DinnerStartTime").equals("00:00:00")) ||
                            (!updateObject.getString("DinnerEndTime").equals("00:00:00"))) {
                        orderTimeList.add(timeFormat(updateObject.getString("BreakfastStartTime")));
                        String startTime = timeFormat(updateObject.getString("DinnerStartTime"));
                        String endTime = timeFormat(updateObject.getString("DinnerEndTime"));

                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        df.setTimeZone(TimeZone.getTimeZone("GMT-5"));
                        String timeUtils = df.format(new Date()) + " " + updateObject.getString("DinnerStartTime");
                        Log.d("TimeUtils", "" + timeUtils);
                        Date d1 = null;
                        try {
                            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
                            df1.setTimeZone(TimeZone.getTimeZone("GMT-5"));
                            d1 = df1.parse(timeUtils);
                            Log.d("d1", "" + d1.getTime());
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(d1);
                            cal.setTimeZone(TimeZone.getTimeZone("GMT-5"));
                            cal.add(Calendar.MINUTE, 15);
                            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm a", Locale.getDefault());
                            df2.setTimeZone(TimeZone.getTimeZone("GMT-5"));
                            String newTime = df2.format(cal.getTime());

//                            String newTime = df.format(cal.getTime());
                            Log.d("New Time", "" + newTime);

//                        orderTimeList.add();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }


                    orderTimeList.add(timeFormat(updateObject.getString("BreakfastStartTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("BreakfastEndTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("LunchStartTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("LunchEndTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("DinnerStartTime")));
                    orderTimeList.add(timeFormat(updateObject.getString("DinnerEndTime")));
                    orderTimeAdapter.notifyDataSetChanged();
                    orderTimeSpinner.setSelection(0);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void datePick() {
        final String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Calendar cal1 = Calendar.getInstance();
        String cal[] = currentDate.split("-");
        Log.d("CurrentDatePicker", currentDate);
        int year = Integer.parseInt(cal[0]);
        int month = Integer.parseInt(cal[1]) - 1;
        int date = Integer.parseInt(cal[2]);
        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.set(year, monthOfYear, dayOfMonth);
//                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                Log.d("selectedDate", "" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + "-" + DateUtils.formatDateRange(OrderTimingsActivity.this, calendar.getTimeInMillis(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY));

                String weekDay = DateUtils.formatDateRange(OrderTimingsActivity.this, calendar.getTimeInMillis(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY);

                String userSelectedDate = new SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(new Date(calendar.getTimeInMillis()));
                Log.d("Formated Date", "" + userSelectedDate);
                int day = 0;
                switch (weekDay) {
                    case "Sunday":
                        day = 0;
                        break;
                    case "Monday":
                        day = 1;
                        break;
                    case "Tuesday":
                        day = 2;
                        break;
                    case "Wednesday":
                        day = 3;
                        break;
                    case "Thursday":
                        day = 4;
                        break;
                    case "Friday":
                        day = 5;
                        break;
                    case "Saturday":
                        day = 6;
                        break;
                }
                String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

                if (selectedDate.equals(currentDate)) {
                    orderDateTxt.setText(getString(R.string.today_txt));
                    scheduleOrder(day);
                } else {
                    orderDateTxt.setText(String.format("%s", userSelectedDate));
                    scheduleOrder(day);
                }

            }
        }, year, month, date);
        cal1.add(Calendar.DATE, 16);
        dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.getDatePicker().setMaxDate(cal1.getTimeInMillis());
        dpd.show();
    }


    private String timeFormat(String time) {
        String updateTimings = "";
        String timing[] = time.split(":");
        String period;
        Integer hours = Integer.valueOf(timing[0]);
        if (hours > 12) {
            period = "PM";
            hours = hours - 12;
        } else if (hours == 12) {
            period = "PM";
        } else if (hours == 0) {
            hours = 12;
            period = "AM";
        } else {
            period = "AM";
        }
        updateTimings = ((hours < 10) ? ("0" + hours) : hours) + ":" + timing[1] + " " + period;
        return updateTimings;
    }

    @Override
    public void userAction(int actionType) {

    }


}
