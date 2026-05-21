package it.attendance100.mybicocca.data.mapper

import it.attendance100.mybicocca.data.local.account.AccountEntity
import it.attendance100.mybicocca.data.local.account.AccountWithCareers
import it.attendance100.mybicocca.data.local.account.CareerEntity
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import java.time.Instant

internal fun AccountWithCareers.toDomain(): Account {
    val academic = AcademicIdentity(
        recordUserId = account.recordUserId,
        personId = account.personId,
        fiscalCode = account.fiscalCode,
        careers = careers.map(CareerEntity::toDomain),
        selectedCareerId = CareerId(account.selectedCareerId),
    )
    val learning = LearningIdentity(
        lmsUserId = account.lmsUserId,
        lmsUsername = account.lmsUsername,
        locale = account.lmsLocale,
        isSiteAdmin = account.lmsIsSiteAdmin,
        maxUploadFileSizeBytes = account.lmsMaxUploadBytes,
        storageQuotaBytes = account.lmsStorageQuotaBytes,
    )
    return Account(
        id = AccountId(account.id),
        username = account.username,
        displayName = account.displayName,
        academic = academic,
        learning = learning,
        createdAt = Instant.ofEpochMilli(account.createdAtEpochMillis),
        lastUsedAt = Instant.ofEpochMilli(account.lastUsedAtEpochMillis),
        lastSyncedAt = Instant.ofEpochMilli(account.lastSyncedAtEpochMillis),
    )
}

internal fun CareerEntity.toDomain(): Career = Career(
    id = CareerId(id),
    enrollmentTraitId = enrollmentTraitId,
    programId = programId,
    easyStaffProgramCode = easyStaffProgramCode,
    academicYearEnrollmentId = academicYearEnrollmentId,
    matricola = matricola,
    description = description,
    academicYear = academicYear,
    status = parseCareerStatus(status),
)

internal fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id.value,
    username = username,
    displayName = displayName,
    recordUserId = academic.recordUserId,
    personId = academic.personId,
    fiscalCode = academic.fiscalCode,
    selectedCareerId = academic.selectedCareerId.value,
    lmsUserId = learning.lmsUserId,
    lmsUsername = learning.lmsUsername,
    lmsLocale = learning.locale,
    lmsIsSiteAdmin = learning.isSiteAdmin,
    lmsMaxUploadBytes = learning.maxUploadFileSizeBytes,
    lmsStorageQuotaBytes = learning.storageQuotaBytes,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    lastUsedAtEpochMillis = lastUsedAt.toEpochMilli(),
    lastSyncedAtEpochMillis = lastSyncedAt.toEpochMilli(),
)

internal fun Career.toEntity(accountId: AccountId): CareerEntity = CareerEntity(
    id = id.value,
    accountId = accountId.value,
    enrollmentTraitId = enrollmentTraitId,
    programId = programId,
    easyStaffProgramCode = easyStaffProgramCode,
    academicYearEnrollmentId = academicYearEnrollmentId,
    matricola = matricola,
    description = description,
    academicYear = academicYear,
    status = status.name,
)

private fun parseCareerStatus(name: String): CareerStatus =
    runCatching { CareerStatus.valueOf(name) }.getOrDefault(CareerStatus.OTHER)
