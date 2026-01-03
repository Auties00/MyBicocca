package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningStartAttemptRequest(
    private val quizId: Int,
    private val preflightData: List<PreflightDataItem> = emptyList(),
    private val forceNew: Boolean = false
) : ElearningRequest<ElearningStartAttemptResponse> {
    override val functionName: String
        get() = "mod_quiz_start_attempt"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("quizid", quizId.toString())
        formData.append("forcenew", if (forceNew) "1" else "0")
        preflightData.forEachIndexed { index, item ->
            formData.append("preflightdata[$index][name]", item.name)
            formData.append("preflightdata[$index][value]", item.value)
        }
    }
}

@Serializable
data class PreflightDataItem(
    val name: String,
    val value: String
)
