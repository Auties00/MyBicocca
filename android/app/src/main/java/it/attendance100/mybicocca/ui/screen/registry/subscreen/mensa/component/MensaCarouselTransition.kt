package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * The transition between a page's grid and its dish carousel, opened or closed by [open] and
 * scrubbed by the back gesture while the carousel is up. One transition, kept by the page that
 * hosts both, so a plain back press animates the plate home just like a slow gesture does.
 */
@Composable
internal fun rememberCarouselTransition(open: Boolean, onClose: () -> Unit): Transition<Boolean> {
    val seek = remember { SeekableTransitionState(open) }
    val transition = rememberTransition(seek, label = "mensa_carousel")
    val latestOpen by rememberUpdatedState(open)
    val rewindScope = rememberCoroutineScope()
    LaunchedEffect(open) {
        if (seek.currentState != open || seek.targetState != open) seek.animateTo(open)
    }
    PredictiveBackHandler(enabled = open) { progress ->
        try {
            progress.collect { event -> seek.seekTo(event.progress, targetState = false) }
        } catch (cancelled: CancellationException) {
            rewindScope.launch { seek.animateTo(latestOpen) }
            throw cancelled
        }
        onClose()
        // A vetoed back step leaves the carousel open: bring the transition back to it.
        rewindScope.launch {
            withFrameNanos { }
            if (latestOpen != seek.targetState) seek.animateTo(latestOpen)
        }
    }
    return transition
}
