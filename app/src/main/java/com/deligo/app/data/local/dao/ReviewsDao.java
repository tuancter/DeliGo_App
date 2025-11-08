package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deligo.app.data.local.entity.ReviewEntity;

import java.util.List;

@Dao
public interface ReviewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReviewEntity review);

    @Update
    int update(ReviewEntity review);

    @Delete
    int delete(ReviewEntity review);

    @Query("SELECT * FROM Reviews WHERE FoodID = :foodId ORDER BY CreatedAt DESC")
    LiveData<List<ReviewEntity>> getReviewsForFood(long foodId);

    @Query("SELECT AVG(Rating) FROM Reviews WHERE FoodID = :foodId")
    LiveData<Double> getAverageRatingForFood(long foodId);
}
