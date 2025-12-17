package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UnreadCountsResponse(
    @SerializedName("favourites") val favourites: Int? = null,
    @SerializedName("types") val types: UnreadCountTypes? = null
)