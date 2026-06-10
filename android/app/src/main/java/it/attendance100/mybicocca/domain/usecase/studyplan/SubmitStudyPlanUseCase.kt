package it.attendance100.mybicocca.domain.usecase.studyplan

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import javax.inject.Inject

/**
 * Submits the activities selected in the plan-edit wizard as a new proposed plan,
 * replacing the student's currently valid one — the confirm action of the wizard in the
 * registry tab. chosenPath is the schema option the rules were compiled against; passing
 * a non-current one records a percorso change on the submission. Throws on failure.
 */
class SubmitStudyPlanUseCase @Inject constructor(
    private val repository: StudyPlanRepository,
) {
    suspend operator fun invoke(
        careerId: CareerId,
        rules: List<EditableRule>,
        chosenPath: StudyPathOption? = null,
    ) = repository.submitStudyPlan(careerId, rules, chosenPath)
}
