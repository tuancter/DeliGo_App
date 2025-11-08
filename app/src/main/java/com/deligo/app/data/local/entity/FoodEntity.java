package com.deligo.app.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Foods",
        foreignKeys = @ForeignKey(
                entity = CategoryEntity.class,
                parentColumns = "CategoryID",
                childColumns = "CategoryID",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "CategoryID")}
)
public class FoodEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "FoodID")
    private long foodId;

    @ColumnInfo(name = "CategoryID")
    private long categoryId;

    @ColumnInfo(name = "Name")
    private String name;

    @ColumnInfo(name = "Description")
    private String description;

    @ColumnInfo(name = "Price")
    private double price;

    @ColumnInfo(name = "ImageUrl")
    private String imageUrl;

    @ColumnInfo(name = "IsAvailable")
    private boolean available;

    public FoodEntity() {
    }

    public long getFoodId() {
        return foodId;
    }

    public void setFoodId(long foodId) {
        this.foodId = foodId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
