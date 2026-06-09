package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryAgreementsUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(libraryId: String): List<LibraryAgreement> =
        repository.getAgreements(libraryId)
}
