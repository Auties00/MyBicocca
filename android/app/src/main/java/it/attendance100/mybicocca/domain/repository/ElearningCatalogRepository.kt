package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog

/**
 * Access to the site-wide e-learning course catalog that drives the add-course
 * enrolment tree. The catalog is a pre-built snapshot bundled with the app, so loading
 * needs no network and no account.
 */
interface ElearningCatalogRepository {
    /**
     * Loads the full catalog; repeated calls return the same parsed instance. Throws
     * if the bundled index cannot be read or parsed.
     */
    suspend fun load(): ElearningCatalog
}
