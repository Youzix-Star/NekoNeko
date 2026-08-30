package top.youzix.nekoneko.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import top.youzix.nekoneko.ui.theme.MaterialTheme

/**
 * Compose version of RingChartView.
 * Draws a ring chart showing token distribution (prompt / completion / cached).
 */
@Composable
fun RingChart(
    promptTokens: Int,
    completionTokens: Int,
    cachedTokens: Int,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    val colorPrompt = colorScheme.primary
    val colorCompletion = colorScheme.tertiary
    val colorCached = colorScheme.secondaryContainer
    val colorBackground = colorScheme.surfaceContainerHigh

    Canvas(modifier = modifier.size(140.dp)) {
        val stroke = size.minDimension * 0.12f
        val padding = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(padding, padding)

        val total = promptTokens + completionTokens
        if (total == 0) {
            // Empty state: draw background ring
            drawArc(
                color = colorBackground,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            return@Canvas
        }

        val sweepPrompt = 360f * promptTokens / total
        val sweepCached = 360f * cachedTokens / total
        val sweepCompletion = 360f * completionTokens / total

        // Background ring
        drawArc(
            color = colorBackground,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        // Prompt tokens
        drawArc(
            color = colorPrompt,
            startAngle = -90f,
            sweepAngle = sweepPrompt,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        // Cached tokens (overlaid on prompt)
        if (cachedTokens > 0) {
            drawArc(
                color = colorCached,
                startAngle = -90f,
                sweepAngle = sweepCached,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }

        // Completion tokens
        drawArc(
            color = colorCompletion,
            startAngle = -90f + sweepPrompt,
            sweepAngle = sweepCompletion,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}
