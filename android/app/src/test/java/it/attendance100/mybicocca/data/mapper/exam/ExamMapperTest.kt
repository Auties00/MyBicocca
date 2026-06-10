package it.attendance100.mybicocca.data.mapper.exam

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3AcknowledgmentOfReceipt
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionEnrollment
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ExamSessionTranscript
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3GraduationTypeCode
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Covers the live Esse3-DTO-to-domain exam mapping. The load-bearing case is the published-outcome
 * sentinel: a booking row carries `superatoFlg=0` even before publication, so [toBookedExam] only
 * trusts the grade when `pubblId` is present — asserted on both sides. Also covers identity-guard
 * nulls, the activity-name dedup fallback, call/exam-type code decoding, and date/time parsing.
 */
class ExamMapperTest {

    private fun outcome(
        votoEsa: Int? = null,
        absent: Int? = null,
        withdrew: Int? = null,
        passed: Int? = null,
    ): JsonObject = buildJsonObject {
        votoEsa?.let { put("votoEsa", JsonPrimitive(it)) }
        absent?.let { put("assenteFlg", JsonPrimitive(it)) }
        withdrew?.let { put("ritiratoFlg", JsonPrimitive(it)) }
        passed?.let { put("superatoFlg", JsonPrimitive(it)) }
    }

    private fun enrollment(
        cdsId: Long? = 10L,
        adId: Int? = 20,
        appId: Int? = 3,
        publicationId: Long? = null,
        outcome: JsonObject? = null,
        studentActivityDescription: String? = "Analisi I",
        examCallDescription: String? = "Appello di Analisi I",
        graduationTypeCode: String? = "S",
        callTypeCode: String? = "PF",
        shiftDateTime: String? = "15/01/2025 09:30:00",
        insertionDate: String? = "10/12/2024 12:00:00",
        enrollmentEndDate: String? = "14/01/2025",
        studentOutcomeReferenceDate: String? = null,
        publicNote: String? = null,
        studentNote: String? = null,
        classroomDescription: String? = "Aula U6-01",
        buildingDescription: String? = "Edificio U6",
        examTypeDescription: String? = "Scritto",
        position: Int? = 5,
        weight: Float? = 6f,
        acknowledgment: Esse3AcknowledgmentOfReceipt? = null,
        applicationListId: Long? = 999L,
        studentId: Int? = 100,
        activityChoiceId: Long? = 77L,
    ) = Esse3ExamSessionEnrollment(
        applicationListId = applicationListId,
        courseOfStudyId = cdsId,
        activityId = adId,
        callId = appId,
        studentId = studentId,
        activityChoiceId = activityChoiceId,
        studentActivityDescription = studentActivityDescription,
        examCallDescription = examCallDescription,
        classroomDescription = classroomDescription,
        buildingDescription = buildingDescription,
        teachingActivityWeight = weight,
        publicationId = publicationId,
        acknowledgmentOfReceipt = acknowledgment,
        studentOutcomeReferenceDate = studentOutcomeReferenceDate,
        publicNote = publicNote,
        outcome = outcome,
        shiftDateTime = shiftDateTime,
        studentNote = studentNote,
        examTypeDescription = examTypeDescription,
        insertionDate = insertionDate,
        callTypeCode = callTypeCode,
        applicationPosition = position,
        enrollmentEndDate = enrollmentEndDate,
        graduationTypeCode = graduationTypeCode,
    )

    private fun transcript(
        cdsId: Long? = 10L,
        adId: Long? = 20L,
        appId: Int? = 3,
        graduationTypeCode: Esse3GraduationTypeCode? = Esse3GraduationTypeCode.Written,
        callTypeCode: String? = "PF",
        reservedFlag: Int? = 0,
        graduationTime: String? = "15/01/2025 09:30:00",
        callStartDate: String? = "15/01/2025",
        enrollmentStartDate: String? = "01/01/2025",
        enrollmentEndDate: String? = "14/01/2025",
        presidentName: String? = null,
        presidentSurname: String? = null,
        presidentId: Long? = null,
        notes: String? = null,
        bookingManagementTypeDescription: String? = null,
    ) = Esse3ExamSessionTranscript(
        courseOfStudyId = cdsId,
        activityId = adId,
        callId = appId,
        examCallId = 5000L,
        activityChoiceId = 77L,
        activityCode = "E3101Q123",
        activityDescription = "Analisi I",
        courseOfStudyDescription = "Informatica",
        callDescription = "Appello di Analisi I",
        callStartDate = callStartDate,
        graduationTime = graduationTime,
        enrollmentStartDate = enrollmentStartDate,
        enrollmentEndDate = enrollmentEndDate,
        enrolledNumber = 42,
        state = "A",
        stateDescription = "Aperto",
        callTypeCode = callTypeCode,
        graduationTypeCode = graduationTypeCode,
        reservedFlag = reservedFlag,
        matId = 9L,
        notes = notes,
        presidentName = presidentName,
        presidentSurname = presidentSurname,
        presidentId = presidentId,
        bookingManagementTypeDescription = bookingManagementTypeDescription,
    )

