package com.clorderclientapp.modelClasses;

public class RestaurantPromotionsModel {

//    "CouponId": 1315,
//            "Title": "1",
//            "Discount": 0,
//            "GratuityDiscount": 0,
//            "Active": true,
//            "CreateDate": 0,
//            "DiscountType": 1,
//            "TotalAmount": 0,
//            "ValidFor": 4,
//            "ValidForOrderType": 1,
//            "MinItemsForDiscount": 0,
//            "ItemsDiscountOn": 0,
//            "IsPublic": true,
//            "DateExpire": "2016-12-31T00:00:00",
//            "Description": "",
//            "ItemIdofFreeItem": 0,
//            "DiscountTotalAmount": 0,
//            "DiscountAmount": 0,
//            "QuantityofFreeItem": 0,
//            "MaxDiscount": 0

    public int couponId;
    public String couponTitle;
    public int discount;
    public int gratuityDiscount;
    public boolean isActive;
    public String createDate;
    public int discountType;
    public int totalAmount;
    public int validFor;
    public int validForOrderType;
    public int minItemsForDiscount;
    public int itemsDiscountOn;
    public boolean isPublic;
    public String dateExpire;
    public String couponDesc;
    public int itemIdofFreeItem;
    public int discountTotalAmount;
    public int discountAmount;
    public int quantityofFreeItem;
    public int maxDiscount;

    public int getCouponId() {
        return couponId;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public String getCouponTitle() {
        return couponTitle;
    }

    public void setCouponTitle(String couponTitle) {
        this.couponTitle = couponTitle;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getGratuityDiscount() {
        return gratuityDiscount;
    }

    public void setGratuityDiscount(int gratuityDiscount) {
        this.gratuityDiscount = gratuityDiscount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getDiscountType() {
        return discountType;
    }

    public void setDiscountType(int discountType) {
        this.discountType = discountType;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getValidFor() {
        return validFor;
    }

    public void setValidFor(int validFor) {
        this.validFor = validFor;
    }

    public int getValidForOrderType() {
        return validForOrderType;
    }

    public void setValidForOrderType(int validForOrderType) {
        this.validForOrderType = validForOrderType;
    }

    public int getMinItemsForDiscount() {
        return minItemsForDiscount;
    }

    public void setMinItemsForDiscount(int minItemsForDiscount) {
        this.minItemsForDiscount = minItemsForDiscount;
    }

    public int getItemsDiscountOn() {
        return itemsDiscountOn;
    }

    public void setItemsDiscountOn(int itemsDiscountOn) {
        this.itemsDiscountOn = itemsDiscountOn;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getDateExpire() {
        return dateExpire;
    }

    public void setDateExpire(String dateExpire) {
        this.dateExpire = dateExpire;
    }

    public String getCouponDesc() {
        return couponDesc;
    }

    public void setCouponDesc(String couponDesc) {
        this.couponDesc = couponDesc;
    }

    public int getItemIdofFreeItem() {
        return itemIdofFreeItem;
    }

    public void setItemIdofFreeItem(int itemIdofFreeItem) {
        this.itemIdofFreeItem = itemIdofFreeItem;
    }

    public int getDiscountTotalAmount() {
        return discountTotalAmount;
    }

    public void setDiscountTotalAmount(int discountTotalAmount) {
        this.discountTotalAmount = discountTotalAmount;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getQuantityofFreeItem() {
        return quantityofFreeItem;
    }

    public void setQuantityofFreeItem(int quantityofFreeItem) {
        this.quantityofFreeItem = quantityofFreeItem;
    }

    public int getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(int maxDiscount) {
        this.maxDiscount = maxDiscount;
    }
}
