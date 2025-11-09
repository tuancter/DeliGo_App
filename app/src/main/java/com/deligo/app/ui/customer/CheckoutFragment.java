package com.deligo.app.ui.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.model.CartItemWithFood;
import com.deligo.app.ui.customer.cart.CartViewModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutFragment extends Fragment {

    private static final long DEFAULT_USER_ID = 1L;

    private CartViewModel cartViewModel;
    private CheckoutViewModel checkoutViewModel;
    private CheckoutItemsAdapter itemsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModels();
        setupRecycler(view);
        setupInputs(view);
        observeData(view);
    }

    private void setupViewModels() {
        ViewModelProvider.AndroidViewModelFactory factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication());
        cartViewModel = new ViewModelProvider(this, factory).get(CartViewModel.class);
        cartViewModel.init(DEFAULT_USER_ID);
        checkoutViewModel = new ViewModelProvider(this, factory).get(CheckoutViewModel.class);
    }

    private void setupRecycler(@NonNull View view) {
        RecyclerView rv = view.findViewById(R.id.recyclerViewCheckoutItems);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        itemsAdapter = new CheckoutItemsAdapter();
        rv.setAdapter(itemsAdapter);
    }

    private void setupInputs(@NonNull View view) {
        Spinner spinner = view.findViewById(R.id.spinnerPaymentMethod);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"Tiền mặt (COD)", "Ví điện tử (Đang phát triển)"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Button btnPlaceOrder = view.findViewById(R.id.buttonPlaceOrder);
        btnPlaceOrder.setOnClickListener(v -> onPlaceOrderClicked());
    }

    private void observeData(@NonNull View view) {
        final TextView textTotal = view.findViewById(R.id.textCheckoutTotal);
        cartViewModel.getItems().observe(getViewLifecycleOwner(), items -> itemsAdapter.submit(items));
        cartViewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String s = "Tổng cộng: " + nf.format(total == null ? 0 : total);
            textTotal.setText(s);
        });

        checkoutViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
        checkoutViewModel.getCreatedOrderId().observe(getViewLifecycleOwner(), orderId -> {
            if (orderId != null && orderId > 0) {
                // Navigate to Orders via BottomNavigationView to keep selection in sync
                com.google.android.material.bottomnavigation.BottomNavigationView bnv = requireActivity().findViewById(R.id.bottom_navigation);
                if (bnv != null) {
                    bnv.setSelectedItemId(R.id.navigation_orders);
                } else {
                    // Fallback direct navigation
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new OrdersFragment())
                            .commit();
                }
            }
        });
    }

    private void onPlaceOrderClicked() {
        View view = getView();
        if (view == null) return;
        EditText editAddress = view.findViewById(R.id.editAddress);
        EditText editPhone = view.findViewById(R.id.editPhone);
        Spinner spinner = view.findViewById(R.id.spinnerPaymentMethod);
        EditText editNote = view.findViewById(R.id.editNote);

        String address = editAddress.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String method = (String) spinner.getSelectedItem();
        String note = editNote.getText().toString().trim();

        if (TextUtils.isEmpty(address)) {
            editAddress.setError("Vui lòng nhập địa chỉ giao hàng");
            return;
        }
        if (spinner.getSelectedItemPosition() != 0) {
            Toast.makeText(getContext(), "Tính năng đang phát triển.", Toast.LENGTH_SHORT).show();
            return;
        }
        checkoutViewModel.placeOrder(DEFAULT_USER_ID, address, phone, "COD", note);
    }

    // Simple adapter for checkout list
    static class CheckoutItemsAdapter extends RecyclerView.Adapter<CheckoutItemsAdapter.VH> {
        private List<CartItemWithFood> data;
        private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        void submit(List<CartItemWithFood> items) {
            this.data = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_cart_item, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            CartItemWithFood item = data.get(position);
            holder.textName.setText(item.foodName);
            holder.textQty.setText("x" + item.quantity);
            holder.textSubtotal.setText(currency.format(item.getSubtotal()));
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView textName, textQty, textSubtotal;
            VH(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textCheckoutItemName);
                textQty = itemView.findViewById(R.id.textCheckoutItemQty);
                textSubtotal = itemView.findViewById(R.id.textCheckoutItemSubtotal);
            }
        }
    }
}
