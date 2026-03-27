package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3EnrollmentClasses(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    @SerialName("lingueIso6391Cod")
    val languagesIso6391Code: String? = null,

    @SerialName("lingueDes")
    val languagesDescription: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null
)

@Serializable
data class Esse3CareerNotes(
    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("notaId")
    val noteId: Int? = null,

    @SerialName("data")
    val date: String? = null,

    @SerialName("tipo")
    val type: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("tipoContrNotaId")
    val contractNoteTypeId: Int? = null,

    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    @SerialName("templateNotaId")
    val noteTemplateId: Int? = null,

    @SerialName("webVisNotaFlg")
    val webNoteVisibleFlag: Int? = null,

    @SerialName("testoNota")
    val noteText: String? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("blocoFlg")
    val blockFlag: Int? = null,

    @SerialName("amminFlg")
    val administrativeFlag: Int? = null,

    @SerialName("carrFlg")
    val careerFlag: Int? = null,

    @SerialName("certFlg")
    val certificateFlag: Int? = null,

    @SerialName("taxFlg")
    val taxFlag: Int? = null,

    @SerialName("webFlg")
    val webFlag: Int? = null,

    @SerialName("abilReplicheFlg")
    val replicasAuthorizationFlag: Int? = null,

    @SerialName("abilNoteMassFlg")
    val massiveNoteAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3AttendanceDays(
    @SerialName("cdsFreqObbl")
    val courseOfStudyMandatoryAttendance: Int? = null,

    @SerialName("nGiorniDaRec")
    val daysToRecoverNumber: Int? = null
)

@Serializable
data class Esse3CareerMinimalDataGDPR(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("nomeAlias")
    val aliasName: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val universityEmail: String? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    @SerialName("dataImm")
    val matriculationDate: String? = null,

    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    @SerialName("sedeId")
    val siteId: Int? = null,

    @SerialName("sediDes")
    val sitesDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    @SerialName("profstuCod")
    val studentProfessionCode: String? = null,

    @SerialName("profstuDes")
    val studentProfessionDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("dataChiusura")
    val closingDate: String? = null,

    @SerialName("matId")
    val matId: Int? = null
)
