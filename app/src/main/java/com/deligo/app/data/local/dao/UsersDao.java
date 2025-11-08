package com.deligo.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deligo.app.data.local.entity.UserEntity;

@Dao
public interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);

    @Query("SELECT * FROM Users WHERE Email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM Users WHERE Email = :email AND Password = :password LIMIT 1")
    UserEntity checkUser(String email, String password);

    @Update
    int update(UserEntity user);
}
