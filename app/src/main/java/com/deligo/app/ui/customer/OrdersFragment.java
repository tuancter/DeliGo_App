package com.deligo.app.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.ui.customer.orders.OrderAdapter;
import com.deligo.app.ui.customer.orders.OrderViewModel;

public class OrdersFragment extends Fragment {

    private static final long DEFAULT_USER_ID = 1L;

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupRecyclerView(view);
        observeOrders(view);
    }

    private void setupViewModel() {
        orderViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(OrderViewModel.class);
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter();
        recyclerView.setAdapter(orderAdapter);
    }

    private void observeOrders(@NonNull View view) {
        final TextView textEmpty = view.findViewById(R.id.textEmptyOrders);
        orderViewModel.getOrdersByCustomer(DEFAULT_USER_ID)
                .observe(getViewLifecycleOwner(), orderEntities -> {
                    orderAdapter.submitList(orderEntities);
                    boolean empty = orderEntities == null || orderEntities.isEmpty();
                    textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                });
    }
}
