package com.deligo.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.ui.auth.LoginActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "MainActivity";
    // 👉 Thay URL này bằng ảnh thật của bạn
    private static final String BG_IMAGE_URL = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Activity started, setting up UI...");

        ImageView backgroundImageView = findViewById(R.id.img_background);
        TextView databaseStatusView = findViewById(R.id.database_status);

        // Tải ảnh nền qua Glide
        Log.d(TAG, "onCreate: Loading background image from URL -> " + BG_IMAGE_URL);
        Glide.with(this)
                .load(BG_IMAGE_URL)
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        Log.e(TAG, "❌ Glide load failed: " + e.getMessage(), e);
                        return false; // vẫn để Glide hiển thị error placeholder
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                   Object model,
                                                   Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.i(TAG, "✅ Glide loaded image successfully from: " + dataSource);
                        return false;
                    }
                })
                .placeholder(R.color.deligo_background)
                .error(R.color.deligo_background)
                .into(backgroundImageView);

        // DB setup
        Log.d(TAG, "Initializing local database...");
        DeliGoDatabase database = DeliGoDatabase.getInstance(getApplicationContext());
        String databaseName = database.getOpenHelper().getDatabaseName();
        if (databaseName == null) {
            databaseName = getString(R.string.unknown_database_name);
        }
        Log.d(TAG, "Database name: " + databaseName);

        databaseStatusView.setText(getString(R.string.database_ready_message));

        // Thêm nút test Firestore
        findViewById(R.id.testFirestoreButton).setOnClickListener(v -> {
            Log.d(TAG, "Opening Firestore Test Activity...");
            Intent intent = new Intent(MainActivity.this, FirestoreTestActivity.class);
            startActivity(intent);
        });

//        // Delay chuyển màn hình
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            Log.d(TAG, "Navigating to LoginActivity...");
//            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        }, 3000);
    }
}
