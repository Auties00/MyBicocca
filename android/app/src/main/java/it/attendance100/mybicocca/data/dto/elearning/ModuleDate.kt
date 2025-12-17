package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ModuleDate(
    @SerializedName("label") val label: String? = null,
    @SerializedName("timestamp") val timestamp: Int? = null
)