package com.deligo.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.deligo.app.data.remote.AuthRepository;
import com.deligo.app.data.remote.model.UserModel;
import com.google.android.gms.tasks.Task;

/**
 * Manager để lưu và validate session token
 */
public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "DeliGoSession";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";
    
    private final SharedPreferences prefs;
    private final AuthRepository authRepository;
    
    public SessionManager(@NonNull Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.authRepository = new AuthRepository();
    }
    
    /**
     * Lưu session sau khi login thành công
     */
    public void saveSession(@NonNull String sessionToken, @NonNull String userId, @NonNull String role) {
        prefs.edit()
                .putString(KEY_SESSION_TOKEN, sessionToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_ROLE, role)
                .apply();
        
        Log.d(TAG, "Session saved for user: " + userId);
    }
    
    /**
     * Lấy session token hiện tại
     */
    @Nullable
    public String getSessionToken() {
        return prefs.getString(KEY_SESSION_TOKEN, null);
    }
    
    /**
     * Lấy user ID hiện tại
     */
    @Nullable
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    
    /**
     * Lấy user role hiện tại
     */
    @Nullable
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }
    
    /**
     * Kiểm tra có session không
     */
    public boolean hasSession() {
        return getSessionToken() != null;
    }
    
    /**
     * Validate session với Firestore
     */
    public Task<UserModel> validateSession() {
        String token = getSessionToken();
        if (token == null) {
            return com.google.android.gms.tasks.Tasks.forException(new Exception("No session found"));
        }
        
        return authRepository.validateSession(token);
    }
    
    /**
     * Clear session (logout)
     */
    public void clearSession() {
        String token = getSessionToken();
        if (token != null) {
            authRepository.logout(token);
        }
        
        prefs.edit().clear().apply();
        UserSession.clear();
        
        Log.d(TAG, "Session cleared");
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return hasSession();
    }
}
