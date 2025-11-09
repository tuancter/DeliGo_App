package com.deligo.app.ui.owner;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.FoodEntity;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageMenuAdapter extends RecyclerView.Adapter<ManageMenuAdapter.FoodViewHolder> {

    public interface OnFoodActionListener {
        void onEditFood(FoodEntity food);

        void onDeleteFood(FoodEntity food);

        void onToggleAvailability(FoodEntity food, boolean isAvailable);
    }

    private final List<FoodEntity> foods = new ArrayList<>();
    private final OnFoodActionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final Map<Long, String> categoryNames = new HashMap<>();

    public ManageMenuAdapter(@NonNull OnFoodActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_manage, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        final FoodEntity food = foods.get(position);
        holder.nameTextView.setText(food.getName());
        String description = food.getDescription();
        if (TextUtils.isEmpty(description)) {
            holder.descriptionTextView.setVisibility(View.GONE);
        } else {
            holder.descriptionTextView.setVisibility(View.VISIBLE);
            holder.descriptionTextView.setText(description);
        }
        holder.priceTextView.setText(currencyFormat.format(food.getPrice()));

        String categoryName = categoryNames.get(food.getCategoryId());
        if (TextUtils.isEmpty(categoryName)) {
            holder.categoryTextView.setVisibility(View.GONE);
        } else {
            holder.categoryTextView.setVisibility(View.VISIBLE);
            holder.categoryTextView.setText(holder.itemView.getContext().getString(R.string.food_category_format, categoryName));
        }

        bindAvailability(holder, food);

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditFood(food);
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteFood(food);
            }
        });
    }

    private void bindAvailability(@NonNull FoodViewHolder holder, @NonNull FoodEntity food) {
        final Context context = holder.itemView.getContext();
        holder.availabilitySwitch.setOnCheckedChangeListener(null);
        holder.availabilitySwitch.setChecked(food.isAvailable());
        updateAvailabilityText(holder, food.isAvailable(), context);
        holder.availabilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateAvailabilityText(holder, isChecked, context);
                listener.onToggleAvailability(food, isChecked);
            }
        });
    }

    private void updateAvailabilityText(@NonNull FoodViewHolder holder, boolean isAvailable, @NonNull Context context) {
        String status = context.getString(isAvailable
                ? R.string.food_status_available
                : R.string.food_status_unavailable);
        holder.availabilityTextView.setText(context.getString(R.string.food_status_format, status));
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public void submitList(List<FoodEntity> newFoods) {
        foods.clear();
        if (newFoods != null) {
            foods.addAll(newFoods);
        }
        notifyDataSetChanged();
    }

    public void updateCategoryNames(@NonNull Map<Long, String> names) {
        categoryNames.clear();
        categoryNames.putAll(names);
        notifyDataSetChanged();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {

        final TextView nameTextView;
        final TextView descriptionTextView;
        final TextView priceTextView;
        final TextView categoryTextView;
        final TextView availabilityTextView;
        final SwitchMaterial availabilitySwitch;
        final Button editButton;
        final Button deleteButton;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_food_name);
            descriptionTextView = itemView.findViewById(R.id.text_food_description);
            priceTextView = itemView.findViewById(R.id.text_food_price);
            categoryTextView = itemView.findViewById(R.id.text_food_category);
            availabilityTextView = itemView.findViewById(R.id.text_food_status);
            availabilitySwitch = itemView.findViewById(R.id.switch_food_available);
            editButton = itemView.findViewById(R.id.button_edit_food);
            deleteButton = itemView.findViewById(R.id.button_delete_food);
        }
    }
}
