package top.youzix.nekoneko;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 悬浮窗设置页：控制悬浮窗中显示哪些元素，快捷悬浮球的自定义样式。
 * 大悬浮窗和快捷悬浮球互斥。
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

        // 大悬浮窗元素
        MaterialSwitch swCaptureText = view.findViewById(R.id.sw_show_capture_text);
        MaterialSwitch swApplyRules = view.findViewById(R.id.sw_show_apply_rules);
        MaterialSwitch swAiModify = view.findViewById(R.id.sw_show_ai_modify);
        MaterialSwitch swLog = view.findViewById(R.id.sw_show_log);

        // 悬浮球
        MaterialSwitch swQuickBall = view.findViewById(R.id.sw_show_quick_ball);
        View quickBallOptions = view.findViewById(R.id.quick_ball_options);

        // 悬浮球自定义
        ChipGroup contentGroup = view.findViewById(R.id.ball_content_group);
        TextInputLayout ballTextLayout = view.findViewById(R.id.ball_text_layout);
        EditText ballTextInput = view.findViewById(R.id.ball_text_input);
        Slider sizeSlider = view.findViewById(R.id.ball_size_slider);
        Slider cornerSlider = view.findViewById(R.id.ball_corner_slider);
        android.widget.TextView sizeValue = view.findViewById(R.id.ball_size_value);
        android.widget.TextView cornerValue = view.findViewById(R.id.ball_corner_value);

        // 回填
        swCaptureText.setChecked(prefs.showCaptureText);
        swApplyRules.setChecked(prefs.showApplyRules);
        swAiModify.setChecked(prefs.showAiModify);
        swLog.setChecked(prefs.showLog);
        swQuickBall.setChecked(prefs.showQuickBall);
        quickBallOptions.setVisibility(prefs.showQuickBall ? View.VISIBLE : View.GONE);

        // 文字/图标模式
        boolean isTextMode = FloatingWindowPrefs.BALL_TEXT.equals(prefs.ballContentType);
        contentGroup.check(isTextMode ? R.id.chip_ball_text : R.id.chip_ball_icon);
        ballTextLayout.setVisibility(isTextMode ? View.VISIBLE : View.GONE);
        ballTextInput.setText(prefs.ballText);

        // 大小/圆角
        sizeSlider.setValue(prefs.ballSizeDp);
        cornerSlider.setValue(prefs.ballCornerDp);
        sizeValue.setText(prefs.ballSizeDp + "dp");
        cornerValue.setText(prefs.ballCornerDp + "dp");

        // --- 交互 ---

        // 悬浮球开关：开启时禁用大悬浮窗元素，反之亦然
        swQuickBall.setOnCheckedChangeListener((btn, checked) -> {
            quickBallOptions.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (checked) {
                swCaptureText.setChecked(false);
                swApplyRules.setChecked(false);
                swAiModify.setChecked(false);
                swLog.setChecked(false);
            }
        });

        // 大悬浮窗开关：开启任何一个都关闭悬浮球
        MaterialSwitch[] windowSwitches = {swCaptureText, swApplyRules, swAiModify, swLog};
        for (MaterialSwitch sw : windowSwitches) {
            sw.setOnCheckedChangeListener((btn, checked) -> {
                if (checked && swQuickBall.isChecked()) {
                    swQuickBall.setChecked(false);
                    quickBallOptions.setVisibility(View.GONE);
                }
            });
        }

        // 内容类型切换
        contentGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            boolean textMode = checkedIds.get(0) == R.id.chip_ball_text;
            ballTextLayout.setVisibility(textMode ? View.VISIBLE : View.GONE);
        });

        // 大小滑块
        sizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            sizeValue.setText((int) value + "dp");
        });

        // 圆角滑块
        cornerSlider.addOnChangeListener((slider, value, fromUser) -> {
            cornerValue.setText((int) value + "dp");
        });

        // 保存
        view.findViewById(R.id.save_floating_config_button).setOnClickListener(v -> {
            FloatingWindowPrefs.Prefs p = new FloatingWindowPrefs.Prefs();
            p.showCaptureText = swCaptureText.isChecked();
            p.showApplyRules = swApplyRules.isChecked();
            p.showAiModify = swAiModify.isChecked();
            p.showLog = swLog.isChecked();
            p.showQuickBall = swQuickBall.isChecked();
            p.ballContentType = contentGroup.getCheckedChipId() == R.id.chip_ball_text
                    ? FloatingWindowPrefs.BALL_TEXT : FloatingWindowPrefs.BALL_ICON;
            p.ballText = ballTextInput.getText().toString();
            p.ballSizeDp = (int) sizeSlider.getValue();
            p.ballCornerDp = (int) cornerSlider.getValue();
            FloatingWindowPrefs.save(requireContext(), p);
            Toast.makeText(requireContext(), R.string.floating_config_saved, Toast.LENGTH_SHORT).show();
        });
    }
}
