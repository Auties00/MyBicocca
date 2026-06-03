package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

interface StudyPlanRepository {
    suspend fun getPlannedCoursesForActiveCareer(careerId: CareerId): List<PlannedCourse>
    suspend fun getActivityYearByCodeForCareer(careerId: CareerId): Map<String, StudyYear>

    // The student's chosen plan (approved if present, else the most recent) with its
    // in-plan activities, or null when the career has no plan. Throws on network failure.
    suspend fun getStudyPlan(careerId: CareerId): StudyPlan?

    // Whether the plan-compilation window for the given choice regulation is open today.
    suspend fun isPlanEditingOpen(choiceRegulationId: Long): Boolean

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
    // valid one). Throws on failure.
    suspend fun submitStudyPlan(careerId: CareerId, rules: List<EditableRule>)
}
