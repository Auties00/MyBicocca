package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningViewUserProfileRequest(
    private val userId: Int,
    private val courseId: Int? = null
) : ElearningRequest<ElearningViewUserProfileResponse> {
    override val functionName = "core_user_view_user_profile"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        courseId?.let { formData.append("courseid", it.toString()) }
    }
}
