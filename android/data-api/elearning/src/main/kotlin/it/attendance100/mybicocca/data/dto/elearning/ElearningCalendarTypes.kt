package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to retrieve the monthly calendar view.
 *
 * @property year The year to retrieve the calendar for.
 * @property month The month to retrieve the calendar for (1-12).
 * @property courseId Optional course identifier to filter events by.
 * @property categoryId Optional category identifier to filter events by.
 * @property includeNavigation Whether to include navigation links to previous/next periods.
 * @property mini Whether to use a minimal view with less details.
 */
@Serializable
class ElearningGetCalendarMonthlyViewRequest(
    private val year: Int,
    private val month: Int,
    private val courseId: Int? = null,
    private val categoryId: Int? = null,
    private val includeNavigation: Boolean = true,
    private val mini: Boolean = false
) : ElearningRequest<ElearningGetCalendarMonthlyViewResponse> {
    override val functionName = "core_calendar_get_calendar_monthly_view"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("year", year.toString())
        formData.append("month", month.toString())
        courseId?.let { formData.append("courseid", it.toString()) }
        categoryId?.let { formData.append("categoryid", it.toString()) }
        formData.append("includenavigation", if (includeNavigation) "1" else "0")
        formData.append("mini", if (mini) "1" else "0")
    }
}

/**
 * Response containing the monthly calendar view data.
 *
 * @property url The URL to the calendar page.
 * @property courseId The course identifier if filtered by course.
 * @property categoryId The category identifier if filtered by category.
 * @property weeks The list of weeks in this monthly view.
 * @property dayNames The list of day names used in the calendar.
 * @property date The date information for the current view.
 * @property periodName The display name of the current period (e.g., "January 2024").
 * @property includeNavigation Whether navigation links are included.
 * @property previousPeriod The date information for the previous period.
 * @property previousPeriodLink The URL to navigate to the previous period.
 * @property previousPeriodName The display name of the previous period.
 * @property nextPeriod The date information for the next period.
 * @property nextPeriodName The display name of the next period.
 * @property nextPeriodLink The URL to navigate to the next period.
 */
@Serializable
data class ElearningGetCalendarMonthlyViewResponse(
    @SerialName("url")
    val url: String? = null,
    @SerialName("courseid")
    val courseId: Int? = null,
    @SerialName("categoryid")
    val categoryId: Int? = null,
    @SerialName("weeks")
    val weeks: List<ElearningCalendarWeek> = emptyList(),
    @SerialName("daynames")
    val dayNames: List<ElearningCalendarDayName> = emptyList(),
    @SerialName("date")
    val date: ElearningCalendarDate? = null,
    @SerialName("periodname")
    val periodName: String? = null,
    @SerialName("includenavigation")
    val includeNavigation: Boolean = true,
    @SerialName("previousperiod")
    val previousPeriod: ElearningCalendarDate? = null,
    @SerialName("previousperiodlink")
    val previousPeriodLink: String? = null,
    @SerialName("previousperiodname")
    val previousPeriodName: String? = null,
    @SerialName("nextperiod")
    val nextPeriod: ElearningCalendarDate? = null,
    @SerialName("nextperiodname")
    val nextPeriodName: String? = null,
    @SerialName("nextperiodlink")
    val nextPeriodLink: String? = null
) : ElearningResponse

/**
 * Request to retrieve the upcoming events calendar view.
 *
 * @property courseId Optional course identifier to filter events by.
 * @property categoryId Optional category identifier to filter events by.
 */
@Serializable
class ElearningGetCalendarUpcomingViewRequest(
    private val courseId: Int? = null,
    private val categoryId: Int? = null
) : ElearningRequest<ElearningGetCalendarUpcomingViewResponse> {
    override val functionName = "core_calendar_get_calendar_upcoming_view"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        courseId?.let { formData.append("courseid", it.toString()) }
        categoryId?.let { formData.append("categoryid", it.toString()) }
    }
}

