package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarAccessInfoResponse(
    @SerializedName("canmanageentries") val canManageEntries: Boolean? = null,
    @SerializedName("canmanageownentries") val canManageOwnEntries: Boolean? = null,
    @SerializedName("canmanagegroupentries") val canManageGroupEntries: Boolean? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)