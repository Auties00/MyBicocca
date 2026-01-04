package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappExamsSessionsResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappModificationResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTaxesResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserCareerResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserExamsResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserProfile
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserRegistrationsResponse

/**
 * User API for student academic data: profile, career, exams, fees, and registrations.
 */
interface BicoccappUserApi {

    /**
     * Retrieves the user's profile information.
     *
     * @param fiscalCode User's fiscal code (codice fiscale).
     * @return Profile with personal data, contact info, and academic identifiers.
     */
    @GET("user_profile")
    suspend fun getProfile(
        @Query("fiscalCode") fiscalCode: String
    ): Response<BicoccappUserProfile>

    /**
     * Retrieves the student's academic career details.
     *
     * @param personId Internal person identifier.
     * @param enrollmentId Enrollment number (matricola).
     * @param studentId Student record identifier.
     * @return Career data including credits, GPA, and study plan.
     */
    @GET("user_career")
    suspend fun getCareer(
        @Query("personId") personId: String,
        @Query("matricId") enrollmentId: String,
        @Query("studentId") studentId: String
    ): Response<BicoccappUserCareerResponse>

    /**
     * Retrieves the student's completed exam history.
     *
     * @param enrollmentId Enrollment number (matricola).
     * @return Exam list with grades (18-30 scale), dates, and credits.
     */
    @GET("user_exams")
    suspend fun getExams(
        @Query("matricId") enrollmentId: String
    ): Response<BicoccappUserExamsResponse>

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
    ): Response<BicoccappExamsSessionsResponse>

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
    ): Response<BicoccappModificationResponse>

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
    ): Response<BicoccappModificationResponse>

    /**
     * Retrieves the student's tuition fee payment history.
     *
     * @param personId Internal person identifier.
     * @param enrollmentId Enrollment number (matricola).
     * @return Fee summary with installments, payment status, and deadlines.
     */
    @GET("user_fees")
    suspend fun getTaxes(
        @Query("personId") personId: String,
        @Query("matricId") enrollmentId: String
    ): Response<BicoccappTaxesResponse>

    /**
     * Retrieves the student's enrollment history across academic years.
     *
     * @param enrollmentId Enrollment number (matricola).
     * @return Registration list with dates, types, and status.
     */
    @GET("user_registrations")
    suspend fun getRegistrations(
        @Query("matricId") enrollmentId: String
    ): Response<BicoccappUserRegistrationsResponse>
}
