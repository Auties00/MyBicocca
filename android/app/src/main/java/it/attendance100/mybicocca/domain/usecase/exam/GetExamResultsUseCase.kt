package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Loads the student's published exam outcomes (the "Bacheca Esiti") live from Esse3 for
 * the exam-results sheet in the registry tab, where grades can be reviewed and
 * accepted/rejected while their decision window is open. Throws on failure.
 */
class GetExamResultsUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<ExamResult> =
        repository.getExamResults(careerId)
}
