package com.youzix.nekoneko.ui

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import com.google.android.material.color.DynamicColors
import top.yukonga.miuix.kmp.basic.ArrowPreference
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.theme.MiuixTheme
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
    val primaryHex = remember { String.format("#%06X", 0xFFFFFF and MiuixTheme.colorScheme.primary.toArgb()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))

        // 应用图标（Miuix 主题色圆形底）
        Icon(
            imageVector = MiuixIcons.Copy,
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(88.dp)
                .background(MiuixTheme.colorScheme.primaryContainer, CircleShape)
                .padding(20.dp),
            tint = MiuixTheme.colorScheme.onPrimaryContainer,
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MiuixTheme.textStyles.title1,
            color = MiuixTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.about_version_fmt, versionName),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = stringResource(
                R.string.about_device_info_fmt,
                Build.VERSION.SDK_INT,
                if (dynamicAvailable) "可用" else "不可用",
                if (wallpaperAvailable) "有" else "无",
                primaryHex,
            ),
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Spacer(Modifier.height(24.dp))

        // 功能特性
        ArrowPreference(
            title = stringResource(R.string.about_features),
            summary = stringResource(R.string.about_feature_capture),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Copy,
                    contentDescription = stringResource(R.string.about_features),
                )
            },
            onClick = null,
        )

        // 技术栈
        ArrowPreference(
            title = stringResource(R.string.about_tech),
            summary = stringResource(R.string.about_tech_content),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Layers,
                    contentDescription = stringResource(R.string.about_tech),
                )
            },
            onClick = null,
        )

        // GitHub 仓库
        ArrowPreference(
            title = stringResource(R.string.about_github),
            summary = stringResource(R.string.about_github_sub),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Link,
                    contentDescription = stringResource(R.string.about_github),
                )
            },
            onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Youzix-Star/NekoNeko")
                    )
                )
            },
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.about_license),
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
        Spacer(Modifier.height(24.dp))
    }
}