    @Test
    fun `transcript maps to an exam call with its key and decoded fields`() {
        val call = transcript().toDomain()!!

        assertThat(call.key).isEqualTo(ExamCallKey(10L, 20L, 3))
        assertThat(call.examCallId).isEqualTo(5000L)
        assertThat(call.activityCode).isEqualTo("E3101Q123")
        assertThat(call.callDate).isEqualTo(LocalDate.of(2025, 1, 15))
        assertThat(call.callTime).isEqualTo(LocalTime.of(9, 30, 0))
        assertThat(call.enrollmentWindow.opensAt).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(call.enrollmentWindow.closesAt).isEqualTo(LocalDate.of(2025, 1, 14))
        assertThat(call.callType).isEqualTo(ExamCallType.Final)
        assertThat(call.examType).isEqualTo(ExamType.Written)
        assertThat(call.isReserved).isFalse()
        assertThat(call.president).isNull()
    }

    @Test
    fun `transcript returns null when any identity component is missing`() {
        assertThat(transcript(cdsId = null).toDomain()).isNull()
        assertThat(transcript(adId = null).toDomain()).isNull()
        assertThat(transcript(appId = null).toDomain()).isNull()
    }

    @Test
    fun `reservedFlag of one marks the call reserved`() {
        assertThat(transcript(reservedFlag = 1).toDomain()!!.isReserved).isTrue()
    }

    @Test
    fun `a president is built when any of its parts is present`() {
        val onlyId = transcript(presidentId = 8L).toDomain()!!.president
        assertThat(onlyId).isNotNull()
        assertThat(onlyId!!.id).isEqualTo(8L)
        assertThat(onlyId.name).isNull()

        val full = transcript(presidentName = "Anna", presidentSurname = "Bianchi", presidentId = 8L)
            .toDomain()!!.president
        assertThat(full!!.name).isEqualTo("Anna")
        assertThat(full.surname).isEqualTo("Bianchi")
    }

    @Test
    fun `blank notes and booking type collapse to null`() {
        val call = transcript(notes = "   ", bookingManagementTypeDescription = "").toDomain()!!

        assertThat(call.notes).isNull()
        assertThat(call.bookingTypeDescription).isNull()
    }

    @Test
    fun `graduation time without a date part still parses the time`() {
        assertThat(transcript(graduationTime = "09:30:00").toDomain()!!.callTime)
            .isEqualTo(LocalTime.of(9, 30, 0))
    }

    @Test
    fun `the DTO graduation type code decodes each exam mode`() {
        assertThat(transcript(graduationTypeCode = Esse3GraduationTypeCode.Written).toDomain()!!.examType)
            .isEqualTo(ExamType.Written)
        assertThat(transcript(graduationTypeCode = Esse3GraduationTypeCode.Oral).toDomain()!!.examType)
            .isEqualTo(ExamType.Oral)
        assertThat(transcript(graduationTypeCode = Esse3GraduationTypeCode.WrittenOralConsecutive).toDomain()!!.examType)
            .isEqualTo(ExamType.WrittenAndOralJoint)
        assertThat(transcript(graduationTypeCode = Esse3GraduationTypeCode.WrittenOralSimultaneous).toDomain()!!.examType)
            .isEqualTo(ExamType.WrittenAndOralSeparate)
        assertThat(transcript(graduationTypeCode = Esse3GraduationTypeCode.Unknown("X")).toDomain()!!.examType)
            .isEqualTo(ExamType.Unknown)
        assertThat(transcript(graduationTypeCode = null).toDomain()!!.examType)
            .isEqualTo(ExamType.Unknown)
    }

