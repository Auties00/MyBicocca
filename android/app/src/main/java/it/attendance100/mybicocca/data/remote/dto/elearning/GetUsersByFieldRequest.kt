package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetUsersByFieldRequest(
    @SerializedName("field") val field: UserSearchFieldEnum,
    @SerializedName("values") val values: List<String>
)