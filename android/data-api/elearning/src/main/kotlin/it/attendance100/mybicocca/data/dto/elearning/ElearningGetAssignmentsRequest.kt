package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetAssignmentsRequest(
    private val courseIds: List<Int>,
    private val capabilities: List<String>? = null,
    private val includeNotEnrolledCourses: Boolean = false
) : ElearningRequest<ElearningGetAssignmentsResponse> {
    override val functionName = "mod_assign_get_assignments"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        courseIds.forEachIndexed { index, id ->
            formData.append("courseids[$index]", id.toString())
        }
        capabilities?.forEachIndexed { index, cap ->
            formData.append("capabilities[$index]", cap)
        }
        if (includeNotEnrolledCourses) formData.append("includenotenrolledcourses", "1")
    }
}
