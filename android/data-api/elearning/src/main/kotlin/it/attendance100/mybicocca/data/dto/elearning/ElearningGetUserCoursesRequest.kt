package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
class ElearningGetUserCoursesRequest(
    private val userId: Int
) : ElearningRequest<ElearningGetUserCoursesResponse> {
    override val functionName: String
        get() = "core_enrol_get_users_courses"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("returnusercount", "0")
    }
}
