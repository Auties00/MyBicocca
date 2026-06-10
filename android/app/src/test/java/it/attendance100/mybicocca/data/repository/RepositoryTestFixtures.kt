package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import java.time.Instant

/**
 * Minimal real domain fixtures shared by the Esse3 live-first repository tests in this
 * package. The `requireCareer`/`activeCareer` helpers each repository runs walk a real
 * [Account] -> [AcademicIdentity] -> [Career] tree, so these build one with just the
 * fields those lookups read.
 */
internal object RepositoryTestFixtures {

    const val CAREER_VALUE: Long = 4242L
    const val ENROLLMENT_TRAIT_ID: Long = 9999L

    val careerId: CareerId = CareerId(CAREER_VALUE)

    fun career(
        id: CareerId = careerId,
        enrollmentTraitId: Long = ENROLLMENT_TRAIT_ID,
        programId: Long = 1000L,
        easyStaffProgramCode: String? = "E3201Q",
        academicYearEnrollmentId: Long = 2024L,
        status: CareerStatus = CareerStatus.ACTIVE,
    ): Career = Career(
        id = id,
        enrollmentTraitId = enrollmentTraitId,
        programId = programId,
        easyStaffProgramCode = easyStaffProgramCode,
        academicYearEnrollmentId = academicYearEnrollmentId,
        studentNumber = "123456",
        description = "Informatica",
        academicYear = 2024,
        status = status,
    )

    fun account(vararg careers: Career = arrayOf(career())): Account {
        val now = Instant.ofEpochMilli(1_700_000_000_000L)
        return Account(
            id = AccountId("account-1"),
            username = "mario.rossi@campus.unimib.it",
            displayName = "Mario Rossi",
            academic = AcademicIdentity(
                recordUserId = "user-1",
                personId = 7L,
                fiscalCode = null,
                careers = careers.toList(),
                selectedCareerId = careers.firstOrNull()?.id ?: careerId,
            ),
            learning = LearningIdentity(
                lmsUserId = 11,
                lmsUsername = "mario.rossi@campus.unimib.it",
                locale = "it",
                isSiteAdmin = false,
                maxUploadFileSizeBytes = 0L,
                storageQuotaBytes = 0L,
            ),
            createdAt = now,
            lastUsedAt = now,
            lastSyncedAt = now,
        )
    }
}
