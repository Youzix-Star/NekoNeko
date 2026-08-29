package top.youzix.nekoneko;

import android.app.ActivityManager;
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

import java.util.concurrent.TimeUnit;

/**
 * 首页（设置列表式）：无障碍服务 / 悬浮窗 / AI 配置。
 */
public class HomeFragment extends Fragment {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    private TextView statusAccessibility;
    private TextView statusFloating;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statusAccessibility = view.findViewById(R.id.status_accessibility);
        statusFloating = view.findViewById(R.id.status_floating);

        view.findViewById(R.id.row_accessibility).setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });

        view.findViewById(R.id.row_floating).setOnClickListener(v -> toggleFloatingWindow());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
        refreshTokenStats();
    }

    private void refreshTokenStats() {
        if (!isAdded()) return;
        Context ctx = requireContext();

        // 总量
        TokenStats.Stats all = TokenStats.query(ctx, 0);
        TextView totalValue = getView() != null ? getView().findViewById(R.id.token_total_value) : null;
        TextView callsValue = getView() != null ? getView().findViewById(R.id.token_calls_value) : null;
        TextView cacheRate = getView() != null ? getView().findViewById(R.id.token_cache_rate) : null;

        if (totalValue != null) {
            totalValue.setText(formatTokenCount(all.totalTokens));
        }
        if (callsValue != null) {
            callsValue.setText(String.valueOf(all.totalCalls));
        }
        if (cacheRate != null) {
            if (all.totalCalls > 0) {
                int rate = (int) (all.cachedCalls * 100f / all.totalCalls);
                cacheRate.setText(rate + "%");
            } else {
                cacheRate.setText("--");
            }
        }

        // 模型名
        TextView modelText = getView() != null ? getView().findViewById(R.id.token_stats_model) : null;
        if (modelText != null) {
            AiManager.Config cfg = AiManager.load(ctx);
            modelText.setText(cfg.model);
        }

        // 1 周 / 1 个月
        long now = System.currentTimeMillis();
        long weekAgo = now - 7L * 24 * 60 * 60 * 1000;
        long monthAgo = now - 30L * 24 * 60 * 60 * 1000;

        TokenStats.Stats week = TokenStats.query(ctx, weekAgo);
        TokenStats.Stats month = TokenStats.query(ctx, monthAgo);

        TextView weekValue = getView() != null ? getView().findViewById(R.id.token_week_value) : null;
        TextView monthValue = getView() != null ? getView().findViewById(R.id.token_month_value) : null;

        if (weekValue != null) {
            weekValue.setText(formatTokenCount(week.totalTokens));
        }
        if (monthValue != null) {
            monthValue.setText(formatTokenCount(month.totalTokens));
        }
    }

    private String formatTokenCount(int count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }

    private void refreshStatus() {
        Context ctx = requireContext();
        if (statusAccessibility != null) {
            statusAccessibility.setText(isAccessibilityEnabled(ctx)
                    ? R.string.acc_on : R.string.acc_off);
        }
        if (statusFloating != null) {
            statusFloating.setText(isServiceRunning(ctx, FloatingWindowService.class)
                    ? R.string.floating_running : R.string.floating_stopped);
        }
    }

    private void toggleFloatingWindow() {
        Context ctx = requireContext();
        if (isServiceRunning(ctx, FloatingWindowService.class)) {
            ctx.stopService(new Intent(ctx, FloatingWindowService.class));
            Toast.makeText(ctx, R.string.floating_stopped, Toast.LENGTH_SHORT).show();
            refreshStatus();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(ctx)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + ctx.getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        } else {
            ctx.startService(new Intent(ctx, FloatingWindowService.class));
            Toast.makeText(ctx, R.string.floating_window_started, Toast.LENGTH_SHORT).show();
            refreshStatus();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            Context ctx = requireContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(ctx)) {
                ctx.startService(new Intent(ctx, FloatingWindowService.class));
                Toast.makeText(ctx, R.string.floating_window_started, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, R.string.floating_window_permission_needed, Toast.LENGTH_SHORT).show();
            }
            refreshStatus();
        }
    }

    private boolean isAccessibilityEnabled(Context ctx) {
        String enabled = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabled != null && enabled.contains(ctx.getPackageName());
    }

    private boolean isServiceRunning(Context ctx, Class<?> serviceClass) {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo info : am.getRunningServices(200)) {
            if (serviceClass.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
