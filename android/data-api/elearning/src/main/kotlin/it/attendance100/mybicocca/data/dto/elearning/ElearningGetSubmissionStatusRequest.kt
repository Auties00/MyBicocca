package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetSubmissionStatusRequest(
    private val assignId: Int,
    private val userId: Int? = null,
    private val groupId: Int? = null
) : ElearningRequest<ElearningGetSubmissionStatusResponse> {
    override val functionName = "mod_assign_get_submission_status"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("assignid", assignId.toString())
        userId?.let { formData.append("userid", it.toString()) }
        groupId?.let { formData.append("groupid", it.toString()) }
    }
}
