package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3SignatureImportData(
    @SerialName("datiFirma")
    val signatureData: List<Esse3SignatureData> = emptyList()
)

@Serializable
data class Esse3SignatureResponse(
    @SerialName("codFis")
    val fiscalCode: String,

    @SerialName("tipoFirmaId")
    val signatureTypeId: Int? = null,

    @SerialName("hsmPrefix")
    val hsmPrefix: String? = null,

    @SerialName("tipoFirmaFaId")
    val faSignatureTypeId: Int? = null,

    @SerialName("faPrefix")
    val faPrefix: String? = null,

    @SerialName("retCod")
    val returnCode: Int? = null,

    @SerialName("errMsg")
    val errorMessage: String? = null
)

@Serializable
data class Esse3ExtendedCareerPortion(
    @SerialName("persId")
    val personId: Long,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("pdsId")
    val studyPlanId: Long,

    @SerialName("matId")
    val matId: Long,

    @SerialName("matricola")
    val matricola: String,

    @SerialName("userId")
    val userId: String,

    @SerialName("linguaId")
    val languageId: Long? = null,

    @SerialName("iso6392Cod")
    val iso6392Code: String? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("staStuDes")
    val studentStatusDescription: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("ordCod")
    val orderCode: String? = null,

    @SerialName("ordDes")
    val orderDescription: String? = null,

    @SerialName("ordNumCiclo")
    val orderCycleNumber: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("aaImm1")
    val academicYearImm1: Int? = null,

    @SerialName("aaImmSu")
    val academicYearImmigrationSu: Int? = null,

    @SerialName("dataImm")
    val matriculationDate: String? = null,

    @SerialName("dataImm1")
    val enrollmentDate1: String? = null,

    @SerialName("dataImmSu")
    val enrollmentDateOn: String? = null,

    @SerialName("dataChiusura")
    val closingDate: String? = null,

    @SerialName("aaRegId")
    val academicYearRegulationId: Int? = null,

    @SerialName("codiceLettore")
    val readerCode: String? = null,

    @SerialName("titoloStudio")
    val degreeTitle: Long? = null,

    @SerialName("tipoLettore")
    val readerType: String? = null,

    @SerialName("autDatiPersonali")
    val personalDataAuthorization: String? = null,

    @SerialName("statoTasse")
    val taxesState: Long? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    @SerialName("ssd")
    val ssd: String? = null,

    @SerialName("ssdArea")
    val ssdArea: String? = null,

    @SerialName("sdrDott")
    val phdSite: String? = null,

    @SerialName("struttResponsabileCds")
    val courseOfStudyResponsibleStructure: Esse3CourseStructure? = null,

    @SerialName("tutor")
    val tutor: Esse3TutorData? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Int? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("areaCod")
    val areaCode: String? = null,

    @SerialName("areaDes")
    val areaDescription: String? = null
)

@Serializable
data class Esse3DigitalIdentities(
    @SerialName("userId")
    val userId: String? = null,

    @SerialName("spidCode")
    val spidCode: String? = null,

    @SerialName("cieCode")
    val cieCode: String? = null
)

@Serializable
data class Esse3CourseStructure(
    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("csaCod")
    val csaCode: String? = null
)

@Serializable
data class Esse3SignatureData(
    @SerialName("codFis")
    val fiscalCode: String,

    @SerialName("tipoFirmaId")
    val signatureTypeId: Int? = null,

    @SerialName("hsmPrefix")
    val hsmPrefix: String? = null,

    @SerialName("tipoFirmaFaId")
    val faSignatureTypeId: Int? = null,

    @SerialName("faPrefix")
    val faPrefix: String? = null
)

@Serializable
data class Esse3FunctionalUser(
    @SerialName("funName")
    val functionName: String? = null,

    @SerialName("funId")
    val functionId: Long? = null,

    @SerialName("funDescr")
    val functionDescription: String? = null
)

@Serializable
data class Esse3UserAlias(
    @SerialName("alias")
    val alias: String? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    @SerialName("tipologia")
    val typology: String? = null
)
