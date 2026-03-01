package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3BadgeData(
    @SerialName("bdgId")
    val badgeId: Long? = null,

    @SerialName("annFlg")
    val yearFlag: Long? = null,

    @SerialName("badgeBlbId")
    val badgeBlobId: Long? = null,

    @SerialName("frontImagePresent")
    val frontImagePresent: Int? = null,

    @SerialName("rearImagePresent")
    val rearImagePresent: Int? = null,

    @SerialName("restFlg")
    val restFlag: Long? = null,

    @SerialName("consFlg")
    val consentFlag: Long? = null,

    @SerialName("dataIni")
    val startDate: String? = null,

    @SerialName("dataStampa")
    val printDate: String? = null,

    @SerialName("dataFin")
    val endDate: String? = null,

    @SerialName("dataConsegna")
    val deliveryDate: String? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("statoNascita")
    val birthState: String? = null,

    @SerialName("statoResidenza")
    val residenceState: String? = null,

    @SerialName("codCds")
    val courseOfStudyCode: String? = null,

    @SerialName("desCds")
    val courseOfStudyDescription: String? = null,

    @SerialName("codFac")
    val facultyCode: String? = null,

    @SerialName("desFac")
    val facultyDescription: String? = null,

    @SerialName("aaIscrAnn")
    val academicYearAnnualEnrollment: Int? = null,

    @SerialName("rfid")
    val rfid: String,

    @SerialName("codUniversita")
    val universityCode: String? = null,

    @SerialName("universita")
    val university: String? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null
)
