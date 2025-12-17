package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName
import java.net.URI

data class UserContact(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("profileurl") val profileUrl: URI? = null,
    @SerializedName("profileimageurl") val profileImageUrl: URI? = null,
    @SerializedName("profileimageurlsmall") val profileImageUrlSmall: URI? = null,
    @SerializedName("isonline") val isOnline: Boolean? = null,
    @SerializedName("showonlinestatus") val showOnlineStatus: Boolean? = null,
    @SerializedName("isblocked") val isBlocked: Boolean? = null,
    @SerializedName("iscontact") val isContact: Boolean? = null,
    @SerializedName("isdeleted") val isDeleted: Boolean? = null,
    @SerializedName("canmessage") val canMessage: Boolean? = null,
    @SerializedName("requirescontact") val requiresContact: Boolean? = null
)