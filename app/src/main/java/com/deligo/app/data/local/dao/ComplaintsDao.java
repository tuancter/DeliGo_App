package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deligo.app.data.local.entity.ComplaintEntity;

import java.util.List;

@Dao
public interface ComplaintsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ComplaintEntity complaint);

    @Update
    int update(ComplaintEntity complaint);

    @Delete
    int delete(ComplaintEntity complaint);

    @Query("SELECT * FROM Complaints WHERE UserID = :userId ORDER BY CreatedAt DESC")
    LiveData<List<ComplaintEntity>> getComplaintsByUser(long userId);

    @Query("SELECT * FROM Complaints WHERE Status = :status ORDER BY CreatedAt DESC")
    LiveData<List<ComplaintEntity>> getComplaintsByStatus(String status);
}
