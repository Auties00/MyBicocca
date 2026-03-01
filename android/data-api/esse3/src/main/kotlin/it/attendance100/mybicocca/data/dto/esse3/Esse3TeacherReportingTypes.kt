package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3TeacherDiaryWithDetails(
    @SerialName("diarioId")
    val diaryId: Long,

    @SerialName("aaId")
    val academicYearId: Int,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("statoDiarioCod")
    val diaryStateCode: String? = null,

    @SerialName("statoDiarioDes")
    val diaryStateDescription: String? = null,

    @SerialName("tipoGestDiarioDocCod")
    val documentDiaryManagementTypeCode: String? = null,

    @SerialName("firmaDigitaleFlg")
    val digitalSignatureFlag: Int? = null,

    @SerialName("osservazioni")
    val observations: String? = null,

    @SerialName("oreDett")
    val detailedHours: Float? = null,

    @SerialName("oreDettAnnuali")
    val annualDetailedHours: Float? = null,

    @SerialName("attivita")
    val activity: List<Esse3TeacherDiaryDetail> = emptyList(),

    @SerialName("attivitaAnnuali")
    val annualActivities: List<Esse3TeacherDiaryDetailAcademicYear> = emptyList(),

    @SerialName("note")
    val notes: List<Esse3TeacherDiaryNotes> = emptyList()
)

@Serializable
data class Esse3TeacherDiaryDetail(
    @SerialName("dettDiarioId")
    val diaryDetailId: Long,

    @SerialName("tipoAttCod")
    val activityTypeCode: String? = null,

    @SerialName("tipoAttDes")
    val activityTypeDescription: String? = null,

    @SerialName("data")
    val date: String? = null,

    @SerialName("ore")
    val hours: Int? = null,

    @SerialName("minuti")
    val minutes: Int? = null
)

