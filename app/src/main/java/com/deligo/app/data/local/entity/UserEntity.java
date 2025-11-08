package com.deligo.app.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "UserID")
    private long userId;

    @ColumnInfo(name = "FullName")
    private String fullName;

    @ColumnInfo(name = "Email")
    private String email;

    @ColumnInfo(name = "Phone")
    private String phone;

    @ColumnInfo(name = "Password")
    private String password;

    @ColumnInfo(name = "Role")
    private String role;

    @ColumnInfo(name = "Status")
    private String status;

    @ColumnInfo(name = "CreatedAt")
    private String createdAt;

    public UserEntity() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
