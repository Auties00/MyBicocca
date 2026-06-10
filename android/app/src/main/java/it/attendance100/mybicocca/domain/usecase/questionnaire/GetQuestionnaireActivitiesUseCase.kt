package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Loads the libretto activities that have VAL_DID evaluation questionnaires attached,
 * live from Esse3, for the questionnaires sub-screen in the registry tab. Throws on
 * failure.
 */
class GetQuestionnaireActivitiesUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<QuestionnaireActivity> =
        repository.getActivities(careerId)
}
