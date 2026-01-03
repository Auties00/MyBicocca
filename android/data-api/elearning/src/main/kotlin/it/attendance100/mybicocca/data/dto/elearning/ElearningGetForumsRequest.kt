package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetForumsRequest(
    private val courseIds: List<Int>
) : ElearningRequest<ElearningGetForumsResponse> {
    override val functionName = "mod_forum_get_forums_by_courses"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        courseIds.forEachIndexed { index, id ->
            formData.append("courseids[$index]", id.toString())
        }
    }
}
