package com.deligo.app.data.remote.model;

import com.google.firebase.firestore.DocumentId;

public class CategoryModel {
    @DocumentId
    private String categoryId;
    private String categoryName;
    
    public CategoryModel() {}
    
    public CategoryModel(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
