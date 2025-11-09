package com.deligo.app.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.UsersDao;
import com.deligo.app.data.local.entity.UserEntity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository wrapper for Users operations.
 */
public class UserRepository {

    private final UsersDao usersDao;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    public UserRepository(@NonNull DeliGoDatabase database) {
        this.usersDao = database.usersDao();
    }

    public LiveData<UserEntity> getUserLive(long userId) {
        return usersDao.getUserLive(userId);
    }

    public void updateUser(@NonNull final UserEntity user,
                           @Nullable final Runnable onSuccess,
                           @Nullable final Runnable onError) {
        ioExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    usersDao.update(user);
                    if (onSuccess != null) onSuccess.run();
                } catch (Exception e) {
                    if (onError != null) onError.run();
                }
            }
        });
    }

    @Nullable
    public UserEntity getUserSync(long userId) {
        try {
            return usersDao.getUserByIdSync(userId);
        } catch (Exception e) {
            return null;
        }
    }
}
