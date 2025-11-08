package com.deligo.app.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deligo.app.R;
import com.deligo.app.data.UserSession;
import com.deligo.app.ui.shipper.AvailableOrdersFragment;
import com.deligo.app.ui.shipper.MyDeliveriesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ShipperMainActivity extends AppCompatActivity {

    public static final String EXTRA_SHIPPER_ID = "extra_shipper_id";

    private long shipperId = -1L;
    private Fragment availableOrdersFragment;
    private Fragment myDeliveriesFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_main);

        shipperId = resolveShipperId(savedInstanceState);
        if (shipperId < 0) {
            Toast.makeText(this, R.string.shipper_id_missing_message, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_shipper);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        if (savedInstanceState == null) {
            availableOrdersFragment = AvailableOrdersFragment.newInstance(shipperId);
            myDeliveriesFragment = MyDeliveriesFragment.newInstance(shipperId);
            bottomNavigationView.setSelectedItemId(R.id.navigation_available_orders);
        } else {
            availableOrdersFragment = getSupportFragmentManager().findFragmentByTag(AvailableOrdersFragment.TAG);
            myDeliveriesFragment = getSupportFragmentManager().findFragmentByTag(MyDeliveriesFragment.TAG);
        }
    }

    private long resolveShipperId(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_SHIPPER_ID)) {
            return savedInstanceState.getLong(EXTRA_SHIPPER_ID, -1L);
        }
        long idFromIntent = getIntent().getLongExtra(EXTRA_SHIPPER_ID, -1L);
        if (idFromIntent > 0) {
            return idFromIntent;
        }
        Long sessionId = UserSession.getCurrentUserId();
        return sessionId != null ? sessionId : -1L;
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment;
        String tag;
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_available_orders) {
            if (availableOrdersFragment == null) {
                availableOrdersFragment = AvailableOrdersFragment.newInstance(shipperId);
            }
            selectedFragment = availableOrdersFragment;
            tag = AvailableOrdersFragment.TAG;
        } else if (itemId == R.id.navigation_my_deliveries) {
            if (myDeliveriesFragment == null) {
                myDeliveriesFragment = MyDeliveriesFragment.newInstance(shipperId);
            }
            selectedFragment = myDeliveriesFragment;
            tag = MyDeliveriesFragment.TAG;
        } else {
            return false;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.shipper_fragment_container, selectedFragment, tag)
                .commit();
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_SHIPPER_ID, shipperId);
    }
}
