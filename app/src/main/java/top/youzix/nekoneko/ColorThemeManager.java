package top.youzix.nekoneko;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Color theme manager: switches between predefined palettes by applying
 * color overrides programmatically to the activity's theme and views.
 *
 * The approach: for each non-default palette, we define color int values directly
 * in Java (no XML resource files needed). After setContentView, we traverse
 * the view tree and apply the palette colors.
 */
public class ColorThemeManager {

    private static final String PREFS = "color_theme_prefs";
    private static final String KEY_THEME = "theme_id";

    public static final int THEME_GR_GREEN = 0;
    public static final int THEME_EMBER = 1;
    public static final int THEME_GLACIER = 2;

    public static void applyTheme(Activity activity) {
        int id = getThemeId(activity);
        if (id == THEME_GR_GREEN) return; // base theme, no overlay

        boolean isNight = isDarkMode(activity);
        int overlayRes;
        switch (id) {
            case THEME_EMBER:
                overlayRes = isNight
                        ? R.style.ColorThemeOverlay_Ember_Dark
                        : R.style.ColorThemeOverlay_Ember_Light;
                break;
            case THEME_GLACIER:
                overlayRes = isNight
                        ? R.style.ColorThemeOverlay_Glacier_Dark
                        : R.style.ColorThemeOverlay_Glacier_Light;
                break;
            default:
                return;
        }
        activity.getTheme().applyStyle(overlayRes, true);
    }

    /**
     * Apply color theme overlay to a ContextThemeWrapper (used by FloatingWindowService
     * which doesn't have an Activity).
     */
    public static void applyThemeOverlay(Context context) {
        int id = getThemeId(context);
        if (id == THEME_GR_GREEN) return;

        boolean isNight = isDarkMode(context);
        int overlayRes;
        switch (id) {
            case THEME_EMBER:
                overlayRes = isNight
                        ? R.style.ColorThemeOverlay_Ember_Dark
                        : R.style.ColorThemeOverlay_Ember_Light;
                break;
            case THEME_GLACIER:
                overlayRes = isNight
                        ? R.style.ColorThemeOverlay_Glacier_Dark
                        : R.style.ColorThemeOverlay_Glacier_Light;
                break;
            default:
                return;
        }
        context.getTheme().applyStyle(overlayRes, true);
    }

