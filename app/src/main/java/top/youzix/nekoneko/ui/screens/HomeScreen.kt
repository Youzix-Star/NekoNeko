package top.youzix.nekoneko.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.youzix.nekoneko.*
import top.youzix.nekoneko.ui.screens.RingChart
import top.youzix.nekoneko.ui.theme.ComposeThemeManager
import top.youzix.nekoneko.ui.theme.SectionLabel
import top.youzix.nekoneko.ui.theme.CardSection
import top.youzix.nekoneko.ui.theme.FeatureRow
import top.youzix.nekoneko.ui.theme.DividerRow

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    var accessibilityEnabled by remember { mutableStateOf(checkAccessibilityEnabled(context)) }
    var floatingRunning by remember { mutableStateOf(checkServiceRunning(context, FloatingWindowService::class.java)) }
    var darkModeLabel by remember { mutableStateOf(getDarkModeLabel(context)) }
    var colorThemeLabel by remember { mutableStateOf(ComposeThemeManager.getThemeName(context, ComposeThemeManager.currentThemeId.collectAsState().value)) }

    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showColorThemeDialog by remember { mutableStateOf(false) }

    // Refresh on resume-like recomposition
    LaunchedEffect(Unit) {
        accessibilityEnabled = checkAccessibilityEnabled(context)
        floatingRunning = checkServiceRunning(context, FloatingWindowService::class.java)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
        )
        Text(
            text = stringResource(R.string.app_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 16.dp),
        )

        // ====== Service Card ======
        SectionLabel(stringResource(R.string.section_functions))
        CardSection {
            // Accessibility
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(R.drawable.ic_accessibility), null,
                    modifier = Modifier.size(40.dp).padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(R.string.row_accessibility), style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (accessibilityEnabled) stringResource(R.string.acc_on) else stringResource(R.string.acc_off),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    painterResource(R.drawable.ic_chevron_right), null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DividerRow()
            // Floating window
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        toggleFloatingWindow(context) {
                            floatingRunning = checkServiceRunning(context, FloatingWindowService::class.java)
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(R.drawable.ic_picture_in_picture), null,
                    modifier = Modifier.size(40.dp).padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(R.string.row_floating), style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (floatingRunning) stringResource(R.string.floating_running) else stringResource(R.string.floating_stopped),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    painterResource(R.drawable.ic_chevron_right), null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ====== Appearance Card ======
        SectionLabel(stringResource(R.string.color_theme_title), Modifier.padding(top = 16.dp))
        CardSection {
            // Color theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showColorThemeDialog = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(R.drawable.ic_auto_fix), null,
                    modifier = Modifier.size(40.dp).padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(R.string.color_theme_title), style = MaterialTheme.typography.titleSmall)
                    Text(
                        colorThemeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    painterResource(R.drawable.ic_chevron_right), null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DividerRow()
            // Dark mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDarkModeDialog = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(R.drawable.ic_dark_mode), null,
                    modifier = Modifier.size(40.dp).padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(R.string.dark_mode_title), style = MaterialTheme.typography.titleSmall)
                    Text(
                        darkModeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    painterResource(R.drawable.ic_chevron_right), null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ====== Token Stats Card ======
        Spacer(Modifier.height(12.dp))
        TokenStatsCard()
    }

    // Dialogs
    if (showDarkModeDialog) {
        DarkModeDialog(
            currentMode = DarkModePrefs.getMode(context),
            onDismiss = { showDarkModeDialog = false },
            onSelect = { mode ->
                DarkModePrefs.save(context, mode)
                darkModeLabel = getDarkModeLabel(context)
                showDarkModeDialog = false
            },
        )
    }
    if (showColorThemeDialog) {
        ColorThemeDialog(
            currentTheme = ComposeThemeManager.currentThemeId.collectAsState().value,
            onDismiss = { showColorThemeDialog = false },
            onSelect = { themeId ->
                ComposeThemeManager.setTheme(context, themeId)
                colorThemeLabel = ComposeThemeManager.getThemeName(context, themeId)
                showColorThemeDialog = false
                // Recreate activity to apply new theme
                (context as? Activity)?.recreate()
            },
        )
    }
}

// ==================== Token Stats Card ====================
@Composable
private fun TokenStatsCard() {
    val context = LocalContext.current
    var selectedModel by remember { mutableStateOf<String?>(null) }
    // Force recomposition on each resume by tracking a tick
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(tick) {
        // This runs on composition, simulating onResume
    }

    val now = System.currentTimeMillis()
    val weekAgo = now - 7L * 24 * 60 * 60 * 1000
    val monthAgo = now - 30L * 24 * 60 * 60 * 1000

    val allStats = remember(tick, selectedModel) { TokenStats.query(context, 0, selectedModel) }
    val weekStats = remember(tick, selectedModel) { TokenStats.query(context, weekAgo, selectedModel) }
    val monthStats = remember(tick, selectedModel) { TokenStats.query(context, monthAgo, selectedModel) }
    val models = remember(tick) { TokenStats.getModelNames(context) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(R.string.token_stats_title), style = MaterialTheme.typography.titleMedium)

            // Model chips
            if (models.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FilterChip(
                        selected = selectedModel == null,
                        onClick = { selectedModel = null; tick++ },
                        label = { Text("全部") },
                    )
                    models.take(5).forEach { model ->
                        FilterChip(
                            selected = selectedModel == model,
                            onClick = { selectedModel = model; tick++ },
                            label = { Text(model, maxLines = 1) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ring chart + center number
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(160.dp),
            ) {
                RingChart(
                    promptTokens = allStats.totalPromptTokens,
                    completionTokens = allStats.totalCompletionTokens,
                    cachedTokens = allStats.cachedTokens,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatTokenCount(allStats.totalTokens),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        stringResource(R.string.token_stats_total),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.token_legend_input))
                Spacer(Modifier.width(16.dp))
                LegendItem(color = MaterialTheme.colorScheme.tertiary, label = stringResource(R.string.token_legend_output))
                Spacer(Modifier.width(16.dp))
                LegendItem(color = MaterialTheme.colorScheme.secondaryContainer, label = stringResource(R.string.token_legend_cached))
            }

            Spacer(Modifier.height(16.dp))

            // Three columns
            Row(modifier = Modifier.fillMaxWidth()) {
                StatColumn(Modifier.weight(1f), formatTokenCount(weekStats.totalTokens), stringResource(R.string.token_stats_week), MaterialTheme.colorScheme.primary)
                StatColumn(Modifier.weight(1f), formatTokenCount(monthStats.totalTokens), stringResource(R.string.token_stats_month), MaterialTheme.colorScheme.onSurface)
                StatColumn(Modifier.weight(1f), if (allStats.totalCalls > 0) "${allStats.cacheHitPercent()}%" else "--", stringResource(R.string.token_stats_cache_rate), MaterialTheme.colorScheme.tertiary)
            }

            // Call info
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(
                    R.string.token_stats_call_fmt,
                    allStats.totalCalls,
                    formatTokenCount(allStats.totalPromptTokens),
                    formatTokenCount(allStats.totalCompletionTokens),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color = color) }
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatColumn(modifier: Modifier, value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ==================== Dialogs ====================
@Composable
private fun DarkModeDialog(currentMode: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val options = listOf(
        stringResource(R.string.dark_mode_follow_system),
        stringResource(R.string.dark_mode_force_light),
        stringResource(R.string.dark_mode_force_dark),
    )
    val modes = listOf(
        DarkModePrefs.MODE_FOLLOW_SYSTEM,
        DarkModePrefs.MODE_FORCE_LIGHT,
        DarkModePrefs.MODE_FORCE_DARK,
    )
    val selectedIndex = modes.indexOf(currentMode).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dark_mode_title)) },
        text = {
            Column {
                options.forEachIndexed { index, label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(modes[index]) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = index == selectedIndex, onClick = { onSelect(modes[index]) })
                        Spacer(Modifier.width(12.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun ColorThemeDialog(currentTheme: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val context = LocalContext.current
    val options = listOf(
        ComposeThemeManager.getThemeName(context, ComposeThemeManager.THEME_GR_GREEN),
        ComposeThemeManager.getThemeName(context, ComposeThemeManager.THEME_EMBER),
        ComposeThemeManager.getThemeName(context, ComposeThemeManager.THEME_GLACIER),
    )
    val themeIds = listOf(
        ComposeThemeManager.THEME_GR_GREEN,
        ComposeThemeManager.THEME_EMBER,
        ComposeThemeManager.THEME_GLACIER,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_theme_title)) },
        text = {
            Column {
                options.forEachIndexed { index, label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(themeIds[index]) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = themeIds[index] == currentTheme, onClick = { onSelect(themeIds[index]) })
                        Spacer(Modifier.width(12.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
    )
}

// ==================== Helpers ====================
private fun checkAccessibilityEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    return enabled != null && enabled.contains(context.packageName)
}

private fun checkServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
    return am.getRunningServices(200).any { serviceClass.name == it.service.className }
}

private fun getDarkModeLabel(context: Context): String {
    return when (DarkModePrefs.getMode(context)) {
        DarkModePrefs.MODE_FORCE_LIGHT -> context.getString(R.string.dark_mode_force_light)
        DarkModePrefs.MODE_FORCE_DARK -> context.getString(R.string.dark_mode_force_dark)
        else -> context.getString(R.string.dark_mode_follow_system)
    }
}

private fun toggleFloatingWindow(context: Context, onResult: () -> Unit) {
    if (checkServiceRunning(context, FloatingWindowService::class.java)) {
        context.stopService(Intent(context, FloatingWindowService::class.java))
        Toast.makeText(context, R.string.floating_stopped, Toast.LENGTH_SHORT).show()
        onResult()
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
        (context as? android.app.Activity)?.startActivityForResult(intent, 1234)
    } else {
        context.startService(Intent(context, FloatingWindowService::class.java))
        Toast.makeText(context, R.string.floating_window_started, Toast.LENGTH_SHORT).show()
        onResult()
    }
}

private fun formatTokenCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

private typealias Activity = android.app.Activity
