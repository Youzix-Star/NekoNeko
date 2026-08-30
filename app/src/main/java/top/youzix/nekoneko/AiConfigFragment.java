package top.youzix.nekoneko;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.ArrayList;

/**
 * AI 配置页：预设（顶部横向 Chip）+ API 设置 + 提示词。
 */
public class AiConfigFragment extends Fragment {

    private TextInputEditText baseUrlInput;
    private TextInputEditText apiKeyInput;
    private TextInputEditText modelInput;
    private TextInputEditText systemPromptInput;
    private TextInputEditText promptInput;
    private ChipGroup presetChipGroup;
    private ExpandableDropdownCard modelDropdownCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        baseUrlInput = view.findViewById(R.id.ai_base_url_input);
        apiKeyInput = view.findViewById(R.id.ai_api_key_input);
        modelInput = view.findViewById(R.id.ai_model_input);
        systemPromptInput = view.findViewById(R.id.ai_system_prompt_input);
        promptInput = view.findViewById(R.id.ai_prompt_input);
        presetChipGroup = view.findViewById(R.id.preset_chip_group);
        modelDropdownCard = view.findViewById(R.id.model_dropdown_card);

        // 回填配置
        AiManager.Config cfg = AiManager.load(requireContext());
        baseUrlInput.setText(cfg.baseUrl);
        apiKeyInput.setText(cfg.apiKey);
        modelInput.setText(cfg.model);
        systemPromptInput.setText(cfg.systemPrompt);
        promptInput.setText(cfg.prompt);

        // 设置模型下拉卡片提示文字
        modelDropdownCard.setHint(getString(R.string.ai_fetch_models));

