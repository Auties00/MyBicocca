package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class ElearningCalendarDayName(
    @SerialName("dayno")
    val dayNo: Int,
    @SerialName("shortname")
    val shortName: String,
    @SerialName("fullname")
    val fullName: String
)
