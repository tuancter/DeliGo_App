package com.deligo.app.data.local.model;

// Simple POJO representing a cart item joined with its food info
public class CartItemWithFood {
    public long cartItemId;
    public long foodId;
    public String foodName;
    public double price;
    public int quantity;

    public double getSubtotal() {
        return price * quantity;
    }
}