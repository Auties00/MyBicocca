package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappExamSession(
    @SerializedName("yearFreqId")
    val yearFreqId: Int? = null,

    @SerializedName("activityCode")
    val activityCode: String? = null,

    @SerializedName("activityItemId")
    val activityItemId: Int? = null,

    @SerializedName("activityDescr")
    val activityDescr: String? = null,

    @SerializedName("courseYear")
    val courseYear: Int? = null,

    @SerializedName("activityId")
    val activityId: Int? = null,

    @SerializedName("activityId_o")
    val activityIdOriginal: Int? = null, // mapped from activityId_o

    @SerializedName("cdsId")
    val cdsId: Int? = null,

    @SerializedName("cdsId_o")
    val cdsIdOriginal: Int? = null, // mapped from cdsId_o

    @SerializedName("appeals")
    val appeals: List<it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappAppealSession> = emptyList()
)