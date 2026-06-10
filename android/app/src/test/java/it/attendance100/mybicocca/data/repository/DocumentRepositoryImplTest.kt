package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.ktor.utils.io.ByteReadChannel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.document.AcademicTitleEntity
import it.attendance100.mybicocca.data.local.document.BadgeImageEntity
import it.attendance100.mybicocca.data.local.document.DocumentCacheDao
import it.attendance100.mybicocca.data.local.document.StudentBadgeEntity
import it.attendance100.mybicocca.data.local.document.TitleAttributeEntity
import it.attendance100.mybicocca.data.remote.esse3.api.Esse3Api
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BadgeData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PersonTitles
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.BadgeBlobId
import it.attendance100.mybicocca.domain.model.document.BadgeId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

/**
 * Behaviour coverage for the badge / titles / badge-image document repository: the live-first
 * triad on each cached read, the "null badge clears the cache" branch (a missing card is a
 * valid result, not an error), and the front/rear side selection plus blob-keyed image cache.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DocumentRepositoryImplTest {

    private val careerId: CareerId = RepositoryTestFixtures.careerId
    private val account: Account = RepositoryTestFixtures.account()
    private val personId: Long = account.academic.personId

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val esse3 = mockk<Esse3Api>(relaxed = true)
    private val dao = mockk<DocumentCacheDao>(relaxed = true)

    private fun newRepository(): DocumentRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.esse3() } returns esse3
        return DocumentRepositoryImpl(sessionManager, dao)
    }

    private fun badgeRow() = StudentBadgeEntity(
        careerId = careerId.value,
        badgeId = 42L,
        blobId = 7L,
        hasFrontImage = true,
        hasRearImage = false,
        rfid = "RF-1",
        studentNumber = "123456",
        fullName = "Mario Rossi",
        courseDescription = "Informatica",
        facultyDescription = "Scienze",
        academicYear = 2024,
        delivered = true,
        cancelled = false,
        returned = false,
        createdOn = null,
        printedOn = null,
        deliveredOn = null,
    )

    @Test
    fun `getBadge success maps the most recent card and writes it through`() = runTest {
        val repository = newRepository()
        coEvery { esse3.badge.getNewBadges(studentId = careerId.value) } returns listOf(
            Esse3BadgeData(badgeId = 42L, badgeBlobId = 7L, frontImagePresent = 1),
        )

        val badge = repository.getBadge(careerId)

        assertThat(badge).isNotNull()
        assertThat(badge!!.id).isEqualTo(BadgeId(42L))
        assertThat(badge.blobId).isEqualTo(BadgeBlobId(7L))
        val captured = slot<StudentBadgeEntity>()
        coVerify { dao.replaceBadge(careerId.value, capture(captured)) }
        assertThat(captured.captured).isNotNull()
    }

    @Test
    fun `getBadge null result clears the cached card`() = runTest {
        val repository = newRepository()
        coEvery { esse3.badge.getNewBadges(studentId = careerId.value) } returns emptyList()

        val badge = repository.getBadge(careerId)

        assertThat(badge).isNull()
        coVerify { dao.replaceBadge(careerId.value, null) }
    }

    @Test
    fun `getBadge offline serves the cached card`() = runTest {
        val repository = newRepository()
        coEvery { esse3.badge.getNewBadges(studentId = careerId.value) } throws IOException("offline")
        coEvery { dao.getBadge(careerId.value) } returns badgeRow()

        val badge = repository.getBadge(careerId)

        assertThat(badge).isNotNull()
        assertThat(badge!!.id).isEqualTo(BadgeId(42L))
    }

    @Test
    fun `getBadge offline with no cached card rethrows`() = runTest {
        val repository = newRepository()
        coEvery { esse3.badge.getNewBadges(studentId = careerId.value) } throws IOException("offline")
        coEvery { dao.getBadge(careerId.value) } returns null

        assertThrows(IOException::class.java) {
            runBlocking { repository.getBadge(careerId) }
        }
    }

    @Test
    fun `getTitles success requests all optional fields and writes through`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.personalData.getTitles(personId = personId, studentId = careerId.value, optionalFields = "ALL")
        } returns Esse3PersonTitles()

        val titles = repository.getTitles(careerId)

        assertThat(titles).isEmpty()
        coVerify { dao.replaceTitles(careerId.value, any(), any()) }
    }

    @Test
    fun `getTitles offline serves the cached titles with their attributes`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.personalData.getTitles(personId = personId, studentId = careerId.value, optionalFields = "ALL")
        } throws IOException("offline")
        coEvery { dao.getTitles(careerId.value) } returns listOf(
            AcademicTitleEntity(
                careerId = careerId.value,
                titleId = "titit-1",
                cacheOrder = 0,
                category = "Italian",
                status = "Awarded",
                typeDescription = "Laurea",
                subject = "Informatica",
                institution = "Bicocca",
                country = null,
                year = "2023/2024",
                grade = "110/110",
                cumLaude = true,
                valueDeclarationFiled = false,
            ),
        )
        coEvery { dao.getTitleAttributes(careerId.value) } returns listOf(
            TitleAttributeEntity(
                careerId = careerId.value,
                titleId = "titit-1",
                attrOrder = 0,
                field = "Institution",
                value = "Bicocca",
            ),
        )

        val titles = repository.getTitles(careerId)

        assertThat(titles).hasSize(1)
        assertThat(titles.first().id).isEqualTo("titit-1")
        assertThat(titles.first().attributes).hasSize(1)
    }

    @Test
    fun `getTitles offline with no cached titles rethrows`() = runTest {
        val repository = newRepository()
        coEvery {
            esse3.personalData.getTitles(personId = personId, studentId = careerId.value, optionalFields = "ALL")
        } throws IOException("offline")
        coEvery { dao.getTitles(careerId.value) } returns emptyList()
        coEvery { dao.getTitleAttributes(careerId.value) } returns emptyList()

        assertThrows(IOException::class.java) {
            runBlocking { repository.getTitles(careerId) }
        }
    }

    @Test
    fun `getBadgeImage front side fetches the front blob, caches it, and returns the bytes`() = runTest {
        val repository = newRepository()
        val badgeApi = esse3.badge
        val bytes = "FRONT".toByteArray()
        coEvery { badgeApi.getBadgeBlobFrontPage(7L) } returns ByteReadChannel(bytes)

        val result = repository.getBadgeImage(BadgeBlobId(7L), rear = false)

        assertThat(result).isEqualTo(bytes)
        coVerify { badgeApi.getBadgeBlobFrontPage(7L) }
        coVerify {
            dao.upsertImage(match { it.blobId == 7L && it.side == "front" })
        }
        coVerify(exactly = 0) { badgeApi.getBadgeBlobRearPage(any()) }
    }

    @Test
    fun `getBadgeImage rear side fetches the rear blob and caches it under rear`() = runTest {
        val repository = newRepository()
        val badgeApi = esse3.badge
        val bytes = "REAR".toByteArray()
        coEvery { badgeApi.getBadgeBlobRearPage(7L) } returns ByteReadChannel(bytes)

        val result = repository.getBadgeImage(BadgeBlobId(7L), rear = true)

        assertThat(result).isEqualTo(bytes)
        coVerify { badgeApi.getBadgeBlobRearPage(7L) }
        coVerify { dao.upsertImage(match { it.side == "rear" }) }
        coVerify(exactly = 0) { badgeApi.getBadgeBlobFrontPage(any()) }
    }

    @Test
    fun `getBadgeImage offline serves the cached image bytes`() = runTest {
        val repository = newRepository()
        val cached = "CACHED".toByteArray()
        coEvery { esse3.badge.getBadgeBlobFrontPage(7L) } throws IOException("offline")
        coEvery { dao.getImage(7L, "front") } returns BadgeImageEntity(7L, "front", cached)

        val result = repository.getBadgeImage(BadgeBlobId(7L), rear = false)

        assertThat(result).isEqualTo(cached)
    }

    @Test
    fun `getBadgeImage offline with no cached image rethrows`() = runTest {
        val repository = newRepository()
        coEvery { esse3.badge.getBadgeBlobFrontPage(7L) } throws IOException("offline")
        coEvery { dao.getImage(7L, "front") } returns null

        assertThrows(IOException::class.java) {
            runBlocking { repository.getBadgeImage(BadgeBlobId(7L), rear = false) }
        }
    }

    @Test
    fun `getBadge rejects a career absent from the active account`() = runTest {
        val repository = newRepository()

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repository.getBadge(CareerId(999_999L)) }
        }
    }
}
