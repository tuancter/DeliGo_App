package com.deligo.app.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.deligo.app.data.local.dao.CartDao;
import com.deligo.app.data.local.dao.CartItemsDao;
import com.deligo.app.data.local.dao.FoodsDao;
import com.deligo.app.data.local.entity.CartEntity;
import com.deligo.app.data.local.entity.CartItemEntity;
import com.deligo.app.data.local.entity.FoodEntity;
import com.deligo.app.data.local.model.CartItemWithFood;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for cart operations.
 */
public class CartRepository {

    private final CartDao cartDao;
    private final CartItemsDao cartItemsDao;
    private final FoodsDao foodsDao;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    public CartRepository(CartDao cartDao, CartItemsDao cartItemsDao, FoodsDao foodsDao) {
        this.cartDao = cartDao;
        this.cartItemsDao = cartItemsDao;
        this.foodsDao = foodsDao;
    }

    public LiveData<CartEntity> getCartForUser(long userId) {
        return cartDao.getCartForUser(userId);
    }

    public LiveData<List<CartItemWithFood>> getItemsWithFood(long cartId) {
        return cartItemsDao.getItemsWithFood(cartId);
    }

    public void addFoodToCart(long userId, FoodEntity food, @Nullable Runnable onSuccess, @Nullable Runnable onFailure) {
        ioExecutor.execute(() -> {
            try {
                CartEntity cart = cartDao.getCartForUserSync(userId);
                if (cart == null) {
                    cart = createCart(userId);
                    long cartId = cartDao.insert(cart);
                    cart.setCartId(cartId);
                }
                if (food == null || !food.isAvailable()) {
                    if (onFailure != null) onFailure.run();
                    return;
                }
                CartItemEntity existing = cartItemsDao.getCartItemSync(cart.getCartId(), food.getFoodId());
                if (existing == null) {
                    CartItemEntity item = new CartItemEntity();
                    item.setCartId(cart.getCartId());
                    item.setFoodId(food.getFoodId());
                    item.setQuantity(1);
                    item.setPrice(food.getPrice());
                    cartItemsDao.insert(item);
                } else {
                    existing.setQuantity(existing.getQuantity() + 1);
                    existing.setPrice(food.getPrice());
                    cartItemsDao.update(existing);
                }
                if (onSuccess != null) onSuccess.run();
            } catch (Exception e) {
                if (onFailure != null) onFailure.run();
            }
        });
    }

    public void updateQuantity(long cartItemId, int newQuantity) {
        ioExecutor.execute(() -> {
            CartItemEntity item = cartItemsDao.getCartItemByIdSync(cartItemId);
            if (item == null) return;
            if (newQuantity <= 0) {
                cartItemsDao.deleteById(cartItemId);
            } else {
                item.setQuantity(newQuantity);
                cartItemsDao.update(item);
            }
        });
    }

    public void removeItem(long cartItemId) {
        ioExecutor.execute(() -> cartItemsDao.deleteById(cartItemId));
    }

    public void clearCart(long cartId) {
        ioExecutor.execute(() -> cartItemsDao.clearCart(cartId));
    }

    public void purgeUnavailableItems(long cartId, @Nullable Runnable onAnyRemoved) {
        ioExecutor.execute(() -> {
            try {
                List<com.deligo.app.data.local.entity.CartItemEntity> items = cartItemsDao.getItemsForCartSync(cartId);
                if (items == null || items.isEmpty()) return;
                boolean removed = false;
                for (com.deligo.app.data.local.entity.CartItemEntity ci : items) {
                    FoodEntity f = foodsDao.getFoodSync(ci.getFoodId());
                    if (f == null || !f.isAvailable()) {
                        cartItemsDao.deleteById(ci.getCartItemId());
                        removed = true;
                    }
                }
                if (removed && onAnyRemoved != null) onAnyRemoved.run();
            } catch (Exception ignored) {
            }
        });
    }

    private CartEntity createCart(long userId) {
        CartEntity cart = new CartEntity();
        cart.setUserId(userId);
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        cart.setCreatedAt(ts);
        cart.setUpdatedAt(ts);
        return cart;
    }
}
