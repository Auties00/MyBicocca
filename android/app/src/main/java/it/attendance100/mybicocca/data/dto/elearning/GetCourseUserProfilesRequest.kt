package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCourseUserProfilesRequest(
    @SerializedName("userlist") val userList: List<UserListEntry>
)

data class UserListEntry(
    @SerializedName("userid") val userId: Int,
    @SerializedName("courseid") val courseId: Int
)