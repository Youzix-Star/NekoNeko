package top.youzix.nekoneko;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * 悬浮窗设置页：控制悬浮窗中显示哪些元素。
 */
public class FloatingConfigFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_floating_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingWindowPrefs.Prefs prefs = FloatingWindowPrefs.load(requireContext());

        MaterialSwitch swCaptureText = view.findViewById(R.id.sw_show_capture_text);
        MaterialSwitch swApplyRules = view.findViewById(R.id.sw_show_apply_rules);
        MaterialSwitch swAiModify = view.findViewById(R.id.sw_show_ai_modify);
        MaterialSwitch swLog = view.findViewById(R.id.sw_show_log);
        MaterialSwitch swQuickBall = view.findViewById(R.id.sw_show_quick_ball);

        swCaptureText.setChecked(prefs.showCaptureText);
        swApplyRules.setChecked(prefs.showApplyRules);
        swAiModify.setChecked(prefs.showAiModify);
        swLog.setChecked(prefs.showLog);
        swQuickBall.setChecked(prefs.showQuickBall);

        view.findViewById(R.id.save_floating_config_button).setOnClickListener(v -> {
            FloatingWindowPrefs.Prefs p = new FloatingWindowPrefs.Prefs();
            p.showCaptureText = swCaptureText.isChecked();
            p.showApplyRules = swApplyRules.isChecked();
            p.showAiModify = swAiModify.isChecked();
            p.showLog = swLog.isChecked();
            p.showQuickBall = swQuickBall.isChecked();
            FloatingWindowPrefs.save(requireContext(), p);
            Toast.makeText(requireContext(), R.string.floating_config_saved, Toast.LENGTH_SHORT).show();
        });
    }
}
