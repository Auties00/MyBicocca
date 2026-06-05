package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.state

sealed interface VideoPlayerOneShotEvent {
    data class ResolveFailed(val cause: Throwable) : VideoPlayerOneShotEvent
    data class PlaybackError(val cause: Throwable) : VideoPlayerOneShotEvent
}
