package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappCareerRegistration(
    @SerializedName("cdsId")
    val cdsId: Int? = null,

    @SerializedName("activityId")
    val activityId: Int? = null,

    @SerializedName("activityAppealId")
    val activityAppealId: Int? = null,

    @SerializedName("activityItemId")
    val activityItemId: Int? = null,

    @SerializedName("studentId")
    val studentId: Int? = null,

    @SerializedName("appealDate")
    val appealDate: String? = null,

    @SerializedName("appealDescr")
    val appealDescr: String? = null,

    @SerializedName("courseDescr")
    val courseDescr: String? = null,

    @SerializedName("sessionDescr")
    val sessionDescr: String? = null,

    @SerializedName("typeAppealCode")
    val typeAppealCode: String? = null,

    @SerializedName("position")
    val position: Int? = null,

    @SerializedName("status")
    val status: String? = null
)