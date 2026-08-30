package top.youzix.nekoneko.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

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

        drawArc(colorBackground, 0f, 360f, false, topLeft, arcSize, Stroke(stroke, cap = StrokeCap.Round))
        drawArc(colorPrompt, -90f, sweepPrompt, false, topLeft, arcSize, Stroke(stroke, cap = StrokeCap.Round))
        if (cachedTokens > 0) {
            drawArc(colorCached, -90f, sweepCached, false, topLeft, arcSize, Stroke(stroke, cap = StrokeCap.Round))
        }
        drawArc(colorCompletion, -90f + sweepPrompt, sweepCompletion, false, topLeft, arcSize, Stroke(stroke, cap = StrokeCap.Round))
    }
}
