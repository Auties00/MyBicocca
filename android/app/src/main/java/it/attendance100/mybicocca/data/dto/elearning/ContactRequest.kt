package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ContactRequest(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("userid") val userId: Int? = null,
    @SerializedName("requesteduserid") val requestedUserId: Int? = null,
    @SerializedName("timecreated") val timeCreated: Int? = null
)