package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deligo.app.data.local.entity.CartEntity;

@Dao
public interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CartEntity cart);

    @Update
    int update(CartEntity cart);

    @Delete
    int delete(CartEntity cart);

    @Query("SELECT * FROM Cart WHERE UserID = :userId LIMIT 1")
    LiveData<CartEntity> getCartForUser(long userId);

    @Query("SELECT * FROM Cart WHERE UserID = :userId LIMIT 1")
    CartEntity getCartForUserSync(long userId);
}
