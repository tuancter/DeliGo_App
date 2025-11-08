package com.deligo.app.ui.owner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.OrderEntity;

import java.util.ArrayList;
import java.util.List;

public class ProcessOrdersFragment extends Fragment implements ProcessOrdersAdapter.OnOrderActionListener {

    private ProcessOrdersViewModel viewModel;
    private ProcessOrdersAdapter adapter;
    private TextView emptyView;
    private final List<OrderEntity> pendingOrders = new ArrayList<>();
    private final List<OrderEntity> preparingOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_process_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_orders);
        emptyView = view.findViewById(R.id.text_empty_orders);

        adapter = new ProcessOrdersAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ProcessOrdersViewModel.class);

        viewModel.getPendingOrders().observe(getViewLifecycleOwner(), orders -> {
            pendingOrders.clear();
            if (orders != null) {
                pendingOrders.addAll(orders);
            }
            updateOrders();
        });

        viewModel.getPreparingOrders().observe(getViewLifecycleOwner(), orders -> {
            preparingOrders.clear();
            if (orders != null) {
                preparingOrders.addAll(orders);
            }
            updateOrders();
        });
    }

    private void updateOrders() {
        List<OrderEntity> combined = new ArrayList<>(pendingOrders.size() + preparingOrders.size());
        combined.addAll(pendingOrders);
        combined.addAll(preparingOrders);
        adapter.submitList(combined);
        toggleEmptyState(combined);
    }

    private void toggleEmptyState(@Nullable List<OrderEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfirmOrder(@NonNull OrderEntity order) {
        viewModel.updateOrderStatus(order, ProcessOrdersViewModel.STATUS_PREPARING);
        Toast.makeText(requireContext(), R.string.order_marked_preparing, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelOrder(@NonNull OrderEntity order) {
        viewModel.updateOrderStatus(order, ProcessOrdersViewModel.STATUS_CANCELLED);
        Toast.makeText(requireContext(), R.string.order_marked_cancelled, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReadyForPickup(@NonNull OrderEntity order) {
        viewModel.updateOrderStatus(order, ProcessOrdersViewModel.STATUS_READY_FOR_PICKUP);
        Toast.makeText(requireContext(), R.string.order_marked_ready, Toast.LENGTH_SHORT).show();
    }
}
