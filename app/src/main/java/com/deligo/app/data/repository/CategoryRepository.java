package com.deligo.app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.CategoriesDao;
import com.deligo.app.data.local.entity.CategoryEntity;
import com.deligo.app.data.remote.FirestoreManager;
import com.deligo.app.data.remote.model.CategoryModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Hybrid Repository: Firestore (online) + Room (offline cache)
 * 
 * Strategy:
 * 1. Đọc từ Room trước (nhanh, offline)
 * 2. Fetch từ Firestore (cập nhật mới nhất)
 * 3. Sync vào Room để cache
 */
public class CategoryRepository {
    private static final String TAG = "CategoryRepository";
    
    private final CategoriesDao categoriesDao;
    private final FirebaseFirestore firestore;
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    public CategoryRepository(@NonNull DeliGoDatabase database) {
        this.categoriesDao = database.categoriesDao();
        this.firestore = FirestoreManager.getInstance().getDb();
    }
    
    /**
     * Lấy categories với cache-first strategy
     * 1. Trả về cache từ Room ngay lập tức
     * 2. Fetch từ Firestore và update cache
     */
    public LiveData<List<CategoryEntity>> getCategories() {
        MutableLiveData<List<CategoryEntity>> result = new MutableLiveData<>();
        
        executor.execute(() -> {
            // 1. Đọc cache từ Room trước
            List<CategoryEntity> cached = categoriesDao.getAllSync();
            result.postValue(cached);
            
            // 2. Fetch từ Firestore
            syncFromFirestore(result);
        });
        
        return result;
    }
    
    /**
     * Sync categories từ Firestore về Room
     */
    private void syncFromFirestore(MutableLiveData<List<CategoryEntity>> liveData) {
        firestore.collection(FirestoreManager.COLLECTION_CATEGORIES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    executor.execute(() -> {
                        List<CategoryEntity> categories = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            CategoryModel model = doc.toObject(CategoryModel.class);
                            
                            // Convert Firestore model -> Room entity
                            CategoryEntity entity = new CategoryEntity();
                            entity.setCategoryId(parseId(model.getCategoryId()));
                            entity.setCategoryName(model.getCategoryName());
                            
                            categories.add(entity);
                        }
                        
                        // Lưu vào Room cache
                        if (!categories.isEmpty()) {
                            categoriesDao.deleteAll();
                            categoriesDao.insertAll(categories);
                        }
                        
                        // Update LiveData
                        liveData.postValue(categories);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching categories from Firestore", e);
                    // Giữ nguyên cache cũ nếu fetch fail
                });
    }
    
    /**
     * Thêm category mới (ghi vào Firestore + Room)
     */
    public void addCategory(@NonNull String categoryName, 
                           @NonNull OnResultListener listener) {
        CategoryModel model = new CategoryModel(categoryName);
        
        firestore.collection(FirestoreManager.COLLECTION_CATEGORIES)
                .add(model)
                .addOnSuccessListener(docRef -> {
                    executor.execute(() -> {
                        // Lưu vào Room cache
                        CategoryEntity entity = new CategoryEntity();
                        entity.setCategoryId(parseId(docRef.getId()));
                        entity.setCategoryName(categoryName);
                        
                        long localId = categoriesDao.insert(entity);
                        listener.onSuccess();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding category", e);
                    listener.onError(e.getMessage());
                });
    }
    
    /**
     * Xóa category (xóa từ Firestore + Room)
     */
    public void deleteCategory(long categoryId, @NonNull OnResultListener listener) {
        String firestoreId = String.valueOf(categoryId);
        
        firestore.collection(FirestoreManager.COLLECTION_CATEGORIES)
                .document(firestoreId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        categoriesDao.deleteById(categoryId);
                        listener.onSuccess();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting category", e);
                    listener.onError(e.getMessage());
                });
    }
    
    // Helper: parse Firestore ID to long
    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return id.hashCode(); // Fallback: use hashcode
        }
    }
    
    // Callback interface
    public interface OnResultListener {
        void onSuccess();
        void onError(String message);
    }
}
