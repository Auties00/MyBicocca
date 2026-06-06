package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.player

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

data class PipState(
    val aspectNumerator: Int,
    val aspectDenominator: Int,
    val isPlaying: Boolean,
)

interface PipController {
    fun setActive(state: PipState?)
    fun currentState(): PipState?
    fun enterPipNow(): Boolean

    // True while the app window is in Picture-in-Picture, so a screen can collapse its chrome
    // to just the video. Updated by the Activity's onPictureInPictureModeChanged.
    val isInPip: State<Boolean>
}

val LocalPipController = compositionLocalOf<PipController> {
    object : PipController {
        override fun setActive(state: PipState?) = Unit
        override fun currentState(): PipState? = null
        override fun enterPipNow(): Boolean = false
        override val isInPip: State<Boolean> = mutableStateOf(false)
    }
}
