package it.attendance100.mybicocca.domain.usecase.elearning.catalog

import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog
import it.attendance100.mybicocca.domain.repository.ElearningCatalogRepository
import javax.inject.Inject

class LoadElearningCatalogUseCase @Inject constructor(
    private val repository: ElearningCatalogRepository,
) {
    suspend operator fun invoke(): ElearningCatalog = repository.load()
}
