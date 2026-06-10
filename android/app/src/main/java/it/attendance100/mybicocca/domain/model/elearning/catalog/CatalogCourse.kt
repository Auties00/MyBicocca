package it.attendance100.mybicocca.domain.model.elearning.catalog

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId

/**
 * A course offered in the site-wide e-learning catalog, regardless of the student's
 * enrolment. Listed as a selectable leaf in the add-course enrolment tree.
 *
 * @property id The Moodle course id, used to self-enrol.
 * @property name Course display name.
 * @property code The course's idNumber-style code as indexed in the catalog.
 * @property url The course page on the Moodle site.
 */
data class CatalogCourse(
    val id: CourseId,
    val name: String,
    val code: String,
    val url: String,
)
