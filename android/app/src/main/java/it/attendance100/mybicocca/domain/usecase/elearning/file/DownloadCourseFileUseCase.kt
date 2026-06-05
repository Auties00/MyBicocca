package it.attendance100.mybicocca.domain.usecase.elearning.file

import it.attendance100.mybicocca.domain.repository.ElearningFileRepository
import javax.inject.Inject

class DownloadCourseFileUseCase @Inject constructor(
    private val repository: ElearningFileRepository,
) {
    suspend operator fun invoke(fileUrl: String, fileName: String): String =
        repository.downloadFile(fileUrl, fileName)
}
