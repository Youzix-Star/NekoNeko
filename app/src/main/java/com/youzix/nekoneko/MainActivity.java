package com.youzix.nekoneko;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 设置欢迎文本
        TextView welcomeText = findViewById(R.id.welcome_text);
        welcomeText.setText("NekoNeko");
        
        // 设置悬浮窗按钮
        Button floatingWindowButton = findViewById(R.id.floating_window_button);
        floatingWindowButton.setOnClickListener(v -> {
            // 检查悬浮窗权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                // 请求悬浮窗权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                // 启动悬浮窗服务
                startFloatingWindowService();
            }
        });
        
        // 设置无障碍服务按钮
        Button accessibilityButton = findViewById(R.id.accessibility_button);
        accessibilityButton.setOnClickListener(v -> {
            // 打开无障碍服务设置
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "请启用NekoNeko无障碍服务", Toast.LENGTH_LONG).show();
        });
    }

    private void startFloatingWindowService() {
        Intent serviceIntent = new Intent(this, FloatingWindowService.class);
        startService(serviceIntent);
        Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // 权限已授予，启动悬浮窗服务
                startFloatingWindowService();
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要悬浮窗权限才能使用此功能", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
