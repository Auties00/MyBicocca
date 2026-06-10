package it.attendance100.mybicocca.data.mapper.exam

import com.google.common.truth.Truth.assertThat
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
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Covers the offline exam-mirror entity <-> domain mapping: the sealed grade encoded as a
 * kind/value pair, enums round-tripping by name with their Unknown/Other fallbacks, dates
 * round-tripping through ISO-8601 strings with unparseable values dropped to null, and the
 * flattened president collapsing to null when fully empty.
 */
class ExamCacheMappersTest {

    private val careerId = CareerId(100L)
    private val key = ExamCallKey(10L, 20L, 3)

    private fun bookedExam(
        grade: ExamGrade = ExamGrade.Numeric(28),
        outcomePublished: Boolean = true,
        examDateTime: LocalDateTime? = LocalDateTime.of(2025, 1, 15, 9, 30),
        bookingDate: LocalDateTime? = LocalDateTime.of(2024, 12, 10, 12, 0),
        cancellableUntil: LocalDate? = LocalDate.of(2025, 1, 14),
    ) = BookedExam(
        key = key,
        applicationListId = 999L,
        studentId = 100L,
        activityChoiceId = 77L,
        activityDescription = "Analisi I",
        examCallDescription = "Appello di Analisi I",
        examType = ExamType.Written,
        callType = ExamCallType.Final,
        examDateTime = examDateTime,
        classroomDescription = "Aula U6-01",
        buildingDescription = "Edificio U6",
        credits = 6f,
        examModeDescription = "Scritto",
        position = 5,
        bookingDate = bookingDate,
        cancellableUntil = cancellableUntil,
        studentNote = "nota",
        grade = grade,
        outcomePublished = outcomePublished,
        publishedNote = "ok",
    )

