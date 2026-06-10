package it.attendance100.mybicocca.data.mapper.account

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Career
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UserSession
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.isSelectable

internal fun buildAcademicIdentity(
    session: Esse3UserSession,
    careers: List<Esse3Career>,
): AcademicIdentity {
    require(careers.isNotEmpty()) {
        "Cannot build AcademicIdentity: getCareers() returned no rows for user '${session.user.userId}'."
    }
    val mappedCareers = careers.map(::toDomainCareer)
    return AcademicIdentity(
        recordUserId = session.user.userId,
        personId = session.user.personId ?: 0L,
        fiscalCode = session.user.fiscalCode,
        careers = mappedCareers,
        selectedCareerId = chooseDefaultSelectedCareer(mappedCareers),
    )
}

internal fun composeDisplayName(session: Esse3UserSession): String {
    val first = session.user.firstName.trim()
    val last = session.user.lastName.trim()
    return when {
        first.isNotEmpty() && last.isNotEmpty() -> "$first $last"
        first.isNotEmpty() -> first
        last.isNotEmpty() -> last
        else -> session.user.userId
    }
}

internal fun selectableCount(careers: List<Career>): Int =
    careers.count { it.status.isSelectable }

private fun toDomainCareer(career: Esse3Career): Career = Career(
    id = CareerId(career.studentId ?: 0L),
    enrollmentTraitId = career.matId ?: 0L,
    programId = career.courseOfStudyId ?: 0L,
    easyStaffProgramCode = career.p06CourseOfStudyCode,
    academicYearEnrollmentId = career.academicYearEnrollmentId?.toLong() ?: 0L,
    studentNumber = career.matricola.orEmpty(),
    description = career.p06CourseOfStudyDescription
        ?: career.studentStatesDescription
        ?: "",
    academicYear = career.academicYearImm1 ?: career.academicYearId ?: 0,
    status = mapCareerStatus(career.studentStatusCode),
)

/**
 * Default selection policy: the selectable career with the most recent starting academic year;
 * ended careers are considered only when no selectable one exists.
 */
private fun chooseDefaultSelectedCareer(careers: List<Career>): CareerId {
    val pool = careers.filter { it.status.isSelectable }
        .ifEmpty { careers }
    return pool.maxByOrNull { it.academicYear }?.id ?: careers.first().id
}
