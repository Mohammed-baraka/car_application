package com.example.carapplication.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.Modle.Booking;
import com.example.carapplication.Modle.Car;
import com.example.carapplication.Notifications.NotificationHelper;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ActivityBookingBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BookingActivity extends AppCompatActivity {

    private ActivityBookingBinding binding;
    private AppDatabase appDatabase;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private Car selectedCar;
    private int carId;
    private int userId;

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());


    private double totalPrice = 0;
    private long totalDays = 0;
    private double taxRate = 0.15;
    private double insurancePrice = 15.0;
    private boolean insuranceSelected = false;
    private double discountAmount = 0;
    private String appliedPromoCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appDatabase = AppDatabase.getInstance(this);
        carId = getIntent().getIntExtra("car_id", -1);
        userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("user_id", -1);

        if (carId == -1) {
            showErrorAndExit("معرف السيارة غير صالح");
            return;
        }

        if (userId == -1) {
            showErrorAndExit("الرجاء تسجيل الدخول أولاً");
            return;
        }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("حجز السيارة");
        }

        binding.toolbar.setNavigationOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("تأكيد الخروج")
                    .setMessage("هل أنت متأكد من الخروج من عملية الحجز؟")
                    .setPositiveButton("نعم", (dialog, which) -> finish())
                    .setNegativeButton("لا", null)
                    .show();
        });

        Calendar today = Calendar.getInstance();
        startDateCalendar.setTime(today.getTime());
        startDateCalendar.add(Calendar.DAY_OF_MONTH, 1);

        endDateCalendar.setTime(startDateCalendar.getTime());
        endDateCalendar.add(Calendar.DAY_OF_MONTH, 3);

        Calendar minStartDate = Calendar.getInstance();
        minStartDate.add(Calendar.DAY_OF_MONTH, 1);

        binding.tilStartDate.setHelperText("يمكن الحجز من " + dateFormat.format(minStartDate.getTime()));

        updateDateDisplays();
        loadCarDetails();
        binding.etStartDate.setOnClickListener(v -> showStartDatePicker());
        binding.tilStartDate.setEndIconOnClickListener(v -> showStartDatePicker());
        binding.etEndDate.setOnClickListener(v -> showEndDatePicker());
        binding.tilEndDate.setEndIconOnClickListener(v -> showEndDatePicker());
        binding.btnCalculate.setOnClickListener(v -> calculateTotalPrice());
        binding.btnApplyPromo.setOnClickListener(v -> applyPromoCode());
        binding.checkboxInsurance.setOnCheckedChangeListener((buttonView, isChecked) -> {
            insuranceSelected = isChecked;
            calculateTotalPrice();
        });
        binding.btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void loadCarDetails() {
        executorService.execute(() -> {
            try {
                selectedCar = appDatabase.carDao().getCarById(carId);

                if (selectedCar == null) {
                    mainHandler.post(() -> {
                        showErrorAndExit("لم يتم العثور على السيارة");
                    });
                    return;
                }

                mainHandler.post(() -> {
                    displayCarDetails();
                    checkCarAvailability();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    showErrorAndExit("حدث خطأ: " + e.getMessage());
                });
            }
        });
    }

    private void displayCarDetails() {
        if (selectedCar == null) return;

        binding.tvCarName.setText(selectedCar.getName());
        binding.tvCarModel.setText(selectedCar.getModel());
        binding.tvCarYear.setText(String.valueOf(selectedCar.getYear()));
        binding.tvCarColor.setText("اللون: " + (selectedCar.getColor() != null ? selectedCar.getColor() : "أبيض"));

        String priceText = String.format(Locale.US, "$%.0f", selectedCar.getPricePerDay());
        binding.tvPricePerDay.setText(priceText);
        binding.tvBottomTotalPrice.setText(priceText);
        double totlPrice = calculateTotalPrice();
        binding.tvBasePrice.setText("$ "+selectedCar.getPricePerDay());
        binding.tvTotalPrice.setText("$ "+totlPrice);

        if (selectedCar.getImageResId() != 0) {
            binding.ivCarImage.setImageResource(selectedCar.getImageResId());
        } else if (selectedCar.getImageUrl() != null && !selectedCar.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(selectedCar.getImageUrl())
                    .placeholder(R.drawable.ic_car_placeholder)
                    .error(R.drawable.ic_car_placeholder)
                    .into(binding.ivCarImage);
        } else {
            binding.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }

        if (!selectedCar.isAvailable() || selectedCar.isBooked()) {
            binding.btnConfirmBooking.setEnabled(false);
            binding.btnConfirmBooking.setText("السيارة غير متاحة");
            Toast.makeText(this, "عذراً، هذه السيارة غير متاحة للحجز حالياً", Toast.LENGTH_LONG).show();
        }
    }

    private void checkCarAvailability() {
        if (selectedCar == null) return;

        executorService.execute(() -> {
            Date startDate = startDateCalendar.getTime();
            Date endDate = endDateCalendar.getTime();

            List<Booking> conflicts = appDatabase.bookingDao()
                    .getConflictingBookings(selectedCar.getId(), startDate, endDate);

            mainHandler.post(() -> {
                if (conflicts != null && !conflicts.isEmpty()) {
                    binding.tilStartDate.setError("السيارة محجوزة في هذه التواريخ");
                    binding.tilEndDate.setError("السيارة محجوزة في هذه التواريخ");
                    binding.btnConfirmBooking.setEnabled(false);

                    showConflictingDatesDialog(conflicts);
                } else {
                    binding.tilStartDate.setError(null);
                    binding.tilEndDate.setError(null);
                    binding.btnConfirmBooking.setEnabled(true);
                }
            });
        });
    }

    private void showConflictingDatesDialog(List<Booking> conflicts) {
        StringBuilder message = new StringBuilder();
        message.append("السيارة محجوزة في التواريخ التالية:\n\n");

        for (Booking booking : conflicts) {
            message.append("• من ")
                    .append(dateFormat.format(booking.getStartDate()))
                    .append(" إلى ")
                    .append(dateFormat.format(booking.getEndDate()))
                    .append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("السيارة غير متاحة")
                .setMessage(message.toString())
                .setPositiveButton("حسناً", null)
                .show();
    }


    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    startDateCalendar.set(Calendar.YEAR, year);
                    startDateCalendar.set(Calendar.MONTH, month);
                    startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (startDateCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                        Toast.makeText(this, "لا يمكن اختيار تاريخ في الماضي", Toast.LENGTH_SHORT).show();
                        startDateCalendar.setTimeInMillis(System.currentTimeMillis());
                        startDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    if (endDateCalendar.before(startDateCalendar)) {
                        endDateCalendar.setTime(startDateCalendar.getTime());
                        endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    updateDateDisplays();
                    calculateTotalPrice();
                    checkCarAvailability();
                },
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    endDateCalendar.set(Calendar.YEAR, year);
                    endDateCalendar.set(Calendar.MONTH, month);
                    endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (endDateCalendar.before(startDateCalendar)) {
                        Toast.makeText(this, "تاريخ الانتهاء يجب أن يكون بعد تاريخ البدء", Toast.LENGTH_SHORT).show();
                        endDateCalendar.setTime(startDateCalendar.getTime());
                        endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    updateDateDisplays();
                    calculateTotalPrice();
                    checkCarAvailability();
                },
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void updateDateDisplays() {
        binding.etStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        binding.etEndDate.setText(dateFormat.format(endDateCalendar.getTime()));

        binding.tvStartDateDisplay.setText(displayDateFormat.format(startDateCalendar.getTime()));
        binding.tvEndDateDisplay.setText(displayDateFormat.format(endDateCalendar.getTime()));
    }

    private double calculateTotalPrice() {
        if (selectedCar == null) {
            Toast.makeText(this, "خطأ في حساب سعر السيارة", Toast.LENGTH_SHORT).show();
        }

        long diffInMillis = endDateCalendar.getTimeInMillis() - startDateCalendar.getTimeInMillis();
        totalDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        if (totalDays <= 0) {
            totalDays = 1;
            endDateCalendar.setTime(startDateCalendar.getTime());
            endDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplays();
        }

        double basePrice = selectedCar.getPricePerDay();
        double taxAmount = selectedCar.getPricePerDay() * taxRate;
        double insuranceAmount = totalDays * basePrice;

        binding.tvDaysCount.setText(totalDays + (totalDays == 1 ? " يوم" : " أيام"));
        binding.tvBottomDays.setText(totalDays + (totalDays == 1 ? " يوم" : " أيام"));
        binding.tvTotalPrice.setText(String.format(Locale.US, "$%.2f", totalPrice));
        binding.tvTaxAmount.setText(String.format(Locale.US, "$%.2f", taxAmount));
        binding.tvBottomTotalPrice.setText(String.format(Locale.US, "$%.2f", insuranceAmount));
        binding.layoutTax.setVisibility(View.VISIBLE);
        if (insuranceSelected) {
            binding.tvInsuranceInfo.setText(String.format(Locale.US, "تأمين شامل: $%.2f × %d أيام = $%.2f",
                    insurancePrice, totalDays, insuranceAmount));
        }
        return totalPrice;
    }

    private double calculateDiscount(double basePrice) {
        double discount = 0;
        if (totalDays >= 7) {
            discount += basePrice * 0.10;
        } else if (totalDays >= 3) {
            discount += basePrice * 0.05;
        }

        if (!appliedPromoCode.isEmpty()) {
            if (appliedPromoCode.equalsIgnoreCase("WELCOME10")) {
                discount += basePrice * 0.10;
            } else if (appliedPromoCode.equalsIgnoreCase("SAVE20")) {
                discount += basePrice * 0.20;
            }
        }

        return discount;
    }

    private void applyPromoCode() {
        String promoCode = binding.etPromoCode.getText().toString().trim();

        if (promoCode.isEmpty()) {
            binding.tilPromoCode.setError("الرجاء إدخال كود ترويجي");
            return;
        }

        if (promoCode.equalsIgnoreCase("WELCOME10") ||
                promoCode.equalsIgnoreCase("SAVE20") ||
                promoCode.equalsIgnoreCase("FIRST5")) {

            appliedPromoCode = promoCode;
            binding.tilPromoCode.setError(null);
            Toast.makeText(this, "تم تطبيق الكود بنجاح!", Toast.LENGTH_SHORT).show();
            calculateTotalPrice();
        } else {
            binding.tilPromoCode.setError("كود غير صالح");
        }
    }

    private void confirmBooking() {
        if (selectedCar == null) {
            Toast.makeText(this, "لم يتم تحديد سيارة", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.etStartDate.getText().toString().isEmpty() ||
                binding.etEndDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "الرجاء تحديد تواريخ الحجز", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Date startDate = startDateCalendar.getTime();
            Date endDate = endDateCalendar.getTime();

            List<Booking> conflicts = appDatabase.bookingDao()
                    .getConflictingBookings(selectedCar.getId(), startDate, endDate);

            if (conflicts != null && !conflicts.isEmpty()) {
                mainHandler.post(() -> {
                    Toast.makeText(BookingActivity.this,
                            "السيارة محجوزة في هذه التواريخ", Toast.LENGTH_LONG).show();
                });
                return;
            }

            Booking booking = new Booking(
                    selectedCar.getId(),
                    userId,
                    startDate,
                    endDate,
                    totalPrice
            );
            booking.setCarName(selectedCar.getName());
            long bookingId = appDatabase.bookingDao().insert(booking);
            booking.setId((int) bookingId);
            appDatabase.carDao().updateBookingStatus(
                    selectedCar.getId(),
                    true,
                    userId,
                    startDate.getTime(),
                    endDate.getTime(),
                    (int) bookingId
            );

            mainHandler.post(() -> {
                try {
                    NotificationHelper notificationHelper = new NotificationHelper(this);
                    notificationHelper.sendBookingConfirmationNotification(booking);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(BookingActivity.this, ConfirmBookingActivity.class);
                intent.putExtra("booking_id", booking.getId());
                intent.putExtra("car_id", selectedCar.getId());
                intent.putExtra("total_price", totalPrice);
                intent.putExtra("days", totalDays);
                startActivity(intent);
                finish();
            });
        });
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("تأكيد الخروج")
                .setMessage("هل أنت متأكد من الخروج من عملية الحجز؟")
                .setPositiveButton("نعم", (dialog, which) -> finish())
                .setNegativeButton("لا", null)
                .show();
    }

    private void showErrorAndExit(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
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