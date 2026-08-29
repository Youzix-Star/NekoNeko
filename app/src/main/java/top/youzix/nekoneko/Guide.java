package top.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

/** 首次启动引导状态。 */
public class Guide {

    private static final String PREFS = "guide";
    private static final String KEY_DONE = "done";

    public static boolean isDone(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DONE, false);
    }

    public static void markDone(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DONE, true)
                .apply();
    }
}
