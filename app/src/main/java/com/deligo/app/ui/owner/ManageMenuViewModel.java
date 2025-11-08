package com.deligo.app.ui.owner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.FoodsDao;
import com.deligo.app.data.local.entity.FoodEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageMenuViewModel extends AndroidViewModel {

    private final FoodsDao foodsDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ManageMenuViewModel(@NonNull Application application) {
        super(application);
        foodsDao = DeliGoDatabase.getInstance(application).foodsDao();
    }

    public LiveData<List<FoodEntity>> getFoods() {
        return foodsDao.getAllFoods();
    }

    public void insertFood(final FoodEntity food) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                foodsDao.insert(food);
            }
        });
    }

    public void updateFood(final FoodEntity food) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                foodsDao.update(food);
            }
        });
    }

    public void deleteFood(final FoodEntity food) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                foodsDao.delete(food);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
