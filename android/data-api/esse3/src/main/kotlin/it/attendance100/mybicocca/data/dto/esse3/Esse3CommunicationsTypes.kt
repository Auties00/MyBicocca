package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3CommunicationInsert(
    @SerialName("destinatari")
    val recipients: List<Esse3RecipientInsert> = emptyList(),

    /** codice tipologia di comunicazione */
    @SerialName("tipoComCod")
    val municipalityTypeCode: String? = null,

    /** codice device di comunicazione */
    @SerialName("deviceCod")
    val deviceCode: String = "",

    /** descrizione mittente */
    @SerialName("mittente")
    val sender: String? = null,

    /** recapito mittente (indirizzo email o numero cellulare) */
    @SerialName("da")
    val from: String? = null,

    /** titolo */
    @SerialName("titolo")
    val title: String = "",

    /** titolo */
    @SerialName("testo")
    val text: String = "",

    /** indica se il testo e' in formato HTML (0=no, 1=si) */
    @SerialName("htmlFlg")
    val htmlFlag: Int? = null,

    /** indica se la comunicazione e' da pubblicare in bacheca privata studente (0=no, 1=si) */
    @SerialName("bachecaFlg")
    val noticeboardFlag: Int? = null,

    /** indica se la comunicazione e' da inviare anche come notifica push sulla App Mobile (0=no, 1=si) */
    @SerialName("notifPushFlg")
    val pushNotificationFlag: Int? = null,

    /** data di inizio validita' */
    @SerialName("dataValIni")
    val initialValidityDate: String? = null,

    /** data di fine validita' */
    @SerialName("dataValFin")
    val finalValidityDate: String? = null
)

@Serializable
data class Esse3RecipientInsert(
    /** tipologia di destinatario (PERSONE=studente/registrato, DOCENTI=docente, SOGG_EST=soggetto esterno, EXTERNAL=recapito email/cellulare) */
    @SerialName("origineDato")
    val dataOrigin: String = "",

    /** ID univoco di anagrafica, per la tipologia di destinatario (se origineDato vale PERSONE, DOCENTI o SOGG_EST) */
    @SerialName("idAnagrafica")
    val personalDataId: Long? = null,

    /** indirizzo email o numero di cellulare del destinatario */
    @SerialName("recapito")
    val contact: String? = null,

    /** indica se il destinatario e' in CC (copia) (0=no, 1=si) */
    @SerialName("ccFlg")
    val carbonCopyFlag: Int? = null,

    /** indica se il destinatario e' in CCN (copia nascosta) (0=no, 1=si) */
    @SerialName("ccnFlg")
    val blindCarbonCopyFlag: Int? = null
)

