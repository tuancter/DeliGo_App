package com.deligo.app.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.deligo.app.data.local.dao.CartDao;
import com.deligo.app.data.local.dao.CartItemsDao;
import com.deligo.app.data.local.dao.CategoriesDao;
import com.deligo.app.data.local.dao.ComplaintsDao;
import com.deligo.app.data.local.dao.FoodsDao;
import com.deligo.app.data.local.dao.OrderDetailsDao;
import com.deligo.app.data.local.dao.OrdersDao;
import com.deligo.app.data.local.dao.ReviewsDao;
import com.deligo.app.data.local.dao.UsersDao;
import com.deligo.app.data.local.entity.CartEntity;
import com.deligo.app.data.local.entity.CartItemEntity;
import com.deligo.app.data.local.entity.CategoryEntity;
import com.deligo.app.data.local.entity.ComplaintEntity;
import com.deligo.app.data.local.entity.FoodEntity;
import com.deligo.app.data.local.entity.OrderDetailEntity;
import com.deligo.app.data.local.entity.OrderEntity;
import com.deligo.app.data.local.entity.ReviewEntity;
import com.deligo.app.data.local.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                UserEntity.class,
                CategoryEntity.class,
                FoodEntity.class,
                OrderEntity.class,
                OrderDetailEntity.class,
                ReviewEntity.class,
                ComplaintEntity.class,
                CartEntity.class,
                CartItemEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class DeliGoDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "deligo_db";
    private static volatile DeliGoDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public abstract UsersDao usersDao();

    public abstract CategoriesDao categoriesDao();

    public abstract FoodsDao foodsDao();

    public abstract OrdersDao ordersDao();

    public abstract OrderDetailsDao orderDetailsDao();

    public abstract CartDao cartDao();

    public abstract CartItemsDao cartItemsDao();

    public abstract ReviewsDao reviewsDao();

    public abstract ComplaintsDao complaintsDao();

    public static DeliGoDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (DeliGoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DeliGoDatabase.class,
                                    DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            DeliGoDatabase database = INSTANCE;
                                            if (database == null) {
                                                return;
                                            }

                                            UsersDao usersDao = database.usersDao();
                                            UserEntity existingOwner = usersDao.getUserByEmail("admin@deligo.com");
                                            if (existingOwner != null) {
                                                return;
                                            }

                                            UserEntity ownerUser = new UserEntity();
                                            ownerUser.setFullName("Chủ cửa hàng");
                                            ownerUser.setEmail("admin@deligo.com");
                                            ownerUser.setPassword("admin123");
                                            ownerUser.setRole("Owner");
                                            ownerUser.setStatus("Active");
                                            usersDao.insert(ownerUser);
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
