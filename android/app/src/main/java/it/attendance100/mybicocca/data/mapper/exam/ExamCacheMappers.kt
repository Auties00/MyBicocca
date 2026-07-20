package it.attendance100.mybicocca.data.mapper.exam

import it.attendance100.mybicocca.data.local.exam.BookedExamEntity
import it.attendance100.mybicocca.data.local.exam.ExamCallEntity
import it.attendance100.mybicocca.data.local.exam.ExamResultEntity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.exam.ExamExaminer
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.model.exam.ExamType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Entity <-> domain mapping for the offline exam mirrors. The sealed ExamGrade is encoded as a
// kind discriminator plus an optional numeric value; enums round-trip by name with an Unknown
// fallback; dates round-trip through ISO-8601 strings, with unparseable values dropped to null.

fun BookedExam.toEntity(careerId: CareerId, order: Int): BookedExamEntity = BookedExamEntity(
    careerId = careerId.value,
    courseOfStudyId = key.courseOfStudyId,
    activityId = key.activityId,
    callId = key.callId,
    cacheOrder = order,
    applicationListId = applicationListId,
    studentId = studentId,
    activityChoiceId = activityChoiceId,
    activityDescription = activityDescription,
    examCallDescription = examCallDescription,
    examType = examType.name,
    callType = callType.name,
    examDateTime = examDateTime?.toString(),
    classroomDescription = classroomDescription,
    buildingDescription = buildingDescription,
    credits = credits,
    examModeDescription = examModeDescription,
    position = position,
    totalBookings = totalBookings,
    bookingDate = bookingDate?.toString(),
    cancellableUntil = cancellableUntil?.toString(),
    studentNote = studentNote,
    gradeKind = grade.kind(),
    gradeValue = grade.numericValue(),
    outcomePublished = outcomePublished,
    publishedNote = publishedNote,
)

fun BookedExamEntity.toDomain(): BookedExam = BookedExam(
    key = ExamCallKey(courseOfStudyId, activityId, callId),
    applicationListId = applicationListId,
    studentId = studentId,
    activityChoiceId = activityChoiceId,
    activityDescription = activityDescription,
    examCallDescription = examCallDescription,
    examType = examType.toExamType(),
    callType = callType.toExamCallType(),
    examDateTime = examDateTime.toLocalDateTimeOrNull(),
    classroomDescription = classroomDescription,
    buildingDescription = buildingDescription,
    credits = credits,
    examModeDescription = examModeDescription,
    position = position,
    totalBookings = totalBookings,
    bookingDate = bookingDate.toLocalDateTimeOrNull(),
    cancellableUntil = cancellableUntil.toLocalDateOrNull(),
    studentNote = studentNote,
    grade = examGrade(gradeKind, gradeValue),
    outcomePublished = outcomePublished,
    publishedNote = publishedNote,
)

fun ExamCall.toEntity(careerId: CareerId, order: Int): ExamCallEntity = ExamCallEntity(
    careerId = careerId.value,
    courseOfStudyId = key.courseOfStudyId,
    activityId = key.activityId,
    callId = key.callId,
    cacheOrder = order,
    examCallId = examCallId,
    activityChoiceId = activityChoiceId,
    activityCode = activityCode,
    activityDescription = activityDescription,
    courseOfStudyDescription = courseOfStudyDescription,
    callDescription = callDescription,
    callDate = callDate?.toString(),
    callTime = callTime?.toString(),
    windowOpensAt = enrollmentWindow.opensAt?.toString(),
    windowClosesAt = enrollmentWindow.closesAt?.toString(),
    enrolledNumber = enrolledNumber,
    state = state,
    stateDescription = stateDescription,
    callType = callType.name,
    examType = examType.name,
    isReserved = isReserved,
    matId = matId,
    notes = notes,
    presidentId = president?.id,
    presidentName = president?.name,
    presidentSurname = president?.surname,
    bookingTypeDescription = bookingTypeDescription,
)

