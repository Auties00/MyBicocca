package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningProcessAttemptRequest(
    private val attemptId: Int,
    private val data: List<AttemptDataItem> = emptyList(),
    private val finishAttempt: Boolean = true,
    private val timeUp: Boolean = false,
    private val preflightData: List<PreflightDataItem> = emptyList()
) : ElearningRequest<ElearningProcessAttemptResponse> {
    override val functionName: String
        get() = "mod_quiz_process_attempt"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        formData.append("finishattempt", if (finishAttempt) "1" else "0")
        formData.append("timeup", if (timeUp) "1" else "0")
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
