package com.deligo.app.ui.shipper;

import android.content.Intent;
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

public class MyDeliveriesFragment extends Fragment implements MyDeliveriesAdapter.OnDeliveryClickListener {

    private static final String ARG_SHIPPER_ID = "arg_shipper_id";
    public static final String TAG = "MyDeliveriesFragment";

    private long shipperId = -1L;
    private MyDeliveriesViewModel viewModel;
    private MyDeliveriesAdapter adapter;
    private TextView emptyView;

    public static MyDeliveriesFragment newInstance(long shipperId) {
        MyDeliveriesFragment fragment = new MyDeliveriesFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SHIPPER_ID, shipperId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_deliveries, container, false);
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
                .get(MyDeliveriesViewModel.class);
        setupRecyclerView(view);
        observeDeliveries();
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewMyDeliveries);
        emptyView = view.findViewById(R.id.textMyDeliveriesEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyDeliveriesAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void observeDeliveries() {
        viewModel.getDeliveriesForShipper(shipperId).observe(getViewLifecycleOwner(), this::handleDeliveriesChanged);
    }

    private void handleDeliveriesChanged(@Nullable List<OrderEntity> deliveries) {
        if (adapter != null) {
            adapter.submitList(deliveries);
        }
        toggleEmptyState(deliveries);
    }

    private void toggleEmptyState(@Nullable List<OrderEntity> deliveries) {
        if (emptyView == null) {
            return;
        }
        boolean isEmpty = deliveries == null || deliveries.isEmpty();
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDeliveryClicked(@NonNull OrderEntity order) {
        if (shipperId < 0) {
            Toast.makeText(requireContext(), R.string.shipper_id_missing_message, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = DeliveryDetailActivity.newIntent(requireContext(), order);
        startActivity(intent);
    }
}
