package com.example.carapplication.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.carapplication.Adapter.OnboardingAdapter;
import com.example.carapplication.Modle.OnboardingItem;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ActivityOnboardingBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingAdapter adapter;
    private List<OnboardingItem> onboardingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        boolean isLoggedIn = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        setupOnboardingData();

        adapter = new OnboardingAdapter(onboardingItems);
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonsState(position);
            }
        });
        binding.btnSkip.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        binding.btnNext.setOnClickListener(v -> {
            int currentItem = binding.viewPager.getCurrentItem();
            int lastItem = onboardingItems.size() - 1;

            if (currentItem < lastItem) {
                binding.viewPager.setCurrentItem(currentItem + 1);
            } else {
                Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        new TabLayoutMediator(binding.tabIndicator, binding.viewPager,
                (tab, position) -> {
                }).attach();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.viewPager.getCurrentItem() > 0) {
                    binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1);
                } else {
                    new AlertDialog.Builder(OnboardingActivity.this)
                            .setTitle("تأكيد الخروج")
                            .setMessage("هل تريد الخروج من التطبيق؟")
                            .setPositiveButton("خروج", (d, w) -> finishAffinity())
                            .setNegativeButton("إلغاء", null)
                            .show();
                }
            }
        });
    }


    private void setupOnboardingData() {
        onboardingItems = new ArrayList<>();

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_car,
                "مرحباً بك في تطبيق حجز السيارات",
                "أفضل السيارات بأفضل الأسعار، اختر سيارتك المفضلة واستمتع برحلة ممتعة"
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_select,
                "اختر سيارتك المناسبة",
                "تصفح مجموعة واسعة من السيارات الحديثة بمواصفات مختلفة تناسب احتياجاتك"
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_track,
                "احجز بخطوات بسيطة وتابع حجوزاتك أولاً بأول",
                "حدد تاريخ الاستلام والإرجاع وأكمل الحجز في ثوانٍ معدودة واستلم إشعارات فورية وتذكيرات قبل موعد الحجز والإرجاع"
        ));
    }

    private void updateButtonsState(int position) {
        int lastItem = onboardingItems.size() - 1;

        if (position == lastItem) {
            binding.btnNext.setText("ابدأ الاستخدام");
            binding.btnNext.setWidth(250);
            binding.btnNext.setBackgroundResource(R.drawable.shape_booking);
            binding.btnSkip.setVisibility(View.GONE);
        } else {
            binding.btnNext.setText("التالي");
            binding.btnSkip.setVisibility(View.VISIBLE);
        }
    }

}