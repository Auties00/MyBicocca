package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

class ConfirmLibraryEmailValidationUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(email: String, requestUuid: String): Boolean =
        repository.confirmEmailValidation(email, requestUuid)
}
