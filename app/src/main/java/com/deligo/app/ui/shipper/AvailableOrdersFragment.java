package com.deligo.app.ui.shipper;

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

import java.util.List;

public class AvailableOrdersFragment extends Fragment implements AvailableOrdersAdapter.OnAcceptDeliveryListener {

    private static final String ARG_SHIPPER_ID = "arg_shipper_id";
    public static final String TAG = "AvailableOrdersFragment";

    private long shipperId = -1L;
    private AvailableOrdersViewModel viewModel;
    private AvailableOrdersAdapter adapter;
    private TextView emptyView;

    public static AvailableOrdersFragment newInstance(long shipperId) {
        AvailableOrdersFragment fragment = new AvailableOrdersFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SHIPPER_ID, shipperId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_available_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            shipperId = getArguments().getLong(ARG_SHIPPER_ID, -1L);
        }
        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AvailableOrdersViewModel.class);
        setupRecyclerView(view);
        observeOrders();
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAvailableOrders);
        emptyView = view.findViewById(R.id.textAvailableOrdersEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AvailableOrdersAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void observeOrders() {
        viewModel.getAvailableOrders().observe(getViewLifecycleOwner(), this::handleOrdersChanged);
    }

    private void handleOrdersChanged(List<OrderEntity> orders) {
        if (adapter != null) {
            adapter.submitList(orders);
        }
        toggleEmptyState(orders);
    }

    private void toggleEmptyState(@Nullable List<OrderEntity> orders) {
        if (emptyView == null) {
            return;
        }
        boolean isEmpty = orders == null || orders.isEmpty();
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAcceptDelivery(@NonNull OrderEntity order) {
        if (shipperId < 0) {
            Toast.makeText(requireContext(), R.string.shipper_id_missing_message, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.acceptOrder(order, shipperId);
        Toast.makeText(requireContext(), R.string.accept_delivery_success, Toast.LENGTH_SHORT).show();
    }
}
