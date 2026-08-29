package com.youzix.nekoneko.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ArrowPreference
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Pin
import top.yukonga.miuix.kmp.icon.extended.ScreenCapture
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.youzix.nekoneko.R

/** 首页：MIUI 风格设置列表（无障碍服务 / 悬浮窗 / AI 配置）。 */
@Composable
fun HomeScreen(padding: PaddingValues, onOpenAiConfig: () -> Unit) {
    val context = LocalContext.current
    var accessibilityOn by remember { mutableStateOf(isAccessibilityEnabled(context)) }
    var floatingOn by remember { mutableStateOf(isServiceRunning(context)) }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 从悬浮窗权限页返回：授予则启动服务
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
            context.startService(Intent(context, FloatingWindowService::class.java))
            Toast.makeText(context, R.string.floating_window_started, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, R.string.floating_window_permission_needed, Toast.LENGTH_SHORT).show()
        }
        floatingOn = isServiceRunning(context)
        accessibilityOn = isAccessibilityEnabled(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MiuixTheme.textStyles.title1,
            color = MiuixTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
        Text(
            text = stringResource(R.string.app_subtitle),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
        Spacer(Modifier.height(16.dp))

        SmallTitle(text = stringResource(R.string.section_functions))

        // 无障碍服务
        ArrowPreference(
            title = stringResource(R.string.row_accessibility),
            summary = stringResource(
                if (accessibilityOn) R.string.acc_on else R.string.acc_off
            ),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.ScreenCapture,
                    contentDescription = stringResource(R.string.row_accessibility),
                )
            },
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
        )

        // 悬浮窗（一键启停）
        ArrowPreference(
            title = stringResource(R.string.row_floating),
            summary = stringResource(
                if (floatingOn) R.string.floating_running else R.string.floating_stopped
            ),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Pin,
                    contentDescription = stringResource(R.string.row_floating),
                )
            },
            onClick = {
                if (isServiceRunning(context)) {
                    context.stopService(Intent(context, FloatingWindowService::class.java))
                    Toast.makeText(context, R.string.floating_stopped, Toast.LENGTH_SHORT).show()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !Settings.canDrawOverlays(context)
                ) {
                    overlayPermissionLauncher.launch(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.packageName)
                        )
                    )
                } else {
                    context.startService(Intent(context, FloatingWindowService::class.java))
                    Toast.makeText(context, R.string.floating_window_started, Toast.LENGTH_SHORT).show()
                }
                floatingOn = isServiceRunning(context)
            },
        )

        // AI 配置
        ArrowPreference(
            title = stringResource(R.string.row_ai),
            summary = stringResource(R.string.row_ai_sub),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Tune,
                    contentDescription = stringResource(R.string.row_ai),
                )
            },
            onClick = onOpenAiConfig,
        )
    }
}

private fun isAccessibilityEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabled != null && enabled.contains(context.packageName)
}

private fun isServiceRunning(context: Context): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
    for (info in am.getRunningServices(200)) {
        if (FloatingWindowService::class.java.name == info.service.className) {
            return true
        }
    }
    return false
}
