package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappAppealSession(
    @SerializedName("appealId")
    val appealId: Int? = null,

    @SerializedName("activityAppealId")
    val activityAppealId: Int? = null,

    @SerializedName("yearCalendaId")
    val yearCalendaId: Int? = null,

    @SerializedName("reserved")
    val reserved: Boolean? = null,

    @SerializedName("bookable")
    val bookable: Boolean? = null,

    @SerializedName("registrationStartDate")
    val registrationStartDate: String? = null,

    @SerializedName("registrationEndDate")
    val registrationEndDate: String? = null,

    @SerializedName("appealStartDate")
    val appealStartDate: String? = null,

    @SerializedName("appealDescr")
    val appealDescr: String? = null,

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("hourExam")
    val hourExam: String? = null,

    @SerializedName("chairmanId")
    val chairmanId: Int? = null,

    @SerializedName("chairmanSurname")
    val chairmanSurname: String? = null,

    @SerializedName("chairmanName")
    val chairmanName: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("statusDescr")
    val statusDescr: String? = null,

    @SerializedName("appealExamCode")
    val appealExamCode: String? = null,

    @SerializedName("appealExamMode")
    val appealExamMode: String? = null,

    @SerializedName("appealExamResult")
    val appealExamResult: String? = null,

    @SerializedName("appealPublicationStatus")
    val appealPublicationStatus: String? = null,

    @SerializedName("appealRegistrationType")
    val appealRegistrationType: String? = null
)