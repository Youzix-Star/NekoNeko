package com.youzix.nekoneko;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

/**
 * AI 配置界面：API 地址 / API Key / 模型 / 提示词。
 */
public class AiConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_config);

        TextInputEditText baseUrlInput = findViewById(R.id.ai_base_url_input);
        TextInputEditText apiKeyInput = findViewById(R.id.ai_api_key_input);
        TextInputEditText modelInput = findViewById(R.id.ai_model_input);
        TextInputEditText promptInput = findViewById(R.id.ai_prompt_input);

        // 回填已保存的配置
        AiManager.Config cfg = AiManager.load(this);
        baseUrlInput.setText(cfg.baseUrl);
        apiKeyInput.setText(cfg.apiKey);
        modelInput.setText(cfg.model);
        promptInput.setText(cfg.prompt);

        // 恢复默认提示词
        findViewById(R.id.restore_prompt_button).setOnClickListener(v ->
                promptInput.setText(AiManager.DEFAULT_PROMPT));

        // 保存配置
        findViewById(R.id.save_ai_config_button).setOnClickListener(v -> {
            AiManager.Config c = new AiManager.Config();
            c.baseUrl = baseUrlInput.getText().toString().trim();
            c.apiKey = apiKeyInput.getText().toString().trim();
            c.model = modelInput.getText().toString().trim();
            c.prompt = promptInput.getText().toString().trim();
            AiManager.save(this, c);
            Toast.makeText(this, R.string.ai_config_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
