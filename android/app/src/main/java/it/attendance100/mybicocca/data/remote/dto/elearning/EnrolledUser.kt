package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.net.URI

data class EnrolledUser(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("firstname") val firstName: String? = null,
    @SerializedName("lastname") val lastName: String? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("firstaccess") val firstAccess: Int? = null,
    @SerializedName("lastaccess") val lastAccess: Int? = null,
    @SerializedName("lastcourseaccess") val lastCourseAccess: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("descriptionformat") val descriptionFormat: Int? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("profileimageurlsmall") val profileImageUrlSmall: URI? = null,
    @SerializedName("profileimageurl") val profileImageUrl: URI? = null,
    @SerializedName("groups") val groups: List<UserGroup>? = null,
    @SerializedName("roles") val roles: List<UserRole>? = null,
    @SerializedName("enrolledcourses") val enrolledCourses: List<UserEnrolledCourse>? = null
)