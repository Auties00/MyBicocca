package it.attendance100.mybicocca.domain.usecase.document

import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import javax.inject.Inject

/**
 * Generates and downloads a certificate PDF when the user taps one on the registry
 * "Certificati" sub-screen. The PDF is produced server-side by Esse3 and returned as raw bytes
 * ready for the viewer.
 */
class DownloadCertificateUseCase @Inject constructor(
    private val repository: CertificateRepository,
) {
    suspend operator fun invoke(id: CertificateId): ByteArray =
        repository.downloadCertificate(id)
}
