package top.youzix.nekoneko;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AboutFragment extends Fragment {

    private TextView updateStatus;
    private TextView updateSub;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 版本号 + 构建号
        TextView versionText = view.findViewById(R.id.about_version);
        try {
            PackageManager pm = requireContext().getPackageManager();
            String versionName = pm.getPackageInfo(requireContext().getPackageName(), 0).versionName;
            int versionCode = pm.getPackageInfo(requireContext().getPackageName(), 0).versionCode;
            versionText.setText(String.format("%s(%d)", versionName, versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText(R.string.about_version_placeholder);
        }

        // 设备信息
        TextView deviceInfo = view.findViewById(R.id.about_device_info);
        String abi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown";
        deviceInfo.setText(String.format("Android %s · %s · %s %s",
                Build.VERSION.RELEASE, abi, Build.MANUFACTURER, Build.MODEL));

        // 赞助弹窗
        view.findViewById(R.id.about_sponsor_button).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.about_sponsor)
                    .setMessage(R.string.about_sponsor_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });

        // GitHub 链接
        view.findViewById(R.id.about_github_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Youzix-Star/NekoNeko"));
            startActivity(intent);
        });

        // 检测更新
        updateStatus = view.findViewById(R.id.about_update_status);
        updateSub = view.findViewById(R.id.about_update_sub);

        view.findViewById(R.id.about_check_update_button).setOnClickListener(v -> {
            updateStatus.setText(R.string.about_update_checking);
            updateSub.setText("");
            UpdateChecker.checkForUpdate(requireContext(), new UpdateChecker.Callback() {
                @Override
                public void onUpdateAvailable(String latestVersion, String body, String apkUrl) {
                    if (!isAdded()) return;
                    updateStatus.setText(getString(R.string.about_update_available, latestVersion));
                    updateSub.setText(R.string.about_update_available_sub);

                    // 弹窗显示更新详情
                    String message = body == null || body.trim().isEmpty()
                            ? getString(R.string.about_update_available, latestVersion)
                            : body;
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.about_update_dialog_title, latestVersion))
                            .setMessage(message)
                            .setPositiveButton(R.string.about_update_dialog_go, (d, w) -> {
                                UpdateChecker.openReleasePage(requireContext());
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }

                @Override
                public void onNoUpdate() {
                    if (!isAdded()) return;
                    updateStatus.setText(R.string.about_update_latest);
                    updateSub.setText("");
                }

                @Override
                public void onError(String message) {
                    if (!isAdded()) return;
                    updateStatus.setText(R.string.about_check_update);
                    updateSub.setText(getString(R.string.about_update_error, message));
                }
            });
        });
    }
}
