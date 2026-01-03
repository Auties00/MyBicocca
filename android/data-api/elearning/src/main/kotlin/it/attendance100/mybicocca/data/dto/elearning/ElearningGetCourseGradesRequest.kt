package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetCourseGradesRequest(
    private val userId: Int? = null
) : ElearningRequest<ElearningGetCourseGradesResponse> {
    override val functionName = "gradereport_overview_get_course_grades"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userId?.let { formData.append("userid", it.toString()) }
    }
}
