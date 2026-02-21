package com.example.carapplication.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.Modle.Booking;
import com.example.carapplication.Modle.Car;
import com.example.carapplication.Modle.ImagePickerHelper;
import com.example.carapplication.Modle.User;
import com.example.carapplication.Notifications.NotificationHelper;
import com.example.carapplication.Notifications.NotificationScheduler;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ActivityConfirmBookingBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfirmBookingActivity extends AppCompatActivity  {

    private ActivityConfirmBookingBinding binding;
    private AppDatabase appDatabase;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private int bookingId ,carId;
    private Booking currentBooking;
    private Car currentCar;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appDatabase = AppDatabase.getInstance(this);
        bookingId = getIntent().getIntExtra("booking_id", -1);

        if (bookingId == -1) {
            showErrorAndExit("معرف الحجز غير صالح");
            return;
        }
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("تأكيد الحجز");
        }

        binding.toolbar.setNavigationOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("تأكيد الخروج")
                    .setMessage("هل أنت متأكد من الخروج من صفحة تأكيد الحجز؟")
                    .setPositiveButton("نعم", (dialog, which) -> {
                        finish();
                    })
                    .setNegativeButton("لا", null)
                    .show();
        });

        loadBookingDetails();

        binding.btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        binding.btnCancelBooking.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("إلغاء الحجز")
                    .setMessage("هل أنت متأكد من إلغاء هذا الحجز؟")
                    .setPositiveButton("نعم، إلغاء", (dialog, which) -> {
                        cancelBooking();
                    })
                    .setNegativeButton("لا", null)
                    .show();
        });
    }

    private void loadBookingDetails() {

        executorService.execute(() -> {
            try {
                Booking booking = getBookingById(bookingId);
                if (bookingId == -1) {
                    mainHandler.post(() -> {
                        showErrorAndExit("لم يتم العثور على الحجز");
                    });
                    return;
                }

                Car car = getCarById(booking.getCarId());

                if (car == null) {
                    mainHandler.post(() -> {
                        showErrorAndExit("لم يتم العثور على بيانات السيارة");
                    });
                    return;
                }

                String customerName = getCustomerName(booking.getUserId());
                currentBooking = booking;
                currentCar = car;

                mainHandler.post(() -> {
                    displayBookingDetails(customerName);
                    try {
                        NotificationHelper notificationHelper = new NotificationHelper(this);
                        notificationHelper.sendBookingConfirmationNotification(currentBooking);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        NotificationScheduler.scheduleAllBookingNotifications(this, currentBooking);
                        NotificationScheduler.scheduleReturnReminder(this, currentBooking);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(this, "حدث خط", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private Booking getBookingById(int bookingId) {
        try {
            List<Booking> bookings = appDatabase.bookingDao().getBookingsByUser(getCurrentUserId());

            if (bookings != null) {
                for (Booking b : bookings) {
                    if (b.getId() == bookingId) {
                        return b;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentBooking;
    }

    private Car getCarById(int carId) {
        try {
            return appDatabase.carDao().getCarById(carId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getCustomerName(int userId) {
        try {
            User user = appDatabase.userDao().getUserById(userId);
            binding.tvCustomerEmail.setText(user.getEmail());
            if (user.getProfileImageUri() != null && !user.getProfileImageUri().isEmpty()) {
                ImagePickerHelper.loadImage(this, Uri.parse(user.getProfileImageUri()), binding.ivCustomerAvatar);
            } else if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                ImagePickerHelper.loadImage(this, user.getProfileImage(), binding.ivCustomerAvatar);
            } else {
                binding.ivCustomerAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            }
            return user != null ? user.getFullName() : "مستخدم";
        } catch (Exception e) {
            e.printStackTrace();
            return "مستخدم";
        }
    }

    private int getCurrentUserId() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("user_id", -1);
    }

    private void displayBookingDetails(String customerName) {
        if (currentBooking == null || currentCar == null) return;
        binding.tvBookingId.setText(String.format("رقم الحجز: #%d", currentBooking.getId()));
        binding.tvCustomerName.setText(customerName);
        binding.tvCarName.setText(currentCar.getName());
        binding.tvCarModel.setText(String.format("%s • %d", currentCar.getModel(), currentCar.getYear()));
        binding.tvBookingDate.setText("تاريخ الحجز: " + dateFormat.format(currentBooking.getBookingDate()));
        binding.tvStartDate.setText(dateFormat.format(currentBooking.getStartDate()));
        binding.tvEndDate.setText(dateFormat.format(currentBooking.getEndDate()));

        if (binding.tvPickupTime != null) {
            binding.tvPickupTime.setText("وقت الاستلام: " + timeFormat.format(currentBooking.getStartDate()));
        }
        if (binding.tvReturnTime != null) {
            binding.tvReturnTime.setText("وقت الإرجاع: " + timeFormat.format(currentBooking.getEndDate()));
        }
        long duration = currentBooking.getDurationInDays();
        binding.tvDuration.setText(String.valueOf(duration) + " أيام");
        binding.tvTotalPrice.setText(String.format("$%.2f", currentBooking.getTotalPrice()));
        binding.tvStatus.setText(currentBooking.getStatusText());
        binding.tvStatus.setTextColor(currentBooking.getStatusColor());
        loadCarImage();
    }

    private void loadCarImage() {
        if (currentCar == null) return;
        if (currentCar.getImageResId() != 0) {
            binding.ivCarImage.setImageResource(currentCar.getImageResId());
        } else if (currentCar.getImageUrl() != null && !currentCar.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentCar.getImageUrl())
                    .placeholder(R.drawable.ic_car_placeholder)
                    .error(R.drawable.ic_car_placeholder)
                    .into(binding.ivCarImage);
        } else {
            binding.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }
    }


    private void showCancelBookingConfirmation() {

    }

    private void cancelBooking() {
        executorService.execute(() -> {
            try {
                appDatabase.bookingDao().cancelBooking(currentBooking.getId());
                appDatabase.carDao().clearBookingStatus(currentBooking.getCarId());

                NotificationScheduler.cancelBookingReminders(this, currentBooking.getId());

                mainHandler.post(() -> {
                    Toast.makeText(this, "تم إلغاء الحجز بنجاح", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(this, "فشل إلغاء الحجز: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private void showErrorAndExit(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }


    @Override
    public boolean onSupportNavigateUp() {
        new AlertDialog.Builder(this)
                .setTitle("تأكيد الخروج")
                .setMessage("هل أنت متأكد من الخروج من صفحة تأكيد الحجز؟")
                .setPositiveButton("نعم", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("لا", null)
                .show();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        binding = null;
    }
}