package top.youzix.nekoneko.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.youzix.nekoneko.R
import top.youzix.nekoneko.RuleManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RulesConfigScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State
    val rules = remember { mutableStateListOf<RuleManager.Rule>() }
    var currentPresetName by remember { mutableStateOf("当前配置") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableIntStateOf(-1) }

    // Edit dialog state
    var editName by remember { mutableStateOf("") }
    var editPattern by remember { mutableStateOf("") }
    var editReplacement by remember { mutableStateOf("") }
    var editIsRegex by remember { mutableStateOf(false) }

    // Import dialog state
    var importText by remember { mutableStateOf("") }

    // Export dialog state
    var exportType by remember { mutableIntStateOf(0) } // 0=全部, 1=仅文本, 2=仅正则

    // Preset state
    var presetNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var showPresetNameDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var showDeletePresetDialog by remember { mutableStateOf(false) }
    var pendingDeletePreset by remember { mutableStateOf<String?>(null) }

    // Delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteIndex by remember { mutableIntStateOf(-1) }

    // Load initial data
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val loadedRules = RuleManager.load(context)
            rules.clear()
            rules.addAll(loadedRules)
            presetNames = RuleManager.getPresetNames(context)
        }
    }

    // --- Dialogs ---

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                importText = ""
            },
            title = { Text("导入规则") },
            text = {
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    label = { Text("粘贴规则文本") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    maxLines = 10
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (importText.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            val imported = RuleManager.importFromText(context, importText)
                            if (imported != null) {
                                withContext(Dispatchers.Main) {
                                    rules.clear()
                                    rules.addAll(imported)
                                    Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "导入失败，请检查格式", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    showImportDialog = false
                    importText = ""
                }) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    importText = ""
                }) {
                    Text("取消")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("导出规则") },
            text = {
                Column {
                    Text("选择导出类型：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = exportType == 0,
                            onClick = { exportType = 0 },
                            label = { Text("全部") }
                        )
                        FilterChip(
                            selected = exportType == 1,
                            onClick = { exportType = 1 },
                            label = { Text("仅文本") }
                        )
                        FilterChip(
                            selected = exportType == 2,
                            onClick = { exportType = 2 },
                            label = { Text("仅正则") }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val filteredRules = when (exportType) {
                        1 -> rules.filter { !it.isRegex }
                        2 -> rules.filter { it.isRegex }
                        else -> rules
                    }
                    val exportText = filteredRules.joinToString("\n") { rule ->
                        "${if (rule.isRegex) "regex:" else "text:"}${rule.pattern} -> ${rule.replacement}"
                    }
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("rules", exportText))
                    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                }) {
                    Text("导出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Edit Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                editingIndex = -1
            },
            title = { Text(if (editingIndex == -1) "添加规则" else "编辑规则") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("规则名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPattern,
                        onValueChange = { editPattern = it },
                        label = { Text("匹配模式") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editReplacement,
                        onValueChange = { editReplacement = it },
                        label = { Text("替换内容") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("使用正则表达式")
                        Switch(
                            checked = editIsRegex,
                            onCheckedChange = { editIsRegex = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank() && editPattern.isNotBlank()) {
                        if (editIsRegex && !RuleManager.isValidPattern(editPattern)) {
                            Toast.makeText(context, "无效的正则表达式", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val rule = RuleManager.Rule(
                            name = editName,
                            pattern = editPattern,
                            replacement = editReplacement,
                            isRegex = editIsRegex,
                            enabled = if (editingIndex == -1) true else rules[editingIndex].enabled
                        )
                        if (editingIndex == -1) {
                            rules.add(rule)
                        } else {
                            rules[editingIndex] = rule
                        }
                        showEditDialog = false
                        editingIndex = -1
                    }
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    editingIndex = -1
                }) {
                    Text("取消")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && deleteIndex in rules.indices) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deleteIndex = -1
            },
            title = { Text("删除规则") },
            text = { Text("确定要删除规则「${rules[deleteIndex].name}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    rules.removeAt(deleteIndex)
                    showDeleteDialog = false
                    deleteIndex = -1
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteIndex = -1
                }) {
                    Text("取消")
                }
            }
        )
    }

    // Preset Name Dialog
    if (showPresetNameDialog) {
        AlertDialog(
            onDismissRequest = {
                showPresetNameDialog = false
                presetNameInput = ""
            },
            title = { Text("保存预设") },
            text = {
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text("预设名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (presetNameInput.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            RuleManager.savePreset(context, presetNameInput, rules.toList())
                            presetNames = RuleManager.getPresetNames(context)
                            withContext(Dispatchers.Main) {
                                currentPresetName = presetNameInput
                                Toast.makeText(context, "预设已保存", Toast.LENGTH_SHORT).show()
                            }
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

    // Preset Delete Confirmation Dialog
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
                        RuleManager.deletePreset(context, pendingDeletePreset!!)
                        presetNames = RuleManager.getPresetNames(context)
                        if (currentPresetName == pendingDeletePreset) {
                            currentPresetName = "当前配置"
                        }
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
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "文本规则",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "配置文本替换规则",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preset Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "预设",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = {
                        presetNameInput = ""
                        showPresetNameDialog = true
                    }) {
                        Text("另存为预设")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                val allPresets = listOf("当前配置") + presetNames
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allPresets.forEach { presetName ->
                        Box(
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    if (presetName == "当前配置") {
                                        scope.launch(Dispatchers.IO) {
                                            val loaded = RuleManager.load(context)
                                            withContext(Dispatchers.Main) {
                                                rules.clear()
                                                rules.addAll(loaded)
                                                currentPresetName = presetName
                                            }
                                        }
                                    } else {
                                        scope.launch(Dispatchers.IO) {
                                            val loaded = RuleManager.loadPreset(context, presetName)
                                            if (loaded != null) {
                                                withContext(Dispatchers.Main) {
                                                    rules.clear()
                                                    rules.addAll(loaded)
                                                    currentPresetName = presetName
                                                    Toast.makeText(context, "已应用预设「$presetName」", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                onLongClick = if (presetName != "当前配置") {
                                    {
                                        pendingDeletePreset = presetName
                                        showDeletePresetDialog = true
                                    }
                                } else null
                            )
                        ) {
                            FilterChip(
                                selected = currentPresetName == presetName,
                                onClick = {},
                                label = { Text(presetName) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rules List
        if (rules.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无规则，点击下方按钮添加",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(rules) { index, rule ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rule.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Switch(
                                    checked = rule.enabled,
                                    onCheckedChange = { enabled ->
                                        rules[index] = rule.copy(enabled = enabled)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (rule.isRegex) "[正则]" else "[文本]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = rule.pattern,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "→",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = rule.replacement,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    editingIndex = index
                                    editName = rule.name
                                    editPattern = rule.pattern
                                    editReplacement = rule.replacement
                                    editIsRegex = rule.isRegex
                                    showEditDialog = true
                                }) {
                                    Text("编辑")
                                }
                                TextButton(onClick = {
                                    deleteIndex = index
                                    showDeleteDialog = true
                                }) {
                                    Text("删除")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TonalButton(
                onClick = {
                    editingIndex = -1
                    editName = ""
                    editPattern = ""
                    editReplacement = ""
                    editIsRegex = false
                    showEditDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("添加规则")
            }
            OutlinedButton(
                onClick = { showImportDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("导入规则")
            }
            OutlinedButton(
                onClick = { showExportDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("导出规则")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    RuleManager.save(context, rules.toList())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存规则")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
