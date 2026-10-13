package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState
import it.attendance100.mybicocca.domain.repository.EnrollmentRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Enrolled in the second year after a regular first one; renewal for next year is not open yet. */
@Singleton
class DemoEnrollmentRepository @Inject constructor() : EnrollmentRepository {

    override suspend fun getHistory(careerId: CareerId): EnrollmentHistory = EnrollmentHistory(
        years = listOf(year(2025, courseYear = 2), year(2024, courseYear = 1)),
        renewal = RenewalState.Enrolled(academicYear = 2025),
    )

    override fun renewalWebUrl(careerId: CareerId): String = DemoElearning.BASE_URL

    private fun year(academicYear: Int, courseYear: Int) = AnnualEnrollment(
        id = EnrollmentId(720000L + courseYear),
        academicYear = academicYear,
        courseYear = courseYear,
        outOfCourseYears = 0,
        type = EnrollmentType.InProgress,
        typeDescription = "In corso",
        status = EnrollmentStatus.Active,
        statusReasonCode = null,
        conditional = false,
        reconstructed = false,
        partTime = null,
        suspension = null,
        awaitingDegree = false,
        degreeAwardDate = null,
        studentTypeDescription = "Studente a tempo pieno",
        exemptionDescription = "Esonero parziale per reddito",
        incomeBandId = 4,
        canteenBandId = 2,
        meritBandId = null,
        meritNote = null,
        enrollmentNote = null,
        disabilityPercentage = null,
        disabilityTypeDescription = null,
        courseDescription = DemoCareer.DEGREE,
        courseTypeDescription = "Corso di Laurea",
        degreeClassCode = "L-31",
        degreeClassDescription = "Scienze e tecnologie informatiche",
        orientationDescription = null,
        addressDescription = "Percorso comune",
        studyOrderDescription = "Ordinamento 2020",
        minimumCredits = DemoCareer.TOTAL_CREDITS.toInt(),
        courseDuration = 3,
        teachingLanguage = "Italiano",
        regulationCode = "E3101Q-20",
        universityDescription = "Università degli Studi di Milano-Bicocca",
        siteDescription = "Milano",
        enrollmentDate = LocalDate.of(academicYear, 9, if (courseYear == 1) 18 else 2),
        insertionDate = LocalDate.of(academicYear, 9, if (courseYear == 1) 18 else 2),
        modificationDate = LocalDate.of(academicYear, 10, 1),
    )
}
