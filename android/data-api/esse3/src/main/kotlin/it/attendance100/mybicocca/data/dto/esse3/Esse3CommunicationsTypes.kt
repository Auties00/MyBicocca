package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3CommunicationInsert(
    @SerialName("destinatari")
    val recipients: List<Esse3RecipientInsert> = emptyList(),

    @SerialName("tipoComCod")
    val municipalityTypeCode: String? = null,

    @SerialName("deviceCod")
    val deviceCode: String,

    @SerialName("mittente")
    val sender: String? = null,

    @SerialName("da")
    val from: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("testo")
    val text: String,

    @SerialName("htmlFlg")
    val htmlFlag: Int? = null,

    @SerialName("bachecaFlg")
    val noticeboardFlag: Int? = null,

    @SerialName("notifPushFlg")
    val pushNotificationFlag: Int? = null,

    @SerialName("dataValIni")
    val initialValidityDate: String? = null,

    @SerialName("dataValFin")
    val finalValidityDate: String? = null
)

@Serializable
data class Esse3RecipientInsert(
    @SerialName("origineDato")
    val dataOrigin: String,

    @SerialName("idAnagrafica")
    val personalDataId: Long? = null,

    @SerialName("recapito")
    val contact: String? = null,

    @SerialName("ccFlg")
    val carbonCopyFlag: Int? = null,

    @SerialName("ccnFlg")
    val blindCarbonCopyFlag: Int? = null
)

@Serializable
data class Esse3Communication(
    @SerialName("dataInvio")
    val sendingDate: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("errDes")
    val errorDescription: String? = null,

    @SerialName("errId")
    val errorId: Int? = null,

    @SerialName("statoComDes")
    val municipalityStateDescription: String? = null,

    @SerialName("statoComId")
    val municipalityStateId: Int? = null,

    @SerialName("tipoDevCod")
    val deviationTypeCode: String? = null,

    @SerialName("eventoDes")
    val eventDescription: String? = null,

    @SerialName("eventoCod")
    val eventCode: String? = null,

    @SerialName("deviceDes")
    val deviceDescription: String? = null,

    @SerialName("tipoComDes")
    val municipalityTypeDescription: String? = null,

    @SerialName("dataValFin")
    val finalValidityDate: String? = null,

    @SerialName("dataValIni")
    val initialValidityDate: String? = null,

    @SerialName("notifPushFlg")
    val pushNotificationFlag: Int? = null,

    @SerialName("bachecaFlg")
    val noticeboardFlag: Int? = null,

    @SerialName("htmlFlg")
    val htmlFlag: Int? = null,

    @SerialName("testo")
    val text: String? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("da")
    val from: String? = null,

    @SerialName("mittente")
    val sender: String? = null,

    @SerialName("deviceCod")
    val deviceCode: String? = null,

    @SerialName("tipoComCod")
    val municipalityTypeCode: String? = null,

    @SerialName("comId")
    val municipalityId: Long? = null
)

@Serializable
data class Esse3Recipient(
    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    @SerialName("dataSped")
    val shippingDate: String? = null,

    @SerialName("spedFlg")
    val shippingFlag: Int? = null,

    @SerialName("idUser")
    val userId: Long? = null,

    @SerialName("nominativo")
    val fullName: String? = null,

    @SerialName("ccnFlg")
    val blindCarbonCopyFlag: Int? = null,

    @SerialName("ccFlg")
    val carbonCopyFlag: Int? = null,

    @SerialName("recapito")
    val contact: String? = null,

    @SerialName("idAnagrafica")
    val personalDataId: Long? = null,

    @SerialName("origineDato")
    val dataOrigin: String? = null,

    @SerialName("destId")
    val destinationId: Long? = null,

    @SerialName("comId")
    val municipalityId: Long? = null,

    @SerialName("comDestId")
    val destinationMunicipalityId: Long? = null
)

@Serializable
data class Esse3CommunicationWithRecipients(
    @SerialName("dataInvio")
    val sendingDate: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("errDes")
    val errorDescription: String? = null,

    @SerialName("errId")
    val errorId: Int? = null,

    @SerialName("statoComDes")
    val municipalityStateDescription: String? = null,

    @SerialName("statoComId")
    val municipalityStateId: Int? = null,

    @SerialName("tipoDevCod")
    val deviationTypeCode: String? = null,

    @SerialName("eventoDes")
    val eventDescription: String? = null,

    @SerialName("eventoCod")
    val eventCode: String? = null,

    @SerialName("deviceDes")
    val deviceDescription: String? = null,

    @SerialName("tipoComDes")
    val municipalityTypeDescription: String? = null,

    @SerialName("dataValFin")
    val finalValidityDate: String? = null,

    @SerialName("dataValIni")
    val initialValidityDate: String? = null,

    @SerialName("notifPushFlg")
    val pushNotificationFlag: Int? = null,

    @SerialName("bachecaFlg")
    val noticeboardFlag: Int? = null,

    @SerialName("htmlFlg")
    val htmlFlag: Int? = null,

    @SerialName("testo")
    val text: String? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("da")
    val from: String? = null,

    @SerialName("mittente")
    val sender: String? = null,

    @SerialName("deviceCod")
    val deviceCode: String? = null,

    @SerialName("tipoComCod")
    val municipalityTypeCode: String? = null,

    @SerialName("comId")
    val municipalityId: Long? = null,

    @SerialName("destinatari")
    val recipients: List<Esse3Recipient> = emptyList()
)
