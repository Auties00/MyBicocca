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
 * Builds a minimal but real [Account] for the e-learning repository tests, carrying the Moodle
 * user id the refreshes thread into their web-service calls. Kept private to the test package so
 * each repo test seeds `sessionManager.activeAccount` with the same well-formed account.
 */
internal fun elearningRepoTestAccount(accountId: AccountId, lmsUserId: Int): Account {
    val careerId = CareerId(1L)
    val career = Career(
        id = careerId,
        enrollmentTraitId = 100L,
        programId = 200L,
        easyStaffProgramCode = "E3101Q",
        academicYearEnrollmentId = 2025L,
        studentNumber = "123456",
        description = "Informatica",
        academicYear = 2024,
        status = CareerStatus.ACTIVE,
    )
    return Account(
        id = accountId,
        username = "mario.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "user-1",
            personId = 999L,
            fiscalCode = null,
            careers = listOf(career),
            selectedCareerId = careerId,
        ),
        learning = LearningIdentity(
            lmsUserId = lmsUserId,
            lmsUsername = "mario.rossi@campus.unimib.it",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )
}
