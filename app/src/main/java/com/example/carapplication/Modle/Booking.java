package com.example.carapplication.Modle;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Entity(tableName = "bookings")
public class Booking {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "carId")
    private int carId;
    @ColumnInfo(name = "carName")
    private String carName;
    
    @ColumnInfo(name = "userId")
    private int userId;
    
    @ColumnInfo(name = "startDate")
    private Date startDate;
    
    @ColumnInfo(name = "endDate")
    private Date endDate;
    
    @ColumnInfo(name = "totalPrice")
    private double totalPrice;
    
    @ColumnInfo(name = "status")
    private String status;
    
    @ColumnInfo(name = "bookingDate")
    private Date bookingDate;
    
    @ColumnInfo(name = "cancelDate")
    private Date cancelDate;
    
    @ColumnInfo(name = "cancelReason")
    private String cancelReason;
    
    @ColumnInfo(name = "cancelledBy")
    private String cancelledBy;

    public Booking() {
        this.bookingDate = new Date();
        this.status = "active";
    }

    @Ignore
    public Booking(int carId, int userId, Date startDate, Date endDate, double totalPrice) {
        this.carId = carId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = "active";
        this.bookingDate = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getCarId() { return carId; }
    public void setCarId(int carId) { this.carId = carId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }
    
    public Date getCancelDate() { return cancelDate; }
    public void setCancelDate(Date cancelDate) { this.cancelDate = cancelDate; }
    
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    
    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

    public long getDurationInDays() {
        if (startDate == null || endDate == null) return 0;
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }
    
    public String getStatusText() {
        if (status == null) return "غير معروف";
        switch (status) {
            case "active": return "نشط";
            case "completed": return "منتهي";
            case "cancelled": return "ملغي";
            case "pending": return "قيد الانتظار";
            default: return "غير معروف";
        }
    }
    
    public int getStatusColor() {
        if (status == null) return 0xFF9E9E9E;
        switch (status) {
            case "active": return 0xFF4CAF50; // أخضر
            case "completed": return 0xFF2196F3; // أزرق
            case "cancelled": return 0xFFF44336; // أحمر
            case "pending": return 0xFFFF9800; // برتقالي
            default: return 0xFF9E9E9E; // رمادي
        }
    }
}