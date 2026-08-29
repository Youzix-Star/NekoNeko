package com.youzix.nekoneko;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingWindowService extends Service implements AccessibilityService.TextCaptureListener {

    private static final String TAG = "FloatingWindowService";
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private float initialTouchX, initialTouchY;
    private int initialX, initialY;
    private TextView capturedTextTextView;
    private BroadcastReceiver textCapturedReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 获取WindowManager系统服务
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // 设置文本捕获监听器
        AccessibilityService.setTextCaptureListener(this);
        
        // 注册广播接收器
        registerTextCapturedReceiver();
        
        // 创建悬浮窗视图
        createFloatingView();
    }

    private void registerTextCapturedReceiver() {
        textCapturedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.youzix.nekoneko.TEXT_CAPTURED".equals(intent.getAction())) {
                    String capturedText = intent.getStringExtra("captured_text");
                    if (capturedText != null && !capturedText.isEmpty()) {
                        updateCapturedText(capturedText);
                    }
                }
            }
        };
        
        IntentFilter filter = new IntentFilter("com.youzix.nekoneko.TEXT_CAPTURED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(textCapturedReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(textCapturedReceiver, filter);
        }
    }

    private void createFloatingView() {
        // 加载悬浮窗布局
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null);
        
        // 获取文本显示控件
        capturedTextTextView = floatingView.findViewById(R.id.captured_text);
        
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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
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
        
        // 设置捕获按钮
        setupCaptureButton();
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
        Button closeButton = floatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭悬浮窗服务
                stopSelf();
            }
        });
    }

    private void setupCaptureButton() {
        Button captureButton = floatingView.findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 尝试获取当前窗口文本
                AccessibilityService accessibilityService = AccessibilityService.getInstance();
                if (accessibilityService != null) {
                    String text = accessibilityService.getCurrentWindowText();
                    if (!text.isEmpty()) {
                        updateCapturedText(text);
                    } else {
                        Toast.makeText(FloatingWindowService.this, 
                            "未找到可捕获的文本", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FloatingWindowService.this, 
                        "请先启用无障碍服务", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onTextCaptured(String text) {
        updateCapturedText(text);
    }

    private void updateCapturedText(String text) {
        if (capturedTextTextView != null && text != null) {
            // 使用Handler在主线程上更新UI
            new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    capturedTextTextView.setText(text);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // 注销广播接收器
        if (textCapturedReceiver != null) {
            unregisterReceiver(textCapturedReceiver);
        }
        
        // 移除文本捕获监听器
        AccessibilityService.setTextCaptureListener(null);
        
        // 移除悬浮窗
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }
}
