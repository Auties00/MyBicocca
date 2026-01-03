package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetActionEventsByCourseRequest(
    private val courseId: Int,
    private val timesortFrom: Long? = null,
    private val timesortTo: Long? = null,
    private val afterEventId: Int? = null,
    private val limitNum: Int = 20,
    private val searchValue: String? = null
) : ElearningRequest<ElearningGetActionEventsByCourseResponse> {
    override val functionName = "core_calendar_get_action_events_by_course"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
        timesortFrom?.let { formData.append("timesortfrom", it.toString()) }
        timesortTo?.let { formData.append("timesortto", it.toString()) }
        afterEventId?.let { formData.append("aftereventid", it.toString()) }
        formData.append("limitnum", limitNum.toString())
        searchValue?.let { formData.append("searchvalue", it) }
    }
}
