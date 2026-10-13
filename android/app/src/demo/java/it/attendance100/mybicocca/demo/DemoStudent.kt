package it.attendance100.mybicocca.demo

import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.elearning.course.CourseLevel
import java.time.LocalDateTime

/** The invented student every demo repository describes; nothing here belongs to a real person. */
object DemoStudent {
    const val FIRST_NAME = "Marco"
    const val LAST_NAME = "Rossi"
    const val FULL_NAME = "$FIRST_NAME $LAST_NAME"
    const val USERNAME = "m.rossi99@campus.unimib.it"
    const val STUDENT_NUMBER = "900123"
    const val FISCAL_CODE = "RSSMRC03A01F205X"

    val accountId = AccountId("demo-account")
    val careerId = CareerId(700001L)

    val career = Career(
        id = careerId,
        enrollmentTraitId = 710001L,
        programId = 10001L,
        easyStaffProgramCode = "E3101Q",
        academicYearEnrollmentId = 720001L,
        studentNumber = STUDENT_NUMBER,
        description = "INFORMATICA",
        academicYear = 2025,
        status = CareerStatus.ACTIVE,
        level = CourseLevel.Bachelor,
    )

    val account = Account(
        id = accountId,
        username = USERNAME,
        displayName = FULL_NAME,
        academic = AcademicIdentity(
            recordUserId = "m.rossi99",
            personId = 730001L,
            fiscalCode = FISCAL_CODE,
            careers = listOf(career),
            selectedCareerId = careerId,
        ),
        learning = LearningIdentity(
            lmsUserId = 40001,
            lmsUsername = USERNAME,
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 256L * 1024 * 1024,
            storageQuotaBytes = 1024L * 1024 * 1024,
        ),
        createdAt = DemoClock.instant(LocalDateTime.of(2023, 9, 18, 9, 0)),
        lastUsedAt = DemoClock.fixed.instant(),
        lastSyncedAt = DemoClock.fixed.instant(),
    )
}
