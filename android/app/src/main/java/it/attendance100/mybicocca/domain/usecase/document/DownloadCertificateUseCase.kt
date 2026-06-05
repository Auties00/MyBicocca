package it.attendance100.mybicocca.domain.usecase.document

import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import javax.inject.Inject

class DownloadCertificateUseCase @Inject constructor(
    private val repository: CertificateRepository,
) {
    suspend operator fun invoke(id: CertificateId): ByteArray =
        repository.downloadCertificate(id)
}
