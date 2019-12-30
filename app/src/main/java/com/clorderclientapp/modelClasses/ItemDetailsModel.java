package com.clorderclientapp.modelClasses;

import java.util.ArrayList;


public class ItemDetailsModel {

    public String itemOrderId;
    public String itemName;
    public String itemQuantity;
    public String note;
    public String price;
    private String isNoteVisible;
    private String isMakeItWithVisible;
    public String totalPrice;
    public String options;
    private ArrayList<ItemDetailsModel> orderItemDetailList;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getIsNoteVisible() {
        return isNoteVisible;
    }

    public void setIsNoteVisible(String isNoteVisible) {
        this.isNoteVisible = isNoteVisible;
    }

    public String getIsMakeItWithVisible() {
        return isMakeItWithVisible;
    }

    public void setIsMakeItWithVisible(String isMakeItWithVisible) {
        this.isMakeItWithVisible = isMakeItWithVisible;
    }

    public ArrayList<ItemDetailsModel> getOrderItemDetailList() {
        return orderItemDetailList;
    }

    public void setOrderItemDetailList(ArrayList<ItemDetailsModel> orderItemDetailList) {
        this.orderItemDetailList = orderItemDetailList;
    }

    public String getItemOrderId() {
        return itemOrderId;
    }

    public void setItemOrderId(String itemOrderId) {
        this.itemOrderId = itemOrderId;
    }
}
