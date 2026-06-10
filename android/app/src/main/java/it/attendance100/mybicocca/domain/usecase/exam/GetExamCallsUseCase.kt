package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Loads the exam calls the student can book (bookable and future appelli) live from
 * Esse3 for the bookable-exams sub-screen in the registry tab. Call data is deliberately
 * not cached locally because seat counts and windows are volatile. Throws on failure.
 */
class GetExamCallsUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<ExamCall> =
        repository.getExamCalls(careerId)
}
