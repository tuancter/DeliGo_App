package com.deligo.app.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "Reviews",
        primaryKeys = {"UserID", "FoodID"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "UserID",
                        childColumns = "UserID",
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
public class ReviewEntity {

    @ColumnInfo(name = "UserID")
    private long userId;

    @ColumnInfo(name = "FoodID")
    private long foodId;

    @ColumnInfo(name = "Rating")
    private int rating;

    @ColumnInfo(name = "Comment")
    private String comment;

    @ColumnInfo(name = "CreatedAt")
    private String createdAt;

    public ReviewEntity() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFoodId() {
        return foodId;
    }

    public void setFoodId(long foodId) {
        this.foodId = foodId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
