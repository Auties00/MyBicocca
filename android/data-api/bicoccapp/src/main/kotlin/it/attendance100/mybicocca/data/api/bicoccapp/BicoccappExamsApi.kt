package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappExamsSessionsResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappExamBookingResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserExamsResponse

/**
 * API for managing user exams and exam sessions.
 */
interface BicoccappExamsApi {
    /**
     * Retrieves the student's completed exam history.
     *
     * @param enrollmentId Enrollment number (matricola).
     * @return Exam list with grades (18-30 scale), dates, and credits.
     */
    @GET("user_exams")
    suspend fun getExams(
        @Query("matricId") enrollmentId: String
    ): BicoccappUserExamsResponse

    /**
     * Retrieves available and registered exam sessions (appelli).
     *
     * @param personId Internal person identifier.
     * @param enrollmentId Enrollment number (matricola).
     * @return Exam sessions with dates, locations, and registration status.
     */
    @GET("user_appeals")
    suspend fun getExamsSessions(
        @Query("personId") personId: String,
        @Query("matricId") enrollmentId: String
    ): BicoccappExamsSessionsResponse

    /**
     * Cancels a student's exam session registration.
     *
     * @param cdsId Degree program identifier.
     * @param activityId Teaching activity identifier.
     * @param activityItemId Activity item identifier.
     * @param activityAppealId Exam session identifier.
     * @param studentId Student record identifier.
     * @return Operation result with success status and message.
     */
    @DELETE("user_appeals")
    suspend fun cancelExamSession(
        @Query("cdsId") cdsId: Int,
        @Query("activityId") activityId: Int,
        @Query("activityItemId") activityItemId: Int,
        @Query("activityAppealId") activityAppealId: Int,
        @Query("studentId") studentId: String
    ): BicoccappExamBookingResponse

    /**
     * Registers the student for an exam session.
     *
     * @param cdsId Degree program identifier.
     * @param activityId Teaching activity identifier.
     * @param activityItemId Activity item identifier.
     * @param activityAppealId Exam session identifier.
     * @return Operation result with success status and message.
     */
    @POST("user_appeals")
    suspend fun addExamSession(
        @Query("cdsId") cdsId: Int,
        @Query("activityId") activityId: Int,
        @Query("activityItemId") activityItemId: Int,
        @Query("activityAppealId") activityAppealId: Int
    ): BicoccappExamBookingResponse
}
