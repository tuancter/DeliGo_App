package com.deligo.app.ui.customer.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.CartDao;
import com.deligo.app.data.local.dao.CartItemsDao;
import com.deligo.app.data.local.dao.FoodsDao;
import com.deligo.app.data.local.entity.CartEntity;
import com.deligo.app.data.local.model.CartItemWithFood;
import com.deligo.app.data.repository.CartRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;
    private final CartDao cartDao;
    private final MediatorLiveData<List<CartItemWithFood>> itemsLiveData = new MediatorLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private LiveData<CartEntity> cartLiveData;
    private LiveData<Double> totalAmountLiveData;

    private long currentUserId;

    public CartViewModel(@NonNull Application application) {
        super(application);
        DeliGoDatabase db = DeliGoDatabase.getInstance(application);
        CartItemsDao cartItemsDao = db.cartItemsDao();
        cartDao = db.cartDao();
        FoodsDao foodsDao = db.foodsDao();
        repository = new CartRepository(cartDao, cartItemsDao, foodsDao);
    }

    public void init(long userId) {
        if (this.currentUserId == userId && cartLiveData != null) return;
        this.currentUserId = userId;
        cartLiveData = repository.getCartForUser(userId);
        itemsLiveData.addSource(cartLiveData, cart -> {
            if (cart == null) {
                itemsLiveData.setValue(Collections.emptyList());
            } else {
                LiveData<List<CartItemWithFood>> src = repository.getItemsWithFood(cart.getCartId());
                itemsLiveData.addSource(src, itemsLiveData::setValue);
            }
        });
        totalAmountLiveData = Transformations.map(itemsLiveData, items -> {
            if (items == null) return 0d;
            double total = 0d;
            for (CartItemWithFood i : items) total += i.getSubtotal();
            return total;
        });
    }

    public LiveData<CartEntity> getCart() { return cartLiveData; }

    public LiveData<List<CartItemWithFood>> getItems() { return itemsLiveData; }

    public LiveData<Double> getTotalAmount() { return totalAmountLiveData; }

    public LiveData<String> getMessage() { return messageLiveData; }

    public void increaseQuantity(@NonNull CartItemWithFood item) {
        repository.updateQuantity(item.cartItemId, item.quantity + 1);
    }

    public void decreaseQuantity(@NonNull CartItemWithFood item) {
        int newQty = item.quantity - 1;
        repository.updateQuantity(item.cartItemId, newQty);
    }

    public void removeItem(@NonNull CartItemWithFood item) {
        repository.removeItem(item.cartItemId);
    }

    public void clearCart() {
        CartEntity cart = cartLiveData != null ? cartLiveData.getValue() : null;
        if (cart != null) repository.clearCart(cart.getCartId());
    }

    public void checkUnavailableItems() {
        CartEntity cart = cartLiveData != null ? cartLiveData.getValue() : null;
        if (cart != null) {
            repository.purgeUnavailableItems(cart.getCartId(), () -> messageLiveData.postValue("Một số món đã hết hàng và được xóa khỏi giỏ hàng."));
        }
    }

    public String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}
