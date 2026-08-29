package com.youzix.nekoneko;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * AI 配置界面：API 地址 / API Key / 模型 / 提示词，支持获取模型列表与预设管理。
 */
public class AiConfigActivity extends AppCompatActivity {

    private TextInputEditText baseUrlInput;
    private TextInputEditText apiKeyInput;
    private TextInputEditText modelInput;
    private TextInputEditText promptInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_config);

        baseUrlInput = findViewById(R.id.ai_base_url_input);
        apiKeyInput = findViewById(R.id.ai_api_key_input);
        modelInput = findViewById(R.id.ai_model_input);
        promptInput = findViewById(R.id.ai_prompt_input);

        // 回填已保存的配置
        AiManager.Config cfg = AiManager.load(this);
        baseUrlInput.setText(cfg.baseUrl);
        apiKeyInput.setText(cfg.apiKey);
        modelInput.setText(cfg.model);
        promptInput.setText(cfg.prompt);

        // 恢复默认提示词
        findViewById(R.id.restore_prompt_button).setOnClickListener(v ->
                promptInput.setText(AiManager.DEFAULT_PROMPT));

        // 获取模型列表
        findViewById(R.id.fetch_models_button).setOnClickListener(v -> fetchModels());

        // 保存为预设
        findViewById(R.id.save_preset_button).setOnClickListener(v -> {
            final EditText nameInput = new EditText(this);
            nameInput.setHint(R.string.ai_preset_name);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ai_save_preset)
                    .setView(nameInput)
                    .setPositiveButton(R.string.ai_save, (d, w) -> {
                        String name = nameInput.getText().toString().trim();
                        if (name.isEmpty()) {
                            Toast.makeText(this, R.string.ai_preset_name, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AiManager.savePreset(this, name, currentConfig());
                        Toast.makeText(this, getString(R.string.ai_preset_saved, name), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        // 加载预设
        findViewById(R.id.load_preset_button).setOnClickListener(v -> {
            final List<String> names = AiManager.getAllPresetNames(this);
            if (names.isEmpty()) {
                Toast.makeText(this, R.string.ai_no_presets, Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ai_load_preset)
                    .setItems(names.toArray(new String[0]), (d, w) -> applyPreset(AiManager.loadPreset(this, names.get(w))))
                    .show();
        });

        // 删除预设（仅用户自定义）
        findViewById(R.id.delete_preset_button).setOnClickListener(v -> {
            final List<String> names = AiManager.getUserPresetNames(this);
            if (names.isEmpty()) {
                Toast.makeText(this, R.string.ai_no_presets, Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ai_delete_preset)
                    .setItems(names.toArray(new String[0]), (d, w) -> {
                        final String name = names.get(w);
                        new AlertDialog.Builder(this)
                                .setMessage(getString(R.string.ai_confirm_delete_preset, name))
                                .setPositiveButton(android.R.string.ok, (d2, w2) -> {
                                    AiManager.deletePreset(this, name);
                                    Toast.makeText(this, getString(R.string.ai_preset_deleted, name), Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                    })
                    .show();
        });

        // 保存配置
        findViewById(R.id.save_ai_config_button).setOnClickListener(v -> {
            AiManager.save(this, currentConfig());
            Toast.makeText(this, R.string.ai_config_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void fetchModels() {
        AiManager.Config cfg = currentConfig();
        if (cfg.apiKey.trim().isEmpty()) {
            Toast.makeText(this, R.string.ai_key_missing, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, R.string.ai_loading_models, Toast.LENGTH_SHORT).show();
        AiManager.listModels(cfg, new AiManager.ListCallback() {
            @Override
            public void onSuccess(List<String> models) {
                final String[] arr = models.toArray(new String[0]);
                new AlertDialog.Builder(AiConfigActivity.this)
                        .setTitle(R.string.ai_models)
                        .setItems(arr, (d, w) -> modelInput.setText(arr[w]))
                        .show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AiConfigActivity.this,
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
        Toast.makeText(this, R.string.ai_preset_applied, Toast.LENGTH_SHORT).show();
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
