package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarMonthDay(
    @SerializedName("seconds") val seconds: Int? = null,
    @SerializedName("minutes") val minutes: Int? = null,
    @SerializedName("hours") val hours: Int? = null,
    @SerializedName("mday") val mday: Int? = null,
    @SerializedName("wday") val wday: Int? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("yday") val yday: Int? = null,
    @SerializedName("istoday") val isToday: Boolean? = null,
    @SerializedName("isweekend") val isWeekend: Boolean? = null,
    @SerializedName("timestamp") val timestamp: Int? = null,
    @SerializedName("neweventtimestamp") val newEventTimestamp: Int? = null,
    @SerializedName("viewdaylink") val viewDayLink: String? = null,
    @SerializedName("viewdaylinktitle") val viewDayLinkTitle: String? = null,
    @SerializedName("events") val events: List<CalendarEvent>? = null,
    @SerializedName("hasevents") val hasEvents: Boolean? = null,
    @SerializedName("calendareventtypes") val calendarEventTypes: List<String>? = null,
    @SerializedName("popovertitle") val popoverTitle: String? = null,
    @SerializedName("haslastdayofevent") val hasLastDayOfEvent: Boolean? = null
)