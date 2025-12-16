package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarDayViewResponse(
    @SerializedName("events") val events: List<CalendarEvent>? = null,
    @SerializedName("defaulteventcontext") val defaultEventContext: Int? = null,
    @SerializedName("filter_selector") val filterSelector: String? = null,
    @SerializedName("courseid") val courseId: Int? = null,
    @SerializedName("categoryid") val categoryId: Int? = null,
    @SerializedName("neweventtimestamp") val newEventTimestamp: Int? = null,
    @SerializedName("date") val date: CalendarDate? = null,
    @SerializedName("periodname") val periodName: String? = null,
    @SerializedName("previousperiod") val previousPeriod: CalendarDate? = null,
    @SerializedName("previousperiodlink") val previousPeriodLink: String? = null,
    @SerializedName("previousperiodname") val previousPeriodName: String? = null,
    @SerializedName("nextperiod") val nextPeriod: CalendarDate? = null,
    @SerializedName("nextperiodlink") val nextPeriodLink: String? = null,
    @SerializedName("nextperiodname") val nextPeriodName: String? = null,
    @SerializedName("larrow") val lArrow: String? = null,
    @SerializedName("rarrow") val rArrow: String? = null
)