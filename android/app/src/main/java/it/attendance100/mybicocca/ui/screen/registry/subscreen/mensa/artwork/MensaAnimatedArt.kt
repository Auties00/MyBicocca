package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.artwork

import androidx.annotation.DrawableRes
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import it.attendance100.mybicocca.R
import kotlinx.coroutines.delay

/**
 * The drawings that move, with how long one round of each lasts. Each starts and ends on the same
 * frame, so playing it again from the start loops without a seam.
 */
internal object MensaAnimatedArt {
    private val loops: Map<Int, Long> = mapOf(
        R.drawable.mensa_art_drink_cola to 8000L,
        R.drawable.mensa_art_drink_still_water to 6000L,
        R.drawable.mensa_art_drink_sparkling_water to 6000L,
    )

    /** How long [drawable]'s loop lasts, or null when it is a still drawing. */
    fun loopMillis(@DrawableRes drawable: Int): Long? = loops[drawable]
}

/** Plays the animated drawing [drawable] over and over, a round every [loopMillis]. */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
internal fun MensaLoopingArt(@DrawableRes drawable: Int, loopMillis: Long, modifier: Modifier = Modifier) {
    val image = AnimatedImageVector.animatedVectorResource(drawable)
    var round by remember(drawable) { mutableIntStateOf(0) }
    // A fresh painter each round plays the drawing again from the start, rather than in reverse.
    key(drawable, round) {
        var atEnd by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            atEnd = true
            delay(loopMillis)
            round++
        }
        Image(
            painter = rememberAnimatedVectorPainter(image, atEnd),
            contentDescription = null,
            modifier = modifier,
        )
    }
}
