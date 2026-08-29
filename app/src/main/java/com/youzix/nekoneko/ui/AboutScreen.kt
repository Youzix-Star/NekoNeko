package com.youzix.nekoneko.ui

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.color.DynamicColors
import com.youzix.nekoneko.R

/** 关于页：应用信息、功能特性、技术栈与 GitHub 链接，附带设备/主题色诊断。 */
@Composable
fun AboutScreen(padding: PaddingValues) {
    val context = LocalContext.current

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (e: PackageManager.NameNotFoundException) {
            "?"
        }
    }

    val dynamicAvailable = remember { DynamicColors.isDynamicColorAvailable() }
    val wallpaperAvailable = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                WallpaperManager.getInstance(context)
                    .getWallpaperColors(WallpaperManager.FLAG_SYSTEM) != null
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryHex = remember(primaryColor) { String.format("#%06X", 0xFFFFFF and primaryColor.toArgb()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))

        // 应用图标（主题色圆形底）
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.about_version_fmt, versionName),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(
                R.string.about_device_info_fmt,
                Build.VERSION.SDK_INT,
                if (dynamicAvailable) "可用" else "不可用",
                if (wallpaperAvailable) "有" else "无",
                primaryHex,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
        ) {
            SettingsRow(
                title = stringResource(R.string.about_features),
                summary = stringResource(R.string.about_feature_capture),
                icon = Icons.Filled.ContentCopy,
            )
            SettingsRow(
                title = stringResource(R.string.about_tech),
                summary = stringResource(R.string.about_tech_content),
                icon = Icons.Filled.Code,
            )
            SettingsRow(
                title = stringResource(R.string.about_github),
                summary = stringResource(R.string.about_github_sub),
                icon = Icons.Filled.Link,
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/Youzix-Star/NekoNeko")
                        )
                    )
                },
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.about_license),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
    }
}
