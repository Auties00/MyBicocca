package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserCareerResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappUserRegistrationsResponse

/**
 * API for managing user career and registrations.
 */
interface BicoccappCareerApi {
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
    ): BicoccappUserCareerResponse

    /**
     * Retrieves the student's enrollment history across academic years.
     *
     * @param enrollmentId Enrollment number (matricola).
     * @return Registration list with dates, types, and status.
     */
    @GET("user_registrations")
    suspend fun getRegistrations(
        @Query("matricId") enrollmentId: String
    ): BicoccappUserRegistrationsResponse
}
