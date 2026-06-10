package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId

/**
 * Self-declaration certificates (autocertificazioni) from the Esse3 legacy web session.
 *
 * Live-first — the list is cheap to refetch, so every call hits the live scrape session while
 * connectivity exists; the list read keeps an offline snapshot of its last success purely for
 * display when the device has no network, while the PDF download stays live-only. Both methods
 * throw on failure (the ViewModel translates to SyncStatus); the impl transparently re-logs in
 * once on session expiry.
 */
interface CertificateRepository {

    suspend fun getCertificates(): List<Certificate>

    /** Streams the certificate PDF into a byte array, ready to hand to a PDF viewer. */
    suspend fun downloadCertificate(id: CertificateId): ByteArray
}
