package it.attendance100.mybicocca.data.remote.api.elearning

import it.attendance100.mybicocca.data.remote.dto.elearning.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * # Elearning Calendar API
 *
 * Handles calendar events, views, and scheduling.
 *
 * ## Key Features
 *
 * - **Events:** Retrieve, create, update, and delete calendar events.
 * - **Views:** Get daily, monthly, and upcoming event views.
 * - **Access:** Check user access permissions for the calendar.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get upcoming events
 * val events = calendarApi.getCalendarUpcomingView(
 *     GetCalendarUpcomingViewRequest(courseId = courseId)
 * )
 * ```
 */
interface ElearningCalendarApi {

    /**
     * Get calendar events based on filters.
     *
     * @param request Filters for events (time range, courses, etc.).
     * @return List of calendar events.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_get_calendar_events")
    suspend fun getCalendarEvents(@Body request: GetCalendarEventsRequest): Response<CalendarEventsResponse>

    /**
     * Get calendar events for a specific month.
     *
     * @param request Month, year, and course ID.
     * @return Monthly calendar view.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_get_calendar_monthly_view")
    suspend fun getCalendarMonthlyView(@Body request: GetCalendarMonthlyViewRequest): Response<CalendarMonthlyViewResponse>

    /**
     * Get calendar events for a specific day.
     *
     * @param request Day, month, year, and course ID.
     * @return Daily calendar view.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_get_calendar_day_view")
    suspend fun getCalendarDayView(@Body request: GetCalendarDayViewRequest): Response<CalendarDayViewResponse>

    /**
     * Get upcoming calendar events.
     *
     * @param request Course ID and category ID.
     * @return Upcoming events.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_get_calendar_upcoming_view")
    suspend fun getCalendarUpcomingView(@Body request: GetCalendarUpcomingViewRequest): Response<CalendarUpcomingViewResponse>

    /**
     * Get calendar access information (permissions).
     *
     * @param request Course ID.
     * @return Access permissions.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_get_calendar_access_information")
    suspend fun getCalendarAccessInformation(@Body request: GetCalendarAccessInfoRequest): Response<CalendarAccessInfoResponse>

    /**
     * Get allowed event types for creation.
     *
     * @param request Course ID.
     * @return List of allowed event types.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_get_allowed_event_types")
    suspend fun getAllowedEventTypes(@Body request: GetAllowedEventTypesRequest): Response<AllowedEventTypesResponse>

    /**
     * Get a single calendar event by ID.
     *
     * @param request Event ID.
     * @return The calendar event details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_get_calendar_event_by_id")
    suspend fun getCalendarEventById(@Body request: GetCalendarEventByIdRequest): Response<CalendarEventResponse>

    /**
     * Create or update a calendar event.
     *
     * @param request Event details form data.
     * @return The created or updated event.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_submit_create_update_form")
    suspend fun submitCreateUpdateForm(@Body request: SubmitCalendarEventFormRequest): Response<SubmitCalendarEventFormResponse>

    /**
     * Delete calendar events.
     *
     * @param request Event IDs and repeat option.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_calendar_delete_calendar_events")
    suspend fun deleteCalendarEvents(@Body request: DeleteCalendarEventsRequest): Response<Any>
}
