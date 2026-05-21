package it.attendance100.mybicocca.data.remote.bicoccapp.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappExamBookingResult
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappExamSession
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappExamsSessionsResponse
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappUserExamsCareer
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappUserExamsResponse
import kotlinx.serialization.json.Json

/**
 * API client for exam-related operations.
 */
class BicoccappExamsApi(
    client: HttpClient,
    json: Json
) : BicoccappAbstractApi(client, json) {

    /**
     * Retrieves the student's completed exam history.
     *
     * @param enrollmentId Enrollment number (matricola).
     * @return Exams career data.
     */
    suspend fun getExams(
        enrollmentId: String
    ): BicoccappUserExamsCareer {
        val response = executeJsonGet<BicoccappUserExamsResponse>("/user_exams") {
            parameter("matricId", enrollmentId)
        }
        return response.career
    }

    /**
     * Retrieves available and registered exam sessions (appelli).
     *
     * @param personId Internal person identifier.
     * @param enrollmentId Enrollment number (matricola).
     * @return Exam sessions with appeals.
     */
    suspend fun getExamsSessions(
        personId: String,
        enrollmentId: String
    ): List<BicoccappExamSession> {
        val response = executeJsonGet<BicoccappExamsSessionsResponse>("/user_appeals") {
            parameter("personId", personId)
            parameter("matricId", enrollmentId)
        }
        return response.career.courses
    }

    /**
     * Registers the student for an exam session.
     *
     * @param degreeCourseCode Degree program identifier.
     * @param activityId Teaching activity identifier.
     * @param activityItemId Activity item identifier.
     * @param activityAppealId Exam session identifier.
     * @return Booking result (success or error).
     */
    suspend fun addExamSession(
        degreeCourseCode: Int,
        activityId: Int,
        activityItemId: Int,
        activityAppealId: Int
    ): BicoccappExamBookingResult {
        return executeJsonPost("/user_appeals") {
            parameter("cdsId", degreeCourseCode)
            parameter("activityId", activityId)
            parameter("activityItemId", activityItemId)
            parameter("activityAppealId", activityAppealId)
        }
    }

    /**
     * Cancels a student's exam session registration.
     *
     * @param cdsId Degree program identifier.
     * @param activityId Teaching activity identifier.
     * @param activityItemId Activity item identifier.
     * @param activityAppealId Exam session identifier.
     * @param studentId Student record identifier.
     * @return Booking result (success or error).
     */
    suspend fun cancelExamSession(
        cdsId: Int,
        activityId: Int,
        activityItemId: Int,
        activityAppealId: Int,
        studentId: String
    ): BicoccappExamBookingResult {
        return executeJsonDelete("/user_appeals") {
            parameter("cdsId", cdsId)
            parameter("activityId", activityId)
            parameter("activityItemId", activityItemId)
            parameter("activityAppealId", activityAppealId)
            parameter("studentId", studentId)
        }
    }
}
