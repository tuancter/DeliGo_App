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

    private static final String TAG = "LoginActivity";

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

        if (usersDao == null) {
            Toast.makeText(this, R.string.login_unexpected_error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "UsersDao is not initialized, aborting login attempt");
            return;
        }

        attemptLogin(email, password, false);
    }

    private void attemptLogin(@NonNull final String email, @NonNull final String password, final boolean hasRetried) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                UsersDao dao = usersDao;
                if (dao == null) {
                    Log.e(TAG, "UsersDao became unavailable while attempting login");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, R.string.login_unexpected_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                UserEntity user = null;
                Exception error = null;
                try {
                    user = dao.checkUser(email, password);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to validate user credentials", e);
                    if (!hasRetried && shouldAttemptDatabaseRecovery(e) && resetDatabaseAndReloadDao()) {
                        attemptLogin(email, password, true);
                        return;
                    }
                    error = e;
                }

                final UserEntity resultUser = user;
                final Exception resultError = error;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultError != null) {
                            Toast.makeText(LoginActivity.this, R.string.login_unexpected_error, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (resultUser == null) {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else {
                            navigateToRoleHome(resultUser);
                        }
                    }
                });
            }
        });
    }

    private boolean shouldAttemptDatabaseRecovery(@NonNull Exception e) {
        if (e instanceof SQLiteException) {
            return true;
        }

        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        String normalizedMessage = message.toLowerCase(Locale.US);

        return normalizedMessage.contains("no such table")
                || normalizedMessage.contains("no such column")
                || normalizedMessage.contains("room cannot verify")
                || normalizedMessage.contains("database is locked")
                || normalizedMessage.contains("malformed");
    }

    private boolean resetDatabaseAndReloadDao() {
        try {
            Log.w(TAG, "Attempting to reset Room database after login query failure");
            DeliGoDatabase.resetInstance(getApplicationContext());
            DeliGoDatabase database = DeliGoDatabase.getInstance(getApplicationContext());
            usersDao = database.usersDao();
            return usersDao != null;
        } catch (Exception resetException) {
            Log.e(TAG, "Unable to reset database during login recovery", resetException);
            return false;
        }
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
            case "admin":
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
