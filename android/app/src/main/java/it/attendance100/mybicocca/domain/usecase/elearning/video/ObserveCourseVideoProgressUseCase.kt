package it.attendance100.mybicocca.domain.usecase.elearning.video

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.video.VideoProgress
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams all saved video progress of a course keyed by course-module id, backing the
 * continue-watching card and the per-video progress indicators of the course detail screen.
 */
class ObserveCourseVideoProgressUseCase @Inject constructor(
    private val repository: VideoPlaybackRepository,
) {
    operator fun invoke(accountId: AccountId, courseId: CourseId): Flow<Map<Int, VideoProgress>> =
        repository.observeCourseProgress(accountId, courseId)
}