/**
 * Response containing the upcoming events calendar view data.
 *
 * @property events The list of upcoming calendar events.
 * @property defaultEventContext The default context for new events.
 * @property courseId The course identifier if filtered by course.
 * @property categoryId The category identifier if filtered by category.
 * @property isLoggedIn Whether the user is currently logged in.
 * @property date The current date information.
 */
@Serializable
data class ElearningGetCalendarUpcomingViewResponse(
    @SerialName("events")
    val events: List<ElearningCalendarEvent> = emptyList(),
    @SerialName("defaulteventcontext")
    val defaultEventContext: Int? = null,
    @SerialName("courseid")
    val courseId: Int? = null,
    @SerialName("categoryid")
    val categoryId: Int? = null,
    @SerialName("isloggedin")
    val isLoggedIn: Boolean = true,
    @SerialName("date")
    val date: ElearningCalendarDate? = null
) : ElearningResponse

/**
 * Represents a calendar event.
 *
 * @property id The unique identifier of the event.
 * @property name The name of the event.
 * @property description The description of the event.
 * @property descriptionFormat The format of the description (0=MOODLE, 1=HTML, 2=PLAIN, 4=MARKDOWN).
 * @property location The location where the event takes place.
 * @property categoryId The category identifier if this is a category event.
 * @property groupId The group identifier if this is a group event.
 * @property userId The user identifier if this is a user event.
 * @property repeatId The identifier of the repeating event series.
 * @property eventCount The number of events in the repeating series.
 * @property component The component that created this event.
 * @property moduleName The name of the module associated with this event.
 * @property activityName The name of the activity associated with this event.
 * @property activityString The activity string representation.
 * @property instance The instance identifier of the associated module.
 * @property eventType The type of event (site, user, course, category, group).
 * @property timeStart The Unix timestamp when the event starts.
 * @property timeDuration The duration of the event in seconds.
 * @property timeSort The Unix timestamp used for sorting events.
 * @property timeUserMidnight The Unix timestamp of midnight in user's timezone.
 * @property visible Whether the event is visible (1=visible, 0=hidden).
 * @property timeModified The Unix timestamp when the event was last modified.
 * @property overdue Whether the event is overdue.
 * @property icon The icon information for this event.
 * @property course The course information associated with this event.
 * @property canEdit Whether the current user can edit this event.
 * @property canDelete Whether the current user can delete this event.
 * @property deleteUrl The URL to delete this event.
 * @property editUrl The URL to edit this event.
 * @property viewUrl The URL to view this event.
 * @property formattedTime The human-readable formatted time of the event.
 * @property formattedLocation The human-readable formatted location of the event.
 * @property isActionEvent Whether this is an action event.
 * @property isCourseEvent Whether this is a course event.
 * @property isCategoryEvent Whether this is a category event.
 * @property groupName The name of the group if this is a group event.
 * @property normalisedEventType The normalised event type identifier.
 * @property normalisedEventTypeText The human-readable normalised event type.
 * @property action The action information associated with this event.
 * @property purpose The purpose of this event.
 * @property url The URL associated with this event.
 */
