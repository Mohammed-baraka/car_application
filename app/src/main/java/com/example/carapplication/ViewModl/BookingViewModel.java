package com.example.carapplication.ViewModl;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.DataBase.BookingDao;
import com.example.carapplication.Modle.Booking;

public class BookingViewModel extends AndroidViewModel {
    private BookingDao bookingDao;

    public BookingViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        bookingDao = database.bookingDao();
    }

    public void insert(Booking booking) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            bookingDao.insert(booking);
        });
    }
    
    public void update(Booking booking) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            bookingDao.update(booking);
        });
    }
    
    public void delete(Booking booking) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            bookingDao.delete(booking);
        });
    }
}