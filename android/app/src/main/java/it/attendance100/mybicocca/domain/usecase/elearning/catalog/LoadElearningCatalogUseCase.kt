package it.attendance100.mybicocca.domain.usecase.elearning.catalog

import it.attendance100.mybicocca.domain.model.elearning.catalog.ElearningCatalog
import it.attendance100.mybicocca.domain.repository.ElearningCatalogRepository
import javax.inject.Inject

/**
 * Loads the bundled e-learning course catalog when the add-course modal opens, giving
 * the enrolment browser its category tree. Throws if the bundled index cannot be read,
 * which the modal surfaces as an in-place error with retry.
 */
class LoadElearningCatalogUseCase @Inject constructor(
    private val repository: ElearningCatalogRepository,
) {
    suspend operator fun invoke(): ElearningCatalog = repository.load()
}