@Serializable
data class Esse3TeacherRegisterDetailGroup(
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3TeacherRegister(
    @SerialName("regId")
    val registrationId: Long,

    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("statoRegCod")
    val regulationStateCode: String? = null,

    @SerialName("statoRegDes")
    val regulationStateDescription: String? = null,

    @SerialName("tipoGestRegCod")
    val regulationManagementTypeCode: String? = null,

    @SerialName("firmaDigitaleFlg")
    val digitalSignatureFlag: Int? = null,

    @SerialName("regadDataStampa")
    val teachingActivityRegistrationPrintDate: String? = null,

    @SerialName("regadFinitaDidFlg")
    val teachingActivityDidacticFinishedFlag: Int? = null,

    @SerialName("numStuL1")
    val l1StudentNumber: Int? = null,

    @SerialName("numStuL4")
    val l4StudentNumber: Int? = null,

    @SerialName("numStuMedio")
    val averageStudentNumber: Int? = null,

    @SerialName("oreRiconosciute")
    val recognizedHours: Int? = null,

    @SerialName("eccedenza")
    val excess: Int? = null,

    @SerialName("liquidatoFlg")
    val settledFlag: Int? = null,

    @SerialName("osservazioni")
    val observations: String? = null,

    @SerialName("dataUltimoTransStato")
    val lastStateTransitionDate: String? = null,

    @SerialName("identificativiCoperture")
    val coverageIdentifiers: List<Esse3TeacherRegisterCoverage> = emptyList(),

    @SerialName("totOreDid")
    val totalDidacticHours: Float? = null,

    @SerialName("totOreAltro")
    val totalOtherHours: Float? = null
)

@Serializable
data class Esse3TeacherDiaryDetailAcademicYear(
    @SerialName("dettAaDiarioId")
    val academicYearDiaryDetailId: Long,

    @SerialName("tipoAttCod")
    val activityTypeCode: String? = null,

    @SerialName("tipoAttDes")
    val activityTypeDescription: String? = null,

    @SerialName("ore")
    val hours: Int? = null,

    @SerialName("minuti")
    val minutes: Int? = null,

    @SerialName("orePrev")
    val predictedHours: Int? = null,

    @SerialName("minutiPrev")
    val predictedMinutes: Int? = null
)

@Serializable
data class Esse3TeacherRegisterDetail(
    @SerialName("dettRegId")
    val ruleDetailId: Long,

    @SerialName("tipoAttCod")
    val activityTypeCode: String? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    @SerialName("tipoAttDes")
    val activityTypeDescription: String? = null,

    @SerialName("data")
    val date: String? = null,

    @SerialName("oraInizio")
    val startTime: String? = null,

    @SerialName("oraFine")
    val endTime: String? = null,

    @SerialName("oreAccademiche")
    val academicHours: Float? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("supplenti")
    val substitutes: String? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("gruppi")
    val groups: List<Esse3TeacherRegisterDetailGroup> = emptyList()
)

@Serializable
data class Esse3TeacherRegisterCoverage(
    @SerialName("coperId")
    val coverageId: Long? = null
)

@Serializable
data class Esse3TeacherRegisterLog(
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    @SerialName("masterFlg")
    val masterFlag: Int? = null
)

@Serializable
data class Esse3TeacherDiary(
    @SerialName("diarioId")
    val diaryId: Long,

    @SerialName("aaId")
    val academicYearId: Int,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("statoDiarioCod")
    val diaryStateCode: String? = null,

    @SerialName("statoDiarioDes")
    val diaryStateDescription: String? = null,

    @SerialName("tipoGestDiarioDocCod")
    val documentDiaryManagementTypeCode: String? = null,

    @SerialName("firmaDigitaleFlg")
    val digitalSignatureFlag: Int? = null,

    @SerialName("osservazioni")
    val observations: String? = null,

    @SerialName("oreDett")
    val detailedHours: Float? = null,

    @SerialName("oreDettAnnuali")
    val annualDetailedHours: Float? = null
)

@Serializable
data class Esse3TeacherRegisterWithDetails(
    @SerialName("regId")
    val registrationId: Long,

    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("statoRegCod")
    val regulationStateCode: String? = null,

    @SerialName("statoRegDes")
    val regulationStateDescription: String? = null,

    @SerialName("tipoGestRegCod")
    val regulationManagementTypeCode: String? = null,

    @SerialName("firmaDigitaleFlg")
    val digitalSignatureFlag: Int? = null,

    @SerialName("regadDataStampa")
    val teachingActivityRegistrationPrintDate: String? = null,

    @SerialName("regadFinitaDidFlg")
    val teachingActivityDidacticFinishedFlag: Int? = null,

    @SerialName("numStuL1")
    val l1StudentNumber: Int? = null,

    @SerialName("numStuL4")
    val l4StudentNumber: Int? = null,

    @SerialName("numStuMedio")
    val averageStudentNumber: Int? = null,

    @SerialName("oreRiconosciute")
    val recognizedHours: Int? = null,

    @SerialName("eccedenza")
    val excess: Int? = null,

    @SerialName("liquidatoFlg")
    val settledFlag: Int? = null,

    @SerialName("osservazioni")
    val observations: String? = null,

    @SerialName("dataUltimoTransStato")
    val lastStateTransitionDate: String? = null,

    @SerialName("identificativiCoperture")
    val coverageIdentifiers: List<Esse3TeacherRegisterCoverage> = emptyList(),

    @SerialName("totOreDid")
    val totalDidacticHours: Float? = null,

    @SerialName("totOreAltro")
    val totalOtherHours: Float? = null,

    @SerialName("logistica")
    val logistics: List<Esse3TeacherRegisterLog> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3TeacherRegisterDetail> = emptyList()
)

@Serializable
data class Esse3TeacherDiaryNotes(
    @SerialName("notaDiarioId")
    val diaryNoteId: Long,

    @SerialName("nota")
    val note: String? = null
)
