package it.attendance100.mybicocca.domain.usecase.studyplan

import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject

class IsStudyPlanEditableUseCase @Inject constructor(
    private val repository: StudyPlanRepository,
) {
    suspend operator fun invoke(choiceRegulationId: Long): Boolean =
        repository.isPlanEditingOpen(choiceRegulationId)
}
