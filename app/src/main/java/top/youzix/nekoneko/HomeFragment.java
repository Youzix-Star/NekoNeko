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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * 首页（设置列表式）：无障碍服务 / 悬浮窗 / AI 配置。
 */
public class HomeFragment extends Fragment {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    private TextView statusAccessibility;
    private TextView statusFloating;
    private TextView statusDarkMode;
    private TextView statusColorTheme;

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

        // Dark mode row
        statusDarkMode = view.findViewById(R.id.status_dark_mode);
        view.findViewById(R.id.row_dark_mode).setOnClickListener(v -> showDarkModeDialog());
        refreshDarkModeStatus();

        // Color theme row
        statusColorTheme = view.findViewById(R.id.status_color_theme);
        view.findViewById(R.id.row_color_theme).setOnClickListener(v -> showColorThemeDialog());
        refreshColorThemeStatus();
    }

    private String selectedModel = null; // null = 全部模型

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
        buildModelChips();
        refreshTokenStats();
    }

    private void buildModelChips() {
        if (!isAdded() || getView() == null) return;
        ChipGroup chipGroup = getView().findViewById(R.id.token_model_chips);
        if (chipGroup == null) return;

        chipGroup.removeAllViews();

        List<String> models = TokenStats.getModelNames(requireContext());
        if (models.isEmpty()) {
            // 没有数据时隐藏芯片组
            chipGroup.setVisibility(View.GONE);
            return;
        }
        chipGroup.setVisibility(View.VISIBLE);

        // "全部"选项
        Chip allChip = new Chip(requireContext());
        allChip.setText("全部");
        allChip.setCheckable(true);
        allChip.setChecked(selectedModel == null);
        allChip.setOnClickListener(v -> {
            selectedModel = null;
            refreshTokenStats();
        });
        chipGroup.addView(allChip);

        for (String model : models) {
            Chip chip = new Chip(requireContext());
            chip.setText(model);
            chip.setCheckable(true);
            chip.setChecked(model.equals(selectedModel));
            chip.setOnClickListener(v -> {
                selectedModel = model;
                refreshTokenStats();
            });
            chipGroup.addView(chip);
        }
    }

    private void refreshTokenStats() {
        if (!isAdded() || getView() == null) return;
        Context ctx = requireContext();
        View v = getView();

        long now = System.currentTimeMillis();
        long weekAgo = now - 7L * 24 * 60 * 60 * 1000;
        long monthAgo = now - 30L * 24 * 60 * 60 * 1000;

        TokenStats.Stats all = TokenStats.query(ctx, 0, selectedModel);
        TokenStats.Stats week = TokenStats.query(ctx, weekAgo, selectedModel);
        TokenStats.Stats month = TokenStats.query(ctx, monthAgo, selectedModel);

        TextView totalVal = v.findViewById(R.id.token_ring_value);
        TextView weekVal = v.findViewById(R.id.token_week_value);
        TextView monthVal = v.findViewById(R.id.token_month_value);
        TextView cacheRate = v.findViewById(R.id.token_cache_rate);
        TextView callsVal = v.findViewById(R.id.token_calls_value);

        if (totalVal != null) totalVal.setText(formatTokenCount(all.totalTokens));
        if (weekVal != null) weekVal.setText(formatTokenCount(week.totalTokens));
        if (monthVal != null) monthVal.setText(formatTokenCount(month.totalTokens));
        if (cacheRate != null) {
            cacheRate.setText(all.totalCalls > 0 ? all.cacheHitPercent() + "%" : "--");
        }
        if (callsVal != null) {
            callsVal.setText(String.format(getString(R.string.token_stats_call_fmt),
                    all.totalCalls, formatTokenCount(all.totalPromptTokens),
                    formatTokenCount(all.totalCompletionTokens)));
        }

        // 环形图
        RingChartView ring = v.findViewById(R.id.ring_chart);
        if (ring != null) {
            ring.setData(all.totalPromptTokens, all.totalCompletionTokens, all.cachedTokens);
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

    private void showDarkModeDialog() {
        Context ctx = requireContext();
        int currentMode = DarkModePrefs.getMode(ctx);

        int checkedItem;
        if (currentMode == DarkModePrefs.MODE_FORCE_LIGHT) {
            checkedItem = 1;
        } else if (currentMode == DarkModePrefs.MODE_FORCE_DARK) {
            checkedItem = 2;
        } else {
            checkedItem = 0; // Follow System
        }

        String[] options = {
                getString(R.string.dark_mode_follow_system),
                getString(R.string.dark_mode_force_light),
                getString(R.string.dark_mode_force_dark)
        };

        new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.dark_mode_title)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    int newMode;
                    switch (which) {
                        case 1:
                            newMode = DarkModePrefs.MODE_FORCE_LIGHT;
                            break;
                        case 2:
                            newMode = DarkModePrefs.MODE_FORCE_DARK;
                            break;
                        default:
                            newMode = DarkModePrefs.MODE_FOLLOW_SYSTEM;
                            break;
                    }
                    DarkModePrefs.save(ctx, newMode);
                    refreshDarkModeStatus();
                    dialog.dismiss();
                })
                .show();
    }

    private void refreshDarkModeStatus() {
        if (!isAdded() || statusDarkMode == null) return;
        int mode = DarkModePrefs.getMode(requireContext());
        if (mode == DarkModePrefs.MODE_FORCE_LIGHT) {
            statusDarkMode.setText(R.string.dark_mode_force_light);
        } else if (mode == DarkModePrefs.MODE_FORCE_DARK) {
            statusDarkMode.setText(R.string.dark_mode_force_dark);
        } else {
            statusDarkMode.setText(R.string.dark_mode_follow_system);
        }
    }

    private void showColorThemeDialog() {
        Context ctx = requireContext();
        int currentTheme = ColorThemeManager.getThemeId(ctx);

        String[] options = {
                getString(R.string.color_theme_green),
                getString(R.string.color_theme_ember),
                getString(R.string.color_theme_glacier)
        };

        new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.color_theme_title)
                .setSingleChoiceItems(options, currentTheme, (dialog, which) -> {
                    if (which != currentTheme) {
                        ColorThemeManager.saveTheme(ctx, which);
                        refreshColorThemeStatus();
                        // Recreate activity to apply new theme
                        if (getActivity() != null) {
                            getActivity().recreate();
                        }
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void refreshColorThemeStatus() {
        if (!isAdded() || statusColorTheme == null) return;
        Context ctx = requireContext();
        int themeId = ColorThemeManager.getThemeId(ctx);
        statusColorTheme.setText(ColorThemeManager.getThemeName(ctx, themeId));
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
