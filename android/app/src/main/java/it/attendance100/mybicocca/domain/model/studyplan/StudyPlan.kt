package it.attendance100.mybicocca.domain.model.studyplan

import java.time.LocalDate

/**
 * Esse3 plan kind (`tipoPiano`): [Standard] plans ("S") hang off a choice regulation and
 * are the only ones compilable in the app; [Individual] plans are hand-built by the
 * segreteria and carry no percorso choice. [Unknown] covers any unrecognised code.
 */
enum class StudyPlanType { Standard, Individual, Unknown }

/**
 * The student's chosen Esse3 study plan: header info plus the activities actually in
 * plan (Esse3 returns the whole choice tree; only chosen activities are kept). Rendered
 * by the study-plan sub-screen in the registry tab.
 *
 * @property planId Esse3 id of the plan.
 * @property studentId The career's `stuId` the plan belongs to.
 * @property type Standard or individual plan.
 * @property statusDescription Human description of the plan's approval state.
 * @property lastUpdated Day the plan last changed state.
 * @property choiceRegulationId Regulation the plan was compiled against; required to
 *   check the compilation window and to open the plan editor.
 * @property schemaId Plan schema the plan was compiled against.
 * @property courses Activities the student actually has in plan, by year.
 */
data class StudyPlan(
    val planId: Long,
    val studentId: Long,
    val type: StudyPlanType,
    val statusDescription: String?,
    val lastUpdated: LocalDate?,
    val choiceRegulationId: Long?,
    val schemaId: Long?,
    val courses: List<StudyPlanCourse>,
)

/**
 * One activity inside the student's study plan.
 *
 * @property id Esse3 choice id of the in-plan activity.
 * @property name Name of the teaching activity.
 * @property code University activity code, when the plan row carries one.
 * @property credits CFU weight of the activity.
 * @property year Year of the course the activity is planned in.
 */
data class StudyPlanCourse(
    val id: Long,
    val name: String,
    val code: String?,
    val credits: Float,
    val year: StudyYear,
)
