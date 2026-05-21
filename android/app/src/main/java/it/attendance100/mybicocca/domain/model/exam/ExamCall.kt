package it.attendance100.mybicocca.domain.model.exam

import java.time.LocalDate
import java.time.LocalTime

data class ExamCall(
    val key: ExamCallKey,
    val examCallId: Long?,
    val activityChoiceId: Long?,
    val activityCode: String?,
    val activityDescription: String?,
    val courseOfStudyDescription: String?,
    val callDescription: String?,
    val callDate: LocalDate?,
    val callTime: LocalTime?,
    val enrollmentWindow: ExamEnrollmentWindow,
    val enrolledNumber: Int?,
    val state: String?,
    val stateDescription: String?,
    val callType: ExamCallType,
    val isReserved: Boolean,
    val matId: Long?,
)

data class ExamEnrollmentWindow(
    val opensAt: LocalDate?,
    val closesAt: LocalDate?,
)

enum class ExamCallType { Final, Partial, Other }
