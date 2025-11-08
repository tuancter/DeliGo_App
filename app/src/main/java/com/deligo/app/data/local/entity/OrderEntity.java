package com.deligo.app.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "Orders",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "UserID",
                        childColumns = "CustomerID",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "UserID",
                        childColumns = "ShipperID",
                        onDelete = ForeignKey.SET_NULL,
                        onUpdate = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "CustomerID"),
                @Index(value = "ShipperID")
        }
)
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "OrderID")
    private long orderId;

    @ColumnInfo(name = "CustomerID")
    private long customerId;

    @ColumnInfo(name = "ShipperID")
    private Long shipperId;

    @ColumnInfo(name = "DeliveryAddress")
    private String deliveryAddress;

    @ColumnInfo(name = "TotalAmount")
    private double totalAmount;

    @ColumnInfo(name = "PaymentMethod")
    private String paymentMethod;

    @ColumnInfo(name = "PaymentStatus")
    private String paymentStatus;

    @ColumnInfo(name = "OrderStatus")
    private String orderStatus;

    @ColumnInfo(name = "Phone")
    private String phone;

    @ColumnInfo(name = "Note")
    private String note;

    @ColumnInfo(name = "CreatedAt")
    private String createdAt;

    public OrderEntity() {
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public Long getShipperId() {
        return shipperId;
    }

    public void setShipperId(Long shipperId) {
        this.shipperId = shipperId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
