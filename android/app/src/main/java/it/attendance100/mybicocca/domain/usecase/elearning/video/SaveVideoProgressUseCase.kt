package it.attendance100.mybicocca.domain.usecase.elearning.video

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import javax.inject.Inject

/**
 * Persists the current playback position as the video player runs and when it pauses or
 * closes. Once playback passes the completion threshold the video is flagged watched and the
 * completion is mirrored to the e-learning platform.
 */
class SaveVideoProgressUseCase @Inject constructor(
    private val repository: VideoPlaybackRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        courseId: CourseId,
        cmId: Int,
        positionMs: Long,
        durationMs: Long,
    ) = repository.saveProgress(accountId, courseId, cmId, positionMs, durationMs)
}