    @Test
    fun `call type code decodes final partial and other`() {
        assertThat(transcript(callTypeCode = "PF").toDomain()!!.callType).isEqualTo(ExamCallType.Final)
        assertThat(transcript(callTypeCode = "PP").toDomain()!!.callType).isEqualTo(ExamCallType.Partial)
        assertThat(transcript(callTypeCode = "ZZ").toDomain()!!.callType).isEqualTo(ExamCallType.Other)
        assertThat(transcript(callTypeCode = null).toDomain()!!.callType).isEqualTo(ExamCallType.Other)
    }

    @Test
    fun `enrollment maps to a lean booking handle`() {
        val booking = enrollment().toDomain()!!

        assertThat(booking.key).isEqualTo(ExamCallKey(10L, 20L, 3))
        assertThat(booking.applicationListId).isEqualTo(999L)
        assertThat(booking.studentId).isEqualTo(100L)
        assertThat(booking.activityChoiceId).isEqualTo(77L)
    }

    @Test
    fun `enrollment booking returns null on an incomplete identity`() {
        assertThat(enrollment(cdsId = null).toDomain()).isNull()
        assertThat(enrollment(adId = null).toDomain()).isNull()
        assertThat(enrollment(appId = null).toDomain()).isNull()
    }

    @Test
    fun `toExamResult decodes a numeric grade and the acknowledgment status`() {
        val result = enrollment(
            publicationId = 1L,
            outcome = outcome(votoEsa = 28),
            acknowledgment = Esse3AcknowledgmentOfReceipt.Viewed,
            studentOutcomeReferenceDate = "20/01/2025 23:59:59",
        ).toExamResult()!!

        assertThat(result.grade).isEqualTo(ExamGrade.Numeric(28))
        assertThat(result.acknowledgment).isEqualTo(AcknowledgmentStatus.Viewed)
        assertThat(result.acknowledgmentDeadline).isEqualTo(LocalDate.of(2025, 1, 20))
        assertThat(result.examDateTime).isEqualTo(LocalDateTime.of(2025, 1, 15, 9, 30, 0))
    }

    @Test
    fun `toExamResult prefers the libretto name over the call description`() {
        val preferred = enrollment(studentActivityDescription = "Analisi I", examCallDescription = "Call X")
            .toExamResult()!!
        assertThat(preferred.activityDescription).isEqualTo("Analisi I")

        val fallback = enrollment(studentActivityDescription = "  ", examCallDescription = "Call X")
            .toExamResult()!!
        assertThat(fallback.activityDescription).isEqualTo("Call X")
    }

    @Test
    fun `toExamResult maps every acknowledgment code`() {
        fun ack(code: Esse3AcknowledgmentOfReceipt?) =
            enrollment(acknowledgment = code).toExamResult()!!.acknowledgment

        assertThat(ack(Esse3AcknowledgmentOfReceipt.NotViewed)).isEqualTo(AcknowledgmentStatus.NotViewed)
        assertThat(ack(Esse3AcknowledgmentOfReceipt.Viewed)).isEqualTo(AcknowledgmentStatus.Viewed)
        assertThat(ack(Esse3AcknowledgmentOfReceipt.Accepted)).isEqualTo(AcknowledgmentStatus.Accepted)
        assertThat(ack(Esse3AcknowledgmentOfReceipt.Rejected)).isEqualTo(AcknowledgmentStatus.Rejected)
        assertThat(ack(Esse3AcknowledgmentOfReceipt.Unknown("Q"))).isEqualTo(AcknowledgmentStatus.Unknown)
        assertThat(ack(null)).isEqualTo(AcknowledgmentStatus.Unknown)
    }

    @Test
    fun `toExamResult returns null on an incomplete identity`() {
        assertThat(enrollment(cdsId = null).toExamResult()).isNull()
    }

    @Test
    fun `booked exam ignores the grade until the outcome is published`() {
        val unpublished = enrollment(publicationId = null, outcome = outcome(passed = 0)).toBookedExam()!!

        assertThat(unpublished.outcomePublished).isFalse()
        assertThat(unpublished.grade).isEqualTo(ExamGrade.Unknown)
    }

