package com.deligo.app.ui.shipper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.OrdersDao;
import com.deligo.app.data.local.entity.OrderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyDeliveriesViewModel extends AndroidViewModel {

    private final OrdersDao ordersDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MyDeliveriesViewModel(@NonNull Application application) {
        super(application);
        ordersDao = DeliGoDatabase.getInstance(application).ordersDao();
    }

    public LiveData<List<OrderEntity>> getDeliveriesForShipper(long shipperId) {
        return Transformations.map(ordersDao.getOrdersByShipperId(shipperId), orders -> {
            if (orders == null) {
                return new ArrayList<>();
            }
            List<OrderEntity> filtered = new ArrayList<>(orders.size());
            for (OrderEntity order : orders) {
                if (ShipperOrderStatus.IN_DELIVERY.equals(order.getOrderStatus())) {
                    filtered.add(order);
                }
            }
            return filtered;
        });
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
