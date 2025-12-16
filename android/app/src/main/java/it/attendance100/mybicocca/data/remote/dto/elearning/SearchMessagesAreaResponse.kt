package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SearchMessagesAreaResponse(
    @SerializedName("contacts") val contacts: List<Any>? = null
)