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
import com.deligo.app.data.local.entity.CategoryEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ManageCategoriesFragment extends Fragment implements ManageCategoriesAdapter.OnCategoryActionListener {

    private ManageCategoriesViewModel viewModel;
    private ManageCategoriesAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_categories);
        emptyView = view.findViewById(R.id.text_empty_categories);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_category);

        adapter = new ManageCategoriesAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ManageCategoriesViewModel.class);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.submitList(categories);
            toggleEmptyState(categories == null || categories.isEmpty());
        });

        fabAdd.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void toggleEmptyState(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showCategoryDialog(@Nullable final CategoryEntity categoryToEdit) {
        final boolean isEdit = categoryToEdit != null;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_form, null, false);
        final TextInputLayout nameLayout = dialogView.findViewById(R.id.layout_category_name);
        final TextInputEditText nameInput = dialogView.findViewById(R.id.input_category_name);

        if (isEdit) {
            nameInput.setText(categoryToEdit.getCategoryName());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? R.string.update_category : R.string.add_new_category)
                .setView(dialogView)
                .setPositiveButton(isEdit ? R.string.update_category : R.string.add_new_category, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dlg -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                nameLayout.setError(null);
                String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                if (TextUtils.isEmpty(name)) {
                    nameLayout.setError(getString(R.string.invalid_category_name));
                    return;
                }

                if (isEdit) {
                    CategoryEntity updated = new CategoryEntity();
                    updated.setCategoryId(categoryToEdit.getCategoryId());
                    updated.setCategoryName(name);
                    viewModel.updateCategory(updated);
                    Toast.makeText(requireContext(), R.string.category_updated_message, Toast.LENGTH_SHORT).show();
                } else {
                    CategoryEntity newCategory = new CategoryEntity();
                    newCategory.setCategoryName(name);
                    viewModel.insertCategory(newCategory);
                    Toast.makeText(requireContext(), R.string.category_added_message, Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onEditCategory(@NonNull CategoryEntity category) {
        showCategoryDialog(category);
    }

    @Override
    public void onDeleteCategory(@NonNull CategoryEntity category) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_category)
                .setMessage(getString(R.string.delete_category_confirmation, category.getCategoryName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteCategory(category);
                    Toast.makeText(requireContext(), R.string.category_deleted_message, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
