package it.attendance100.mybicocca.data.remote.api.bicoccapp

import it.attendance100.mybicocca.data.remote.dto.bicoccapp.Alerts
import it.attendance100.mybicocca.data.remote.dto.bicoccapp.Conversations
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
     * The [Alerts] object contains:
     * - A list of alert items with title, body, timestamp, and priority
     * - Read/unread status for each alert
     * - Deep links for actionable alerts
     *
     * ## Caching Behavior
     * Alerts are fetched fresh on each request. Consider implementing
     * client-side caching with a short TTL (e.g., 5 minutes) for better UX.
     *
     * @return A [Response] containing [Alerts] with all active notifications.
     *         Returns an empty list if no alerts are available.
     *
     * @see getSystemInfoMessage For app-wide announcements
     */
    @GET("alerts")
    suspend fun getAlerts(): Response<Alerts>

    /**
     * Retrieves the paginated list of message conversations with teachers.
     *
     * This endpoint returns all message threads the student has with university
     * staff, including professors, teaching assistants, and administrative personnel.
     * Conversations are sorted by most recent activity.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `conversations`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Pagination
     * Results are paginated to optimize performance. Each page contains a fixed
     * number of conversations (typically 20). Use the `page` parameter to navigate:
     * - Page 1: Most recent conversations
     * - Page N: Older conversations
     *
     * ## Response Structure
     * The [Conversations] object contains:
     * - List of conversation threads with preview of last message
     * - Participant information (teacher name, email, department)
     * - Unread message count per conversation
     * - Timestamp of last activity
     *
     * ## Usage Pattern
     * ```kotlin
     * // Initial load
     * val firstPage = messagesApi.getConversations(page = 1)
     *
     * // Load more on scroll
     * val nextPage = messagesApi.getConversations(page = 2)
     * ```
     *
     * @param page The page number for pagination (1-indexed).
     *             Defaults to the first page if not specified.
     *             Use `null` or omit for the first page of results.
     *
     * @return A [Response] containing [Conversations] with the requested page
     *         of conversation threads. Returns an empty list if no conversations
     *         exist or if the requested page exceeds available data.
     *
     * @see sendAppointmentRequest For initiating new conversations
     */
    @GET("conversations")
    suspend fun getConversations(
        @Query("page") page: Int? = null
    ): Response<Conversations>

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
     *
     * @see getConversations For viewing existing conversations
     */
    @FormUrlEncoded
    @POST("messages/appointment")
    suspend fun sendAppointmentRequest(
        @Field("teacher_key") teacherKey: String? = null,
        @Field("student_id") studentId: Int? = null,
        @Field("message_body") messageBody: String? = null
    ): Response<Unit>
}
