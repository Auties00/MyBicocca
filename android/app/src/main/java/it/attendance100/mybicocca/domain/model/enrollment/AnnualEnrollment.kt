package it.attendance100.mybicocca.domain.model.enrollment

import java.time.LocalDate

/**
 * A single academic-year enrollment ("iscrizione annuale") of a career — one per year.
 * Sourced from Esse3's annual-enrollment list and rendered as the career timeline by the
 * enrollments sub-screen in the registry tab.
 *
 * Surfaces the meaningful slice of the ~87-field Esse3 `IscrizioneAnnuale`; flags that
 * are commonly 0 for a clean career (part-time, suspension, conditional, degree-award)
 * are kept because they materially change another student's situation.
 *
 * @property id Stable identity of the enrollment row.
 * @property academicYear Academic year the enrollment refers to, e.g. 2024 means
 *   a.y. 2024/2025.
 * @property courseYear Year of the course (anno di corso), 1-based.
 * @property outOfCourseYears Years off-course (anni fuori corso); greater than 0 only
 *   once the legal duration is exceeded.
 * @property type In-course / out-of-course / repeating classification.
 * @property typeDescription Server-provided description of [type], useful when the code
 *   is unrecognised.
 * @property status Whether the enrollment is active, canceled or suspended.
 * @property statusReasonCode Esse3 motivation code behind a non-active [status].
 * @property conditional True for enrollments accepted "con riserva".
 * @property reconstructed True when the year was reconstructed (ricostruzione di
 *   carriera) rather than enrolled normally.
 * @property partTime Part-time terms for the year; null when enrolled full-time.
 * @property suspension Suspension details; null when the year was not suspended.
 * @property awaitingDegree True when the student has finished and awaits the degree
 *   award.
 * @property degreeAwardDate Date the degree was (or is scheduled to be) awarded.
 * @property studentTypeDescription Esse3 student-typology label (`tipoStuDes`).
 * @property exemptionDescription Tuition exemption label, when an exemption applies.
 * @property incomeBandId Income (ISEE) band the year was billed under.
 * @property canteenBandId Canteen-fee band assigned for the year.
 * @property meritBandId Merit band assigned for the year.
 * @property meritNote Free-text note attached to the merit evaluation.
 * @property enrollmentNote Free-text note attached to the enrollment.
 * @property disabilityPercentage Recognised disability percentage; null when none or
 *   zero.
 * @property disabilityTypeDescription Description of the recognised disability type.
 * @property courseDescription Name of the course of study for the year.
 * @property courseTypeDescription Kind of course, e.g. "Corso di Laurea Magistrale".
 * @property degreeClassCode Ministerial degree class code, e.g. "LM-18".
 * @property degreeClassDescription Human description of the degree class.
 * @property orientationDescription Orientamento label, when the course defines one.
 * @property addressDescription Indirizzo label, when the course defines one.
 * @property studyOrderDescription Ordinamento label of the regulation followed.
 * @property minimumCredits Minimum CFU required to earn the degree (Esse3 `valoreMin`).
 * @property courseDuration Legal duration of the course in years.
 * @property teachingLanguage Main teaching language of the course.
 * @property regulationCode Code of the regulation (normativa) the year follows.
 * @property universityDescription Awarding university name.
 * @property siteDescription Campus/site the course is taught at.
 * @property enrollmentDate Day the year's enrollment was registered.
 * @property insertionDate Day the row was inserted in Esse3.
 * @property modificationDate Day the row was last modified in Esse3.
 */
data class AnnualEnrollment(
    val id: EnrollmentId,
    val academicYear: Int,
    val courseYear: Int,
    val outOfCourseYears: Int,
    val type: EnrollmentType,
    val typeDescription: String?,
    val status: EnrollmentStatus,
    val statusReasonCode: String?,
    val conditional: Boolean,
    val reconstructed: Boolean,
    val partTime: PartTimeInfo?,
    val suspension: SuspensionInfo?,
    val awaitingDegree: Boolean,
    val degreeAwardDate: LocalDate?,
    val studentTypeDescription: String?,
    val exemptionDescription: String?,
    val incomeBandId: Long?,
    val canteenBandId: Long?,
    val meritBandId: Long?,
    val meritNote: String?,
    val enrollmentNote: String?,
    val disabilityPercentage: Float?,
    val disabilityTypeDescription: String?,
    val courseDescription: String?,
    val courseTypeDescription: String?,
    val degreeClassCode: String?,
    val degreeClassDescription: String?,
    val orientationDescription: String?,
    val addressDescription: String?,
    val studyOrderDescription: String?,
    val minimumCredits: Int?,
    val courseDuration: Int?,
    val teachingLanguage: String?,
    val regulationCode: String?,
    val universityDescription: String?,
    val siteDescription: String?,
    val enrollmentDate: LocalDate?,
    val insertionDate: LocalDate?,
    val modificationDate: LocalDate?,
)
