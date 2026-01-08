package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappPointOfInterestsResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappTeacherResponse

/**
 * Campus API for locations, facilities, and faculty directory.
 */
interface BicoccappCampusApi {
    /**
     * Retrieves all campus points of interest (buildings, facilities, services).
     *
     * @return Categorized locations with coordinates and metadata.
     */
    @GET("point_of_interests")
    suspend fun getPointsOfInterest(): BicoccappPointOfInterestsResponse

    /**
     * Retrieves a teacher's profile by their institutional email.
     *
     * @param email Teacher's @unimib.it email address.
     * @return Teacher profile with contact info and office location, or 404 if not found.
     */
    @GET("teacher")
    suspend fun getTeacherByEmail(
        @Query("teacherEmail") email: String
    ): BicoccappTeacherResponse

    /**
     * Sends an appointment request to a teacher.
     *
     * @param teacherKey Teacher's identifier (typically their email).
     * @param appUserId Student's app user numeric identifier.
     * @param messageBody Optional message explaining the appointment purpose.
     */
    @FormUrlEncoded
    @POST("messages/appointment")
    suspend fun sendAppointmentRequest(
        @Field("teacher_key") teacherKey: String,
        @Field("student_id") appUserId: Int,
        @Field("message_body") messageBody: String = ""
    )
}
