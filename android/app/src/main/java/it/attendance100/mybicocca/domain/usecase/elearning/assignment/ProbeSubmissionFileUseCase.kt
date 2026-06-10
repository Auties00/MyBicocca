package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFileMetadata
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import javax.inject.Inject

/**
 * Resolves display metadata for a file picked in the consegna editor — a cheap provider query
 * with no content read, so picked files can be listed instantly.
 */
class ProbeSubmissionFileUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    suspend operator fun invoke(uri: String): SubmissionFileMetadata = repository.probeSubmissionFile(uri)
}
