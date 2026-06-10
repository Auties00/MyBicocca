package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.ktor.utils.io.ByteReadChannel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.certificate.CertificateCacheDao
import it.attendance100.mybicocca.data.local.certificate.CertificateEntity
import it.attendance100.mybicocca.data.remote.esse3.scraper.api.Esse3Api as Esse3LegacyApi
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertification
import it.attendance100.mybicocca.data.remote.esse3.scraper.dto.Esse3SelfCertificationType
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.document.CertificateId
import it.attendance100.mybicocca.domain.model.document.CertificateType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

/**
 * Behaviour coverage for the certificate (autocertificazioni) repository: the live-first triad
 * over the scraped list, the once-only legacy-session re-auth that detects expiry by message
 * sniffing, and the PDF download path that rebuilds the opaque request DTO around the
 * [CertificateId].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CertificateRepositoryImplTest {

    private val account: Account = RepositoryTestFixtures.account()
    private val ownerId: Long = account.academic.personId

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val legacyApi = mockk<Esse3LegacyApi>(relaxed = true)
    private val dao = mockk<CertificateCacheDao>(relaxed = true)

    private fun newRepository(): CertificateRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.esse3Legacy() } returns legacyApi
        return CertificateRepositoryImpl(sessionManager, dao)
    }

    private fun selfCertification(path: String) = Esse3SelfCertification(
        configurationId = 1,
        documentId = 2,
        description = "Autodichiarazione Iscrizione",
        type = Esse3SelfCertificationType.Enrolment,
        solarYear = 2024,
        digitallySigned = true,
        requestPath = path,
    )

    private fun cachedRow(path: String, order: Int) = CertificateEntity(
        ownerId = ownerId,
        certificateId = path,
        cacheOrder = order,
        description = "Autodichiarazione Iscrizione",
        type = CertificateType.Enrolment.name,
        solarYear = 2024,
        digitallySigned = true,
    )

    @Test
    fun `getCertificates success maps and writes through to the mirror`() = runTest {
        val repository = newRepository()
        coEvery { legacyApi.legacy.getSelfCertifications() } returns listOf(
            selfCertification("/path/cert-1"),
        )

        val result = repository.getCertificates()

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(CertificateId("/path/cert-1"))
        assertThat(result.first().type).isEqualTo(CertificateType.Enrolment)
        coVerify { dao.replaceCertificates(ownerId, any()) }
        coVerify(exactly = 0) { dao.getCertificates(any()) }
    }

    @Test
    fun `getCertificates offline serves the cached rows`() = runTest {
        val repository = newRepository()
        coEvery { legacyApi.legacy.getSelfCertifications() } throws IOException("offline")
        coEvery { dao.getCertificates(ownerId) } returns listOf(cachedRow("/path/cert-1", 0))

        val result = repository.getCertificates()

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(CertificateId("/path/cert-1"))
        coVerify(exactly = 0) { dao.replaceCertificates(any(), any()) }
    }

    @Test
    fun `getCertificates offline with empty cache rethrows`() = runTest {
        val repository = newRepository()
        coEvery { legacyApi.legacy.getSelfCertifications() } throws IOException("offline")
        coEvery { dao.getCertificates(ownerId) } returns emptyList()

        assertThrows(IOException::class.java) {
            runBlocking { repository.getCertificates() }
        }
    }

    @Test
    fun `getCertificates reauthenticates once on an expired session and retries`() = runTest {
        val repository = newRepository()
        val freshApi = mockk<Esse3LegacyApi>(relaxed = true)
        coEvery { legacyApi.legacy.getSelfCertifications() } throws
            IllegalStateException("Session expired, redirected to identity provider")
        coEvery { sessionManager.reauthEsse3Legacy() } returns freshApi
        coEvery { freshApi.legacy.getSelfCertifications() } returns listOf(selfCertification("/p/2"))

        val result = repository.getCertificates()

        assertThat(result).hasSize(1)
        coVerify(exactly = 1) { sessionManager.reauthEsse3Legacy() }
        coVerify { freshApi.legacy.getSelfCertifications() }
    }

    @Test
    fun `getCertificates propagates a non-session error without reauthenticating`() = runTest {
        val repository = newRepository()
        coEvery { legacyApi.legacy.getSelfCertifications() } throws
            IllegalArgumentException("parse failure")

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.getCertificates() }
        }
        coVerify(exactly = 0) { sessionManager.reauthEsse3Legacy() }
    }

    @Test
    fun `downloadCertificate carries the id as the request path and drains the pdf`() = runTest {
        val repository = newRepository()
        val pdf = "PDF-BYTES".toByteArray()
        val captured = mutableListOf<Esse3SelfCertification>()
        coEvery { legacyApi.legacy.downloadSelfCertification(capture(captured)) } returns
            ByteReadChannel(pdf)

        val bytes = repository.downloadCertificate(CertificateId("/download/path"))

        assertThat(bytes).isEqualTo(pdf)
        assertThat(captured).hasSize(1)
        assertThat(captured.first().requestPath).isEqualTo("/download/path")
    }

    @Test
    fun `getCertificates uses owner id zero when there is no active account`() = runTest {
        every { sessionManager.activeAccount } returns MutableStateFlow(null)
        coEvery { sessionManager.esse3Legacy() } returns legacyApi
        val repository = CertificateRepositoryImpl(sessionManager, dao)
        coEvery { legacyApi.legacy.getSelfCertifications() } throws IOException("offline")
        coEvery { dao.getCertificates(0L) } returns listOf(cachedRow("/p/1", 0))

        val result = repository.getCertificates()

        assertThat(result).hasSize(1)
        coVerify { dao.getCertificates(0L) }
    }
}
