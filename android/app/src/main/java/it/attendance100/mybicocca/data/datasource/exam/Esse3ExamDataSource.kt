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
        // Fetch available activities with exam calls for the student
        val activities = esse3Api.calesa.getActivitiesForExamCalls()
        val results = mutableListOf<ExamCall>()

        for (activity in activities) {
            val sessions = esse3Api.calesa.getExamCalls(
                courseOfStudyId = activity.courseOfStudyDefaultCallId,
                activityId = activity.activityExamDefinitionId.toLong(),
            )
            sessions.forEach { session ->
                results.add(
                    ExamCall(
                        id = session.examCallId ?: session.callId?.toLong() ?: return@forEach,
                        careerId = careerId,
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
        // TODO: Map from esse3Api - requires matching enrolled exam calls with student data
        emptyList()
    }
}
