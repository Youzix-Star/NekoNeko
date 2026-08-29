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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.youzix.nekoneko.AiManager
import com.youzix.nekoneko.R

/** AI 配置页：API 地址 / API Key / 模型 / 提示词，支持获取模型列表与预设管理。 */
@Composable
fun AiConfigScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val cfg = remember { AiManager.load(context) }

    var baseUrl by remember { mutableStateOf(cfg.baseUrl) }
    var apiKey by remember { mutableStateOf(cfg.apiKey) }
    var model by remember { mutableStateOf(cfg.model) }
    var prompt by remember { mutableStateOf(cfg.prompt) }

    var models by remember { mutableStateOf<List<String>>(emptyList()) }
    var showModelsDialog by remember { mutableStateOf(false) }
    var presetNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showLoadPresetDialog by remember { mutableStateOf(false) }
    var showDeletePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }

    fun currentConfig(): AiManager.Config {
        val c = AiManager.Config()
        c.baseUrl = baseUrl.trim()
        c.apiKey = apiKey.trim()
        c.model = model.trim()
        c.prompt = prompt.trim()
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
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.ai_config),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
            text = stringResource(R.string.ai_config_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(24.dp))

        SectionTitle(text = stringResource(R.string.section_service))
        TextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text(stringResource(R.string.ai_base_url)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )
        TextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text(stringResource(R.string.ai_api_key)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )
        TextField(
            value = model,
            onValueChange = { model = it },
            label = { Text(stringResource(R.string.ai_model)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        TextButton(
            onClick = { fetchModels() },
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(stringResource(R.string.ai_fetch_models))
        }

        SectionTitle(text = stringResource(R.string.ai_prompt))
        TextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text(stringResource(R.string.ai_prompt)) },
            minLines = 6,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        TextButton(
            onClick = { prompt = AiManager.DEFAULT_PROMPT },
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(stringResource(R.string.ai_restore_default_prompt))
        }

        SectionTitle(text = stringResource(R.string.ai_presets))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = { showSavePresetDialog = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.ai_save_preset))
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
                Text(stringResource(R.string.ai_load_preset))
            }
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    presetNames = AiManager.getUserPresetNames(context)
                    if (presetNames.isEmpty()) {
                        Toast.makeText(context, R.string.ai_no_presets, Toast.LENGTH_SHORT).show()
                    } else {
                        showDeletePresetDialog = true
                    }
                },
            ) {
                Text(stringResource(R.string.ai_delete_preset))
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
                .padding(horizontal = 16.dp),
        ) {
            Text(stringResource(R.string.ai_save))
        }
        Spacer(Modifier.height(24.dp))
    }

    // 模型列表对话框
    if (showModelsDialog) {
        AlertDialog(
            onDismissRequest = { showModelsDialog = false },
            title = { Text(stringResource(R.string.ai_models)) },
            text = {
                Column {
                    models.forEach { m ->
                        Text(
                            text = m,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    model = m
                                    showModelsDialog = false
                                }
                                .padding(vertical = 10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelsDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }

    // 保存预设对话框
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text(stringResource(R.string.ai_save_preset)) },
            text = {
                TextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text(stringResource(R.string.ai_preset_name)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = presetName.trim()
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
                        presetName = ""
                    }
                }) {
                    Text(stringResource(R.string.ai_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }

    // 加载预设对话框
    if (showLoadPresetDialog) {
        AlertDialog(
            onDismissRequest = { showLoadPresetDialog = false },
            title = { Text(stringResource(R.string.ai_load_preset)) },
            text = {
                Column {
                    presetNames.forEach { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val preset = AiManager.loadPreset(context, name)
                                    val cur = currentConfig()
                                    if (!preset.baseUrl.isNullOrBlank()) baseUrl = preset.baseUrl
                                    if (!preset.apiKey.isNullOrBlank()) apiKey = preset.apiKey
                                    if (!preset.model.isNullOrBlank()) model = preset.model
                                    if (!preset.prompt.isNullOrBlank()) prompt = preset.prompt
                                    showLoadPresetDialog = false
                                }
                                .padding(vertical = 10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLoadPresetDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }

    // 删除预设对话框
    if (showDeletePresetDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePresetDialog = false },
            title = { Text(stringResource(R.string.ai_delete_preset)) },
            text = {
                Column {
                    presetNames.forEach { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
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
                                .padding(vertical = 10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeletePresetDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}
