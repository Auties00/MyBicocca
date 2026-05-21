package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player

import androidx.compose.runtime.compositionLocalOf

data class PipState(
    val aspectNumerator: Int,
    val aspectDenominator: Int,
    val isPlaying: Boolean,
)

interface PipController {
    fun setActive(state: PipState?)
    fun currentState(): PipState?
    fun enterPipNow(): Boolean
}

val LocalPipController = compositionLocalOf<PipController> {
    object : PipController {
        override fun setActive(state: PipState?) = Unit
        override fun currentState(): PipState? = null
        override fun enterPipNow(): Boolean = false
    }
}
