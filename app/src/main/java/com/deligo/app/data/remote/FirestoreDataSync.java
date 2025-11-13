package com.deligo.app.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.entity.CategoryEntity;
import com.deligo.app.data.local.entity.FoodEntity;
import com.deligo.app.data.remote.model.CategoryModel;
import com.deligo.app.data.remote.model.FoodModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Helper class để sync dữ liệu từ Room lên Firestore lần đầu
 * (Chỉ dùng 1 lần khi migrate từ SQLite sang Firestore)
 */
public class FirestoreDataSync {
    private static final String TAG = "FirestoreDataSync";
    private final DeliGoDatabase localDb;
    private final FirebaseFirestore firestore;
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    public FirestoreDataSync(@NonNull DeliGoDatabase localDb) {
        this.localDb = localDb;
        this.firestore = FirestoreManager.getInstance().getDb();
    }
    
    /**
     * Sync categories từ Room lên Firestore
     */
    public void syncCategoriesToFirestore(@NonNull OnSyncListener listener) {
        executor.execute(() -> {
            List<CategoryEntity> categories = localDb.categoriesDao().getAllSync();
            
            if (categories.isEmpty()) {
                listener.onComplete("No categories to sync");
                return;
            }
            
            int[] count = {0};
            for (CategoryEntity entity : categories) {
                CategoryModel model = new CategoryModel(entity.getCategoryName());
                
                firestore.collection(FirestoreManager.COLLECTION_CATEGORIES)
                        .document(String.valueOf(entity.getCategoryId()))
                        .set(model)
                        .addOnSuccessListener(aVoid -> {
                            count[0]++;
                            if (count[0] == categories.size()) {
                                listener.onComplete("Synced " + count[0] + " categories");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error syncing category", e);
                            listener.onError(e.getMessage());
                        });
            }
        });
    }
    
    /**
     * Sync foods từ Room lên Firestore
     */
    public void syncFoodsToFirestore(@NonNull OnSyncListener listener) {
        executor.execute(() -> {
            List<FoodEntity> foods = localDb.foodsDao().getAllFoodsSync();
            
            if (foods.isEmpty()) {
                listener.onComplete("No foods to sync");
                return;
            }
            
            int[] count = {0};
            for (FoodEntity entity : foods) {
                FoodModel model = new FoodModel();
                model.setFoodId(String.valueOf(entity.getFoodId()));
                model.setCategoryId(String.valueOf(entity.getCategoryId()));
                model.setName(entity.getName());
                model.setDescription(entity.getDescription());
                model.setPrice(entity.getPrice());
                model.setImageUrl(entity.getImageUrl());
                model.setAvailable(entity.isAvailable());
                
                firestore.collection(FirestoreManager.COLLECTION_FOODS)
                        .document(String.valueOf(entity.getFoodId()))
                        .set(model)
                        .addOnSuccessListener(aVoid -> {
                            count[0]++;
                            if (count[0] == foods.size()) {
                                listener.onComplete("Synced " + count[0] + " foods");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error syncing food", e);
                            listener.onError(e.getMessage());
                        });
            }
        });
    }
    
    /**
     * Sync tất cả dữ liệu cơ bản
     */
    public void syncAllBasicData(@NonNull OnSyncListener listener) {
        syncCategoriesToFirestore(new OnSyncListener() {
            @Override
            public void onComplete(String message) {
                Log.d(TAG, "Categories synced: " + message);
                syncFoodsToFirestore(listener);
            }
            
            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }
    
    public interface OnSyncListener {
        void onComplete(String message);
        void onError(String error);
    }
}
