package com.deligo.app.ui.owner;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.OrderEntity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProcessOrdersAdapter extends RecyclerView.Adapter<ProcessOrdersAdapter.OrderViewHolder> {

    public interface OnOrderActionListener {
        void onConfirmOrder(@NonNull OrderEntity order);

        void onCancelOrder(@NonNull OrderEntity order);

        void onReadyForPickup(@NonNull OrderEntity order);
    }

    private final List<OrderEntity> orders = new ArrayList<>();
    private final OnOrderActionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

    public ProcessOrdersAdapter(@NonNull OnOrderActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_process_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        final OrderEntity order = orders.get(position);
        holder.orderIdTextView.setText(holder.itemView.getContext().getString(R.string.order_id_format, order.getOrderId()));
        holder.customerIdTextView.setText(holder.itemView.getContext().getString(R.string.customer_id_format, order.getCustomerId()));
        String formattedTotal = currencyFormat.format(order.getTotalAmount());
        holder.totalAmountTextView.setText(holder.itemView.getContext().getString(R.string.total_amount_format, formattedTotal));

        String address = order.getDeliveryAddress();
        if (TextUtils.isEmpty(address)) {
            holder.addressTextView.setVisibility(View.GONE);
        } else {
            holder.addressTextView.setVisibility(View.VISIBLE);
            holder.addressTextView.setText(holder.itemView.getContext().getString(R.string.delivery_address_format, address));
        }

        String paymentMethod = order.getPaymentMethod();
        if (TextUtils.isEmpty(paymentMethod)) {
            holder.paymentMethodTextView.setVisibility(View.GONE);
        } else {
            holder.paymentMethodTextView.setVisibility(View.VISIBLE);
            holder.paymentMethodTextView.setText(holder.itemView.getContext().getString(R.string.payment_method_format, paymentMethod));
        }

        holder.statusTextView.setText(holder.itemView.getContext().getString(R.string.order_status_format, order.getOrderStatus()));

        String createdAt = order.getCreatedAt();
        if (TextUtils.isEmpty(createdAt)) {
            holder.createdAtTextView.setVisibility(View.GONE);
        } else {
            holder.createdAtTextView.setVisibility(View.VISIBLE);
            holder.createdAtTextView.setText(holder.itemView.getContext().getString(R.string.order_created_at_format, createdAt));
        }

        holder.confirmButton.setVisibility(ProcessOrdersViewModel.STATUS_PENDING_CONFIRMATION.equals(order.getOrderStatus())
                ? View.VISIBLE : View.GONE);
        holder.cancelButton.setVisibility(ProcessOrdersViewModel.STATUS_PENDING_CONFIRMATION.equals(order.getOrderStatus())
                ? View.VISIBLE : View.GONE);
        holder.readyButton.setVisibility(ProcessOrdersViewModel.STATUS_PREPARING.equals(order.getOrderStatus())
                ? View.VISIBLE : View.GONE);

        holder.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onConfirmOrder(order);
            }
        });
        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancelOrder(order);
            }
        });
        holder.readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReadyForPickup(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void submitList(@Nullable List<OrderEntity> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        final TextView orderIdTextView;
        final TextView customerIdTextView;
        final TextView totalAmountTextView;
        final TextView addressTextView;
        final TextView paymentMethodTextView;
        final TextView statusTextView;
        final TextView createdAtTextView;
        final Button confirmButton;
        final Button cancelButton;
        final Button readyButton;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.text_order_id);
            customerIdTextView = itemView.findViewById(R.id.text_customer_id);
            totalAmountTextView = itemView.findViewById(R.id.text_total_amount);
            addressTextView = itemView.findViewById(R.id.text_delivery_address);
            paymentMethodTextView = itemView.findViewById(R.id.text_payment_method);
            statusTextView = itemView.findViewById(R.id.text_order_status);
            createdAtTextView = itemView.findViewById(R.id.text_created_at);
            confirmButton = itemView.findViewById(R.id.button_confirm_order);
            cancelButton = itemView.findViewById(R.id.button_cancel_order);
            readyButton = itemView.findViewById(R.id.button_ready_for_pickup);
        }
    }
}
