package com.example.carapplication.Modle;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "cars")
public class Car {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "model")
    private String model;
    @ColumnInfo(name = "year")
    private int year;
    @ColumnInfo(name = "color")
    private String color;
    @ColumnInfo(name = "pricePerDay")
    private double pricePerDay;
    @ColumnInfo(name = "imageResId")
    private int imageResId;
    @ColumnInfo(name = "imageUrl")
    private String imageUrl;
    @ColumnInfo(name = "isAvailable")
    private boolean isAvailable;
    @ColumnInfo(name = "description")
    private String description;
    
    private boolean isBooked;
    @ColumnInfo(name = "bookedByUserId")
    private int bookedByUserId;
    @ColumnInfo(name = "bookedFromDate")
    private Date bookedFromDate;
    @ColumnInfo(name = "bookedToDate")
    private Date bookedToDate;
    @ColumnInfo(name = "currentBookingId")
    private int currentBookingId;

    @ColumnInfo(name = "lastAvailableDate")
    private Date lastAvailableDate;
    
    @ColumnInfo(name = "totalBookingsCount")
    private int totalBookingsCount;
    
    @ColumnInfo(name = "cancelledBookingsCount")
    private int cancelledBookingsCount;

    public Car() {
        this.isAvailable = true;
        this.isBooked = false;
        this.totalBookingsCount = 0;
        this.cancelledBookingsCount = 0;
    }
    @Ignore
    public Car(String name, String model, int year, String color, 
               double pricePerDay, int imageResId, String description) {
        this.name = name;
        this.model = model;
        this.year = year;
        this.color = color;
        this.pricePerDay = pricePerDay;
        this.imageResId = imageResId;
        this.description = description;
        this.isAvailable = true;
        this.isBooked = false;
        this.totalBookingsCount = 0;
        this.cancelledBookingsCount = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }
    
    public int getBookedByUserId() { return bookedByUserId; }
    public void setBookedByUserId(int bookedByUserId) { this.bookedByUserId = bookedByUserId; }
    
    public Date getBookedFromDate() { return bookedFromDate; }
    public void setBookedFromDate(Date bookedFromDate) { this.bookedFromDate = bookedFromDate; }
    
    public Date getBookedToDate() { return bookedToDate; }
    public void setBookedToDate(Date bookedToDate) { this.bookedToDate = bookedToDate; }
    
    public int getCurrentBookingId() { return currentBookingId; }
    public void setCurrentBookingId(int currentBookingId) { this.currentBookingId = currentBookingId; }
    
    public Date getLastAvailableDate() { return lastAvailableDate; }
    public void setLastAvailableDate(Date lastAvailableDate) { this.lastAvailableDate = lastAvailableDate; }
    
    public int getTotalBookingsCount() { return totalBookingsCount; }
    public void setTotalBookingsCount(int totalBookingsCount) { this.totalBookingsCount = totalBookingsCount; }
    
    public int getCancelledBookingsCount() { return cancelledBookingsCount; }
    public void setCancelledBookingsCount(int cancelledBookingsCount) { this.cancelledBookingsCount = cancelledBookingsCount; }

}