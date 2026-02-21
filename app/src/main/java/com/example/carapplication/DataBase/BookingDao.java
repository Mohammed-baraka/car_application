package com.example.carapplication.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import com.example.carapplication.Modle.Booking;

import java.util.Date;
import java.util.List;

@Dao
public interface BookingDao {

    @Insert
    long insert(Booking booking);

    @Update
    void update(Booking booking);

    @Delete
    void delete(Booking booking);

    @Query("SELECT * FROM bookings WHERE userId = :userId")
    List<Booking> getBookingsByUser(int userId);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status = 'active'")
    List<Booking> getActiveBookings(int userId);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status = 'completed'")
    List<Booking> getCompletedBookings(int userId);

    @Query("SELECT * FROM bookings WHERE userId = :userId AND status = 'cancelled'")
    List<Booking> getCancelledBookings(int userId);

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    Booking getBookingById(int bookingId);


    @Query("SELECT * FROM bookings WHERE carId = :carId AND status = 'active' " +
            "AND ((startDate BETWEEN :startDate AND :endDate) " +
            "OR (endDate BETWEEN :startDate AND :endDate) " +
            "OR (startDate <= :startDate AND endDate >= :endDate))")
    List<Booking> getConflictingBookings(int carId, Date startDate, Date endDate);

    @Query("UPDATE bookings SET status = 'cancelled' WHERE id = :bookingId")
    void cancelBooking(int bookingId);

    @Transaction
    default void cancelBookingAndUpdateCar(int bookingId, int carId) {
        cancelBooking(bookingId);
    }
}