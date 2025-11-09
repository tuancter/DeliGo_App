package com.deligo.app.ui.customer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deligo.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMainActivity extends AppCompatActivity {

    private final Fragment menuFragment = new MenuFragment();
    private final Fragment cartFragment = new CartFragment();
    private final Fragment ordersFragment = new OrdersFragment();
    private final Fragment userFragment = new UserFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_menu);
        }
    }

    private boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        Fragment selectedFragment;
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_menu) {
            selectedFragment = menuFragment;
        } else if (itemId == R.id.navigation_cart) {
            selectedFragment = cartFragment;
        } else if (itemId == R.id.navigation_orders) {
            selectedFragment = ordersFragment;
        } else if (itemId == R.id.navigation_user) {
            selectedFragment = userFragment;
        } else {
            return false;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
        return true;
    }
}
