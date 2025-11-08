package com.deligo.app.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Cart",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "UserID",
                childColumns = "UserID",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = @Index(value = "UserID")
)
public class CartEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CartID")
    private long cartId;

    @ColumnInfo(name = "UserID")
    private long userId;

    @ColumnInfo(name = "CreatedAt")
    private String createdAt;

    @ColumnInfo(name = "UpdatedAt")
    private String updatedAt;

    public CartEntity() {
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