        // 手动输入模型时同步更新下拉卡片状态
        modelInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // 失去焦点时，更新下拉卡片的选中状态
                String currentModel = modelInput.getText().toString().trim();
                if (!currentModel.isEmpty()) {
                    modelDropdownCard.setHint(getString(R.string.ai_selected_model_fmt, currentModel));
                }
            }
        });

        view.findViewById(R.id.restore_system_prompt_button).setOnClickListener(v ->
                systemPromptInput.setText(AiManager.DEFAULT_SYSTEM_PROMPT));
        view.findViewById(R.id.restore_prompt_button).setOnClickListener(v ->
                promptInput.setText(AiManager.DEFAULT_PROMPT));
        // 首次点击下拉卡片标题行 → 获取模型列表
        modelDropdownCard.setOnFirstClickAction(this::fetchModels);
        view.findViewById(R.id.save_as_preset_button).setOnClickListener(v -> saveAsPreset());
        view.findViewById(R.id.save_ai_config_button).setOnClickListener(v -> {
            AiManager.save(requireContext(), currentConfig());
            Toast.makeText(requireContext(), R.string.ai_config_saved, Toast.LENGTH_SHORT).show();
        });

        refreshPresetChips();
    }

    // ---------- 预设 ----------

    private void refreshPresetChips() {
        presetChipGroup.removeAllViews();

        // 内置预设
        for (String name : AiManager.getAllPresetNames(requireContext())) {
            Chip chip = makeChip(name);
            presetChipGroup.addView(chip);
        }
    }

    private Chip makeChip(String name) {
        Chip chip = new Chip(requireContext());
        chip.setText(name);
        chip.setCheckable(true);

        chip.setOnLongClickListener(v -> {
            // 内置预设不能删除
            if (AiManager.PRESET_MS_TRANSLATE.equals(name)
                    || AiManager.PRESET_MS_CHINESE.equals(name)
                    || AiManager.PRESET_EMOJI.equals(name)) {
                return true;
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage(getString(R.string.preset_confirm_delete, name))
                    .setPositiveButton(android.R.string.ok, (d, w) -> {
                        AiManager.deletePreset(requireContext(), name);
                        refreshPresetChips();
                        Toast.makeText(requireContext(),
                                getString(R.string.preset_deleted, name), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        });

        chip.setOnClickListener(v -> {
            AiManager.Config preset = AiManager.loadPreset(requireContext(), name);
            applyPreset(preset);
            Toast.makeText(requireContext(), R.string.preset_applied, Toast.LENGTH_SHORT).show();
        });

        return chip;
    }

    private void saveAsPreset() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preset_name, null);
        final EditText nameInput = dialogView.findViewById(R.id.preset_name_input);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.save_as_preset)
                .setView(dialogView)
                .setPositiveButton(R.string.ai_save, (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.preset_no_name,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AiManager.savePreset(requireContext(), name, currentConfig());
                    refreshPresetChips();
                    Toast.makeText(requireContext(),
                            getString(R.string.preset_saved, name), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ---------- AI 调用 ----------

    private void fetchModels() {
        AiManager.Config cfg = currentConfig();
        if (cfg.apiKey.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.ai_key_missing, Toast.LENGTH_LONG).show();
            return;
        }

        // 显示加载状态
        modelDropdownCard.showLoading();

        AiManager.listModels(cfg, new AiManager.ListCallback() {
            @Override
            public void onSuccess(List<String> models) {
                if (!isAdded()) return;

                // 构建列表：在前面加一个"手动输入"占位项
                List<String> list = new ArrayList<>();
                list.add(0, getString(R.string.ai_manual_input_model));
                list.addAll(models);
                final String[] arr = list.toArray(new String[0]);

                // 找到当前模型在列表中的位置，如果有就自动选中
                String currentModel = modelInput.getText().toString().trim();
                int preSelectIndex = -1;
                if (!currentModel.isEmpty()) {
                    for (int i = 1; i < arr.length; i++) {
                        if (currentModel.equals(arr[i])) {
                            preSelectIndex = i;
                            break;
                        }
                    }
                }

                // 设置到下拉卡片，触发展开
                modelDropdownCard.setItems(arr, (position, itemName) -> {
                    if (position == 0) {
                        // "手动输入" → 清空模型输入框，让用户自己输入
                        modelInput.setText("");
                        modelInput.requestFocus();
                        modelDropdownCard.setHint(getString(R.string.ai_fetch_models));
                    } else {
                        modelInput.setText(itemName);
                    }
                });

                // 自动展开
                modelDropdownCard.expand();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                modelDropdownCard.showError(getString(R.string.ai_fetch_models_error, message));
                Toast.makeText(requireContext(),
                        getString(R.string.ai_call_failed, message), Toast.LENGTH_LONG).show();
            }
        });
    }

    private AiManager.Config currentConfig() {
        AiManager.Config c = new AiManager.Config();
        c.baseUrl = baseUrlInput.getText().toString().trim();
        c.apiKey = apiKeyInput.getText().toString().trim();
        c.model = modelInput.getText().toString().trim();
        c.systemPrompt = systemPromptInput.getText().toString().trim();
        c.prompt = promptInput.getText().toString().trim();
        return c;
    }

    private void applyPreset(AiManager.Config preset) {
        AiManager.Config current = currentConfig();
        AiManager.Config merged = new AiManager.Config();
        merged.baseUrl = notEmpty(preset.baseUrl) ? preset.baseUrl : current.baseUrl;
        merged.apiKey = notEmpty(preset.apiKey) ? preset.apiKey : current.apiKey;
        merged.model = notEmpty(preset.model) ? preset.model : current.model;
        merged.systemPrompt = notEmpty(preset.systemPrompt) ? preset.systemPrompt : current.systemPrompt;
        merged.prompt = notEmpty(preset.prompt) ? preset.prompt : current.prompt;
        baseUrlInput.setText(merged.baseUrl);
        apiKeyInput.setText(merged.apiKey);
        modelInput.setText(merged.model);
        systemPromptInput.setText(merged.systemPrompt);
        promptInput.setText(merged.prompt);
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
