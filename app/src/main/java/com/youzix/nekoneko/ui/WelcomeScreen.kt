package com.youzix.nekoneko.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.youzix.nekoneko.R

/** 首次启动引导：隐私与许可 → 无障碍服务 → 悬浮窗权限 → 主题（4 步）。 */
@Composable
fun WelcomeScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(0) }
    val totalPages = 4

    val overlayLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    Column(modifier = Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { (page + 1) / totalPages.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when (page) {
                0 -> WelcomePage(
                    icon = Icons.Filled.Lock,
                    title = stringResource(R.string.welcome_privacy_title),
                    body = stringResource(R.string.welcome_privacy_body),
                    footnote = stringResource(R.string.welcome_privacy_license),
                )

                1 -> WelcomePage(
                    icon = Icons.Filled.AccessibilityNew,
                    title = stringResource(R.string.welcome_accessibility_title),
                    body = stringResource(R.string.welcome_accessibility_body),
                    actionLabel = stringResource(R.string.welcome_accessibility_action),
                    onAction = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                )

                2 -> WelcomePage(
                    icon = Icons.Filled.PictureInPicture,
                    title = stringResource(R.string.welcome_overlay_title),
                    body = stringResource(R.string.welcome_overlay_body),
                    actionLabel = stringResource(R.string.welcome_overlay_action),
                    onAction = {
                        overlayLauncher.launch(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.packageName)
                            )
                        )
                    },
                )

                else -> WelcomePage(
                    icon = Icons.Filled.Palette,
                    title = stringResource(R.string.welcome_theme_title),
                    body = stringResource(R.string.welcome_theme_body),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { if (page > 0) page-- },
                enabled = page > 0,
            ) {
                Text(stringResource(R.string.welcome_prev))
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    when (page) {
                        totalPages - 1 -> onFinish()
                        else -> page++
                    }
                },
            ) {
                Text(
                    text = stringResource(
                        when (page) {
                            0 -> R.string.welcome_agree
                            totalPages - 1 -> R.string.welcome_finish
                            else -> R.string.welcome_next
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun WelcomePage(
    icon: ImageVector,
    title: String,
    body: String,
    footnote: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (footnote != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = footnote,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
