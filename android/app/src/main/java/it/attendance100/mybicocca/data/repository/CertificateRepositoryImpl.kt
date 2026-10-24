package it.attendance100.mybicocca.data.repository

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.certificate.CertificateCacheDao
import it.attendance100.mybicocca.data.mapper.document.toDomain
import it.attendance100.mybicocca.data.mapper.document.toEntity
import it.attendance100.mybicocca.data.mapper.document.toPrintRequest
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation over the self-declarations page of the Esse3 web portal (ListaCertificati.do),
 * the only surface that exposes them, through the generated [Esse3Api.certificates]. Reads and
 * downloads run on the portal session, which is re-established once when a page reports it
 * expired. The certificate list is live-first: each successful read is written through to the
 * offline certificate mirror ([CertificateCacheDao]), and the mirror is read back only when a
 * live call fails with an [IOException] (no network), so the "Certificati" screen stays readable
 * offline. An offline read with no cached rows rethrows, surfacing the error state rather than a
 * misleading empty list. The PDF download itself stays live-only and its action is disabled
 * offline.
 */
@Singleton
class CertificateRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val certificateCacheDao: CertificateCacheDao,
) : CertificateRepository {

    override suspend fun getCertificates(): List<Certificate> {
        val ownerId = ownerId()
        return try {
            val certificates = withPortalSession { api ->
                api.certificates.getCertificates().takeUnless { it.sessionExpired }?.certificates
            }
                // The same declaration can be listed twice, in Italian and in English.
                .distinctBy { it.configurationId to it.solarYear }
                .map { it.toDomain() }
            certificateCacheDao.replaceCertificates(
                ownerId,
                certificates.mapIndexed { index, certificate -> certificate.toEntity(ownerId, index) },
            )
            certificates
        } catch (e: IOException) {
            certificateCacheDao.getCertificates(ownerId).map { it.toDomain() }.ifEmpty { throw e }
        }
    }

    /**
     * Prints the declaration with the parameters [CertificateId] carries. With an expired session
     * the portal answers its login page instead of the PDF, which is told apart by its header.
     */
    override suspend fun downloadCertificate(id: CertificateId): ByteArray {
        val request = requireNotNull(id.toPrintRequest()) { "Not a certificate: ${id.value}" }
        return withPortalSession { api ->
            api.certificates.getCertificatePdf(
                configurationId = request.configurationId,
                documentId = request.documentId,
                typeCode = request.typeCode,
                solarYear = request.solarYear,
                signatureFlag = request.signatureFlag,
            ).drainToByteArray().takeIf { it.isPdf() }
        }
    }

    /**
     * Runs [block] on the portal session, logging in again once when it returns null, which
     * means the page reported the session expired.
     */
    private suspend fun <T : Any> withPortalSession(block: suspend (Esse3Api) -> T?): T =
        block(sessionManager.esse3Web())
            ?: block(sessionManager.reauthEsse3Web())
            ?: throw IllegalStateException("The Esse3 portal session expired again right after logging in")

    private fun ownerId(): Long =
        sessionManager.activeAccount.value?.academic?.personId ?: 0L

    /** Fully reads a streamed response into memory off the main thread. */
    private suspend fun ByteReadChannel.drainToByteArray(): ByteArray =
        withContext(Dispatchers.IO) { toInputStream().use { it.readBytes() } }

    private fun ByteArray.isPdf(): Boolean =
        size >= PDF_MAGIC.size && PDF_MAGIC.indices.all { this[it] == PDF_MAGIC[it] }

    private companion object {
        /** The header every PDF starts with, `%PDF-`. */
        val PDF_MAGIC = "%PDF-".toByteArray(Charsets.US_ASCII)
    }
}
