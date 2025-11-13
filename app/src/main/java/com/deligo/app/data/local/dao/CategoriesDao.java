package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
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

    @Delete
    int delete(CategoryEntity category);

    @Query("SELECT * FROM Categories ORDER BY CategoryName ASC")
    LiveData<List<CategoryEntity>> getAllCategories();

    @Query("SELECT * FROM Categories ORDER BY CategoryName ASC")
    List<CategoryEntity> getAllSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CategoryEntity> categories);

    @Query("DELETE FROM Categories")
    void deleteAll();

    @Query("DELETE FROM Categories WHERE CategoryID = :categoryId")
    void deleteById(long categoryId);

    @Query("SELECT COUNT(*) FROM Categories")
    long count();
}
