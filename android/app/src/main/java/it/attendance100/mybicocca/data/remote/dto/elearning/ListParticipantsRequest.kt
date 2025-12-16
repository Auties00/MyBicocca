package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ListParticipantsRequest(
    @SerializedName("assignid") val assignId: Int,
    @SerializedName("groupid") val groupId: Int? = null,
    @SerializedName("filter") val filter: String? = null,
    @SerializedName("skip") val skip: Int? = null,
    @SerializedName("limit") val limit: Int? = null,
    @SerializedName("onlyids") val onlyIds: Boolean? = null,
    @SerializedName("includeenrolments") val includeEnrolments: Boolean? = null,
    @SerializedName("tablesort") val tableSort: Boolean? = null
)
