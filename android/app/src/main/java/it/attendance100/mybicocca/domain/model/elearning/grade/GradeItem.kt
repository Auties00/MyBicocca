package it.attendance100.mybicocca.domain.model.elearning.grade

import java.time.Instant

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
