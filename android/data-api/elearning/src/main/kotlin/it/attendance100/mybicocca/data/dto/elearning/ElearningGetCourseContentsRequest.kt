package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetCourseContentsRequest(
    private val courseId: Int
) : ElearningRequest<ElearningGetCourseContentsResponse> {
    override val functionName: String
        get() = "core_course_get_contents"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
    }
}