    @Test
    fun `booked exam trusts the grade once the outcome is published`() {
        val published = enrollment(publicationId = 7L, outcome = outcome(votoEsa = 30)).toBookedExam()!!

        assertThat(published.outcomePublished).isTrue()
        assertThat(published.grade).isEqualTo(ExamGrade.Numeric(30))
    }

    @Test
    fun `a published passing flag of zero reads as not passed`() {
        val published = enrollment(publicationId = 7L, outcome = outcome(passed = 0)).toBookedExam()!!

        assertThat(published.grade).isEqualTo(ExamGrade.NotPassed)
    }

    @Test
    fun `a published passing flag of one reads as passed`() {
        val published = enrollment(publicationId = 7L, outcome = outcome(passed = 1)).toBookedExam()!!

        assertThat(published.grade).isEqualTo(ExamGrade.Passed)
    }

    @Test
    fun `absence and withdrawal flags win over the numeric grade`() {
        val absent = enrollment(publicationId = 7L, outcome = outcome(absent = 1, votoEsa = 18)).toBookedExam()!!
        assertThat(absent.grade).isEqualTo(ExamGrade.Absent)

        val withdrew = enrollment(publicationId = 7L, outcome = outcome(withdrew = 1, votoEsa = 18)).toBookedExam()!!
        assertThat(withdrew.grade).isEqualTo(ExamGrade.Withdrew)
    }

    @Test
    fun `a published null outcome stays unknown`() {
        val published = enrollment(publicationId = 7L, outcome = null).toBookedExam()!!

        assertThat(published.outcomePublished).isTrue()
        assertThat(published.grade).isEqualTo(ExamGrade.Unknown)
    }

    @Test
    fun `booked exam projects dates booking fields and blank-collapsed strings`() {
        val booked = enrollment(
            shiftDateTime = "15/01/2025 09:30:00",
            insertionDate = "10/12/2024 12:00:00",
            enrollmentEndDate = "14/01/2025",
            classroomDescription = "  ",
            buildingDescription = "",
            studentNote = "  ",
            examTypeDescription = "Scritto",
            position = 5,
            weight = 6f,
        ).toBookedExam()!!

        assertThat(booked.examDateTime).isEqualTo(LocalDateTime.of(2025, 1, 15, 9, 30, 0))
        assertThat(booked.bookingDate).isEqualTo(LocalDateTime.of(2024, 12, 10, 12, 0, 0))
        assertThat(booked.cancellableUntil).isEqualTo(LocalDate.of(2025, 1, 14))
        assertThat(booked.classroomDescription).isNull()
        assertThat(booked.buildingDescription).isNull()
        assertThat(booked.studentNote).isNull()
        assertThat(booked.examModeDescription).isEqualTo("Scritto")
        assertThat(booked.position).isEqualTo(5)
        assertThat(booked.credits).isEqualTo(6f)
    }

    @Test
    fun `booked exam decodes the string exam-type code into the domain exam type`() {
        assertThat(enrollment(graduationTypeCode = "S").toBookedExam()!!.examType).isEqualTo(ExamType.Written)
        assertThat(enrollment(graduationTypeCode = "O").toBookedExam()!!.examType).isEqualTo(ExamType.Oral)
        assertThat(enrollment(graduationTypeCode = "SOC").toBookedExam()!!.examType)
            .isEqualTo(ExamType.WrittenAndOralJoint)
        assertThat(enrollment(graduationTypeCode = "SOS").toBookedExam()!!.examType)
            .isEqualTo(ExamType.WrittenAndOralSeparate)
        assertThat(enrollment(graduationTypeCode = "??").toBookedExam()!!.examType).isEqualTo(ExamType.Unknown)
        assertThat(enrollment(graduationTypeCode = null).toBookedExam()!!.examType).isEqualTo(ExamType.Unknown)
    }

    @Test
    fun `booked exam prefers the libretto name over the call description`() {
        val booked = enrollment(studentActivityDescription = "  ", examCallDescription = "Call X").toBookedExam()!!

        assertThat(booked.activityDescription).isEqualTo("Call X")
    }

    @Test
    fun `booked exam returns null on an incomplete identity`() {
        assertThat(enrollment(cdsId = null).toBookedExam()).isNull()
    }
}
