package top.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 悬浮窗显示设置：控制悬浮窗中哪些元素可见。
 */
public class FloatingWindowPrefs {

    private static final String PREFS = "floating_window_prefs";

    private static final String KEY_SHOW_CAPTURE_TEXT = "show_capture_text";
    private static final String KEY_SHOW_APPLY_RULES = "show_apply_rules";
    private static final String KEY_SHOW_AI_MODIFY = "show_ai_modify";
    private static final String KEY_SHOW_LOG = "show_log";
    private static final String KEY_SHOW_QUICK_BALL = "show_quick_ball";

    public static class Prefs {
        public boolean showCaptureText = true;
        public boolean showApplyRules = true;
        public boolean showAiModify = true;
        public boolean showLog = true;
        public boolean showQuickBall = false;
    }

    public static Prefs load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Prefs p = new Prefs();
        p.showCaptureText = sp.getBoolean(KEY_SHOW_CAPTURE_TEXT, true);
        p.showApplyRules = sp.getBoolean(KEY_SHOW_APPLY_RULES, true);
        p.showAiModify = sp.getBoolean(KEY_SHOW_AI_MODIFY, true);
        p.showLog = sp.getBoolean(KEY_SHOW_LOG, true);
        p.showQuickBall = sp.getBoolean(KEY_SHOW_QUICK_BALL, false);
        return p;
    }

    public static void save(Context context, Prefs p) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SHOW_CAPTURE_TEXT, p.showCaptureText)
                .putBoolean(KEY_SHOW_APPLY_RULES, p.showApplyRules)
                .putBoolean(KEY_SHOW_AI_MODIFY, p.showAiModify)
                .putBoolean(KEY_SHOW_LOG, p.showLog)
                .putBoolean(KEY_SHOW_QUICK_BALL, p.showQuickBall)
                .apply();
    }
}
