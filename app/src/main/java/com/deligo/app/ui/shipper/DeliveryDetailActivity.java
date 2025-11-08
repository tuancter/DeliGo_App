package com.deligo.app.ui.shipper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.OrderEntity;

public class DeliveryDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ORDER = "extra_order";

    @Nullable
    private OrderEntity order;
    private DeliveryDetailViewModel viewModel;

    public static Intent newIntent(@NonNull Context context, @NonNull OrderEntity order) {
        Intent intent = new Intent(context, DeliveryDetailActivity.class);
        intent.putExtra(EXTRA_ORDER, order);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_detail);

        viewModel = new ViewModelProvider(this).get(DeliveryDetailViewModel.class);
        order = extractOrderFromIntent();
        if (order == null) {
            Toast.makeText(this, R.string.delivery_detail_missing_order, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView addressView = findViewById(R.id.textDeliveryDetailAddress);
        TextView phoneView = findViewById(R.id.textDeliveryDetailPhone);
        Button completeButton = findViewById(R.id.buttonCompleteDelivery);

        addressView.setText(order.getDeliveryAddress());
        String phoneText = order.getPhone();
        if (phoneText == null || phoneText.trim().isEmpty()) {
            phoneText = getString(R.string.customer_phone_not_available);
        }
        phoneView.setText(phoneText);

        completeButton.setOnClickListener(view -> {
            if (order != null) {
                viewModel.completeDelivery(order);
                Toast.makeText(this, R.string.complete_delivery_success, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Nullable
    private OrderEntity extractOrderFromIntent() {
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(EXTRA_ORDER)) {
            return null;
        }
        Object extra = intent.getSerializableExtra(EXTRA_ORDER);
        if (extra instanceof OrderEntity) {
            return (OrderEntity) extra;
        }
        return null;
    }
}
