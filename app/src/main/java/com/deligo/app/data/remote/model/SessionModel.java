package com.deligo.app.data.remote.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model cho Session trong Firestore
 */
public class SessionModel {
    @PropertyName("sessionToken")
    private String sessionToken;
    
    @PropertyName("userId")
    private String userId;
    
    @PropertyName("createdAt")
    @ServerTimestamp
    private Date createdAt;
    
    @PropertyName("expiresAt")
    private Date expiresAt;
    
    @PropertyName("isActive")
    private boolean isActive;
    
    public SessionModel() {
        // Required empty constructor for Firestore
    }
    
    public SessionModel(@NonNull String sessionToken, @NonNull String userId, @NonNull Date expiresAt) {
        this.sessionToken = sessionToken;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.isActive = true;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}
