package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.deligo.app.data.local.entity.OrderDetailEntity;
import com.deligo.app.data.local.entity.OrderEntity;

import java.util.List;

@Dao
public interface OrdersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrder(OrderEntity order);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrderDetails(List<OrderDetailEntity> details);

    @Update
    int updateOrder(OrderEntity order);

    @Query("SELECT * FROM Orders WHERE CustomerID = :customerId ORDER BY CreatedAt DESC")
    LiveData<List<OrderEntity>> getOrdersByCustomerId(long customerId);

    @Query("SELECT * FROM Orders WHERE OrderStatus = :status ORDER BY CreatedAt DESC")
    LiveData<List<OrderEntity>> getOrdersByStatus(String status);

    @Query("SELECT * FROM Orders WHERE ShipperID = :shipperId ORDER BY CreatedAt DESC")
    LiveData<List<OrderEntity>> getOrdersByShipperId(long shipperId);

    @Transaction
    default long insertOrderWithDetails(OrderEntity order, List<OrderDetailEntity> details) {
        long orderId = insertOrder(order);
        if (details != null && !details.isEmpty()) {
            for (OrderDetailEntity detail : details) {
                detail.setOrderId(orderId);
            }
            insertOrderDetails(details);
        }
        return orderId;
    }
}
