package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class StoredFileUrls(
    @SerializedName("export") val export: String? = null
)
