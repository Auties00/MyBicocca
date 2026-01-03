package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningUpdateActivityCompletionRequest(
    private val cmId: Int,
    private val completed: Boolean
) : ElearningRequest<ElearningUpdateActivityCompletionResponse> {
    override val functionName = "core_completion_update_activity_completion_status_manually"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("cmid", cmId.toString())
        formData.append("completed", if (completed) "1" else "0")
    }
}
