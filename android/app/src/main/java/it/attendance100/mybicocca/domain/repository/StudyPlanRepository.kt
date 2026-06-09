package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan

interface StudyPlanRepository {
    suspend fun getPlannedCoursesForActiveCareer(careerId: CareerId): List<PlannedCourse>

    // The student's chosen plan (approved if present, else the most recent) with its
    // in-plan activities, or null when the career has no plan. Throws on network failure.
    suspend fun getStudyPlan(careerId: CareerId): StudyPlan?

    // The student's path configuration (percorso / orientamento / profilo / part-time)
    // plus any selectable alternatives offered by the plan's choice regulation, or null
    // when the career has no standard plan. Carries the compilation-window state too
    // (editingOpen), so callers don't need a separate windows lookup. Throws on network
    // failure.
    suspend fun getStudyPath(careerId: CareerId): StudyPath?

    // The official plan PDF ("stampa piano") fully read into memory.
    suspend fun getStudyPlanPrint(careerId: CareerId, planId: Long): ByteArray

    // The compilable schema for the plan editor: every rule with its selectable
    // activities, pre-selecting mandatory ones and those already in the student's plan
    // (when planId is non-null). Throws on network failure.
    suspend fun getStudyPlanDraft(
        careerId: CareerId,
        planId: Long?,
        choiceRegulationId: Long,
        schemaId: Long,
    ): List<EditableRule>

    // Submits the selected activities as a new proposed plan (replacing the current
    // valid one). chosenPath is the schema option the rules were compiled against: its
    // percorso is recorded on the submission when it differs from the student's current
    // one, and its approval flavour always rides along (Esse3 requires tipoRegsce).
    // Throws on failure.
    suspend fun submitStudyPlan(
        careerId: CareerId,
        rules: List<EditableRule>,
        chosenPath: StudyPathOption?,
    )
}
