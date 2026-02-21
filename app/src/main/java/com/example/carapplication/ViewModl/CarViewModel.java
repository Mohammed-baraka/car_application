package com.example.carapplication.ViewModl;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.Modle.Car;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarViewModel extends AndroidViewModel {

    private AppDatabase database;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private MutableLiveData<List<Car>> allCarsLiveData = new MutableLiveData<>();
    private MutableLiveData<Car> carLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public CarViewModel(Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
    }

    public void loadAllCars() {
        loadingLiveData.postValue(true);

        executorService.execute(() -> {
            try {
                List<Car> cars = database.carDao().getAllCars();
                allCarsLiveData.postValue(cars);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    public void loadCar(int carId) {
        loadingLiveData.postValue(true);

        executorService.execute(() -> {
            try {
                Car car = database.carDao().getCarById(carId);
                carLiveData.postValue(car);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                loadingLiveData.postValue(false);
            }
        });
    }

    public void loadAvailableCars() {
        executorService.execute(() -> {
            try {
                List<Car> cars = database.carDao().getAvailableCars();
                allCarsLiveData.postValue(cars);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    public void searchCars(String query) {
        executorService.execute(() -> {
            try {
                List<Car> cars = database.carDao().searchCars(query);
                allCarsLiveData.postValue(cars);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    public LiveData<List<Car>> getAllCarsLiveData() {
        return allCarsLiveData;
    }

    public LiveData<Car> getCarLiveData() {
        return carLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}