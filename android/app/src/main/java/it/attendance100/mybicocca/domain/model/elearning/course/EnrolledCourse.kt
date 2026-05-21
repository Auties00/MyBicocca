package it.attendance100.mybicocca.domain.model.elearning.course

import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import java.time.Instant
import java.time.LocalDate

data class EnrolledCourse(
    val id: CourseId,
    val shortName: String,
    val fullName: String,
    val displayName: String,
    val idNumber: String?,
    val summary: String?,
    val courseImageUrl: String?,
    val format: String?,
    val language: String?,
    val categoryId: Int?,
    val progress: Float?,
    val completed: Boolean,
    val completionEnabled: Boolean,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val lastAccessDate: Instant?,
    val isFavourite: Boolean,
    val hidden: Boolean,
    val deadlines: List<Deadline> = emptyList(),
)
