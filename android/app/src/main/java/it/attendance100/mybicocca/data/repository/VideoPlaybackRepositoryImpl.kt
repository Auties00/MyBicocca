package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressDao
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressEntity
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningKalturaApi
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaEntryIdResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningKalturaVideoStreamResponse
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlaybackRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val videoProgressDao: VideoProgressDao,
    private val elearningCourseRepository: ElearningCourseRepository,
) : VideoPlaybackRepository {

    override suspend fun resolveStream(cmId: Int): VideoStream {
        val (api, _) = sessionManager.elearning()
        when (val result = api.kaltura.resolveVideoStreamForModule(cmId)) {
            is ElearningKalturaVideoStreamResponse.Success -> return result.toDomain(cmId)
            ElearningKalturaVideoStreamResponse.RequiresReauth -> Unit // fall through to reauth + retry
        }

        sessionManager.reauthElearning()
        return when (val retry = sessionManager.elearning().api.kaltura.resolveVideoStreamForModule(cmId)) {
            is ElearningKalturaVideoStreamResponse.Success -> retry.toDomain(cmId)
            ElearningKalturaVideoStreamResponse.RequiresReauth -> throw ElearningException(
                errorCode = null,
                errorMessage = "Could not resolve video stream for cmId=$cmId after reauth",
            )
        }
    }

    private val entryIdCacheMutex = Mutex()
    private val entryIdByCmId = mutableMapOf<Int, String>()

    override suspend fun thumbnailUrl(cmId: Int): String? {
        val cached = entryIdCacheMutex.withLock { entryIdByCmId[cmId] }
        if (cached != null) return ElearningKalturaApi.thumbnailUrl(cached, widthPx = THUMBNAIL_WIDTH_PX)
        val (api, _) = sessionManager.elearning()
        val entryId = when (val response = api.kaltura.resolveKalturaEntryIdForModule(cmId)) {
            is ElearningKalturaEntryIdResponse.Success -> response.kalturaEntryId
            ElearningKalturaEntryIdResponse.RequiresReauth -> {
                sessionManager.reauthElearning()
                when (val retry = sessionManager.elearning().api.kaltura.resolveKalturaEntryIdForModule(cmId)) {
                    is ElearningKalturaEntryIdResponse.Success -> retry.kalturaEntryId
                    ElearningKalturaEntryIdResponse.RequiresReauth -> return null
                }
            }
        }
        entryIdCacheMutex.withLock { entryIdByCmId[cmId] = entryId }
        return ElearningKalturaApi.thumbnailUrl(entryId, widthPx = THUMBNAIL_WIDTH_PX)
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

    override suspend fun saveProgress(
        accountId: AccountId,
        courseId: CourseId,
        cmId: Int,
        positionMs: Long,
        durationMs: Long,
    ) {
        val existing = videoProgressDao.getOnce(accountId.value, cmId)
        val alreadyCompleted = existing?.completed == true
        // Player.duration is C.TIME_UNSET until the manifest fully loads; ticking saves
        // would otherwise reset a known duration back to 0.
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

    // Best-effort: a Moodle completion failure must not break local playback state.
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
        const val COMPLETION_FRACTION = 0.9

        // Without an explicit size Kaltura serves its small default thumb, which gets upscaled
        // and blurry in the full-width continue-watching card; 1280 covers high-density screens.
        const val THUMBNAIL_WIDTH_PX = 1280
    }
}
