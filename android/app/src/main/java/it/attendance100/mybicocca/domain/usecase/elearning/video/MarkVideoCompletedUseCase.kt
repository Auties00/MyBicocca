package it.attendance100.mybicocca.domain.usecase.elearning.video

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import javax.inject.Inject

/**
 * Marks a video as fully watched — for example when playback reaches the end — persisting the
 * flag locally and mirroring the completion to the e-learning platform.
 */
class MarkVideoCompletedUseCase @Inject constructor(
    private val repository: VideoPlaybackRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, cmId: Int) =
        repository.markCompleted(accountId, courseId, cmId)
}
