package com.deligo.app.ui.shipper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.OrderEntity;

import java.util.ArrayList;
import java.util.List;

public class MyDeliveriesAdapter extends RecyclerView.Adapter<MyDeliveriesAdapter.MyDeliveryViewHolder> {

    interface OnDeliveryClickListener {
        void onDeliveryClicked(@NonNull OrderEntity order);
    }

    private final List<OrderEntity> deliveries = new ArrayList<>();
    private final OnDeliveryClickListener listener;

    public MyDeliveriesAdapter(@NonNull OnDeliveryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyDeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_delivery, parent, false);
        return new MyDeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyDeliveryViewHolder holder, int position) {
        OrderEntity order = deliveries.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    public void submitList(List<OrderEntity> newDeliveries) {
        deliveries.clear();
        if (newDeliveries != null) {
            deliveries.addAll(newDeliveries);
        }
        notifyDataSetChanged();
    }

    static class MyDeliveryViewHolder extends RecyclerView.ViewHolder {

        private final TextView orderIdView;
        private final TextView addressView;
        private final TextView statusView;

        MyDeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdView = itemView.findViewById(R.id.textMyDeliveryOrderId);
            addressView = itemView.findViewById(R.id.textMyDeliveryAddress);
            statusView = itemView.findViewById(R.id.textMyDeliveryStatus);
        }

        void bind(@NonNull final OrderEntity order, @NonNull final OnDeliveryClickListener listener) {
            orderIdView.setText(itemView.getContext().getString(R.string.order_id_format, order.getOrderId()));
            addressView.setText(itemView.getContext().getString(R.string.delivery_address_format, order.getDeliveryAddress()));
            statusView.setText(itemView.getContext().getString(R.string.order_status_format, order.getOrderStatus()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeliveryClicked(order);
                }
            });
        }
    }
}
