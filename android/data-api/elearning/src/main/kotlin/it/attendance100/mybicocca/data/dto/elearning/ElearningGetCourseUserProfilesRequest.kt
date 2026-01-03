package it.attendance100.mybicocca.data.dto.elearning

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.Serializable

@Serializable
data class UserCourseRequest(
    val userid: Int,
    val courseid: Int
)

@Serializable
class ElearningGetCourseUserProfilesRequest(
    private val userList: List<UserCourseRequest>
) : ElearningRequest<ElearningGetCourseUserProfilesResponse> {
    override val functionName = "core_user_get_course_user_profiles"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        userList.forEachIndexed { index, request ->
            formData.append("userlist[$index][userid]", request.userid.toString())
            formData.append("userlist[$index][courseid]", request.courseid.toString())
        }
    }
}
