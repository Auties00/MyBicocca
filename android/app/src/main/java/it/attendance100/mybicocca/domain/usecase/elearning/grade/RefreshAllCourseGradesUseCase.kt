package it.attendance100.mybicocca.domain.usecase.elearning.grade

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.repository.ElearningGradeRepository
import javax.inject.Inject

class RefreshAllCourseGradesUseCase @Inject constructor(
    private val repository: ElearningGradeRepository,
) {
    suspend operator fun invoke(accountId: AccountId, force: Boolean = false) =
        repository.refreshAllCourseGrades(accountId, force)
}
