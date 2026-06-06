package it.attendance100.mybicocca.ui.component.modal

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

// Forward/back page transition for in-sheet navigation (edifici list -> building -> room):
// a soft horizontal push with a synchronized sheet-height morph. Scoped to
// AnimatedContentTransitionScope because `using` is a member of the transitionSpec scope.
fun AnimatedContentTransitionScope<*>.sheetPageTransform(forward: Boolean): ContentTransform =
    (fadeIn(tween(280, delayMillis = 40)) + slideInHorizontally(tween(350)) { if (forward) it / 8 else -it / 8 })
        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(350)) { if (forward) -it / 8 else it / 8 })
        .using(SizeTransform(clip = true) { _, _ -> tween(350) })
