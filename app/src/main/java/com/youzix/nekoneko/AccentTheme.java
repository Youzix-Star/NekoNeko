package com.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 主题色管理（对齐 legado-with-MD3 的 ThemeStore 思路）：
 * - DEFAULT(0) = GR 色板（默认主题，静态主题本身即是 GR，无需 overlay）
 * - MONET(1)   = Android 12+ 系统莫奈动态色（运行时叠加官方 overlay）
 * - 2-6        = Lemon / WH / Koharu / Sora / Elink 预设色板
 */
public class AccentTheme {

    private static final String PREFS = "accent";
    private static final String KEY = "index";

    public static final int DEFAULT = 0;   // GR
    public static final int MONET = 1;
    public static final int LEMON = 2;
    public static final int WH = 3;
    public static final int KOHARU = 4;
    public static final int SORA = 5;
    public static final int ELINK = 6;

    public static void save(Context context, int index) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY, index)
                .apply();
    }

    public static int load(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY, DEFAULT);
    }

    /** 返回对应色板 overlay 样式；DEFAULT/MONET 返回 0（MONET 由调用方处理动态色）。 */
    public static int overlayStyle(int index) {
        switch (index) {
            case LEMON:
                return R.style.AccentOverlayLemon;
            case WH:
                return R.style.AccentOverlayWH;
            case KOHARU:
                return R.style.AccentOverlayKoharu;
            case SORA:
                return R.style.AccentOverlaySora;
            case ELINK:
                return R.style.AccentOverlayElink;
            default:
                return 0;
        }
    }
}
