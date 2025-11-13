package com.deligo.app.data.remote.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model cho User trong Firestore
 */
public class UserModel {
    @PropertyName("userId")
    private String userId;
    
    @PropertyName("fullName")
    private String fullName;
    
    @PropertyName("email")
    private String email;
    
    @PropertyName("passwordHash")
    private String passwordHash;
    
    @PropertyName("role")
    private String role;
    
    @PropertyName("status")
    private String status;
    
    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;
    
    @PropertyName("updatedAt")
    @ServerTimestamp
    private Date updatedAt;
    
    public UserModel() {
        // Required empty constructor for Firestore
    }
    
    public UserModel(@NonNull String userId, @NonNull String fullName, @NonNull String email,
                     @NonNull String passwordHash, @NonNull String role, @NonNull String status) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
