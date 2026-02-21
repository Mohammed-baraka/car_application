package com.example.carapplication.Activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.carapplication.Modle.Car;
import com.example.carapplication.R;
import com.example.carapplication.ViewModl.CarViewModel;
import com.example.carapplication.databinding.ActivityCarDetailsBinding;

public class CarDetailsActivity extends AppCompatActivity {

    private ActivityCarDetailsBinding binding;
    private CarViewModel carViewModel;
    private int carId;
    private double priceTotel;
    private String statse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        carId = getIntent().getIntExtra("car_id", -1);
        priceTotel = getIntent().getDoubleExtra("total_price", -1);
        statse = getIntent().getStringExtra("status");
        if (statse.equals("booking")){
            binding.btnBook.setText("Active");
            binding.btnBook.setBackgroundResource(R.drawable.shape_booking);
            binding.tvTotalPrice.setText("$ "+priceTotel);
        }else if (statse.equals("not_booking")){
            binding.btnBook.setText("احجز");
            binding.btnBook.setBackgroundResource(R.drawable.shape_book);
        }
        if (priceTotel != -1) {
            binding.tvTotalPrice.setText("$ " + priceTotel);
            binding.btnBook.setBackgroundResource(R.drawable.shape_booking);
            binding.btnBook.setText("Active");
            binding.btnBook.setEnabled(true);
        }


        if (carId == -1) {
            Toast.makeText(this, "خطأ: لم يتم العثور على السيارة", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        carViewModel = new ViewModelProvider(this).get(CarViewModel.class);

        carViewModel.getCarLiveData().observe(this, new Observer<Car>() {
            @Override
            public void onChanged(Car car) {
                if (car != null) {
                    displayCarDetails(car);
                } else {
                    Toast.makeText(CarDetailsActivity.this, "لم يتم العثور على السيارة", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("تفاصيل السيارة");
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        carViewModel.loadCar(carId);

        binding.btnBook.setOnClickListener(v -> {
            Car car = carViewModel.getCarLiveData().getValue();
            Toast.makeText(this, "" + car.getPricePerDay(), Toast.LENGTH_SHORT).show();
            if (car != null && car.isAvailable() && !car.isBooked()) {
                Intent intent = new Intent(CarDetailsActivity.this, BookingActivity.class);
                intent.putExtra("car_id", car.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "هذه السيارة غير متاحة للحجز حالياً", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnShare.setOnClickListener(v -> {
            Car car = carViewModel.getCarLiveData().getValue();
            if (car == null) return;

            String shareText = String.format("🚗 %s %s\n💰 السعر: $%.2f/اليوم\n\nحمّل تطبيق حجز السيارات الآن!", car.getName(), car.getModel(), car.getPricePerDay());

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "مشاركة السيارة"));
        });


    }


    private void loadCarImage(Car car) {
        if (car == null) return;

        try {
            if (car.getImageResId() != 0) {
                try {
                    getResources().getDrawable(car.getImageResId(), getTheme());
                    binding.ivCarImage.setImageResource(car.getImageResId());
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                    binding.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
                }
            } else if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
                Glide.with(this).load(car.getImageUrl()).placeholder(R.drawable.ic_car_placeholder).error(R.drawable.ic_car_placeholder).into(binding.ivCarImage);
            } else {
                binding.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            binding.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }
    }

    private void displayCarDetails(Car car) {
        binding.tvCarName.setText(car.getName());
        binding.tvCarModel.setText(car.getModel() + " • " + car.getYear());
        binding.tvCarColor.setText("اللون: " + (car.getColor() != null ? car.getColor() : "أبيض"));
        binding.tvPricePerDay.setText(String.format("$%.2f / يوم", car.getPricePerDay()));
        binding.tvDescription.setText(car.getDescription() != null ? car.getDescription() : "سيارة ممتازة بمواصفات عالية");
        binding.tvTotalPrice.setText("$ "+car.getPricePerDay());

        if (car.isAvailable() && !car.isBooked()) {
            binding.tvStatus.setText("متاح");
            binding.tvStatus.setTextColor(getResources().getColor(R.color.green));
            binding.btnBook.setEnabled(true);
        } else if (car.isBooked()) {
            binding.tvStatus.setText("محجوزة");
            binding.tvStatus.setTextColor(getResources().getColor(R.color.orange));
            binding.btnBook.setEnabled(false);
        } else {
            binding.tvStatus.setText("غير متاح");
            binding.tvStatus.setTextColor(getResources().getColor(R.color.red));
            binding.btnBook.setEnabled(false);
        }
        loadCarImage(car);
    }
}