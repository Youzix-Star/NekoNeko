package com.youzix.nekoneko.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Settings
import com.youzix.nekoneko.R

/** 主界面：Miuix 底部导航 + 三个页面（首页 / AI 配置 / 关于）。 */
@Composable
fun MainApp() {
    var tab by remember { mutableIntStateOf(0) }
    val homeLabel = stringResource(R.string.tab_home)
    val configLabel = stringResource(R.string.tab_config)
    val aboutLabel = stringResource(R.string.tab_about)

    Scaffold(
        bottomBar = {
            NavigationBar(showDivider = false) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = MiuixIcons.Home,
                    label = homeLabel,
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = MiuixIcons.Settings,
                    label = configLabel,
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = MiuixIcons.Info,
                    label = aboutLabel,
                )
            }
        }
    ) { padding: PaddingValues ->
        when (tab) {
            0 -> HomeScreen(padding = padding, onOpenAiConfig = { tab = 1 })
            1 -> AiConfigScreen(padding = padding)
            else -> AboutScreen(padding = padding)
        }
    }
}