    private fun examCall(
        callType: ExamCallType = ExamCallType.Final,
        examType: ExamType = ExamType.Written,
        president: ExamExaminer? = ExamExaminer(8L, "Anna", "Bianchi"),
    ) = ExamCall(
        key = key,
        examCallId = 5000L,
        activityChoiceId = 77L,
        activityCode = "E3101Q123",
        activityDescription = "Analisi I",
        courseOfStudyDescription = "Informatica",
        callDescription = "Appello di Analisi I",
        callDate = LocalDate.of(2025, 1, 15),
        callTime = LocalTime.of(9, 30),
        enrollmentWindow = ExamEnrollmentWindow(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 14)),
        enrolledNumber = 42,
        state = "A",
        stateDescription = "Aperto",
        callType = callType,
        examType = examType,
        isReserved = false,
        matId = 9L,
        notes = "note",
        president = president,
        bookingTypeDescription = "online",
    )

    private fun examResult(
        grade: ExamGrade = ExamGrade.Numeric(30),
        acknowledgment: AcknowledgmentStatus = AcknowledgmentStatus.Viewed,
    ) = ExamResult(
        key = key,
        applicationListId = 999L,
        publicationId = 1L,
        activityDescription = "Analisi I",
        examDateTime = LocalDateTime.of(2025, 1, 15, 9, 30),
        grade = grade,
        acknowledgment = acknowledgment,
        publishedNote = "ok",
        acknowledgmentDeadline = LocalDate.of(2025, 1, 20),
    )

    @Test
    fun `booked exam round-trips through its entity`() {
        val entity = bookedExam().toEntity(careerId, order = 2)

        assertThat(entity.careerId).isEqualTo(100L)
        assertThat(entity.cacheOrder).isEqualTo(2)
        assertThat(entity.courseOfStudyId).isEqualTo(10L)
        assertThat(entity.activityId).isEqualTo(20L)
        assertThat(entity.callId).isEqualTo(3)
        assertThat(entity.examType).isEqualTo("Written")
        assertThat(entity.callType).isEqualTo("Final")
        assertThat(entity.examDateTime).isEqualTo("2025-01-15T09:30")

        assertThat(entity.toDomain()).isEqualTo(bookedExam())
    }

    @Test
    fun `each grade kind encodes and decodes back`() {
        val grades = listOf(
            ExamGrade.Numeric(18),
            ExamGrade.Passed,
            ExamGrade.NotPassed,
            ExamGrade.Withdrew,
            ExamGrade.Absent,
            ExamGrade.Unknown,
        )

        grades.forEach { grade ->
            val roundTripped = bookedExam(grade = grade).toEntity(careerId, 0).toDomain().grade
            assertThat(roundTripped).isEqualTo(grade)
        }
    }

    @Test
    fun `the numeric grade stores its value and other kinds store none`() {
        val numeric = bookedExam(grade = ExamGrade.Numeric(27)).toEntity(careerId, 0)
        assertThat(numeric.gradeKind).isEqualTo("numeric")
        assertThat(numeric.gradeValue).isEqualTo(27)

        val passed = bookedExam(grade = ExamGrade.Passed).toEntity(careerId, 0)
        assertThat(passed.gradeKind).isEqualTo("passed")
        assertThat(passed.gradeValue).isNull()
    }

    @Test
    fun `a numeric kind with a missing stored value decodes to unknown`() {
        val corrupted = bookedExam(grade = ExamGrade.Numeric(20)).toEntity(careerId, 0)
            .copy(gradeValue = null)

        assertThat(corrupted.toDomain().grade).isEqualTo(ExamGrade.Unknown)
    }

    @Test
    fun `an unrecognized stored grade kind decodes to unknown`() {
        val corrupted = bookedExam().toEntity(careerId, 0).copy(gradeKind = "weird", gradeValue = null)

        assertThat(corrupted.toDomain().grade).isEqualTo(ExamGrade.Unknown)
    }

    @Test
    fun `booked exam date strings round-trip and nulls stay null`() {
        val withNullDates = bookedExam(examDateTime = null, bookingDate = null, cancellableUntil = null)
            .toEntity(careerId, 0)

        assertThat(withNullDates.examDateTime).isNull()
        assertThat(withNullDates.bookingDate).isNull()
        assertThat(withNullDates.cancellableUntil).isNull()

        val domain = withNullDates.toDomain()
        assertThat(domain.examDateTime).isNull()
        assertThat(domain.bookingDate).isNull()
        assertThat(domain.cancellableUntil).isNull()
    }

    @Test
    fun `an unparseable stored date decodes to null`() {
        val corrupted = bookedExam().toEntity(careerId, 0).copy(examDateTime = "not-a-date")

        assertThat(corrupted.toDomain().examDateTime).isNull()
    }

    @Test
    fun `unknown stored exam and call types decode to their fallbacks`() {
        val entity = BookedExamEntity(
            careerId = 100L,
            courseOfStudyId = 10L,
            activityId = 20L,
            callId = 3,
            cacheOrder = 0,
            applicationListId = null,
            studentId = null,
            activityChoiceId = null,
            activityDescription = null,
            examCallDescription = null,
            examType = "Bogus",
            callType = "Bogus",
            examDateTime = null,
            classroomDescription = null,
            buildingDescription = null,
            credits = null,
            examModeDescription = null,
            position = null,
            bookingDate = null,
            cancellableUntil = null,
            studentNote = null,
            gradeKind = "unknown",
            gradeValue = null,
            outcomePublished = false,
            publishedNote = null,
        )

        val domain = entity.toDomain()
        assertThat(domain.examType).isEqualTo(ExamType.Unknown)
        assertThat(domain.callType).isEqualTo(ExamCallType.Other)
    }

    @Test
    fun `exam call round-trips through its entity`() {
        val entity = examCall().toEntity(careerId, order = 1)

        assertThat(entity.cacheOrder).isEqualTo(1)
        assertThat(entity.callDate).isEqualTo("2025-01-15")
        assertThat(entity.callTime).isEqualTo("09:30")
        assertThat(entity.windowOpensAt).isEqualTo("2025-01-01")
        assertThat(entity.windowClosesAt).isEqualTo("2025-01-14")
        assertThat(entity.presidentId).isEqualTo(8L)
        assertThat(entity.presidentName).isEqualTo("Anna")

        assertThat(entity.toDomain()).isEqualTo(examCall())
    }

    @Test
    fun `a fully empty president flattens to null on the way back`() {
        val entity = examCall(president = null).toEntity(careerId, 0)

        assertThat(entity.presidentId).isNull()
        assertThat(entity.presidentName).isNull()
        assertThat(entity.presidentSurname).isNull()
        assertThat(entity.toDomain().president).isNull()
    }

    @Test
    fun `a president with only an id survives the round-trip`() {
        val entity = examCall(president = ExamExaminer(8L, null, null)).toEntity(careerId, 0)

        assertThat(entity.toDomain().president).isEqualTo(ExamExaminer(8L, null, null))
    }

    @Test
    fun `an unparseable stored call time decodes to null`() {
        val corrupted = examCall().toEntity(careerId, 0).copy(callTime = "99:99")

        assertThat(corrupted.toDomain().callTime).isNull()
    }

    @Test
    fun `exam result round-trips through its entity`() {
        val entity = examResult().toEntity(careerId, order = 0)

        assertThat(entity.acknowledgment).isEqualTo("Viewed")
        assertThat(entity.gradeKind).isEqualTo("numeric")
        assertThat(entity.gradeValue).isEqualTo(30)
        assertThat(entity.acknowledgmentDeadline).isEqualTo("2025-01-20")

        assertThat(entity.toDomain()).isEqualTo(examResult())
    }

    @Test
    fun `each acknowledgment status round-trips by name`() {
        AcknowledgmentStatus.entries.forEach { status ->
            val roundTripped = examResult(acknowledgment = status).toEntity(careerId, 0).toDomain().acknowledgment
            assertThat(roundTripped).isEqualTo(status)
        }
    }

    @Test
    fun `an unrecognized stored acknowledgment decodes to unknown`() {
        val corrupted = examResult().toEntity(careerId, 0).copy(acknowledgment = "Mystery")

        assertThat(corrupted.toDomain().acknowledgment).isEqualTo(AcknowledgmentStatus.Unknown)
    }

    @Test
    fun `exam result keeps a null deadline`() {
        val entity = ExamResultEntity(
            careerId = 100L,
            courseOfStudyId = 10L,
            activityId = 20L,
            callId = 3,
            cacheOrder = 0,
            applicationListId = null,
            publicationId = null,
            activityDescription = null,
            examDateTime = null,
            gradeKind = "passed",
            gradeValue = null,
            acknowledgment = "NotViewed",
            publishedNote = null,
            acknowledgmentDeadline = null,
        )

        val domain = entity.toDomain()
        assertThat(domain.acknowledgmentDeadline).isNull()
        assertThat(domain.grade).isEqualTo(ExamGrade.Passed)
    }

    @Test
    fun `exam call with null dates and times maps cleanly both ways`() {
        val entity = ExamCallEntity(
            careerId = 100L,
            courseOfStudyId = 10L,
            activityId = 20L,
            callId = 3,
            cacheOrder = 0,
            examCallId = null,
            activityChoiceId = null,
            activityCode = null,
            activityDescription = null,
            courseOfStudyDescription = null,
            callDescription = null,
            callDate = null,
            callTime = null,
            windowOpensAt = null,
            windowClosesAt = null,
            enrolledNumber = null,
            state = null,
            stateDescription = null,
            callType = "Partial",
            examType = "Oral",
            isReserved = true,
            matId = null,
            notes = null,
            presidentId = null,
            presidentName = null,
            presidentSurname = null,
            bookingTypeDescription = null,
        )

        val domain = entity.toDomain()
        assertThat(domain.callDate).isNull()
        assertThat(domain.callTime).isNull()
        assertThat(domain.enrollmentWindow.opensAt).isNull()
        assertThat(domain.enrollmentWindow.closesAt).isNull()
        assertThat(domain.callType).isEqualTo(ExamCallType.Partial)
        assertThat(domain.examType).isEqualTo(ExamType.Oral)
        assertThat(domain.isReserved).isTrue()
    }
}
