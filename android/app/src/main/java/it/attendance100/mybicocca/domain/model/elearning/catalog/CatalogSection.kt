package it.attendance100.mybicocca.domain.model.elearning.catalog

/**
 * A top-level area of the e-learning catalog grouping related category trees, shown as
 * a sticky group label in the add-course enrolment tree.
 *
 * @property name Area display name.
 * @property nodes The area's root categories.
 */
data class CatalogSection(
    val name: String,
    val nodes: List<CatalogNode>,
)
