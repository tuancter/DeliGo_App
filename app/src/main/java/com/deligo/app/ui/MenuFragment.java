package com.deligo.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.data.local.entity.FoodEntity;
import com.deligo.app.ui.menu.MenuAdapter;
import com.deligo.app.ui.menu.MenuViewModel;

public class MenuFragment extends Fragment {

    private static final long DEFAULT_USER_ID = 1L;

    private MenuViewModel menuViewModel;
    private MenuAdapter menuAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupRecyclerView(view);
        observeFoods();
        observeAddToCartMessages();
    }

    private void setupViewModel() {
        menuViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(MenuViewModel.class);
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewMenu);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuAdapter = new MenuAdapter(new MenuAdapter.OnAddToCartClickListener() {
            @Override
            public void onAddToCart(FoodEntity food) {
                menuViewModel.addFoodToCart(DEFAULT_USER_ID, food);
            }
        });
        recyclerView.setAdapter(menuAdapter);
    }

    private void observeFoods() {
        menuViewModel.getFoods().observe(getViewLifecycleOwner(), foodEntities -> menuAdapter.submitList(foodEntities));
    }

    private void observeAddToCartMessages() {
        menuViewModel.getAddToCartMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
