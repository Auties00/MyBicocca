package it.attendance100.mybicocca.data.repository

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.mapper.document.toDomain
import it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3Api as Esse3LegacyApi
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertification
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertificationType
import it.attendance100.mybicocca.domain.model.document.Certificate
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.repository.CertificateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
) : CertificateRepository {

    override suspend fun getCertificates(): List<Certificate> =
        withLegacySession { it.legacy.getSelfCertifications() }
            .map { it.toDomain() }

    override suspend fun downloadCertificate(id: CertificateId): ByteArray {
        // Only the request path is needed to fetch the PDF; the other DTO fields are
        // display-only on the list. CertificateId carries that path opaquely.
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

    // Runs [block] against the legacy scrape session, re-logging in once if the session
    // has expired (the scraper bounces to the IdP and raises AuthenticationException —
    // detected by message since that type isn't on the app's compile classpath).
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

    private suspend fun ByteReadChannel.drainToByteArray(): ByteArray =
        withContext(Dispatchers.IO) { toInputStream().use { it.readBytes() } }
}
