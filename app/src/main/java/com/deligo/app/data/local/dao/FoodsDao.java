package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deligo.app.data.local.entity.FoodEntity;

import java.util.List;

@Dao
public interface FoodsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FoodEntity food);

    @Update
    int update(FoodEntity food);

    @Delete
    int delete(FoodEntity food);

    @Query("SELECT * FROM Foods ORDER BY Name ASC")
    LiveData<List<FoodEntity>> getAllFoods();

    @Query("SELECT * FROM Foods WHERE CategoryID = :categoryId ORDER BY Name ASC")
    LiveData<List<FoodEntity>> getFoodsByCategory(long categoryId);

    @Query("SELECT * FROM Foods WHERE FoodID = :foodId LIMIT 1")
    FoodEntity getFoodSync(long foodId);
}
