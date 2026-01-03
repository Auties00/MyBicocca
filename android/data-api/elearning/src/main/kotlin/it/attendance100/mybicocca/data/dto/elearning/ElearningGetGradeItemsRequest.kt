package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetGradeItemsRequest(
    private val courseId: Int,
    private val userId: Int? = null,
    private val groupId: Int? = null
) : ElearningRequest<ElearningGetGradeItemsResponse> {
    override val functionName = "gradereport_user_get_grade_items"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("courseid", courseId.toString())
        userId?.let { formData.append("userid", it.toString()) }
        groupId?.let { formData.append("groupid", it.toString()) }
    }
}
