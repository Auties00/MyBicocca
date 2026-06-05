package it.attendance100.mybicocca.data.mapper.enrollment

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import it.attendance100.mybicocca.domain.model.enrollment.PartTimeInfo
import it.attendance100.mybicocca.domain.model.enrollment.SuspensionInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun Esse3AnnualEnrollment.toDomain(): AnnualEnrollment? {
    val year = academicYearEnrollmentId ?: return null
    return AnnualEnrollment(
        id = EnrollmentId(enrollmentId ?: matId ?: year),
        academicYear = year.toInt(),
        courseYear = courseYear ?: 0,
        outOfCourseYears = fcYears ?: 0,
        type = EnrollmentType.fromCode(enrollmentTypeCode),
        typeDescription = enrollmentTypeDescription?.takeIf { it.isNotBlank() },
        status = EnrollmentStatus.fromCode(enrollmentStatusCode),
        statusReasonCode = enrollmentStatusReasonCode?.takeIf { it.isNotBlank() },
        conditional = conditionFlag == 1,
        reconstructed = searchFlag == 1,
        partTime = if (ptFlag == 1) {
            PartTimeInfo(
                credits = ptCredits,
                extraCredits = ptExtraCredits?.takeIf { it > 0 },
                locked = ptBlockedFlag == 1,
            )
        } else {
            null
        },
        suspension = if (suspensionFlag == 1) {
            SuspensionInfo(reasonCode = suspensionReasonCode?.takeIf { it.isNotBlank() })
        } else {
            null
        },
        awaitingDegree = degreeAwardFlag == 1,
        degreeAwardDate = degreeAwardDate.toEsse3LocalDate(),
        studentTypeDescription = studentTypeDescription?.takeIf { it.isNotBlank() },
        exemptionDescription = exemptionTypeDescription
            ?.takeIf { it.isNotBlank() && exemptionTypeCode?.uppercase() != "N" },
        incomeBandId = bandId,
        canteenBandId = canteenBandId,
        meritBandId = meritBandId,
        meritNote = meritNote?.takeIf { it.isNotBlank() },
        enrollmentNote = enrollmentNote?.takeIf { it.isNotBlank() },
        disabilityPercentage = disabilityPercentage?.takeIf { it > 0f },
        disabilityTypeDescription = handicapTypeDescription?.takeIf { it.isNotBlank() },
        courseDescription = courseOfStudyDescription?.takeIf { it.isNotBlank() },
        courseTypeDescription = courseTypeDescription?.takeIf { it.isNotBlank() },
        degreeClassCode = enrollmentClassCode?.takeIf { it.isNotBlank() }
            ?: classMurstCode?.takeIf { it.isNotBlank() },
        degreeClassDescription = classMurstDescription?.takeIf { it.isNotBlank() }
            ?: classUniversityDescription?.takeIf { it.isNotBlank() },
        orientationDescription = orientationDescription?.takeIf { it.isNotBlank() },
        addressDescription = addressDescription?.takeIf { it.isNotBlank() },
        studyOrderDescription = studyOrderDescription?.takeIf { it.isNotBlank() },
        minimumCredits = minimumValue?.toFloatOrNull()?.toInt(),
        courseDuration = courseDuration,
        teachingLanguage = teachingLanguage?.takeIf { it.isNotBlank() },
        regulationCode = normCode?.takeIf { it.isNotBlank() },
        universityDescription = universityDescription?.takeIf { it.isNotBlank() },
        siteDescription = siteDescription?.takeIf { it.isNotBlank() },
        enrollmentDate = enrollmentDate.toEsse3LocalDate(),
        insertionDate = insertionDate.toEsse3LocalDate(),
        modificationDate = modificationDate.toEsse3LocalDate(),
    )
}

// Esse3 returns dates as dd/MM/yyyy, sometimes with a trailing " HH:mm:ss" to strip.
private val ESSE3_DATE_PATTERNS = listOf(
    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    DateTimeFormatter.ISO_LOCAL_DATE,
)

private fun String?.toEsse3LocalDate(): LocalDate? {
    val raw = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val datePart = raw.substringBefore('T').substringBefore(' ')
    for (formatter in ESSE3_DATE_PATTERNS) {
        runCatching { return LocalDate.parse(datePart, formatter) }
    }
    return null
}