@Serializable
data class ElearningCalendarEvent(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("descriptionformat")
    val descriptionFormat: Int? = null,
    @SerialName("location")
    val location: String? = null,
    @SerialName("categoryid")
    val categoryId: Int? = null,
    @SerialName("groupid")
    val groupId: Int? = null,
    @SerialName("userid")
    val userId: Int? = null,
    @SerialName("repeatid")
    val repeatId: Int? = null,
    @SerialName("eventcount")
    val eventCount: Int? = null,
    @SerialName("component")
    val component: String? = null,
    @SerialName("modulename")
    val moduleName: String? = null,
    @SerialName("activityname")
    val activityName: String? = null,
    @SerialName("activitystr")
    val activityString: String? = null,
    @SerialName("instance")
    val instance: Int? = null,
    @SerialName("eventtype")
    val eventType: String? = null,
    @SerialName("timestart")
    val timeStart: Long,
    @SerialName("timeduration")
    val timeDuration: Long = 0,
    @SerialName("timesort")
    val timeSort: Long? = null,
    @SerialName("timeusermidnight")
    val timeUserMidnight: Long? = null,
    @SerialName("visible")
    val visible: Int = 1,
    @SerialName("timemodified")
    val timeModified: Long? = null,
    @SerialName("overdue")
    val overdue: Boolean? = null,
    @SerialName("icon")
    val icon: ElearningEventIcon? = null,
    @SerialName("course")
    val course: ElearningEventCourse? = null,
    @SerialName("canedit")
    val canEdit: Boolean = false,
    @SerialName("candelete")
    val canDelete: Boolean = false,
    @SerialName("deleteurl")
    val deleteUrl: String? = null,
    @SerialName("editurl")
    val editUrl: String? = null,
    @SerialName("viewurl")
    val viewUrl: String? = null,
    @SerialName("formattedtime")
    val formattedTime: String? = null,
    @SerialName("formattedlocation")
    val formattedLocation: String? = null,
    @SerialName("isactionevent")
    val isActionEvent: Boolean = false,
    @SerialName("iscourseevent")
    val isCourseEvent: Boolean = false,
    @SerialName("iscategoryevent")
    val isCategoryEvent: Boolean = false,
    @SerialName("groupname")
    val groupName: String? = null,
    @SerialName("normalisedeventtype")
    val normalisedEventType: String? = null,
    @SerialName("normalisedeventtypetext")
    val normalisedEventTypeText: String? = null,
    @SerialName("action")
    val action: ElearningEventAction? = null,
    @SerialName("purpose")
    val purpose: String? = null,
    @SerialName("url")
    val url: String? = null
) {
    /**
     * Whether this is a site-wide event.
     */
    val isSiteEvent: Boolean get() = eventType == "site"

    /**
     * Whether this is a personal user event.
     */
    val isUserEvent: Boolean get() = eventType == "user"

    /**
     * Whether this is a group event.
     */
    val isGroupEvent: Boolean get() = eventType == "group"
}

/**
 * Represents an icon associated with a calendar event.
 *
 * @property key The icon key identifier.
 * @property component The component that provides this icon.
 * @property alternativeText The alternative text for accessibility.
 * @property iconUrl The URL to the icon image.
 * @property iconClass The CSS class for the icon.
 */
@Serializable
data class ElearningEventIcon(
    @SerialName("key")
    val key: String? = null,
    @SerialName("component")
    val component: String? = null,
    @SerialName("alttext")
    val alternativeText: String? = null,
    @SerialName("iconurl")
    val iconUrl: String? = null,
    @SerialName("iconclass")
    val iconClass: String? = null
)

/**
 * Represents course information associated with a calendar event.
 *
 * @property id The unique identifier of the course.
 * @property fullName The full name of the course.
 * @property shortName The short name of the course.
 * @property identificationNumber The identification number of the course.
 * @property summary The course summary text.
 * @property summaryFormat The format of the summary (0=MOODLE, 1=HTML, 2=PLAIN, 4=MARKDOWN).
 * @property startDate The Unix timestamp when the course starts.
 * @property endDate The Unix timestamp when the course ends.
 * @property visible Whether the course is visible.
 * @property fullNameDisplay The full name for display purposes.
 * @property viewUrl The URL to view this course.
 * @property courseImage The URL to the course image.
 * @property progress The course completion progress percentage.
 * @property hasProgress Whether the course has progress tracking enabled.
 * @property isFavourite Whether the course is marked as favourite.
 * @property hidden Whether the course is hidden from the user.
 * @property courseCategory The category name of the course.
 */
@Serializable
data class ElearningEventCourse(
    @SerialName("id")
    val id: Int,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("shortname")
    val shortName: String? = null,
    @SerialName("idnumber")
    val identificationNumber: String? = null,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("summaryformat")
    val summaryFormat: Int? = null,
    @SerialName("startdate")
    val startDate: Long? = null,
    @SerialName("enddate")
    val endDate: Long? = null,
    @SerialName("visible")
    val visible: Boolean? = null,
    @SerialName("fullnamedisplay")
    val fullNameDisplay: String? = null,
    @SerialName("viewurl")
    val viewUrl: String? = null,
    @SerialName("courseimage")
    val courseImage: String? = null,
    @SerialName("progress")
    val progress: Double? = null,
    @SerialName("hasprogress")
    val hasProgress: Boolean? = null,
    @SerialName("isfavourite")
    val isFavourite: Boolean? = null,
    @SerialName("hidden")
    val hidden: Boolean? = null,
    @SerialName("coursecategory")
    val courseCategory: String? = null
)

