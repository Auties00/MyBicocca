package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappAppealSession(
    @SerialName("appealId")
    val appealId: Int? = null,

    @SerialName("activityAppealId")
    val activityAppealId: Int? = null,

    @SerialName("yearCalendaId")
    val yearCalendaId: Int? = null,

    @SerialName("reserved")
    val reserved: Boolean? = null,

    @SerialName("bookable")
    val bookable: Boolean? = null,

    @SerialName("registrationStartDate")
    val registrationStartDate: String? = null,

    @SerialName("registrationEndDate")
    val registrationEndDate: String? = null,

    @SerialName("appealStartDate")
    val appealStartDate: String? = null,

    @SerialName("appealDescr")
    val appealDescr: String? = null,

    @SerialName("note")
    val note: String? = null,

    @SerialName("hourExam")
    val hourExam: String? = null,

    @SerialName("chairmanId")
    val chairmanId: Int? = null,

    @SerialName("chairmanSurname")
    val chairmanSurname: String? = null,

    @SerialName("chairmanName")
    val chairmanName: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("statusDescr")
    val statusDescr: String? = null,

    @SerialName("appealExamCode")
    val appealExamCode: String? = null,

    @SerialName("appealExamMode")
    val appealExamMode: String? = null,

    @SerialName("appealExamResult")
    val appealExamResult: String? = null,

    @SerialName("appealPublicationStatus")
    val appealPublicationStatus: String? = null,

    @SerialName("appealRegistrationType")
    val appealRegistrationType: String? = null
)