package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetQuizAccessInfoRequest(
    private val quizId: Int
) : ElearningRequest<ElearningGetQuizAccessInfoResponse> {
    override val functionName: String
        get() = "mod_quiz_get_quiz_access_information"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("quizid", quizId.toString())
    }
}
