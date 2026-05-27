package it.attendance100.mybicocca.ui.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import it.attendance100.mybicocca.R
import kotlin.math.roundToInt

@Composable
fun SplashRevealOverlay(controller: SplashRevealController) {
    if (controller.finished) return

    val background = colorResource(R.color.launcher_background)
    val fallbackLogo = painterResource(R.drawable.logo)
    val density = LocalDensity.current
    val snapshot = controller.iconSnapshot
    val revealStarted = controller.revealStarted
    val progress = remember { Animatable(0f) }
    var overlayOriginInWindow by remember { mutableStateOf(IntOffset.Zero) }

    LaunchedEffect(revealStarted) {
        if (!revealStarted) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = RevealDurationMs, easing = LinearEasing),
        )
        controller.finish()
    }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                overlayOriginInWindow = IntOffset(position.x.roundToInt(), position.y.roundToInt())
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial).changes.forEach { change ->
                            change.consume()
                        }
                    }
                }
            }
            .drawBehind {
                drawRect(
                    color = background,
                    alpha = backgroundAlpha(progress.value),
                )
            },
    ) {
        if (snapshot != null) {
            Image(
                bitmap = snapshot.image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = snapshot.boundsInWindow.left - overlayOriginInWindow.x,
                            y = snapshot.boundsInWindow.top - overlayOriginInWindow.y,
                        )
                    }
                    .requiredSize(
                        width = with(density) { snapshot.boundsInWindow.width.toDp() },
                        height = with(density) { snapshot.boundsInWindow.height.toDp() },
                    )
                    .nativeSplashRevealLayer(snapshot, progress),
            )
        } else {
            Image(
                painter = fallbackLogo,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .requiredSize(FallbackIconSize)
                    .nativeSplashRevealLayer(snapshot = null, progress = progress),
            )
        }
    }
}

private fun Modifier.nativeSplashRevealLayer(
    snapshot: SplashIconSnapshot?,
    progress: Animatable<Float, *>,
): Modifier = graphicsLayer {
    val p = progress.value
    val revealScale = iconScale(p)
    scaleX = (snapshot?.scaleX ?: 1f) * revealScale
    scaleY = (snapshot?.scaleY ?: 1f) * revealScale
    alpha = (snapshot?.alpha ?: 1f) * iconAlpha(p)
    transformOrigin = snapshot?.let {
        TransformOrigin(it.pivotXFraction, it.pivotYFraction)
    } ?: TransformOrigin.Center
    clip = false
}

private const val RevealDurationMs = 280
private val FallbackIconSize = 288.dp
private const val MaxScale = 5.5f
private val LaunchEasing = CubicBezierEasing(0.45f, 0f, 0.9f, 0.35f)

private fun iconScale(p: Float): Float =
    lerp(1f, MaxScale, LaunchEasing.transform(p))

private fun iconAlpha(p: Float): Float = when {
    p <= IconFadeStart -> 1f
    p >= 1f -> 0f
    else -> 1f - ((p - IconFadeStart) / (1f - IconFadeStart)).coerceIn(0f, 1f)
}

private const val IconFadeStart = 0.48f
private const val BgFadeStart = 0f
private const val BgFadeEnd = 0.56f

private fun backgroundAlpha(p: Float): Float = when {
    p <= BgFadeStart -> 1f
    p >= BgFadeEnd -> 0f
    else -> 1f - FastOutSlowInEasing.transform((p - BgFadeStart) / (BgFadeEnd - BgFadeStart))
}
