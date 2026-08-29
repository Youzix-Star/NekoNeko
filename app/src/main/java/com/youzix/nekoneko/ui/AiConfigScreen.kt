package com.youzix.nekoneko.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import com.youzix.nekoneko.AiManager
import com.youzix.nekoneko.R

/** AI 配置页：API 地址 / API Key / 模型 / 提示词，支持获取模型列表与预设管理。 */
@Composable
fun AiConfigScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val cfg = remember { AiManager.load(context) }

    val baseUrlState = remember { TextFieldState(cfg.baseUrl) }
    val apiKeyState = remember { TextFieldState(cfg.apiKey) }
    val modelState = remember { TextFieldState(cfg.model) }
    val promptState = remember { TextFieldState(cfg.prompt) }

    var models by remember { mutableStateOf<List<String>>(emptyList()) }
    var showModelsDialog by remember { mutableStateOf(false) }
    var presetNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showLoadPresetDialog by remember { mutableStateOf(false) }
    var showDeletePresetDialog by remember { mutableStateOf(false) }

    fun currentConfig(): AiManager.Config {
        val c = AiManager.Config()
        c.baseUrl = baseUrlState.text.toString().trim()
        c.apiKey = apiKeyState.text.toString().trim()
        c.model = modelState.text.toString().trim()
        c.prompt = promptState.text.toString().trim()
        return c
    }

    fun fetchModels() {
        val c = currentConfig()
        if (c.apiKey.isEmpty()) {
            Toast.makeText(context, R.string.ai_key_missing, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(context, R.string.ai_loading_models, Toast.LENGTH_SHORT).show()
        AiManager.listModels(c, object : AiManager.ListCallback {
            override fun onSuccess(list: List<String>) {
                models = list
                showModelsDialog = true
            }

            override fun onError(message: String) {
                Toast.makeText(context, context.getString(R.string.ai_call_failed, message), Toast.LENGTH_LONG).show()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.ai_config),
            style = MiuixTheme.textStyles.title1,
            color = MiuixTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
        Text(
            text = stringResource(R.string.ai_config_subtitle),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
        Spacer(Modifier.height(16.dp))

        // 服务配置
        SmallTitle(text = stringResource(R.string.section_service))
        Card(modifier = Modifier.padding(horizontal = 12.dp)) {
            TextField(
                state = baseUrlState,
                label = stringResource(R.string.ai_base_url),
            )
            TextField(
                state = apiKeyState,
                label = stringResource(R.string.ai_api_key),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            TextField(
                state = modelState,
                label = stringResource(R.string.ai_model),
            )
            TextButton(
                text = stringResource(R.string.ai_fetch_models),
                onClick = { fetchModels() },
            )
        }

        // 提示词
        SmallTitle(text = stringResource(R.string.ai_prompt))
        Card(modifier = Modifier.padding(horizontal = 12.dp)) {
            TextField(
                state = promptState,
                label = stringResource(R.string.ai_prompt),
                useLabelAsPlaceholder = true,
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 6, maxHeightInLines = 6),
            )
            TextButton(
                text = stringResource(R.string.ai_restore_default_prompt),
                onClick = { promptState.setTextAndPlaceCursorAtEnd(AiManager.DEFAULT_PROMPT) },
            )
        }

        // 预设
        SmallTitle(text = stringResource(R.string.ai_presets))
        Card(modifier = Modifier.padding(horizontal = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { showSavePresetDialog = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.ai_save_preset))
                }
                Button(
                    onClick = {
                        presetNames = AiManager.getAllPresetNames(context)
                        if (presetNames.isEmpty()) {
                            Toast.makeText(context, R.string.ai_no_presets, Toast.LENGTH_SHORT).show()
                        } else {
                            showLoadPresetDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.ai_load_preset))
                }
                TextButton(
                    text = stringResource(R.string.ai_delete_preset),
                    onClick = {
                        presetNames = AiManager.getUserPresetNames(context)
                        if (presetNames.isEmpty()) {
                            Toast.makeText(context, R.string.ai_no_presets, Toast.LENGTH_SHORT).show()
                        } else {
                            showDeletePresetDialog = true
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                AiManager.save(context, currentConfig())
                Toast.makeText(context, R.string.ai_config_saved, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Text(text = stringResource(R.string.ai_save))
        }
        Spacer(Modifier.height(24.dp))
    }

    // 模型列表对话框
    if (showModelsDialog) {
        WindowDialog(
            show = true,
            title = stringResource(R.string.ai_models),
            onDismissRequest = { showModelsDialog = false },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                models.forEach { m ->
                    Text(
                        text = m,
                        style = MiuixTheme.textStyles.main,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                modelState.setTextAndPlaceCursorAtEnd(m)
                                showModelsDialog = false
                            }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }

    // 保存预设对话框
    if (showSavePresetDialog) {
        val nameState = remember { TextFieldState("") }
        WindowDialog(
            show = true,
            title = stringResource(R.string.ai_save_preset),
            onDismissRequest = { showSavePresetDialog = false },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    state = nameState,
                    label = stringResource(R.string.ai_preset_name),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        text = stringResource(android.R.string.cancel),
                        onClick = { showSavePresetDialog = false },
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val name = nameState.text.toString().trim()
                            if (name.isEmpty()) {
                                Toast.makeText(context, R.string.ai_preset_name, Toast.LENGTH_SHORT).show()
                            } else {
                                AiManager.savePreset(context, name, currentConfig())
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.ai_preset_saved, name),
                                    Toast.LENGTH_SHORT
                                ).show()
                                showSavePresetDialog = false
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.ai_save))
                    }
                }
            }
        }
    }

    // 加载预设对话框
    if (showLoadPresetDialog) {
        WindowDialog(
            show = true,
            title = stringResource(R.string.ai_load_preset),
            onDismissRequest = { showLoadPresetDialog = false },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                presetNames.forEach { name ->
                    Text(
                        text = name,
                        style = MiuixTheme.textStyles.main,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                applyPreset(AiManager.loadPreset(context, name), currentConfig(),
                                    baseUrlState, apiKeyState, modelState, promptState)
                                showLoadPresetDialog = false
                            }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }

    // 删除预设对话框
    if (showDeletePresetDialog) {
        WindowDialog(
            show = true,
            title = stringResource(R.string.ai_delete_preset),
            onDismissRequest = { showDeletePresetDialog = false },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                presetNames.forEach { name ->
                    Text(
                        text = name,
                        style = MiuixTheme.textStyles.main,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AiManager.deletePreset(context, name)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.ai_preset_deleted, name),
                                    Toast.LENGTH_SHORT
                                ).show()
                                showDeletePresetDialog = false
                            }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

/** 应用预设：预设中非空字段覆盖当前值，空字段沿用当前值（避免覆盖 API Key）。 */
private fun applyPreset(
    preset: AiManager.Config,
    current: AiManager.Config,
    baseUrlState: TextFieldState,
    apiKeyState: TextFieldState,
    modelState: TextFieldState,
    promptState: TextFieldState,
) {
    if (!preset.baseUrl.isNullOrBlank()) baseUrlState.setTextAndPlaceCursorAtEnd(preset.baseUrl)
    if (!preset.apiKey.isNullOrBlank()) apiKeyState.setTextAndPlaceCursorAtEnd(preset.apiKey)
    if (!preset.model.isNullOrBlank()) modelState.setTextAndPlaceCursorAtEnd(preset.model)
    if (!preset.prompt.isNullOrBlank()) promptState.setTextAndPlaceCursorAtEnd(preset.prompt)
    // 与旧行为一致：合并语义（空字段沿用当前值）——current 参数保留用于说明
}
