package it.attendance100.mybicocca.data.mapper.account

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.AccountEntity
import it.attendance100.mybicocca.data.local.account.AccountWithCareers
import it.attendance100.mybicocca.data.local.account.CareerEntity
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import org.junit.Test
import java.time.Instant

/**
 * Covers the account entity <-> domain mapping: the embedded-account-with-careers projection
 * folds into the nested academic/learning identities, the career status round-trips through its
 * enum name (with an OTHER fallback for unknown stored values), and instants round-trip through
 * epoch milliseconds.
 */
class AccountMapperTest {

    private fun accountEntity(
        id: String = "uuid-1",
        selectedCareerId: Long = 100L,
        createdAt: Long = 1_000L,
        lastUsedAt: Long = 2_000L,
        lastSyncedAt: Long = 3_000L,
    ) = AccountEntity(
        id = id,
        username = "name.surname@campus.unimib.it",
        displayName = "Name Surname",
        recordUserId = "rec-1",
        personId = 55L,
        fiscalCode = "RSSMRA80A01F205X",
        selectedCareerId = selectedCareerId,
        lmsUserId = 4242,
        lmsUsername = "lms-user",
        lmsLocale = "it",
        lmsIsSiteAdmin = false,
        lmsMaxUploadBytes = 52_428_800L,
        lmsStorageQuotaBytes = 104_857_600L,
        createdAtEpochMillis = createdAt,
        lastUsedAtEpochMillis = lastUsedAt,
        lastSyncedAtEpochMillis = lastSyncedAt,
    )

    private fun careerEntity(
        id: Long = 100L,
        accountId: String = "uuid-1",
        status: String = "ACTIVE",
    ) = CareerEntity(
        id = id,
        accountId = accountId,
        enrollmentTraitId = 7L,
        programId = 8L,
        easyStaffProgramCode = "E0601",
        academicYearEnrollmentId = 2023L,
        studentNumber = "123456",
        description = "Informatica",
        academicYear = 2021,
        status = status,
    )

    @Test
    fun `AccountWithCareers maps into nested academic and learning identities`() {
        val domain = AccountWithCareers(
            account = accountEntity(),
            careers = listOf(careerEntity()),
        ).toDomain()

        assertThat(domain.id).isEqualTo(AccountId("uuid-1"))
        assertThat(domain.username).isEqualTo("name.surname@campus.unimib.it")
        assertThat(domain.displayName).isEqualTo("Name Surname")
        assertThat(domain.academic.recordUserId).isEqualTo("rec-1")
        assertThat(domain.academic.personId).isEqualTo(55L)
        assertThat(domain.academic.fiscalCode).isEqualTo("RSSMRA80A01F205X")
        assertThat(domain.academic.selectedCareerId).isEqualTo(CareerId(100L))
        assertThat(domain.academic.careers).hasSize(1)
        assertThat(domain.learning.lmsUserId).isEqualTo(4242)
        assertThat(domain.learning.maxUploadFileSizeBytes).isEqualTo(52_428_800L)
        assertThat(domain.learning.storageQuotaBytes).isEqualTo(104_857_600L)
    }

    @Test
    fun `epoch millis round-trip into instants`() {
        val domain = AccountWithCareers(
            account = accountEntity(createdAt = 1_700_000_000_000L, lastUsedAt = 1_700_000_100_000L, lastSyncedAt = 1_700_000_200_000L),
            careers = emptyList(),
        ).toDomain()

        assertThat(domain.createdAt).isEqualTo(Instant.ofEpochMilli(1_700_000_000_000L))
        assertThat(domain.lastUsedAt).isEqualTo(Instant.ofEpochMilli(1_700_000_100_000L))
        assertThat(domain.lastSyncedAt).isEqualTo(Instant.ofEpochMilli(1_700_000_200_000L))
    }

    @Test
    fun `CareerEntity maps every field onto the domain career`() {
        val career = careerEntity().toDomain()

        assertThat(career.id).isEqualTo(CareerId(100L))
        assertThat(career.enrollmentTraitId).isEqualTo(7L)
        assertThat(career.programId).isEqualTo(8L)
        assertThat(career.easyStaffProgramCode).isEqualTo("E0601")
        assertThat(career.academicYearEnrollmentId).isEqualTo(2023L)
        assertThat(career.studentNumber).isEqualTo("123456")
        assertThat(career.description).isEqualTo("Informatica")
        assertThat(career.academicYear).isEqualTo(2021)
        assertThat(career.status).isEqualTo(CareerStatus.ACTIVE)
    }

    @Test
    fun `a known status name parses back to its enum`() {
        assertThat(careerEntity(status = "GRADUATED").toDomain().status)
            .isEqualTo(CareerStatus.GRADUATED)
    }

    @Test
    fun `an unknown stored status name degrades to OTHER`() {
        assertThat(careerEntity(status = "GARBAGE").toDomain().status)
            .isEqualTo(CareerStatus.OTHER)
    }

    @Test
    fun `Account maps into an entity carrying the flattened identities`() {
        val account = Account(
            id = AccountId("uuid-9"),
            username = "user@campus.unimib.it",
            displayName = "User Nine",
            academic = AcademicIdentity(
                recordUserId = "rec-9",
                personId = 99L,
                fiscalCode = "FC9",
                careers = emptyList(),
                selectedCareerId = CareerId(900L),
            ),
            learning = LearningIdentity(
                lmsUserId = 9,
                lmsUsername = "lms-9",
                locale = "en",
                isSiteAdmin = true,
                maxUploadFileSizeBytes = 10L,
                storageQuotaBytes = 20L,
            ),
            createdAt = Instant.ofEpochMilli(11L),
            lastUsedAt = Instant.ofEpochMilli(22L),
            lastSyncedAt = Instant.ofEpochMilli(33L),
        )

        val entity = account.toEntity()

        assertThat(entity.id).isEqualTo("uuid-9")
        assertThat(entity.recordUserId).isEqualTo("rec-9")
        assertThat(entity.personId).isEqualTo(99L)
        assertThat(entity.selectedCareerId).isEqualTo(900L)
        assertThat(entity.lmsLocale).isEqualTo("en")
        assertThat(entity.lmsIsSiteAdmin).isTrue()
        assertThat(entity.createdAtEpochMillis).isEqualTo(11L)
        assertThat(entity.lastUsedAtEpochMillis).isEqualTo(22L)
        assertThat(entity.lastSyncedAtEpochMillis).isEqualTo(33L)
    }

    @Test
    fun `Career maps into an entity stamped with the owning account id and status name`() {
        val career = Career(
            id = CareerId(100L),
            enrollmentTraitId = 7L,
            programId = 8L,
            easyStaffProgramCode = null,
            academicYearEnrollmentId = 2023L,
            studentNumber = "123456",
            description = "Informatica",
            academicYear = 2021,
            status = CareerStatus.SUSPENDED,
        )

        val entity = career.toEntity(AccountId("owner"))

        assertThat(entity.id).isEqualTo(100L)
        assertThat(entity.accountId).isEqualTo("owner")
        assertThat(entity.easyStaffProgramCode).isNull()
        assertThat(entity.status).isEqualTo("SUSPENDED")
    }

    @Test
    fun `entity and domain round-trip preserves the career`() {
        val original = careerEntity(status = "SUSPENDED")
        val roundTripped = original.toDomain().toEntity(AccountId("uuid-1"))

        assertThat(roundTripped).isEqualTo(original)
    }
}
