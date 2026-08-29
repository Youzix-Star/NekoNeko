package com.youzix.nekoneko;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 关于页：应用信息、功能特性、技术栈与 GitHub 链接。
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

        view.findViewById(R.id.about_github_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Youzix-Star/NekoNeko"));
            startActivity(intent);
        });
    }
}
