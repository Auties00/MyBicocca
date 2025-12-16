package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class MessageSearchUsersResponse(
    @SerializedName("contacts") val contacts: List<UserContact>? = null,
    @SerializedName("noncontacts") val nonContacts: List<UserContact>? = null
)