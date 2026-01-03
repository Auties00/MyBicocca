package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.Serializable

@Serializable
data class ElearningGetCourseUserProfilesResponse(
    override val items: List<ElearningUserProfile>
) : ElearningListResponse<ElearningUserProfile> {
    val users: List<ElearningUserProfile> get() = items
}
