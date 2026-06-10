package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.state

/**
 * One course video as the playlist side panel renders it: identity and titles plus the watch
 * state driving the row's decorations — saved progress, completion, whether it is the video
 * playing right now, and whether it is the one the user most recently left unfinished (which
 * gets the wavy progress indicator).
 */
data class VideoPlayerPlaylistItem(
    val cmId: Int,
    val title: String,
    val sectionName: String?,
    val progressFraction: Float,
    val completed: Boolean,
    val isCurrent: Boolean,
    val isLastSeen: Boolean,
)
