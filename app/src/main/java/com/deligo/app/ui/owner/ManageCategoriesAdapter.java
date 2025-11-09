package com.deligo.app.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesAdapter extends RecyclerView.Adapter<ManageCategoriesAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEditCategory(@NonNull CategoryEntity category);
        void onDeleteCategory(@NonNull CategoryEntity category);
    }

    private final List<CategoryEntity> categories = new ArrayList<>();
    private final OnCategoryActionListener listener;

    public ManageCategoriesAdapter(@NonNull OnCategoryActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_manage, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        final CategoryEntity category = categories.get(position);
        holder.nameTextView.setText(category.getCategoryName());
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditCategory(category);
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteCategory(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void submitList(List<CategoryEntity> newCategories) {
        categories.clear();
        if (newCategories != null) {
            categories.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final Button editButton;
        final Button deleteButton;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_category_name);
            editButton = itemView.findViewById(R.id.button_edit_category);
            deleteButton = itemView.findViewById(R.id.button_delete_category);
        }
    }
}
