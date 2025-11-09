package com.deligo.app.ui.owner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.CategoriesDao;
import com.deligo.app.data.local.entity.CategoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageCategoriesViewModel extends AndroidViewModel {

    private final CategoriesDao categoriesDao;
    private final LiveData<List<CategoryEntity>> categories;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ManageCategoriesViewModel(@NonNull Application application) {
        super(application);
        DeliGoDatabase database = DeliGoDatabase.getInstance(application);
        categoriesDao = database.categoriesDao();
        categories = categoriesDao.getAllCategories();
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public void insertCategory(final CategoryEntity category) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                categoriesDao.insert(category);
            }
        });
    }

    public void updateCategory(final CategoryEntity category) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                categoriesDao.update(category);
            }
        });
    }

    public void deleteCategory(final CategoryEntity category) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                categoriesDao.delete(category);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
