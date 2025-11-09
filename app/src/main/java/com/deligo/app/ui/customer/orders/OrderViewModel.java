package com.deligo.app.ui.customer.orders;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.OrdersDao;
import com.deligo.app.data.local.entity.OrderEntity;

import java.util.List;

public class OrderViewModel extends AndroidViewModel {

    private final OrdersDao ordersDao;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        DeliGoDatabase database = DeliGoDatabase.getInstance(application);
        ordersDao = database.ordersDao();
    }

    public LiveData<List<OrderEntity>> getOrdersByCustomer(long customerId) {
        return ordersDao.getOrdersByCustomerId(customerId);
    }
}
