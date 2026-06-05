package it.attendance100.mybicocca.domain.usecase.degreeAward

import it.attendance100.mybicocca.domain.model.degreeaward.ThesisId
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import javax.inject.Inject

class SetThesisDiscussionModeUseCase @Inject constructor(
    private val repository: DegreeAwardRepository,
) {
    suspend operator fun invoke(thesisId: ThesisId, discussionModeCode: String) =
        repository.setDiscussionMode(thesisId, discussionModeCode)
}
