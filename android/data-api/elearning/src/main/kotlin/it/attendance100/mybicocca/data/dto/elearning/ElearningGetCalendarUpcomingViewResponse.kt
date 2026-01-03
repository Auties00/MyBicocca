package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
