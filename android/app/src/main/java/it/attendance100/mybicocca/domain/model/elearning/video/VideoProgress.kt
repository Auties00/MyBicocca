package it.attendance100.mybicocca.domain.model.elearning.video

import java.time.Instant

data class VideoProgress(
    val cmId: Int,
    val courseId: Int,
    val positionMs: Long,
    val durationMs: Long,
    val completed: Boolean,
    val lastUpdatedAt: Instant,
) {
    val progressFraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
