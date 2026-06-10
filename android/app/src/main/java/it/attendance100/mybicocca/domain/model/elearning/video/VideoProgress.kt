package it.attendance100.mybicocca.domain.model.elearning.video

import java.time.Instant

/**
 * The student's playback progress on a lecture video (Kaltura kalvidres course module),
 * persisted locally per account. Drives the resume position of the video player and the
 * continue-watching card and per-video progress indicators of the course detail screen.
 *
 * @property cmId Moodle course-module id of the video activity.
 * @property courseId Course the video belongs to.
 * @property positionMs Last playback position in milliseconds.
 * @property durationMs Known media duration in milliseconds; 0 until a player reports it.
 * @property completed Whether the video counts as watched; set once playback passes roughly
 * 90% of the duration and mirrored to the platform's activity completion.
 * @property lastUpdatedAt When the progress was last saved.
 */
data class VideoProgress(
    val cmId: Int,
    val courseId: Int,
    val positionMs: Long,
    val durationMs: Long,
    val completed: Boolean,
    val lastUpdatedAt: Instant,
) {
    /** Watched fraction in 0..1; 0 while the duration is still unknown. */
    val progressFraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
