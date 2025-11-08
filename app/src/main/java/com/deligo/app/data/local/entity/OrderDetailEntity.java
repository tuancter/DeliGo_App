package com.deligo.app.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "OrderDetails",
        primaryKeys = {"OrderID", "FoodID"},
        foreignKeys = {
                @ForeignKey(
                        entity = OrderEntity.class,
                        parentColumns = "OrderID",
                        childColumns = "OrderID",
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
                @Index(value = "FoodID")
        }
)
public class OrderDetailEntity {

    @ColumnInfo(name = "OrderID")
    private long orderId;

    @ColumnInfo(name = "FoodID")
    private long foodId;

    @ColumnInfo(name = "Quantity")
    private int quantity;

    @ColumnInfo(name = "UnitPrice")
    private double unitPrice;

    public OrderDetailEntity() {
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
