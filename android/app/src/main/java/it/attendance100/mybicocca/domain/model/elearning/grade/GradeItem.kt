package it.attendance100.mybicocca.domain.model.elearning.grade

import java.time.Instant

/**
 * One row of a course's Moodle gradebook, from the grade-items web service. Rendered
 * in the course detail grades list.
 *
 * @property id The gradebook item id.
 * @property name Item title; falls back to the activity module name when the
 * gradebook leaves it blank.
 * @property type What the row grades (a single activity, a category aggregate, the
 * course total, or a manual item).
 * @property activityType The Moodle module name behind activity rows ("assign",
 * "quiz", ...), null for non-activity rows.
 * @property grade The numeric grade, or null when ungraded or hidden.
 * @property maxGrade Maximum achievable grade for the item.
 * @property percentage Grade as a percentage in 0..100, when the gradebook reports one.
 * @property gradeFormatted The grade exactly as the gradebook formats it (handles
 * scales and letter grades).
 * @property feedback Teacher feedback HTML, when given.
 * @property gradedAt When the grade was recorded, when reported.
 */
data class GradeItem(
    val id: Long,
    val name: String,
    val type: GradeItemType,
    val activityType: String?,
    val grade: Double?,
    val maxGrade: Double?,
    val percentage: Double?,
    val gradeFormatted: String?,
    val feedback: String?,
    val gradedAt: Instant?,
)
