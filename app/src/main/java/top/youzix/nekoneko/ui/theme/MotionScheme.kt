package top.youzix.nekoneko.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * M3 Expressive Motion Scheme.
 *
 * Stores spring parameters (dampingRatio + stiffness) and easing curves
 * for the entire app. Use the extension functions to build actual spring specs.
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

    // ---- Spring parameters (store raw values to avoid API compatibility issues) ----
    val springDefaultDampingRatio: Float,
    val springDefaultStiffness: Float,
    val springFastDampingRatio: Float,
    val springFastStiffness: Float,
    val springSlowDampingRatio: Float,
    val springSlowStiffness: Float,
    val springExpressiveDampingRatio: Float,
    val springExpressiveStiffness: Float,
    val springSpatialDampingRatio: Float,
    val springSpatialStiffness: Float,

    // ---- Duration tokens (ms) ----
    val durationShort1: Int = 50,
    val durationShort2: Int = 100,
    val durationShort3: Int = 150,
    val durationShort4: Int = 200,
    val durationMedium1: Int = 250,
    val durationMedium2: Int = 300,
    val durationMedium3: Int = 350,
    val durationMedium4: Int = 400,
    val durationLong1: Int = 450,
    val durationLong2: Int = 500,
    val durationLong3: Int = 550,
    val durationLong4: Int = 600,
    val durationExtraLong1: Int = 700,
    val durationExtraLong2: Int = 800,
    val durationExtraLong3: Int = 900,
    val durationExtraLong4: Int = 1000,
) {
    companion object {
        // M3 standard damping ratios
        const val DAMPING_RATIO_NO_BOUNCY = 0.8f
        const val DAMPING_RATIO_MEDIUM_BOUNCY = 0.5f
        const val DAMPING_RATIO_LOW_BOUNCY = 0.3f

        // M3 standard stiffness values
        const val STIFFNESS_VERY_LOW = 100f
        const val STIFFNESS_LOW = 200f
        const val STIFFNESS_MEDIUM = 400f
        const val STIFFNESS_HIGH = 600f
        const val STIFFNESS_VERY_HIGH = 800f
    }
}

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
    springDefaultDampingRatio = NekoMotionScheme.DAMPING_RATIO_NO_BOUNCY,
    springDefaultStiffness = NekoMotionScheme.STIFFNESS_LOW,
    springFastDampingRatio = NekoMotionScheme.DAMPING_RATIO_NO_BOUNCY,
    springFastStiffness = NekoMotionScheme.STIFFNESS_MEDIUM,
    springSlowDampingRatio = NekoMotionScheme.DAMPING_RATIO_NO_BOUNCY,
    springSlowStiffness = NekoMotionScheme.STIFFNESS_VERY_LOW,
    springExpressiveDampingRatio = NekoMotionScheme.DAMPING_RATIO_MEDIUM_BOUNCY,
    springExpressiveStiffness = NekoMotionScheme.STIFFNESS_LOW,
    springSpatialDampingRatio = NekoMotionScheme.DAMPING_RATIO_NO_BOUNCY,
    springSpatialStiffness = NekoMotionScheme.STIFFNESS_VERY_HIGH,
)

/** CompositionLocal to provide the motion scheme down the tree. */
val LocalMotionScheme = staticCompositionLocalOf { DefaultMotionScheme }
