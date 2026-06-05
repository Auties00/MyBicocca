package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import javax.inject.Inject

class GetCourseDetailUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        activityChoiceId: Long,
        alreadyPassed: Boolean,
    ): CourseDetail = repository.getCourseDetail(careerId, activityChoiceId, alreadyPassed)
}
