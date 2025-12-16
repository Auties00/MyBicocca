package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class UpdateDetail(
    @SerializedName("name") val name: String? = null,
    @SerializedName("timeupdated") val timeUpdated: Int? = null,
    @SerializedName("itemids") val itemIds: List<Int>? = null
)