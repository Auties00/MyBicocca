package it.attendance100.mybicocca.domain.model.studyplan

import java.time.LocalDate

enum class StudyPlanType { Standard, Individual, Unknown }

// The student's chosen Esse3 study plan: header info plus the activities actually in
// plan (Esse3 returns the whole choice tree; only chosen activities are kept).
data class StudyPlan(
    val planId: Long,
    val studentId: Long,
    val type: StudyPlanType,
    val statusDescription: String?,
    val lastUpdated: LocalDate?,
    // Required to check the compilation window and to open the plan editor.
    val choiceRegulationId: Long?,
    val schemaId: Long?,
    val courses: List<StudyPlanCourse>,
)

data class StudyPlanCourse(
    val id: Long,
    val name: String,
    val code: String?,
    val credits: Float,
    val year: StudyYear,
)
