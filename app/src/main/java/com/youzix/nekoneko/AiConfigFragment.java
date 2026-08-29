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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * AI 配置页：API 地址 / API Key / 模型 / 提示词，支持获取模型列表与预设管理。
 */
public class AiConfigFragment extends Fragment {

    private TextInputEditText baseUrlInput;
    private TextInputEditText apiKeyInput;
    private TextInputEditText modelInput;
    private TextInputEditText promptInput;

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

        // 展开按钮：弹出全屏对话框编辑提示词
        view.findViewById(R.id.expand_prompt_button).setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_prompt_edit, null);
            EditText fullEditor = dialogView.findViewById(R.id.prompt_edit_full);
            fullEditor.setText(promptInput.getText());

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.ai_prompt)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ai_save, (d, w) -> {
                        promptInput.setText(fullEditor.getText());
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        // 回填已保存的配置
        AiManager.Config cfg = AiManager.load(requireContext());
        baseUrlInput.setText(cfg.baseUrl);
        apiKeyInput.setText(cfg.apiKey);
        modelInput.setText(cfg.model);
        promptInput.setText(cfg.prompt);

        view.findViewById(R.id.restore_prompt_button).setOnClickListener(v ->
                promptInput.setText(AiManager.DEFAULT_PROMPT));

        view.findViewById(R.id.fetch_models_button).setOnClickListener(v -> fetchModels());

        view.findViewById(R.id.save_preset_button).setOnClickListener(v -> savePreset());

        view.findViewById(R.id.load_preset_button).setOnClickListener(v -> loadPreset());

        view.findViewById(R.id.delete_preset_button).setOnClickListener(v -> deletePreset());

        view.findViewById(R.id.save_ai_config_button).setOnClickListener(v -> {
            AiManager.save(requireContext(), currentConfig());
            Toast.makeText(requireContext(), R.string.ai_config_saved, Toast.LENGTH_SHORT).show();
        });
    }

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
                if (!isAdded()) {
                    return;
                }
                final String[] arr = models.toArray(new String[0]);
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.ai_models)
                        .setItems(arr, (d, w) -> modelInput.setText(arr[w]))
                        .show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(),
                        getString(R.string.ai_call_failed, message), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void savePreset() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preset_name, null);
        final EditText nameInput = dialogView.findViewById(R.id.preset_name_input);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ai_save_preset)
                .setView(dialogView)
                .setPositiveButton(R.string.ai_save, (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.ai_preset_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AiManager.savePreset(requireContext(), name, currentConfig());
                    Toast.makeText(requireContext(),
                            getString(R.string.ai_preset_saved, name), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void loadPreset() {
        final List<String> names = AiManager.getAllPresetNames(requireContext());
        if (names.isEmpty()) {
            Toast.makeText(requireContext(), R.string.ai_no_presets, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ai_load_preset)
                .setItems(names.toArray(new String[0]), (d, w) ->
                        applyPreset(AiManager.loadPreset(requireContext(), names.get(w))))
                .show();
    }

    private void deletePreset() {
        final List<String> names = AiManager.getUserPresetNames(requireContext());
        if (names.isEmpty()) {
            Toast.makeText(requireContext(), R.string.ai_no_presets, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ai_delete_preset)
                .setItems(names.toArray(new String[0]), (d, w) -> {
                    final String name = names.get(w);
                    new MaterialAlertDialogBuilder(requireContext())
                            .setMessage(getString(R.string.ai_confirm_delete_preset, name))
                            .setPositiveButton(android.R.string.ok, (d2, w2) -> {
                                AiManager.deletePreset(requireContext(), name);
                                Toast.makeText(requireContext(),
                                        getString(R.string.ai_preset_deleted, name), Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                })
                .show();
    }

    private AiManager.Config currentConfig() {
        AiManager.Config c = new AiManager.Config();
        c.baseUrl = baseUrlInput.getText().toString().trim();
        c.apiKey = apiKeyInput.getText().toString().trim();
        c.model = modelInput.getText().toString().trim();
        c.prompt = promptInput.getText().toString().trim();
        return c;
    }

    /** 应用预设：预设中非空字段覆盖当前值，空字段沿用当前值（避免覆盖 API Key）。 */
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
        Toast.makeText(requireContext(), R.string.ai_preset_applied, Toast.LENGTH_SHORT).show();
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
