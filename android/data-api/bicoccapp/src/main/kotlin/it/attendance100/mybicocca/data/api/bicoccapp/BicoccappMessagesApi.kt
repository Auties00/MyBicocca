package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappAlertsResponse
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST

/**
 * Messages API for notifications and appointment requests.
 */
interface BicoccappMessagesApi {

    /**
     * Retrieves all active alerts and notifications for the user.
     *
     * @return Alerts including academic, administrative, and system notifications.
     */
    @GET("alerts")
    suspend fun getAlerts(): Response<BicoccappAlertsResponse>

    /**
     * Sends an appointment request to a teacher.
     *
     * @param teacherKey Teacher's identifier (typically their email).
     * @param studentId Student's numeric identifier.
     * @param messageBody Optional message explaining the appointment purpose.
     * @return Empty response; check isSuccessful for delivery confirmation.
     */
    @FormUrlEncoded
    @POST("messages/appointment")
    suspend fun sendAppointmentRequest(
        @Field("teacher_key") teacherKey: String,
        @Field("student_id") studentId: Int,
        @Field("message_body") messageBody: String = ""
    ): Response<Unit>
}
