package com.youzix.nekoneko;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.color.DynamicColors;

/**
 * 关于页：应用信息、版本号、功能特性、技术栈与 GitHub 链接。
 * 附带设备/主题色诊断信息，用于验证莫奈动态取色是否生效。
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

        // 诊断：Android 版本 + 动态取色是否可用 + 当前解析出的主题主色
        // （莫奈生效时，主题色会随壁纸变化）
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
        deviceInfo.setText(getString(R.string.about_device_info_fmt,
                Build.VERSION.SDK_INT,
                dynamicAvailable ? "可用" : "不可用",
                primaryHex));

        view.findViewById(R.id.about_github_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Youzix-Star/NekoNeko"));
            startActivity(intent);
        });
    }
}
