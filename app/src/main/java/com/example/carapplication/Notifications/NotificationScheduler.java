package com.example.carapplication.Notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.carapplication.Modle.Booking;

import java.util.Calendar;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    public static void scheduleAllBookingNotifications(Context context, Booking booking) {
        sendImmediateConfirmation(context, booking);
        schedulePickupReminder(context, booking);
        scheduleReturnReminder(context, booking);
        scheduleExpiryNotification(context, booking);
        scheduleWelcomeNotification(context, booking);
    }

    private static void sendImmediateConfirmation(Context context, Booking booking) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("notification_type", "booking_confirmation");
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("car_name", booking.getCarName());
        intent.putExtra("start_date", booking.getStartDate().getTime());
        intent.putExtra("end_date", booking.getEndDate().getTime());
        intent.putExtra("total_price", booking.getTotalPrice());

        context.sendBroadcast(intent);
    }

    private static void schedulePickupReminder(Context context, Booking booking) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTime(booking.getStartDate());
        reminderTime.add(Calendar.HOUR_OF_DAY, -24);

        if (reminderTime.getTimeInMillis() <= System.currentTimeMillis()) {
            reminderTime = Calendar.getInstance();
            reminderTime.add(Calendar.MINUTE, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("notification_type", "pickup_reminder");
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("car_name", booking.getCarName());
        intent.putExtra("start_date", booking.getStartDate().getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                booking.getId() + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        setAlarm(alarmManager, reminderTime.getTimeInMillis(), pendingIntent);
    }

    public static void scheduleReturnReminder(Context context, Booking booking) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTime(booking.getEndDate());
        reminderTime.add(Calendar.HOUR_OF_DAY, -24);

        if (reminderTime.getTimeInMillis() <= System.currentTimeMillis()) {
            reminderTime = Calendar.getInstance();
            reminderTime.add(Calendar.MINUTE, 2);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("notification_type", "return_reminder");
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("car_name", booking.getCarName());
        intent.putExtra("end_date", booking.getEndDate().getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                booking.getId() + 2000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        setAlarm(alarmManager, reminderTime.getTimeInMillis(), pendingIntent);
    }

    private static void scheduleExpiryNotification(Context context, Booking booking) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar expiryTime = Calendar.getInstance();
        expiryTime.setTime(booking.getEndDate());
        expiryTime.set(Calendar.HOUR_OF_DAY, 12);
        expiryTime.set(Calendar.MINUTE, 0);
        expiryTime.set(Calendar.SECOND, 0);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("notification_type", "booking_expired");
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("car_name", booking.getCarName());
        intent.putExtra("end_date", booking.getEndDate().getTime());
        intent.putExtra("total_price", booking.getTotalPrice());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                booking.getId() + 3000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        setAlarm(alarmManager, expiryTime.getTimeInMillis(), pendingIntent);
    }

    private static void scheduleWelcomeNotification(Context context, Booking booking) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar welcomeTime = Calendar.getInstance();
        welcomeTime.add(Calendar.HOUR_OF_DAY, 1);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("notification_type", "welcome_after_booking");
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("car_name", booking.getCarName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                booking.getId() + 5000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        setAlarm(alarmManager, welcomeTime.getTimeInMillis(), pendingIntent);
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleDailyFollowUp(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("notification_type", "daily_followup");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private static void setAlarm(AlarmManager alarmManager, long timeInMillis, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
            );
        }
    }

    public static void cancelAllReminders(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (int i = 1; i <= 10000; i++) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    i,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    public static void cancelBookingReminders(Context context, int bookingId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int[] requestCodes = {
                bookingId,
                bookingId + 1000,
                bookingId + 2000,
                bookingId + 3000,
                bookingId + 4000,
                bookingId + 5000
        };

        for (int code : requestCodes) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    code,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }
}