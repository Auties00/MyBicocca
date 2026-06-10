package it.attendance100.mybicocca.domain.usecase.questionnaire

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.questionnaire.ActivityQuestionnaires
import it.attendance100.mybicocca.domain.repository.QuestionnaireRepository
import javax.inject.Inject

/**
 * Loads the compilable questionnaire units (teaching unit / lecturer / partition) of a
 * libretto activity live from Esse3, shown when the student picks a course in the
 * questionnaires sub-screen of the registry tab. Throws on failure.
 */
class GetActivityQuestionnairesUseCase @Inject constructor(
    private val repository: QuestionnaireRepository,
) {
    suspend operator fun invoke(careerId: CareerId, activityChoiceId: Long): ActivityQuestionnaires =
        repository.getActivityQuestionnaires(careerId, activityChoiceId)
}
