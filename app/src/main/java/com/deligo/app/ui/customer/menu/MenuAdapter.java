package com.deligo.app.ui.customer.menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.FoodEntity;

import java.text.NumberFormat;
import java.util.Locale;

public class MenuAdapter extends ListAdapter<FoodEntity, MenuAdapter.MenuViewHolder> {

    public interface OnAddToCartClickListener {
        void onAddToCart(FoodEntity food);
    }

    private final OnAddToCartClickListener listener;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public MenuAdapter(@NonNull OnAddToCartClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_food, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static final DiffUtil.ItemCallback<FoodEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<FoodEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull FoodEntity oldItem, @NonNull FoodEntity newItem) {
            return oldItem.getFoodId() == newItem.getFoodId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FoodEntity oldItem, @NonNull FoodEntity newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getPrice() == newItem.getPrice();
        }
    };

    class MenuViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView priceTextView;
        private final Button addToCartButton;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textFoodName);
            priceTextView = itemView.findViewById(R.id.textFoodPrice);
            addToCartButton = itemView.findViewById(R.id.buttonAddToCart);
        }

        void bind(final FoodEntity food) {
            nameTextView.setText(food.getName());
            priceTextView.setText(currencyFormatter.format(food.getPrice()));
            addToCartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onAddToCart(food);
                    }
                }
            });
        }
    }
}
