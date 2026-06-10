package it.attendance100.mybicocca.core.os

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * What the active media screen wants a Picture-in-Picture window to look like: the aspect ratio
 * the window should take and whether playback is currently running (which gates auto-entry when
 * the user leaves the app).
 */
data class PipState(
    val aspectNumerator: Int,
    val aspectDenominator: Int,
    val isPlaying: Boolean,
)

/**
 * The activity-owned bridge between media screens and Android's Picture-in-Picture machinery.
 * A screen registers its PiP eligibility with [setActive] while media is on screen and clears it
 * with null when it leaves composition; the activity reads [currentState] to build the PiP
 * parameters.
 */
interface PipController {
    fun setActive(state: PipState?)
    fun currentState(): PipState?

    /** Requests immediate PiP entry, returning whether the system accepted it. */
    fun enterPipNow(): Boolean

    /**
     * True while the app window is in Picture-in-Picture, so a screen can collapse its chrome
     * to just the video. Updated by the Activity's onPictureInPictureModeChanged.
     */
    val isInPip: State<Boolean>
}

/**
 * Provided around the whole app by the activity; the default is a no-op controller that never
 * enters PiP, so previews and hosts without PiP support stay safe.
 */
val LocalPipController = compositionLocalOf<PipController> {
    object : PipController {
        override fun setActive(state: PipState?) = Unit
        override fun currentState(): PipState? = null
        override fun enterPipNow(): Boolean = false
        override val isInPip: State<Boolean> = mutableStateOf(false)
    }
}
