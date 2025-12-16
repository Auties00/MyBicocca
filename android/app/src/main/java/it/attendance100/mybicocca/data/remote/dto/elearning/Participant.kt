package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class Participant(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("submitted") val submitted: Boolean? = null,
    @SerializedName("requiregrading") val requireGrading: Boolean? = null,
    @SerializedName("grantedextension") val grantedExtension: Boolean? = null,
    @SerializedName("groupid") val groupId: Int? = null,
    @SerializedName("groupname") val groupName: String? = null,
    @SerializedName("submissionstatus") val submissionStatus: String? = null
)