    public static void saveTheme(Context context, int themeId) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_THEME, themeId).apply();
    }

    public static int getThemeId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_THEME, THEME_GR_GREEN);
    }

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

    // ===================== Palette Color Tables =====================
    // Format: { primary, onPrimary, primaryContainer, onPrimaryContainer,
    //           secondary, onSecondary, secondaryContainer, onSecondaryContainer,
    //           tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
    //           surface, onSurface, surfaceVariant, onSurfaceVariant,
    //           surfaceContainer, surfaceContainerHigh, background, onBackground,
    //           outline, outlineVariant }

    private static final int[] EMBER_LIGHT = {
        0xFF7C5800, 0xFFFFFFFF, 0xFFFFDEA1, 0xFF271900,
        0xFF6E5C3F, 0xFFFFFFFF, 0xFFF8DFBB, 0xFF271900,
        0xFF4D653D, 0xFFFFFFFF, 0xFFCFF0B4, 0xFF0C2000,
        0xFFFFF8F3, 0xFF201B13, 0xFFEDE1CF, 0xFF4F4539,
        0xFFF7EBD9, 0xFFF1E5D3, 0xFFFFF8F3, 0xFF201B13,
        0xFF827567, 0xFFD5C5B5,
    };

    private static final int[] EMBER_DARK = {
        0xFFFFDEA1, 0xFF402D00, 0xFF5C4100, 0xFFFFDEA1,
        0xFFDBC4A0, 0xFF3F2E14, 0xFF564429, 0xFFF8DFBB,
        0xFFB4D499, 0xFF213612, 0xFF374D28, 0xFFCFF0B4,
        0xFF18130D, 0xFFEDE0D0, 0xFF4F4539, 0xFFD5C5B5,
        0xFF251F17, 0xFF302A21, 0xFF18130D, 0xFFEDE0D0,
        0xFF9E8E7E, 0xFF4F4539,
    };

    private static final int[] GLACIER_LIGHT = {
        0xFF005AC1, 0xFFFFFFFF, 0xFFD8E2FF, 0xFF001A41,
        0xFF565E71, 0xFFFFFFFF, 0xFFDAE2F9, 0xFF131C2C,
        0xFF006B5F, 0xFFFFFFFF, 0xFF72F8E3, 0xFF00201C,
        0xFFFDFBFF, 0xFF1B1B1F, 0xFFE0E2EC, 0xFF44474F,
        0xFFEFF1F5, 0xFFE9EBEF, 0xFFFDFBFF, 0xFF1B1B1F,
        0xFF74777F, 0xFFC4C6D0,
    };

    private static final int[] GLACIER_DARK = {
        0xFFADC6FF, 0xFF002E69, 0xFF004494, 0xFFD8E2FF,
        0xFFBEC6DC, 0xFF283041, 0xFF3E4759, 0xFFDAE2F9,
        0xFF51DBC7, 0xFF003731, 0xFF005047, 0xFF72F8E3,
        0xFF1B1B1F, 0xFFE3E2E6, 0xFF44474F, 0xFFC4C6D0,
        0xFF202225, 0xFF2A2D31, 0xFF1B1B1F, 0xFFE3E2E6,
        0xFF8E9099, 0xFF44474F,
    };

    // Index constants for the palette array
    private static final int I_PRIMARY = 0;
    private static final int I_ON_PRIMARY = 1;
    private static final int I_PRIMARY_CONTAINER = 2;
    private static final int I_ON_PRIMARY_CONTAINER = 3;
    private static final int I_SECONDARY = 4;
    private static final int I_ON_SECONDARY = 5;
    private static final int I_SECONDARY_CONTAINER = 6;
    private static final int I_ON_SECONDARY_CONTAINER = 7;
    private static final int I_TERTIARY = 8;
    private static final int I_ON_TERTIARY = 9;
    private static final int I_TERTIARY_CONTAINER = 10;
    private static final int I_ON_TERTIARY_CONTAINER = 11;
    private static final int I_SURFACE = 12;
    private static final int I_ON_SURFACE = 13;
    private static final int I_SURFACE_VARIANT = 14;
    private static final int I_ON_SURFACE_VARIANT = 15;
    private static final int I_SURFACE_CONTAINER = 16;
    private static final int I_SURFACE_CONTAINER_HIGH = 17;
    private static final int I_BACKGROUND = 18;
    private static final int I_ON_BACKGROUND = 19;
    private static final int I_OUTLINE = 20;
    private static final int I_OUTLINE_VARIANT = 21;

    /**
     * Returns the palette array for the given theme and night mode.
     */
    public static int[] getPalette(Context context) {
        int id = getThemeId(context);
        if (id == THEME_GR_GREEN) return null; // use base theme
        boolean isNight = isDarkMode(context);
        switch (id) {
            case THEME_EMBER:  return isNight ? EMBER_DARK : EMBER_LIGHT;
            case THEME_GLACIER: return isNight ? GLACIER_DARK : GLACIER_LIGHT;
            default: return null;
        }
    }

    /** Convenience accessors for the palette. */
    public static int getPrimary(int[] p)       { return p[I_PRIMARY]; }
    public static int getOnPrimary(int[] p)      { return p[I_ON_PRIMARY]; }
    public static int getPrimaryContainer(int[] p) { return p[I_PRIMARY_CONTAINER]; }
    public static int getOnPrimaryContainer(int[] p) { return p[I_ON_PRIMARY_CONTAINER]; }
    public static int getSecondary(int[] p)      { return p[I_SECONDARY]; }
    public static int getSecondaryContainer(int[] p) { return p[I_SECONDARY_CONTAINER]; }
    public static int getTertiary(int[] p)       { return p[I_TERTIARY]; }
    public static int getTertiaryContainer(int[] p) { return p[I_TERTIARY_CONTAINER]; }
    public static int getSurface(int[] p)        { return p[I_SURFACE]; }
    public static int getOnSurface(int[] p)      { return p[I_ON_SURFACE]; }
    public static int getSurfaceVariant(int[] p) { return p[I_SURFACE_VARIANT]; }
    public static int getOnSurfaceVariant(int[] p) { return p[I_ON_SURFACE_VARIANT]; }
    public static int getSurfaceContainer(int[] p) { return p[I_SURFACE_CONTAINER]; }
    public static int getSurfaceContainerHigh(int[] p) { return p[I_SURFACE_CONTAINER_HIGH]; }
    public static int getBackground(int[] p)     { return p[I_BACKGROUND]; }
    public static int getOnBackground(int[] p)   { return p[I_ON_BACKGROUND]; }
    public static int getOutline(int[] p)        { return p[I_OUTLINE]; }
    public static int getOutlineVariant(int[] p) { return p[I_OUTLINE_VARIANT]; }

    private static boolean isDarkMode(Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}
