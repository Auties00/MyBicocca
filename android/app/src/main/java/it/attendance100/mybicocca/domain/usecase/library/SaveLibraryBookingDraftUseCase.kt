package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import javax.inject.Inject

/** Persists the current booking wizard selections before e-mail verification. */
class SaveLibraryBookingDraftUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(draft: LibraryBookingDraft) = repository.saveBookingDraft(draft)
}
