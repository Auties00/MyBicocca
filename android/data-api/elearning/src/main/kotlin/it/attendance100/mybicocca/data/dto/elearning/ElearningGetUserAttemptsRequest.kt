package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetUserAttemptsRequest(
    private val quizId: Int,
    private val userId: Int? = null,
    private val status: AttemptStatus = AttemptStatus.ALL,
    private val includePreviews: Boolean = false
) : ElearningRequest<ElearningGetUserAttemptsResponse> {
    override val functionName: String
        get() = "mod_quiz_get_user_attempts"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("quizid", quizId.toString())
        if (userId != null) {
            formData.append("userid", userId.toString())
        }
        formData.append("status", status.value)
        formData.append("includepreviews", if (includePreviews) "1" else "0")
    }

    enum class AttemptStatus(val value: String) {
        ALL("all"),
        FINISHED("finished"),
        UNFINISHED("unfinished")
    }
}
