package com.ajproject.moneymap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ajproject.moneymap.R;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.User;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private MoneyMapDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = MoneyMapDatabase.getInstance(this);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            // Check if email already exists
            User existingUser = database.userDao().getUserByEmail(email);

            runOnUiThread(() -> {
                if (existingUser != null) {
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
                } else {
                    // Register new user
                    User newUser = new User(email, password, name, System.currentTimeMillis());

                    new Thread(() -> {
                        database.userDao().insert(newUser);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Registration successful! Please login",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }).start();
                }
            });
        }).start();
    }
}