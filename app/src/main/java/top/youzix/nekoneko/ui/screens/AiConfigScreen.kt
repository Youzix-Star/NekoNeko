package top.youzix.nekoneko.ui.screens

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.youzix.nekoneko.AiManager
import top.youzix.nekoneko.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var presets by remember { mutableStateOf<List<String>>(emptyList()) }

    // UI state
    var showPassword by remember { mutableStateOf(false) }
    var showPresetNameDialog by remember { mutableStateOf(false) }
    var showModelListDialog by remember { mutableStateOf(false) }
    var showDeletePresetDialog by remember { mutableStateOf(false) }
    var pendingDeletePreset by remember { mutableStateOf<String?>(null) }
    var presetNameInput by remember { mutableStateOf("") }
    var modelList by remember { mutableStateOf<List<String>>(emptyList()) }

    // Load initial data
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val config = AiManager.load(context)
            if (config != null) {
                baseUrl = config.baseUrl ?: ""
                apiKey = config.apiKey ?: ""
                model = config.model ?: ""
                systemPrompt = config.systemPrompt ?: ""
                prompt = config.prompt ?: ""
            }
            presets = AiManager.getAllPresetNames(context)
        }
    }

    // --- Dialogs ---

    // Preset name input dialog
    if (showPresetNameDialog) {
        AlertDialog(
            onDismissRequest = { showPresetNameDialog = false },
            title = { Text("保存预设") },
            text = {
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text("预设名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (presetNameInput.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            val cfg = AiManager.Config()
                            cfg.baseUrl = baseUrl
                            cfg.apiKey = apiKey
                            cfg.model = model
                            cfg.systemPrompt = systemPrompt
                            cfg.prompt = prompt
                            AiManager.savePreset(
                                context,
                                presetNameInput,
                                cfg
                            )
                            presets = AiManager.getAllPresetNames(context)
                        }
                    }
                    showPresetNameDialog = false
                    presetNameInput = ""
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPresetNameDialog = false
                    presetNameInput = ""
                }) {
                    Text("取消")
                }
            }
        )
    }

    // Model list dialog
    if (showModelListDialog) {
        AlertDialog(
            onDismissRequest = { showModelListDialog = false },
            title = { Text("选择模型") },
            text = {
                if (modelList.isEmpty()) {
                    Text("暂无可用模型")
                } else {
                    Column {
                        modelList.forEach { modelName ->
                            TextButton(
                                onClick = {
                                    model = modelName
                                    showModelListDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(modelName)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelListDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Preset delete confirmation dialog
    if (showDeletePresetDialog && pendingDeletePreset != null) {
        AlertDialog(
            onDismissRequest = {
                showDeletePresetDialog = false
                pendingDeletePreset = null
            },
            title = { Text("删除预设") },
            text = { Text("确定要删除预设「$pendingDeletePreset」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        AiManager.deletePreset(context, pendingDeletePreset!!)
                        presets = AiManager.getAllPresetNames(context) ?: emptyList()
                    }
                    showDeletePresetDialog = false
                    pendingDeletePreset = null
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeletePresetDialog = false
                    pendingDeletePreset = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    // --- Main UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "AI 配置",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "配置 AI 服务",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Presets Section ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(text = "预设")
                    TextButton(onClick = {
                        presetNameInput = ""
                        showPresetNameDialog = true
                    }) {
                        Text("另存为预设")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (presets.isEmpty()) {
                    Text(
                        text = "暂无预设",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { presetName ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val loaded = AiManager.loadPreset(context, presetName)
                                        if (loaded != null) {
                                            if (!loaded.baseUrl.isNullOrEmpty()) baseUrl = loaded.baseUrl
                                            if (!loaded.apiKey.isNullOrEmpty()) apiKey = loaded.apiKey
                                            if (!loaded.model.isNullOrEmpty()) model = loaded.model
                                            if (!loaded.systemPrompt.isNullOrEmpty()) systemPrompt = loaded.systemPrompt
                                            if (!loaded.prompt.isNullOrEmpty()) prompt = loaded.prompt
                                        }
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "已应用预设「$presetName」", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                label = { Text(presetName) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Service Config Section ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionLabel(text = "服务配置")

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text(stringResource(R.string.ai_base_url)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPassword) "隐藏" else "显示"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("模型") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                val list = AiManager.listModels(context)
                                modelList = list ?: emptyList()
                                withContext(Dispatchers.Main) {
                                    showModelListDialog = true
                                }
                            }
                        }) {
                            Text("▼")
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- System Prompt Section ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(text = "系统框架提示词")
                    TextButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            systemPrompt = AiManager.getDefaultSystemPrompt(context) ?: ""
                        }
                    }) {
                        Text("恢复默认框架")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("系统提示词") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    maxLines = 10
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- User Prompt Section ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(text = "人设提示词")
                    TextButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            prompt = AiManager.getDefaultPrompt(context) ?: ""
                        }
                    }) {
                        Text("恢复默认人设")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("人设提示词") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    maxLines = 10
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Save Button ---
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    AiManager.save(
                        context,
                        AiManager.Config(
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            model = model,
                            systemPrompt = systemPrompt,
                            prompt = prompt
                        )
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}
