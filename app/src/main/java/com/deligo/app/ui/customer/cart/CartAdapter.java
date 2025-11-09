package com.deligo.app.ui.customer.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.model.CartItemWithFood;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Adapter for displaying cart items with ability to increase/decrease/remove.
 */
public class CartAdapter extends ListAdapter<CartItemWithFood, CartAdapter.CartViewHolder> {

    public interface Listener {
        void onIncreaseQty(CartItemWithFood item);
        void onDecreaseQty(CartItemWithFood item);
        void onRemove(CartItemWithFood item);
    }

    private final Listener listener;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public CartAdapter(@NonNull Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CartItemWithFood> DIFF = new DiffUtil.ItemCallback<CartItemWithFood>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItemWithFood oldItem, @NonNull CartItemWithFood newItem) {
            return oldItem.cartItemId == newItem.cartItemId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItemWithFood oldItem, @NonNull CartItemWithFood newItem) {
            return oldItem.foodId == newItem.foodId && oldItem.quantity == newItem.quantity && oldItem.price == newItem.price;
        }
    };

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textPrice;
        private final TextView textQty;
        private final TextView textSubtotal;
        private final ImageButton btnMinus;
        private final ImageButton btnPlus;
        private final ImageButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textCartItemName);
            textPrice = itemView.findViewById(R.id.textCartItemPrice);
            textQty = itemView.findViewById(R.id.textCartItemQty);
            textSubtotal = itemView.findViewById(R.id.textCartItemSubtotal);
            btnMinus = itemView.findViewById(R.id.buttonMinus);
            btnPlus = itemView.findViewById(R.id.buttonPlus);
            btnRemove = itemView.findViewById(R.id.buttonRemove);
        }

        void bind(final CartItemWithFood item) {
            textName.setText(item.foodName);
            textPrice.setText(currency.format(item.price));
            textQty.setText(String.valueOf(item.quantity));
            textSubtotal.setText(currency.format(item.getSubtotal()));

            btnMinus.setOnClickListener(v -> {
                if (listener != null) listener.onDecreaseQty(item);
            });
            btnPlus.setOnClickListener(v -> {
                if (listener != null) listener.onIncreaseQty(item);
            });
            btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemove(item);
            });
        }
    }
}
