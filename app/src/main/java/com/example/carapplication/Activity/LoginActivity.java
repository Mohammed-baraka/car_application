package com.example.carapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.DataBase.UserDao;
import com.example.carapplication.Modle.User;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ActivityLoginBinding;


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppDatabase database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        binding.btnLogin.setOnClickListener(v -> {
            loginUser();
        });

        binding.btnRegister.setOnClickListener(v -> {
            registerUser();
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "سيتم إرسال رابط إعادة تعيين كلمة المرور", Toast.LENGTH_SHORT).show();
        });
        insertSampleUser();
    }


    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.login(email, password);
            runOnUiThread(() -> {
                if (user != null) {
                    // Save login state
                    getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("is_logged_in", true)
                            .putInt("user_id", user.getId())
                            .apply();

                    Toast.makeText(LoginActivity.this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "البريد الإلكتروني أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void registerUser() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void insertSampleUser() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User testUser = userDao.getUserByEmail("test@test.com");
            if (testUser == null) {
                userDao.insert(new User("test@test.com", "123456", "مستخدم تجريبي", "0512345678"));
            }
        });
    }


}