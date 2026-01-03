package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningSaveAttemptRequest(
    private val attemptId: Int,
    private val data: List<AttemptDataItem> = emptyList(),
    private val preflightData: List<PreflightDataItem> = emptyList()
) : ElearningRequest<ElearningSaveAttemptResponse> {
    override val functionName: String
        get() = "mod_quiz_save_attempt"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        data.forEachIndexed { index, item ->
            formData.append("data[$index][name]", item.name)
            formData.append("data[$index][value]", item.value)
        }
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}

@Serializable
data class AttemptDataItem(
    val name: String,
    val value: String
)
