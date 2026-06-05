package it.attendance100.mybicocca.domain.usecase.document

import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import javax.inject.Inject

class GetCertificatesUseCase @Inject constructor(
    private val repository: CertificateRepository,
) {
    suspend operator fun invoke(): List<Certificate> = repository.getCertificates()
}
