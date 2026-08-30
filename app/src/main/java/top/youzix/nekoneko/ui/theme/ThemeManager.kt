package top.youzix.nekoneko.ui.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Compose-compatible color theme manager.
 * Bridges the existing Java SharedPreferences-based theme selection
 * to a StateFlow that Compose can observe.
 */
object ComposeThemeManager {

    const val THEME_GR_GREEN = 0
    const val THEME_EMBER = 1
    const val THEME_GLACIER = 2

    private const val PREFS = "color_theme_prefs"
    private const val KEY_THEME = "theme_id"

    private val _currentThemeId = MutableStateFlow(THEME_GR_GREEN)
    val currentThemeId: StateFlow<Int> = _currentThemeId.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _currentThemeId.value = prefs.getInt(KEY_THEME, THEME_GR_GREEN)
    }

    fun setTheme(context: Context, themeId: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_THEME, themeId).apply()
        _currentThemeId.value = themeId
    }

    fun getThemeName(context: Context, themeId: Int): String {
        return when (themeId) {
            THEME_EMBER -> context.getString(R.string.color_theme_ember)
            THEME_GLACIER -> context.getString(R.string.color_theme_glacier)
            else -> context.getString(R.string.color_theme_green)
        }
    }

    /**
     * Get the appropriate NekoColorPalette for the current theme and dark mode.
     * For THEME_GR_GREEN, returns null to signal using dynamic/monet colors on Android 12+.
     */
    fun getColorPalette(themeId: Int, isDark: Boolean): NekoColorPalette? {
        return when (themeId) {
            THEME_EMBER -> if (isDark) EmberDark else EmberLight
            THEME_GLACIER -> if (isDark) GlacierDark else GlacierLight
            else -> null // GR Green uses dynamic color or fallback in NekoNekoTheme
        }
    }

    /** Get GR Green palette explicitly (for fallback when dynamic color is not available). */
    fun getGRGreenPalette(isDark: Boolean): NekoColorPalette {
        return if (isDark) GRGreenDark else GRGreenLight
    }
}
