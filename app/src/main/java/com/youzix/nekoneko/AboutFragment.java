package com.youzix.nekoneko;

import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.color.DynamicColors;

/**
 * 关于页：应用信息、版本号、功能特性、技术栈与 GitHub 链接。
 * 附带设备/主题色诊断信息（验证莫奈是否生效），并提供手动主题色兜底选择。
 */
public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView versionText = view.findViewById(R.id.about_version);
        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            versionText.setText(getString(R.string.about_version_fmt, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText(getString(R.string.about_version_placeholder));
        }

        // 诊断：Android 版本 + 动态取色是否可用 + 壁纸是否提供取色 + 当前主题主色
        TextView deviceInfo = view.findViewById(R.id.about_device_info);
        String primaryHex = "?";
        TypedValue tv = new TypedValue();
        if (requireContext().getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorPrimary, tv, true)
                && tv.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            primaryHex = String.format("#%06X", 0xFFFFFF & tv.data);
        }
        boolean dynamicAvailable = DynamicColors.isDynamicColorAvailable();
        String wallpaperColor = "无";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                WallpaperColors wc = WallpaperManager.getInstance(requireContext())
                        .getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
                if (wc != null) {
                    wallpaperColor = "有";
                }
            } catch (Exception ignored) {
            }
        }
        deviceInfo.setText(getString(R.string.about_device_info_fmt,
                Build.VERSION.SDK_INT,
                dynamicAvailable ? "可用" : "不可用",
                wallpaperColor,
                primaryHex));

        // 主题色圆点预览（与当前实际解析出的主色一致）
        if (!"?".equals(primaryHex)) {
            try {
                view.findViewById(R.id.about_color_swatch)
                        .getBackground().setTint(android.graphics.Color.parseColor(primaryHex));
            } catch (Exception ignored) {
            }
        }

        // 手动主题色选择
        final int[] buttonIds = {
                R.id.accent_default_button, R.id.accent_blue_button, R.id.accent_green_button,
                R.id.accent_purple_button, R.id.accent_pink_button, R.id.accent_orange_button
        };
        final int[] accents = {
                AccentTheme.DEFAULT, AccentTheme.BLUE, AccentTheme.GREEN,
                AccentTheme.PURPLE, AccentTheme.PINK, AccentTheme.ORANGE
        };
        for (int i = 0; i < buttonIds.length; i++) {
            final int accent = accents[i];
            view.findViewById(buttonIds[i]).setOnClickListener(v -> {
                AccentTheme.save(requireContext(), accent);
                Toast.makeText(requireContext(), R.string.about_accent_applied, Toast.LENGTH_SHORT).show();
                requireActivity().recreate();
            });
        }

        view.findViewById(R.id.about_github_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Youzix-Star/NekoNeko"));
            startActivity(intent);
        });
    }
}
