package com.deligo.app.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.deligo.app.data.remote.FirestoreManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class để test kết nối Firestore và tạo collections
 */
public class FirestoreTestHelper {
    private static final String TAG = "FirestoreTestHelper";
    
    /**
     * Test kết nối Firestore bằng cách tạo một document test
     */
    public static void testConnection(@NonNull TestCallback callback) {
        FirebaseFirestore db = FirestoreManager.getInstance().getDb();
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("test", "connection");
        testData.put("timestamp", System.currentTimeMillis());
        
        db.collection("_test")
                .document("connection_test")
                .set(testData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Firestore connection successful!");
                    callback.onSuccess("Firestore connected successfully");
                    
                    // Xóa test document
                    db.collection("_test")
                            .document("connection_test")
                            .delete();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Firestore connection failed", e);
                    callback.onError("Connection failed: " + e.getMessage());
                });
    }
    
    /**
     * Tạo các collections cơ bản với document mẫu
     */
    public static void initializeCollections(@NonNull TestCallback callback) {
        FirebaseFirestore db = FirestoreManager.getInstance().getDb();
        
        // Tạo collection users với document mẫu
        Map<String, Object> sampleUser = new HashMap<>();
        sampleUser.put("userId", "sample_user_id");
        sampleUser.put("fullName", "Sample User");
        sampleUser.put("email", "sample@example.com");
        sampleUser.put("passwordHash", "sample_hash");
        sampleUser.put("role", "Customer");
        sampleUser.put("status", "Active");
        sampleUser.put("createdAt", com.google.firebase.Timestamp.now());
        
        db.collection(FirestoreManager.COLLECTION_USERS)
                .document("_sample")
                .set(sampleUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Users collection created");
                    
                    // Tạo collection sessions với document mẫu
                    Map<String, Object> sampleSession = new HashMap<>();
                    sampleSession.put("sessionToken", "sample_token");
                    sampleSession.put("userId", "sample_user_id");
                    sampleSession.put("createdAt", com.google.firebase.Timestamp.now());
                    sampleSession.put("expiresAt", com.google.firebase.Timestamp.now());
                    sampleSession.put("isActive", false);
                    
                    db.collection("sessions")
                            .document("_sample")
                            .set(sampleSession)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "✅ Sessions collection created");
                                callback.onSuccess("Collections initialized successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Failed to create sessions collection", e);
                                callback.onError("Failed to create sessions: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to create users collection", e);
                    callback.onError("Failed to create users: " + e.getMessage());
                });
    }
    
    /**
     * Kiểm tra xem collections đã tồn tại chưa
     */
    public static void checkCollections(@NonNull TestCallback callback) {
        FirebaseFirestore db = FirestoreManager.getInstance().getDb();
        
        StringBuilder result = new StringBuilder();
        
        // Check users collection
        db.collection(FirestoreManager.COLLECTION_USERS)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    result.append("Users collection: ")
                            .append(querySnapshot.isEmpty() ? "Empty" : "Has data")
                            .append("\n");
                    
                    // Check sessions collection
                    db.collection("sessions")
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot2 -> {
                                result.append("Sessions collection: ")
                                        .append(querySnapshot2.isEmpty() ? "Empty" : "Has data")
                                        .append("\n");
                                
                                Log.d(TAG, result.toString());
                                callback.onSuccess(result.toString());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Failed to check sessions collection", e);
                                callback.onError("Failed to check sessions: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to check users collection", e);
                    callback.onError("Failed to check users: " + e.getMessage());
                });
    }
    
    /**
     * Xóa các document mẫu
     */
    public static void cleanupSampleData(@NonNull TestCallback callback) {
        FirebaseFirestore db = FirestoreManager.getInstance().getDb();
        
        db.collection(FirestoreManager.COLLECTION_USERS)
                .document("_sample")
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("sessions")
                            .document("_sample")
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "✅ Sample data cleaned up");
                                callback.onSuccess("Sample data cleaned up");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete sample session", e);
                                callback.onError("Failed to delete sample session: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete sample user", e);
                    callback.onError("Failed to delete sample user: " + e.getMessage());
                });
    }
    
    public interface TestCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
