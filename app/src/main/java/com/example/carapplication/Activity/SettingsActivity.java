package com.example.carapplication.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.Modle.ThemeHelper;
import com.example.carapplication.Notifications.NotificationHelper;
import com.example.carapplication.Notifications.NotificationScheduler;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_THEME_CHANGE = 1001;
    private ActivitySettingsBinding binding;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        editor = preferences.edit();

        notificationHelper = new NotificationHelper(this);

        binding.tvVersion.setText(getVersionName());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("الإعدادات");
        }

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        loadCurrentSettings();

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();

            if (isChecked) {
                Toast.makeText(this, "✅ تم تفعيل الإشعارات", Toast.LENGTH_SHORT).show();
            } else {
                NotificationScheduler.cancelAllReminders(this);
                notificationHelper.cancelAllNotifications();
                Toast.makeText(this, "❌ تم إيقاف الإشعارات", Toast.LENGTH_SHORT).show();
            }
        });

        binding.cardNotifications.setOnClickListener(v -> {
            binding.switchNotifications.setChecked(!binding.switchNotifications.isChecked());
        });
        ;
        setupDarkModeSwitch();

        binding.cardDarkMode.setOnClickListener(v -> showThemeDialog());
        binding.cardLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.cardFontSize.setOnClickListener(v -> showFontSizeDialog());
        binding.cardClearData.setOnClickListener(v -> showClearDataConfirmationDialog());
        binding.cardAbout.setOnClickListener(v -> showAboutDialog());

        binding.cardContact.setOnClickListener(v -> showContactDialog());
        binding.btnContactUs.setOnClickListener(v -> showContactDialog());
        binding.btnSaveSettings.setOnClickListener(v -> {
            editor.apply();
            Toast.makeText(this, "💾 تم حفظ الإعدادات", Toast.LENGTH_SHORT).show();
            recreate();
        });
    }

    private void loadCurrentSettings() {
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", true);
        binding.switchNotifications.setChecked(notificationsEnabled);

        boolean darkModeEnabled = preferences.getBoolean("dark_mode_enabled", false);
        binding.switchDarkMode.setChecked(darkModeEnabled);
        if (darkModeEnabled) {
            binding.tvThemeDesc.setText("داكن");
        } else {
            binding.tvThemeDesc.setText("فاتح");
        }

        updateThemeDisplay();

        String language = preferences.getString("language", "ar");
        binding.tvLanguage.setText(language.equals("ar") ? "العربية" : "English");

        String fontSize = preferences.getString("font_size", "medium");
        switch (fontSize) {
            case "small":
                binding.tvFontSize.setText("صغير");
                break;
            case "large":
                binding.tvFontSize.setText("كبير");
                break;
            default:
                binding.tvFontSize.setText("متوسط");
                break;
        }
    }

    private void updateThemeDisplay() {
        int currentTheme = ThemeHelper.getThemeMode(this);
        binding.tvDarkModeTitle.setText(ThemeHelper.getThemeName(this, currentTheme));
    }

    private void setupDarkModeSwitch() {
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("dark_mode_enabled", isChecked);
            editor.apply();

            if (isChecked) {
                ThemeHelper.saveThemeMode(this, ThemeHelper.THEME_DARK);
                ThemeHelper.setThemeMode(ThemeHelper.THEME_DARK);
            } else {
                ThemeHelper.saveThemeMode(this, ThemeHelper.THEME_LIGHT);
                ThemeHelper.setThemeMode(ThemeHelper.THEME_LIGHT);
            }

            updateThemeDisplay();

            String mode = isChecked ? "الوضع الداكن" : "الوضع الفاتح";
            Toast.makeText(this, "🌓 تم تفعيل " + mode, Toast.LENGTH_SHORT).show();

            recreate();
        });

        binding.cardDarkMode.setOnClickListener(v -> {
            binding.switchDarkMode.setChecked(!binding.switchDarkMode.isChecked());
        });
    }


    private void showThemeDialog() {
        String[] themes = {"فاتح", "داكن", "تبعاً للنظام", "تبعاً لتوفير الطاقة"};
        int currentTheme = ThemeHelper.getThemeMode(this);

        new AlertDialog.Builder(this)
                .setTitle("اختر المظهر")
                .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                    ThemeHelper.saveThemeMode(this, which);
                    updateThemeDisplay();
                    ThemeHelper.setThemeMode(which);
                    binding.switchDarkMode.setChecked(which == ThemeHelper.THEME_DARK);

                    String themeName = ThemeHelper.getThemeName(this, which);
                    Toast.makeText(this, "تم تطبيق المظهر: " + themeName, Toast.LENGTH_SHORT).show();

                    recreate();
                    dialog.dismiss();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {"العربية", "English"};
        int currentLang = preferences.getString("language", "ar").equals("ar") ? 0 : 1;

        new AlertDialog.Builder(this)
                .setTitle("اختر اللغة")
                .setSingleChoiceItems(languages, currentLang, (dialog, which) -> {
                    String selectedLang = which == 0 ? "ar" : "en";
                    editor.putString("language", selectedLang);
                    editor.apply();

                    binding.tvLanguage.setText(which == 0 ? "العربية" : "English");

                    Toast.makeText(this, "سيتم تطبيق اللغة بعد إعادة التشغيل", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }


    private void showFontSizeDialog() {
        String[] sizes = {"صغير", "متوسط", "كبير"};
        int currentSize = 1;

        String savedSize = preferences.getString("font_size", "medium");
        switch (savedSize) {
            case "small":
                currentSize = 0;
                break;
            case "large":
                currentSize = 2;
                break;
            default:
                currentSize = 1;
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle("اختر حجم الخط")
                .setSingleChoiceItems(sizes, currentSize, (dialog, which) -> {
                    String selectedSize;
                    String displaySize;

                    switch (which) {
                        case 0:
                            selectedSize = "small";
                            displaySize = "صغير";
                            break;
                        case 2:
                            selectedSize = "large";
                            displaySize = "كبير";
                            break;
                        default:
                            selectedSize = "medium";
                            displaySize = "متوسط";
                            break;
                    }

                    editor.putString("font_size", selectedSize);
                    editor.apply();
                    binding.tvFontSize.setText(displaySize);

                    Toast.makeText(this, "تم تغيير حجم الخط", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showClearDataConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ مسح البيانات")
                .setMessage("هل أنت متأكد من مسح جميع البيانات المحلية؟\n\nسيتم مسح:"
                        + "\n• سجل الحجوزات"
                        + "\n• قائمة المفضلة"
                        + "\n\nلن يتم مسح بيانات الحساب الشخصي.")
                .setPositiveButton("نعم، مسح", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearAppData();
                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("is_logged_in", false).apply();
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void clearAppData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getInstance(this).clearAllTables();

            runOnUiThread(() -> {
                editor.putBoolean("notifications_enabled", true);
                editor.putString("font_size", "medium");
                editor.apply();

                loadCurrentSettings();
                Toast.makeText(this, "✅ تم مسح جميع البيانات المحلية", Toast.LENGTH_LONG).show();
            });
        });
    }


    private void showAboutDialog() {
        String aboutMessage = "🚗 تطبيق حجز السيارات\n\n"
                + "الإصدار: " + getVersionName() + "\n"
                + "مميزات التطبيق:\n"
                + "✓ حجز السيارات بسهولة\n"
                + "✓ تتبع الحجوزات\n"
                + "✓ إشعارات وتذكيرات\n"
                + "✓ واجهة عربية سهلة\n"
                + "✓ دعم الوضع الداكن\n\n";

        new AlertDialog.Builder(this)
                .setTitle(" عن التطبيق")
                .setMessage(aboutMessage)
                .setPositiveButton("موافق", null)
                .setIcon(R.drawable.ic_info)
                .show();
    }


    private void showContactDialog() {
        String[] contactOptions = {"📧 البريد الإلكتروني", "📞 الهاتف", "🌐 الموقع الإلكتروني", "📱 واتساب"};

        new AlertDialog.Builder(this)
                .setTitle("📱 تواصل معنا")
                .setItems(contactOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sendEmail();
                            break;
                        case 1:
                            makePhoneCall();
                            break;
                        case 2:
                            openWebsite();
                            break;
                        case 3:
                            openWhatsApp();
                            break;
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@carrental.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "استفسار عن تطبيق حجز السيارات");
        startActivity(Intent.createChooser(intent, "إرسال بريد إلكتروني"));
    }

    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(android.net.Uri.parse("tel:+966512345678"));
        startActivity(intent);
    }

    private void openWebsite() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse("https://www.carrental.com"));
        startActivity(intent);
    }

    private void openWhatsApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://wa.me/966512345678"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "تطبيق واتساب غير مثبت", Toast.LENGTH_SHORT).show();
        }
    }

    private String getVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}