package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deligo.app.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CategoryEntity category);

    @Update
    int update(CategoryEntity category);

    @Query("SELECT * FROM Categories ORDER BY CategoryName ASC")
    LiveData<List<CategoryEntity>> getAllCategories();
}
