package com.deligo.app.ui.auth;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.deligo.app.R;
import com.deligo.app.data.SessionManager;
import com.deligo.app.data.UserSession;
import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.UsersDao;
import com.deligo.app.data.local.entity.UserEntity;
import com.deligo.app.data.remote.AuthRepository;
import com.deligo.app.data.remote.model.UserModel;
import com.deligo.app.ui.customer.CustomerMainActivity;
import com.deligo.app.ui.owner.OwnerMainActivity;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // 👉 Có thể dùng chung URL với MainActivity hoặc chọn ảnh riêng cho màn login
    private static final String BG_IMAGE_URL = "";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ImageButton passwordToggleButton;

    private UsersDao usersDao;
    private AuthRepository authRepository;
    private SessionManager sessionManager;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Load background image
        ImageView backgroundImageView = findViewById(R.id.img_background_login);
        if (backgroundImageView != null) {
            Log.d(TAG, "Loading login background from URL: " + BG_IMAGE_URL);
            Glide.with(this)
                    .load(BG_IMAGE_URL)
                    .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<android.graphics.drawable.Drawable> target,
                                                    boolean isFirstResource) {
                            Log.e(TAG, "❌ Glide login background load failed: "
                                    + (e != null ? e.getMessage() : "unknown"), e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                       Object model,
                                                       Target<android.graphics.drawable.Drawable> target,
                                                       com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.i(TAG, "✅ Glide login background loaded from: " + dataSource);
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
        sessionManager = new SessionManager(this);

        initViews();
        setupLoginButton();
        setupRegisterLink();
        setupPasswordToggle();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.editTextLoginEmail);
        passwordEditText = findViewById(R.id.editTextLoginPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerTextView = findViewById(R.id.textViewRegister);
        passwordToggleButton = findViewById(R.id.buttonToggleLoginPassword);
    }

    private void setupPasswordToggle() {
        if (passwordToggleButton == null) {
            return;
        }

        passwordToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPasswordVisible = !isPasswordVisible;
                updatePasswordVisibility();
            }
        });

        updatePasswordVisibility();
    }

    private void updatePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordToggleButton.setImageResource(R.drawable.ic_visibility_off);
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggleButton.setImageResource(R.drawable.ic_visibility);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    private void setupRegisterLink() {
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString();

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

        loginButton.setEnabled(false);
        
        authRepository.loginUser(email, password)
                .addOnSuccessListener(sessionResult -> {
                    Log.d(TAG, "Login successful, session token: " + sessionResult.getSession().getSessionToken());
                    UserModel user = sessionResult.getUser();
                    String sessionToken = sessionResult.getSession().getSessionToken();
                    
                    runOnUiThread(() -> {
                        // Lưu session vào SharedPreferences
                        sessionManager.saveSession(sessionToken, user.getUserId(), user.getRole());
                        
                        // Lưu vào memory session
                        UserSession.setCurrentUser(user, sessionToken);
                        
                        navigateToRoleHome(user);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login failed", e);
                    runOnUiThread(() -> {
                        loginButton.setEnabled(true);
                        String errorMessage = e.getMessage();
                        if (errorMessage != null && errorMessage.contains("Invalid email or password")) {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }

    private void navigateToRoleHome(@NonNull UserModel user) {
        String role = user.getRole();
        if (TextUtils.isEmpty(role)) {
            Toast.makeText(this, "User role is not defined", Toast.LENGTH_SHORT).show();
            return;
        }

        String normalizedRole = role.trim().toLowerCase(Locale.US);
        Class<?> targetClass;
        Intent intent;
        switch (normalizedRole) {
            case "customer":
                targetClass = CustomerMainActivity.class;
                break;
            case "admin":
            case "owner":
                targetClass = OwnerMainActivity.class;
                break;
            default:
                targetClass = null;
                break;
        }

        if (targetClass == null) {
            Toast.makeText(this, "Unknown user role", Toast.LENGTH_SHORT).show();
            return;
        }

        intent = new Intent(this, targetClass);
        startActivity(intent);
        finish();
    }
}
