package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UnreadCountTypes(
    @SerializedName("1") val type1: Int? = null,
    @SerializedName("2") val type2: Int? = null,
    @SerializedName("3") val type3: Int? = null
)