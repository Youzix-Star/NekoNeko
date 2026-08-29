package com.youzix.nekoneko;

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
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
        
        // 设置日志监听器
        Logger.setLogListener(this);

        // 创建悬浮窗视图
        try {
            createFloatingView();
            Logger.i("悬浮窗服务启动完成");
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
        
        Logger.d("悬浮窗视图创建完成");
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

        // 缩放动画：以顶部中心为轴，主体像"拉链"一样收起/展开
        windowBody.setPivotX(windowBody.getWidth() / 2f);
        windowBody.setPivotY(0f);

        if (isMinimized) {
            windowBody.animate()
                    .scaleX(0f).scaleY(0f)
                    .setDuration(180)
                    .withEndAction(() -> {
                        windowBody.setVisibility(View.GONE);
                        relayoutWindow();
                    })
                    .start();
        } else {
            windowBody.setScaleX(0f);
            windowBody.setScaleY(0f);
            windowBody.setVisibility(View.VISIBLE);
            windowBody.animate().scaleX(1f).scaleY(1f).setDuration(180).start();
            relayoutWindow();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        Logger.i("悬浮窗服务正在销毁...");
        
        // 移除日志监听器
        Logger.setLogListener(null);
        
        // 移除悬浮窗
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            Logger.d("悬浮窗已移除");
        }
        
        Logger.w("悬浮窗服务已销毁");
    }
}
