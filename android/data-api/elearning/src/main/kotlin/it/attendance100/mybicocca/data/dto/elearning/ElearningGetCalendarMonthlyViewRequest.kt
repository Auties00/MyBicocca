package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

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
