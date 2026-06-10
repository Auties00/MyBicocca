package it.attendance100.mybicocca.domain.usecase.studyplan

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject

/**
 * Loads the student's study plan (approved if present, else the most recent) with its
 * in-plan activities, live from Esse3, for the study-plan sub-screen in the registry
 * tab. Returns null when the career has no plan; throws on network failure.
 */
class GetStudyPlanUseCase @Inject constructor(
    private val repository: StudyPlanRepository,
) {
    suspend operator fun invoke(careerId: CareerId): StudyPlan? =
        repository.getStudyPlan(careerId)
}
