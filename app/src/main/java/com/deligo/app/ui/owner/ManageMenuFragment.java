package com.deligo.app.ui.owner;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.CategoryEntity;
import com.deligo.app.data.local.entity.FoodEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageMenuFragment extends Fragment implements ManageMenuAdapter.OnFoodActionListener {

    private ManageMenuViewModel viewModel;
    private ManageMenuAdapter adapter;
    private TextView emptyView;
    private final List<CategoryEntity> categories = new ArrayList<>();

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

        viewModel.getCategories().observe(getViewLifecycleOwner(), categoryEntities -> {
            categories.clear();
            Map<Long, String> categoryNames = new HashMap<>();
            if (categoryEntities != null) {
                categories.addAll(categoryEntities);
                for (CategoryEntity category : categoryEntities) {
                    categoryNames.put(category.getCategoryId(), category.getCategoryName());
                }
            }
            adapter.updateCategoryNames(categoryNames);
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
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), R.string.food_category_missing_message, Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_food_form, null, false);
        final TextInputLayout nameLayout = dialogView.findViewById(R.id.layout_food_name);
        final TextInputLayout priceLayout = dialogView.findViewById(R.id.layout_food_price);
        final TextInputLayout categoryLayout = dialogView.findViewById(R.id.layout_food_category);
        final TextInputLayout imageLayout = dialogView.findViewById(R.id.layout_food_image);
        final TextInputEditText nameInput = dialogView.findViewById(R.id.input_food_name);
        final TextInputEditText priceInput = dialogView.findViewById(R.id.input_food_price);
        final TextInputEditText descriptionInput = dialogView.findViewById(R.id.input_food_description);
        final TextInputEditText imageInput = dialogView.findViewById(R.id.input_food_image);
        final AutoCompleteTextView categoryInput = dialogView.findViewById(R.id.input_food_category);
        final SwitchMaterial availabilitySwitch = dialogView.findViewById(R.id.switch_food_available);

        final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                extractCategoryNames());
        categoryInput.setAdapter(categoryAdapter);

        if (isEdit) {
            nameInput.setText(foodToEdit.getName());
            priceInput.setText(String.valueOf(foodToEdit.getPrice()));
            descriptionInput.setText(foodToEdit.getDescription() != null ? foodToEdit.getDescription() : "");
            imageInput.setText(foodToEdit.getImageUrl() != null ? foodToEdit.getImageUrl() : "");
            availabilitySwitch.setChecked(foodToEdit.isAvailable());
            String categoryName = resolveCategoryName(foodToEdit.getCategoryId());
            if (!TextUtils.isEmpty(categoryName)) {
                categoryInput.setText(categoryName, false);
            }
        } else {
            availabilitySwitch.setChecked(true);
        }

        final long[] selectedCategoryId = new long[]{isEdit ? foodToEdit.getCategoryId() : -1L};
        categoryInput.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < categories.size()) {
                selectedCategoryId[0] = categories.get(position).getCategoryId();
            }
        });

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
                categoryLayout.setError(null);
                imageLayout.setError(null);

                String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                String priceText = priceInput.getText() != null ? priceInput.getText().toString().trim() : "";
                String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";
                String imageUri = imageInput.getText() != null ? imageInput.getText().toString().trim() : "";
                String categoryName = categoryInput.getText() != null ? categoryInput.getText().toString().trim() : "";

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

                long categoryId = selectedCategoryId[0];
                if (categoryId <= 0) {
                    categoryId = findCategoryIdByName(categoryName);
                }

                if (categoryId <= 0) {
                    categoryLayout.setError(getString(R.string.invalid_food_category));
                    return;
                }

                if (TextUtils.isEmpty(imageUri)) {
                    imageLayout.setError(getString(R.string.invalid_food_image));
                    return;
                }

                boolean available = availabilitySwitch.isChecked();

                if (isEdit) {
                    FoodEntity updatedFood = new FoodEntity();
                    updatedFood.setFoodId(foodToEdit.getFoodId());
                    updatedFood.setCategoryId(categoryId);
                    updatedFood.setName(name);
                    updatedFood.setPrice(price);
                    updatedFood.setDescription(description);
                    updatedFood.setImageUrl(imageUri);
                    updatedFood.setAvailable(available);
                    viewModel.updateFood(updatedFood);
                    Toast.makeText(requireContext(), R.string.food_updated_message, Toast.LENGTH_SHORT).show();
                } else {
                    FoodEntity newFood = new FoodEntity();
                    newFood.setName(name);
                    newFood.setPrice(price);
                    newFood.setDescription(description);
                    newFood.setCategoryId(categoryId);
                    newFood.setImageUrl(imageUri);
                    newFood.setAvailable(available);
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
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteFood(food);
                    Toast.makeText(requireContext(), R.string.food_deleted_message, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onToggleAvailability(FoodEntity food, boolean isAvailable) {
        viewModel.updateFoodAvailability(food, isAvailable);
        Toast.makeText(requireContext(), R.string.food_status_updated_message, Toast.LENGTH_SHORT).show();
    }

    private List<String> extractCategoryNames() {
        List<String> names = new ArrayList<>();
        for (CategoryEntity category : categories) {
            if (!TextUtils.isEmpty(category.getCategoryName())) {
                names.add(category.getCategoryName());
            }
        }
        return names;
    }

    @Nullable
    private String resolveCategoryName(long categoryId) {
        for (CategoryEntity category : categories) {
            if (category.getCategoryId() == categoryId) {
                return category.getCategoryName();
            }
        }
        return null;
    }

    private long findCategoryIdByName(@Nullable String categoryName) {
        if (TextUtils.isEmpty(categoryName)) {
            return -1L;
        }
        for (CategoryEntity category : categories) {
            if (categoryName.equalsIgnoreCase(category.getCategoryName())) {
                return category.getCategoryId();
            }
        }
        return -1L;
    }
}
