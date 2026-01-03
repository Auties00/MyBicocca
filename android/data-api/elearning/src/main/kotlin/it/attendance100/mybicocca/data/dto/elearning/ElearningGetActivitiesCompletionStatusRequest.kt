package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetActivitiesCompletionStatusRequest(
    private val courseId: Int,
    private val userId: Int? = null
) : ElearningRequest<ElearningGetActivitiesCompletionStatusResponse> {
    override val functionName = "core_completion_get_activities_completion_status"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
        userId?.let { formData.append("userid", it.toString()) }
    }
}
