package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a calendar event.
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
    val activityStr: String? = null,
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
    val isSiteEvent: Boolean get() = eventType == "site"
    val isUserEvent: Boolean get() = eventType == "user"
    val isGroupEvent: Boolean get() = eventType == "group"
}

@Serializable
data class ElearningEventIcon(
    @SerialName("key")
    val key: String? = null,
    @SerialName("component")
    val component: String? = null,
    @SerialName("alttext")
    val altText: String? = null,
    @SerialName("iconurl")
    val iconUrl: String? = null,
    @SerialName("iconclass")
    val iconClass: String? = null
)

@Serializable
data class ElearningEventCourse(
    @SerialName("id")
    val id: Int,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("shortname")
    val shortName: String? = null,
    @SerialName("idnumber")
    val idNumber: String? = null,
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

@Serializable
data class ElearningCalendarWeek(
    @SerialName("prepadding")
    val prePadding: List<Int> = emptyList(),
    @SerialName("postpadding")
    val postPadding: List<Int> = emptyList(),
    @SerialName("days")
    val days: List<ElearningCalendarDay> = emptyList()
)

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
