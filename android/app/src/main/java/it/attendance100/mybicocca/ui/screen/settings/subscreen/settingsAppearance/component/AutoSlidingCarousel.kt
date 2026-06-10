package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsAppearance.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * Auto-advancing carousel: every [slideDurationMillis] the next item enters from the right
 * while the previous one exits left, both fading, wrapping around past the last item. Override
 * the motion via [enterAnim]/[exitAnim]. Renders nothing while [items] is empty, and the
 * advance timer restarts whenever [items] or [slideDurationMillis] change. If the list shrinks
 * underneath a pending transition the index is clamped to bounds instead of crashing.
 * [itemContent] renders the current item.
 */
@Composable
fun <T> AutoSlidingCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    slideDurationMillis: Long = 2000L,
    animationDurationMillis: Int = 500,
    enterAnim: EnterTransition? = null,
    exitAnim: ExitTransition? = null,
    itemContent: @Composable (item: T) -> Unit
) {
    if (items.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(items, slideDurationMillis) {
        while (true) {
            delay(slideDurationMillis)
            currentIndex = (currentIndex + 1) % items.size
        }
    }

    Box(modifier = modifier) {
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                val enterAnimFinal = enterAnim ?: (slideInHorizontally(
                    animationSpec = tween(animationDurationMillis),
                    initialOffsetX = { fullWidth -> fullWidth }
                ) + fadeIn(animationSpec = tween(animationDurationMillis)))

                val exitAnimFinal = exitAnim ?: (slideOutHorizontally(
                    animationSpec = tween(animationDurationMillis),
                    targetOffsetX = { fullWidth -> -fullWidth }
                ) + fadeOut(animationSpec = tween(animationDurationMillis)))

                enterAnimFinal.togetherWith(exitAnimFinal)
            },
            label = "auto_carousel_animation"
        ) { targetIndex ->
            val safeIndex = targetIndex.coerceIn(0, items.lastIndex)
            itemContent(items[safeIndex])
        }
    }
}

/** Overload for a list of self-contained composable slides: delegates to the generic carousel with each lambda as its own item. */
@Composable
fun AutoSlidingCarousel(
    items: List<@Composable (() -> Unit)>,
    modifier: Modifier = Modifier,
    slideDurationMillis: Long = 2000L,
    animationDurationMillis: Int = 500,
    enterAnim: EnterTransition? = null,
    exitAnim: ExitTransition? = null,
) {
    AutoSlidingCarousel(
        items = items,
        modifier = modifier,
        slideDurationMillis = slideDurationMillis,
        animationDurationMillis = animationDurationMillis,
        enterAnim = enterAnim,
        exitAnim = exitAnim,
    ) { composableItem ->
        composableItem()
    }
}