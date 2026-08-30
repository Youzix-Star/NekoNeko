package top.youzix.nekoneko;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 悬浮窗显示设置：控制悬浮窗中哪些元素可见，以及快捷悬浮球的自定义样式。
 * 大悬浮窗和快捷悬浮球互斥，不能同时开启。
 */
public class FloatingWindowPrefs {

    private static final String PREFS = "floating_window_prefs";

    // 大悬浮窗元素
    private static final String KEY_SHOW_CAPTURE_TEXT = "show_capture_text";
    private static final String KEY_SHOW_APPLY_RULES = "show_apply_rules";
    private static final String KEY_SHOW_AI_MODIFY = "show_ai_modify";
    private static final String KEY_SHOW_LOG = "show_log";

    // 悬浮球
    private static final String KEY_SHOW_QUICK_BALL = "show_quick_ball";
    private static final String KEY_BALL_CONTENT_TYPE = "ball_content_type"; // "icon" or "text"
    private static final String KEY_BALL_TEXT = "ball_text";
    private static final String KEY_BALL_SIZE_DP = "ball_size_dp";
    private static final String KEY_BALL_CORNER_DP = "ball_corner_dp";
    private static final String KEY_BALL_POS_X = "ball_pos_x";
    private static final String KEY_BALL_POS_Y = "ball_pos_y";

    /** 内容类型常量 */
    public static final String BALL_ICON = "icon";
    public static final String BALL_TEXT = "text";

    /** 设置变更监听器。 */
    public interface OnPrefsChangedListener {
        void onPrefsChanged(Prefs prefs);
    }

    private static volatile OnPrefsChangedListener sListener;

    public static void setOnPrefsChangedListener(OnPrefsChangedListener listener) {
        sListener = listener;
    }

    public static class Prefs {
        // 大悬浮窗元素
        public boolean showCaptureText = true;
        public boolean showApplyRules = true;
        public boolean showAiModify = true;
        public boolean showLog = true;

        // 悬浮球开关
        public boolean showQuickBall = false;

        // 悬浮球自定义
        public String ballContentType = BALL_ICON; // "icon" or "text"
        public String ballText = "AI";
        public float ballSizeDp = 48;
        public float ballCornerDp = 24; // 默认全圆
        public int ballPosX = 20;
        public int ballPosY = 300;
    }

    public static Prefs load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Prefs p = new Prefs();
        p.showCaptureText = sp.getBoolean(KEY_SHOW_CAPTURE_TEXT, true);
        p.showApplyRules = sp.getBoolean(KEY_SHOW_APPLY_RULES, true);
        p.showAiModify = sp.getBoolean(KEY_SHOW_AI_MODIFY, true);
        p.showLog = sp.getBoolean(KEY_SHOW_LOG, true);
        p.showQuickBall = sp.getBoolean(KEY_SHOW_QUICK_BALL, false);
        p.ballContentType = sp.getString(KEY_BALL_CONTENT_TYPE, BALL_ICON);
        p.ballText = sp.getString(KEY_BALL_TEXT, "AI");
        p.ballSizeDp = sp.getFloat(KEY_BALL_SIZE_DP, 48);
        p.ballCornerDp = sp.getFloat(KEY_BALL_CORNER_DP, 24);
        p.ballPosX = sp.getInt(KEY_BALL_POS_X, 20);
        p.ballPosY = sp.getInt(KEY_BALL_POS_Y, 300);
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
                .putString(KEY_BALL_CONTENT_TYPE, p.ballContentType)
                .putString(KEY_BALL_TEXT, p.ballText)
                .putFloat(KEY_BALL_SIZE_DP, p.ballSizeDp)
                .putFloat(KEY_BALL_CORNER_DP, p.ballCornerDp)
                .putInt(KEY_BALL_POS_X, p.ballPosX)
                .putInt(KEY_BALL_POS_Y, p.ballPosY)
                .apply();

        // 通知监听器
        OnPrefsChangedListener listener = sListener;
        if (listener != null) {
            listener.onPrefsChanged(p);
        }
    }
}
