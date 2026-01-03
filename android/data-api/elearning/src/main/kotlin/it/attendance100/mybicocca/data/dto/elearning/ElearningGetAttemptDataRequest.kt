package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetAttemptDataRequest(
    private val attemptId: Int,
    private val page: Int = 0,
    private val preflightData: List<PreflightDataItem> = emptyList()
) : ElearningRequest<ElearningGetAttemptDataResponse> {
    override val functionName: String
        get() = "mod_quiz_get_attempt_data"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        formData.append("page", page.toString())
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}
