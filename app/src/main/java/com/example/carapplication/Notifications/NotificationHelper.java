package com.example.carapplication.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.carapplication.Activity.ConfirmBookingActivity;
import com.example.carapplication.Activity.HomeActivity;
import com.example.carapplication.Activity.MyBookingsActivity;
import com.example.carapplication.Modle.Booking;
import com.example.carapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationHelper {

    private static final String CHANNEL_ID = "car_rental_channel";
    private static final String CHANNEL_NAME = "Car Rental Notifications";
    private static final String CHANNEL_DESC = "Notifications for car rental bookings";

    private Context context;
    private NotificationManager notificationManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendBookingConfirmationNotification(Booking booking) {
        Intent intent = new Intent(context, ConfirmBookingActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("notification_type", "booking_confirmation");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                booking.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "✅ تم تأكيد الحجز بنجاح";
        String message = "سيارتك " + booking.getCarName() + " في انتظارك";

        String bigText = "تفاصيل الحجز:\n" +
                "🚗 السيارة: " + booking.getCarName() + "\n" +
                "📅 تاريخ الاستلام: " + dateFormat.format(booking.getStartDate()) + "\n" +
                "📅 تاريخ الإرجاع: " + dateFormat.format(booking.getEndDate()) + "\n" +
                "💰 السعر الإجمالي: $" + String.format("%.2f", booking.getTotalPrice()) + "\n" +
                "🆔 رقم الحجز: #" + booking.getId();

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_booking)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#4CAF50"))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notificationManager.notify(booking.getId(), notification);
    }

    public void sendBookingReminderNotification(Booking booking) {
        Intent intent = new Intent(context, MyBookingsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("notification_type", "booking_reminder");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                booking.getId() + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "⏰ تذكير بموعد استلام السيارة";
        String message = "غداً موعد استلام سيارتك " + booking.getCarName();

        long hoursLeft = getHoursUntil(booking.getStartDate());
        String timeLeft = hoursLeft > 24 ? (hoursLeft / 24) + " يوم" : hoursLeft + " ساعة";

        String bigText = "🔔 تذكير هام:\n\n" +
                "🚗 السيارة: " + booking.getCarName() + "\n" +
                "⏳ الوقت المتبقي: " + timeLeft + "\n" +
                "📅 موعد الاستلام: " + dateFormat.format(booking.getStartDate()) + "\n\n" +
                "📍 يرجى الحضور في الوقت المحدد\n" +
                "📞 للاستفسار: 966512345678+";

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#2196F3"))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notificationManager.notify(booking.getId() + 1000, notification);
    }

    public void sendBookingExpiryReminderNotification(Booking booking) {
        Intent intent = new Intent(context, MyBookingsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("notification_type", "expiry_reminder");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                booking.getId() + 2000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "⚠️ غداً ينتهي حجز سيارتك";
        String message = "يجب إرجاع " + booking.getCarName() + " غداً";

        long hoursLeft = getHoursUntil(booking.getEndDate());

        String bigText = "🔔 تنبيه هام:\n\n" +
                "🚗 السيارة: " + booking.getCarName() + "\n" +
                "⏳ الوقت المتبقي: " + hoursLeft + " ساعة\n" +
                "📅 موعد الإرجاع: " + dateFormat.format(booking.getEndDate()) + "\n\n" +
                "⚠️ في حالة التأخير سيتم تطبيق رسوم إضافية\n" +
                "📍 يرجى إرجاع السيارة في الوقت المحدد";

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_warning)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#FF9800"))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notificationManager.notify(booking.getId() + 2000, notification);
    }

    public void sendBookingExpiredNotification(Booking booking) {
        Intent intent = new Intent(context, MyBookingsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("notification_type", "booking_expired");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                booking.getId() + 3000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "⌛ انتهت مدة حجز سيارتك";
        String message = "تم انتهاء حجز " + booking.getCarName();

        String bigText = "✅ تم إنهاء الحجز بنجاح\n\n" +
                "🚗 السيارة: " + booking.getCarName() + "\n" +
                "📅 تاريخ الإرجاع: " + dateFormat.format(booking.getEndDate()) + "\n" +
                "💰 المبلغ المدفوع: $" + String.format("%.2f", booking.getTotalPrice()) + "\n\n" +
                "⭐ شكراً لاستخدامك تطبيقنا\n" +
                "نتمنى أن تكون رحلتك ممتعة";

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_completed)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#9C27B0"))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notificationManager.notify(booking.getId() + 3000, notification);
    }

    public void sendBookingLateNotification(Booking booking) {
        Intent intent = new Intent(context, MyBookingsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("notification_type", "booking_late");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                booking.getId() + 4000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long lateHours = getHoursSince(booking.getEndDate());
        double lateFee = lateHours * 10;

        String title = "⚠️ أنت متأخر في إرجاع السيارة";
        String message = "تأخرت " + lateHours + " ساعة";

        String bigText = "🔴 تنبيه هام:\n\n" +
                "🚗 السيارة: " + booking.getCarName() + "\n" +
                "⏰ مدة التأخير: " + lateHours + " ساعة\n" +
                "💰 الرسوم الإضافية: $" + String.format("%.2f", lateFee) + "\n\n" +
                "⚠️ يرجى إرجاع السيارة فوراً\n" +
                "📞 اتصل بنا: 966512345678+";

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_late)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#F44336"))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notificationManager.notify(booking.getId() + 4000, notification);
    }

    public void sendPromotionalNotification(String title, String message) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_booking)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#FF9800"))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    private long getHoursUntil(Date targetDate) {
        long diffInMillis = targetDate.getTime() - System.currentTimeMillis();
        return TimeUnit.HOURS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    private long getHoursSince(Date targetDate) {
        long diffInMillis = System.currentTimeMillis() - targetDate.getTime();
        return TimeUnit.HOURS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }


    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}