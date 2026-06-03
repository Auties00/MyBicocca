package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

class GetQuestionnaireActivitiesUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<QuestionnaireActivity> =
        repository.getActivities(careerId)
}
