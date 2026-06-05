package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId

// Self-declaration certificates (autocertificazioni) from the Esse3 legacy web session.
// NOT cached locally — there is no Room SSOT for this and the list is cheap to refetch;
// every call hits the live scrape session. Both methods throw on failure (the ViewModel
// translates to SyncStatus); the impl transparently re-logs in once on session expiry.
interface CertificateRepository {

    suspend fun getCertificates(): List<Certificate>

    // Streams the certificate PDF into a byte array, ready to hand to a PDF viewer.
    suspend fun downloadCertificate(id: CertificateId): ByteArray
}
