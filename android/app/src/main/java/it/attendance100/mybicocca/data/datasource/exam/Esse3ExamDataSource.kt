package it.attendance100.mybicocca.data.datasource.exam

import it.attendance100.mybicocca.data.api.esse3.Esse3Api
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Esse3ExamDataSource @Inject constructor(
    private val esse3Api: Esse3Api,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    }

    suspend fun getExamCalls(careerId: Long, matricolaId: Long? = null): List<ExamCall> =
        withContext(ioDispatcher) {
            val activities = esse3Api.examsCalendar.getActivitiesForExamCalls(matId = matricolaId)
        val results = mutableListOf<ExamCall>()
        val today = java.time.LocalDate.now().toString()

        for (activity in activities) {
            val cdsId = activity.courseOfStudyDefaultCallId
            val adId = activity.activityExamDefinitionId.toLong()
            val sessions = esse3Api.examsCalendar.getExamCalls(
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

    suspend fun getBookings(matricolaId: Long?): List<ExamBooking> {
        if(matricolaId == null) return listOf()
        val now = LocalDateTime.now()
        val nowFormatted = now.format(formatter)
        val values = esse3Api.examsCalendar.getBookingsByMatId(
            matricolaId,
            optionalFields = "ALL",
            filter = "dataOraTurno>=\"$nowFormatted\""
        )
        return withContext(ioDispatcher) {
            values.mapNotNull { enrollment ->
                val id = enrollment.applicationListId
                    ?: return@mapNotNull null
                ExamBooking(
                    id = id,
                    activityName = enrollment.studentActivityDescription ?: "",
                    examDate = enrollment.shiftDateTime,
                    bookingDate = enrollment.insertionDate,
                    position = enrollment.position,
                )
            }
        }
    }
}
