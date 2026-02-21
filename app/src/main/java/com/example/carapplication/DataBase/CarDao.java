package com.example.carapplication.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.carapplication.Modle.Car;

import java.util.List;

@Dao
public interface CarDao {

    @Insert
    void insert(Car car);

    @Update
    void update(Car car);

    @Delete
    void delete(Car car);

    @Query("SELECT * FROM cars")
    List<Car> getAllCars();

    @Query("SELECT * FROM cars WHERE isAvailable = 1")
    List<Car> getAvailableCars();

    @Query("SELECT * FROM cars WHERE isBooked = 1")
    List<Car> getBookedCars();

    @Query("SELECT * FROM cars WHERE id = :id")
    Car getCarById(int id);

    @Query("SELECT * FROM cars WHERE name LIKE '%' || :search || '%' OR model LIKE '%' || :search || '%'")
    List<Car> searchCars(String search);

    @Query("UPDATE cars SET isBooked = :isBooked, bookedByUserId = :userId, " +
            "bookedFromDate = :fromDate, bookedToDate = :toDate, currentBookingId = :bookingId " +
            "WHERE id = :carId")
    void updateBookingStatus(int carId, boolean isBooked, int userId,
                             long fromDate, long toDate, int bookingId);

    @Query("UPDATE cars SET isBooked = 0, bookedByUserId = 0, " +
            "bookedFromDate = 0, bookedToDate = 0, currentBookingId = 0 " +
            "WHERE id = :carId")
    void clearBookingStatus(int carId);

}