package com.deligo.app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.FoodsDao;
import com.deligo.app.data.local.entity.FoodEntity;
import com.deligo.app.data.remote.FirestoreManager;
import com.deligo.app.data.remote.model.FoodModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Food Repository với realtime sync
 */
public class FoodRepository {
    private static final String TAG = "FoodRepository";
    
    private final FoodsDao foodsDao;
    private final FirebaseFirestore firestore;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private ListenerRegistration foodsListener;
    
    public FoodRepository(@NonNull DeliGoDatabase database) {
        this.foodsDao = database.foodsDao();
        this.firestore = FirestoreManager.getInstance().getDb();
    }
    
    /**
     * Lấy foods theo category với realtime updates
     */
    public LiveData<List<FoodEntity>> getFoodsByCategory(long categoryId) {
        MutableLiveData<List<FoodEntity>> result = new MutableLiveData<>();
        
        executor.execute(() -> {
            // 1. Load cache từ Room
            List<FoodEntity> cached = foodsDao.getFoodsByCategorySync(categoryId);
            result.postValue(cached);
            
            // 2. Setup realtime listener từ Firestore
            setupRealtimeListener(categoryId, result);
        });
        
        return result;
    }
    
    /**
     * Setup realtime listener cho foods
     */
    private void setupRealtimeListener(long categoryId, MutableLiveData<List<FoodEntity>> liveData) {
        String categoryIdStr = String.valueOf(categoryId);
        
        foodsListener = firestore.collection(FirestoreManager.COLLECTION_FOODS)
                .whereEqualTo("categoryId", categoryIdStr)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }
                    
                    if (snapshots != null) {
                        executor.execute(() -> {
                            List<FoodEntity> foods = new ArrayList<>();
                            
                            for (QueryDocumentSnapshot doc : snapshots) {
                                FoodModel model = doc.toObject(FoodModel.class);
                                
                                FoodEntity entity = new FoodEntity();
                                entity.setFoodId(parseId(model.getFoodId()));
                                entity.setCategoryId(categoryId);
                                entity.setName(model.getName());
                                entity.setDescription(model.getDescription());
                                entity.setPrice(model.getPrice());
                                entity.setImageUrl(model.getImageUrl());
                                entity.setAvailable(model.isAvailable());
                                
                                foods.add(entity);
                            }
                            
                            // Update Room cache
                            foodsDao.insertAll(foods);
                            
                            // Update LiveData
                            liveData.postValue(foods);
                        });
                    }
                });
    }
    
    /**
     * Thêm food mới
     */
    public void addFood(@NonNull FoodEntity food, @NonNull OnResultListener listener) {
        FoodModel model = new FoodModel();
        model.setCategoryId(String.valueOf(food.getCategoryId()));
        model.setName(food.getName());
        model.setDescription(food.getDescription());
        model.setPrice(food.getPrice());
        model.setImageUrl(food.getImageUrl());
        model.setAvailable(food.isAvailable());
        
        firestore.collection(FirestoreManager.COLLECTION_FOODS)
                .add(model)
                .addOnSuccessListener(docRef -> {
                    food.setFoodId(parseId(docRef.getId()));
                    executor.execute(() -> {
                        foodsDao.insert(food);
                        listener.onSuccess();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding food", e);
                    listener.onError(e.getMessage());
                });
    }
    
    /**
     * Cập nhật food
     */
    public void updateFood(@NonNull FoodEntity food, @NonNull OnResultListener listener) {
        String firestoreId = String.valueOf(food.getFoodId());
        
        FoodModel model = new FoodModel();
        model.setFoodId(firestoreId);
        model.setCategoryId(String.valueOf(food.getCategoryId()));
        model.setName(food.getName());
        model.setDescription(food.getDescription());
        model.setPrice(food.getPrice());
        model.setImageUrl(food.getImageUrl());
        model.setAvailable(food.isAvailable());
        
        firestore.collection(FirestoreManager.COLLECTION_FOODS)
                .document(firestoreId)
                .set(model)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        foodsDao.update(food);
                        listener.onSuccess();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating food", e);
                    listener.onError(e.getMessage());
                });
    }
    
    /**
     * Cleanup listener khi không dùng nữa
     */
    public void removeListener() {
        if (foodsListener != null) {
            foodsListener.remove();
            foodsListener = null;
        }
    }
    
    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return id.hashCode();
        }
    }
    
    public interface OnResultListener {
        void onSuccess();
        void onError(String message);
    }
}
