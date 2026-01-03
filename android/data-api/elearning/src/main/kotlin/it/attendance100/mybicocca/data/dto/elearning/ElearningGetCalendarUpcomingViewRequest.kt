package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

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
