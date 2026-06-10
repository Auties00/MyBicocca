package it.attendance100.mybicocca.domain.usecase.elearning.file

import it.attendance100.mybicocca.domain.repository.ElearningFileRepository
import javax.inject.Inject

/**
 * Produces a directly-fetchable URL for a course file by appending the Moodle
 * web-service token, used when streaming to a media player or handing the file to an
 * external app without downloading it first.
 */
class GetAuthenticatedFileUrlUseCase @Inject constructor(
    private val repository: ElearningFileRepository,
) {
    suspend operator fun invoke(fileUrl: String): String =
        repository.authenticatedFileUrl(fileUrl)
}
