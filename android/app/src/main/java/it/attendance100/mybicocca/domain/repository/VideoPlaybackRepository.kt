package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import kotlinx.coroutines.flow.Flow

interface VideoPlaybackRepository {
    suspend fun resolveStream(cmId: Int): VideoStream

    /**
     * Returns the Kaltura thumbnail URL for [cmId], or `null` if the entry id cannot be
     * resolved (e.g. the Moodle session has expired, the cmId isn't a kalvidres module).
     * Results are cached in-memory for the lifetime of the repository so the same cmId
     * doesn't hit the network twice.
     */
    suspend fun thumbnailUrl(cmId: Int): String?

    fun observeProgress(accountId: AccountId, cmId: Int): Flow<VideoProgress?>
    fun observeCourseProgress(accountId: AccountId, courseId: CourseId): Flow<Map<Int, VideoProgress>>

    suspend fun saveProgress(
        accountId: AccountId,
        courseId: CourseId,
        cmId: Int,
        positionMs: Long,
        durationMs: Long,
    )

    suspend fun markCompleted(accountId: AccountId, courseId: CourseId, cmId: Int)

    suspend fun clearForAccount(accountId: AccountId)
}
