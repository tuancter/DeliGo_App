package com.deligo.app.data.remote;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Singleton quản lý Firestore instance và collections
 */
public class FirestoreManager {
    private static FirestoreManager instance;
    private final FirebaseFirestore db;
    
    // Collection names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_CATEGORIES = "categories";
    public static final String COLLECTION_FOODS = "foods";
    public static final String COLLECTION_ORDERS = "orders";
    public static final String COLLECTION_ORDER_DETAILS = "order_details";
    public static final String COLLECTION_REVIEWS = "reviews";
    public static final String COLLECTION_COMPLAINTS = "complaints";
    public static final String COLLECTION_CART = "cart";
    public static final String COLLECTION_CART_ITEMS = "cart_items";
    
    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        
        // Cấu hình Firestore
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .build();
        db.setFirestoreSettings(settings);
    }
    
    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }
    
    public FirebaseFirestore getDb() {
        return db;
    }
}
