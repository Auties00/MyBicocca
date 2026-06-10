package it.attendance100.mybicocca.domain.usecase.document

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.StudentBadge
import it.attendance100.mybicocca.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Loads the career's university card ("tessera") metadata from Esse3, or null when no card has
 * been issued. Fetches live — never cached.
 */
class GetStudentBadgeUseCase @Inject constructor(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(careerId: CareerId): StudentBadge? =
        repository.getBadge(careerId)
}
