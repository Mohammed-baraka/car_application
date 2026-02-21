package com.example.carapplication.Modle;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    private static final String PREF_KEY_THEME = "theme_mode";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    public static final int THEME_BATTERY_SAVER = 3;

    public static void applyTheme(Context context) {
        int themeMode = getThemeMode(context);
        setThemeMode(themeMode);
    }

    public static void setThemeMode(int themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case THEME_BATTERY_SAVER:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void saveThemeMode(Context context, int themeMode) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(PREF_KEY_THEME, themeMode).apply();
    }

    public static int getThemeMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_THEME, THEME_SYSTEM);
    }

    public static boolean isDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static String getThemeName(Context context, int themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                return "فاتح";
            case THEME_DARK:
                return "داكن";
            case THEME_SYSTEM:
                return "تبعاً للنظام";
            case THEME_BATTERY_SAVER:
                return "تبعاً لتوفير الطاقة";
            default:
                return "غير معروف";
        }
    }
}