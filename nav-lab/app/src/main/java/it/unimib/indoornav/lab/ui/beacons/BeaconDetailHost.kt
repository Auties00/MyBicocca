package it.unimib.indoornav.lab.ui.beacons

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val HERO_DURATION_MS = 450

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalBeaconSharedScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalBeaconAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Shows [list] or the detail of the beacon [detailKey], morphing the beacon model between them. The
 * back gesture scrubs the morph and only closes the detail once released.
 *
 * The list leaves composition while the detail is shown, so callers keep its scroll state outside.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BeaconDetailHost(
    detailKey: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    list: @Composable () -> Unit,
    detail: @Composable (key: String) -> Unit,
) {
    val seekState = remember { SeekableTransitionState(detailKey) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(detailKey) {
        if (seekState.currentState != detailKey || seekState.targetState != detailKey) {
            seekState.animateTo(detailKey)
        }
    }

    PredictiveBackHandler(enabled = detailKey != null) { progress ->
        val openKey = detailKey
        try {
            progress.collect { event -> seekState.seekTo(event.progress, targetState = null) }
            onClose()
        } catch (e: CancellationException) {
            scope.launch { seekState.animateTo(openKey) }
            throw e
        }
    }

    val transition = rememberTransition(seekState, label = "beaconDetail")
    SharedTransitionLayout(modifier = modifier) {
        transition.AnimatedContent(
            transitionSpec = { fadeIn(tween(300, delayMillis = 90)) togetherWith fadeOut(tween(250)) },
        ) { key ->
            CompositionLocalProvider(
                LocalBeaconSharedScope provides this@SharedTransitionLayout,
                LocalBeaconAnimatedScope provides this@AnimatedContent,
            ) {
                if (key == null) list() else detail(key)
            }
        }
    }
}

/** Marks the halo and model box of one beacon as the element that flies between card and detail. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.beaconHero(tileKey: String): Modifier {
    val shared = LocalBeaconSharedScope.current ?: return this
    val animated = LocalBeaconAnimatedScope.current ?: return this
    return with(shared) {
        this@beaconHero.sharedBounds(
            sharedContentState = rememberSharedContentState("beacon-hero-$tileKey"),
            animatedVisibilityScope = animated,
            boundsTransform = { _, _ -> tween(HERO_DURATION_MS, easing = FastOutSlowInEasing) },
            // Scaling keeps the model at its laid-out size, so no new bitmap is rendered per frame.
            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
        )
    }
}
