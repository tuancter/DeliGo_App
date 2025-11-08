package com.deligo.app.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "CartItems",
        foreignKeys = {
                @ForeignKey(
                        entity = CartEntity.class,
                        parentColumns = "CartID",
                        childColumns = "CartID",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = FoodEntity.class,
                        parentColumns = "FoodID",
                        childColumns = "FoodID",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "CartID"),
                @Index(value = "FoodID")
        }
)
public class CartItemEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CartItemID")
    private long cartItemId;

    @ColumnInfo(name = "CartID")
    private long cartId;

    @ColumnInfo(name = "FoodID")
    private long foodId;

    @ColumnInfo(name = "Quantity")
    private int quantity;

    @ColumnInfo(name = "Price")
    private double price;

    @ColumnInfo(name = "Note")
    private String note;

    public CartItemEntity() {
    }

    public long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public long getFoodId() {
        return foodId;
    }

    public void setFoodId(long foodId) {
        this.foodId = foodId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
