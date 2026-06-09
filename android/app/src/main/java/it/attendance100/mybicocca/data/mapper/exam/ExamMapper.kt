package it.attendance100.mybicocca.data.mapper.exam

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AcknowledgmentOfReceipt
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionEnrollment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionTranscript
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GraduationTypeCode
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamBooking
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.exam.ExamExaminer
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.model.exam.ExamType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal val ESSE3_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val ESSE3_TIME = DateTimeFormatter.ofPattern("HH:mm:ss")
private val ESSE3_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

fun Esse3ExamSessionTranscript.toDomain(): ExamCall? {
    val cdsId = courseOfStudyId ?: return null
    val adId = activityId ?: return null
    val appId = callId ?: return null
    return ExamCall(
        key = ExamCallKey(cdsId, adId, appId),
        examCallId = examCallId,
        activityChoiceId = activityChoiceId,
        activityCode = activityCode,
        activityDescription = activityDescription,
        courseOfStudyDescription = courseOfStudyDescription,
        callDescription = callDescription,
        callDate = callStartDate.parseDate(),
        callTime = graduationTime.parseTime(),
        enrollmentWindow = ExamEnrollmentWindow(
            opensAt = enrollmentStartDate.parseDate(),
            closesAt = enrollmentEndDate.parseDate(),
        ),
        enrolledNumber = enrolledNumber,
        state = state,
        stateDescription = stateDescription,
        callType = callTypeCode.toCallType(),
        examType = graduationTypeCode.toExamType(),
        isReserved = reservedFlag == 1,
        matId = matId,
        notes = notes?.takeIf { it.isNotBlank() },
        president = if (presidentName != null || presidentSurname != null || presidentId != null) {
            ExamExaminer(id = presidentId, name = presidentName, surname = presidentSurname)
        } else null,
        bookingTypeDescription = bookingManagementTypeDescription?.takeIf { it.isNotBlank() },
    )
}

fun Esse3ExamSessionEnrollment.toDomain(): ExamBooking? {
    val cdsId = courseOfStudyId ?: return null
    val adId = activityId?.toLong() ?: return null
    val appId = callId ?: return null
    return ExamBooking(
        key = ExamCallKey(cdsId, adId, appId),
        applicationListId = applicationListId,
        studentId = studentId?.toLong(),
        activityChoiceId = activityChoiceId,
    )
}

fun Esse3ExamSessionEnrollment.toExamResult(): ExamResult? {
    val cdsId = courseOfStudyId ?: return null
    val adId = activityId?.toLong() ?: return null
    val appId = callId ?: return null
    return ExamResult(
        key = ExamCallKey(cdsId, adId, appId),
        applicationListId = applicationListId,
        publicationId = publicationId,
        activityDescription = studentActivityDescription?.takeIf { it.isNotBlank() }
            ?: examCallDescription,
        examDateTime = shiftDateTime.parseDateTime(),
        grade = outcome.toExamGrade(),
        acknowledgment = acknowledgmentOfReceipt.toAcknowledgmentStatus(),
        publishedNote = publicNote?.takeIf { it.isNotBlank() },
        // dataRifEsitoStu comes back as "DD/MM/YYYY HH:mm:ss" — strip the time, it's always 23:59:59.
        acknowledgmentDeadline = studentOutcomeReferenceDate.parseDateTime()?.toLocalDate(),
    )
}

private fun JsonObject?.toExamGrade(): ExamGrade {
    if (this == null) return ExamGrade.Unknown
    val votoEsa = this["votoEsa"]?.runCatching { jsonPrimitive.intOrNull }?.getOrNull()
    val absentFlg = this["assenteFlg"]?.runCatching { jsonPrimitive.intOrNull }?.getOrNull()
    val withdrewFlg = this["ritiratoFlg"]?.runCatching { jsonPrimitive.intOrNull }?.getOrNull()
    val passedFlg = this["superatoFlg"]?.runCatching { jsonPrimitive.intOrNull }?.getOrNull()
    return when {
        absentFlg == 1 -> ExamGrade.Absent
        withdrewFlg == 1 -> ExamGrade.Withdrew
        votoEsa != null -> ExamGrade.Numeric(votoEsa)
        passedFlg == 1 -> ExamGrade.Passed
        passedFlg == 0 -> ExamGrade.NotPassed
        else -> ExamGrade.Unknown
    }
}

