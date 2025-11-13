package com.deligo.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deligo.app.data.SessionManager;
import com.deligo.app.ui.auth.LoginActivity;

/**
 * Base activity cho các màn hình cần authentication
 * Tự động validate session khi activity được tạo
 */
public abstract class BaseAuthActivity extends AppCompatActivity {
    private static final String TAG = "BaseAuthActivity";
    
    protected SessionManager sessionManager;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        
        // Validate session khi activity được tạo
        if (!sessionManager.hasSession()) {
            redirectToLogin();
            return;
        }
        
        // Validate session với Firestore
        sessionManager.validateSession()
                .addOnSuccessListener(user -> {
                    Log.d(TAG, "Session is valid for user: " + user.getUserId());
                    onSessionValid();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Session validation failed", e);
                    sessionManager.clearSession();
                    redirectToLogin();
                });
    }
    
    /**
     * Được gọi khi session hợp lệ
     */
    protected void onSessionValid() {
        // Override trong subclass nếu cần
    }
    
    /**
     * Logout và redirect về login
     */
    protected void logout() {
        sessionManager.clearSession();
        redirectToLogin();
    }
    
    /**
     * Redirect về màn hình login
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