/**
 * Represents an action associated with a calendar event.
 *
 * @property name The name of the action.
 * @property url The URL to perform this action.
 * @property itemCount The number of items related to this action.
 * @property actionable Whether the action can be performed.
 * @property showItemCount Whether to display the item count.
 */
@Serializable
data class ElearningEventAction(
    @SerialName("name")
    val name: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("itemcount")
    val itemCount: Int = 0,
    @SerialName("actionable")
    val actionable: Boolean = true,
    @SerialName("showitemcount")
    val showItemCount: Boolean = false
)

/**
 * Represents a date/time in the calendar.
 *
 * @property seconds The seconds component (0-59).
 * @property minutes The minutes component (0-59).
 * @property hours The hours component (0-23).
 * @property monthDay The day of the month (1-31).
 * @property weekDay The day of the week (0=Sunday, 6=Saturday).
 * @property month The month (1-12).
 * @property year The year.
 * @property yearDay The day of the year (0-365).
 * @property weekdayName The full name of the weekday.
 * @property timestamp The Unix timestamp.
 */
@Serializable
data class ElearningCalendarDate(
    @SerialName("seconds")
    val seconds: Int? = null,
    @SerialName("minutes")
    val minutes: Int? = null,
    @SerialName("hours")
    val hours: Int? = null,
    @SerialName("mday")
    val monthDay: Int? = null,
    @SerialName("wday")
    val weekDay: Int? = null,
    @SerialName("mon")
    val month: Int? = null,
    @SerialName("year")
    val year: Int? = null,
    @SerialName("yday")
    val yearDay: Int? = null,
    @SerialName("weekday")
    val weekdayName: String? = null,
    @SerialName("timestamp")
    val timestamp: Long? = null
)

/**
 * Represents a week in the calendar monthly view.
 *
 * @property prePadding List of day numbers to pad before the first day of the week.
 * @property postPadding List of day numbers to pad after the last day of the week.
 * @property days The list of days in this week.
 */
@Serializable
data class ElearningCalendarWeek(
    @SerialName("prepadding")
    val prePadding: List<Int> = emptyList(),
    @SerialName("postpadding")
    val postPadding: List<Int> = emptyList(),
    @SerialName("days")
    val days: List<ElearningCalendarDay> = emptyList()
)

/**
 * Represents a day in the calendar.
 *
 * @property monthDay The day of the month (1-31).
 * @property weekDay The day of the week (0=Sunday, 6=Saturday).
 * @property year The year.
 * @property yearDay The day of the year (0-365).
 * @property isToday Whether this day is today.
 * @property isWeekend Whether this day is a weekend day.
 * @property timestamp The Unix timestamp of this day.
 * @property newEventTimestamp The Unix timestamp to use when creating a new event on this day.
 * @property viewDayLink The URL to view this day in detail.
 * @property events The list of events on this day.
 * @property hasEvents Whether this day has any events.
 * @property calendarEventTypes The list of event type identifiers on this day.
 * @property popoverTitle The title to display in a popover for this day.
 * @property dayTitle The title of this day.
 */
@Serializable
data class ElearningCalendarDay(
    @SerialName("mday")
    val monthDay: Int,
    @SerialName("wday")
    val weekDay: Int? = null,
    @SerialName("year")
    val year: Int? = null,
    @SerialName("yday")
    val yearDay: Int? = null,
    @SerialName("istoday")
    val isToday: Boolean = false,
    @SerialName("isweekend")
    val isWeekend: Boolean = false,
    @SerialName("timestamp")
    val timestamp: Long? = null,
    @SerialName("neweventtimestamp")
    val newEventTimestamp: Long? = null,
    @SerialName("viewdaylink")
    val viewDayLink: String? = null,
    @SerialName("events")
    val events: List<ElearningCalendarEvent> = emptyList(),
    @SerialName("hasevents")
    val hasEvents: Boolean = false,
    @SerialName("calendareventtypes")
    val calendarEventTypes: List<String> = emptyList(),
    @SerialName("popovertitle")
    val popoverTitle: String? = null,
    @SerialName("daytitle")
    val dayTitle: String? = null
)

