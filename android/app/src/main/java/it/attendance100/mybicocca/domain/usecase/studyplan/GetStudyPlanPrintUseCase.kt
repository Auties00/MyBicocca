package it.attendance100.mybicocca.domain.usecase.studyplan

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject

/**
 * Downloads the official study-plan PDF ("stampa piano") for the student's plan,
 * triggered from the study-plan sub-screen in the registry tab. Returns the raw PDF
 * bytes and throws on failure.
 */
class GetStudyPlanPrintUseCase @Inject constructor(
    private val repository: StudyPlanRepository,
) {
    suspend operator fun invoke(careerId: CareerId, planId: Long): ByteArray =
        repository.getStudyPlanPrint(careerId, planId)
}