fun ExamCallEntity.toDomain(): ExamCall = ExamCall(
    key = ExamCallKey(courseOfStudyId, activityId, callId),
    examCallId = examCallId,
    activityChoiceId = activityChoiceId,
    activityCode = activityCode,
    activityDescription = activityDescription,
    courseOfStudyDescription = courseOfStudyDescription,
    callDescription = callDescription,
    callDate = callDate.toLocalDateOrNull(),
    callTime = callTime.toLocalTimeOrNull(),
    enrollmentWindow = ExamEnrollmentWindow(
        opensAt = windowOpensAt.toLocalDateOrNull(),
        closesAt = windowClosesAt.toLocalDateOrNull(),
    ),
    enrolledNumber = enrolledNumber,
    state = state,
    stateDescription = stateDescription,
    callType = callType.toExamCallType(),
    examType = examType.toExamType(),
    isReserved = isReserved,
    matId = matId,
    notes = notes,
    president = examiner(presidentId, presidentName, presidentSurname),
    bookingTypeDescription = bookingTypeDescription,
)

fun ExamResult.toEntity(careerId: CareerId, order: Int): ExamResultEntity = ExamResultEntity(
    careerId = careerId.value,
    courseOfStudyId = key.courseOfStudyId,
    activityId = key.activityId,
    callId = key.callId,
    cacheOrder = order,
    applicationListId = applicationListId,
    publicationId = publicationId,
    activityDescription = activityDescription,
    examDateTime = examDateTime?.toString(),
    gradeKind = grade.kind(),
    gradeValue = grade.numericValue(),
    acknowledgment = acknowledgment.name,
    publishedNote = publishedNote,
    acknowledgmentDeadline = acknowledgmentDeadline?.toString(),
)

fun ExamResultEntity.toDomain(): ExamResult = ExamResult(
    key = ExamCallKey(courseOfStudyId, activityId, callId),
    applicationListId = applicationListId,
    publicationId = publicationId,
    activityDescription = activityDescription,
    examDateTime = examDateTime.toLocalDateTimeOrNull(),
    grade = examGrade(gradeKind, gradeValue),
    acknowledgment = runCatching { AcknowledgmentStatus.valueOf(acknowledgment) }
        .getOrDefault(AcknowledgmentStatus.Unknown),
    publishedNote = publishedNote,
    acknowledgmentDeadline = acknowledgmentDeadline.toLocalDateOrNull(),
)

private fun ExamGrade.kind(): String = when (this) {
    is ExamGrade.Numeric -> "numeric"
    ExamGrade.Passed -> "passed"
    ExamGrade.NotPassed -> "not_passed"
    ExamGrade.Withdrew -> "withdrew"
    ExamGrade.Absent -> "absent"
    ExamGrade.Unknown -> "unknown"
}

private fun ExamGrade.numericValue(): Int? = (this as? ExamGrade.Numeric)?.value

private fun examGrade(kind: String, value: Int?): ExamGrade = when (kind) {
    "numeric" -> ExamGrade.Numeric(value ?: return ExamGrade.Unknown)
    "passed" -> ExamGrade.Passed
    "not_passed" -> ExamGrade.NotPassed
    "withdrew" -> ExamGrade.Withdrew
    "absent" -> ExamGrade.Absent
    else -> ExamGrade.Unknown
}

private fun examiner(id: Long?, name: String?, surname: String?): ExamExaminer? =
    if (id == null && name == null && surname == null) null else ExamExaminer(id, name, surname)

private fun String.toExamType(): ExamType =
    runCatching { ExamType.valueOf(this) }.getOrDefault(ExamType.Unknown)

private fun String.toExamCallType(): ExamCallType =
    runCatching { ExamCallType.valueOf(this) }.getOrDefault(ExamCallType.Other)

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

private fun String?.toLocalTimeOrNull(): LocalTime? =
    this?.let { runCatching { LocalTime.parse(it) }.getOrNull() }

private fun String?.toLocalDateTimeOrNull(): LocalDateTime? =
    this?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
