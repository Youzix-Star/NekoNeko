package top.youzix.nekoneko;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

public class FloatingWindowService extends Service implements Logger.LogListener {

    private static final String TAG = "FloatingWindowService";
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private float initialTouchX, initialTouchY;
    private int initialX, initialY;
    private TextView capturedTextTextView;
    private TextView logTextView;
    private ScrollView logScrollView;
    private View windowBody;
    private ImageButton minimizeButton;
    private boolean isMinimized = false;

    // 快捷悬浮球
    private View quickBallView;
    private WindowManager.LayoutParams quickBallParams;
    private boolean isQuickBallProcessing = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.i("悬浮窗服务正在启动...");

        // 获取WindowManager系统服务
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 注册设置实时监听
        FloatingWindowPrefs.setOnPrefsChangedListener(this::onPrefsChanged);

        // 设置日志监听器
        Logger.setLogListener(this);

        // 创建悬浮窗视图
        FloatingWindowPrefs.Prefs prefs = FloatingWindowPrefs.load(this);
        try {
            if (prefs.showQuickBall) {
                // 悬浮球模式：只创建小球，不创建大悬浮窗
                createQuickBall();
                Logger.i("悬浮窗服务启动完成（快捷球模式）");
            } else {
                // 大悬浮窗模式
                createFloatingView();
                Logger.i("悬浮窗服务启动完成");
            }
        } catch (Exception e) {
            // 创建失败时给出可见提示，包含异常类与根因便于排查
            Logger.e("悬浮窗创建失败", e);
            String detail = e.getClass().getSimpleName() + ": "
                    + (e.getMessage() == null ? e.toString() : e.getMessage());
            if (e.getCause() != null) {
                detail += "\n根因: " + e.getCause().getClass().getSimpleName()
                        + ": " + e.getCause().getMessage();
            }
            Toast.makeText(this, "悬浮窗创建失败: " + detail, Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private void createFloatingView() {
        Logger.d("正在创建悬浮窗视图...");
        
        // 悬浮窗固定使用基础 M3 DayNight 主题膨胀（AppTheme.Overlay）；
        // Android 12+ 按系统深浅色应用莫奈动态取色（与主界面一致）。
        Context themed = new ContextThemeWrapper(this, R.style.AppTheme_Overlay);
        ThemeUtils.applyDynamicColors(this, themed.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Logger.d("已为悬浮窗应用莫奈动态取色");
        }
        
        // 加载悬浮窗布局
        floatingView = LayoutInflater.from(themed).inflate(R.layout.floating_window, null);
        
        // 获取文本显示控件
        capturedTextTextView = floatingView.findViewById(R.id.captured_text);
        logTextView = floatingView.findViewById(R.id.log_text);
        logScrollView = floatingView.findViewById(R.id.log_scroll_view);
        windowBody = floatingView.findViewById(R.id.window_body);
        minimizeButton = floatingView.findViewById(R.id.minimize_button);
        
        // 设置悬浮窗参数
        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
        );

        // 设置悬浮窗位置
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        // 添加悬浮窗到窗口
        windowManager.addView(floatingView, params);

        // 设置悬浮窗触摸事件
        setupTouchListener();
        
        // 设置关闭按钮
        setupCloseButton();
        
        // 设置最小化按钮
        setupMinimizeButton();
        
        // 设置捕获按钮
        setupCaptureButton();
        
        // 设置应用规则按钮
        setupApplyRulesButton();
        
        // 设置 AI 修改按钮
        setupAiModifyButton();
        
        // 设置清除日志按钮
        setupClearLogButton();

        // 根据设置显示/隐藏各区域
        applyVisibilityPrefs();

        Logger.d("悬浮窗视图创建完成");
    }

    /** 根据用户设置控制悬浮窗内各区域的显示/隐藏。 */
    private void applyVisibilityPrefs() {
        FloatingWindowPrefs.Prefs prefs = FloatingWindowPrefs.load(this);

        // 捕获文本区域
        View captureArea = floatingView.findViewById(R.id.capture_area);
        if (captureArea != null) {
            captureArea.setVisibility(prefs.showCaptureText ? View.VISIBLE : View.GONE);
        }

        // 应用规则按钮
        View applyRulesBtn = floatingView.findViewById(R.id.apply_rules_button);
        if (applyRulesBtn != null) {
            applyRulesBtn.setVisibility(prefs.showApplyRules ? View.VISIBLE : View.GONE);
        }

        // AI 修改按钮
        View aiModifyBtn = floatingView.findViewById(R.id.ai_modify_button);
        if (aiModifyBtn != null) {
            aiModifyBtn.setVisibility(prefs.showAiModify ? View.VISIBLE : View.GONE);
        }

        // 日志区域
        View logArea = floatingView.findViewById(R.id.log_area);
        if (logArea != null) {
            logArea.setVisibility(prefs.showLog ? View.VISIBLE : View.GONE);
        }
    }

    private void setupTouchListener() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录初始位置
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // 更新悬浮窗位置
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void setupCloseButton() {
        ImageButton closeButton = floatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("用户点击关闭按钮");
                // 关闭悬浮窗服务
                stopSelf();
            }
        });
    }

    private void setupMinimizeButton() {
        minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("用户点击最小化按钮");
                toggleMinimized();
            }
        });
    }

    private void toggleMinimized() {
        if (windowBody == null || minimizeButton == null) {
            return;
        }
        isMinimized = !isMinimized;
        minimizeButton.setImageResource(isMinimized ? R.drawable.ic_add : R.drawable.ic_remove);
        minimizeButton.setContentDescription(getString(
                isMinimized ? R.string.restore_floating_window : R.string.minimize_floating_window));

        if (isMinimized) {
            // 记录当前高度，然后动画收缩到 0
            final int startHeight = windowBody.getHeight();
            windowBody.animate()
                    .setDuration(180)
                    .setUpdateListener(animation -> {
                        float fraction = animation.getAnimatedFraction();
                        int h = (int) (startHeight * (1f - fraction));
                        windowBody.getLayoutParams().height = Math.max(h, 0);
                        windowBody.requestLayout();
                    })
                    .withEndAction(() -> {
                        windowBody.setVisibility(View.GONE);
                        windowBody.getLayoutParams().height = WindowManager.LayoutParams.WRAP_CONTENT;
                        relayoutWindow();
                    })
                    .start();
        } else {
            // 先设为可见并测量原始高度，再从 0 动画展开
            windowBody.setVisibility(View.VISIBLE);
            windowBody.measure(
                    View.MeasureSpec.makeMeasureSpec(
                            ((View) windowBody.getParent()).getWidth(),
                            View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            final int targetHeight = windowBody.getMeasuredHeight();
            windowBody.getLayoutParams().height = 0;
            windowBody.requestLayout();
            windowBody.animate()
                    .setDuration(180)
                    .setUpdateListener(animation -> {
                        float fraction = animation.getAnimatedFraction();
                        int h = (int) (targetHeight * fraction);
                        windowBody.getLayoutParams().height = h;
                        windowBody.requestLayout();
                    })
                    .withEndAction(() -> {
                        windowBody.getLayoutParams().height = WindowManager.LayoutParams.WRAP_CONTENT;
                        relayoutWindow();
                    })
                    .start();
        }
        Logger.d(isMinimized ? "悬浮窗已最小化" : "悬浮窗已展开");
    }

    /** 按当前内容重新测量悬浮窗大小。 */
    private void relayoutWindow() {
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowManager.updateViewLayout(floatingView, params);
    }

    private void setupCaptureButton() {
        Button captureButton = floatingView.findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("用户点击捕获按钮");
                
                // 尝试获取当前窗口文本
                AccessibilityService accessibilityService = AccessibilityService.getInstance();
                if (accessibilityService != null) {
                    Logger.d("无障碍服务已连接，尝试获取文本...");
                    String text = accessibilityService.getCurrentWindowText();
                    if (!text.isEmpty()) {
                        updateCapturedText(text);
                    } else {
                        Logger.w("未捕获到文本");
                        Toast.makeText(FloatingWindowService.this, 
                            "未找到可捕获的文本", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Logger.w("无障碍服务未连接");
                    Toast.makeText(FloatingWindowService.this, 
                        "请先启用无障碍服务", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupApplyRulesButton() {
        Button applyRulesButton = floatingView.findViewById(R.id.apply_rules_button);
        applyRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("用户点击应用规则按钮");

                // 优先使用悬浮窗当前显示的捕获文本；没有则现场重新捕获
                String text = capturedTextTextView != null
                        ? capturedTextTextView.getText().toString() : "";
                if (text.isEmpty() || text.equals(getString(R.string.waiting_for_text))) {
                    AccessibilityService service = AccessibilityService.getInstance();
                    if (service != null) {
                        text = service.getCurrentWindowText();
                    }
                }
                if (text.isEmpty()) {
                    Toast.makeText(FloatingWindowService.this,
                            getString(R.string.no_text_found), Toast.LENGTH_SHORT).show();
                    return;
                }

                // 加载规则并应用
                java.util.List<RuleManager.Rule> rules = RuleManager.load(FloatingWindowService.this);
                if (rules.isEmpty()) {
                    Toast.makeText(FloatingWindowService.this,
                            getString(R.string.rules_none_hint), Toast.LENGTH_SHORT).show();
                    return;
                }

                String result = RuleManager.applyRules(text, rules);
                Logger.i("规则应用完成: " + text + " → " + result);
                updateCapturedText(result);

                // 将结果替换回输入框
                AccessibilityService service = AccessibilityService.getInstance();
                boolean replaced = service != null && service.replaceInputText(result);
                Toast.makeText(FloatingWindowService.this,
                        replaced ? R.string.rules_applied : R.string.no_text_found,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAiModifyButton() {
        Button aiModifyButton = floatingView.findViewById(R.id.ai_modify_button);
        aiModifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("用户点击 AI 修改按钮");

                // 优先使用悬浮窗当前显示的捕获文本；没有则现场重新捕获
                String text = capturedTextTextView != null
                        ? capturedTextTextView.getText().toString() : "";
                if (text.isEmpty() || text.equals(getString(R.string.waiting_for_text))) {
                    AccessibilityService service = AccessibilityService.getInstance();
                    if (service != null) {
                        text = service.getCurrentWindowText();
                    }
                }
                if (text.isEmpty()) {
                    Toast.makeText(FloatingWindowService.this,
                            getString(R.string.no_text_found), Toast.LENGTH_SHORT).show();
                    return;
                }

                AiManager.Config cfg = AiManager.load(FloatingWindowService.this);
                if (cfg.apiKey == null || cfg.apiKey.trim().isEmpty()) {
                    Toast.makeText(FloatingWindowService.this,
                            getString(R.string.ai_key_missing), Toast.LENGTH_LONG).show();
                    return;
                }

                updateCapturedText(getString(R.string.ai_modifying));
                final String originalText = text;
                AiManager.modifyText(cfg, originalText, new AiManager.Callback() {
                    @Override
                    public void onSuccess(String modifiedText) {
                        Logger.i("AI 修改成功: " + modifiedText);
                        updateCapturedText(modifiedText);

                        // 记录 token 用量
                        AiManager.UsageRecord usage = AiManager.consumeLastUsage();
                        if (usage != null) {
                            TokenStats.record(FloatingWindowService.this, usage.model,
                                    usage.promptTokens, usage.completionTokens,
                                    usage.totalTokens, usage.cachedTokens);
                        }

                        AccessibilityService service = AccessibilityService.getInstance();
                        boolean replaced = service != null && service.replaceInputText(modifiedText);
                        Toast.makeText(FloatingWindowService.this,
                                replaced ? R.string.ai_replace_success : R.string.ai_no_input,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Logger.e("AI 调用失败: " + message);
                        updateCapturedText(originalText);
                        Toast.makeText(FloatingWindowService.this,
                                getString(R.string.ai_call_failed, message), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setupClearLogButton() {
        Button clearLogButton = floatingView.findViewById(R.id.clear_log_button);
        clearLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("用户点击清除日志按钮");
                Logger.clearLogs();
                updateLogDisplay();
            }
        });
    }

    @Override
    public void onLogAdded(String logEntry) {
        updateLogDisplay();
    }

    private void updateCapturedText(String text) {
        if (capturedTextTextView != null && text != null) {
            // 使用Handler在主线程上更新UI
            new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    capturedTextTextView.setText(text);
                    Logger.d("悬浮窗文本显示已更新");
                }
            });
        }
    }

    private void updateLogDisplay() {
        if (logTextView != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String logText = Logger.getLogEntriesAsString();
                    logTextView.setText(logText);
                    
                    // 自动滚动到底部
                    if (logScrollView != null) {
                        logScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                logScrollView.fullScroll(View.FOCUS_DOWN);
                            }
                        });
                    }
                }
            });
        }
    }

    // ========== 快捷悬浮球 ==========

    private void createQuickBall() {
        FloatingWindowPrefs.Prefs prefs = FloatingWindowPrefs.load(this);
        if (!prefs.showQuickBall) return;

        Context themed = new ContextThemeWrapper(this, R.style.AppTheme_Overlay);
        ThemeUtils.applyDynamicColors(this, themed.getTheme());

        quickBallView = LayoutInflater.from(themed).inflate(R.layout.floating_quick_ball, null);

        // 应用自定义大小和圆角
        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (prefs.ballSizeDp * density);
        int cornerPx = (int) (prefs.ballCornerDp * density);

        // 直接用 FrameLayout.LayoutParams 设大小
        quickBallView.setLayoutParams(new FrameLayout.LayoutParams(sizePx, sizePx));

        // 设置圆角背景 — 从 themed 主题解析颜色，确保莫奈动态色生效
        int bgColor;
        android.util.TypedValue tv = new android.util.TypedValue();
        if (themed.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, tv, true)) {
            bgColor = tv.data;
        } else {
            bgColor = getResources().getColor(R.color.colorPrimaryContainer);
        }
        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(cornerPx);
        quickBallView.setBackground(bg);

        // 内部控件大小 = 60% 的球大小
        int innerPx = (int) (sizePx * 0.6);
        FrameLayout.LayoutParams innerLp = new FrameLayout.LayoutParams(innerPx, innerPx);
        innerLp.gravity = android.view.Gravity.CENTER;

        View iconView = quickBallView.findViewById(R.id.quick_ball_icon);
        TextView textView = quickBallView.findViewById(R.id.quick_ball_text);
        ProgressBar progressView = quickBallView.findViewById(R.id.quick_ball_progress);

        if (FloatingWindowPrefs.BALL_TEXT.equals(prefs.ballContentType)) {
            // 文字模式
            iconView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(prefs.ballText);
            int textLen = prefs.ballText.length();
            float textSizeSp;
            if (textLen <= 1) {
                textSizeSp = sizePx / density * 0.5f;
            } else if (textLen <= 2) {
                textSizeSp = sizePx / density * 0.4f;
            } else {
                textSizeSp = sizePx / density * 0.28f;
            }
            textView.setTextSize(textSizeSp);
            textView.setLayoutParams(innerLp);
        } else {
            // 图标模式
            iconView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            iconView.setLayoutParams(innerLp);
        }

        progressView.setLayoutParams(innerLp);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        quickBallParams = new WindowManager.LayoutParams(
                sizePx, sizePx,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
        );
        quickBallParams.gravity = Gravity.TOP | Gravity.START;
        quickBallParams.x = 20;
        quickBallParams.y = 300;

        windowManager.addView(quickBallView, quickBallParams);

        setupQuickBallTouch();

        quickBallView.setOnClickListener(v -> {
            if (isQuickBallProcessing) return;
            executeQuickBallAction();
        });

        Logger.d("快捷悬浮球已创建: " + prefs.ballSizeDp + "dp, 圆角" + prefs.ballCornerDp + "dp");
    }

    private void removeQuickBall() {
        if (quickBallView != null) {
            try {
                windowManager.removeView(quickBallView);
            } catch (Exception ignored) {
            }
            quickBallView = null;
        }
    }

    // ========== 实时设置生效 ==========

    private void onPrefsChanged(FloatingWindowPrefs.Prefs prefs) {
        Logger.i("设置已变更，正在实时刷新...");

        boolean wasQuickBall = (quickBallView != null);
        boolean wasFloatingWindow = (floatingView != null);

        if (prefs.showQuickBall) {
            // 切到悬浮球模式
            if (wasFloatingWindow) {
                // 移除大悬浮窗
                try { windowManager.removeView(floatingView); } catch (Exception ignored) {}
                floatingView = null;
            }
            // 重建悬浮球（应用新样式）
            removeQuickBall();
            createQuickBall();
        } else {
            // 切到大悬浮窗模式
            if (wasQuickBall) {
                removeQuickBall();
            }
            if (wasFloatingWindow) {
                // 大悬浮窗还活着：直接刷新可见性
                applyVisibilityPrefs();
            } else {
                // 从悬浮球切回来：重建大悬浮窗
                createFloatingView();
            }
        }
    }

    private void setupQuickBallTouch() {
        final float[] touchX = new float[1];
        final float[] touchY = new float[1];
        final int[] startPos = new int[2];
        final boolean[] moved = new boolean[1];

        quickBallView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startPos[0] = quickBallParams.x;
                    startPos[1] = quickBallParams.y;
                    touchX[0] = event.getRawX();
                    touchY[0] = event.getRawY();
                    moved[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - touchX[0];
                    float dy = event.getRawY() - touchY[0];
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        moved[0] = true;
                    }
                    quickBallParams.x = startPos[0] + (int) dx;
                    quickBallParams.y = startPos[1] + (int) dy;
                    windowManager.updateViewLayout(quickBallView, quickBallParams);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!moved[0]) {
                        quickBallView.performClick();
                    }
                    return true;
            }
            return false;
        });
    }

    private void executeQuickBallAction() {
        isQuickBallProcessing = true;
        setQuickBallLoading(true);
        Logger.i("快捷球：开始执行 AI 流程");

        // 1. 捕获输入框文本
        String rawText = "";
        AccessibilityService service = AccessibilityService.getInstance();
        if (service != null) {
            rawText = service.getCurrentWindowText();
        }
        if (rawText.isEmpty()) {
            Logger.w("快捷球：未捕获到文本");
            Toast.makeText(this, R.string.no_text_found, Toast.LENGTH_SHORT).show();
            setQuickBallLoading(false);
            isQuickBallProcessing = false;
            return;
        }
        final String text = rawText;
        Logger.d("快捷球：捕获到文本: " + text);
        updateCapturedText(text);

        // 2. 检查 AI 配置
        AiManager.Config cfg = AiManager.load(this);
        if (cfg.apiKey == null || cfg.apiKey.trim().isEmpty()) {
            Toast.makeText(this, R.string.ai_key_missing, Toast.LENGTH_LONG).show();
            setQuickBallLoading(false);
            isQuickBallProcessing = false;
            return;
        }

        // 3. 调用 AI
        updateCapturedText(getString(R.string.ai_modifying));
        AiManager.modifyText(cfg, text, new AiManager.Callback() {
            @Override
            public void onSuccess(String modifiedText) {
                Logger.i("快捷球：AI 成功: " + modifiedText);
                updateCapturedText(modifiedText);

                // 记录 token 用量
                AiManager.UsageRecord usage = AiManager.consumeLastUsage();
                if (usage != null) {
                    TokenStats.record(FloatingWindowService.this, usage.model,
                            usage.promptTokens, usage.completionTokens,
                            usage.totalTokens, usage.cachedTokens);
                }

                // 4. 替换回输入框
                AccessibilityService svc = AccessibilityService.getInstance();
                boolean replaced = svc != null && svc.replaceInputText(modifiedText);
                Toast.makeText(FloatingWindowService.this,
                        replaced ? R.string.ai_replace_success : R.string.ai_no_input,
                        Toast.LENGTH_SHORT).show();

                setQuickBallLoading(false);
                isQuickBallProcessing = false;
            }

            @Override
            public void onError(String message) {
                Logger.e("快捷球：AI 失败: " + message);
                updateCapturedText(text);
                Toast.makeText(FloatingWindowService.this,
                        getString(R.string.ai_call_failed, message), Toast.LENGTH_LONG).show();
                setQuickBallLoading(false);
                isQuickBallProcessing = false;
            }
        });
    }

    private void setQuickBallLoading(boolean loading) {
        if (quickBallView == null) return;
        ImageView icon = quickBallView.findViewById(R.id.quick_ball_icon);
        ProgressBar progress = quickBallView.findViewById(R.id.quick_ball_progress);
        if (icon != null && progress != null) {
            icon.setVisibility(loading ? View.GONE : View.VISIBLE);
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    // ========== 生命周期 ==========

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        Logger.i("悬浮窗服务正在销毁...");

        // 注销设置监听器
        FloatingWindowPrefs.setOnPrefsChangedListener(null);

        // 移除日志监听器
        Logger.setLogListener(null);

        // 移除悬浮窗（大悬浮窗或快捷球，按需清理）
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            Logger.d("悬浮窗已移除");
        }

        removeQuickBall();
        
        Logger.w("悬浮窗服务已销毁");
    }
}
