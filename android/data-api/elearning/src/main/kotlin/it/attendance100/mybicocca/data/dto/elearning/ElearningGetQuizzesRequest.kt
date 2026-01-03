package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetQuizzesRequest(
    private val courseIds: List<Int>
) : ElearningRequest<ElearningGetQuizzesResponse> {
    override val functionName: String
        get() = "mod_quiz_get_quizzes_by_courses"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        courseIds.forEachIndexed { index, courseId ->
            formData.append("courseids[$index]", courseId.toString())
        }
    }
}
