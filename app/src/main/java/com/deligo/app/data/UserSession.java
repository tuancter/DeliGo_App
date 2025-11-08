package com.deligo.app.data;

import androidx.annotation.Nullable;

import com.deligo.app.data.local.entity.UserEntity;

/**
 * Simple in-memory session holder for the currently authenticated user.
 */
public final class UserSession {

    private static Long currentUserId;
    private static String currentUserRole;

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

    public static synchronized void clear() {
        currentUserId = null;
        currentUserRole = null;
    }

    @Nullable
    public static synchronized Long getCurrentUserId() {
        return currentUserId;
    }

    @Nullable
    public static synchronized String getCurrentUserRole() {
        return currentUserRole;
    }
}
