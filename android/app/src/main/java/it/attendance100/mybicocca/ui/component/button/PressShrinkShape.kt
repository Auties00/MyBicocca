package it.attendance100.mybicocca.ui.component.button

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Returns a [Shape] whose corner radius shrinks while [interactionSource] is being
 * pressed and springs back when released — that "squish on tap" effect we use on
 * the primary auth buttons. Reusable for any button across the app.
 *
 * Caller pattern:
 * ```kotlin
 * val source = remember { MutableInteractionSource() }
 * Button(
 *     onClick = { ... },
 *     interactionSource = source,
 *     shape = rememberPressShrinkShape(source),
 * ) { ... }
 * ```
 *
 * Each button must `remember` its own [MutableInteractionSource]; sharing one
 * couples their press states (touching SPID would also squish CIE).
 *
 * @param interactionSource the same source handed to the button so this helper
 *   sees the press events.
 * @param idleCorner radius when not pressed. Defaults to 26dp — half the 52dp
 *   primary-button height, giving a full pill at rest.
 * @param pressedFraction the fraction of [idleCorner] to shrink to while held;
 *   defaults to 0.25 (a quarter), the value the auth screen was tuned with.
 */
@Composable
fun rememberPressShrinkShape(
    interactionSource: InteractionSource,
    idleCorner: Dp = 26.dp,
    pressedFraction: Float = 0.25f,
): Shape {
    val isPressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (isPressed) idleCorner * pressedFraction else idleCorner,
        label = "pressShrinkCorner",
    )
    return RoundedCornerShape(corner)
}

/**
 * Convenience overload for callers that don't otherwise need to hold the
 * [InteractionSource] — creates one, remembers it, and returns it alongside the
 * animated shape so the button can wire both up.
 *
 * ```kotlin
 * val (source, shape) = rememberPressShrink()
 * Button(onClick = { ... }, interactionSource = source, shape = shape) { ... }
 * ```
 */
@Composable
fun rememberPressShrink(
    idleCorner: Dp = 26.dp,
    pressedFraction: Float = 0.25f,
): Pair<MutableInteractionSource, Shape> {
    val source = remember { MutableInteractionSource() }
    val shape = rememberPressShrinkShape(source, idleCorner, pressedFraction)
    return source to shape
}
