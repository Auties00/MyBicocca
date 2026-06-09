package it.attendance100.mybicocca.domain.usecase.library

import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLibraryLinkedEmailUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<String?> = repository.observeLinkedEmail()
}
