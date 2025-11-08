package com.deligo.app.ui.shipper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.OrdersDao;
import com.deligo.app.data.local.entity.OrderEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeliveryDetailViewModel extends AndroidViewModel {

    private final OrdersDao ordersDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DeliveryDetailViewModel(@NonNull Application application) {
        super(application);
        ordersDao = DeliGoDatabase.getInstance(application).ordersDao();
    }

    public void completeDelivery(@NonNull final OrderEntity order) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                OrderEntity updatedOrder = copyOrder(order);
                updatedOrder.setOrderStatus(ShipperOrderStatus.COMPLETED);
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
