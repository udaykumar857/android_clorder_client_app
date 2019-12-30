package com.clorderclientapp.RealmModels;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CartModel extends RealmObject {

    @PrimaryKey
    private int cartId;
    public int orderId = 0;
    public String orderPlacedTime = null;
    public String orderPlacedDate = null;
    public boolean isFutureOrder;
    public String specialNotes;
    public float subtotal = 0.0f;
    public float tax;
    public int deliveryCharge = 0;
    public float deliveryDist = 0.0f;
    public float discountAmount = 0.0f;
    public String discountCode = null;
    public int discountType = 0;
    public int validForOrderType;
    public int tipPosition = 0;
    public float tipAmount;
    public float orderTotal;
    public String paymentType;
    public RealmList<CartItemModel> cartItemList;

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderPlacedTime() {
        return orderPlacedTime;
    }

    public void setOrderPlacedTime(String orderPlacedTime) {
        this.orderPlacedTime = orderPlacedTime;
    }

    public String getOrderPlacedDate() {
        return orderPlacedDate;
    }

    public void setOrderPlacedDate(String orderPlacedDate) {
        this.orderPlacedDate = orderPlacedDate;
    }

    public boolean isFutureOrder() {
        return isFutureOrder;
    }

    public void setFutureOrder(boolean futureOrder) {
        isFutureOrder = futureOrder;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public float getTax() {
        return tax;
    }

    public void setTax(float tax) {
        this.tax = tax;
    }

    public int getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(int deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public float getDeliveryDist() {
        return deliveryDist;
    }

    public void setDeliveryDist(float deliveryDist) {
        this.deliveryDist = deliveryDist;
    }

    public String getDiscountCode() {

        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public float getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(float discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getDiscountType() {
        return discountType;
    }

    public void setDiscountType(int discountType) {
        this.discountType = discountType;
    }

    public int getValidForOrderType() {
        return validForOrderType;
    }

    public void setValidForOrderType(int validForOrderType) {
        this.validForOrderType = validForOrderType;
    }

    public int getTipPosition() {
        return tipPosition;
    }

    public void setTipPosition(int tipPosition) {
        this.tipPosition = tipPosition;
    }

    public float getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(float tipAmount) {
        this.tipAmount = tipAmount;
    }

    public float getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(float orderTotal) {
        this.orderTotal = orderTotal;
    }

    public RealmList<CartItemModel> getCartItemList() {
        return cartItemList;
    }

    public void setCartItemList(RealmList<CartItemModel> cartItemList) {
        this.cartItemList = cartItemList;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    @Override
    public String toString() {
        return "CartModel{" +
                "tipPosition=" + tipPosition + "\n" +
                ", deliveryCharge=" + deliveryCharge + "\n" +
                ", tax=" + tax + "\n" +
                ", subtotal=" + subtotal + "\n" +
                ", specialNotes='" + specialNotes + '\'' + "\n" +
                ", cartId=" + cartId + "\n" +
                '}';
    }
}
