package com.example.carapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carapplication.Modle.ThemeHelper;
import com.example.carapplication.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int theme=ThemeHelper.getThemeMode(this);
        ThemeHelper.setThemeMode(theme);

        new Handler().postDelayed(() -> {
            boolean isLoggedIn = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getBoolean("is_logged_in", false);

            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}