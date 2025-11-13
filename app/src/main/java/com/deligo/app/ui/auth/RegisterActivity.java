package com.deligo.app.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.deligo.app.R;
import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.UsersDao;
import com.deligo.app.data.local.entity.UserEntity;
import com.deligo.app.data.remote.AuthRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Có thể dùng chung URL với MainActivity/LoginActivity cho đồng bộ
    private static final String BG_IMAGE_URL = "";
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Spinner roleSpinner;
    private Button registerButton;
    private ImageButton passwordToggleButton;
    private ImageButton confirmPasswordToggleButton;

    private UsersDao usersDao;
    private AuthRepository authRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Load ảnh nền
        ImageView backgroundImageView = findViewById(R.id.img_background_register);
        if (backgroundImageView != null) {
            Log.d(TAG, "Loading register background from URL: " + BG_IMAGE_URL);
            Glide.with(this)
                    .load(BG_IMAGE_URL)
                    .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<android.graphics.drawable.Drawable> target,
                                                    boolean isFirstResource) {
                            Log.e(TAG, "❌ Glide register background load failed: "
                                    + (e != null ? e.getMessage() : "unknown"), e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                       Object model,
                                                       Target<android.graphics.drawable.Drawable> target,
                                                       com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.i(TAG, "✅ Glide register background loaded from: " + dataSource);
                            return false;
                        }
                    })
                    .placeholder(R.color.deligo_background)
                    .error(R.color.deligo_background)
                    .into(backgroundImageView);
        }

        DeliGoDatabase database = DeliGoDatabase.getInstance(getApplicationContext());
        usersDao = database.usersDao();
        authRepository = new AuthRepository();

        initViews();
        setupRoleSpinner();
        setupRegisterButton();
        setupPasswordToggle();
    }

    private void initViews() {
        fullNameEditText = findViewById(R.id.editTextFullName);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        roleSpinner = findViewById(R.id.spinnerRole);
        registerButton = findViewById(R.id.buttonRegister);
        passwordToggleButton = findViewById(R.id.buttonToggleRegisterPassword);
        confirmPasswordToggleButton = findViewById(R.id.buttonToggleConfirmPassword);
    }

    private void setupPasswordToggle() {
        if (passwordToggleButton != null) {
            passwordToggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isPasswordVisible = !isPasswordVisible;
                    updatePasswordVisibility();
                }
            });
        }

        if (confirmPasswordToggleButton != null) {
            confirmPasswordToggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isConfirmPasswordVisible = !isConfirmPasswordVisible;
                    updateConfirmPasswordVisibility();
                }
            });
        }

        updatePasswordVisibility();
        updateConfirmPasswordVisibility();
    }

    private void updatePasswordVisibility() {
        if (passwordToggleButton == null) return;
        
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordToggleButton.setImageResource(R.drawable.ic_visibility_off);
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggleButton.setImageResource(R.drawable.ic_visibility);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    private void updateConfirmPasswordVisibility() {
        if (confirmPasswordToggleButton == null) return;
        
        if (isConfirmPasswordVisible) {
            confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            confirmPasswordToggleButton.setImageResource(R.drawable.ic_visibility_off);
        } else {
            confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            confirmPasswordToggleButton.setImageResource(R.drawable.ic_visibility);
        }
        confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
    }

    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Customer"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
    }

    private void setupRegisterButton() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });
    }

    private void handleRegistration() {
        final String fullName = fullNameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString();
        final String confirmPassword = confirmPasswordEditText.getText().toString();
        final String role = roleSpinner.getSelectedItem() != null
                ? roleSpinner.getSelectedItem().toString()
                : "";

        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError("Full name is required");
            fullNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm password is required");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(role)) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);
        
        authRepository.registerUser(fullName, email, password, role)
                .addOnSuccessListener(userId -> {
                    Log.d(TAG, "User registered successfully with ID: " + userId);
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Registration failed", e);
                    runOnUiThread(() -> {
                        registerButton.setEnabled(true);
                        String errorMessage = e.getMessage();
                        if (errorMessage != null && errorMessage.contains("Email already registered")) {
                            Toast.makeText(RegisterActivity.this, "Email already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }
}
