package com.deligo.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deligo.app.data.local.entity.CartItemEntity;
import com.deligo.app.data.local.model.CartItemWithFood;

import java.util.List;

@Dao
public interface CartItemsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CartItemEntity cartItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CartItemEntity> cartItems);

    @Update
    int update(CartItemEntity cartItem);

    @Delete
    int delete(CartItemEntity cartItem);

    @Query("SELECT * FROM CartItems WHERE CartID = :cartId")
    LiveData<List<CartItemEntity>> getItemsForCart(long cartId);

    @Query("SELECT * FROM CartItems WHERE CartID = :cartId")
    List<CartItemEntity> getItemsForCartSync(long cartId);

    @Query("SELECT ci.CartItemID AS cartItemId, ci.FoodID AS foodId, f.Name AS foodName, ci.Price AS price, ci.Quantity AS quantity FROM CartItems ci JOIN Foods f ON f.FoodID = ci.FoodID WHERE ci.CartID = :cartId")
    LiveData<List<CartItemWithFood>> getItemsWithFood(long cartId);

    @Query("SELECT * FROM CartItems WHERE CartItemID = :cartItemId LIMIT 1")
    CartItemEntity getCartItemByIdSync(long cartItemId);

    @Query("DELETE FROM CartItems WHERE CartItemID = :cartItemId")
    void deleteById(long cartItemId);

    @Query("DELETE FROM CartItems WHERE CartID = :cartId")
    void clearCart(long cartId);

    @Query("SELECT * FROM CartItems WHERE CartID = :cartId AND FoodID = :foodId LIMIT 1")
    CartItemEntity getCartItemSync(long cartId, long foodId);
}
