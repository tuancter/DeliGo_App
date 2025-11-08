package com.deligo.app.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "Complaints",
        primaryKeys = {"UserID", "OrderID"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "UserID",
                        childColumns = "UserID",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = OrderEntity.class,
                        parentColumns = "OrderID",
                        childColumns = "OrderID",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "OrderID")
        }
)
public class ComplaintEntity {

    @ColumnInfo(name = "UserID")
    private long userId;

    @ColumnInfo(name = "OrderID")
    private long orderId;

    @ColumnInfo(name = "Content")
    private String content;

    @ColumnInfo(name = "Status")
    private String status;

    @ColumnInfo(name = "CreatedAt")
    private String createdAt;

    public ComplaintEntity() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
