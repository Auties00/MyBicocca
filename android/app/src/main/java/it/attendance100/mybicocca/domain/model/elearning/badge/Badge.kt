package it.attendance100.mybicocca.domain.model.elearning.badge

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import java.time.Instant

/**
 * An achievement badge awarded to the student on Moodle, from the user-badges web
 * service. Shown in the e-learning badge gallery and on the awarding course's detail.
 *
 * @property id The Moodle badge id.
 * @property name Badge title.
 * @property description What the badge was awarded for, when authored.
 * @property imageUrl The badge artwork served by Moodle.
 * @property issuedAt When the badge was awarded, when reported.
 * @property courseId The awarding course, or null for site-wide badges.
 */
data class Badge(
    val id: Int,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val issuedAt: Instant?,
    val courseId: CourseId?,
)
