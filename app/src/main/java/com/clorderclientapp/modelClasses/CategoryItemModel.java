package com.clorderclientapp.modelClasses;


public class CategoryItemModel {

//    {
//        "CategoryId": 31,
//            "CreateDate": "2012-06-30T23:18:00",
//            "Description": "Simple Tastier &amp; Favourite Dosa",
//            "Fields": [
//        2,
//                44
//        ],
//        "ImageUrl": "",
//            "IsAvailable": true,
//            "IsDiscount": true,
//            "IsTaxable": false,
//            "ItemCode": "DD-GC",
//            "ItemFilters": 1,
//            "ItemId": 1017,
//            "ItemTitleCode": "Pancake Delight (1017)",
//            "MinQuantity": 1,
//            "Mode": 1,
//            "Notes": null,
//            "OrderNo": 1,
//            "Price": 7.99,
//            "Title": "Pancake Delight",
//            "Visible": false
//    }

    public int categoryId;
    public String itemDesc;
    public String itemImageUrl;
    public Boolean itemIsAvailable;
    public Boolean itemIsDiscount;
    public Boolean itemIsTaxable;
    public String itemCode;
    public String itemFilters;
    public int itemId;
    public String itemTitleCode;
    public int itemMinQuantity;
    private int itemOrderedQuantity;
    public int itemMode;
    public String itemNotes;
    public int itemOrderNo;
    public String itemPrice;
    public String itemTitle;
    public Boolean itemVisible;


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

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        this.itemImageUrl = itemImageUrl;
    }

    public Boolean getItemIsAvailable() {
        return itemIsAvailable;
    }

    public void setItemIsAvailable(Boolean itemIsAvailable) {
        this.itemIsAvailable = itemIsAvailable;
    }

    public Boolean getItemIsDiscount() {
        return itemIsDiscount;
    }

    public void setItemIsDiscount(Boolean itemIsDiscount) {
        this.itemIsDiscount = itemIsDiscount;
    }

    public Boolean getItemIsTaxable() {
        return itemIsTaxable;
    }

    public void setItemIsTaxable(Boolean itemIsTaxable) {
        this.itemIsTaxable = itemIsTaxable;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemFilters() {
        return itemFilters;
    }

    public void setItemFilters(String itemFilters) {
        this.itemFilters = itemFilters;
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

    public int getItemOrderedQuantity() {
        return itemOrderedQuantity;
    }

    public void setItemOrderedQuantity(int itemOrderedQuantity) {
        this.itemOrderedQuantity = itemOrderedQuantity;
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

    public Boolean getItemVisible() {
        return itemVisible;
    }

    public void setItemVisible(Boolean itemVisible) {
        this.itemVisible = itemVisible;
    }

}
