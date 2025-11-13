package com.deligo.app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.OrdersDao;
import com.deligo.app.data.local.entity.OrderEntity;
import com.deligo.app.data.remote.FirestoreManager;
import com.deligo.app.data.remote.model.OrderModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Order Repository - Realtime sync cho đơn hàng
 */
public class OrderRepository {
    private static final String TAG = "OrderRepository";
    
    private final OrdersDao ordersDao;
    private final FirebaseFirestore firestore;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private ListenerRegistration ordersListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    
    public OrderRepository(@NonNull DeliGoDatabase database) {
        this.ordersDao = database.ordersDao();
        this.firestore = FirestoreManager.getInstance().getDb();
    }
    
    /**
     * Lấy orders của customer với realtime updates
     */
    public LiveData<List<OrderEntity>> getCustomerOrders(long customerId) {
        MutableLiveData<List<OrderEntity>> result = new MutableLiveData<>();
        
        executor.execute(() -> {
            // Load cache
            List<OrderEntity> cached = ordersDao.getOrdersByCustomerSync(customerId);
            result.postValue(cached);
            
            // Setup realtime listener
            setupCustomerOrdersListener(customerId, result);
        });
        
        return result;
    }
    
    /**
     * Lấy tất cả orders (cho Owner/Admin) với realtime
     */
    public LiveData<List<OrderEntity>> getAllOrders() {
        MutableLiveData<List<OrderEntity>> result = new MutableLiveData<>();
        
        executor.execute(() -> {
            List<OrderEntity> cached = ordersDao.getAllOrdersSync();
            result.postValue(cached);
            
            setupAllOrdersListener(result);
        });
        
        return result;
    }
    
    private void setupCustomerOrdersListener(long customerId, MutableLiveData<List<OrderEntity>> liveData) {
        String customerIdStr = String.valueOf(customerId);
        
        ordersListener = firestore.collection(FirestoreManager.COLLECTION_ORDERS)
                .whereEqualTo("customerId", customerIdStr)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }
                    
                    if (snapshots != null) {
                        executor.execute(() -> {
                            List<OrderEntity> orders = convertToEntities(snapshots);
                            ordersDao.insertAll(orders);
                            liveData.postValue(orders);
                        });
                    }
                });
    }
    
    private void setupAllOrdersListener(MutableLiveData<List<OrderEntity>> liveData) {
        ordersListener = firestore.collection(FirestoreManager.COLLECTION_ORDERS)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }
                    
                    if (snapshots != null) {
                        executor.execute(() -> {
                            List<OrderEntity> orders = convertToEntities(snapshots);
                            ordersDao.insertAll(orders);
                            liveData.postValue(orders);
                        });
                    }
                });
    }
    
    private List<OrderEntity> convertToEntities(Iterable<QueryDocumentSnapshot> snapshots) {
        List<OrderEntity> orders = new ArrayList<>();
        
        for (QueryDocumentSnapshot doc : snapshots) {
            OrderModel model = doc.toObject(OrderModel.class);
            
            OrderEntity entity = new OrderEntity();
            entity.setOrderId(parseId(model.getOrderId()));
            entity.setCustomerId(parseId(model.getCustomerId()));
            entity.setShipperId(model.getShipperId() != null ? parseId(model.getShipperId()) : null);
            entity.setDeliveryAddress(model.getDeliveryAddress());
            entity.setTotalAmount(model.getTotalAmount());
            entity.setPaymentMethod(model.getPaymentMethod());
            entity.setPaymentStatus(model.getPaymentStatus());
            entity.setOrderStatus(model.getOrderStatus());
            entity.setNote(model.getNote());
            
            if (model.getCreatedAt() != null) {
                entity.setCreatedAt(dateFormat.format(model.getCreatedAt()));
            }
            
            orders.add(entity);
        }
        
        return orders;
    }
    
    /**
     * Tạo order mới
     */
    public void createOrder(@NonNull OrderEntity order, @NonNull OnResultListener listener) {
        OrderModel model = new OrderModel();
        model.setCustomerId(String.valueOf(order.getCustomerId()));
        model.setDeliveryAddress(order.getDeliveryAddress());
        model.setTotalAmount(order.getTotalAmount());
        model.setPaymentMethod(order.getPaymentMethod());
        model.setPaymentStatus(order.getPaymentStatus());
        model.setOrderStatus(order.getOrderStatus());
        model.setNote(order.getNote());
        
        firestore.collection(FirestoreManager.COLLECTION_ORDERS)
                .add(model)
                .addOnSuccessListener(docRef -> {
                    order.setOrderId(parseId(docRef.getId()));
                    executor.execute(() -> {
                        long localId = ordersDao.insertOrder(order);
                        listener.onSuccess(localId);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating order", e);
                    listener.onError(e.getMessage());
                });
    }
    
    /**
     * Cập nhật trạng thái order (quan trọng cho realtime)
     */
    public void updateOrderStatus(long orderId, @NonNull String newStatus, @NonNull OnResultListener listener) {
        String firestoreId = String.valueOf(orderId);
        
        firestore.collection(FirestoreManager.COLLECTION_ORDERS)
                .document(firestoreId)
                .update("orderStatus", newStatus)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        ordersDao.updateOrderStatus(orderId, newStatus);
                        listener.onSuccess(orderId);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating order status", e);
                    listener.onError(e.getMessage());
                });
    }
    
    public void removeListener() {
        if (ordersListener != null) {
            ordersListener.remove();
            ordersListener = null;
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
        void onSuccess(long orderId);
        void onError(String message);
    }
}