@Serializable
data class Esse3Communication(
    /** data/ora di invio della comunicazione */
    @SerialName("dataInvio")
    val sendingDate: String? = null,

    /** data/ora di creazione della comunicazione */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** eventuale errore verificatosi sull'invio della comunicazione (descrizione) */
    @SerialName("errDes")
    val errorDescription: String? = null,

    /** eventuale errore verificatosi sull'invio della comunicazione (ID univoco) Valore | Descrizione -      | - 0  | Nessun errore 1  | Connessione o login al server di posta fallita 4  | Lista dei destinatari vuota 5  | Inviata non a tutti i destinatari 26 | Annullata durante il processo di spedizione 27 | Timeout nel collegamento con il server */
    @SerialName("errId")
    val errorId: Int? = null,

    /** stato in cui si trova la comunicazione (descrizione) */
    @SerialName("statoComDes")
    val municipalityStateDescription: String? = null,

    /** stato in cui si trova la comunicazione (ID univoco) (1=in bozza, 2=accodata per l'invio, 3=invio in corso, 4=invio effettuato, 5=annullata) Valore | Descrizione -      | - 1 | in bozza 2 | accodata per l'invio 3 | invio in corso 4 | invio effettuato 5 | annullata */
    @SerialName("statoComId")
    val municipalityStateId: Int? = null,

    /** codice tipologia di device di comunicazione */
    @SerialName("tipoDevCod")
    val deviationTypeCode: String? = null,

    /** descrizione evento applicativo */
    @SerialName("eventoDes")
    val eventDescription: String? = null,

    /** codice evento applicativo */
    @SerialName("eventoCod")
    val eventCode: String? = null,

    /** descrizione device di comunicazione */
    @SerialName("deviceDes")
    val deviceDescription: String? = null,

    /** descrizione tipologia di comunicazione */
    @SerialName("tipoComDes")
    val municipalityTypeDescription: String? = null,

    /** data di fine validita' */
    @SerialName("dataValFin")
    val finalValidityDate: String? = null,

    /** data di inizio validita' */
    @SerialName("dataValIni")
    val initialValidityDate: String? = null,

    /** indica se la comunicazione e' da inviare anche come notifica push sulla App Mobile (0=no, 1=si) */
    @SerialName("notifPushFlg")
    val pushNotificationFlag: Int? = null,

    /** indica se la comunicazione e' da pubblicare in bacheca privata studente (0=no, 1=si) */
    @SerialName("bachecaFlg")
    val noticeboardFlag: Int? = null,

    /** indica se il testo e' in formato HTML (0=no, 1=si) */
    @SerialName("htmlFlg")
    val htmlFlag: Int? = null,

    /** titolo */
    @SerialName("testo")
    val text: String? = null,

    /** titolo */
    @SerialName("titolo")
    val title: String? = null,

    /** recapito mittente (indirizzo email o numero cellulare) */
    @SerialName("da")
    val from: String? = null,

    /** descrizione mittente */
    @SerialName("mittente")
    val sender: String? = null,

    /** codice device di comunicazione */
    @SerialName("deviceCod")
    val deviceCode: String? = null,

    /** codice tipologia di comunicazione */
    @SerialName("tipoComCod")
    val municipalityTypeCode: String? = null,

    /** ID univoco della comunicazione */
    @SerialName("comId")
    val municipalityId: Long? = null
)

@Serializable
data class Esse3Recipient(
    /** descrizione esito invio comunicazione per il destinatario */
    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    /** esito di invio comunicazione per il destinatario (SENT=inviata, FAIL=errore nell'invio, CANC=invio annullato, DRAFT=comunicazione ancora in bozza, ACTIVE=da inviare, WAIT=invio in corso) */
    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    /** data/ora in cui la comunicazione e' stata spedita al destinatario */
    @SerialName("dataSped")
    val shippingDate: String? = null,

    /** indica la comunicazione e' stata spedita al destinatario (0=no, 1=si, -1=errore, -2=spedizione annullata) */
    @SerialName("spedFlg")
    val shippingFlag: Int? = null,

    /** ID univoco dell'utente, se valorizzato come destinatario */
    @SerialName("idUser")
    val userId: Long? = null,

    /** nominativo del destinatario */
    @SerialName("nominativo")
    val fullName: String? = null,

    /** indica se il destinatario e' in CCN (copia nascosta) (0=no, 1=si) */
    @SerialName("ccnFlg")
    val blindCarbonCopyFlag: Int? = null,

    /** indica se il destinatario e' in CC (copia) (0=no, 1=si) */
    @SerialName("ccFlg")
    val carbonCopyFlag: Int? = null,

    /** indirizzo email o numero di cellulare del destinatario */
    @SerialName("recapito")
    val contact: String? = null,

    /** ID univoco di anagrafica, per la tipologia di destinatario (se origineDato vale PERSONE, DOCENTI o SOGG_EST) */
    @SerialName("idAnagrafica")
    val personalDataId: Long? = null,

    /** tipologia di destinatario (PERSONE=studente/registrato, DOCENTI=docente, SOGG_EST=soggetto esterno, EXTERNAL=recapito email/cellulare) */
    @SerialName("origineDato")
    val dataOrigin: String? = null,

    /** ID univoco di anagrafica destinatari */
    @SerialName("destId")
    val destinationId: Long? = null,

    /** ID univoco della comunicazione */
    @SerialName("comId")
    val municipalityId: Long? = null,

    /** ID univoco del destinatario di una comunicazione */
    @SerialName("comDestId")
    val destinationMunicipalityId: Long? = null
)

