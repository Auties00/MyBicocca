package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCombinedReviewOptionsResponse(
    @SerializedName("someoptions") val someOptions: List<ReviewOption>? = null,
    @SerializedName("alloptions") val allOptions: List<ReviewOption>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
