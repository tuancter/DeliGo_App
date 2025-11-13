package com.deligo.app.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.deligo.app.data.remote.model.SessionModel;
import com.deligo.app.data.remote.model.UserModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Repository xử lý authentication với Firestore
 */
public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private static final String COLLECTION_SESSIONS = "sessions";
    private static final int SESSION_DURATION_HOURS = 1;
    
    private final FirebaseFirestore db;
    
    public AuthRepository() {
        this.db = FirestoreManager.getInstance().getDb();
    }
    
    /**
     * Đăng ký user mới
     */
    public Task<String> registerUser(@NonNull String fullName, @NonNull String email,
                                     @NonNull String password, @NonNull String role) {
        return checkEmailExists(email).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            
            if (task.getResult()) {
                throw new Exception("Email already registered");
            }
            
            String userId = UUID.randomUUID().toString();
            String passwordHash = hashPassword(password);
            
            UserModel user = new UserModel(userId, fullName, email, passwordHash, role, "Active");
            
            return db.collection(FirestoreManager.COLLECTION_USERS)
                    .document(userId)
                    .set(user)
                    .continueWith(setTask -> {
                        if (!setTask.isSuccessful()) {
                            throw setTask.getException();
                        }
                        Log.d(TAG, "User registered successfully: " + userId);
                        return userId;
                    });
        });
    }
    
    /**
     * Đăng nhập và tạo session token
     */
    public Task<SessionResult> loginUser(@NonNull String email, @NonNull String password) {
        String passwordHash = hashPassword(password);
        
        return db.collection(FirestoreManager.COLLECTION_USERS)
                .whereEqualTo("email", email)
                .whereEqualTo("passwordHash", passwordHash)
                .whereEqualTo("status", "Active")
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot.isEmpty()) {
                        throw new Exception("Invalid email or password");
                    }
                    
                    DocumentSnapshot userDoc = snapshot.getDocuments().get(0);
                    UserModel user = userDoc.toObject(UserModel.class);
                    
                    if (user == null) {
                        throw new Exception("User data is invalid");
                    }
                    
                    return createSession(user.getUserId()).continueWith(sessionTask -> {
                        if (!sessionTask.isSuccessful()) {
                            throw sessionTask.getException();
                        }
                        
                        SessionModel session = sessionTask.getResult();
                        return new SessionResult(user, session);
                    });
                });
    }
    
    /**
     * Tạo session token mới
     */
    private Task<SessionModel> createSession(@NonNull String userId) {
        String sessionToken = generateSessionToken();
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, SESSION_DURATION_HOURS);
        Date expiresAt = calendar.getTime();
        
        SessionModel session = new SessionModel(sessionToken, userId, expiresAt);
        
        return db.collection(COLLECTION_SESSIONS)
                .document(sessionToken)
                .set(session)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    Log.d(TAG, "Session created: " + sessionToken);
                    return session;
                });
    }
    
    /**
     * Validate session token
     */
    public Task<UserModel> validateSession(@NonNull String sessionToken) {
        return db.collection(COLLECTION_SESSIONS)
                .document(sessionToken)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    
                    DocumentSnapshot sessionDoc = task.getResult();
                    if (!sessionDoc.exists()) {
                        throw new Exception("Invalid session");
                    }
                    
                    SessionModel session = sessionDoc.toObject(SessionModel.class);
                    if (session == null || !session.isActive()) {
                        throw new Exception("Session is not active");
                    }
                    
                    if (session.getExpiresAt().before(new Date())) {
                        invalidateSession(sessionToken);
                        throw new Exception("Session expired");
                    }
                    
                    return db.collection(FirestoreManager.COLLECTION_USERS)
                            .document(session.getUserId())
                            .get()
                            .continueWith(userTask -> {
                                if (!userTask.isSuccessful()) {
                                    throw userTask.getException();
                                }
                                
                                DocumentSnapshot userDoc = userTask.getResult();
                                if (!userDoc.exists()) {
                                    throw new Exception("User not found");
                                }
                                
                                return userDoc.toObject(UserModel.class);
                            });
                });
    }
    
    /**
     * Logout - invalidate session
     */
    public Task<Void> logout(@NonNull String sessionToken) {
        return invalidateSession(sessionToken);
    }
    
    private Task<Void> invalidateSession(@NonNull String sessionToken) {
        return db.collection(COLLECTION_SESSIONS)
                .document(sessionToken)
                .update("isActive", false);
    }
    
    /**
     * Kiểm tra email đã tồn tại chưa
     */
    private Task<Boolean> checkEmailExists(@NonNull String email) {
        return db.collection(FirestoreManager.COLLECTION_USERS)
                .whereEqualTo("email", email)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return !task.getResult().isEmpty();
                });
    }
    
    /**
     * Hash password bằng SHA-256
     */
    private String hashPassword(@NonNull String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Generate session token ngẫu nhiên
     */
    private String generateSessionToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        
        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }
        return token.toString();
    }
    
    /**
     * Result class chứa user và session
     */
    public static class SessionResult {
        private final UserModel user;
        private final SessionModel session;
        
        public SessionResult(@NonNull UserModel user, @NonNull SessionModel session) {
            this.user = user;
            this.session = session;
        }
        
        public UserModel getUser() {
            return user;
        }
        
        public SessionModel getSession() {
            return session;
        }
    }
}
