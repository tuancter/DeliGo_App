package com.deligo.app.ui.owner;

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

public class ProcessOrdersViewModel extends AndroidViewModel {

    public static final String STATUS_PENDING_CONFIRMATION = "Chờ xác nhận";
    public static final String STATUS_PREPARING = "Đang chuẩn bị";
    public static final String STATUS_READY_FOR_PICKUP = "Sẵn sàng giao";
    public static final String STATUS_CANCELLED = "Bị hủy";

    private final OrdersDao ordersDao;
    private final LiveData<List<OrderEntity>> pendingOrders;
    private final LiveData<List<OrderEntity>> preparingOrders;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ProcessOrdersViewModel(@NonNull Application application) {
        super(application);
        ordersDao = DeliGoDatabase.getInstance(application).ordersDao();
        pendingOrders = ordersDao.getOrdersByStatus(STATUS_PENDING_CONFIRMATION);
        preparingOrders = ordersDao.getOrdersByStatus(STATUS_PREPARING);
    }

    public LiveData<List<OrderEntity>> getPendingOrders() {
        return pendingOrders;
    }

    public LiveData<List<OrderEntity>> getPreparingOrders() {
        return preparingOrders;
    }

    public void updateOrderStatus(@NonNull final OrderEntity order, @NonNull final String status) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                OrderEntity updatedOrder = copyOrder(order);
                updatedOrder.setOrderStatus(status);
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
        return copy;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
