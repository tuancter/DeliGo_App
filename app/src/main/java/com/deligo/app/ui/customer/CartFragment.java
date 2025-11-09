package com.deligo.app.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.ui.customer.cart.CartAdapter;
import com.deligo.app.ui.customer.cart.CartViewModel;

public class CartFragment extends Fragment {

    private static final long DEFAULT_USER_ID = 1L;

    private CartViewModel viewModel;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupRecycler(view);
        setupButtons(view);
        observeData(view);
        viewModel.checkUnavailableItems();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(CartViewModel.class);
        viewModel.init(DEFAULT_USER_ID);
    }

    private void setupRecycler(@NonNull View view) {
        RecyclerView rv = view.findViewById(R.id.recyclerViewCart);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(new CartAdapter.Listener() {
            @Override
            public void onIncreaseQty(com.deligo.app.data.local.model.CartItemWithFood item) {
                viewModel.increaseQuantity(item);
            }

            @Override
            public void onDecreaseQty(com.deligo.app.data.local.model.CartItemWithFood item) {
                viewModel.decreaseQuantity(item);
            }

            @Override
            public void onRemove(com.deligo.app.data.local.model.CartItemWithFood item) {
                viewModel.removeItem(item);
            }
        });
        rv.setAdapter(adapter);
    }

    private void setupButtons(@NonNull View view) {
        Button btnContinue = view.findViewById(R.id.buttonContinueShopping);
        Button btnCheckout = view.findViewById(R.id.buttonCheckout);
        btnContinue.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new MenuFragment())
                .commit());
        btnCheckout.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new CheckoutFragment())
                .addToBackStack(null)
                .commit());
    }

    private void observeData(@NonNull View view) {
        final TextView textEmpty = view.findViewById(R.id.textEmptyCart);
        final TextView textTotal = view.findViewById(R.id.textTotal);
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean empty = items == null || items.isEmpty();
            textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            String s = "Tổng: " + viewModel.formatCurrency(total == null ? 0 : total);
            textTotal.setText(s);
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getContext() != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
