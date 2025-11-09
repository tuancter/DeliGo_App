package com.deligo.app.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.CartDao;
import com.deligo.app.data.local.dao.CartItemsDao;
import com.deligo.app.data.local.dao.OrdersDao;
import com.deligo.app.data.local.entity.CartEntity;
import com.deligo.app.data.local.entity.CartItemEntity;
import com.deligo.app.data.local.entity.OrderDetailEntity;
import com.deligo.app.data.local.entity.OrderEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckoutViewModel extends AndroidViewModel {

    private final OrdersDao ordersDao;
    private final CartDao cartDao;
    private final CartItemsDao cartItemsDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Boolean> placingOrder = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Long> createdOrderId = new MutableLiveData<>();

    public CheckoutViewModel(@NonNull Application application) {
        super(application);
        DeliGoDatabase db = DeliGoDatabase.getInstance(application);
        ordersDao = db.ordersDao();
        cartDao = db.cartDao();
        cartItemsDao = db.cartItemsDao();
    }

    public LiveData<Boolean> getPlacingOrder() { return placingOrder; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Long> getCreatedOrderId() { return createdOrderId; }

    public void placeOrder(long userId, String address, String phone, String paymentMethod, String note) {
        placingOrder.setValue(true);
        io.execute(() -> {
            try {
                CartEntity cart = cartDao.getCartForUserSync(userId);
                if (cart == null) {
                    message.postValue("Không tìm thấy giỏ hàng.");
                    placingOrder.postValue(false);
                    return;
                }
                List<CartItemEntity> items = cartItemsDao.getItemsForCartSync(cart.getCartId());
                if (items == null || items.isEmpty()) {
                    message.postValue("Giỏ hàng trống.");
                    placingOrder.postValue(false);
                    return;
                }
                double total = 0d;
                List<OrderDetailEntity> details = new ArrayList<>();
                for (CartItemEntity ci : items) {
                    total += ci.getPrice() * ci.getQuantity();
                    OrderDetailEntity d = new OrderDetailEntity();
                    d.setFoodId(ci.getFoodId());
                    d.setQuantity(ci.getQuantity());
                    d.setUnitPrice(ci.getPrice());
                    details.add(d);
                }
                OrderEntity order = new OrderEntity();
                order.setCustomerId(userId);
                order.setDeliveryAddress(address);
                order.setPhone(phone);
                order.setPaymentMethod(paymentMethod);
                order.setPaymentStatus("Chưa thanh toán");
                order.setOrderStatus("Chờ xác nhận");
                order.setTotalAmount(total);
                order.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                long orderId = ordersDao.insertOrderWithDetails(order, details);
                cartItemsDao.clearCart(cart.getCartId());

                createdOrderId.postValue(orderId);
                message.postValue("Đặt hàng thành công");
            } catch (Exception e) {
                message.postValue("Không thể tạo đơn hàng, vui lòng thử lại sau.");
            } finally {
                placingOrder.postValue(false);
            }
        });
    }
}
