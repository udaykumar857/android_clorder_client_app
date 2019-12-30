package com.clorderclientapp.RealmModels;


import com.clorderclientapp.modelClasses.ItemModifiersModel;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CartItemModel extends RealmObject {

    @PrimaryKey
    private int serialId;
    private int itemId;
    private int categoryId;
    private String itemDesc;
    private String itemTitleCode;
    private int itemMinQuantity;
    private int itemOrderQuantity;
    private int itemMode;
    private String itemNotes;
    private int itemOrderNo;
    private String itemPrice;
    private String itemTitle;
    private String specialNotes;
    private float totalItemPrice;//total cost with modifiers
    private RealmList<ItemModifiersModel> itemModifiersList;
    private String userSelectedModifierOptions;


    public int getSerialId() {
        return serialId;
    }

    public void setSerialId(int serialId) {
        this.serialId = serialId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemTitleCode() {
        return itemTitleCode;
    }

    public void setItemTitleCode(String itemTitleCode) {
        this.itemTitleCode = itemTitleCode;
    }

    public int getItemMinQuantity() {
        return itemMinQuantity;
    }

    public void setItemMinQuantity(int itemMinQuantity) {
        this.itemMinQuantity = itemMinQuantity;
    }

    public int getItemOrderQuantity() {
        return itemOrderQuantity;
    }

    public void setItemOrderQuantity(int itemOrderQuantity) {
        this.itemOrderQuantity = itemOrderQuantity;
    }

    public int getItemMode() {
        return itemMode;
    }

    public void setItemMode(int itemMode) {
        this.itemMode = itemMode;
    }

    public String getItemNotes() {
        return itemNotes;
    }

    public void setItemNotes(String itemNotes) {
        this.itemNotes = itemNotes;
    }

    public int getItemOrderNo() {
        return itemOrderNo;
    }

    public void setItemOrderNo(int itemOrderNo) {
        this.itemOrderNo = itemOrderNo;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public float getTotalItemPrice() {
        return totalItemPrice;
    }

    public void setTotalItemPrice(float totalItemPrice) {
        this.totalItemPrice = totalItemPrice;
    }

    public RealmList<ItemModifiersModel> getItemModifiersList() {
        return itemModifiersList;
    }

    public void setItemModifiersList(RealmList<ItemModifiersModel> itemModifiersList) {
        this.itemModifiersList = itemModifiersList;
    }

    public String getUserSelectedModifierOptions() {
        return userSelectedModifierOptions;
    }

    public void setUserSelectedModifierOptions(String userSelectedModifierOptions) {
        this.userSelectedModifierOptions = userSelectedModifierOptions;
    }

    @Override
    public String toString() {
        return "CartItemModel{" +
                "serialId=" + serialId + "\n" +
                ", itemId=" + itemId + "\n" +
                ", categoryId=" + categoryId + "\n" +
                ", itemDesc='" + itemDesc + '\'' + "\n" +
                ", itemTitleCode='" + itemTitleCode + '\'' + "\n" +
                ", itemMinQuantity=" + itemMinQuantity + "\n" +
                ", itemOrderQuantity=" + itemOrderQuantity + "\n" +
                ", itemMode=" + itemMode + "\n" +
                ", itemNotes='" + itemNotes + '\'' + "\n" +
                ", itemOrderNo=" + itemOrderNo + "\n" +
                ", itemPrice='" + itemPrice + '\'' + "\n" +
                ", itemTitle='" + itemTitle + '\'' + "\n" +
                ", specialNotes='" + specialNotes + '\'' + "\n" +
                ", totalItemPrice=" + totalItemPrice + "\n" +
                '}';
    }
}
