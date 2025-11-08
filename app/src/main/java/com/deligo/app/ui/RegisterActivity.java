package com.deligo.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deligo.app.R;
import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.dao.UsersDao;
import com.deligo.app.data.local.entity.UserEntity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Spinner roleSpinner;
    private Button registerButton;

    private UsersDao usersDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        DeliGoDatabase database = DeliGoDatabase.getInstance(getApplicationContext());
        usersDao = database.usersDao();

        initViews();
        setupRoleSpinner();
        setupRegisterButton();
    }

    private void initViews() {
        fullNameEditText = findViewById(R.id.editTextFullName);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        roleSpinner = findViewById(R.id.spinnerRole);
        registerButton = findViewById(R.id.buttonRegister);
    }

    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Customer", "Shipper"}
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

        if (TextUtils.isEmpty(role)) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                UserEntity existingUser = usersDao.getUserByEmail(email);
                if (existingUser != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, "Email already registered", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                UserEntity user = new UserEntity();
                user.setFullName(fullName);
                user.setEmail(email);
                user.setPassword(password);
                user.setRole(role);
                user.setStatus("Active");

                usersDao.insert(user);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }
}
