package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

class RejectExamResultUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId, applicationListId: Long) =
        repository.acknowledgeExamResult(careerId, applicationListId, accept = false)
}
