package com.deligo.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deligo.app.util.FirestoreTestHelper;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity để test kết nối Firestore
 * Chạy activity này để kiểm tra và khởi tạo collections
 */
public class FirestoreTestActivity extends AppCompatActivity {
    private static final String TAG = "FirestoreTestActivity";
    
    private TextView statusTextView;
    private ProgressBar progressBar;
    private Button testConnectionButton;
    private Button initCollectionsButton;
    private Button checkCollectionsButton;
    private Button cleanupButton;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_test);
        
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
        testConnectionButton = findViewById(R.id.testConnectionButton);
        initCollectionsButton = findViewById(R.id.initCollectionsButton);
        checkCollectionsButton = findViewById(R.id.checkCollectionsButton);
        cleanupButton = findViewById(R.id.cleanupButton);
        
        // Hiển thị thông tin Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String info = "Firebase App: " + db.getApp().getName() + "\n";
        info += "Firestore initialized: " + (db != null ? "Yes" : "No") + "\n\n";
        statusTextView.setText(info);
        
        setupButtons();
    }
    
    private void setupButtons() {
        testConnectionButton.setOnClickListener(v -> testConnection());
        initCollectionsButton.setOnClickListener(v -> initializeCollections());
        checkCollectionsButton.setOnClickListener(v -> checkCollections());
        cleanupButton.setOnClickListener(v -> cleanupSampleData());
    }
    
    private void testConnection() {
        showLoading(true);
        appendStatus("Testing Firestore connection...\n");
        
        FirestoreTestHelper.testConnection(new FirestoreTestHelper.TestCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    appendStatus("✅ " + message + "\n");
                    Toast.makeText(FirestoreTestActivity.this, "Connection successful!", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    appendStatus("❌ " + error + "\n");
                    Toast.makeText(FirestoreTestActivity.this, "Connection failed!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void initializeCollections() {
        showLoading(true);
        appendStatus("Initializing collections...\n");
        
        FirestoreTestHelper.initializeCollections(new FirestoreTestHelper.TestCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    appendStatus("✅ " + message + "\n");
                    Toast.makeText(FirestoreTestActivity.this, "Collections created!", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    appendStatus("❌ " + error + "\n");
                    Toast.makeText(FirestoreTestActivity.this, "Failed to create collections!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void checkCollections() {
        showLoading(true);
        appendStatus("Checking collections...\n");
        
        FirestoreTestHelper.checkCollections(new FirestoreTestHelper.TestCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    appendStatus(message + "\n");
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    appendStatus("❌ " + error + "\n");
                });
            }
        });
    }
    
    private void cleanupSampleData() {
        showLoading(true);
        appendStatus("Cleaning up sample data...\n");
        
        FirestoreTestHelper.cleanupSampleData(new FirestoreTestHelper.TestCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    appendStatus("✅ " + message + "\n");
                    Toast.makeText(FirestoreTestActivity.this, "Cleanup complete!", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    appendStatus("❌ " + error + "\n");
                });
            }
        });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        testConnectionButton.setEnabled(!show);
        initCollectionsButton.setEnabled(!show);
        checkCollectionsButton.setEnabled(!show);
        cleanupButton.setEnabled(!show);
    }
    
    private void appendStatus(String message) {
        statusTextView.append(message);
        Log.d(TAG, message);
    }
}
