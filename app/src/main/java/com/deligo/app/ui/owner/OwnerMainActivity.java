package com.deligo.app.ui.owner;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deligo.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OwnerMainActivity extends AppCompatActivity {

    private final Fragment manageMenuFragment = new ManageMenuFragment();
    private final Fragment processOrdersFragment = new ProcessOrdersFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_owner);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_manage_menu);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment;
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_manage_menu) {
            selectedFragment = manageMenuFragment;
        } else if (itemId == R.id.navigation_process_orders) {
            selectedFragment = processOrdersFragment;
        } else {
            return false;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.owner_fragment_container, selectedFragment)
                .commit();
        return true;
    }
}
