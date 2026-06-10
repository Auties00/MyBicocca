package it.attendance100.mybicocca.domain.usecase.studyplan

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject

/**
 * Loads the compilable rule set of a plan schema for the plan-edit wizard in the
 * registry tab: every rule with its selectable activities, pre-selecting mandatory ones
 * and those already in the student's plan when planId is non-null. Compiling against a
 * non-current schema is how the percorso is changed. Throws on network failure.
 */
class GetStudyPlanDraftUseCase @Inject constructor(
    private val repository: StudyPlanRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        planId: Long?,
        choiceRegulationId: Long,
        schemaId: Long,
    ): List<EditableRule> =
        repository.getStudyPlanDraft(careerId, planId, choiceRegulationId, schemaId)
}
