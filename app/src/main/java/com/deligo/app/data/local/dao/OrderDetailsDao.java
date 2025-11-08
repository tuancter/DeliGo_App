package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deligo.app.data.local.entity.OrderDetailEntity;

import java.util.List;

@Dao
public interface OrderDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OrderDetailEntity detail);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OrderDetailEntity> details);

    @Update
    int update(OrderDetailEntity detail);

    @Delete
    int delete(OrderDetailEntity detail);

    @Query("SELECT * FROM OrderDetails WHERE OrderID = :orderId")
    LiveData<List<OrderDetailEntity>> getDetailsForOrder(long orderId);
}
