package com.example.carapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carapplication.Adapter.CarAdapter;
import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.DataBase.CarDao;
import com.example.carapplication.Modle.Car;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private CarAdapter carAdapter;
    private List<Car> carList = new ArrayList<>();
    private CarDao carDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppDatabase database = AppDatabase.getInstance(this);
        carDao = database.carDao();

        int idUser = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("user_id", -1);
        if (idUser != -1) {
            binding.tvWelcome.setText(getSharedPreferences("app_prefs", MODE_PRIVATE).getString("user_name", "") + "  " + "مرحباً بك   ");
        }

        setSupportActionBar(binding.toolbar);
        carAdapter = new CarAdapter(carList, car -> {
            Intent intent = new Intent(HomeActivity.this, CarDetailsActivity.class);
            intent.putExtra("car_id", car.getId());
            intent.putExtra("status","not_booking");
            startActivity(intent);
        });

        binding.rvRecommendedCars.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecommendedCars.setAdapter(carAdapter);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_bookings) {
                startActivity(new Intent(this, MyBookingsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        binding.fabSearch.setOnClickListener(v -> {
            String searchText = binding.etSearch.getText().toString().trim();
            searchCars(searchText);
        });

        binding.ivFilter.setOnClickListener(v -> {
            Toast.makeText(this, "عرض خيارات التصفية", Toast.LENGTH_SHORT).show();
        });
        loadCars();
        insertSampleCars();
    }


    private void loadCars() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Car> cars = carDao.getAllCars();
            runOnUiThread(() -> {
                carList.clear();
                carList.addAll(cars);
                carAdapter.updateList(carList);
            });
        });
    }

    private void searchCars(String searchText) {
        if (searchText.isEmpty()) {
            loadCars();
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Car> cars = carDao.searchCars(searchText);
            runOnUiThread(() -> {
                carList.clear();
                carList.addAll(cars);
                carAdapter.updateList(carList);
            });
        });
    }

    private void insertSampleCars() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Car> existingCars = carDao.getAllCars();
            if (existingCars.isEmpty()) {
                // سيارة 1: تويوتا كامري
                Car car1 = new Car(
                        "تويوتا كامري",
                        "كامري",
                        2023,
                        "أبيض لؤلؤي",
                        150.0,
                        R.drawable.car_toyota_camry,
                        "سيارة عائلية فاخرة بتصميم عصري ومريح، مثالية للرحلات الطويلة والاستخدام اليومي. تتميز بكفاءة عالية في استهلاك الوقود وتقنيات أمان متقدمة."
                );
                car1.setAvailable(true);
                car1.setBooked(false);
                carDao.insert(car1);

                Car car2 = new Car(
                        "هيونداي سوناتا",
                        "سوناتا",
                        2022,
                        "أسود لامع",
                        120.0,
                        R.drawable.car_hyundai_sonata,
                        "سيارة اقتصادية مع تقنيات حديثة وتصميم رياضي. توفر راحة عالية وأداء ممتاز مع استهلاك منخفض للوقود."
                );
                car2.setAvailable(true);
                car2.setBooked(false);
                carDao.insert(car2);

                // سيارة 3: مرسيدس E-Class
                Car car3 = new Car(
                        "مرسيدس E-Class",
                        "E 300",
                        2024,
                        "فضي معدني",
                        300.0,
                        R.drawable.car_mercedes_eclass,
                        "سيارة فاخرة من الفئة التنفيذية بتصميم أنيق وتقنيات متطورة. مقصورة داخلية فاخرة مع أنظمة ترفيه ومساعدة متقدمة."
                );
                car3.setAvailable(true);
                car3.setBooked(false);
                carDao.insert(car3);

                // سيارة 4: شيفروليه تاهو
                Car car4 = new Car(
                        "شيفروليه تاهو",
                        "تاهو",
                        2023,
                        "أزرق داكن",
                        250.0,
                        R.drawable.car_chevrolet_tahoe,
                        "سيارة دفع رباعي كبيرة الحجم، مثالية للعائلات والرحلات البرية. تتميز بقوة المحرك ومساحة داخلية واسعة ومريحة."
                );
                car4.setAvailable(true);
                car4.setBooked(false);
                carDao.insert(car4);

                // سيارة 5: فورد موستانج
                Car car5 = new Car(
                        "فورد موستانج",
                        "GT",
                        2023,
                        "أحمر",
                        200.0,
                        R.drawable.car_ford_mustang,
                        "سيارة رياضية كلاسيكية بتصميم جذاب وأداء قوي. محرك V8 يوفر تجربة قيادة مثيرة مع صوت عادم مميز."
                );
                car5.setAvailable(true);
                car5.setBooked(false);
                carDao.insert(car5);


                runOnUiThread(this::loadCars);
            }
        });
    }
}