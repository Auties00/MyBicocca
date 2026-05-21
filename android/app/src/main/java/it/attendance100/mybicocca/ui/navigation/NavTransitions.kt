package it.attendance100.mybicocca.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MotionScheme

internal fun defaultEnterTransition(motion: MotionScheme): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it / 2 },
        animationSpec = motion.defaultSpatialSpec(),
    ) + fadeIn(animationSpec = motion.defaultEffectsSpec())

internal fun defaultExitTransition(motion: MotionScheme): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { -it / 2 },
        animationSpec = motion.defaultSpatialSpec(),
    ) + fadeOut(animationSpec = motion.defaultEffectsSpec())

internal fun defaultPopEnterTransition(motion: MotionScheme): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { -it / 2 },
        animationSpec = motion.defaultSpatialSpec(),
    ) + fadeIn(animationSpec = motion.defaultEffectsSpec())

internal fun defaultPopExitTransition(motion: MotionScheme): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it / 4 },
        animationSpec = motion.defaultSpatialSpec(),
    ) + fadeOut(animationSpec = motion.defaultEffectsSpec())
