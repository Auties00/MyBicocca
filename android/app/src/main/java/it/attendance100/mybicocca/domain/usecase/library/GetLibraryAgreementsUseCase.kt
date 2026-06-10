package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/**
 * Loads the library's terms-of-use agreements shown for consent in the summary step of the
 * Biblioteca booking wizard.
 */
class GetLibraryAgreementsUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String): List<LibraryAgreement> =
        repository.getAgreements(libraryId)
}
