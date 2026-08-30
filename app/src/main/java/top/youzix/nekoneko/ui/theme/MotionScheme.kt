package top.youzix.nekoneko.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * M3 Expressive Motion Scheme.
 *
 * Defines spring-based and easing-based motion tokens for the entire app.
 * Maps directly to Material 3 Expressive's motion system:
 * - Expressive springs for entering/exiting animations
 * - Standard springs for persistent state changes
 * - Tweens with cubic bezier easing for precise timing
 */
@Immutable
data class NekoMotionScheme(
    // ---- Easing curves ----
    val emphasizedEasing: CubicBezierEasing,
    val emphasizedDecelerateEasing: CubicBezierEasing,
    val emphasizedAccelerateEasing: CubicBezierEasing,
    val standardEasing: CubicBezierEasing,
    val standardDecelerateEasing: CubicBezierEasing,
    val standardAccelerateEasing: CubicBezierEasing,

    // ---- Spring specs ----
    val springDefault: Spring<Float>,
    val springFast: Spring<Float>,
    val springSlow: Spring<Float>,
    val springExpressive: Spring<Float>,
    val springSpatial: Spring<Float>,

    // ---- Duration tokens (ms) ----
    val durationShort1: Int,
    val durationShort2: Int,
    val durationShort3: Int,
    val durationShort4: Int,
    val durationMedium1: Int,
    val durationMedium2: Int,
    val durationMedium3: Int,
    val durationMedium4: Int,
    val durationLong1: Int,
    val durationLong2: Int,
    val durationLong3: Int,
    val durationLong4: Int,
    val durationExtraLong1: Int,
    val durationExtraLong2: Int,
    val durationExtraLong3: Int,
    val durationExtraLong4: Int,
)

/** Default M3 Expressive motion scheme. */
val DefaultMotionScheme = NekoMotionScheme(
    // Easing — M3 Expressive standard curves
    emphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    emphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f),
    emphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f),
    standardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    standardDecelerateEasing = CubicBezierEasing(0f, 0f, 0f, 1f),
    standardAccelerateEasing = CubicBezierEasing(0.3f, 0f, 1f, 1f),

    // Springs — expressive motion
    springDefault = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow,
    ),
    springFast = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    ),
    springSlow = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
    ),
    springExpressive = spring(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessLow,
    ),
    springSpatial = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 800f,
    ),

    // Duration tokens
    durationShort1 = 50,
    durationShort2 = 100,
    durationShort3 = 150,
    durationShort4 = 200,
    durationMedium1 = 250,
    durationMedium2 = 300,
    durationMedium3 = 350,
    durationMedium4 = 400,
    durationLong1 = 450,
    durationLong2 = 500,
    durationLong3 = 550,
    durationLong4 = 600,
    durationExtraLong1 = 700,
    durationExtraLong2 = 800,
    durationExtraLong3 = 900,
    durationExtraLong4 = 1000,
)

/** CompositionLocal to provide the motion scheme down the tree. */
val LocalMotionScheme = staticCompositionLocalOf { DefaultMotionScheme }

/** Convenience accessor via MaterialTheme-like pattern. */
object NekoMotion {
    val scheme: NekoMotionScheme
        @Composable
        get() = LocalMotionScheme.current
}
