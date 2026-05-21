package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3BadgeData(
    /** id del badge */
    @SerialName("bdgId")
    val badgeId: Long? = null,

    /** flag che indica se il badge è stato restituito */
    @SerialName("annFlg")
    val yearFlag: Long? = null,

    /** id del blob con le immagini del badge */
    @SerialName("badgeBlbId")
    val badgeBlobId: Long? = null,

    /** flag che indica se è presente un blob con l'immagine anteriore */
    @SerialName("frontImagePresent")
    val frontImagePresent: Int? = null,

    /** flag che indica se è presente un blob con l'immagine posteriore */
    @SerialName("rearImagePresent")
    val rearImagePresent: Int? = null,

    /** flag che indica se il badge è annullato */
    @SerialName("restFlg")
    val restFlag: Long? = null,

    /** flag che indica se il badge è stato consegnato */
    @SerialName("consFlg")
    val consentFlag: Long? = null,

    /** data e ora di creazione del badge (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIni")
    val startDate: String? = null,

    /** data e ora di stampa del badge (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataStampa")
    val printDate: String? = null,

    /** data e ora di annullamento del badge (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataFin")
    val endDate: String? = null,

    /** data e ora di consegna del badge (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataConsegna")
    val deliveryDate: String? = null,

    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** id della carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id univoco della matricola */
    @SerialName("matId")
    val matId: Long? = null,

    /** matricola dello studente, è il numero assegnato dalle segreterie allo studente per identificarlo nel sistema, una matricola potrebbe non identificare univocamente un libretto collegato ad un tratto di carriera */
    @SerialName("matricola")
    val matricola: String? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** codice fiscale della persona */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** denominazione della nazione di nascita */
    @SerialName("statoNascita")
    val birthState: String? = null,

    /** denominazione della nazione di residenza */
    @SerialName("statoResidenza")
    val residenceState: String? = null,

    /** codice del corso di studio di iscrizione dello studente */
    @SerialName("codCds")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di iscrizione dello studente */
    @SerialName("desCds")
    val courseOfStudyDescription: String? = null,

    /** Codice del dipartimento/facoltà */
    @SerialName("codFac")
    val facultyCode: String? = null,

    /** Descrizione del dipartimento/facoltà */
    @SerialName("desFac")
    val facultyDescription: String? = null,

    /** anno di ordinamento del corso di iscrizione dello studente */
    @SerialName("aaIscrAnn")
    val academicYearAnnualEnrollment: Int? = null,

    /** rfid associato al badge */
    @SerialName("rfid")
    val rfid: String = "",

    /** Codice dell'università */
    @SerialName("codUniversita")
    val universityCode: String? = null,

    /** Descrizione dell'università */
    @SerialName("universita")
    val university: String? = null,

    /** Descrizione dell'università */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** Descrizione dell'università */
    @SerialName("staMatCod")
    val matStatusCode: String? = null
)
