package com.deligo.app.ui.shipper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.OrdersDao;
import com.deligo.app.data.local.entity.OrderEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AvailableOrdersViewModel extends AndroidViewModel {

    private final OrdersDao ordersDao;
    private final LiveData<List<OrderEntity>> availableOrders;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AvailableOrdersViewModel(@NonNull Application application) {
        super(application);
        ordersDao = DeliGoDatabase.getInstance(application).ordersDao();
        availableOrders = ordersDao.getOrdersByStatus(ShipperOrderStatus.READY_FOR_DELIVERY);
    }

    public LiveData<List<OrderEntity>> getAvailableOrders() {
        return availableOrders;
    }

    public void acceptOrder(@NonNull final OrderEntity order, final long shipperId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                OrderEntity updatedOrder = copyOrder(order);
                updatedOrder.setShipperId(shipperId);
                updatedOrder.setOrderStatus(ShipperOrderStatus.IN_DELIVERY);
                ordersDao.updateOrder(updatedOrder);
            }
        });
    }

    @NonNull
    private OrderEntity copyOrder(@NonNull OrderEntity source) {
        OrderEntity copy = new OrderEntity();
        copy.setOrderId(source.getOrderId());
        copy.setCustomerId(source.getCustomerId());
        copy.setShipperId(source.getShipperId());
        copy.setDeliveryAddress(source.getDeliveryAddress());
        copy.setTotalAmount(source.getTotalAmount());
        copy.setPaymentMethod(source.getPaymentMethod());
        copy.setPaymentStatus(source.getPaymentStatus());
        copy.setOrderStatus(source.getOrderStatus());
        copy.setNote(source.getNote());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setPhone(source.getPhone());
        return copy;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
