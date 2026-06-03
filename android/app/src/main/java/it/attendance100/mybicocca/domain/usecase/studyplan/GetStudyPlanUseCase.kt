package it.attendance100.mybicocca.domain.usecase.studyplan

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject

class GetStudyPlanUseCase @Inject constructor(
    private val repository: StudyPlanRepository,
) {
    suspend operator fun invoke(careerId: CareerId): StudyPlan? =
        repository.getStudyPlan(careerId)
}
