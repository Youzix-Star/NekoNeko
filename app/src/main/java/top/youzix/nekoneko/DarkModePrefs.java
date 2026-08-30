package top.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class DarkModePrefs {
    private static final String PREFS = "dark_mode_prefs";
    private static final String KEY_MODE = "dark_mode";

    public static final int MODE_FOLLOW_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    public static final int MODE_FORCE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_FORCE_DARK = AppCompatDelegate.MODE_NIGHT_YES;

    public static void apply(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int mode = sp.getInt(KEY_MODE, MODE_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void save(Context context, int mode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static int getMode(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_MODE, MODE_FOLLOW_SYSTEM);
    }
}
