package com.ajproject.moneymap.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ajproject.moneymap.MainActivity;
import com.ajproject.moneymap.R;
import com.ajproject.moneymap.database.MoneyMapDatabase;
import com.ajproject.moneymap.models.User;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private MoneyMapDatabase database;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = MoneyMapDatabase.getInstance(this);
        prefs = getSharedPreferences("MoneyMapPrefs", MODE_PRIVATE);

        // Check if already logged in
        if (prefs.getBoolean("isLoggedIn", false)) {
            goToMain();
            return;
        }

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(v -> login());
        tvRegister.setOnClickListener(v -> goToRegister());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            User user = database.userDao().login(email, password);

            runOnUiThread(() -> {
                if (user != null) {
                    // Save login state
                    prefs.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("userEmail", user.getEmail())
                            .putString("userName", user.getName())
                            .apply();

                    Toast.makeText(this, "Welcome back, " + user.getName() + "!",
                            Toast.LENGTH_SHORT).show();
                    goToMain();
                } else {
                    Toast.makeText(this, "Invalid email or password",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void goToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}