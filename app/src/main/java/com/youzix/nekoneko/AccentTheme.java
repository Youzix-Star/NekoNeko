package com.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 手动主题色管理（莫奈动态色不可用/不生效时的兜底方案）。
 * 0 = 跟随系统莫奈（Android 12+）/默认浅蓝，1-5 = 预设主题色。
 */
public class AccentTheme {

    private static final String PREFS = "accent";
    private static final String KEY = "index";

    public static final int DEFAULT = 0;
    public static final int BLUE = 1;
    public static final int GREEN = 2;
    public static final int PURPLE = 3;
    public static final int PINK = 4;
    public static final int ORANGE = 5;

    public static void save(Context context, int index) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY, index)
                .apply();
    }

    public static int load(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY, DEFAULT);
    }

    /** 返回对应 accent overlay 样式；DEFAULT 返回 0（表示不使用自定义 overlay）。 */
    public static int overlayStyle(int index) {
        switch (index) {
            case BLUE:
                return R.style.AccentOverlayBlue;
            case GREEN:
                return R.style.AccentOverlayGreen;
            case PURPLE:
                return R.style.AccentOverlayPurple;
            case PINK:
                return R.style.AccentOverlayPink;
            case ORANGE:
                return R.style.AccentOverlayOrange;
            default:
                return 0;
        }
    }
}
