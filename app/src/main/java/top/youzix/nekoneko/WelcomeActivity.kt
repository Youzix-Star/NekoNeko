package top.youzix.nekoneko

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import top.youzix.nekoneko.ui.screens.WelcomeScreen
import top.youzix.nekoneko.ui.theme.NekoNekoTheme

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        DarkModePrefs.apply(this)
        super.onCreate(savedInstanceState)

        ThemeUtils.applyDynamicColors(this, theme)
        ColorThemeManager.applyTheme(this)

        // Skip if already completed
        if (Guide.isDone(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            NekoNekoTheme {
                WelcomeScreen(onFinish = {
                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                    finish()
                })
            }
        }
    }

    @Deprecated("Use OnBackPressedCallback")
    override fun onBackPressed() {
        super.onBackPressed()
    }
}
