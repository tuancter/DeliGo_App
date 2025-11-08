package com.deligo.app.ui.shipper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.OrderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AvailableOrdersAdapter extends RecyclerView.Adapter<AvailableOrdersAdapter.AvailableOrderViewHolder> {

    interface OnAcceptDeliveryListener {
        void onAcceptDelivery(@NonNull OrderEntity order);
    }

    private final List<OrderEntity> orders = new ArrayList<>();
    private final OnAcceptDeliveryListener listener;

    public AvailableOrdersAdapter(@NonNull OnAcceptDeliveryListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvailableOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_available_order, parent, false);
        return new AvailableOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailableOrderViewHolder holder, int position) {
        OrderEntity order = orders.get(position);
        holder.bind(order, listener);
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

    static class AvailableOrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView orderIdView;
        private final TextView addressView;
        private final TextView totalAmountView;
        private final Button acceptButton;

        AvailableOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdView = itemView.findViewById(R.id.textAvailableOrderId);
            addressView = itemView.findViewById(R.id.textAvailableOrderAddress);
            totalAmountView = itemView.findViewById(R.id.textAvailableOrderTotal);
            acceptButton = itemView.findViewById(R.id.buttonAcceptDelivery);
        }

        void bind(@NonNull final OrderEntity order, @NonNull final OnAcceptDeliveryListener listener) {
            orderIdView.setText(itemView.getContext().getString(R.string.order_id_format, order.getOrderId()));
            addressView.setText(itemView.getContext().getString(R.string.delivery_address_format, order.getDeliveryAddress()));
            totalAmountView.setText(itemView.getContext().getString(R.string.total_amount_format, String.format(Locale.getDefault(), "%.2f", order.getTotalAmount())));
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAcceptDelivery(order);
                }
            });
        }
    }
}
