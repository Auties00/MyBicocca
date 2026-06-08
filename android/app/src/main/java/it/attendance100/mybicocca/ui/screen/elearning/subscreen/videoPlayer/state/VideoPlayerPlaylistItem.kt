package it.attendance100.mybicocca.ui.screen.elearning.subscreen.videoPlayer.state

data class VideoPlayerPlaylistItem(
    val cmId: Int,
    val title: String,
    val sectionName: String?,
    val progressFraction: Float,
    val completed: Boolean,
    val isCurrent: Boolean,
    val isLastSeen: Boolean,
)
