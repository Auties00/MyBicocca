package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class UserRole(
    @SerializedName("roleid") val roleId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("shortname") val shortName: String? = null,
    @SerializedName("sortorder") val sortOrder: Int? = null
)