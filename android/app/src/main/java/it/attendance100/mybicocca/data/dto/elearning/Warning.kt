package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class Warning (
    @SerializedName("warningcode") val warningCode: String,
    @SerializedName("message") val message: String,
    @SerializedName("item") val item: String? = null,
    @SerializedName("itemid") val itemId: Int? = null
)