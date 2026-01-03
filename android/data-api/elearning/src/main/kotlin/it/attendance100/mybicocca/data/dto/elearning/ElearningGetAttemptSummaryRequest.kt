package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetAttemptSummaryRequest(
    private val attemptId: Int,
    private val preflightData: List<PreflightDataItem> = emptyList()
) : ElearningRequest<ElearningGetAttemptSummaryResponse> {
    override val functionName: String
        get() = "mod_quiz_get_attempt_summary"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}
