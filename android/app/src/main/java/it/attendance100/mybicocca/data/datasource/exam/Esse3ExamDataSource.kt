package it.attendance100.mybicocca.data.datasource.exam

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3ExamDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getExamCalls(careerId: Long): List<ExamCall> = withContext(ioDispatcher) {
        val activities = esse3Api.calesa.getActivitiesForExamCalls()
        val results = mutableListOf<ExamCall>()
        val today = java.time.LocalDate.now().toString()

        for (activity in activities) {
            val cdsId = activity.courseOfStudyDefaultCallId
            val adId = activity.activityExamDefinitionId.toLong()
            val sessions = esse3Api.calesa.getExamCalls(
                courseOfStudyId = cdsId,
                activityId = adId,
                minCallDate = today,
            )
            sessions.forEach { session ->
                results.add(
                    ExamCall(
                        id = session.examCallId ?: session.callId?.toLong() ?: return@forEach,
                        careerId = careerId,
                        courseOfStudyId = cdsId,
                        activityId = adId,
                        activityName = session.activityDescription ?: activity.activityDescription ?: "",
                        activityCode = session.activityCode ?: activity.activityCode,
                        date = session.callStartDate?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() },
                        startTime = session.graduationTime?.let { runCatching { java.time.LocalTime.parse(it) }.getOrNull() },
                        endTime = null,
                        room = null,
                        building = null,
                        enrollmentStartDate = session.enrollmentStartDate,
                        enrollmentEndDate = session.enrollmentEndDate,
                        enrolledCount = session.enrolledNumber,
                        stateDescription = session.stateDescription,
                        examinerEmails = null,
                    )
                )
            }
        }

        results
    }

    suspend fun getBookings(careerId: Long): List<ExamBooking> = withContext(ioDispatcher) {
        val activities = esse3Api.calesa.getActivitiesForExamCalls()
        val studentId = careerId
        val results = mutableListOf<ExamBooking>()
        val today = java.time.LocalDate.now().toString()

        for (activity in activities) {
            val cdsId = activity.courseOfStudyDefaultCallId
            val adId = activity.activityExamDefinitionId.toLong()
            val sessions = esse3Api.calesa.getExamCalls(
                courseOfStudyId = cdsId,
                activityId = adId,
                minCallDate = today,
            )

            for (session in sessions) {
                val callId = session.examCallId ?: session.callId?.toLong() ?: continue
                val enrollment = runCatching {
                    esse3Api.calesa.getEnrolledExamCall(
                        courseOfStudyId = cdsId,
                        activityId = adId,
                        callId = callId,
                        studentId = studentId,
                    )
                }.getOrNull() ?: continue

                results.add(
                    ExamBooking(
                        id = enrollment.applicationListId ?: callId,
                        careerId = careerId,
                        activityName = enrollment.studentActivityDescription
                            ?: session.activityDescription
                            ?: activity.activityDescription
                            ?: "",
                        examDate = enrollment.graduationDate ?: session.callStartDate,
                        bookingDate = enrollment.insertionDate,
                        position = enrollment.position,
                    )
                )
            }
        }

        results
    }
}
