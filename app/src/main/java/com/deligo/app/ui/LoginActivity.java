package com.deligo.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deligo.app.R;
import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.UsersDao;
import com.deligo.app.data.local.entity.UserEntity;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;

    private UsersDao usersDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DeliGoDatabase database = DeliGoDatabase.getInstance(getApplicationContext());
        usersDao = database.usersDao();

        initViews();
        setupLoginButton();
        setupRegisterLink();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.editTextLoginEmail);
        passwordEditText = findViewById(R.id.editTextLoginPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerTextView = findViewById(R.id.textViewRegister);
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
                            navigateToRoleHome(user.getRole());
                        }
                    }
                });
            }
        });
    }

    private void navigateToRoleHome(String role) {
        if (TextUtils.isEmpty(role)) {
            Toast.makeText(this, "User role is not defined", Toast.LENGTH_SHORT).show();
            return;
        }

        String normalizedRole = role.trim().toLowerCase(Locale.US);
        String targetClassName;
        switch (normalizedRole) {
            case "customer":
                targetClassName = "com.deligo.app.ui.CustomerMainActivity";
                break;
            case "owner":
                targetClassName = "com.deligo.app.ui.OwnerMainActivity";
                break;
            case "shipper":
                targetClassName = "com.deligo.app.ui.ShipperMainActivity";
                break;
            default:
                targetClassName = null;
                break;
        }

        if (targetClassName == null) {
            Toast.makeText(this, "Unknown user role", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Class<?> targetClass = Class.forName(targetClassName);
            Intent intent = new Intent(this, targetClass);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Destination screen not found", Toast.LENGTH_SHORT).show();
        }
    }
}
