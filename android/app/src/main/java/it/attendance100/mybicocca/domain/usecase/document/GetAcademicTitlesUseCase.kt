package it.attendance100.mybicocca.domain.usecase.document

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Loads the student's qualifications across all Esse3 title families when the registry
 * "Titoli" sub-screen opens or is refreshed. Fetches live from Esse3 — never cached.
 */
class GetAcademicTitlesUseCase @Inject constructor(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<AcademicTitle> =
        repository.getTitles(careerId)
}
