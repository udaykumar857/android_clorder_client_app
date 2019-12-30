package com.clorderclientapp.Singleton;

import com.clorderclientapp.RealmModels.CartItemModel;

import java.util.ArrayList;

public class CartSingleton {
    private static CartSingleton mCartSingleton = null;
    private ArrayList<CartItemModel> cartItemModelList = new ArrayList<>();
    private float subtotal=0.0f;

    public static CartSingleton getInstance() {
        if (mCartSingleton == null) {
            mCartSingleton = new CartSingleton();
        }
        return mCartSingleton;
    }

    public ArrayList<CartItemModel> getCartItemModelList() {
        return cartItemModelList;
    }

    public void setCartItemModelList(ArrayList<CartItemModel> cartItemModelList) {
        this.cartItemModelList = cartItemModelList;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }
}
