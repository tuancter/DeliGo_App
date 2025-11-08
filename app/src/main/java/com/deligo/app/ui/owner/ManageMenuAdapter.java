package com.deligo.app.ui.owner;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.FoodEntity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageMenuAdapter extends RecyclerView.Adapter<ManageMenuAdapter.FoodViewHolder> {

    public interface OnFoodActionListener {
        void onEditFood(FoodEntity food);

        void onDeleteFood(FoodEntity food);
    }

    private final List<FoodEntity> foods = new ArrayList<>();
    private final OnFoodActionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

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

    static class FoodViewHolder extends RecyclerView.ViewHolder {

        final TextView nameTextView;
        final TextView descriptionTextView;
        final TextView priceTextView;
        final Button editButton;
        final Button deleteButton;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_food_name);
            descriptionTextView = itemView.findViewById(R.id.text_food_description);
            priceTextView = itemView.findViewById(R.id.text_food_price);
            editButton = itemView.findViewById(R.id.button_edit_food);
            deleteButton = itemView.findViewById(R.id.button_delete_food);
        }
    }
}
