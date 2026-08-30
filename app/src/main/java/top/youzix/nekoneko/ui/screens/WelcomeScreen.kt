package top.youzix.nekoneko.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import top.youzix.nekoneko.Guide
import top.youzix.nekoneko.MainActivity
import top.youzix.nekoneko.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // Page data
    val pageData = listOf(
        PageData(
            titleRes = R.string.welcome_0_title,
            summaryRes = R.string.welcome_0_summary,
            iconRes = R.drawable.ic_description,
            iconDesc = stringResource(R.string.welcome_privacy_title),
            bodyRes = R.string.welcome_privacy_body,
            extraRes = R.string.welcome_privacy_license,
            hasButton = false,
        ),
        PageData(
            titleRes = R.string.welcome_1_title,
            summaryRes = R.string.welcome_1_summary,
            iconRes = R.drawable.ic_accessibility,
            iconDesc = stringResource(R.string.welcome_accessibility_title),
            bodyRes = R.string.welcome_accessibility_body,
            buttonTextRes = R.string.welcome_accessibility_action,
            hasButton = true,
        ),
        PageData(
            titleRes = R.string.welcome_2_title,
            summaryRes = R.string.welcome_2_summary,
            iconRes = R.drawable.ic_picture_in_picture,
            iconDesc = stringResource(R.string.welcome_overlay_title),
            bodyRes = R.string.welcome_overlay_body,
            buttonTextRes = R.string.welcome_overlay_action,
            hasButton = true,
        ),
        PageData(
            titleRes = R.string.welcome_3_title,
            summaryRes = R.string.welcome_3_summary,
            iconRes = R.drawable.ic_auto_fix,
            iconDesc = stringResource(R.string.welcome_theme_title),
            bodyRes = R.string.welcome_theme_body,
            hasButton = false,
        ),
    )

    val currentTitle = stringResource(pageData[pagerState.currentPage].titleRes)
    val currentSummary = stringResource(pageData[pagerState.currentPage].summaryRes)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = { (pagerState.currentPage * 1f) / (pageData.size - 1) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),

        )

        // Title + summary (outside pager for smooth transition)
        Text(
            text = currentTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Text(
            text = currentSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            WelcomePageContent(pageData[page], context)
        }

        // Bottom navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Prev button
            if (pagerState.currentPage > 0) {
                TextButton(onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }) {
                    Text(stringResource(R.string.welcome_prev))
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Spacer(Modifier.weight(1f))

            // Next button
            Button(onClick = {
                when {
                    pagerState.currentPage == 0 -> {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                    pagerState.currentPage == pageData.size - 1 -> {
                        Guide.markDone(context)
                        onFinish()
                    }
                    else -> {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            }) {
                Text(
                    if (pagerState.currentPage == 0) stringResource(R.string.welcome_agree)
                    else if (pagerState.currentPage == pageData.size - 1) stringResource(R.string.welcome_finish)
                    else stringResource(R.string.welcome_next)
                )
            }
        }
    }
}

@Composable
private fun WelcomePageContent(page: PageData, context: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(page.iconRes),
                contentDescription = page.iconDesc,
                modifier = Modifier.size(48.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        // Body text
        Text(
            text = stringResource(page.bodyRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // Extra text (license) or action button
        if (page.extraRes != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(page.extraRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }

        if (page.buttonTextRes != null) {
            Spacer(Modifier.height(24.dp))
            FilledTonalButton(onClick = {
                when (page.iconRes) {
                    R.drawable.ic_accessibility -> {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        Toast.makeText(context, R.string.accessibility_service_enabled, Toast.LENGTH_LONG).show()
                    }
                    R.drawable.ic_picture_in_picture -> {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                }
            }) {
                Text(stringResource(page.buttonTextRes))
            }
        }
    }
}

private data class PageData(
    val titleRes: Int,
    val summaryRes: Int,
    val iconRes: Int,
    val iconDesc: String,
    val bodyRes: Int,
    val extraRes: Int? = null,
    val buttonTextRes: Int? = null,
    val hasButton: Boolean,
)
