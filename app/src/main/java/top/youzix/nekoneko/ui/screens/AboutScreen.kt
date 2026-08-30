package top.youzix.nekoneko.ui.screens

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.youzix.nekoneko.R
import top.youzix.nekoneko.Guide
import top.youzix.nekoneko.UpdateChecker
import top.youzix.nekoneko.WelcomeActivity

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val pm = context.packageManager

    val versionText = remember {
        try {
            val pi = pm.getPackageInfo(context.packageName, 0)
            "${pi.versionName}(${pi.versionCode})"
        } catch (_: Exception) { "v1.0" }
    }
    val deviceInfo = remember {
        val abi = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "unknown"
        "Android ${Build.VERSION.RELEASE} · $abi · ${Build.MANUFACTURER} ${Build.MODEL}"
    }

    var updateStatus by remember { mutableStateOf("") }
    var updateSub by remember { mutableStateOf("") }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateDialogText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text("关于", style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
        Text("NekoNeko 是一个轻量的文本捕获悬浮窗工具",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

        // 版本头部
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(R.drawable.ic_launcher), null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text("NekoNeko", style = MaterialTheme.typography.headlineSmall)
            Text(versionText, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.alpha(0.7f))
            Text(deviceInfo, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.alpha(0.7f))
        }

        // 功能特性
        SectionLabel(stringResource(R.string.about_features))
        CardSection {
            FeatureRow(R.drawable.ic_content_copy, stringResource(R.string.about_features), stringResource(R.string.about_feature_capture))
            DividerRow()
            FeatureRow(R.drawable.ic_auto_fix, stringResource(R.string.about_tech), stringResource(R.string.about_tech_content))
            DividerRow()
            FeatureRow(R.drawable.ic_tutorial, stringResource(R.string.about_guide), stringResource(R.string.about_guide_sub)) {
                Guide.reset(context)
                context.startActivity(Intent(context, WelcomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
            }
        }

        // 操作卡片
        SectionLabel(stringResource(R.string.about_github))
        CardSection {
            FeatureRow(R.drawable.ic_coffee, stringResource(R.string.about_sponsor), stringResource(R.string.about_sponsor_sub), trailing = true) {
                android.app.AlertDialog.Builder(context)
                    .setTitle(R.string.about_sponsor).setMessage(R.string.about_sponsor_message)
                    .setPositiveButton(android.R.string.ok, null).show()
            }
            DividerRow()
            FeatureRow(R.drawable.ic_open_in_new, stringResource(R.string.about_github), stringResource(R.string.about_github_sub), trailing = true) {
                context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/Youzix-Star/NekoNeko")))
            }
            DividerRow()

            // 检测更新
            Row(modifier = Modifier.fillMaxWidth().clickable {
                updateStatus = "正在检查更新…"
                updateSub = stringResource(R.string.about_check_update_sub)
                UpdateChecker.checkForUpdate(context, object : UpdateChecker.Callback {
                    override fun onUpdateAvailable(latestVersion: String, body: String?, apkUrl: String?) {
                        updateStatus = "发现新版本 v$latestVersion"
                        updateSub = stringResource(R.string.about_update_available_sub)
                        updateDialogText = body?.trim()?.ifEmpty { updateStatus } ?: updateStatus
                        showUpdateDialog = true
                    }
                    override fun onNoUpdate() {
                        updateStatus = stringResource(R.string.about_update_latest)
                        try {
                            val ver = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                            updateSub = "当前版本 v$ver"
                        } catch (_: Exception) { updateSub = stringResource(R.string.about_check_update_sub) }
                    }
                    override fun onError(message: String?) {
                        updateStatus = stringResource(R.string.about_check_update)
                        updateSub = "检查失败：${message ?: "未知错误"}"
                    }
                })
            }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_info), null,
                    modifier = Modifier.size(40.dp).padding(8.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(updateStatus.ifEmpty { stringResource(R.string.about_check_update) }, style = MaterialTheme.typography.titleSmall)
                    Text(updateSub.ifEmpty { stringResource(R.string.about_check_update_sub) }, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.alpha(0.7f))
                }
                Icon(painterResource(R.drawable.ic_chevron_right), null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Text(stringResource(R.string.about_license), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.alpha(0.5f),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).wrapContentWidth(Alignment.CenterHorizontally))
        Spacer(Modifier.height(16.dp))
    }

    if (showUpdateDialog) {
        AlertDialog(onDismissRequest = { showUpdateDialog = false }, title = { Text("发现新版本") },
            text = { Text(updateDialogText) },
            confirmButton = { TextButton(onClick = { showUpdateDialog = false; UpdateChecker.openReleasePage(context) }) { Text(stringResource(R.string.about_update_dialog_go)) } },
            dismissButton = { TextButton(onClick = { showUpdateDialog = false }) { Text(stringResource(android.R.string.cancel)) } })
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
}

@Composable
fun CardSection(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp)) { Column(content = content) }
}

@Composable
fun FeatureRow(iconRes: Int, title: String, subtitle: String, trailing: Boolean = false, onClick: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(iconRes), null, modifier = Modifier.size(40.dp).padding(8.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.alpha(0.7f))
        }
        if (trailing) Icon(painterResource(R.drawable.ic_chevron_right), null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DividerRow() {
    Divider(modifier = Modifier.padding(start = 58.dp), color = MaterialTheme.colorScheme.outline.alpha(0.15f), thickness = 1.dp)
}
