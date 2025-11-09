package com.deligo.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deligo.app.R;
import com.deligo.app.data.UserSession;
import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.UsersDao;
import com.deligo.app.data.local.entity.UserEntity;
import com.deligo.app.ui.customer.CustomerMainActivity;
import com.deligo.app.ui.owner.OwnerMainActivity;
import com.deligo.app.ui.shipper.ShipperMainActivity;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ImageButton passwordToggleButton;

    private UsersDao usersDao;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DeliGoDatabase database = DeliGoDatabase.getInstance(getApplicationContext());
        usersDao = database.usersDao();

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

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final UserEntity user = usersDao.checkUser(email, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (user == null) {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else {
                            navigateToRoleHome(user);
                        }
                    }
                });
            }
        });
    }

    private void navigateToRoleHome(@NonNull UserEntity user) {
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
            case "owner":
                targetClass = OwnerMainActivity.class;
                break;
            case "shipper":
                targetClass = ShipperMainActivity.class;
                break;
            default:
                targetClass = null;
                break;
        }

        if (targetClass == null) {
            if ("shipper".equals(normalizedRole)) {
                Toast.makeText(this, "Destination screen not found", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unknown user role", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        intent = new Intent(this, targetClass);
        if (normalizedRole.equals("shipper")) {
            intent.putExtra(ShipperMainActivity.EXTRA_SHIPPER_ID, user.getUserId());
        }
        UserSession.setCurrentUser(user);
        startActivity(intent);
        finish();
    }
}
