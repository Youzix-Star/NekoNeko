package com.youzix.nekoneko;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 首页：应用介绍 + 无障碍 / 悬浮窗 / AI 配置入口。
 */
public class HomeFragment extends Fragment {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView welcomeText = view.findViewById(R.id.welcome_text);
        welcomeText.setText("NekoNeko");

        view.findViewById(R.id.accessibility_button).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(requireContext(), "请启用NekoNeko无障碍服务", Toast.LENGTH_LONG).show();
        });

        view.findViewById(R.id.floating_window_button).setOnClickListener(v -> {
            Context ctx = requireContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(ctx)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + ctx.getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                startFloatingWindowService();
            }
        });

        view.findViewById(R.id.ai_config_button).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.nav_config);
            }
        });
    }

    private void startFloatingWindowService() {
        Context ctx = requireContext();
        ctx.startService(new Intent(ctx, FloatingWindowService.class));
        Toast.makeText(ctx, R.string.floating_window_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            Context ctx = requireContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(ctx)) {
                startFloatingWindowService();
            } else {
                Toast.makeText(ctx, R.string.floating_window_permission_needed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
