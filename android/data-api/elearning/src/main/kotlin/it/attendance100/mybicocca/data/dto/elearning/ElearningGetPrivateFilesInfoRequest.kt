package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetPrivateFilesInfoRequest(
    private val userId: Int? = null
) : ElearningRequest<ElearningGetPrivateFilesInfoResponse> {
    override val functionName = "core_user_get_private_files_info"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userId?.let { formData.append("userid", it.toString()) }
    }
}
