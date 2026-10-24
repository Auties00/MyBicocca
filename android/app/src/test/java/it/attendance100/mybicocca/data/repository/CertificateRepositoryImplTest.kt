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
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Certificate
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CertificateType
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3CertificatesPage
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
 * over the portal's list, the once-only portal re-login when a page reports the session expired,
 * and the PDF download printed from the parameters the [CertificateId] carries.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CertificateRepositoryImplTest {

    private val account: Account = RepositoryTestFixtures.account()
    private val ownerId: Long = account.academic.personId

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val portal = mockk<Esse3Api>(relaxed = true)
    private val dao = mockk<CertificateCacheDao>(relaxed = true)

    private fun newRepository(): CertificateRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.esse3Web() } returns portal
        return CertificateRepositoryImpl(sessionManager, dao)
    }

    private fun certificate(configurationId: Int = 1, solarYear: Int? = 2024) = Esse3Certificate(
        configurationId = configurationId,
        documentId = 2,
        description = "Autodichiarazione Iscrizione",
        type = Esse3CertificateType.Enrolment,
        solarYear = solarYear,
        signatureFlag = "1",
    )

    private fun page(vararg certificates: Esse3Certificate, sessionExpired: Boolean = false) =
        Esse3CertificatesPage(sessionExpired = sessionExpired, certificates = certificates.toList())

    private fun cachedRow(id: String, order: Int) = CertificateEntity(
        ownerId = ownerId,
        certificateId = id,
        cacheOrder = order,
        description = "Autodichiarazione Iscrizione",
        type = CertificateType.Enrolment.name,
        solarYear = 2024,
        digitallySigned = true,
    )

    @Test
    fun `getCertificates success maps, drops the other language's duplicates and writes through`() = runTest {
        val repository = newRepository()
        coEvery { portal.certificates.getCertificates() } returns page(certificate(), certificate(), certificate(configurationId = 3))

        val result = repository.getCertificates()

        assertThat(result).hasSize(2)
        assertThat(result.first().id).isEqualTo(CertificateId("cert_conf_id=1&doc_id=2&tipo_cert_cod=I&annosolare=2024&firma_dig_flg=1"))
        assertThat(result.first().type).isEqualTo(CertificateType.Enrolment)
        coVerify { dao.replaceCertificates(ownerId, any()) }
        coVerify(exactly = 0) { dao.getCertificates(any()) }
    }

    @Test
    fun `getCertificates offline serves the cached rows`() = runTest {
        val repository = newRepository()
        coEvery { portal.certificates.getCertificates() } throws IOException("offline")
        coEvery { dao.getCertificates(ownerId) } returns listOf(cachedRow("cert_conf_id=1&doc_id=2", 0))

        val result = repository.getCertificates()

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(CertificateId("cert_conf_id=1&doc_id=2"))
        coVerify(exactly = 0) { dao.replaceCertificates(any(), any()) }
    }

    @Test
    fun `getCertificates offline with empty cache rethrows`() = runTest {
        val repository = newRepository()
        coEvery { portal.certificates.getCertificates() } throws IOException("offline")
        coEvery { dao.getCertificates(ownerId) } returns emptyList()

        assertThrows(IOException::class.java) {
            runBlocking { repository.getCertificates() }
        }
    }

    @Test
    fun `getCertificates logs in again once when the session expired and retries`() = runTest {
        val repository = newRepository()
        val fresh = mockk<Esse3Api>(relaxed = true)
        coEvery { portal.certificates.getCertificates() } returns page(sessionExpired = true)
        coEvery { sessionManager.reauthEsse3Web() } returns fresh
        coEvery { fresh.certificates.getCertificates() } returns page(certificate())

        val result = repository.getCertificates()

        assertThat(result).hasSize(1)
        coVerify(exactly = 1) { sessionManager.reauthEsse3Web() }
    }

    @Test
    fun `getCertificates propagates other errors without logging in again`() = runTest {
        val repository = newRepository()
        coEvery { portal.certificates.getCertificates() } throws IllegalArgumentException("parse failure")

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.getCertificates() }
        }
        coVerify(exactly = 0) { sessionManager.reauthEsse3Web() }
    }

    @Test
    fun `downloadCertificate prints with the id's parameters and drains the pdf`() = runTest {
        val repository = newRepository()
        val pdf = "%PDF-1.4 bytes".toByteArray()
        coEvery { portal.certificates.getCertificatePdf(1, 2, "I", 2024, "1") } returns ByteReadChannel(pdf)

        val bytes = repository.downloadCertificate(CertificateId("cert_conf_id=1&doc_id=2&tipo_cert_cod=I&annosolare=2024&firma_dig_flg=1"))

        assertThat(bytes).isEqualTo(pdf)
    }

    @Test
    fun `downloadCertificate logs in again when the portal answers its login page`() = runTest {
        val repository = newRepository()
        val fresh = mockk<Esse3Api>(relaxed = true)
        val pdf = "%PDF-1.4 bytes".toByteArray()
        coEvery { portal.certificates.getCertificatePdf(any(), any(), any(), any(), any()) } returns
            ByteReadChannel("<html>login</html>".toByteArray())
        coEvery { sessionManager.reauthEsse3Web() } returns fresh
        coEvery { fresh.certificates.getCertificatePdf(any(), any(), any(), any(), any()) } returns ByteReadChannel(pdf)

        val bytes = repository.downloadCertificate(CertificateId("MessaggiCertificato.do?cert_conf_id=1&doc_id=2"))

        assertThat(bytes).isEqualTo(pdf)
        coVerify { fresh.certificates.getCertificatePdf(1, 2, null, null, null) }
    }

    @Test
    fun `getCertificates uses owner id zero when there is no active account`() = runTest {
        every { sessionManager.activeAccount } returns MutableStateFlow(null)
        coEvery { sessionManager.esse3Web() } returns portal
        val repository = CertificateRepositoryImpl(sessionManager, dao)
        coEvery { portal.certificates.getCertificates() } throws IOException("offline")
        coEvery { dao.getCertificates(0L) } returns listOf(cachedRow("cert_conf_id=1&doc_id=2", 0))

        val result = repository.getCertificates()

        assertThat(result).hasSize(1)
        coVerify { dao.getCertificates(0L) }
    }
}