@Serializable
data class Esse3CommunicationWithRecipients(
    /** data/ora di invio della comunicazione */
    @SerialName("dataInvio")
    val sendingDate: String? = null,

    /** data/ora di creazione della comunicazione */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** eventuale errore verificatosi sull'invio della comunicazione (descrizione) */
    @SerialName("errDes")
    val errorDescription: String? = null,

    /** eventuale errore verificatosi sull'invio della comunicazione (ID univoco) Valore | Descrizione -      | - 0  | Nessun errore 1  | Connessione o login al server di posta fallita 4  | Lista dei destinatari vuota 5  | Inviata non a tutti i destinatari 26 | Annullata durante il processo di spedizione 27 | Timeout nel collegamento con il server */
    @SerialName("errId")
    val errorId: Int? = null,

    /** stato in cui si trova la comunicazione (descrizione) */
    @SerialName("statoComDes")
    val municipalityStateDescription: String? = null,

    /** stato in cui si trova la comunicazione (ID univoco) (1=in bozza, 2=accodata per l'invio, 3=invio in corso, 4=invio effettuato, 5=annullata) Valore | Descrizione -      | - 1 | in bozza 2 | accodata per l'invio 3 | invio in corso 4 | invio effettuato 5 | annullata */
    @SerialName("statoComId")
    val municipalityStateId: Int? = null,

    /** codice tipologia di device di comunicazione */
    @SerialName("tipoDevCod")
    val deviationTypeCode: String? = null,

    /** descrizione evento applicativo */
    @SerialName("eventoDes")
    val eventDescription: String? = null,

    /** codice evento applicativo */
    @SerialName("eventoCod")
    val eventCode: String? = null,

    /** descrizione device di comunicazione */
    @SerialName("deviceDes")
    val deviceDescription: String? = null,

    /** descrizione tipologia di comunicazione */
    @SerialName("tipoComDes")
    val municipalityTypeDescription: String? = null,

    /** data di fine validita' */
    @SerialName("dataValFin")
    val finalValidityDate: String? = null,

    /** data di inizio validita' */
    @SerialName("dataValIni")
    val initialValidityDate: String? = null,

    /** indica se la comunicazione e' da inviare anche come notifica push sulla App Mobile (0=no, 1=si) */
    @SerialName("notifPushFlg")
    val pushNotificationFlag: Int? = null,

    /** indica se la comunicazione e' da pubblicare in bacheca privata studente (0=no, 1=si) */
    @SerialName("bachecaFlg")
    val noticeboardFlag: Int? = null,

    /** indica se il testo e' in formato HTML (0=no, 1=si) */
    @SerialName("htmlFlg")
    val htmlFlag: Int? = null,

    /** titolo */
    @SerialName("testo")
    val text: String? = null,

    /** titolo */
    @SerialName("titolo")
    val title: String? = null,

    /** recapito mittente (indirizzo email o numero cellulare) */
    @SerialName("da")
    val from: String? = null,

    /** descrizione mittente */
    @SerialName("mittente")
    val sender: String? = null,

    /** codice device di comunicazione */
    @SerialName("deviceCod")
    val deviceCode: String? = null,

    /** codice tipologia di comunicazione */
    @SerialName("tipoComCod")
    val municipalityTypeCode: String? = null,

    /** ID univoco della comunicazione */
    @SerialName("comId")
    val municipalityId: Long? = null,

    @SerialName("destinatari")
    val recipients: List<Esse3Recipient> = emptyList()
)
