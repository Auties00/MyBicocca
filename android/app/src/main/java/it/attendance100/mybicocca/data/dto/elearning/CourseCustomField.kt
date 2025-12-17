package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CourseCustomField(
    @SerializedName("name") val name: String,
    @SerializedName("shortname") val shortName: String,
    @SerializedName("type") val type: String,
    @SerializedName("valueraw") val valueRaw: String,
    @SerializedName("value") val value: String
)