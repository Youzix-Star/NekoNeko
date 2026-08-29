package com.youzix.nekoneko;

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

/**
 * AI 配置页：预设（顶部横向 Chip）+ API 设置 + 提示词。
 */
public class AiConfigFragment extends Fragment {

    private TextInputEditText baseUrlInput;
    private TextInputEditText apiKeyInput;
    private TextInputEditText modelInput;
    private TextInputEditText promptInput;
    private ChipGroup presetChipGroup;

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
        promptInput = view.findViewById(R.id.ai_prompt_input);
        presetChipGroup = view.findViewById(R.id.preset_chip_group);

        // 回填配置
        AiManager.Config cfg = AiManager.load(requireContext());
        baseUrlInput.setText(cfg.baseUrl);
        apiKeyInput.setText(cfg.apiKey);
        modelInput.setText(cfg.model);
        promptInput.setText(cfg.prompt);

        view.findViewById(R.id.restore_prompt_button).setOnClickListener(v ->
                promptInput.setText(AiManager.DEFAULT_PROMPT));
        view.findViewById(R.id.fetch_models_button).setOnClickListener(v -> fetchModels());
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
                    || AiManager.PRESET_MS_CHINESE.equals(name)) {
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
        Toast.makeText(requireContext(), R.string.ai_loading_models, Toast.LENGTH_SHORT).show();
        AiManager.listModels(cfg, new AiManager.ListCallback() {
            @Override
            public void onSuccess(List<String> models) {
                if (!isAdded()) return;
                final String[] arr = models.toArray(new String[0]);
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.ai_models)
                        .setItems(arr, (d, w) -> modelInput.setText(arr[w]))
                        .show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
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
        c.prompt = promptInput.getText().toString().trim();
        return c;
    }

    private void applyPreset(AiManager.Config preset) {
        AiManager.Config current = currentConfig();
        AiManager.Config merged = new AiManager.Config();
        merged.baseUrl = notEmpty(preset.baseUrl) ? preset.baseUrl : current.baseUrl;
        merged.apiKey = notEmpty(preset.apiKey) ? preset.apiKey : current.apiKey;
        merged.model = notEmpty(preset.model) ? preset.model : current.model;
        merged.prompt = notEmpty(preset.prompt) ? preset.prompt : current.prompt;
        baseUrlInput.setText(merged.baseUrl);
        apiKeyInput.setText(merged.apiKey);
        modelInput.setText(merged.model);
        promptInput.setText(merged.prompt);
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
