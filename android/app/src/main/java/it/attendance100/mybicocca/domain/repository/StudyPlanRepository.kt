package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan

/**
 * The student's study plan: in-plan activities, path configuration and the compilation
 * (plan-edit) flow.
 *
 * Plan data is NOT persisted in Room: reads hit Esse3 (and EasyStaff for
 * [getPlannedCoursesForActiveCareer]) live, with only a short-lived in-memory cache on
 * the planned-courses intersection that other repositories poll frequently. All methods
 * throw on network failure unless noted.
 */
interface StudyPlanRepository {
    /**
     * The Esse3 plan activities matched to their EasyStaff timetable subjects, the
     * course set the calendar and attendance features operate on. Returns an empty list
     * when the career has no plan or no EasyStaff program; failures degrade to an empty
     * list rather than throwing.
     */
    suspend fun getPlannedCoursesForActiveCareer(careerId: CareerId): List<PlannedCourse>

    /**
     * The student's chosen plan (approved if present, else the most recent) with its
     * in-plan activities, or null when the career has no plan.
     */
    suspend fun getStudyPlan(careerId: CareerId): StudyPlan?

    /**
     * The student's path configuration (percorso / orientamento / profilo / part-time)
     * plus any selectable alternatives offered by the plan's choice regulation, or null
     * when the career has no standard plan. Carries the compilation-window state too
     * (editingOpen), so callers need no separate windows lookup.
     */
    suspend fun getStudyPath(careerId: CareerId): StudyPath?

    /** The official plan PDF ("stampa piano") fully read into memory. */
    suspend fun getStudyPlanPrint(careerId: CareerId, planId: Long): ByteArray

    /**
     * The compilable schema for the plan editor: every rule with its selectable
     * activities, pre-selecting mandatory ones and those already in the student's plan
     * (when planId is non-null).
     */
    suspend fun getStudyPlanDraft(
        careerId: CareerId,
        planId: Long?,
        choiceRegulationId: Long,
        schemaId: Long,
    ): List<EditableRule>

    /**
     * Submits the selected activities as a new proposed plan (replacing the current
     * valid one). chosenPath is the schema option the rules were compiled against: its
     * percorso is recorded on the submission when it differs from the student's current
     * one, and its approval flavour always rides along (Esse3 requires `tipoRegsce`).
     */
    suspend fun submitStudyPlan(
        careerId: CareerId,
        rules: List<EditableRule>,
        chosenPath: StudyPathOption?,
    )
}
