package top.youzix.nekoneko;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;

/**
 * 悬浮窗设置页：控制悬浮窗中显示哪些元素，快捷悬浮球的自定义样式。
 * 大悬浮窗和快捷悬浮球互斥。
 * 所有设置即时生效，无需点击保存按钮。
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
        ChipGroup actionGroup = view.findViewById(R.id.ball_action_group);
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

        // 点击动作
        boolean isRulesAction = FloatingWindowPrefs.ACTION_RULES.equals(prefs.ballAction);
        actionGroup.check(isRulesAction ? R.id.chip_ball_action_rules : R.id.chip_ball_action_ai);

        // 大小/圆角
        sizeSlider.setValue(prefs.ballSizeDp);
        cornerSlider.setValue(prefs.ballCornerDp);
        sizeValue.setText(prefs.ballSizeDp + "dp");
        cornerValue.setText(prefs.ballCornerDp + "dp");

        // --- 交互：所有变更即时保存 ---

        // 悬浮球开关：开启时禁用大悬浮窗元素，反之亦然
        swQuickBall.setOnCheckedChangeListener((btn, checked) -> {
            quickBallOptions.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (checked) {
                swCaptureText.setChecked(false);
                swApplyRules.setChecked(false);
                swAiModify.setChecked(false);
                swLog.setChecked(false);
            }
            savePrefs(swCaptureText, swApplyRules, swAiModify, swLog,
                    swQuickBall, contentGroup, actionGroup, ballTextInput, sizeSlider, cornerSlider);
        });

        // 大悬浮窗开关：开启任何一个都关闭悬浮球
        MaterialSwitch[] windowSwitches = {swCaptureText, swApplyRules, swAiModify, swLog};
        for (MaterialSwitch sw : windowSwitches) {
            sw.setOnCheckedChangeListener((btn, checked) -> {
                if (checked && swQuickBall.isChecked()) {
                    swQuickBall.setChecked(false);
                    quickBallOptions.setVisibility(View.GONE);
                }
                savePrefs(swCaptureText, swApplyRules, swAiModify, swLog,
                        swQuickBall, contentGroup, actionGroup, ballTextInput, sizeSlider, cornerSlider);
            });
        }

        // 内容类型切换
        contentGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            boolean textMode = checkedIds.get(0) == R.id.chip_ball_text;
            ballTextLayout.setVisibility(textMode ? View.VISIBLE : View.GONE);
            savePrefs(swCaptureText, swApplyRules, swAiModify, swLog,
                    swQuickBall, contentGroup, actionGroup, ballTextInput, sizeSlider, cornerSlider);
        });

        // 点击动作切换
        actionGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                savePrefs(swCaptureText, swApplyRules, swAiModify, swLog,
                        swQuickBall, contentGroup, actionGroup, ballTextInput, sizeSlider, cornerSlider);
            }
        });

        // 大小滑块
        sizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            sizeValue.setText((int) value + "dp");
            if (fromUser) {
                savePrefs(swCaptureText, swApplyRules, swAiModify, swLog,
                        swQuickBall, contentGroup, actionGroup, ballTextInput, sizeSlider, cornerSlider);
            }
        });

        // 圆角滑块
        cornerSlider.addOnChangeListener((slider, value, fromUser) -> {
            cornerValue.setText((int) value + "dp");
            if (fromUser) {
                savePrefs(swCaptureText, swApplyRules, swAiModify, swLog,
                        swQuickBall, contentGroup, actionGroup, ballTextInput, sizeSlider, cornerSlider);
            }
        });

        // 图标选择：当 ChipGroup 切到"图标"模式时弹出图标选择器
        contentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_ball_icon) {
                showIconPicker();
            }
        });
    }

    /** 预设图标资源 ID 列表 */
    private static final int[] BALL_ICON_RES = {
            R.drawable.ic_ball_auto_fix,
            R.drawable.ic_ball_content_copy,
            R.drawable.ic_ball_swap_horiz,
            R.drawable.ic_ball_rule,
            R.drawable.ic_ball_description,
            R.drawable.ic_ball_dark_mode,
            R.drawable.ic_ball_info,
            R.drawable.ic_ball_settings,
            R.drawable.ic_ball_home,
    };

    private void showIconPicker() {
        GridLayout grid = new GridLayout(requireContext());
        grid.setColumnCount(5);
        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        grid.setPadding(pad, pad, pad, pad);

        FloatingWindowPrefs.Prefs prefs = FloatingWindowPrefs.load(requireContext());
        int currentRes = prefs.ballIconRes;

        for (int i = 0; i < BALL_ICON_RES.length; i++) {
            ImageView iv = new ImageView(requireContext());
            int iconSize = (int) (40 * getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = iconSize;
            lp.height = iconSize;
            lp.setMargins(pad / 2, pad / 2, pad / 2, pad / 2);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iv.setImageResource(BALL_ICON_RES[i]);
            iv.setBackgroundResource(R.drawable.icon_button_background);
            iv.setPadding(pad / 2, pad / 2, pad / 2, pad / 2);

            final int resId = BALL_ICON_RES[i];
            boolean selected = (currentRes == resId) || (currentRes == 0 && i == 0);
            if (selected) {
                iv.setAlpha(1.0f);
            } else {
                iv.setAlpha(0.45f);
            }

            iv.setOnClickListener(v -> {
                FloatingWindowPrefs.Prefs p = FloatingWindowPrefs.load(requireContext());
                p.ballIconRes = resId;
                FloatingWindowPrefs.save(requireContext(), p);
            });

            grid.addView(iv);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.floating_config_ball_icon_pick)
                .setView(grid)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void savePrefs(MaterialSwitch swCaptureText, MaterialSwitch swApplyRules,
                           MaterialSwitch swAiModify, MaterialSwitch swLog,
                           MaterialSwitch swQuickBall, ChipGroup contentGroup,
                           ChipGroup actionGroup, EditText ballTextInput,
                           Slider sizeSlider, Slider cornerSlider) {
        FloatingWindowPrefs.Prefs p = new FloatingWindowPrefs.Prefs();
        p.showCaptureText = swCaptureText.isChecked();
        p.showApplyRules = swApplyRules.isChecked();
        p.showAiModify = swAiModify.isChecked();
        p.showLog = swLog.isChecked();
        p.showQuickBall = swQuickBall.isChecked();
        p.ballContentType = contentGroup.getCheckedChipId() == R.id.chip_ball_text
                ? FloatingWindowPrefs.BALL_TEXT : FloatingWindowPrefs.BALL_ICON;
        p.ballAction = actionGroup.getCheckedChipId() == R.id.chip_ball_action_rules
                ? FloatingWindowPrefs.ACTION_RULES : FloatingWindowPrefs.ACTION_AI;
        p.ballText = ballTextInput.getText().toString();
        p.ballSizeDp = sizeSlider.getValue();
        p.ballCornerDp = cornerSlider.getValue();
        FloatingWindowPrefs.save(requireContext(), p);
    }
}
