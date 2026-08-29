package com.youzix.nekoneko.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.youzix.nekoneko.Guide
import com.youzix.nekoneko.ui.theme.AppTheme

/**
 * 唯一宿主 Activity（Compose）：
 * 首次启动显示引导页，完成后进入主界面（底部导航 + 三页面）。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                var guideDone by remember { mutableStateOf(Guide.isDone(this@MainActivity)) }
                if (guideDone) {
                    MainApp()
                } else {
                    WelcomeScreen(
                        onFinish = {
                            Guide.markDone(this@MainActivity)
                            guideDone = true
                        }
                    )
                }
            }
        }
    }
}
