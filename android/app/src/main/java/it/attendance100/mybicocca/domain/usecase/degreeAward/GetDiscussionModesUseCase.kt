package it.attendance100.mybicocca.domain.usecase.degreeAward

import it.attendance100.mybicocca.domain.model.degreeaward.DiscussionMode
import it.attendance100.mybicocca.domain.repository.DegreeAwardRepository
import javax.inject.Inject

class GetDiscussionModesUseCase @Inject constructor(
    private val repository: DegreeAwardRepository,
) {
    suspend operator fun invoke(): List<DiscussionMode> = repository.getDiscussionModes()
}
