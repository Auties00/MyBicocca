package it.attendance100.mybicocca.domain.usecase.transcript

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.CourseDetail
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import javax.inject.Inject

/**
 * Loads the attempt history and propedeuticità outcome of a libretto activity live from
 * Esse3 when the student opens a course from the profile screen's course-detail
 * sub-screen. Passing alreadyPassed skips the prerequisite call, which Esse3 rejects
 * with 422 for passed activities. Throws when the attempt-history fetch fails.
 */
class GetCourseDetailUseCase @Inject constructor(
    private val repository: TranscriptRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        activityChoiceId: Long,
        alreadyPassed: Boolean,
    ): CourseDetail = repository.getCourseDetail(careerId, activityChoiceId, alreadyPassed)
}
