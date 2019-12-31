package com.clorderclientapp.activites;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.clorderclientapp.R;
import com.clorderclientapp.RealmModels.CartItemModel;
import com.clorderclientapp.RealmModels.CartModel;
import com.clorderclientapp.RealmModels.OptionsModifiersModel;
import com.clorderclientapp.httpClient.HttpRequest;
import com.clorderclientapp.interfaces.ResponseHandler;
import com.clorderclientapp.interfaces.UserActionInterface;
import com.clorderclientapp.modelClasses.ItemModifiersModel;
import com.clorderclientapp.utils.Constants;
import com.clorderclientapp.utils.Converter;
import com.clorderclientapp.utils.FontTextViewRegularClass;
import com.clorderclientapp.utils.Utils;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

public class ItemSelectionActivity extends AppCompatActivity implements View.OnClickListener, ResponseHandler, UserActionInterface {

    private ImageView specialBack, mToolBarBack;
    private int itemPosition, itemQty = 1, minQty = 1;
    private float totalItemCost = 0.0f;
    private HttpRequest httpRequest;
    private CartItemModel cartItemModel;
    private RealmList<ItemModifiersModel> itemModifiersList;
    private ImageView modifiersImage, minusImage, plusImage;
    private AlertDialog modifierDialog;
    private ImageView modifierCloseBtn;
    private Button doneBtn;
    private LinearLayout mainLayout, itemDetailsLayout, optionModifierLayout, addToOrderLayout;
    private TextView itemName;
    private TextView qtyNum, addOrderPrice, addToOrderTxt, itemPriceTxt, itemDetailTitleTxt, itemDetailDescTxt, itemDetailsTxt, makeSelectionTxt;
    private EditText specialInstructions;
    private float modifierTotalAmount = 0.0f;
    private int modifierSelection;
    private String itemPrice;
    private boolean itemsDetailsStatus = true;
    private String userSelectedModifierOptions = "";
    private ImageView cartImg;
    private Toolbar mToolbar;
    private View actionBarCustomView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_selection);
        httpRequest = new HttpRequest();
        itemPosition = getIntent().getIntExtra("position", 0);
        modifierSelection = getIntent().getIntExtra("modifierSelection", 0);
        initViews();
        listeners();
        initActionBarViews();
        setupToolBar();
        if (modifierSelection == 0) {
            itemModifiersList = new RealmList<>();
            itemQty = Constants.CategoryItemList.get(itemPosition).getItemMinQuantity();
            minQty = Constants.CategoryItemList.get(itemPosition).getItemMinQuantity();
            qtyNum.setText(String.valueOf(itemQty));
            itemPrice = (Constants.CategoryItemList.get(itemPosition).getItemPrice());
            itemDetailTitleTxt.setText(Constants.CategoryItemList.get(itemPosition).getItemTitle());
//            itemDetailDescTxt.setText(Constants.CategoryItemList.get(itemPosition).getItemDesc());
//            itemName.append(Constants.CategoryItemList.get(itemPosition).getItemTitle());
            setData(itemPrice);
            if (Utils.isNetworkAvailable(this)) {
                getModifiersForItemRequest();
            } else {
                Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionModifiersItemFailed);
            }
        } else {
            Realm realm = Realm.getDefaultInstance();
            cartItemModel = realm.where(CartModel.class).findFirst().getCartItemList().get(itemPosition);
            itemModifiersList = cartItemModel.getItemModifiersList();
            itemQty = cartItemModel.getItemOrderQuantity();
            minQty = cartItemModel.getItemMinQuantity();
//            itemName.append(cartItemModel.getItemTitle());
            qtyNum.setText(String.valueOf(itemQty));
            itemPrice = cartItemModel.getItemPrice();
            if (cartItemModel.getSpecialNotes() != null) {
                specialInstructions.setText(cartItemModel.getSpecialNotes());
            }
            itemDetailTitleTxt.setText(cartItemModel.getItemTitle());

            if (itemModifiersList.size() > 0) {
                optionModifierLayout.setVisibility(View.VISIBLE);
            }
            //Calculate the total Item price with modifiers when Cart item edit.
            getTotalModifiersAmount();
            setData(itemPrice);
            userSelectedModifierOptions = cartItemModel.getUserSelectedModifierOptions();
            if (userSelectedModifierOptions.length() > 0) {
                makeSelectionTxt.setText(userSelectedModifierOptions);
            }

            realm.close();
//            generateLayout();
        }
    }

    private void initViews() {
        cartImg = (ImageView) findViewById(R.id.cartImg);
        itemName = (TextView) findViewById(R.id.item_name);
//        specialBack = (ImageView) findViewById(R.id.special_back);
        modifiersImage = (ImageView) findViewById(R.id.modifiers_image);
        qtyNum = (TextView) findViewById(R.id.qty_num);
        minusImage = (ImageView) findViewById(R.id.minus_image);
        plusImage = (ImageView) findViewById(R.id.plus_image);
        addOrderPrice = (TextView) findViewById(R.id.add_order_price);
        addToOrderTxt = (TextView) findViewById(R.id.add_to_order_txt);
        addToOrderLayout = (LinearLayout) findViewById(R.id.add_to_order_layout);
        specialInstructions = (EditText) findViewById(R.id.special_instructions);
        itemPriceTxt = (TextView) findViewById(R.id.item_price);
        itemDetailsLayout = (LinearLayout) findViewById(R.id.item_details_layout);
        itemDetailTitleTxt = (TextView) findViewById(R.id.item_detail_title);
//        itemDetailDescTxt = (TextView) findViewById(R.id.item_detail_desc);
        itemDetailsTxt = (TextView) findViewById(R.id.item_details_txt);
        optionModifierLayout = (LinearLayout) findViewById(R.id.option_modifier_layout);
        makeSelectionTxt = (TextView) findViewById(R.id.make_selection_txt);
        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        actionBarCustomView = LayoutInflater.from(this).inflate(R.layout.layout_item_selection_toolbar, null, false);
    }

    private void initActionBarViews() {
        mToolBarBack = (ImageView) actionBarCustomView.findViewById(R.id.special_back);
        mToolBarBack.setOnClickListener(this);
    }

    private void listeners() {
//        cartImg.setOnClickListener(this);
//        specialBack.setOnClickListener(this);
        minusImage.setOnClickListener(this);
        modifiersImage.setOnClickListener(this);
        plusImage.setOnClickListener(this);
        addToOrderLayout.setOnClickListener(this);
        itemDetailsTxt.setOnClickListener(this);

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
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.cartImg:
                startActivity(new Intent(this, CartActivity.class));
                break;
            case R.id.special_back:
                onBackPressed();
                break;

            case R.id.item_details_txt:
//                if (itemsDetailsStatus) {
//                    itemDetailsLayout.setVisibility(View.VISIBLE);
//                    itemsDetailsStatus = false;
//                } else {
//                    itemDetailsLayout.setVisibility(View.GONE);
//                    itemsDetailsStatus = true;
//                }

                break;


            case R.id.modifiers_image:
                if (itemModifiersList.size() > 0) {
                    generateLayout();
                } else {
                    Utils.toastDisplay(this, getString(R.string.no_modifiers_add_msg));
                }

                break;

            case R.id.modifier_close_btn:
                modifierDialog.dismiss();
                break;

            case R.id.done_btn:
                modifierTotalAmount = 0.0f;
                getUserModifierData();
                getTotalModifiersAmount();
                modifierDialog.dismiss();
                setUserOptionsModifiers();
                if (userSelectedModifierOptions.length() > 0) {
                    makeSelectionTxt.setText(userSelectedModifierOptions);
                }
                setData(itemPrice);
                break;


            case R.id.minus_image:
                if (itemQty > minQty) {
                    itemQty--;
                } else {
                    itemQty = minQty;
                }
                qtyNum.setText(String.format("%s", itemQty));
                setData(itemPrice);
                break;

            case R.id.plus_image:
                itemQty++;
                qtyNum.setText(String.format("%s", itemQty));
                setData(itemPrice);
                break;

            case R.id.add_to_order_layout:
                Realm realm = Realm.getDefaultInstance();
                CartModel cartModel = realm.where(CartModel.class).findFirst();
                realm.beginTransaction();
                if (cartModel == null) {
                    cartModel = new CartModel();
                    cartModel.setCartId(1);
                } else {
                    cartModel.setCartId(cartModel.getCartId());
                }
                CartItemModel cartItemModel = null;

                if (modifierSelection == 0) {
                    cartItemModel = new CartItemModel();
                    cartItemModel.setCategoryId(Constants.CategoryItemList.get(itemPosition).getCategoryId());
                    cartItemModel.setItemDesc(Constants.CategoryItemList.get(itemPosition).getItemDesc());
                    cartItemModel.setItemId(Constants.CategoryItemList.get(itemPosition).getItemId());
                    cartItemModel.setItemTitle(Constants.CategoryItemList.get(itemPosition).getItemTitle());
                    cartItemModel.setItemMinQuantity(minQty);
                    cartItemModel.setItemOrderQuantity(itemQty);
                    cartItemModel.setItemPrice(Constants.CategoryItemList.get(itemPosition).getItemPrice());
                    cartItemModel.setTotalItemPrice(totalItemCost);
                    cartItemModel.setUserSelectedModifierOptions(setUserOptionsModifiers());


                    RealmList<CartItemModel> cartItemList;
                    if (cartModel.getCartItemList() == null) {
                        cartItemList = new RealmList<>();
                        cartItemModel.setSerialId(1);
                        for (int p = 0; p < itemModifiersList.size(); p++) {
                            itemModifiersList.get(p).setModifierSerialId(100 + p);
                            for (int k = 0; k < itemModifiersList.get(p).getOptionsModifiersList().size(); k++) {
                                //(id+(index*10000))
                                itemModifiersList.get(p).getOptionsModifiersList().get(k).setModifierSerialId(100 + p);
                                itemModifiersList.get(p).getOptionsModifiersList().get(k).setOptionSerialId(itemModifiersList.get(p).getOptionsModifiersList().get(k).getOptionId());
                            }
                        }
                        cartItemModel.setItemModifiersList(itemModifiersList);
                        cartItemList.add(cartItemModel);
                        cartModel.setCartItemList(cartItemList);
                    } else {
                        cartItemList = cartModel.getCartItemList();
                        int serialId = 1;
                        if (cartItemList.size() > 0) {
                            serialId = cartItemList.get(cartItemList.size() - 1).getSerialId() + 1;
                        }
                        cartItemModel.setSerialId(serialId);
                        for (int p = 0; p < itemModifiersList.size(); p++) {
                            itemModifiersList.get(p).setModifierSerialId((serialId * 1000) + p);
                            for (int k = 0; k < itemModifiersList.get(p).getOptionsModifiersList().size(); k++) {
                                itemModifiersList.get(p).getOptionsModifiersList().get(k).setModifierSerialId((serialId * 1000) + p);
                                itemModifiersList.get(p).getOptionsModifiersList().get(k).setOptionSerialId((serialId * itemModifiersList.get(p).getOptionsModifiersList().get(k).getOptionId()));
                            }
                        }

                        cartItemModel.setItemModifiersList(itemModifiersList);
                        cartItemList.add(cartItemModel);
                    }
                    for (CartItemModel model : cartItemList) {
                        Log.d("Item Model", model.toString());
                    }
                } else {
                    // Edit existing cart item
                    Log.d("CartModel", cartModel.toString());
                    RealmList<CartItemModel> cartItemList = cartModel.getCartItemList();
                    cartItemModel = cartItemList.get(itemPosition);
                    Log.d("Existing Item", cartItemModel.toString());
                    cartItemModel.setItemOrderQuantity(itemQty);
                    cartItemModel.setTotalItemPrice(totalItemCost);
                    cartItemModel.setSpecialNotes(specialInstructions.getText().toString());
                    cartItemModel.setUserSelectedModifierOptions(setUserOptionsModifiers());
//                    cartItemModel.setItemModifiersList(itemModifiersList);
                    cartItemList.set(itemPosition, cartItemModel);
                }
                realm.copyToRealmOrUpdate(cartModel);
                realm.commitTransaction();
                getSubTotalValue();
                onBackPressed();
//                startActivity(new Intent(this, AllDayMenuActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                finish();
                break;
        }
    }

    private void getModifiersForItemRequest() {
        if (Utils.isNetworkAvailable(this)) {
            Utils.startLoadingScreen(this);
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("clientId", Utils.getClientId(this));
                requestObject.put("ItemId", Constants.CategoryItemList.get(itemPosition).getItemId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpRequest.getModifiersForItem(this, requestObject, Constants.GetModifiersForItem);
        } else {
            Utils.showPositiveDialog(this, getString(R.string.alert_txt), getString(R.string.request_fail_msg), Constants.ActionItemModifierFailed);
        }
    }

    private void getSubTotalValue() {
        Realm realm = Realm.getDefaultInstance();
        CartModel cartModel = realm.where(CartModel.class).findFirst();
        if (cartModel != null) {
            float subtotalValue = 0.0f;
            realm.beginTransaction();
            for (int i = 0; i < cartModel.getCartItemList().size(); i++) {
                subtotalValue += cartModel.getCartItemList().get(i).getTotalItemPrice();
            }
            cartModel.setSubtotal(Utils.roundUpFloatValue(subtotalValue, 2));
            realm.copyToRealmOrUpdate(cartModel);
            realm.commitTransaction();
            realm.close();
        }

    }

    private void calculateModifiers() {
        modifierTotalAmount = 0.0f;
        getUserModifierData();
        getTotalModifiersAmount();
//        modifierDialog.dismiss();
        setUserOptionsModifiers();
        if (userSelectedModifierOptions.length() > 0) {
            makeSelectionTxt.setText(userSelectedModifierOptions);
        }
        setData(itemPrice);
    }

    @Override
    public void responseHandler(Object response, int requestType) {

        switch (requestType) {
            case Constants.GetModifiersForItem:
                Utils.cancelLoadingScreen();
                itemModifiersList.clear();
                itemModifiersList.addAll((RealmList<ItemModifiersModel>) response);
                if (itemModifiersList.size() > 0) {
                    optionModifierLayout.setVisibility(View.VISIBLE);
//                    generateLayout();
                }
                break;
        }
    }

    private void generateLayout() {

//            Checkbox = 1,
//            Radio = 2,
//            DropDown = 3,
//            CheckboxList = 4

        Typeface font = Typeface.createFromAsset(getAssets(), "Lora-Regular.ttf");
        Typeface fontBold = Typeface.createFromAsset(getAssets(), "Lora-Bold.ttf");
        modifierDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.item_selection_modifiers_dilog_layout, null, false);
        modifierCloseBtn = (ImageView) view.findViewById(R.id.modifier_close_btn);
        modifierCloseBtn.setOnClickListener(this);
        doneBtn = (Button) view.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(this);
        mainLayout = (LinearLayout) view.findViewById(R.id.modifiers_layout);
        for (int i = 0; i < itemModifiersList.size(); i++) {
            if (itemModifiersList.get(i).isModifierActive()) {
                LinearLayout optionsLayout = new LinearLayout(this);
                optionsLayout.setOrientation(LinearLayout.HORIZONTAL);
                optionsLayout.setPadding(5, 5, 5, 5);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.weight = 2f;
                lp.gravity = Gravity.CENTER;
                optionsLayout.setLayoutParams(lp);
                TextView title = new TextView(this);

                title.setText(itemModifiersList.get(i).getModifierName());
                title.setGravity(Gravity.CENTER);
                title.setTextColor(ContextCompat.getColor(this, R.color.salad_options_txt_col));
                title.setTypeface(fontBold);
                title.setTextSize(pixelsToSp(getResources().getDimension(R.dimen.price_custom_txt_size)));

                LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                titleLp.gravity = Gravity.CENTER;
                titleLp.weight = 0.6f;
                title.setLayoutParams(titleLp);

                optionsLayout.addView(title);
                LinearLayout optionsModifiersLayout = new LinearLayout(this);
                optionsModifiersLayout.setOrientation(LinearLayout.VERTICAL);
                optionsModifiersLayout.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams optionsModifiersLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                optionsModifiersLp.weight = 1.4f;
                optionsModifiersLp.gravity = Gravity.CENTER;
                optionsModifiersLayout.setLayoutParams(optionsModifiersLp);
                final RealmList<OptionsModifiersModel> optionsModifiersModelArrayList = itemModifiersList.get(i).getOptionsModifiersList();
                switch (itemModifiersList.get(i).getModifierType()) {
                    case 1:
                        //            Checkbox = 1,
                        CheckBox checkBox = new CheckBox(this);
                        checkBox.setTypeface(font);
                        for (int k = 0; k < optionsModifiersModelArrayList.size(); k++) {
                            if (itemModifiersList.get(i).isPriceField()) {
                                if (!(optionsModifiersModelArrayList.get(k).getPrice().equals("null"))) {//if true both prices need to be added...
                                    String costPrice = Utils.roundFloatString(Float.parseFloat(optionsModifiersModelArrayList.get(k).getPrice()) + Float.parseFloat((itemModifiersList.get(i).getPrice())), 2);
                                    checkBox.setText(optionsModifiersModelArrayList.get(k).getTitle() + "($" + costPrice + ")");
                                } else {
                                    checkBox.setText(optionsModifiersModelArrayList.get(k).getTitle());
                                }
                            } else {
                                if (!(optionsModifiersModelArrayList.get(k).getPrice().equals("null"))) { //only options price is added.
                                    checkBox.setText(optionsModifiersModelArrayList.get(k).getTitle() + "($" + (Utils.roundFloatString(Float.parseFloat(optionsModifiersModelArrayList.get(k).getPrice()), 2) + ")"));
                                } else {
                                    checkBox.setText(optionsModifiersModelArrayList.get(k).getTitle());
                                }
                            }
                            if (optionsModifiersModelArrayList.get(k).isOptionsDefault()) {
                                checkBox.setChecked(true);
                            }

                            optionsModifiersLayout.addView(checkBox);
                        }
                        break;
                    case 2:
                        //            Radio = 2,
                        final int itemModifierPosition = i;
                        final RadioGroup radioGroup = new RadioGroup(this);
                        radioGroup.setOrientation(LinearLayout.VERTICAL);
                        for (int k = 0; k < optionsModifiersModelArrayList.size(); k++) {
                            RadioButton radioButton = new RadioButton(this);
                            radioButton.setTypeface(font);
                            if (itemModifiersList.get(i).isPriceField()) {
                                if (!(optionsModifiersModelArrayList.get(k).getPrice().equals("null"))) {//if true both prices need to be added...
                                    String costPrice = Utils.roundFloatString(Float.parseFloat(optionsModifiersModelArrayList.get(k).getPrice()) + Float.parseFloat((itemModifiersList.get(i).getPrice())), 2);
                                    radioButton.setText(optionsModifiersModelArrayList.get(k).getTitle() + "($" + costPrice + ")");
                                } else {
                                    radioButton.setText(optionsModifiersModelArrayList.get(k).getTitle());
                                }
                            } else {
                                if (!(optionsModifiersModelArrayList.get(k).getPrice().equals("null"))) { //only options price is added.
                                    radioButton.setText(optionsModifiersModelArrayList.get(k).getTitle() + "($" + (Utils.roundFloatString(Float.parseFloat(optionsModifiersModelArrayList.get(k).getPrice()), 2) + ")"));
                                } else {
                                    radioButton.setText(optionsModifiersModelArrayList.get(k).getTitle());
                                }
                            }

                            if (optionsModifiersModelArrayList.get(k).isOptionsDefault()) {
                                radioButton.setChecked(true);
                            }
                            radioGroup.addView(radioButton);
                        }
                        optionsModifiersLayout.addView(radioGroup);
                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                RadioButton previousButton = null, newButton = null;
                                Log.d("group count", " ->  " + group.getChildCount());
                                for (int i = 0; i < optionsModifiersModelArrayList.size(); i++) {
                                    if (optionsModifiersModelArrayList.get(i).isOptionsDefault()) {
                                        RadioButton radioButton = (RadioButton) group.getChildAt(i);
                                        Log.d("index of previous", "->  " + group.indexOfChild(radioButton));
                                        previousButton = radioButton;
                                        break;
                                    }
                                }
                                newButton = (RadioButton) group.findViewById(checkedId);
                                if (previousButton != null) {
                                    int previousPosition = group.indexOfChild(previousButton);
                                    int newPosition = group.indexOfChild(newButton);
                                    Log.d("new Position", "" + newPosition);
                                    Log.d("prev Position", "" + previousPosition);

                                    ///Update database
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    optionsModifiersModelArrayList.get(previousPosition).setOptionsDefault(false);
                                    optionsModifiersModelArrayList.get(newPosition).setOptionsDefault(true);
//                                    itemModifiersList.get(itemModifierPosition).setOptionsModifiersList(optionsModifiersModelArrayList);
//                                    realm.copyToRealmOrUpdate(itemModifiersList);
                                    realm.commitTransaction();
                                    previousButton.setChecked(false);
                                    group.check(checkedId);

//                                    newButton.setChecked(true);
                                }
                            }
                        });
                        break;

                    case 3:
                        //            DropDown = 3,
                        Spinner spinner = new Spinner(this);
                        ArrayList<String> spinnerList = new ArrayList<>();
                        spinner.setBackgroundResource(R.mipmap.dropdown_icon);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                                R.layout.spinner_text_view, spinnerList);
                        int spinnerPosition = 0;
                        for (int k = 0; k < optionsModifiersModelArrayList.size(); k++) {
                            spinnerList.add(optionsModifiersModelArrayList.get(k).getTitle()+ "($" + (Utils.roundFloatString(Float.parseFloat(optionsModifiersModelArrayList.get(k).getPrice()), 2) + ")"));
                            if (optionsModifiersModelArrayList.get(k).isOptionsDefault()) {
                                spinnerPosition = k;
                            }
                        }
                        spinner.setAdapter(arrayAdapter);
                        spinner.setSelection(spinnerPosition);
                        optionsModifiersLayout.addView(spinner);
                        break;
                    case 4:
                        //            CheckboxList = 4

                        for (int k = 0; k < optionsModifiersModelArrayList.size(); k++) {
                            CheckBox checkBox1 = new CheckBox(this);
                            checkBox1.setTypeface(font);
                            if (itemModifiersList.get(i).isPriceField()) {//if true both(Modifier and options)prices need to be added...
                                if (!(optionsModifiersModelArrayList.get(k).getPrice().equals("null"))) {
                                    String costPrice = Utils.roundFloatString(Float.parseFloat(optionsModifiersModelArrayList.get(k).getPrice()) + Float.parseFloat((itemModifiersList.get(i).getPrice())), 2);
                                    checkBox1.setText(optionsModifiersModelArrayList.get(k).getTitle() + "($" + costPrice + ")");
                                }
                            } else {//only options price is added.
                                if (!(optionsModifiersModelArrayList.get(k).getPrice().equals("null"))) {
                                    checkBox1.setText(optionsModifiersModelArrayList.get(k).getTitle() + "($" + (Utils.roundFloatString(Float.parseFloat(optionsModifiersModelArrayList.get(k).getPrice()), 2) + ")"));
                                } else {
                                    checkBox1.setText(optionsModifiersModelArrayList.get(k).getTitle());
                                }
                            }
                            if (optionsModifiersModelArrayList.get(k).isOptionsDefault()) {
                                checkBox1.setChecked(true);
                            }
                            optionsModifiersLayout.addView(checkBox1);
                        }
                        break;
                }

                optionsLayout.addView(optionsModifiersLayout);
                mainLayout.addView(optionsLayout);

                View view1 = new View(this);
                view1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
                view1.setBackgroundResource(R.color.view_bg);
                mainLayout.addView(view1);
            }
        }

        modifierDialog.setView(view);
        modifierDialog.show();


    }

    public float pixelsToSp(float px) {
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    private void getUserModifierData() {
        int modifierSelectedCount = 0;
        int count = mainLayout.getChildCount();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        for (int i = 0; i < count; i++) {
            if (mainLayout.getChildAt(i) instanceof LinearLayout) {
                LinearLayout optionsLayout = (LinearLayout) mainLayout.getChildAt(i);
                int optionsCount = optionsLayout.getChildCount();
                RealmList<OptionsModifiersModel> optionsModifiersList =
                        itemModifiersList.get(i / 2).getOptionsModifiersList();
                for (int k = 0; k < optionsCount; k++) {
                    modifierSelectedCount = 0;
                    if (optionsLayout.getChildAt(k) instanceof LinearLayout) {
                        LinearLayout optionsModifiersLayout = (LinearLayout) optionsLayout.getChildAt(k);
                        for (int j = 0; j < optionsModifiersLayout.getChildCount(); j++) {
                            if ((optionsModifiersLayout.getChildAt(j)) instanceof CheckBox) {
                                CheckBox checkBox = (CheckBox) optionsModifiersLayout.getChildAt(j);
                                String checkText = (String) checkBox.getText();
                                if (checkBox.isChecked()) {
                                    if (checkText.contains(optionsModifiersList.get(j).getTitle())) {
                                        optionsModifiersList.get(j).setOptionsDefault(true);
                                        modifierSelectedCount++;
                                    }
                                } else {
                                    if (checkText.contains(optionsModifiersList.get(j).getTitle())) {
                                        optionsModifiersList.get(j).setOptionsDefault(false);
                                    }
                                }
                                if (modifierSelectedCount >= 1) {
                                    itemModifiersList.get(i / 2).setIsModifierSelected(true);
                                } else {
                                    itemModifiersList.get(i / 2).setIsModifierSelected(false);
                                }
                            } else if ((optionsModifiersLayout.getChildAt(j)) instanceof Spinner) {
                                Spinner spinner = (Spinner) optionsModifiersLayout.getChildAt(j);
                                for (int p = 0; p < optionsModifiersList.size(); p++) {
                                    if (p == spinner.getSelectedItemPosition()) {
                                        optionsModifiersList.get(spinner.getSelectedItemPosition()).setOptionsDefault(true);
                                        modifierSelectedCount++;
                                    } else {
                                        optionsModifiersList.get(p).setOptionsDefault(false);
                                    }
                                }
                                if (modifierSelectedCount >= 1) {
                                    itemModifiersList.get(i / 2).setIsModifierSelected(true);
                                } else {
                                    itemModifiersList.get(i / 2).setIsModifierSelected(false);
                                }

                            } else if ((optionsModifiersLayout.getChildAt(j)) instanceof RadioGroup) {
                                for (int p = 0; p < optionsModifiersList.size(); p++) {
                                    if (optionsModifiersList.get(p).isOptionsDefault()) {
                                        modifierSelectedCount++;
                                    }

                                }
                                if (modifierSelectedCount >= 1) {
                                    itemModifiersList.get(i / 2).setIsModifierSelected(true);
                                } else {
                                    itemModifiersList.get(i / 2).setIsModifierSelected(false);
                                }
                            }
                        }

                    }
                }
                //itemModifiersList.get(i / 2).setOptionsModifiersList(optionsModifiersList);
            }
        }

//        CartModel cartModel = realm.where(CartModel.class).findFirst();
//        if (cartModel == null) {
//            cartModel = new CartModel();
//            cartModel.setCartId(1);
//        } else {
//            cartModel.setCartId(cartModel.getCartId());
//        }


        realm.commitTransaction();
    }

    private void getTotalModifiersAmount() {

        for (int i = 0; i < itemModifiersList.size(); i++) {
            RealmList<OptionsModifiersModel> optionsModifiersModelArrayList = itemModifiersList.get(i).getOptionsModifiersList();
            for (int j = 0; j < optionsModifiersModelArrayList.size(); j++) {
                if (optionsModifiersModelArrayList.get(j).isOptionsDefault()) {
                    if (!(optionsModifiersModelArrayList.get(j).getPrice().equals("null"))) {
                        modifierTotalAmount += (Float.parseFloat(optionsModifiersModelArrayList.get(j).getPrice()));
                    }
                }
            }
        }
    }

    @Override
    public void userAction(int actionType) {

    }

    private void setData(String price) {
        itemPriceTxt.setText(String.format("$%s", Utils.roundFloatString(Float.parseFloat(price), 2)));
        totalItemCost = itemQty * (Float.parseFloat(price) + modifierTotalAmount);
        addOrderPrice.setText(String.format("$%s", Utils.roundFloatString(totalItemCost, 2)));
    }


    private String setUserOptionsModifiers() {
        StringBuilder stringBuilder = new StringBuilder();
        int modifierCount = 0, optionsCount = 0;
        for (int i = 0; i < itemModifiersList.size(); i++) {
            RealmList<OptionsModifiersModel> optionsModifiersModelRealmList = itemModifiersList.get(i).getOptionsModifiersList();
            optionsCount = 0;
            if (itemModifiersList.get(i).getIsModifierSelected()) {
                modifierCount++;
                if (modifierCount == 1) {
                    stringBuilder.append("(");
                }
                stringBuilder.append((itemModifiersList.get(i).getModifierName())).append(":").append(" ");
                for (int j = 0; j < optionsModifiersModelRealmList.size(); j++) {
                    if (optionsModifiersModelRealmList.get(j).isOptionsDefault()) {
                        optionsCount++;
                        if (optionsCount > 1) {
                            stringBuilder.append(",").append(" ");
                        }
                        stringBuilder.append(optionsModifiersModelRealmList.get(j).getTitle());
                    }

                }
                stringBuilder.append("\n");
            }

        }
        if (modifierCount >= 1) {
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.append(")");
        }
        if (stringBuilder.length() > 0) {
//            makeSelectionTxt.setText(stringBuilder.toString());
            userSelectedModifierOptions = stringBuilder.toString();
            return userSelectedModifierOptions;
        } else {
            return "";
        }
    }


}
