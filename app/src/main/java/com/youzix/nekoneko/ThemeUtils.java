package com.youzix.nekoneko;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

/**
 * 主题工具：Android 12+ 按系统深浅色应用官方莫奈动态取色 overlay
 * （DayNight 主题下需显式选择 Light/Dark 变体）。
 */
public final class ThemeUtils {

    private ThemeUtils() {
    }

    public static boolean isNightMode(Context context) {
        int mode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    /** 在指定主题上叠加官方莫奈动态取色（Light 或 Dark，随系统）。 */
    public static void applyDynamicColors(Context context, Resources.Theme theme) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return;
        }
        int overlay = isNightMode(context)
                ? com.google.android.material.R.style.ThemeOverlay_Material3_DynamicColors_Dark
                : com.google.android.material.R.style.ThemeOverlay_Material3_DynamicColors_Light;
        theme.applyStyle(overlay, true);
    }
}
