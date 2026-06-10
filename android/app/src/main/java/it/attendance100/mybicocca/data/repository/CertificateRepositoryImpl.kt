package it.attendance100.mybicocca.data.repository

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.certificate.CertificateCacheDao
import it.attendance100.mybicocca.data.mapper.document.toDomain
import it.attendance100.mybicocca.data.mapper.document.toEntity
import it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3Api as Esse3LegacyApi
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertification
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertificationType
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scrape-based implementation over the Esse3 legacy web pages (ListaCertificati.do), the only
 * surface that exposes self-certifications. Reads and downloads run inside a legacy scrape
 * session that is transparently re-established once when expired. The certificate list is
 * live-first: each successful read is written through to the offline certificate mirror
 * ([CertificateCacheDao]), and the mirror is read back only when a live call fails with an
 * [IOException] (no network), so the "Certificati" screen stays readable offline. An offline
 * read with no cached rows rethrows, surfacing the error state rather than a misleading empty
 * list. The PDF download itself stays live-only and its action is disabled offline.
 */
@Singleton
class CertificateRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val certificateCacheDao: CertificateCacheDao,
) : CertificateRepository {

    override suspend fun getCertificates(): List<Certificate> {
        val ownerId = ownerId()
        return try {
            val certificates = withLegacySession { it.legacy.getSelfCertifications() }
                .map { dto -> dto.toDomain() }
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
     * Rebuilds a scraper DTO around the request path carried opaquely by [CertificateId]: only
     * that path is needed to fetch the PDF, the other DTO fields are display-only on the list.
     */
    override suspend fun downloadCertificate(id: CertificateId): ByteArray {
        val request = Esse3SelfCertification(
            configurationId = 0,
            documentId = 0,
            description = "",
            type = Esse3SelfCertificationType.Other(""),
            solarYear = null,
            digitallySigned = false,
            requestPath = id.value,
        )
        return withLegacySession { it.legacy.downloadSelfCertification(request) }
            .drainToByteArray()
    }

    /**
     * Runs [block] against the legacy scrape session, re-logging in once if the session has
     * expired (the scraper bounces to the IdP and raises AuthenticationException — detected by
     * message since that type isn't on the app's compile classpath).
     */
    private suspend fun <T> withLegacySession(block: suspend (Esse3LegacyApi) -> T): T {
        val api = sessionManager.esse3Legacy()
        return try {
            block(api)
        } catch (t: Throwable) {
            if (!looksLikeExpiredSession(t)) throw t
            block(sessionManager.reauthEsse3Legacy())
        }
    }

    private fun looksLikeExpiredSession(t: Throwable): Boolean {
        var cur: Throwable? = t
        while (cur != null) {
            val msg = cur.message?.lowercase()
            if (msg != null && ("session expired" in msg || "identity provider" in msg)) {
                return true
            }
            cur = cur.cause
        }
        return false
    }

    private fun ownerId(): Long =
        sessionManager.activeAccount.value?.academic?.personId ?: 0L

    /** Fully reads a streamed PDF response into memory off the main thread. */
    private suspend fun ByteReadChannel.drainToByteArray(): ByteArray =
        withContext(Dispatchers.IO) { toInputStream().use { it.readBytes() } }
}
