package it.attendance100.mybicocca.domain.model.career

/**
 * One enrollment of a student in a study programme, as listed by Esse3 and cached in Room. A
 * student carries one career per programme attended; the account's selected career scopes most
 * features of the app.
 *
 * @property id Esse3 career id (`stuId`).
 * @property enrollmentTraitId Esse3 `matId`; the career identifier the transcript, exam-booking,
 *   and questionnaire endpoints expect. 0 when Esse3 omits it.
 * @property programId Esse3 course-of-study id (`cdsId`); keys study-plan lookups. 0 when
 *   omitted.
 * @property easyStaffProgramCode Mnemonic course-of-study code (`p06CdsCod`); joins the career
 *   to EasyStaff timetable data and public Esse3 catalogs. Null when Esse3 does not expose it.
 * @property academicYearEnrollmentId Enrollment academic year (`aaIscrId`); the cohort used for
 *   study-plan schema lookups. 0 when omitted.
 * @property studentNumber The student's registration number (matricola); printed on documents
 *   and badge views and sent to EasyBadge when certifying presence.
 * @property description Programme name shown in the career picker; falls back to the Esse3
 *   career-status description and may be empty.
 * @property academicYear Academic year the career started (`aaImm1`, falling back to `aaId`);
 *   ranks careers when choosing the default selection. 0 when unknown.
 * @property status Lifecycle state mapped from the Esse3 status code.
 */
data class Career(
    val id: CareerId,
    val enrollmentTraitId: Long,
    val programId: Long,
    val easyStaffProgramCode: String?,
    val academicYearEnrollmentId: Long,
    val studentNumber: String,
    val description: String,
    val academicYear: Int,
    val status: CareerStatus,
)
