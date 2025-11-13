package com.deligo.app.data;

import androidx.annotation.Nullable;

import com.deligo.app.data.local.entity.UserEntity;
import com.deligo.app.data.remote.model.UserModel;

/**
 * Simple in-memory session holder for the currently authenticated user.
 */
public final class UserSession {

    private static Long currentUserId;
    private static String currentUserRole;
    private static String sessionToken;
    private static String firestoreUserId;

    private UserSession() {
        // Utility class
    }

    public static synchronized void setCurrentUser(UserEntity user) {
        if (user == null) {
            clear();
            return;
        }
        currentUserId = user.getUserId();
        currentUserRole = user.getRole();
    }

    public static synchronized void setCurrentUser(UserModel user, String token) {
        if (user == null) {
            clear();
            return;
        }
        firestoreUserId = user.getUserId();
        currentUserRole = user.getRole();
        sessionToken = token;
    }

    public static synchronized void clear() {
        currentUserId = null;
        currentUserRole = null;
        sessionToken = null;
        firestoreUserId = null;
    }

    @Nullable
    public static synchronized Long getCurrentUserId() {
        return currentUserId;
    }

    @Nullable
    public static synchronized String getCurrentUserRole() {
        return currentUserRole;
    }

    @Nullable
    public static synchronized String getSessionToken() {
        return sessionToken;
    }

    @Nullable
    public static synchronized String getFirestoreUserId() {
        return firestoreUserId;
    }
}
