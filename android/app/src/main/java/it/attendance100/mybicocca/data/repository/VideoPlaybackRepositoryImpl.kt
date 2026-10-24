package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.video.KalturaEntryDao
import it.attendance100.mybicocca.data.local.elearning.video.KalturaEntryEntity
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressDao
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressEntity
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.elearning.ElearningKaltura
import it.attendance100.mybicocca.data.elearning.ElearningKalturaEntryIdResponse
import it.attendance100.mybicocca.data.elearning.ElearningKalturaVideoStreamResponse
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import it.attendance100.mybicocca.domain.repository.VideoUnavailableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Video playback repository on top of the e-learning platform's Kaltura (kalvidres)
 * integration, with watch progress persisted in Room per account.
 *
 * A module's Kaltura entry id is read once from `mod/kalvidres/view.php`, with one session
 * renewal and retry when the `MoodleSession` cookie has expired, and then persisted in Room:
 * every load of that page logs a "viewed" event in Moodle. Streams are looked up with Kaltura
 * on every resolution, since an entry can be withdrawn at any time. When saved progress passes
 * the completion threshold the row is flagged completed and the activity completion is
 * mirrored to the platform best-effort — a mirror failure never breaks local playback state.
 */
@Singleton
class VideoPlaybackRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val videoProgressDao: VideoProgressDao,
    private val kalturaEntryDao: KalturaEntryDao,
    private val elearningCourseRepository: ElearningCourseRepository,
) : VideoPlaybackRepository {

    override suspend fun resolveStream(cmId: Int): VideoStream {
        val entryId = resolveEntryId(cmId)
            ?: throw IllegalStateException("Could not resolve the Kaltura entry of cmId=$cmId after reauth")
        return when (val stream = sessionManager.elearning().api.kaltura.getVideoStream(entryId)) {
            is ElearningKalturaVideoStreamResponse.Success -> stream.toDomain(cmId)
            ElearningKalturaVideoStreamResponse.Unavailable -> throw VideoUnavailableException(cmId)
        }
    }

    override suspend fun thumbnailUrl(cmId: Int): String? =
        resolveEntryId(cmId)?.let { ElearningKaltura.thumbnailUrl(it, widthPx = THUMBNAIL_WIDTH_PX) }

    /**
     * Returns the persisted entry id of [cmId], or reads it from `view.php`, renewing the
     * Moodle session once if the page came back without it. Null when even the retry fails.
     */
    private suspend fun resolveEntryId(cmId: Int): String? {
        kalturaEntryDao.getEntryId(cmId)?.let { return it }
        val entryId = when (val response = sessionManager.elearning().api.kaltura.resolveKalturaEntryIdForModule(cmId)) {
            is ElearningKalturaEntryIdResponse.Success -> response.kalturaEntryId
            ElearningKalturaEntryIdResponse.RequiresReauth -> {
                when (val retry = sessionManager.reauthElearning().api.kaltura.resolveKalturaEntryIdForModule(cmId)) {
                    is ElearningKalturaEntryIdResponse.Success -> retry.kalturaEntryId
                    ElearningKalturaEntryIdResponse.RequiresReauth -> return null
                }
            }
        }
        kalturaEntryDao.upsert(KalturaEntryEntity(cmId, entryId, System.currentTimeMillis()))
        return entryId
    }

    override fun observeProgress(accountId: AccountId, cmId: Int): Flow<VideoProgress?> =
        videoProgressDao.observe(accountId.value, cmId)
            .map { it?.toDomain() }
            .flowOn(Dispatchers.Default)

    override fun observeCourseProgress(
        accountId: AccountId,
        courseId: CourseId,
    ): Flow<Map<Int, VideoProgress>> =
        videoProgressDao.observeByCourse(accountId.value, courseId.value)
            .map { rows -> rows.associate { it.cmId to it.toDomain() } }
            .flowOn(Dispatchers.Default)

    /**
     * A non-positive incoming duration falls back to the stored one: the player reports an
     * unset duration until the manifest fully loads, and periodic ticking saves must not reset
     * a known duration back to zero. Completion is one-way — once flagged, later saves keep it.
     */
    override suspend fun saveProgress(
        accountId: AccountId,
        courseId: CourseId,
        cmId: Int,
        positionMs: Long,
        durationMs: Long,
    ) {
        val existing = videoProgressDao.getOnce(accountId.value, cmId)
        val alreadyCompleted = existing?.completed == true
        val mergedDuration = durationMs.coerceAtLeast(0L)
            .takeIf { it > 0L }
            ?: existing?.durationMs
            ?: 0L
        val shouldComplete = !alreadyCompleted && mergedDuration > 0 &&
            positionMs >= (mergedDuration * COMPLETION_FRACTION).toLong()

        videoProgressDao.upsert(
            VideoProgressEntity(
                accountId = accountId.value,
                cmId = cmId,
                courseId = courseId.value,
                positionMs = positionMs.coerceAtLeast(0L),
                durationMs = mergedDuration,
                completed = alreadyCompleted || shouldComplete,
                lastUpdatedAtMs = System.currentTimeMillis(),
            )
        )

        if (shouldComplete) {
            mirrorCompletionToMoodle(accountId, courseId, cmId)
        }
    }

    override suspend fun markCompleted(accountId: AccountId, courseId: CourseId, cmId: Int) {
        val existing = videoProgressDao.getOnce(accountId.value, cmId)
        if (existing?.completed == true) return

        videoProgressDao.upsert(
            VideoProgressEntity(
                accountId = accountId.value,
                cmId = cmId,
                courseId = courseId.value,
                positionMs = existing?.positionMs ?: 0L,
                durationMs = existing?.durationMs ?: 0L,
                completed = true,
                lastUpdatedAtMs = System.currentTimeMillis(),
            )
        )
        mirrorCompletionToMoodle(accountId, courseId, cmId)
    }

    override suspend fun clearForAccount(accountId: AccountId) {
        videoProgressDao.deleteForAccount(accountId.value)
    }

    /** Best-effort: a platform completion failure must not break local playback state. */
    private suspend fun mirrorCompletionToMoodle(
        accountId: AccountId,
        courseId: CourseId,
        cmId: Int,
    ) {
        runCatching {
            elearningCourseRepository.setActivityCompleted(
                accountId = accountId,
                courseId = courseId,
                cmId = cmId,
                completed = true,
            )
        }
    }

    private companion object {
        /** Watched fraction past which a video counts as completed. */
        const val COMPLETION_FRACTION = 0.9

        /**
         * Without an explicit size Kaltura serves its small default thumb, which gets upscaled
         * and blurry in the full-width continue-watching card; 1280 covers high-density screens.
         */
        const val THUMBNAIL_WIDTH_PX = 1280
    }
}
