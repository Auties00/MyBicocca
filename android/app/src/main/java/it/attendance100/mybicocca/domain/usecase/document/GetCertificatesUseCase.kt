package it.attendance100.mybicocca.domain.usecase.document

import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import javax.inject.Inject

/**
 * Loads the self-declaration ("autocertificazione") templates available to the student when
 * the registry "Certificati" sub-screen opens or is refreshed. Scraped live from the Esse3
 * legacy web session.
 */
class GetCertificatesUseCase @Inject constructor(
    private val repository: CertificateRepository,
) {
    suspend operator fun invoke(): List<Certificate> = repository.getCertificates()
}
