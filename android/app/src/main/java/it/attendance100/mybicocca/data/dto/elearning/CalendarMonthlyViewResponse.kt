package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarMonthlyViewResponse(
    @SerializedName("url") val url: String? = null,
    @SerializedName("courseid") val courseId: Int? = null,
    @SerializedName("categoryid") val categoryId: Int? = null,
    @SerializedName("filter_selector") val filterSelector: String? = null,
    @SerializedName("weeks") val weeks: List<CalendarWeek>? = null,
    @SerializedName("daynames") val dayNames: List<CalendarDayName>? = null,
    @SerializedName("date") val date: CalendarDate? = null,
    @SerializedName("periodname") val periodName: String? = null,
    @SerializedName("includenavigation") val includeNavigation: Boolean? = null,
    @SerializedName("initialeventsloaded") val initialEventsLoaded: Boolean? = null,
    @SerializedName("previousperiod") val previousPeriod: CalendarDate? = null,
    @SerializedName("previousperiodlink") val previousPeriodLink: String? = null,
    @SerializedName("previousperiodname") val previousPeriodName: String? = null,
    @SerializedName("nextperiod") val nextPeriod: CalendarDate? = null,
    @SerializedName("nextperiodlink") val nextPeriodLink: String? = null,
    @SerializedName("nextperiodname") val nextPeriodName: String? = null,
    @SerializedName("larrow") val lArrow: String? = null,
    @SerializedName("rarrow") val rArrow: String? = null,
    @SerializedName("defaulteventcontext") val defaultEventContext: Int? = null
)