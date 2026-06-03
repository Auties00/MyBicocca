package it.attendance100.mybicocca.domain.usecase.studyplan

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject

class GetStudyPlanPrintUseCase @Inject constructor(
    private val repository: StudyPlanRepository,
) {
    suspend operator fun invoke(careerId: CareerId, planId: Long): ByteArray =
        repository.getStudyPlanPrint(careerId, planId)
}
