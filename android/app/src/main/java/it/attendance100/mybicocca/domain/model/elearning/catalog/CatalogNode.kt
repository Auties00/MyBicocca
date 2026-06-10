package it.attendance100.mybicocca.domain.model.elearning.catalog

/**
 * One Moodle category in the e-learning catalog tree (degree programme, year of study,
 * curriculum, ...). Rendered as an expandable branch of the add-course enrolment tree.
 *
 * @property id Synthetic stable id built by chaining ancestor names; category URLs are
 * not always indexed and the same name recurs under different ancestors (e.g.
 * "1° anno" under each programme).
 * @property name Category display name.
 * @property url The category page on the Moodle site, when indexed.
 * @property children Nested sub-categories.
 * @property courses Courses listed directly under this category.
 */
data class CatalogNode(
    val id: String,
    val name: String,
    val url: String?,
    val children: List<CatalogNode>,
    val courses: List<CatalogCourse>,
)
