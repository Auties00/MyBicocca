package it.attendance100.mybicocca.data.mapper.exam

import it.attendance100.mybicocca.data.mapper.common.parseEsse3Date
import it.attendance100.mybicocca.data.mapper.common.parseEsse3DateTime
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val ESSE3_TIME = DateTimeFormatter.ofPattern("HH:mm:ss")

/**
 * Maps a bookable-calls row from Esse3's record-book exam-call list to the domain exam
 * call. Returns null when any component of the call's identity (cdsId/adId/appId) is
 * missing, since such a row cannot be booked or correlated.
 */
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
        callDate = callStartDate.parseEsse3Date(),
        callTime = graduationTime.parseTime(),
        enrollmentWindow = ExamEnrollmentWindow(
            opensAt = enrollmentStartDate.parseEsse3Date(),
            closesAt = enrollmentEndDate.parseEsse3Date(),
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

/**
 * Maps a booking row to the lean booking handle (ids only), used where the full booked
 * exam payload is not needed. Returns null when the call identity is incomplete.
 */
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

/**
 * Maps a booking row with a published outcome to the domain exam result. The activity
 * name prefers the libretto spelling (`adStuDes`) over the call description (`desAppello`)
 * — Esse3 duplicates the name across both. The acknowledgment deadline comes from
 * `dataRifEsitoStu`, a "DD/MM/YYYY HH:mm:ss" value whose time component is a fixed
 * 23:59:59, so only the date part is kept. Returns null when the call identity is
 * incomplete.
 */
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
        examDateTime = shiftDateTime.parseEsse3DateTime(),
        grade = outcome.toExamGrade(),
        acknowledgment = acknowledgmentOfReceipt.toAcknowledgmentStatus(),
        publishedNote = publicNote?.takeIf { it.isNotBlank() },
        acknowledgmentDeadline = studentOutcomeReferenceDate.parseEsse3DateTime()?.toLocalDate(),
    )
}

/**
 * Decodes Esse3's untyped `esito` object into a grade: absence and withdrawal flags win
 * over the numeric grade, which wins over the bare pass/fail flag.
 */
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

/**
 * Maps a booking-history row to the domain booked exam. Esse3 duplicates information
 * across fields, so validated dedup heuristics apply: the activity name prefers the
 * libretto spelling (`adStuDes`) and falls back to the call description (`desAppello`),
 * which usually repeats it; the exam mode comes from `tipoEsaCod`, which the DTO calls
 * `graduationTypeCode`. The outcome is interpreted only once it is published — an
 * unpublished row carries `superatoFlg=0`, which would otherwise decode as NotPassed —
 * matching the contract documented on the domain model's grade property. Returns null
 * when the call identity is incomplete.
 */
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
        examType = graduationTypeCode.toExamType(),
        callType = callTypeCode.toCallType(),
        examDateTime = shiftDateTime.parseEsse3DateTime(),
        classroomDescription = classroomDescription?.takeIf { it.isNotBlank() },
        buildingDescription = buildingDescription?.takeIf { it.isNotBlank() },
        credits = teachingActivityWeight,
        examModeDescription = examTypeDescription?.takeIf { it.isNotBlank() },
        position = applicationPosition,
        bookingDate = insertionDate.parseEsse3DateTime(),
        cancellableUntil = enrollmentEndDate.parseEsse3Date(),
        studentNote = studentNote?.takeIf { it.isNotBlank() },
        grade = if (publicationId != null) outcome.toExamGrade() else ExamGrade.Unknown,
        outcomePublished = publicationId != null,
        publishedNote = publicNote?.takeIf { it.isNotBlank() },
    )
}

private fun String?.parseTime(): LocalTime? {
    val raw = this?.trim() ?: return null
    val timePart = raw.substringAfter(' ', missingDelimiterValue = "").ifBlank { raw }
    return runCatching { LocalTime.parse(timePart, ESSE3_TIME) }.getOrNull()
}

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
