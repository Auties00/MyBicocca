package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCalendarEventsRequest(
    @SerializedName("events") val events: CalendarEventsFilter? = null,
    @SerializedName("options") val options: CalendarEventsOptions? = null
)

data class CalendarEventsFilter(
    @SerializedName("eventids") val eventIds: List<Int>? = null,
    @SerializedName("courseids") val courseIds: List<Int>? = null,
    @SerializedName("groupids") val groupIds: List<Int>? = null,
    @SerializedName("categoryids") val categoryIds: List<Int>? = null
)

data class CalendarEventsOptions(
    @SerializedName("userevents") val userEvents: Boolean? = null,
    @SerializedName("siteevents") val siteEvents: Boolean? = null,
    @SerializedName("timestart") val timeStart: Int? = null,
    @SerializedName("timeend") val timeEnd: Int? = null,
    @SerializedName("ignorehidden") val ignoreHidden: Boolean? = null
)