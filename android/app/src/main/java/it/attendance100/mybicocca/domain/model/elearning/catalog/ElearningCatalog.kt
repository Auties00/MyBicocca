package it.attendance100.mybicocca.domain.model.elearning.catalog

/**
 * The full e-learning course catalog: a pre-built snapshot of the Moodle category tree
 * bundled with the app. Drives the add-course enrolment browser.
 *
 * @property sections Top-level areas of the catalog (e.g. schools/departments), each
 * holding its own category tree.
 */
data class ElearningCatalog(
    val sections: List<CatalogSection>,
)
