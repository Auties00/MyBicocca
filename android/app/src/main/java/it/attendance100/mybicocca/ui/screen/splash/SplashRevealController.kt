package it.attendance100.mybicocca.ui.screen.splash

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntRect

@Immutable
data class SplashIconSnapshot(
    val image: ImageBitmap,
    val boundsInWindow: IntRect,
    val scaleX: Float,
    val scaleY: Float,
    val alpha: Float,
    val pivotXFraction: Float,
    val pivotYFraction: Float,
)

class SplashRevealController {
    var iconSnapshot by mutableStateOf<SplashIconSnapshot?>(null)
        private set
    var handoffStarted by mutableStateOf(false)
        private set
    var revealStarted by mutableStateOf(false)
        private set
    var finished by mutableStateOf(false)
        private set

    fun beginHandoff(snapshot: SplashIconSnapshot?) {
        if (handoffStarted) return
        iconSnapshot = snapshot
        handoffStarted = true
    }

    fun startReveal() {
        if (!handoffStarted || revealStarted) return
        revealStarted = true
    }

    fun finish() {
        finished = true
    }
}
