package top.youzix.nekoneko.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-level motion scheme accessor.
 * Now delegates to Material 3 Expressive's built-in MotionScheme.
 */
val LocalMotionScheme = staticCompositionLocalOf { MotionScheme.expressive() }

/** Convenience accessor. */
object NekoMotion {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val scheme: MotionScheme
        @Composable
        get() = LocalMotionScheme.current
}
