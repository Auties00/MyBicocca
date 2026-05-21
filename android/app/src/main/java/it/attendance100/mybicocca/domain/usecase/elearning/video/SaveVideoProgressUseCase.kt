package it.attendance100.mybicocca.domain.usecase.elearning.video

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.VideoPlaybackRepository
import javax.inject.Inject

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
