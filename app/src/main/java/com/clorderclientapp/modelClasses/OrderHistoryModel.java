package com.clorderclientapp.modelClasses;


public class OrderHistoryModel {
    //    {
//        "orderid": 121794,
//            "OrderTotal": "19.6200",
//            "OrderDate": "8/19/2016 6:36:58 AM"
//    }
    private String clientName;
    private String orderId;
    private String orderTotal;
    private String orderDate;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(String orderTotal) {
        this.orderTotal = orderTotal;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}
