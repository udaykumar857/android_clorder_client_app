package com.clorderclientapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.security.ProviderInstaller;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import io.realm.Realm;

import static com.clorderclientapp.activites.JohnniesPizzaScreenActivity.mGoogleApiClient;
import static com.facebook.FacebookSdk.getApplicationContext;


public class Utils {
    static ProgressDialog dialog;
    private static AlertDialog alertDialog;


    public static void startLoadingScreen(Context mContext) {
        if (dialog == null) {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("Loading");
            dialog.setCancelable(false);
            dialog.setInverseBackgroundForced(false);
            dialog.show();
        }

    }

    public static void cancelLoadingScreen() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public static int getClientId(Context mContext) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("ClientID", Constants.clientId);
    }

    public static void toastDisplay(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void showPositiveDialog(final Context mContext,
                                          final String title,
                                          final String message,
                                          final int actionType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton(mContext.getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog = null;
                dialog.dismiss();
                UserActionInterface listener;
                listener = (UserActionInterface) mContext;
                listener.userAction(actionType);
            }
        });

        if (alertDialog == null) {
            alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
    }


    public static void showDialog(final Context mContext,
                                  final String title,
                                  final String message,
                                  final int actionType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton(mContext.getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog = null;
                dialog.dismiss();
                UserActionInterface listener;
                listener = (UserActionInterface) mContext;
                listener.userAction(actionType);
            }
        });
        dialogBuilder.setNegativeButton(mContext.getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog = null;
                dialog.dismiss();
            }
        });
        if (alertDialog == null) {
            alertDialog = dialogBuilder.create();
            alertDialog.show();
        }

    }


    public static void showScheduleOrderDialog(final Context mContext,
                                               final String title,
                                               final String message,
                                               final int actionType, final int actionType1) {
        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "Lora-Regular.ttf");
        Typeface fontBold = Typeface.createFromAsset(mContext.getAssets(), "Lora-Bold.ttf");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);


        dialogBuilder.setCancelable(false);


        dialogBuilder.setPositiveButton(mContext.getString(R.string.confirm_order), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog = null;
                dialog.dismiss();
                UserActionInterface listener;
                listener = (UserActionInterface) mContext;
                listener.userAction(actionType);
            }
        });


        dialogBuilder.setNegativeButton(mContext.getString(R.string.change_order), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog = null;
                dialog.dismiss();
                UserActionInterface listener;
                listener = (UserActionInterface) mContext;
                listener.userAction(actionType1);
            }
        });
        if (alertDialog == null) {
            alertDialog = dialogBuilder.create();
            alertDialog.show();
            TextView msgTxt = alertDialog.findViewById(android.R.id.message);
            TextView titleTxt = alertDialog.findViewById(android.support.v7.appcompat.R.id.alertTitle);
            msgTxt.setTypeface(font);
            titleTxt.setTypeface(fontBold);
            Button pstBtn = alertDialog.findViewById(android.R.id.button1);
            pstBtn.setTypeface(fontBold);
            Button ngBtn = alertDialog.findViewById(android.R.id.button2);
            ngBtn.setTypeface(fontBold);
        }

    }

    public static void showNoLocationDialog(final Context mContext) {
        LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        if(!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
            dialogBuilder.setTitle(mContext.getString(R.string.location_not_enabled_title));
            dialogBuilder.setMessage(mContext.getString(R.string.location_not_enabled_message));
            dialogBuilder.setPositiveButton(mContext.getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog = null;
                    dialog.dismiss();
                    mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            dialogBuilder.setCancelable(false);
            if (alertDialog == null) {
                alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        }
    }


    public static void showMissingPermissionDialog(final Context mContext,
                                                   final Fragment mFragment,
                                                   final String title,
                                                   final String message,
                                                   final Integer actionType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton(mContext.getString(R.string.open_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        alertDialog = null;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                        mContext.startActivity(intent);
                    }
                });
        dialogBuilder.setNegativeButton(mContext.getString(R.string.alert_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                alertDialog = null;
            }
        });
        if (alertDialog == null) {
            alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
    }


    public static void logout(Context mContext) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);
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

    private static void signOutGoogle() {

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });
    }

    private static void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
    }


    public static boolean isNetworkAvailable(final Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) {
                return info.isConnected();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean domainValid(String domainString) {
        boolean isDomain = false;
        String domainList[] = {"com", "co.in", "net", "org", "edu", "co.nz"};
        for (int i = 0; i < domainList.length; i++) {
            if (domainString.equals(domainList[i])) {
                isDomain = true;
                break;
            }

        }
        return isDomain;
    }

    public static float roundUpFloatValue(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        String Value = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).toPlainString();
        Float roundOfValue = Float.parseFloat(Value);
        Log.d("roundUpValue ", "" + roundOfValue);
        return roundOfValue;
    }


    public static String roundFloatString(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        String value = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).toPlainString();
        Log.d("Value ", "" + value);
        return value;
    }


    public static ArrayList<String> getCreditCardTypeList() {
        ArrayList<String> listOfPattern = new ArrayList<String>();
        String ptVisa = "^4[0-9]{6,}$";
        listOfPattern.add(ptVisa);
        String ptMasterCard = "^5[1-5][0-9]{5,}$";
        listOfPattern.add(ptMasterCard);
        String ptAmeExp = "^3[47][0-9]{5,}$";
        listOfPattern.add(ptAmeExp);
        String ptDinClb = "^3(?:0[0-5]|[68][0-9])[0-9]{4,}$";
        listOfPattern.add(ptDinClb);
        String ptDiscover = "^6(?:011|5[0-9]{2})[0-9]{3,}$";
        listOfPattern.add(ptDiscover);
        String ptJcb = "^(?:2131|1800|35[0-9]{3})[0-9]{3,}$";
        listOfPattern.add(ptJcb);
        return listOfPattern;
    }

    public static boolean isCardValid(String cardNumber) {
        ArrayList<String> cardList = getCreditCardTypeList();
        for (int i = 0; i < cardList.size(); i++) {
            if (cardNumber.matches(cardList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public static void hideKeyboard(Activity activity, View v) {
        InputMethodManager imm2 = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm2 != null) {
            imm2.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static void initializeSSLContext(Context mContext) {
        try {
            SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            ProviderInstaller.installIfNeeded(mContext.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public static void resetOrderTimings() {
        Constants.selectedTime = null;
        Constants.selectedDate = null;
    }

    public static boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,15}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private static int checkCreditCardType(String cardNumberText) {
        int cardType = 0;
        if (cardNumberText.length() >= 13 && cardNumberText.length() <= 16) {
            if (cardType == 0) {
                int prefix = Integer.parseInt(cardNumberText.substring(0, 2));

                switch (prefix) {
                    case 34://americanExpress
                    case 37://americanExpress
                        cardType = 1;
                        Log.d("cardType", "americanExpress");
                        break;
                    case 36://Diners Club
                        cardType = 16;
                        Log.d("cardType", "Diners Club");
                        break;
                    case 38://Carte Blanche
                        cardType = 8;
                        Log.d("cardType", "Carte Blanche");
                        break;

                    case 51://Master Card
                    case 52://Master Card
                    case 53://Master Card
                    case 54://Master Card
                    case 55://Master Card
                        cardType = 4;
                        Log.d("cardType", "Master Card");
                        break;

                    default:
                        cardType = 0;
                        break;
                }

                if (cardType == 0) {
                    int prefix1 = Integer.parseInt(cardNumberText.substring(0, 3));
                    switch (prefix1) {
                        case 300://American Diners Club
                        case 301://American Diners Club
                        case 302://American Diners Club
                        case 303://American Diners Club
                        case 304://American Diners Club
                        case 305://American Diners Club
                            cardType = 16;
                            Log.d("cardType", "American Diners Club");
                            break;

                        case 603://Maestro
                        case 630://Maestro
                            cardType = 0;
                            Log.d("cardType", "Maestro");
                            break;
                        default:
                            cardType = 0;
                            break;
                    }
                }

                if (cardType == 0) {
                    int prefix2 = Integer.parseInt(cardNumberText.substring(0, 4));
                    switch (prefix2) {
                        case 6011://Discover
                            cardType = 2;
                            Log.d("cardType", "Discover");
                            break;

                        case 2014://EnRoute
                        case 2149://EnRoute
                            cardType = 8;
                            Log.d("cardType", "EnRoute");
                            break;

                        case 2131://JCB
                        case 1800://JCB
                            cardType = 32;
                            Log.d("cardType", "JCB");
                            break;

                        default:
                            cardType = 0;
                            break;
                    }
                }

                if (cardType == 0) {
                    int prefix3 = Integer.parseInt(cardNumberText.substring(0, 1));
                    switch (prefix3) {
                        case 3://JCB
                            cardType = 32;
                            Log.d("cardType", "JCB");
                            break;

                        case 4://Visa
                            cardType = 8;
                            Log.d("cardType", "Visa");
                            break;

                        default:
                            cardType = 0;
                            break;
                    }
                }
            }

        }
        return cardType;
    }


    public static boolean isValidType(String cardNumber) {
        if (checkCreditCardType(cardNumber) != 0) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean Check(String ccNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }


    public static boolean isValid(long number) {

        int total = sumOfDoubleEvenPlace(number) + sumOfOddPlace(number);

        return (total % 10 == 0) && (prefixMatched(number, 1) == true) &&
                (getSize(number) >= 13) && (getSize(number) <= 16);
    }

    public static int sumOfDoubleEvenPlace(long number) {

        int doubledevensum = 0;
        long place = 0;

        while (number > 0) {
            place = number % 100;
            doubledevensum += getDigit((int) (place / 10) * 2);
            number = number / 100;
        }
        return doubledevensum;
    }

    public static int sumOfOddPlace(long number) {

        int oddsum = 0;

        while (number <= 9) {
            oddsum += (int) (number % 10);
            number = number % 100;
        }
        return oddsum;
    }

    public static int getDigit(int number) {
        if (number <= 9) {
            return number;
        } else {
            int firstDigit = number % 10;
            int secondDigit = (int) (number / 10);
            return firstDigit + secondDigit;
        }
    }

    public static boolean prefixMatched(long number, int d) {

        if ((getPrefix(number, d) == 4)
                || (getPrefix(number, d) == 5)
                || (getPrefix(number, d) == 3)) {

            if (getPrefix(number, d) == 3) {
                System.out.println("\nVisa Card ");
            } else if (getPrefix(number, d) == 5) {
                System.out.println("\nMaster Card ");
            } else if (getPrefix(number, d) == 3) {
                System.out.println("\nAmerican Express Card ");
            }

            return true;

        } else {

            return false;

        }
    }

    public static int getSize(long d) {

        int count = 0;

        while (d > 0) {
            d = d / 10;
            count++;
        }
        return count;
    }

    public static long getPrefix(long number, int k) {
        if (getSize(number) < k) {
            return number;
        } else {
            int size = (int) getSize(number);

            for (int i = 0; i < (size - k); i++) {
                number = number / 10;
            }
            return number;
        }
    }

    public static Bitmap drawMultilineTextToBitmap(Context gContext,
                                                   int gResId,
                                                   String gText) {

        // prepare canvas
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Log.d("scale", "" + scale);
        Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);

        // new antialiased Paint
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(255, 255, 255));
        // text size in pixels
        paint.setTextSize((int) (16 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // set text width to canvas width minus 16dp padding
        int textWidth = canvas.getWidth() - (int) (16 * scale);

        // init StaticLayout for text
        StaticLayout textLayout = new StaticLayout(
                gText, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // get height of multiline text
        int textHeight = textLayout.getHeight();

        // get position of text's top left corner
        float x = (bitmap.getWidth() - textWidth) / 2;
        float y = (bitmap.getHeight() - textHeight) / 2;
        Rect bounds = new Rect();
        // draw text to the Canvas center
        canvas.save();
        canvas.translate(x, bounds.top + 10);
        textLayout.draw(canvas);
        canvas.restore();

        return bitmap;
    }

    public boolean isDayLightSavings(Date isDayLightDate) {
        boolean isDaylight = false;
        String timezoneList[] = {"America/Santa_Isabel", "America/Los_Angeles", "America/Ensenada", "America/Dawson", "America/Tijuana",
                "America/Vancouver", "America/Whitehorse", "Canada/Pacific", "Canada/Saskatchewan", "Canada/Yukon", "Etc/GMT+8",
                "Mexico/BajaNorte", "Pacific/Pitcairn", "PST8PDT", "US/Pacific", "US/Pacific-New"};
        for (int i = 0; i < timezoneList.length; i++) {
            TimeZone timeZone = TimeZone.getTimeZone(timezoneList[i]);
            boolean isDay = timeZone.inDaylightTime(new Date("2016-12-08 9:45 AM"));
            if (isDay) {
                isDaylight = true;
            }
            Log.d(timezoneList[i], "" + isDay);
        }
        return isDaylight;
    }


//    America/Santa_Isabel
//    America/Los_Angeles
//    America/Ensenada
//    America/Dawson
//    America/Tijuana
//    America/Vancouver
//    America/Whitehorse
//    Canada/Pacific
//    Canada/Saskatchewan
//    Canada/Yukon
//    Etc/GMT+8
//    Mexico/BajaNorte
//    Pacific/Pitcairn
//    PST8PDT
//    US/Pacific
//    US/Pacific-New

//    TimeZone timeZone = TimeZone.getTimeZone("America/Dawson");
//    Calendar calendar = Calendar.getInstance();
//calendar.setTimeZone(timeZone);
//calendar.set(Calendar.MONTH, Calendar.MARCH);
//calendar.set(Calendar.DAY_OF_MONTH, 13);
//    SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
//sourceFormat.setTimeZone(TimeZone.getTimeZone("America/Dawson"));
//System.out.println(sourceFormat.format(calendar.getTimeInMillis()));
//System.out.println("isDayLightSaving"+"\t"+timeZone.inDaylightTime(calendar.getTime()));


}
