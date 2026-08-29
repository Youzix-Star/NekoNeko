package com.youzix.nekoneko.ui

import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.youzix.nekoneko.AccessibilityService
import com.youzix.nekoneko.AiManager
import com.youzix.nekoneko.Logger
import com.youzix.nekoneko.R

/** 悬浮窗内容（Compose）：可拖动，支持最小化、捕获文本、替换/增加、AI 修改与运行日志。 */
@Composable
fun FloatingWindowContent(
    onClose: () -> Unit,
    windowManager: WindowManager,
    view: View,
    params: WindowManager.LayoutParams,
) {
    val context = LocalContext.current
    var minimized by remember { mutableStateOf(false) }
    var capturedText by remember { mutableStateOf(context.getString(R.string.waiting_for_text)) }
    var logText by remember { mutableStateOf(Logger.getLogEntriesAsString()) }
    var aiBusy by remember { mutableStateOf(false) }

    // 日志轮询刷新
    LaunchedEffect(Unit) {
        while (true) {
            logText = Logger.getLogEntriesAsString()
            delay(500)
        }
    }

    var dragStartX by remember { mutableStateOf(0f) }
    var dragStartY by remember { mutableStateOf(0f) }

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                        dragStartY = offset.y
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        params.x = (params.x + (change.position.x - dragStartX)).toInt()
                        params.y = (params.y + (change.position.y - dragStartY)).toInt()
                        windowManager.updateViewLayout(view, params)
                    }
                )
            },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.floating_window_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { minimized = !minimized }) {
                    Icon(
                        imageVector = if (minimized) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                        contentDescription = stringResource(
                            if (minimized) R.string.restore_floating_window else R.string.minimize_floating_window
                        ),
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_floating_window),
                    )
                }
            }

            AnimatedVisibility(
                visible = !minimized,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = stringResource(R.string.capture_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // 捕获的文本显示区域
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                    ) {
                        Text(
                            text = capturedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    // 捕获按钮（主操作）
                    Button(
                        onClick = {
                            val service = AccessibilityService.getInstance()
                            if (service == null) {
                                Toast.makeText(context, R.string.please_enable_accessibility, Toast.LENGTH_SHORT).show()
                            } else {
                                val text = service.getCurrentWindowText()
                                if (text.isEmpty()) {
                                    Toast.makeText(context, R.string.no_text_found, Toast.LENGTH_SHORT).show()
                                } else {
                                    capturedText = text
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.capture_text))
                    }

                    // 替换 / 增加（次级：Tonal 风格）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                val service = AccessibilityService.getInstance()
                                if (service == null) {
                                    Toast.makeText(context, R.string.please_enable_accessibility, Toast.LENGTH_SHORT).show()
                                } else if (service.replaceInputText("test")) {
                                    Toast.makeText(context, R.string.replace_success, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, R.string.no_text_found, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.replace_text))
                        }
                        Button(
                            onClick = {
                                val service = AccessibilityService.getInstance()
                                if (service == null) {
                                    Toast.makeText(context, R.string.please_enable_accessibility, Toast.LENGTH_SHORT).show()
                                } else if (service.appendInputText("test")) {
                                    Toast.makeText(context, R.string.append_success, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, R.string.no_text_found, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.append_text))
                        }
                    }

                    // AI 修改（Outlined）
                    OutlinedButton(
                        onClick = {
                            if (aiBusy) return@OutlinedButton
                            val service = AccessibilityService.getInstance()
                            var text = capturedText
                            if (text.isEmpty() || text == context.getString(R.string.waiting_for_text)) {
                                text = if (service != null) service.getCurrentWindowText() else ""
                            }
                            if (text.isEmpty()) {
                                Toast.makeText(context, R.string.no_text_found, Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val cfg = AiManager.load(context)
                            if (cfg.apiKey.isNullOrBlank()) {
                                Toast.makeText(context, R.string.ai_key_missing, Toast.LENGTH_LONG).show()
                                return@OutlinedButton
                            }
                            aiBusy = true
                            capturedText = context.getString(R.string.ai_modifying)
                            val originalText = text
                            AiManager.modifyText(cfg, originalText, object : AiManager.Callback {
                                override fun onSuccess(modifiedText: String) {
                                    capturedText = modifiedText
                                    val s = AccessibilityService.getInstance()
                                    val replaced = s != null && s.replaceInputText(modifiedText)
                                    Toast.makeText(
                                        context,
                                        if (replaced) R.string.ai_replace_success else R.string.ai_no_input,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    aiBusy = false
                                }

                                override fun onError(message: String) {
                                    capturedText = originalText
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.ai_call_failed, message),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    aiBusy = false
                                }
                            })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.ai_modify))
                    }

                    // 日志
                    Text(
                        text = stringResource(R.string.running_log),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(top = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp),
                    ) {
                        Text(
                            text = logText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                        )
                    }

                    // 状态提示
                    Text(
                        text = stringResource(R.string.status_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }
        }
    }
}
