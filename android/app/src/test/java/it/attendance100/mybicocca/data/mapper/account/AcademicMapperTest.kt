package it.attendance100.mybicocca.data.mapper.account

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3Career
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3User
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3UserSession
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Covers the Esse3-session-to-academic-identity mapping: the require-non-empty guard, the
 * career field projection with its `0`/empty-string fallbacks, the display-name composition
 * branches, the selectable count, and the default-selection policy (most recent selectable
 * career, ended careers only as a last resort). All status codes exercised here resolve
 * without hitting the logged unknown-code fallback.
 */
class AcademicMapperTest {

    private fun user(
        firstName: String = "Mario",
        lastName: String = "Rossi",
        userId: String = "u-1",
        personId: Long? = 77L,
        fiscalCode: String? = "RSSMRA80A01F205X",
    ) = Esse3User(
        firstName = firstName,
        lastName = lastName,
        userId = userId,
        personId = personId,
        fiscalCode = fiscalCode,
    )

    private fun session(user: Esse3User = user()) = Esse3UserSession(user = user)

    private fun esse3Career(
        studentId: Long? = 100L,
        statusCode: String? = "A",
        academicYearImm1: Int? = 2021,
        academicYearId: Int? = null,
        p06Description: String? = "Informatica",
        studentStatesDescription: String? = null,
    ) = Esse3Career(
        studentId = studentId,
        studentStatusCode = statusCode,
        academicYearImm1 = academicYearImm1,
        academicYearId = academicYearId,
        p06CourseOfStudyDescription = p06Description,
        studentStatesDescription = studentStatesDescription,
        matId = 9L,
        courseOfStudyId = 8L,
        p06CourseOfStudyCode = "E0601",
        academicYearEnrollmentId = 2023,
        matricola = "123456",
    )

    @Test
    fun `buildAcademicIdentity rejects an empty career list`() {
        assertThrows(IllegalArgumentException::class.java) {
            buildAcademicIdentity(session(), emptyList())
        }
    }

    @Test
    fun `buildAcademicIdentity carries user identity fields`() {
        val identity = buildAcademicIdentity(session(), listOf(esse3Career()))

        assertThat(identity.recordUserId).isEqualTo("u-1")
        assertThat(identity.personId).isEqualTo(77L)
        assertThat(identity.fiscalCode).isEqualTo("RSSMRA80A01F205X")
        assertThat(identity.careers).hasSize(1)
    }

    @Test
    fun `a missing person id falls back to zero`() {
        val identity = buildAcademicIdentity(session(user(personId = null)), listOf(esse3Career()))

        assertThat(identity.personId).isEqualTo(0L)
    }

    @Test
    fun `the mapped career projects its fields`() {
        val career = buildAcademicIdentity(session(), listOf(esse3Career())).careers.single()

        assertThat(career.id).isEqualTo(CareerId(100L))
        assertThat(career.enrollmentTraitId).isEqualTo(9L)
        assertThat(career.programId).isEqualTo(8L)
        assertThat(career.easyStaffProgramCode).isEqualTo("E0601")
        assertThat(career.academicYearEnrollmentId).isEqualTo(2023L)
        assertThat(career.studentNumber).isEqualTo("123456")
        assertThat(career.description).isEqualTo("Informatica")
        assertThat(career.academicYear).isEqualTo(2021)
        assertThat(career.status).isEqualTo(CareerStatus.ACTIVE)
    }

    @Test
    fun `missing numeric career ids fall back to zero`() {
        val career = buildAcademicIdentity(
            session(),
            listOf(
                Esse3Career(
                    studentId = null,
                    matId = null,
                    courseOfStudyId = null,
                    academicYearEnrollmentId = null,
                    academicYearImm1 = null,
                    academicYearId = null,
                    studentStatusCode = "A",
                ),
            ),
        ).careers.single()

        assertThat(career.id).isEqualTo(CareerId(0L))
        assertThat(career.enrollmentTraitId).isEqualTo(0L)
        assertThat(career.programId).isEqualTo(0L)
        assertThat(career.academicYearEnrollmentId).isEqualTo(0L)
        assertThat(career.academicYear).isEqualTo(0)
    }

    @Test
    fun `a blank matricola becomes an empty student number`() {
        val career = buildAcademicIdentity(
            session(),
            listOf(esse3Career().copy(matricola = null)),
        ).careers.single()

        assertThat(career.studentNumber).isEqualTo("")
    }

