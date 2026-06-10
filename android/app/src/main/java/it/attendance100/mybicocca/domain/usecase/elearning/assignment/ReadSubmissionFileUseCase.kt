package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFile
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import javax.inject.Inject

/**
 * Reads a picked file's full content into memory as a [SubmissionFile], ready for upload.
 * Invoked at save time for each picked attachment of an assignment submission.
 */
class ReadSubmissionFileUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    suspend operator fun invoke(uri: String): SubmissionFile = repository.readSubmissionFile(uri)
}
