package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarUpcomingViewResponse(
    @SerializedName("events") val events: List<CalendarEvent>? = null,
    @SerializedName("defaulteventcontext") val defaultEventContext: Int? = null,
    @SerializedName("filter_selector") val filterSelector: String? = null,
    @SerializedName("courseid") val courseId: Int? = null,
    @SerializedName("categoryid") val categoryId: Int? = null,
    @SerializedName("isloggedin") val isLoggedIn: Boolean? = null
)