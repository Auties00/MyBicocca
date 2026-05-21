package it.attendance100.mybicocca.domain.usecase.elearning.catalog

import it.attendance100.mybicocca.domain.model.elearning.catalog.CatalogSearchHit
import it.attendance100.mybicocca.domain.repository.ElearningCatalogRepository
import javax.inject.Inject

class SearchElearningCatalogUseCase @Inject constructor(
    private val repository: ElearningCatalogRepository,
) {
    suspend operator fun invoke(query: String, limit: Int = 80): List<CatalogSearchHit> =
        repository.search(query, limit)
}
