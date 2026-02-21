package com.example.carapplication.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.carapplication.Modle.Booking;

import java.util.Date;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationType = intent.getStringExtra("notification_type");
        NotificationHelper notificationHelper = new NotificationHelper(context);

        if (notificationType == null) return;

        int bookingId = intent.getIntExtra("booking_id", -1);
        String carName = intent.getStringExtra("car_name");
        long startDateMillis = intent.getLongExtra("start_date", 0);
        long endDateMillis = intent.getLongExtra("end_date", 0);
        double totalPrice = intent.getDoubleExtra("total_price", 0);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCarName(carName);
        booking.setStartDate(new Date(startDateMillis));
        booking.setEndDate(new Date(endDateMillis));
        booking.setTotalPrice(totalPrice);

        switch (notificationType) {
            case "booking_confirmation":
                notificationHelper.sendBookingConfirmationNotification(booking);
                break;

            case "pickup_reminder":
                notificationHelper.sendBookingReminderNotification(booking);
                break;

            case "return_reminder":
                notificationHelper.sendBookingExpiryReminderNotification(booking);
                break;

            case "booking_expired":
                notificationHelper.sendBookingExpiredNotification(booking);
                break;

            case "booking_late":
                notificationHelper.sendBookingLateNotification(booking);
                break;

            case "daily_followup":
                notificationHelper.sendPromotionalNotification(
                        "🔔 تذكير يومي",
                        "لا تنسَ متابعة حجوزاتك والعروض الحصرية"
                );
                break;
        }
    }
}