package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetUserPreferencesRequest(
    private val name: String? = null,
    private val userId: Int? = null
) : ElearningRequest<ElearningGetUserPreferencesResponse> {
    override val functionName = "core_user_get_user_preferences"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        name?.let { formData.append("name", it) }
        userId?.let { formData.append("userid", it.toString()) }
    }
}
