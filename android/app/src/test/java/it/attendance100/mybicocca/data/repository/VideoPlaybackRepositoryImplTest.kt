package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.data.auth.ElearningSession
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.video.KalturaEntryDao
import it.attendance100.mybicocca.data.local.elearning.video.KalturaEntryEntity
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressDao
import it.attendance100.mybicocca.data.local.elearning.video.VideoProgressEntity
import it.attendance100.mybicocca.data.elearning.ElearningApi
import it.attendance100.mybicocca.data.elearning.ElearningKaltura
import it.attendance100.mybicocca.data.elearning.ElearningKalturaEntryIdResponse
import it.attendance100.mybicocca.data.elearning.ElearningKalturaVideoStreamResponse
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.VideoUnavailableException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Behaviour coverage for the video playback repository: the one-shot completion semantics and
 * duration-fallback of saveProgress, the idempotent markCompleted, entry-id resolution with its
 * single reauth-retry and Room cache, and the mapping of withdrawn Kaltura entries.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VideoPlaybackRepositoryImplTest {

    private val accountId = AccountId("acc-1")
    private val courseId = CourseId(42)
    private val cmId = 555
    private val token = "x".repeat(32)

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val videoProgressDao = mockk<VideoProgressDao>(relaxed = true)
    private val kalturaEntryDao = mockk<KalturaEntryDao>(relaxed = true)
    private val courseRepository = mockk<ElearningCourseRepository>(relaxed = true)
    private val kalturaApi = mockk<ElearningKaltura>(relaxed = true)
    private val elearningApi = mockk<ElearningApi>(relaxed = true) {
        every { kaltura } returns kalturaApi
    }

    private fun newRepository(cachedEntryId: String? = null): VideoPlaybackRepositoryImpl {
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, token)
        coEvery { sessionManager.reauthElearning() } returns ElearningSession(elearningApi, token)
        coEvery { kalturaEntryDao.getEntryId(cmId) } returns cachedEntryId
        return VideoPlaybackRepositoryImpl(sessionManager, videoProgressDao, kalturaEntryDao, courseRepository)
    }

    private fun streamSuccess() = ElearningKalturaVideoStreamResponse.Success(
        kalturaEntryId = "1_entry",
        partnerId = 2351962,
        hlsStreamUrl = "https://cdn/hls.m3u8",
        dashStreamUrl = "https://cdn/manifest.mpd",
        availableVideoVariants = emptyList(),
    )

    @Test
    fun `resolveStream uses the cached entry id without loading view php`() = runTest {
        val repository = newRepository(cachedEntryId = "1_entry")
        coEvery { kalturaApi.getVideoStream("1_entry", any()) } returns streamSuccess()

        val stream = repository.resolveStream(cmId)

        assertThat(stream.cmId).isEqualTo(cmId)
        assertThat(stream.kalturaEntryId).isEqualTo("1_entry")
        assertThat(stream.hlsUrl).isEqualTo("https://cdn/hls.m3u8")
        coVerify(exactly = 0) { kalturaApi.resolveKalturaEntryIdForModule(any()) }
    }

    @Test
    fun `resolveStream resolves and persists a missing entry id`() = runTest {
        val repository = newRepository()
        coEvery { kalturaApi.resolveKalturaEntryIdForModule(cmId) } returns
            ElearningKalturaEntryIdResponse.Success("1_entry")
        coEvery { kalturaApi.getVideoStream("1_entry", any()) } returns streamSuccess()

        repository.resolveStream(cmId)

        val row = slot<KalturaEntryEntity>()
        coVerify { kalturaEntryDao.upsert(capture(row)) }
        assertThat(row.captured.cmId).isEqualTo(cmId)
        assertThat(row.captured.entryId).isEqualTo("1_entry")
        coVerify(exactly = 0) { sessionManager.reauthElearning() }
    }

    @Test
    fun `resolveStream reauths once then retries on RequiresReauth`() = runTest {
        val repository = newRepository()
        coEvery { kalturaApi.resolveKalturaEntryIdForModule(cmId) } returnsMany listOf(
            ElearningKalturaEntryIdResponse.RequiresReauth,
            ElearningKalturaEntryIdResponse.Success("1_entry"),
        )
        coEvery { kalturaApi.getVideoStream("1_entry", any()) } returns streamSuccess()

        val stream = repository.resolveStream(cmId)

        assertThat(stream.cmId).isEqualTo(cmId)
        coVerify(exactly = 1) { sessionManager.reauthElearning() }
        coVerify(exactly = 2) { kalturaApi.resolveKalturaEntryIdForModule(cmId) }
    }

    @Test
    fun `resolveStream throws when the retry still requires reauth`() = runTest {
        val repository = newRepository()
        coEvery { kalturaApi.resolveKalturaEntryIdForModule(cmId) } returns
            ElearningKalturaEntryIdResponse.RequiresReauth

        val thrown = repository.runCatching { resolveStream(cmId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        coVerify(exactly = 1) { sessionManager.reauthElearning() }
    }

    @Test
    fun `resolveStream throws VideoUnavailableException for a withdrawn entry`() = runTest {
        val repository = newRepository(cachedEntryId = "1_gone")
        coEvery { kalturaApi.getVideoStream("1_gone", any()) } returns
            ElearningKalturaVideoStreamResponse.Unavailable

        val thrown = repository.runCatching { resolveStream(cmId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(VideoUnavailableException::class.java)
    }

    @Test
    fun `thumbnailUrl builds the CDN url from the cached entry id`() = runTest {
        val repository = newRepository(cachedEntryId = "1_thumb")

        val url = repository.thumbnailUrl(cmId)

        assertThat(url).contains("1_thumb")
        assertThat(url).contains("/width/1280")
        coVerify(exactly = 0) { kalturaApi.resolveKalturaEntryIdForModule(any()) }
    }

    @Test
    fun `thumbnailUrl returns null when reauth retry still fails`() = runTest {
        val repository = newRepository()
        coEvery { kalturaApi.resolveKalturaEntryIdForModule(cmId) } returns
            ElearningKalturaEntryIdResponse.RequiresReauth

        val url = repository.thumbnailUrl(cmId)

        assertThat(url).isNull()
        coVerify(exactly = 1) { sessionManager.reauthElearning() }
    }

    @Test
    fun `saveProgress flags completion once past ninety percent and mirrors it`() = runTest {
        val repository = newRepository()
        coEvery { videoProgressDao.getOnce(accountId.value, cmId) } returns null

        repository.saveProgress(accountId, courseId, cmId, positionMs = 95_000L, durationMs = 100_000L)

        val row = slot<VideoProgressEntity>()
        coVerify { videoProgressDao.upsert(capture(row)) }
        assertThat(row.captured.completed).isTrue()
        assertThat(row.captured.durationMs).isEqualTo(100_000L)
        coVerify { courseRepository.setActivityCompleted(accountId, courseId, cmId, completed = true) }
    }

    @Test
    fun `saveProgress below the threshold does not complete or mirror`() = runTest {
        val repository = newRepository()
        coEvery { videoProgressDao.getOnce(accountId.value, cmId) } returns null

        repository.saveProgress(accountId, courseId, cmId, positionMs = 10_000L, durationMs = 100_000L)

        val row = slot<VideoProgressEntity>()
        coVerify { videoProgressDao.upsert(capture(row)) }
        assertThat(row.captured.completed).isFalse()
        coVerify(exactly = 0) { courseRepository.setActivityCompleted(any(), any(), any(), any()) }
    }

    @Test
    fun `saveProgress falls back to the stored duration when the incoming one is non-positive`() = runTest {
        val repository = newRepository()
        coEvery { videoProgressDao.getOnce(accountId.value, cmId) } returns VideoProgressEntity(
            accountId = accountId.value,
            cmId = cmId,
            courseId = courseId.value,
            positionMs = 5_000L,
            durationMs = 120_000L,
            completed = false,
            lastUpdatedAtMs = 0L,
        )

        repository.saveProgress(accountId, courseId, cmId, positionMs = 6_000L, durationMs = 0L)

        val row = slot<VideoProgressEntity>()
        coVerify { videoProgressDao.upsert(capture(row)) }
        assertThat(row.captured.durationMs).isEqualTo(120_000L)
    }

    @Test
    fun `saveProgress keeps completion one-way once already completed`() = runTest {
        val repository = newRepository()
        coEvery { videoProgressDao.getOnce(accountId.value, cmId) } returns VideoProgressEntity(
            accountId = accountId.value,
            cmId = cmId,
            courseId = courseId.value,
            positionMs = 95_000L,
            durationMs = 100_000L,
            completed = true,
            lastUpdatedAtMs = 0L,
        )

        repository.saveProgress(accountId, courseId, cmId, positionMs = 1_000L, durationMs = 100_000L)

        val row = slot<VideoProgressEntity>()
        coVerify { videoProgressDao.upsert(capture(row)) }
        assertThat(row.captured.completed).isTrue()
        coVerify(exactly = 0) { courseRepository.setActivityCompleted(any(), any(), any(), any()) }
    }

    @Test
    fun `saveProgress clamps a negative incoming position to zero`() = runTest {
        val repository = newRepository()
        coEvery { videoProgressDao.getOnce(accountId.value, cmId) } returns null

        repository.saveProgress(accountId, courseId, cmId, positionMs = -100L, durationMs = 100_000L)

        val row = slot<VideoProgressEntity>()
        coVerify { videoProgressDao.upsert(capture(row)) }
        assertThat(row.captured.positionMs).isEqualTo(0L)
    }

    @Test
    fun `markCompleted short-circuits when already completed`() = runTest {
        val repository = newRepository()
        coEvery { videoProgressDao.getOnce(accountId.value, cmId) } returns VideoProgressEntity(
            accountId = accountId.value,
            cmId = cmId,
            courseId = courseId.value,
            positionMs = 0L,
            durationMs = 0L,
            completed = true,
            lastUpdatedAtMs = 0L,
        )

        repository.markCompleted(accountId, courseId, cmId)

        coVerify(exactly = 0) { videoProgressDao.upsert(any()) }
        coVerify(exactly = 0) { courseRepository.setActivityCompleted(any(), any(), any(), any()) }
    }

    @Test
    fun `markCompleted writes a completed row and mirrors when not yet completed`() = runTest {
        val repository = newRepository()
        coEvery { videoProgressDao.getOnce(accountId.value, cmId) } returns null

        repository.markCompleted(accountId, courseId, cmId)

        val row = slot<VideoProgressEntity>()
        coVerify { videoProgressDao.upsert(capture(row)) }
        assertThat(row.captured.completed).isTrue()
        coVerify { courseRepository.setActivityCompleted(accountId, courseId, cmId, completed = true) }
    }

    @Test
    fun `saveProgress completion mirror failure does not break the local write`() = runTest {
        val repository = newRepository()
        coEvery { videoProgressDao.getOnce(accountId.value, cmId) } returns null
        coEvery { courseRepository.setActivityCompleted(any(), any(), any(), any()) } throws RuntimeException("offline")

        repository.saveProgress(accountId, courseId, cmId, positionMs = 95_000L, durationMs = 100_000L)

        coVerify { videoProgressDao.upsert(any()) }
    }

    @Test
    fun `clearForAccount delegates to the DAO`() = runTest {
        val repository = newRepository()

        repository.clearForAccount(accountId)

        coVerify { videoProgressDao.deleteForAccount(accountId.value) }
    }
}
