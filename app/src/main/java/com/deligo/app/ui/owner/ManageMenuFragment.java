package com.deligo.app.ui.owner;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.FoodEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class ManageMenuFragment extends Fragment implements ManageMenuAdapter.OnFoodActionListener {

    private ManageMenuViewModel viewModel;
    private ManageMenuAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_foods);
        emptyView = view.findViewById(R.id.text_empty_foods);
        FloatingActionButton fabAddFood = view.findViewById(R.id.fab_add_food);

        adapter = new ManageMenuAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ManageMenuViewModel.class);

        viewModel.getFoods().observe(getViewLifecycleOwner(), foods -> {
            adapter.submitList(foods);
            toggleEmptyState(foods);
        });

        fabAddFood.setOnClickListener(v -> showFoodDialog(null));
    }

    private void toggleEmptyState(@Nullable List<FoodEntity> foods) {
        if (foods == null || foods.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showFoodDialog(@Nullable final FoodEntity foodToEdit) {
        boolean isEdit = foodToEdit != null;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_food_form, null, false);
        final TextInputLayout nameLayout = dialogView.findViewById(R.id.layout_food_name);
        final TextInputLayout priceLayout = dialogView.findViewById(R.id.layout_food_price);
        final TextInputEditText nameInput = dialogView.findViewById(R.id.input_food_name);
        final TextInputEditText priceInput = dialogView.findViewById(R.id.input_food_price);
        final TextInputEditText descriptionInput = dialogView.findViewById(R.id.input_food_description);

        if (isEdit) {
            nameInput.setText(foodToEdit.getName());
            priceInput.setText(String.valueOf(foodToEdit.getPrice()));
            descriptionInput.setText(foodToEdit.getDescription() != null ? foodToEdit.getDescription() : "");
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? R.string.update_food : R.string.add_new_food)
                .setView(dialogView)
                .setPositiveButton(isEdit ? R.string.update_food : R.string.add_food, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                nameLayout.setError(null);
                priceLayout.setError(null);

                String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                String priceText = priceInput.getText() != null ? priceInput.getText().toString().trim() : "";
                String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";

                if (TextUtils.isEmpty(name)) {
                    nameLayout.setError(getString(R.string.invalid_food_name));
                    return;
                }

                double price;
                try {
                    price = Double.parseDouble(priceText);
                } catch (NumberFormatException ex) {
                    priceLayout.setError(getString(R.string.invalid_food_price));
                    return;
                }

                if (price < 0) {
                    priceLayout.setError(getString(R.string.invalid_food_price));
                    return;
                }

                if (isEdit) {
                    FoodEntity updatedFood = new FoodEntity();
                    updatedFood.setFoodId(foodToEdit.getFoodId());
                    updatedFood.setCategoryId(foodToEdit.getCategoryId());
                    updatedFood.setName(name);
                    updatedFood.setPrice(price);
                    updatedFood.setDescription(description);
                    updatedFood.setImageUrl(foodToEdit.getImageUrl());
                    updatedFood.setAvailable(foodToEdit.isAvailable());
                    viewModel.updateFood(updatedFood);
                    Toast.makeText(requireContext(), R.string.food_updated_message, Toast.LENGTH_SHORT).show();
                } else {
                    FoodEntity newFood = new FoodEntity();
                    newFood.setName(name);
                    newFood.setPrice(price);
                    newFood.setDescription(description);
                    newFood.setCategoryId(0);
                    newFood.setAvailable(true);
                    viewModel.insertFood(newFood);
                    Toast.makeText(requireContext(), R.string.food_added_message, Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onEditFood(FoodEntity food) {
        showFoodDialog(food);
    }

    @Override
    public void onDeleteFood(FoodEntity food) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_food)
                .setMessage(getString(R.string.delete_food_confirmation, food.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deleteFood(food))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
