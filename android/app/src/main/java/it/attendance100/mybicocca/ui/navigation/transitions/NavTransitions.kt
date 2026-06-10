package it.attendance100.mybicocca.ui.navigation.transitions

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MotionScheme

/** Forward push: the incoming page slides in from the right half-width while fading in. */
internal fun defaultEnterTransition(motion: MotionScheme): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it / 2 },
        animationSpec = motion.defaultSpatialSpec(),
    ) + fadeIn(animationSpec = motion.defaultEffectsSpec())

/** Forward push: the outgoing page slides off to the left half-width while fading out. */
internal fun defaultExitTransition(motion: MotionScheme): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { -it / 2 },
        animationSpec = motion.defaultSpatialSpec(),
    ) + fadeOut(animationSpec = motion.defaultEffectsSpec())

/** Pop: the restored page slides back in from the left half-width while fading in. */
internal fun defaultPopEnterTransition(motion: MotionScheme): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { -it / 2 },
        animationSpec = motion.defaultSpatialSpec(),
    ) + fadeIn(animationSpec = motion.defaultEffectsSpec())

/**
 * Pop: the popped page drifts only a quarter-width right while the fade does most of the work —
 * a shallow exit that keeps the predictive-back scrub subtle.
 */
internal fun defaultPopExitTransition(motion: MotionScheme): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it / 4 },
        animationSpec = motion.defaultSpatialSpec(),
    ) + fadeOut(animationSpec = motion.defaultEffectsSpec())
