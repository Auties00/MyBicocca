package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetUserBestGradeRequest(
    private val quizId: Int,
    private val userId: Int? = null
) : ElearningRequest<ElearningGetUserBestGradeResponse> {
    override val functionName: String
        get() = "mod_quiz_get_user_best_grade"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("quizid", quizId.toString())
        if (userId != null) {
            formData.append("userid", userId.toString())
        }
    }
}
