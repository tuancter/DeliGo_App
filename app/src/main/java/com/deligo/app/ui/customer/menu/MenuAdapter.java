package com.deligo.app.ui.customer.menu;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.deligo.app.R;
import com.deligo.app.data.local.entity.FoodEntity;

import java.text.NumberFormat;
import java.util.Locale;

public class MenuAdapter extends ListAdapter<FoodEntity, MenuAdapter.MenuViewHolder> {

    private static final String TAG = "MenuAdapter";

    public interface OnAddToCartClickListener {
        void onAddToCart(FoodEntity food);
    }

    private final OnAddToCartClickListener listener;
    private final NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public MenuAdapter(@NonNull OnAddToCartClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_food, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static final DiffUtil.ItemCallback<FoodEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<FoodEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull FoodEntity oldItem,
                                               @NonNull FoodEntity newItem) {
                    return oldItem.getFoodId() == newItem.getFoodId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull FoodEntity oldItem,
                                                  @NonNull FoodEntity newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getPrice() == newItem.getPrice();
                }
            };

    class MenuViewHolder extends RecyclerView.ViewHolder {

        private final ImageView foodImageView;
        private final TextView nameTextView;
        private final TextView priceTextView;
        private final TextView descriptionTextView;
        private final Button addToCartButton;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.imageFood);
            nameTextView = itemView.findViewById(R.id.textFoodName);
            priceTextView = itemView.findViewById(R.id.textFoodPrice);
            descriptionTextView = itemView.findViewById(R.id.textFoodDescription);
            addToCartButton = itemView.findViewById(R.id.buttonAddToCart);
        }

        void bind(final FoodEntity food) {
            nameTextView.setText(food.getName());
            priceTextView.setText(currencyFormatter.format(food.getPrice()));
            if (descriptionTextView != null) {
                descriptionTextView.setText(food.getDescription());
            }

            String imageUrl = food.getImageUrl(); // đổi cho đúng field nếu khác

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                Log.w(TAG, "⚠️ Food '" + food.getName() + "' has empty image URL");
                foodImageView.setImageResource(android.R.drawable.ic_menu_report_image);
            } else {
                Log.i(TAG, "🖼️ Loading image for '" + food.getName() + "': " + imageUrl);
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(
                                    GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Log.e(TAG, "❌ Glide load failed for " + imageUrl, e);
                                if (e != null) e.logRootCauses(TAG);
                                return false; // vẫn hiển thị error drawable
                            }

                            @Override
                            public boolean onResourceReady(
                                    android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target,
                                    DataSource dataSource, boolean isFirstResource) {
                                Log.i(TAG, "✅ Glide loaded image for '" + food.getName() +
                                        "' from: " + dataSource.name());
                                return false;
                            }
                        })
                        .into(foodImageView);
            }

            addToCartButton.setOnClickListener(v -> {
                if (listener != null) listener.onAddToCart(food);
            });
        }
    }
}
