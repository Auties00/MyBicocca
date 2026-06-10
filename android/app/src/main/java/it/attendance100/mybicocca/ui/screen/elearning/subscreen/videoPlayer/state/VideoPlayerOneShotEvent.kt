package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.state

/**
 * One-shot failure signals from the video player, distinguishing the two stages that can break:
 * resolving the Kaltura stream before playback starts versus an error raised by the player while
 * already playing.
 */
sealed interface VideoPlayerOneShotEvent {
    data class ResolveFailed(val cause: Throwable) : VideoPlayerOneShotEvent
    data class PlaybackError(val cause: Throwable) : VideoPlayerOneShotEvent
}
