package top.youzix.nekoneko;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

/**
 * Color theme manager: switches between predefined palettes by applying
 * color overrides programmatically on the Activity's theme.
 */
public class ColorThemeManager {

    private static final String PREFS = "color_theme_prefs";
    private static final String KEY_THEME = "theme_id";

    public static final int THEME_GR_GREEN = 0;     // Default GR palette
    public static final int THEME_EMBER = 1;         // Warm orange
    public static final int THEME_GLACIER = 2;       // Cool blue

    /**
     * Apply the saved theme to an Activity.
     * Call in onCreate BEFORE setContentView.
     */
    public static void applyTheme(Activity activity) {
        int id = getThemeId(activity);
        if (id == THEME_GR_GREEN) {
            return; // GR Green = base theme, no override needed
        }
        applyPaletteColors(activity, id);
    }

    public static void saveTheme(Context context, int themeId) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_THEME, themeId).apply();
    }

    public static int getThemeId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_THEME, THEME_GR_GREEN);
    }

    /**
     * Returns a human-readable name for the given theme id.
     */
    public static String getThemeName(Context context, int themeId) {
        switch (themeId) {
            case THEME_EMBER:
                return context.getString(R.string.color_theme_ember);
            case THEME_GLACIER:
                return context.getString(R.string.color_theme_glacier);
            default:
                return context.getString(R.string.color_theme_green);
        }
    }

    /**
     * Apply palette color overrides to the activity's theme.
     * We resolve each color value and use TypedValue to set it on the theme.
     */
    private static void applyPaletteColors(Activity activity, int themeId) {
        boolean isNight = isDarkMode();
        int[][] colorMap = getColorMap(themeId, isNight);
        if (colorMap == null) return;

        Resources.Theme theme = activity.getTheme();
        for (int[] pair : colorMap) {
            int attrResId = pair[0];
            int colorValue = pair[1];
            try {
                theme.setAttribute(attrResId, colorValue);
            } catch (Exception e) {
                // Some attributes may not be settable on all API levels
            }
        }
    }

    private static int[][] getColorMap(int themeId, boolean isNight) {
        if (themeId == THEME_EMBER) {
            return isNight ? EMBER_DARK_COLORS : EMBER_LIGHT_COLORS;
        } else if (themeId == THEME_GLACIER) {
            return isNight ? GLACIER_DARK_COLORS : GLACIER_LIGHT_COLORS;
        }
        return null;
    }

    private static boolean isDarkMode() {
        int nightMode = Resources.getSystem().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    // =========================================================
    // Ember Light
    // =========================================================
    private static final int[][] EMBER_LIGHT_COLORS = {
        { R.attr.colorPrimary, 0xFF7C5800 },
        { R.attr.colorOnPrimary, 0xFFFFFFFF },
        { R.attr.colorPrimaryContainer, 0xFFFFDEA1 },
        { R.attr.colorOnPrimaryContainer, 0xFF271900 },
        { R.attr.colorSecondary, 0xFF6E5C3F },
        { R.attr.colorOnSecondary, 0xFFFFFFFF },
        { R.attr.colorSecondaryContainer, 0xFFF8DFBB },
        { R.attr.colorOnSecondaryContainer, 0xFF271900 },
        { R.attr.colorTertiary, 0xFF4D653D },
        { R.attr.colorOnTertiary, 0xFFFFFFFF },
        { R.attr.colorTertiaryContainer, 0xFFCFF0B4 },
        { R.attr.colorOnTertiaryContainer, 0xFF0C2000 },
        { R.attr.colorError, 0xFFBA1A1A },
        { R.attr.colorOnError, 0xFFFFFFFF },
        { R.attr.colorErrorContainer, 0xFFFFDAD6 },
        { R.attr.colorOnErrorContainer, 0xFF93000A },
        { android.R.attr.colorBackground, 0xFFFFF8F3 },
        { R.attr.colorOnBackground, 0xFF201B13 },
        { R.attr.colorSurface, 0xFFFFF8F3 },
        { R.attr.colorOnSurface, 0xFF201B13 },
        { R.attr.colorSurfaceVariant, 0xFFEDE1CF },
        { R.attr.colorOnSurfaceVariant, 0xFF4F4539 },
        { R.attr.colorSurfaceDim, 0xFFE3D7C8 },
        { R.attr.colorSurfaceBright, 0xFFFFF8F3 },
        { R.attr.colorSurfaceContainerLowest, 0xFFFFFFFF },
        { R.attr.colorSurfaceContainerLow, 0xFFFDF1E5 },
        { R.attr.colorSurfaceContainer, 0xFFF7EBD9 },
        { R.attr.colorSurfaceContainerHigh, 0xFFF1E5D3 },
        { R.attr.colorSurfaceContainerHighest, 0xFFEBE0CE },
        { R.attr.colorOutline, 0xFF827567 },
        { R.attr.colorOutlineVariant, 0xFFD5C5B5 },
    };

    // =========================================================
    // Ember Dark
    // =========================================================
    private static final int[][] EMBER_DARK_COLORS = {
        { R.attr.colorPrimary, 0xFFFFDEA1 },
        { R.attr.colorOnPrimary, 0xFF402D00 },
        { R.attr.colorPrimaryContainer, 0xFF5C4100 },
        { R.attr.colorOnPrimaryContainer, 0xFFFFDEA1 },
        { R.attr.colorSecondary, 0xFFDBC4A0 },
        { R.attr.colorOnSecondary, 0xFF3F2E14 },
        { R.attr.colorSecondaryContainer, 0xFF564429 },
        { R.attr.colorOnSecondaryContainer, 0xFFF8DFBB },
        { R.attr.colorTertiary, 0xFFB4D499 },
        { R.attr.colorOnTertiary, 0xFF213612 },
        { R.attr.colorTertiaryContainer, 0xFF374D28 },
        { R.attr.colorOnTertiaryContainer, 0xFFCFF0B4 },
        { R.attr.colorError, 0xFFFFB4AB },
        { R.attr.colorOnError, 0xFF690005 },
        { R.attr.colorErrorContainer, 0xFF93000A },
        { R.attr.colorOnErrorContainer, 0xFFFFDAD6 },
        { android.R.attr.colorBackground, 0xFF18130D },
        { R.attr.colorOnBackground, 0xFFEDE0D0 },
        { R.attr.colorSurface, 0xFF18130D },
        { R.attr.colorOnSurface, 0xFFEDE0D0 },
        { R.attr.colorSurfaceVariant, 0xFF4F4539 },
        { R.attr.colorOnSurfaceVariant, 0xFFD5C5B5 },
        { R.attr.colorSurfaceDim, 0xFF18130D },
        { R.attr.colorSurfaceBright, 0xFF3F3729 },
        { R.attr.colorSurfaceContainerLowest, 0xFF0E0A05 },
        { R.attr.colorSurfaceContainerLow, 0xFF201B13 },
        { R.attr.colorSurfaceContainer, 0xFF251F17 },
        { R.attr.colorSurfaceContainerHigh, 0xFF302A21 },
        { R.attr.colorSurfaceContainerHighest, 0xFF3B342A },
        { R.attr.colorOutline, 0xFF9E8E7E },
        { R.attr.colorOutlineVariant, 0xFF4F4539 },
    };

    // =========================================================
    // Glacier Light
    // =========================================================
    private static final int[][] GLACIER_LIGHT_COLORS = {
        { R.attr.colorPrimary, 0xFF005AC1 },
        { R.attr.colorOnPrimary, 0xFFFFFFFF },
        { R.attr.colorPrimaryContainer, 0xFFD8E2FF },
        { R.attr.colorOnPrimaryContainer, 0xFF001A41 },
        { R.attr.colorSecondary, 0xFF565E71 },
        { R.attr.colorOnSecondary, 0xFFFFFFFF },
        { R.attr.colorSecondaryContainer, 0xFFDAE2F9 },
        { R.attr.colorOnSecondaryContainer, 0xFF131C2C },
        { R.attr.colorTertiary, 0xFF006B5F },
        { R.attr.colorOnTertiary, 0xFFFFFFFF },
        { R.attr.colorTertiaryContainer, 0xFF72F8E3 },
        { R.attr.colorOnTertiaryContainer, 0xFF00201C },
        { R.attr.colorError, 0xFFBA1A1A },
        { R.attr.colorOnError, 0xFFFFFFFF },
        { R.attr.colorErrorContainer, 0xFFFFDAD6 },
        { R.attr.colorOnErrorContainer, 0xFF93000A },
        { android.R.attr.colorBackground, 0xFFFDFBFF },
        { R.attr.colorOnBackground, 0xFF1B1B1F },
        { R.attr.colorSurface, 0xFFFDFBFF },
        { R.attr.colorOnSurface, 0xFF1B1B1F },
        { R.attr.colorSurfaceVariant, 0xFFE0E2EC },
        { R.attr.colorOnSurfaceVariant, 0xFF44474F },
        { R.attr.colorSurfaceDim, 0xFFDBD9DD },
        { R.attr.colorSurfaceBright, 0xFFFDFBFF },
        { R.attr.colorSurfaceContainerLowest, 0xFFFFFFFF },
        { R.attr.colorSurfaceContainerLow, 0xFFF5F3F7 },
        { R.attr.colorSurfaceContainer, 0xFFEFF1F5 },
        { R.attr.colorSurfaceContainerHigh, 0xFFE9EBEF },
        { R.attr.colorSurfaceContainerHighest, 0xFFE3E5E9 },
        { R.attr.colorOutline, 0xFF74777F },
        { R.attr.colorOutlineVariant, 0xFFC4C6D0 },
    };

    // =========================================================
    // Glacier Dark
    // =========================================================
    private static final int[][] GLACIER_DARK_COLORS = {
        { R.attr.colorPrimary, 0xFFADC6FF },
        { R.attr.colorOnPrimary, 0xFF002E69 },
        { R.attr.colorPrimaryContainer, 0xFF004494 },
        { R.attr.colorOnPrimaryContainer, 0xFFD8E2FF },
        { R.attr.colorSecondary, 0xFFBEC6DC },
        { R.attr.colorOnSecondary, 0xFF283041 },
        { R.attr.colorSecondaryContainer, 0xFF3E4759 },
        { R.attr.colorOnSecondaryContainer, 0xFFDAE2F9 },
        { R.attr.colorTertiary, 0xFF51DBC7 },
        { R.attr.colorOnTertiary, 0xFF003731 },
        { R.attr.colorTertiaryContainer, 0xFF005047 },
        { R.attr.colorOnTertiaryContainer, 0xFF72F8E3 },
        { R.attr.colorError, 0xFFFFB4AB },
        { R.attr.colorOnError, 0xFF690005 },
        { R.attr.colorErrorContainer, 0xFF93000A },
        { R.attr.colorOnErrorContainer, 0xFFFFDAD6 },
        { android.R.attr.colorBackground, 0xFF1B1B1F },
        { R.attr.colorOnBackground, 0xFFE3E2E6 },
        { R.attr.colorSurface, 0xFF1B1B1F },
        { R.attr.colorOnSurface, 0xFFE3E2E6 },
        { R.attr.colorSurfaceVariant, 0xFF44474F },
        { R.attr.colorOnSurfaceVariant, 0xFFC4C6D0 },
        { R.attr.colorSurfaceDim, 0xFF1B1B1F },
        { R.attr.colorSurfaceBright, 0xFF3B393D },
        { R.attr.colorSurfaceContainerLowest, 0xFF111318 },
        { R.attr.colorSurfaceContainerLow, 0xFF1B1B1F },
        { R.attr.colorSurfaceContainer, 0xFF202225 },
        { R.attr.colorSurfaceContainerHigh, 0xFF2A2D31 },
        { R.attr.colorSurfaceContainerHighest, 0xFF35373B },
        { R.attr.colorOutline, 0xFF8E9099 },
        { R.attr.colorOutlineVariant, 0xFF44474F },
    };
}
