package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.model.elearning.video.VideoStream
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watch progress for the recorded lectures. There is no stream behind them: the demo build
 * ships no video, so opening a recording fails the way an unreachable one would.
 */
@Singleton
class DemoVideoPlaybackRepository @Inject constructor() : VideoPlaybackRepository {

    private val progress = MutableStateFlow(seed())

    override suspend fun resolveStream(cmId: Int): VideoStream =
        error("The demo build has no lecture recordings to play.")

    override suspend fun thumbnailUrl(cmId: Int): String? = null

    override fun observeProgress(accountId: AccountId, cmId: Int): Flow<VideoProgress?> = progress.map { it[cmId] }

    override fun observeCourseProgress(accountId: AccountId, courseId: CourseId): Flow<Map<Int, VideoProgress>> =
        progress.map { all -> all.filterValues { it.courseId == courseId.value } }

    override suspend fun saveProgress(
        accountId: AccountId,
        courseId: CourseId,
        cmId: Int,
        positionMs: Long,
        durationMs: Long,
    ) {
        progress.update {
            it + (cmId to VideoProgress(cmId, courseId.value, positionMs, durationMs, false, DemoClock.fixed.instant()))
        }
    }

    override suspend fun markCompleted(accountId: AccountId, courseId: CourseId, cmId: Int) {
        progress.update { all ->
            val current = all[cmId] ?: VideoProgress(cmId, courseId.value, LECTURE_MS, LECTURE_MS, true, DemoClock.fixed.instant())
            all + (cmId to current.copy(completed = true, positionMs = current.durationMs))
        }
    }

    override suspend fun clearForAccount(accountId: AccountId) = Unit

    // First two recordings watched, the third left part-way, so "continue watching" has a target.
    private fun seed(): Map<Int, VideoProgress> = DemoCourses.all.flatMap { course ->
        listOf(1.0f, 1.0f, 0.4f).mapIndexed { week, fraction ->
            val cmId = DemoElearning.cmId(course, 10 + week * 10 + 1)
            VideoProgress(
                cmId = cmId,
                courseId = course.moodleCourseId,
                positionMs = (LECTURE_MS * fraction).toLong(),
                durationMs = LECTURE_MS,
                completed = fraction >= 1.0f,
                lastUpdatedAt = DemoClock.instant(DemoClock.now.minusDays(14L - week * 5)),
            )
        }
    }.associateBy { it.cmId }

    private companion object {
        const val LECTURE_MS = 95L * 60 * 1000
    }
}
