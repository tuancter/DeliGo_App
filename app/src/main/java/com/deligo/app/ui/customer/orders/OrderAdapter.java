package com.deligo.app.ui.customer.orders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.OrderEntity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<OrderEntity> orders = new ArrayList<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderEntity order = orders.get(position);
        holder.bind(order, currency);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void submitList(List<OrderEntity> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView textOrderId;
        private final TextView textOrderAmount;
        private final TextView textOrderStatus;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderAmount = itemView.findViewById(R.id.textOrderAmount);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
        }

        void bind(@NonNull OrderEntity order, @NonNull NumberFormat currency) {
            textOrderId.setText(String.format(Locale.getDefault(), "Đơn #%d", order.getOrderId()));
            textOrderAmount.setText(String.format(Locale.getDefault(), "Tổng: %s", currency.format(order.getTotalAmount())));
            textOrderStatus.setText(String.format(Locale.getDefault(), "Trạng thái: %s", order.getOrderStatus()));
        }
    }
}
