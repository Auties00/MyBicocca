package it.attendance100.mybicocca.data.mapper.enrollment

import it.attendance100.mybicocca.data.local.enrollment.AnnualEnrollmentEntity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentId
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentType
import it.attendance100.mybicocca.domain.model.enrollment.PartTimeInfo
import it.attendance100.mybicocca.domain.model.enrollment.SuspensionInfo
import java.time.LocalDate

// Entity <-> domain mapping for the offline enrollment-history mirror. The nested part-time and
// suspension objects are flattened behind a presence flag and reconstructed only when set; enums
// round-trip by name with an Unknown fallback; dates round-trip through ISO-8601 strings, with
// unparseable values dropped to null.

fun AnnualEnrollment.toEntity(careerId: CareerId, order: Int): AnnualEnrollmentEntity =
    AnnualEnrollmentEntity(
        careerId = careerId.value,
        enrollmentId = id.value,
        cacheOrder = order,
        academicYear = academicYear,
        courseYear = courseYear,
        outOfCourseYears = outOfCourseYears,
        type = type.name,
        typeDescription = typeDescription,
        status = status.name,
        statusReasonCode = statusReasonCode,
        conditional = conditional,
        reconstructed = reconstructed,
        hasPartTime = partTime != null,
        partTimeCredits = partTime?.credits,
        partTimeExtraCredits = partTime?.extraCredits,
        partTimeLocked = partTime?.locked,
        hasSuspension = suspension != null,
        suspensionReasonCode = suspension?.reasonCode,
        awaitingDegree = awaitingDegree,
        degreeAwardDate = degreeAwardDate?.toString(),
        studentTypeDescription = studentTypeDescription,
        exemptionDescription = exemptionDescription,
        incomeBandId = incomeBandId,
        canteenBandId = canteenBandId,
        meritBandId = meritBandId,
        meritNote = meritNote,
        enrollmentNote = enrollmentNote,
        disabilityPercentage = disabilityPercentage,
        disabilityTypeDescription = disabilityTypeDescription,
        courseDescription = courseDescription,
        courseTypeDescription = courseTypeDescription,
        degreeClassCode = degreeClassCode,
        degreeClassDescription = degreeClassDescription,
        orientationDescription = orientationDescription,
        addressDescription = addressDescription,
        studyOrderDescription = studyOrderDescription,
        minimumCredits = minimumCredits,
        courseDuration = courseDuration,
        teachingLanguage = teachingLanguage,
        regulationCode = regulationCode,
        universityDescription = universityDescription,
        siteDescription = siteDescription,
        enrollmentDate = enrollmentDate?.toString(),
        insertionDate = insertionDate?.toString(),
        modificationDate = modificationDate?.toString(),
    )

fun AnnualEnrollmentEntity.toDomain(): AnnualEnrollment = AnnualEnrollment(
    id = EnrollmentId(enrollmentId),
    academicYear = academicYear,
    courseYear = courseYear,
    outOfCourseYears = outOfCourseYears,
    type = type.toEnrollmentType(),
    typeDescription = typeDescription,
    status = status.toEnrollmentStatus(),
    statusReasonCode = statusReasonCode,
    conditional = conditional,
    reconstructed = reconstructed,
    partTime = if (hasPartTime) {
        PartTimeInfo(
            credits = partTimeCredits,
            extraCredits = partTimeExtraCredits,
            locked = partTimeLocked == true,
        )
    } else {
        null
    },
    suspension = if (hasSuspension) SuspensionInfo(reasonCode = suspensionReasonCode) else null,
    awaitingDegree = awaitingDegree,
    degreeAwardDate = degreeAwardDate.toLocalDateOrNull(),
    studentTypeDescription = studentTypeDescription,
    exemptionDescription = exemptionDescription,
    incomeBandId = incomeBandId,
    canteenBandId = canteenBandId,
    meritBandId = meritBandId,
    meritNote = meritNote,
    enrollmentNote = enrollmentNote,
    disabilityPercentage = disabilityPercentage,
    disabilityTypeDescription = disabilityTypeDescription,
    courseDescription = courseDescription,
    courseTypeDescription = courseTypeDescription,
    degreeClassCode = degreeClassCode,
    degreeClassDescription = degreeClassDescription,
    orientationDescription = orientationDescription,
    addressDescription = addressDescription,
    studyOrderDescription = studyOrderDescription,
    minimumCredits = minimumCredits,
    courseDuration = courseDuration,
    teachingLanguage = teachingLanguage,
    regulationCode = regulationCode,
    universityDescription = universityDescription,
    siteDescription = siteDescription,
    enrollmentDate = enrollmentDate.toLocalDateOrNull(),
    insertionDate = insertionDate.toLocalDateOrNull(),
    modificationDate = modificationDate.toLocalDateOrNull(),
)

private fun String.toEnrollmentType(): EnrollmentType =
    runCatching { EnrollmentType.valueOf(this) }.getOrDefault(EnrollmentType.Unknown)

private fun String.toEnrollmentStatus(): EnrollmentStatus =
    runCatching { EnrollmentStatus.valueOf(this) }.getOrDefault(EnrollmentStatus.Unknown)

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
