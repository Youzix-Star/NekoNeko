package com.youzix.nekoneko.ui.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * 应用主题：Miuix（MIUI 风格）。
 * - Android 12+：MonetSystem —— 跟随系统莫奈动态取色（含 Android 13+ 的系统调色板样式）
 * - Android 11 及以下：System —— 回退 Miuix 默认 MIUI 蓝色系（浅/深色随系统）
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val controller = remember {
        ThemeController(
            colorSchemeMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ColorSchemeMode.MonetSystem
            } else {
                ColorSchemeMode.System
            }
        )
    }
    MiuixTheme(controller = controller) {
        content()
    }
}
