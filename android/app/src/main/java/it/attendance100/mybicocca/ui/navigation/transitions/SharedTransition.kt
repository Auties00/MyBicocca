package it.attendance100.mybicocca.ui.navigation.transitions

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionDefaults
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale

// Modifier to apply shared element transition to a static/only scalable element
@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.bicoccaSharedElement(
    sharedTransitionScope: SharedTransitionScope? = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedContentScope? = LocalAnimatedContentScope.current,
    key: SharedElementKey,
    zIndexInOverlay: Float = 0f,
    clipShape: Shape? = null,
    easing: (Rect, Rect) -> FiniteAnimationSpec<Rect> = { _, _ ->
        spring(stiffness = StiffnessMediumLow, visibilityThreshold = Rect.VisibilityThreshold)
    },
): Modifier {
    return if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            if (clipShape != null) this@bicoccaSharedElement.sharedElement(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                zIndexInOverlay = zIndexInOverlay,
                clipInOverlayDuringTransition = OverlayClip(clipShape),
                boundsTransform = { initialBounds, targetBounds ->
                    easing(initialBounds, targetBounds)
                }
            )
            else this@bicoccaSharedElement.sharedElement(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                zIndexInOverlay = zIndexInOverlay,
                boundsTransform = { initialBounds, targetBounds ->
                    easing(initialBounds, targetBounds)
                }
            )
        }
    } else {
        this
    }
}


// Modifier to apply shared bounds transition to a dynamic element
@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.bicoccaSharedBounds(
    sharedTransitionScope: SharedTransitionScope? = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedContentScope? = LocalAnimatedContentScope.current,
    key: SharedElementKey,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    resizeMode: SharedTransitionScope.ResizeMode = scaleToBounds(ContentScale.FillWidth, Center),
    zIndexInOverlay: Float = 0f,
    clipShape: Shape? = null,
    boundsTransform: BoundsTransform = SharedTransitionDefaults.BoundsTransform
): Modifier {
    return if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            if (clipShape != null) this@bicoccaSharedBounds.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = enter,
                exit = exit,
                resizeMode = resizeMode,
                zIndexInOverlay = zIndexInOverlay,
                boundsTransform = boundsTransform,
                clipInOverlayDuringTransition = OverlayClip(clipShape)
            )
            else this@bicoccaSharedBounds.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = enter,
                exit = exit,
                resizeMode = resizeMode,
                zIndexInOverlay = zIndexInOverlay,
                boundsTransform = boundsTransform
            )
        }
    } else {
        this
    }
}