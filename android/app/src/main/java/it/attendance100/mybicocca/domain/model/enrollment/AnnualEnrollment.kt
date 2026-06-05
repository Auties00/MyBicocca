package it.attendance100.mybicocca.domain.model.enrollment

import java.time.LocalDate

// A single academic-year enrollment ("iscrizione annuale") for a career. One per year.
// Surfaces the meaningful slice of the ~87-field Esse3 IscrizioneAnnuale; flags that are
// commonly 0 for a clean career (part-time, suspension, conditional, degree-award) are
// kept because they materially change another student's situation.
data class AnnualEnrollment(
    val id: EnrollmentId,
    // Academic year the enrollment refers to, e.g. 2024 means a.y. 2024/2025.
    val academicYear: Int,
    // Year of the course (anno di corso), 1-based.
    val courseYear: Int,
    // Years off-course (anni fuori corso); > 0 only once the legal duration is exceeded.
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
