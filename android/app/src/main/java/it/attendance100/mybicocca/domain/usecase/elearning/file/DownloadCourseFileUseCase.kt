package it.attendance100.mybicocca.domain.usecase.elearning.file

import it.attendance100.mybicocca.domain.repository.ElearningFileRepository
import javax.inject.Inject

/**
 * Downloads a course file when the user opens it from a module row or the in-app file
 * viewer, returning the absolute path of the local copy. Repeated opens of the same
 * file are served from the download cache without touching the network; throws on
 * network or authentication failure.
 */
class DownloadCourseFileUseCase @Inject constructor(
    private val repository: ElearningFileRepository,
) {
    suspend operator fun invoke(fileUrl: String, fileName: String): String =
        repository.downloadFile(fileUrl, fileName)
}
