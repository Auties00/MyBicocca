package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UpdateInstance(
    @SerializedName("contextlevel") val contextLevel: String? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("updates") val updates: List<UpdateDetail>? = null
)