/**
 * Represents a day name in the calendar.
 *
 * @property dayNumber The day number (0=Sunday, 6=Saturday).
 * @property shortName The abbreviated name of the day.
 * @property fullName The full name of the day.
 */
@Serializable
data class ElearningCalendarDayName(
    @SerialName("dayno")
    val dayNumber: Int,
    @SerialName("shortname")
    val shortName: String,
    @SerialName("fullname")
    val fullName: String
)

/**
 * Request to retrieve action events sorted by time.
 *
 * @property timesortFrom The Unix timestamp to start filtering events from.
 * @property timesortTo The Unix timestamp to stop filtering events at.
 * @property afterEventId Only return events after this event identifier (for pagination).
 * @property limitNum The maximum number of events to return.
 * @property searchValue Optional search string to filter events.
 */
@Serializable
class ElearningGetActionEventsByTimesortRequest(
    private val timesortFrom: Long? = null,
    private val timesortTo: Long? = null,
    private val afterEventId: Int? = null,
    private val limitNum: Int = 20,
    private val searchValue: String? = null
) : ElearningRequest<ElearningGetActionEventsByTimesortResponse> {
    override val functionName = "core_calendar_get_action_events_by_timesort"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        timesortFrom?.let { formData.append("timesortfrom", it.toString()) }
        timesortTo?.let { formData.append("timesortto", it.toString()) }
        afterEventId?.let { formData.append("aftereventid", it.toString()) }
        formData.append("limitnum", limitNum.toString())
        searchValue?.let { formData.append("searchvalue", it) }
    }
}

/**
 * Response containing action events sorted by time.
 *
 * @property events The list of calendar events.
 * @property firstId The identifier of the first event in the result set.
 * @property lastId The identifier of the last event in the result set.
 */
@Serializable
data class ElearningGetActionEventsByTimesortResponse(
    @SerialName("events")
    val events: List<ElearningCalendarEvent> = emptyList(),
    @SerialName("firstid")
    val firstId: Int? = null,
    @SerialName("lastid")
    val lastId: Int? = null
) : ElearningResponse

/**
 * Request to retrieve action events for a specific course.
 *
 * @property courseId The course identifier to get action events for.
 * @property timesortFrom The Unix timestamp to start filtering events from.
 * @property timesortTo The Unix timestamp to stop filtering events at.
 * @property afterEventId Only return events after this event identifier (for pagination).
 * @property limitNum The maximum number of events to return.
 * @property searchValue Optional search string to filter events.
 */
@Serializable
class ElearningGetActionEventsByCourseRequest(
    private val courseId: Int,
    private val timesortFrom: Long? = null,
    private val timesortTo: Long? = null,
    private val afterEventId: Int? = null,
    private val limitNum: Int = 20,
    private val searchValue: String? = null
) : ElearningRequest<ElearningGetActionEventsByCourseResponse> {
    override val functionName = "core_calendar_get_action_events_by_course"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
        timesortFrom?.let { formData.append("timesortfrom", it.toString()) }
        timesortTo?.let { formData.append("timesortto", it.toString()) }
        afterEventId?.let { formData.append("aftereventid", it.toString()) }
        formData.append("limitnum", limitNum.toString())
        searchValue?.let { formData.append("searchvalue", it) }
    }
}

/**
 * Response containing action events for a course.
 *
 * @property events The list of calendar events for the course.
 * @property firstId The identifier of the first event in the result set.
 * @property lastId The identifier of the last event in the result set.
 */
@Serializable
data class ElearningGetActionEventsByCourseResponse(
    @SerialName("events")
    val events: List<ElearningCalendarEvent> = emptyList(),
    @SerialName("firstid")
    val firstId: Int? = null,
    @SerialName("lastid")
    val lastId: Int? = null
) : ElearningResponse
