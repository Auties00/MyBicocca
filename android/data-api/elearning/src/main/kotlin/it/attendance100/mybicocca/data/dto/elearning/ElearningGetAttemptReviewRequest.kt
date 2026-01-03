package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetAttemptReviewRequest(
    private val attemptId: Int,
    private val page: Int? = null
) : ElearningRequest<ElearningGetAttemptReviewResponse> {
    override val functionName: String
        get() = "mod_quiz_get_attempt_review"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("attemptid", attemptId.toString())
        if (page != null) {
            formData.append("page", page.toString())
        }
    }
}
