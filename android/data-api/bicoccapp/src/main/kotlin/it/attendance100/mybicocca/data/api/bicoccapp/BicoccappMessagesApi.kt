package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappAlertsResponse
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST

/**
 * # BicoccApp Messages API
 *
 * This interface provides endpoints for managing user communications within
 * the BicoccApp platform. It handles notifications, alerts, teacher conversations,
 * and appointment scheduling.
 *
 * ## Features
 *
 * - **Alerts & Notifications:** Fetch system-wide and user-specific alerts
 * - **Conversations:** View message threads with teachers and staff
 * - **Appointments:** Request meetings with teachers for academic support
 * - **System Messages:** Retrieve app-wide informational announcements
 *
 * ## Authentication
 *
 * All endpoints in this interface require a valid authentication token.
 * Ensure the user is logged in before making requests.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Fetch user alerts
 * val alerts = messagesApi.getAlerts()
 *
 * // Load conversations with pagination
 * val conversations = messagesApi.getConversations(page = 1)
 *
 * // Request an appointment with a teacher
 * messagesApi.sendAppointmentRequest(
 *     teacherKey = "prof.rossi@unimib.it",
 *     studentId = 123456,
 *     messageBody = "I would like to discuss my thesis project."
 * )
 * ```
 */
interface BicoccappMessagesApi {

    /**
     * Retrieves all active alerts and notifications for the authenticated user.
     *
     * This endpoint returns a collection of alerts including:
     * - **Academic alerts:** Exam registration deadlines, grade publications
     * - **Administrative alerts:** Fee payment reminders, document requests
     * - **System alerts:** Maintenance windows, service announcements
     * - **Personal alerts:** Messages from professors, appointment confirmations
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `alerts`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappAlertsResponse] object contains:
     * - A list of alert items with title, body, timestamp, and priority
     * - Read/unread status for each alert
     * - Deep links for actionable alerts
     *
     * ## Caching Behavior
     * Alerts are fetched fresh on each request. Consider implementing
     * client-side caching with a short TTL (e.g., 5 minutes) for better UX.
     *
     * @return A [Response] containing [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappAlertsResponse] with all active notifications.
     *         Returns an empty list if no alerts are available.
     *
     * @see getSystemInfoMessage For app-wide announcements
     */
    @GET("alerts")
    suspend fun getAlerts(): Response<BicoccappAlertsResponse>

    /**
     * Sends an appointment request message to a teacher.
     *
     * This endpoint allows students to request a meeting with a professor
     * or teaching assistant. The request creates a new conversation thread
     * or appends to an existing one if a conversation already exists.
     *
     * ## HTTP Details
     * - **Method:** POST
     * - **Path:** `messages/appointment`
     * - **Content-Type:** `application/x-www-form-urlencoded`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Request Flow
     * 1. Student submits appointment request with message
     * 2. Server validates the request and teacher availability
     * 3. Teacher receives notification about the request
     * 4. Conversation thread is created/updated
     *
     * ## Response Codes
     * - **200 OK:** Appointment request sent successfully
     * - **400 Bad Request:** Invalid parameters (missing teacher or message)
     * - **403 Forbidden:** Student not authorized to contact this teacher
     * - **404 Not Found:** Teacher not found in the system
     * - **429 Too Many Requests:** Rate limit exceeded
     *
     * ## Best Practices
     * - Keep messages professional and concise
     * - Include specific topics or questions in the message body
     * - Avoid sending duplicate requests to the same teacher
     *
     * @param teacherKey The unique identifier for the teacher, typically their
     *                   institutional email address (e.g., "name.surname@unimib.it").
     *                   This is used to route the message to the correct recipient.
     *
     * @param studentId The numeric identifier of the student sending the request.
     *                  This should match the authenticated user's student ID.
     *
     * @param messageBody The content of the appointment request message.
     *                    Should clearly state the purpose of the meeting
     *                    and any relevant context. HTML is not supported.
     *
     * @return A [Response] containing [Unit]. A successful response (200)
     *         indicates the message was sent. Check [Response.isSuccessful]
     *         to verify delivery.
     */
    @FormUrlEncoded
    @POST("messages/appointment")
    suspend fun sendAppointmentRequest(
        @Field("teacher_key") teacherKey: String,
        @Field("student_id") studentId: Int,
        @Field("message_body") messageBody: String = ""
    ): Response<Unit>
}
