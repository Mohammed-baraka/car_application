package com.example.carapplication.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carapplication.Adapter.BookingAdapter;
import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.DataBase.BookingDao;
import com.example.carapplication.Modle.Booking;
import com.example.carapplication.databinding.ActivityMyBookingsBinding;

import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {
    private ActivityMyBookingsBinding binding;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList = new ArrayList<>();
    private BookingDao bookingDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppDatabase database = AppDatabase.getInstance(this);
        bookingDao = database.bookingDao();

        bookingAdapter = new BookingAdapter(bookingList);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBookings.setAdapter(bookingAdapter);
        setupTabLayout();
        loadBookings("active");
    }

    private void setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("الحجوزات النشطة"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("الحجوزات المنتهية"));

        binding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadBookings("active");
                } else {
                    loadBookings("completed");
                }
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }
        });
    }

    private void loadBookings(String status) {
        int userId = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getInt("user_id", 1);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Booking> bookings;
            if (status.equals("active")) {
                bookings = bookingDao.getActiveBookings(userId);
            } else {
                bookings = bookingDao.getCompletedBookings(userId);
            }

            runOnUiThread(() -> {
                bookingList.clear();
                bookingList.addAll(bookings);
                bookingAdapter.updateList(bookingList);

                if (bookingList.isEmpty()) {
                    binding.tvNoBookings.setVisibility(android.view.View.VISIBLE);
                } else {
                    binding.tvNoBookings.setVisibility(android.view.View.GONE);
                }
            });
        });
    }
}