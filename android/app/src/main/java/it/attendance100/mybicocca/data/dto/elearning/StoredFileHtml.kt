package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class StoredFileHtml(
    @SerializedName("plagiarism") val plagiarism: String? = null
)
