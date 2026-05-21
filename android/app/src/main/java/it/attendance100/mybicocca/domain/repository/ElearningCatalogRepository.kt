package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSearchHit
import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog

interface ElearningCatalogRepository {
    suspend fun load(): ElearningCatalog

    suspend fun search(query: String, limit: Int = 80): List<CatalogSearchHit>
}