    @Test
    fun `description prefers the programme name then the status description then empty`() {
        val withProgramme = buildAcademicIdentity(
            session(),
            listOf(esse3Career(p06Description = "Matematica", studentStatesDescription = "Attiva")),
        ).careers.single()
        assertThat(withProgramme.description).isEqualTo("Matematica")

        val withStatusOnly = buildAcademicIdentity(
            session(),
            listOf(esse3Career(p06Description = null, studentStatesDescription = "Attiva")),
        ).careers.single()
        assertThat(withStatusOnly.description).isEqualTo("Attiva")

        val withNeither = buildAcademicIdentity(
            session(),
            listOf(esse3Career(p06Description = null, studentStatesDescription = null)),
        ).careers.single()
        assertThat(withNeither.description).isEqualTo("")
    }

    @Test
    fun `academic year prefers aaImm1 and falls back to aaId`() {
        val fromImm1 = buildAcademicIdentity(
            session(),
            listOf(esse3Career(academicYearImm1 = 2020, academicYearId = 2018)),
        ).careers.single()
        assertThat(fromImm1.academicYear).isEqualTo(2020)

        val fromId = buildAcademicIdentity(
            session(),
            listOf(esse3Career(academicYearImm1 = null, academicYearId = 2018)),
        ).careers.single()
        assertThat(fromId.academicYear).isEqualTo(2018)
    }

    @Test
    fun `default selection picks the most recent selectable career`() {
        val older = esse3Career(studentId = 1L, statusCode = "A", academicYearImm1 = 2019)
        val newer = esse3Career(studentId = 2L, statusCode = "A", academicYearImm1 = 2023)

        val identity = buildAcademicIdentity(session(), listOf(older, newer))

        assertThat(identity.selectedCareerId).isEqualTo(CareerId(2L))
    }

    @Test
    fun `default selection ignores an ended career when a selectable one exists`() {
        val endedRecent = esse3Career(studentId = 1L, statusCode = "L", academicYearImm1 = 2024)
        val activeOlder = esse3Career(studentId = 2L, statusCode = "A", academicYearImm1 = 2020)

        val identity = buildAcademicIdentity(session(), listOf(endedRecent, activeOlder))

        assertThat(identity.selectedCareerId).isEqualTo(CareerId(2L))
    }

    @Test
    fun `default selection falls back to the most recent ended career when none is selectable`() {
        val endedOlder = esse3Career(studentId = 1L, statusCode = "L", academicYearImm1 = 2018)
        val endedNewer = esse3Career(studentId = 2L, statusCode = "L", academicYearImm1 = 2022)

        val identity = buildAcademicIdentity(session(), listOf(endedOlder, endedNewer))

        assertThat(identity.selectedCareerId).isEqualTo(CareerId(2L))
    }

    @Test
    fun `composeDisplayName joins first and last name`() {
        assertThat(composeDisplayName(session(user(firstName = "Mario", lastName = "Rossi"))))
            .isEqualTo("Mario Rossi")
    }

    @Test
    fun `composeDisplayName trims surrounding whitespace before joining`() {
        assertThat(composeDisplayName(session(user(firstName = "  Mario  ", lastName = "  Rossi  "))))
            .isEqualTo("Mario Rossi")
    }

    @Test
    fun `composeDisplayName uses the first name alone when the last is blank`() {
        assertThat(composeDisplayName(session(user(firstName = "Mario", lastName = "   "))))
            .isEqualTo("Mario")
    }

    @Test
    fun `composeDisplayName uses the last name alone when the first is blank`() {
        assertThat(composeDisplayName(session(user(firstName = "", lastName = "Rossi"))))
            .isEqualTo("Rossi")
    }

    @Test
    fun `composeDisplayName falls back to the user id when both names are blank`() {
        assertThat(composeDisplayName(session(user(firstName = " ", lastName = " ", userId = "fallback-id"))))
            .isEqualTo("fallback-id")
    }

    @Test
    fun `selectableCount counts only active and suspended careers`() {
        val careers = listOf(
            career(CareerStatus.ACTIVE),
            career(CareerStatus.SUSPENDED),
            career(CareerStatus.GRADUATED),
            career(CareerStatus.INTERRUPTED),
            career(CareerStatus.OTHER),
        )

        assertThat(selectableCount(careers)).isEqualTo(2)
    }

    private fun career(status: CareerStatus): Career = Career(
        id = CareerId(1L),
        enrollmentTraitId = 0L,
        programId = 0L,
        easyStaffProgramCode = null,
        academicYearEnrollmentId = 0L,
        studentNumber = "",
        description = "",
        academicYear = 0,
        status = status,
    )
}
