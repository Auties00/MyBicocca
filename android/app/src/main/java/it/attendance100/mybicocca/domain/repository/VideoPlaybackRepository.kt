package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import kotlinx.coroutines.flow.Flow

/**
 * Contract for lecture-video (Kaltura) playback: on-demand stream resolution plus locally
 * persisted, account-scoped watch progress. Observe methods are hot flows from the local
 * cache; stream resolution hits the e-learning platform on every call because the URLs are
 * session-scoped, and throws on failure.
 */
interface VideoPlaybackRepository {
    /** Resolves the playable stream for a video course module; never cached. Throws on failure. */
    suspend fun resolveStream(cmId: Int): VideoStream

    /**
     * Returns the Kaltura thumbnail URL for [cmId], or `null` if the entry id cannot be
     * resolved (e.g. the Moodle session has expired, the cmId isn't a kalvidres module).
     * Results are cached in-memory for the lifetime of the repository so the same cmId
     * doesn't hit the network twice.
     */
    suspend fun thumbnailUrl(cmId: Int): String?

    /** Streams the saved progress of one video; null while it has never been played. */
    fun observeProgress(accountId: AccountId, cmId: Int): Flow<VideoProgress?>

    /** Streams all saved progress of a course, keyed by course-module id. */
    fun observeCourseProgress(accountId: AccountId, courseId: CourseId): Flow<Map<Int, VideoProgress>>

    /**
     * Persists the playback position; once it passes the completion threshold the video is
     * flagged watched and the completion is mirrored to the platform.
     */
    suspend fun saveProgress(
        accountId: AccountId,
        courseId: CourseId,
        cmId: Int,
        positionMs: Long,
        durationMs: Long,
    )

    /** Marks a video fully watched locally and mirrors the completion to the platform. */
    suspend fun markCompleted(accountId: AccountId, courseId: CourseId, cmId: Int)

    /** Drops every saved video progress of the account. */
    suspend fun clearForAccount(accountId: AccountId)
}
