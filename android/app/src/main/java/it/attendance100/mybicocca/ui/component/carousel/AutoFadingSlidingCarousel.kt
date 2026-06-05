package it.attendance100.mybicocca.ui.component.carousel

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

    // Restart the timer only if the items list or duration changes
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
            // Prevent OutOfBounds exception if the list suddenly shrinks
            val safeIndex = targetIndex.coerceIn(0, items.lastIndex)

            // Delegate the UI rendering to the caller
            itemContent(items[safeIndex])
        }
    }
}

@Composable
fun AutoSlidingCarousel(
    items: List<@Composable (() -> Unit)>,
    modifier: Modifier = Modifier,
    slideDurationMillis: Long = 2000L,
    animationDurationMillis: Int = 500,
    enterAnim: EnterTransition? = null,
    exitAnim: ExitTransition? = null,
) {
    // We call the generic version we already built, where T is the Composable lambda itself.
    AutoSlidingCarousel(
        items = items,
        modifier = modifier,
        slideDurationMillis = slideDurationMillis,
        animationDurationMillis = animationDurationMillis,
        enterAnim = enterAnim,
        exitAnim = exitAnim,
    ) { composableItem ->
        // Execute the lambda to render the UI
        composableItem()
    }
}