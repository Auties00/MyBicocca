package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ReviewOption(
    @SerializedName("name") val name: String? = null,
    @SerializedName("value") val value: Int? = null
)
