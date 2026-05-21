package it.attendance100.mybicocca.domain.model.elearning.badge

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import java.time.Instant

data class Badge(
    val id: Int,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val issuedAt: Instant?,
    val courseId: CourseId?,
)