private fun Esse3AcknowledgmentOfReceipt?.toAcknowledgmentStatus(): AcknowledgmentStatus =
    when (this) {
        Esse3AcknowledgmentOfReceipt.NotViewed -> AcknowledgmentStatus.NotViewed
        Esse3AcknowledgmentOfReceipt.Viewed -> AcknowledgmentStatus.Viewed
        Esse3AcknowledgmentOfReceipt.Accepted -> AcknowledgmentStatus.Accepted
        Esse3AcknowledgmentOfReceipt.Rejected -> AcknowledgmentStatus.Rejected
        is Esse3AcknowledgmentOfReceipt.Unknown, null -> AcknowledgmentStatus.Unknown
    }

fun Esse3ExamSessionEnrollment.toBookedExam(): BookedExam? {
    val cdsId = courseOfStudyId ?: return null
    val adId = activityId?.toLong() ?: return null
    val appId = callId ?: return null
    return BookedExam(
        key = ExamCallKey(cdsId, adId, appId),
        applicationListId = applicationListId,
        studentId = studentId?.toLong(),
        activityChoiceId = activityChoiceId,
        activityDescription = studentActivityDescription?.takeIf { it.isNotBlank() }
            ?: examCallDescription,
        examCallDescription = examCallDescription,
        // tipoEsaCod — the DTO calls it graduationTypeCode, but it's the call's exam mode
        examType = graduationTypeCode.toExamType(),
        callType = callTypeCode.toCallType(),
        examDateTime = shiftDateTime.parseDateTime(),
        classroomDescription = classroomDescription?.takeIf { it.isNotBlank() },
        buildingDescription = buildingDescription?.takeIf { it.isNotBlank() },
        credits = teachingActivityWeight,
        examModeDescription = examTypeDescription?.takeIf { it.isNotBlank() },
        position = applicationPosition,
        bookingDate = insertionDate.parseDateTime(),
        cancellableUntil = enrollmentEndDate.parseDate(),
        studentNote = studentNote?.takeIf { it.isNotBlank() },
        // Interpret the outcome only once it is published — see BookedExam.grade. An
        // un-published row carries superatoFlg=0, which toExamGrade would map to NotPassed.
        grade = if (publicationId != null) outcome.toExamGrade() else ExamGrade.Unknown,
        outcomePublished = publicationId != null,
        publishedNote = publicNote?.takeIf { it.isNotBlank() },
    )
}

private fun String?.parseDate(): LocalDate? {
    val raw = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val datePart = raw.substringBefore(' ')
    return runCatching { LocalDate.parse(datePart, ESSE3_DATE) }.getOrNull()
}

private fun String?.parseTime(): LocalTime? {
    val raw = this?.trim() ?: return null
    val timePart = raw.substringAfter(' ', missingDelimiterValue = "").ifBlank { raw }
    return runCatching { LocalTime.parse(timePart, ESSE3_TIME) }.getOrNull()
}

private fun String?.parseDateTime(): LocalDateTime? =
    this?.trim()?.takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalDateTime.parse(it, ESSE3_DATE_TIME) }.getOrNull() }

private fun String?.toCallType(): ExamCallType = when (this?.trim()?.uppercase()) {
    "PF" -> ExamCallType.Final
    "PP" -> ExamCallType.Partial
    else -> ExamCallType.Other
}

private fun String?.toExamType(): ExamType = when (this?.trim()?.uppercase()) {
    "S" -> ExamType.Written
    "O" -> ExamType.Oral
    "SOC" -> ExamType.WrittenAndOralJoint
    "SOS" -> ExamType.WrittenAndOralSeparate
    else -> ExamType.Unknown
}

private fun Esse3GraduationTypeCode?.toExamType(): ExamType = when (this) {
    Esse3GraduationTypeCode.Written -> ExamType.Written
    Esse3GraduationTypeCode.Oral -> ExamType.Oral
    Esse3GraduationTypeCode.WrittenOralConsecutive -> ExamType.WrittenAndOralJoint
    Esse3GraduationTypeCode.WrittenOralSimultaneous -> ExamType.WrittenAndOralSeparate
    is Esse3GraduationTypeCode.Unknown, null -> ExamType.Unknown
}
