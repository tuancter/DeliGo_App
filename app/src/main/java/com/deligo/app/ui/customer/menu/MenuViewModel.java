package com.deligo.app.ui.customer.menu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.CartDao;
import com.deligo.app.data.local.dao.CartItemsDao;
import com.deligo.app.data.local.dao.FoodsDao;
import com.deligo.app.data.local.entity.CartEntity;
import com.deligo.app.data.local.entity.CartItemEntity;
import com.deligo.app.data.local.entity.FoodEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MenuViewModel extends AndroidViewModel {

    private final FoodsDao foodsDao;
    private final CartDao cartDao;
    private final CartItemsDao cartItemsDao;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> addToCartMessage = new MutableLiveData<>();

    public MenuViewModel(@NonNull Application application) {
        super(application);
        DeliGoDatabase database = DeliGoDatabase.getInstance(application);
        foodsDao = database.foodsDao();
        cartDao = database.cartDao();
        cartItemsDao = database.cartItemsDao();
    }

    public LiveData<List<FoodEntity>> getFoods() {
        return foodsDao.getAllFoods();
    }

    public LiveData<String> getAddToCartMessage() {
        return addToCartMessage;
    }

    public void addFoodToCart(final long userId, final FoodEntity food) {
        if (food == null) {
            addToCartMessage.setValue("Không thể thêm món này vào giỏ hàng");
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                CartEntity cart = cartDao.getCartForUserSync(userId);
                if (cart == null) {
                    cart = createCart(userId);
                    long cartId = cartDao.insert(cart);
                    cart.setCartId(cartId);
                }

                CartItemEntity existingItem = cartItemsDao.getCartItemSync(cart.getCartId(), food.getFoodId());

                if (existingItem == null) {
                    CartItemEntity cartItem = new CartItemEntity();
                    cartItem.setCartId(cart.getCartId());
                    cartItem.setFoodId(food.getFoodId());
                    cartItem.setQuantity(1);
                    cartItem.setPrice(food.getPrice());
                    cartItemsDao.insert(cartItem);
                } else {
                    existingItem.setQuantity(existingItem.getQuantity() + 1);
                    existingItem.setPrice(food.getPrice());
                    cartItemsDao.update(existingItem);
                }

                addToCartMessage.postValue("Đã thêm " + food.getName() + " vào giỏ hàng");
            }
        });
    }

    private CartEntity createCart(long userId) {
        CartEntity cart = new CartEntity();
        cart.setUserId(userId);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        cart.setCreatedAt(timestamp);
        cart.setUpdatedAt(timestamp);
        return cart;
    }
}
