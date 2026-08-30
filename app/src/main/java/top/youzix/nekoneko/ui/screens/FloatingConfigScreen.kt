package top.youzix.nekoneko.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.youzix.nekoneko.FloatingWindowPrefs
import top.youzix.nekoneko.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingConfigScreen() {
    val context = LocalContext.current
    
    // State variables
    var showCaptureText by remember { mutableStateOf(false) }
    var showApplyRules by remember { mutableStateOf(false) }
    var showAiModify by remember { mutableStateOf(false) }
    var showLog by remember { mutableStateOf(false) }
    var showQuickBall by remember { mutableStateOf(false) }
    var ballContentType by remember { mutableStateOf("图标") }
    var ballText by remember { mutableStateOf("") }
    var ballAction by remember { mutableStateOf("应用规则") }
    var ballSizeDp by remember { mutableFloatStateOf(48f) }
    var ballCornerDp by remember { mutableFloatStateOf(24f) }
    var ballIconRes by remember { mutableIntStateOf(R.drawable.ic_ball_auto_fix) }
    var showIconPicker by remember { mutableStateOf(false) }
    
    val ballIconResArray = intArrayOf(
        R.drawable.ic_ball_auto_fix,
        R.drawable.ic_ball_content_copy,
        R.drawable.ic_ball_swap_horiz,
        R.drawable.ic_ball_rule,
        R.drawable.ic_ball_description,
        R.drawable.ic_ball_dark_mode,
        R.drawable.ic_ball_info,
        R.drawable.ic_ball_settings,
        R.drawable.ic_ball_home
    )
    
    // Load preferences
    LaunchedEffect(Unit) {
        val prefs = FloatingWindowPrefs.load(context)
        showCaptureText = prefs.showCaptureText
        showApplyRules = prefs.showApplyRules
        showAiModify = prefs.showAiModify
        showLog = prefs.showLog
        showQuickBall = prefs.showQuickBall
        ballContentType = prefs.ballContentType
        ballText = prefs.ballText
        ballAction = prefs.ballAction
        ballSizeDp = prefs.ballSizeDp
        ballCornerDp = prefs.ballCornerDp
        ballIconRes = prefs.ballIconRes
    }
    
    // Save preferences function
    fun savePrefs() {
        val p = FloatingWindowPrefs.Prefs()
        p.showCaptureText = showCaptureText
        p.showApplyRules = showApplyRules
        p.showAiModify = showAiModify
        p.showLog = showLog
        p.showQuickBall = showQuickBall
        p.ballContentType = ballContentType
        p.ballText = ballText
        p.ballAction = ballAction
        p.ballSizeDp = ballSizeDp
        p.ballCornerDp = ballCornerDp
        p.ballIconRes = ballIconRes
        FloatingWindowPrefs.save(context, p)
    }
    
    // Helper function to handle mutual exclusion
    fun handleQuickBallToggle(newValue: Boolean) {
        showQuickBall = newValue
        if (newValue) {
            showCaptureText = false
            showApplyRules = false
            showAiModify = false
            showLog = false
        }
        savePrefs()
    }
    
    fun handleWindowSwitchToggle(field: String, newValue: Boolean) {
        when (field) {
            "captureText" -> showCaptureText = newValue
            "applyRules" -> showApplyRules = newValue
            "aiModify" -> showAiModify = newValue
            "log" -> showLog = newValue
        }
        if (newValue) {
            showQuickBall = false
        }
        savePrefs()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "悬浮窗设置",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "配置悬浮窗和快捷悬浮球的显示内容",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Warning Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_dialog_alert),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "快捷悬浮球与显示内容为互斥模式，开启其中一个会自动关闭另一个",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Ball Section
        Text(
            text = "快捷悬浮球",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "显示快捷悬浮球",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "在屏幕上显示可拖动的快捷悬浮球",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = showQuickBall,
                onCheckedChange = { handleQuickBallToggle(it) }
            )
        }
        
        // Quick Ball Options (AnimatedVisibility)
        AnimatedVisibility(visible = showQuickBall) {
            Column(modifier = Modifier.padding(start = 8.dp, top = 12.dp)) {
                // Content Type Section
                Text(
                    text = "悬浮球内容",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = ballContentType == "图标",
                        onClick = {
                            ballContentType = "图标"
                            savePrefs()
                        },
                        label = { Text("图标") }
                    )
                    FilterChip(
                        selected = ballContentType == "文字",
                        onClick = {
                            ballContentType = "文字"
                            savePrefs()
                        },
                        label = { Text("文字") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ball Action Section
                Text(
                    text = "点击动作",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = ballAction == "应用规则",
                        onClick = {
                            ballAction = "应用规则"
                            savePrefs()
                        },
                        label = { Text("应用规则") }
                    )
                    FilterChip(
                        selected = ballAction == "套用 AI",
                        onClick = {
                            ballAction = "套用 AI"
                            savePrefs()
                        },
                        label = { Text("套用 AI") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ball Text Input (shown only when text mode)
                AnimatedVisibility(visible = ballContentType == "文字") {
                    OutlinedTextField(
                        value = ballText,
                        onValueChange = {
                            ballText = it
                            savePrefs()
                        },
                        label = { Text("悬浮球文字") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Icon Picker Button (shown when icon mode)
                AnimatedVisibility(visible = ballContentType == "图标") {
                    OutlinedButton(
                        onClick = { showIconPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(ballIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("选择图标")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Size Slider
                Text(
                    text = "悬浮球大小: ${ballSizeDp.toInt()}dp",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = ballSizeDp,
                    onValueChange = {
                        ballSizeDp = it
                        savePrefs()
                    },
                    valueRange = 32f..80f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Corner Slider
                Text(
                    text = "圆角半径: ${ballCornerDp.toInt()}dp",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = ballCornerDp,
                    onValueChange = {
                        ballCornerDp = it
                        savePrefs()
                    },
                    valueRange = 0f..40f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Display Content Section
        Text(
            text = "显示内容",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Switch Rows
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "显示捕获文本",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "在悬浮窗中显示捕获的屏幕文本",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = showCaptureText,
                onCheckedChange = { handleWindowSwitchToggle("captureText", it) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "显示应用规则",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "在悬浮窗中显示规则应用状态",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = showApplyRules,
                onCheckedChange = { handleWindowSwitchToggle("applyRules", it) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "显示 AI 修改",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "在悬浮窗中显示 AI 修改建议",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = showAiModify,
                onCheckedChange = { handleWindowSwitchToggle("aiModify", it) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "显示日志",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "在悬浮窗中显示操作日志",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = showLog,
                onCheckedChange = { handleWindowSwitchToggle("log", it) }
            )
        }
    }
    
    // Icon Picker Dialog
    if (showIconPicker) {
        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            title = { Text("选择图标") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ballIconResArray.toList()) { iconRes ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (ballIconRes == iconRes) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    ballIconRes = iconRes
                                    savePrefs()
                                    showIconPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = if (ballIconRes == iconRes) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}
