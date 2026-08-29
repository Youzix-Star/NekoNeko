package com.youzix.nekoneko.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.youzix.nekoneko.R

/** 主界面：Material You 底部导航 + 三个页面（首页 / AI 配置 / 关于）。 */
@Composable
fun MainApp() {
    var tab by remember { mutableIntStateOf(0) }
    val homeLabel = stringResource(R.string.tab_home)
    val configLabel = stringResource(R.string.tab_config)
    val aboutLabel = stringResource(R.string.tab_about)

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = homeLabel) },
                    label = { Text(homeLabel) },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = configLabel) },
                    label = { Text(configLabel) },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Info, contentDescription = aboutLabel) },
                    label = { Text(aboutLabel) },
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
