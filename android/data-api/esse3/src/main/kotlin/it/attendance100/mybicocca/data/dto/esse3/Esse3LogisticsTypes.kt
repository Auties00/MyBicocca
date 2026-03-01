package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3EasystaffStudyPlanOrder(
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("comuneFlg")
    val municipalityFlag: Int? = null
)

@Serializable
data class Esse3EasystaffStudyPlanOrderWithDetails(
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("comuneFlg")
    val municipalityFlag: Int? = null,

    @SerialName("sedi")
    val sites: List<Esse3EasystaffLocations> = emptyList()
)

@Serializable
data class Esse3ReducedContextualizedActivityKey(
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("adId")
    val activityId: Long? = null
)

@Serializable
data class Esse3SyllabusActivity(
    @SerialName("adLogId")
    val activityLogId: Long,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    @SerialName("desAdPubblFlg")
    val teachingActivityPublicationFlag: Int,

    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    @SerialName("realFisicaFlg")
    val realPhysicalFlag: Int? = null,

    @SerialName("tipoCatalogoCod")
    val catalogTypeCode: String? = null,

    @SerialName("contenuti")
    val contents: String? = null,

    @SerialName("contenutiEng")
    val contentsEnglish: String? = null,

    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    @SerialName("obiettiviFormativiEng")
    val trainingObjectivesEnglish: String? = null,

    @SerialName("prerequisiti")
    val prerequisites: String? = null,

    @SerialName("prerequisitiEng")
    val prerequisitesEnglish: String? = null,

    @SerialName("metodiDidattici")
    val teachingMethods: String? = null,

    @SerialName("metodiDidatticiEng")
    val teachingMethodsEnglish: String? = null,

    @SerialName("modalitaVerificaApprendimento")
    val learningVerificationMethod: String? = null,

    @SerialName("modalitaVerificaApprendimentoEng")
    val learningVerificationMethodEnglish: String? = null,

    @SerialName("altreInfo")
    val otherInfo: String? = null,

    @SerialName("altreInfoEng")
    val otherInfoEnglish: String? = null,

    @SerialName("testiRiferimento")
    val referenceTexts: String? = null,

    @SerialName("testiRiferimentoEng")
    val referenceTextsEnglish: String? = null,

    @SerialName("adLogOpz")
    val optionalActivityLog: List<Esse3OptionalActivityLog> = emptyList(),

    @SerialName("syllabusOpt1")
    val syllabusOption1: String? = null,

    @SerialName("syllabusOpt1Eng")
    val syllabusOption1English: String? = null,

    @SerialName("syllabusOpt2")
    val syllabusOption2: String? = null,

    @SerialName("syllabusOpt2Eng")
    val syllabusOption2English: String? = null,

    @SerialName("syllabusOpt3")
    val syllabusOption3: String? = null,

    @SerialName("syllabusOpt3Eng")
    val syllabusOption3English: String? = null,

    @SerialName("obiettiviSvilSostenibileDes")
    val sustainableDevelopmentObjectivesDescription: String? = null,

    @SerialName("obiettiviSvilSostenibileDesEng")
    val sustainableDevelopmentObjectivesDescriptionEnglish: String? = null,

    @SerialName("obiettiviSvilSostenibileList")
    val sustainableDevelopmentObjectivesList: String? = null,

    @SerialName("obiettiviSvilSostenibile")
    val sustainableDevelopmentObjectives: List<Esse3SustainableDevelopmentGoals> = emptyList(),

    @SerialName("contenutiSpa")
    val contentsSpanish: String? = null,

    @SerialName("obiettiviFormativiSpa")
    val trainingObjectivesSpanish: String? = null,

    @SerialName("prerequisitiSpa")
    val prerequisitesSpanish: String? = null,

    @SerialName("metodiDidatticiSpa")
    val teachingMethodsSpanish: String? = null,

    @SerialName("modalitaVerificaApprendimentoSpa")
    val learningVerificationMethodSpanish: String? = null,

    @SerialName("altreInfoSpa")
    val otherInfoSpanish: String? = null,

    @SerialName("testiRiferimentoSpa")
    val referenceTextsSpanish: String? = null,

    @SerialName("syllabusOpt1Spa")
    val syllabusOption1Spanish: String? = null,

    @SerialName("syllabusOpt2Spa")
    val syllabusOption2Spanish: String? = null,

    @SerialName("syllabusOpt3Spa")
    val syllabusOption3Spanish: String? = null,

    @SerialName("obiettiviSvilSostenibileDesSpa")
    val sustainableDevelopmentObjectivesDescriptionSpanish: String? = null,

    @SerialName("contenutiFra")
    val contentsFrench: String? = null,

    @SerialName("obiettiviFormativiFra")
    val trainingObjectivesFrench: String? = null,

    @SerialName("prerequisitiFra")
    val prerequisitesFrench: String? = null,

    @SerialName("metodiDidatticiFra")
    val teachingMethodsFrench: String? = null,

    @SerialName("modalitaVerificaApprendimentoFra")
    val learningVerificationMethodFrench: String? = null,

    @SerialName("altreInfoFra")
    val otherInfoFrench: String? = null,

    @SerialName("testiRiferimentoFra")
    val referenceTextsFrench: String? = null,

    @SerialName("syllabusOpt1Fra")
    val syllabusOption1French: String? = null,

    @SerialName("syllabusOpt2Fra")
    val syllabusOption2French: String? = null,

    @SerialName("syllabusOpt3Fra")
    val syllabusOption3French: String? = null,

    @SerialName("obiettiviSvilSostenibileDesFra")
    val sustainableDevelopmentObjectivesDescriptionFrench: String? = null,

    @SerialName("contenutiDeu")
    val contentsGerman: String? = null,

    @SerialName("obiettiviFormativiDeu")
    val trainingObjectivesGerman: String? = null,

    @SerialName("prerequisitiDeu")
    val prerequisitesGerman: String? = null,

    @SerialName("metodiDidatticiDeu")
    val teachingMethodsGerman: String? = null,

    @SerialName("modalitaVerificaApprendimentoDeu")
    val learningVerificationMethodGerman: String? = null,

    @SerialName("altreInfoDeu")
    val otherInfoGerman: String? = null,

    @SerialName("testiRiferimentoDeu")
    val referenceTextsGerman: String? = null,

    @SerialName("syllabusOpt1Deu")
    val syllabusOption1German: String? = null,

    @SerialName("syllabusOpt2Deu")
    val syllabusOption2German: String? = null,

    @SerialName("syllabusOpt3Deu")
    val syllabusOption3German: String? = null,

    @SerialName("obiettiviSvilSostenibileDesDeu")
    val sustainableDevelopmentObjectivesDescriptionGerman: String? = null
)

@Serializable
data class Esse3ActivityLog(
    @SerialName("dataModLog")
    val logModificationDate: String? = null,

    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("partEffDesEng")
    val effectivePartialDescriptionEnglish: String? = null,

    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    @SerialName("domPartEffDes")
    val domicileEffectivePartialDescription: String? = null,

    @SerialName("domPartEffCod")
    val domicileEffectivePartialCode: String? = null,

    @SerialName("fatPartEffDes")
    val invoiceEffectivePartialDescription: String? = null,

    @SerialName("fatPartEffCod")
    val invoiceEffectivePartialCode: String? = null,

    @SerialName("dataFinValDid")
    val didacticEvaluationEndDate: String? = null,

    @SerialName("dataIniValDid")
    val didacticEvaluationStartDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("chiaveADFisica")
    val physicalTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey
)

@Serializable
data class Esse3Building(
    @SerialName("edificioId")
    val buildingId: Long? = null,

    @SerialName("edificioCod")
    val buildingCode: String? = null,

    @SerialName("extEdificioCod")
    val externalBuildingCode: String? = null,

    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    @SerialName("comuneId")
    val municipalityId: Long? = null,

    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("citstra")
    val foreignCity: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("urlWeb")
    val webUrl: String? = null
)

@Serializable
data class Esse3EasystaffActivity(
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsordCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("cdsordDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipCsaCod")
    val departmentCsaCode: String? = null,

    @SerialName("dipIstatCod")
    val departmentIstatCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    @SerialName("docenteTitolareId")
    val holderLecturerId: Long? = null,

    @SerialName("docenteTitolareMatricola")
    val holderLecturerMatricola: String? = null,

    @SerialName("docenteTitolareCodFis")
    val holderLecturerFiscalCode: String? = null,

    @SerialName("docenteTitolareIdAb")
    val holderLecturerAbbreviatedId: Long? = null
)

@Serializable
data class Esse3OptionalTeacherWorkload(
    @SerialName("adLogId")
    val activityLogId: Long,

    @SerialName("udLogId")
    val teachingUnitLogId: Long,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3ReducedPartitionKey,

    @SerialName("tipoCreCod")
    val creditTypeCode: String,

    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("eMail")
    val email: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("titolareFlg")
    val holderFlag: Int? = null,

    @SerialName("respDidFlg")
    val didacticResponsibleFlag: Int? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("ugovCoperId")
    val uGovCoverageId: Long? = null,

    @SerialName("tipoCoperturaCod")
    val coverageTypeCode: String? = null,

    @SerialName("tipoCoperturaDes")
    val coverageTypeDescription: String? = null
)

@Serializable
data class Esse3SustainableDevelopmentGoals(
    @SerialName("obiettiviSvilSosCod")
    val sustainableDevelopmentObjectiveCode: String? = null,

    @SerialName("obiettiviSvilSosDes")
    val sustainableDevelopmentObjectiveDescription: String? = null,

    @SerialName("obiettiviSvilSosDesEstesa")
    val extendedSustainableDevelopmentObjectiveDescription: String? = null,

    @SerialName("obiettiviSvilSosDesEng")
    val sustainableDevelopmentObjectiveDescriptionEnglish: String? = null,

    @SerialName("obiettiviSvilSosDesEngEstesa")
    val extendedSustainableDevelopmentObjectiveDescriptionEnglish: String? = null
)

@Serializable
data class Esse3SyllabusActivityPatchResult(
    @SerialName("righeAdLogPdsAggiornate")
    val updatedStudyPlanTeachingActivityLogRows: Int? = null,

    @SerialName("righeAdLogPdsDesLinAggiornate")
    val updatedStudyPlanTeachingActivityLogDescriptionLines: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

@Serializable
data class Esse3Classroom(
    @SerialName("aulaId")
    val classroomId: Long? = null,

    @SerialName("edificioId")
    val buildingId: Long? = null,

    @SerialName("aulaCod")
    val classroomCode: String,

    @SerialName("edificioCod")
    val buildingCode: String,

    @SerialName("extAulaCod")
    val externalClassroomCode: String,

    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    @SerialName("capienza")
    val capacity: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("abilCbt")
    val computerBasedTestAuthorization: String? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("dipartimenti")
    val departments: List<Esse3Department> = emptyList()
)

@Serializable
data class Esse3LogisticsWithDetails(
    @SerialName("UdLogConDettagli")
    val teachingUnitLogWithDetails: List<Esse3TeachingUnitLogWithDetails> = emptyList(),

    @SerialName("SyllabusAD")
    val syllabusTeachingActivity: List<Esse3SyllabusActivity> = emptyList(),

    @SerialName("dataModLog")
    val logModificationDate: String? = null,

    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("partEffDesEng")
    val effectivePartialDescriptionEnglish: String? = null,

    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    @SerialName("domPartEffDes")
    val domicileEffectivePartialDescription: String? = null,

    @SerialName("domPartEffCod")
    val domicileEffectivePartialCode: String? = null,

    @SerialName("fatPartEffDes")
    val invoiceEffectivePartialDescription: String? = null,

    @SerialName("fatPartEffCod")
    val invoiceEffectivePartialCode: String? = null,

    @SerialName("dataFinValDid")
    val didacticEvaluationEndDate: String? = null,

    @SerialName("dataIniValDid")
    val didacticEvaluationStartDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("chiaveADFisica")
    val physicalTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null
)

@Serializable
data class Esse3SyllabusActivityPatchField(
    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("nomeCampo")
    val fieldName: String? = null,

    @SerialName("valore")
    val value: String? = null,

    @SerialName("iso6392")
    val iso6392: String
)

@Serializable
data class Esse3EasystaffLocations(
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sedePrincipaleFlg")
    val mainSiteFlag: Int? = null
)

@Serializable
data class Esse3ReducedPartitionKey(
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null
)

@Serializable
data class Esse3EasystaffCourseOrderWithDetails(
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsordCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("cdsordDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipCsaCod")
    val departmentCsaCode: String? = null,

    @SerialName("dipIstatCod")
    val departmentIstatCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("acScelta")
    val choiceActivity: Int? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("percorsi")
    val paths: List<Esse3EasystaffStudyPlanOrderWithDetails> = emptyList()
)

@Serializable
data class Esse3SystemLogImportResult(
    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("sistLog")
    val systemLog: String? = null,

    @SerialName("ret")
    val ret: Int? = null,

    @SerialName("errMsg")
    val errorMessage: String? = null,

    @SerialName("logs")
    val logs: List<Esse3SystemLogImportMessage> = emptyList()
)

@Serializable
data class Esse3DeletedLogistics(
    @SerialName("adLogId")
    val activityLogId: Long,

    @SerialName("dataModLog")
    val logModificationDate: String? = null
)

@Serializable
data class Esse3OptionalActivityLog(
    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ReducedContextualizedActivityKey,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3ReducedPartitionKey,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("facDesEng")
    val facultyDescriptionEnglish: String? = null,

    @SerialName("areaDiscCod")
    val disciplinaryAreaCode: String? = null,

    @SerialName("areaDiscDes")
    val disciplinaryAreaDescription: String? = null,

    @SerialName("areaDiscDesEng")
    val disciplinaryAreaDescriptionEnglish: String? = null,

    @SerialName("integratoFlg")
    val integratedFlag: Int? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null
)

@Serializable
data class Esse3Department(
    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null
)

@Serializable
data class Esse3EasystaffTeachingUnit(
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("masterFlg")
    val masterFlag: Int? = null,

    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null
)

@Serializable
data class Esse3EasystaffActivityLogWithDetails(
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("partDes")
    val partialDescription: String? = null,

    @SerialName("docenti")
    val lecturers: List<Esse3EasystaffTeacher> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3EasystaffActivityWithDetails> = emptyList()
)

@Serializable
data class Esse3TeacherWorkload(
    @SerialName("adLogId")
    val activityLogId: Long,

    @SerialName("udLogId")
    val teachingUnitLogId: Long,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("tipoCreCod")
    val creditTypeCode: String,

    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    @SerialName("tipoCreDesEng")
    val creditTypeDescriptionEnglish: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("ore")
    val hours: Float? = null,

    @SerialName("frazioneCarico")
    val fractionCharge: Float? = null,

    @SerialName("valDidFlg")
    val didacticEvaluationFlag: Int? = null,

    @SerialName("oreAttSuppDid")
    val supplementaryDidacticHours: Float? = null,

    @SerialName("uGovCoperId")
    val uGovCoverageId: Long? = null,

    @SerialName("tipoCoperturaCod")
    val coverageTypeCode: String? = null,

    @SerialName("tipoCoperturaDes")
    val coverageTypeDescription: String? = null,

    @SerialName("CaricoDocentiOpz")
    val optionalTeachingLoad: List<Esse3OptionalTeacherWorkload> = emptyList()
)

@Serializable
data class Esse3LogisticsPerTeacher(
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsDesEng")
    val courseOfStudyDescriptionEnglish: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("aaOrdCod")
    val academicYearOrderCode: String? = null,

    @SerialName("aaOrdDes")
    val academicYearOrderDescription: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("adDesEng")
    val activityEnglishDescription: String? = null,

    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("udDesEng")
    val teachingUnitDescriptionEnglish: String? = null,

    @SerialName("masterFlg")
    val masterFlag: Int? = null,

    @SerialName("uGovArId")
    val uGovAreaId: Long? = null,

    @SerialName("uGovAfId")
    val uGovTeachingActivityId: Long? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

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

    @SerialName("partDes")
    val partialDescription: String? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteEMail")
    val lecturerEmail: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("frazioneCarico")
    val fractionCharge: Float? = null,

    @SerialName("ore")
    val hours: Float? = null,

    @SerialName("valDidFlg")
    val didacticEvaluationFlag: Int? = null,

    @SerialName("oreAttSupDid")
    val supplementaryDidacticActivityHours: Float? = null,

    @SerialName("tipoCoperturaCod")
    val coverageTypeCode: String? = null,

    @SerialName("uGovCoperId")
    val uGovCoverageId: Long? = null,

    @SerialName("titolareFlg")
    val holderFlag: Int? = null,

    @SerialName("respDidFlg")
    val didacticResponsibleFlag: Int? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("dataModDoc")
    val documentModificationDate: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("claMCod")
    val classMCode: String? = null
)

@Serializable
data class Esse3EasystaffActivityLog(
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("partDes")
    val partialDescription: String? = null
)

@Serializable
data class Esse3PartitionKey(
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String,

    @SerialName("fatPartDes")
    val invoicePartialDescription: String? = null,

    @SerialName("fatPartDesEng")
    val invoicePartialDescriptionEnglish: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("domPartDesEng")
    val domicilePartialDescriptionEnglish: String? = null,

    @SerialName("partCod")
    val partialCode: String,

    @SerialName("partDes")
    val partialDescription: String? = null,

    @SerialName("partDesEng")
    val partialDescriptionEnglish: String? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null
)

@Serializable
data class Esse3EasystaffTeacher(
    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null
)

@Serializable
data class Esse3SyllabusActivityPatch(
    @SerialName("aaOffId")
    val academicYearOfferId: Long,

    @SerialName("cdsCod")
    val courseOfStudyCode: String,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("pdsCod")
    val studyPlanCode: String,

    @SerialName("adCod")
    val activityCode: String,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("obiettiviSvilSostenibileList")
    val sustainableDevelopmentObjectivesList: String? = null,

    @SerialName("rimuoviObiettiviSvilSostenibileList")
    val removeSustainableDevelopmentObjectivesList: Int? = null,

    @SerialName("desAdPubblFlg")
    val teachingActivityPublicationFlag: Int? = null,

    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    @SerialName("campiSyllabus")
    val syllabusFields: List<Esse3SyllabusActivityPatchField> = emptyList()
)

@Serializable
data class Esse3TeachingUnitLogWithDetails(
    @SerialName("CaricoDocenti")
    val teachingLoad: List<Esse3TeacherWorkload> = emptyList(),

    @SerialName("SyllabusUD")
    val syllabusTeachingUnit: List<Esse3SyllabusTeachingUnit> = emptyList(),

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null,

    @SerialName("chiaveUDMaster")
    val masterTeachingUnitKey: Esse3ContextualizedTeachingUnitKey? = null,

    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

    @SerialName("uGovArId")
    val uGovAreaId: Long? = null
)

@Serializable
data class Esse3SystemLogImportMessage(
    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("level")
    val level: String? = null,

    @SerialName("msg")
    val message: String? = null
)

@Serializable
data class Esse3EasystaffCourseOrder(
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsordCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("cdsordDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipCsaCod")
    val departmentCsaCode: String? = null,

    @SerialName("dipIstatCod")
    val departmentIstatCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("acScelta")
    val choiceActivity: Int? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null
)

@Serializable
data class Esse3EasystaffActivityWithDetails(
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsordCod")
    val courseOfStudyOrderCode: String? = null,

    @SerialName("cdsordDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipCsaCod")
    val departmentCsaCode: String? = null,

    @SerialName("dipIstatCod")
    val departmentIstatCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("fisicaFlg")
    val physicalFlag: Int? = null,

    @SerialName("docenteTitolareId")
    val holderLecturerId: Long? = null,

    @SerialName("docenteTitolareMatricola")
    val holderLecturerMatricola: String? = null,

    @SerialName("docenteTitolareCodFis")
    val holderLecturerFiscalCode: String? = null,

    @SerialName("docenteTitolareIdAb")
    val holderLecturerAbbreviatedId: Long? = null,

    @SerialName("moduli")
    val modules: List<Esse3EasystaffTeachingUnit> = emptyList()
)

@Serializable
data class Esse3SyllabusTeachingUnit(
    @SerialName("adLogId")
    val activityLogId: Long,

    @SerialName("udLogId")
    val teachingUnitLogId: Long,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("chiaveUDContestualizzata")
    val contextualizedTeachingUnitKey: Esse3ContextualizedTeachingUnitKey,

    @SerialName("desUdPubblFlg")
    val teachingUnitPublicationFlag: Int,

    @SerialName("masterFlg")
    val masterFlag: Int? = null,

    @SerialName("realMasterFlg")
    val realMasterFlag: Int? = null,

    @SerialName("contenuti")
    val contents: String? = null,

    @SerialName("contenutiEng")
    val contentsEnglish: String? = null,

    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    @SerialName("obiettiviFormativiEng")
    val trainingObjectivesEnglish: String? = null,

    @SerialName("prerequisiti")
    val prerequisites: String? = null,

    @SerialName("prerequisitiEng")
    val prerequisitesEnglish: String? = null,

    @SerialName("testiRiferimento")
    val referenceTexts: String? = null,

    @SerialName("testiRiferimentoEng")
    val referenceTextsEnglish: String? = null,

    @SerialName("uGovAfId")
    val uGovTeachingActivityId: Long? = null,

    @SerialName("uGovArId")
    val uGovAreaId: Long? = null,

    @SerialName("obiettiviSvilSostenibileDes")
    val sustainableDevelopmentObjectivesDescription: String? = null,

    @SerialName("obiettiviSvilSostenibileDesEng")
    val sustainableDevelopmentObjectivesDescriptionEnglish: String? = null,

    @SerialName("obiettiviSvilSostenibileList")
    val sustainableDevelopmentObjectivesList: String? = null,

    @SerialName("obiettiviSvilSostenibile")
    val sustainableDevelopmentObjectives: List<Esse3SustainableDevelopmentGoals> = emptyList(),

    @SerialName("contenutiSpa")
    val contentsSpanish: String? = null,

    @SerialName("obiettiviFormativiSpa")
    val trainingObjectivesSpanish: String? = null,

    @SerialName("prerequisitiSpa")
    val prerequisitesSpanish: String? = null,

    @SerialName("testiRiferimentoSpa")
    val referenceTextsSpanish: String? = null,

    @SerialName("obiettiviSvilSostenibileDesSpa")
    val sustainableDevelopmentObjectivesDescriptionSpanish: String? = null,

    @SerialName("contenutiFra")
    val contentsFrench: String? = null,

    @SerialName("obiettiviFormativiFra")
    val trainingObjectivesFrench: String? = null,

    @SerialName("prerequisitiFra")
    val prerequisitesFrench: String? = null,

    @SerialName("testiRiferimentoFra")
    val referenceTextsFrench: String? = null,

    @SerialName("obiettiviSvilSostenibileDesFra")
    val sustainableDevelopmentObjectivesDescriptionFrench: String? = null,

    @SerialName("contenutiDeu")
    val contentsGerman: String? = null,

    @SerialName("obiettiviFormativiDeu")
    val trainingObjectivesGerman: String? = null,

    @SerialName("prerequisitiDeu")
    val prerequisitesGerman: String? = null,

    @SerialName("testiRiferimentoDeu")
    val referenceTextsGerman: String? = null,

    @SerialName("obiettiviSvilSostenibileDesDeu")
    val sustainableDevelopmentObjectivesDescriptionGerman: String? = null
)

@Serializable
data class Esse3CoverageDeletable(
    @SerialName("retCode")
    val returnCode: Int? = null,

    @SerialName("msg")
    val message: String? = null
)

@Serializable
data class Esse3ActivityLogWithSyllabus(
    @SerialName("SyllabusAD")
    val syllabusTeachingActivity: List<Esse3SyllabusActivity> = emptyList(),

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null,

    @SerialName("chiaveADFisica")
    val physicalTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("dataIniValDid")
    val didacticEvaluationStartDate: String? = null,

    @SerialName("dataFinValDid")
    val didacticEvaluationEndDate: String? = null,

    @SerialName("fatPartEffCod")
    val invoiceEffectivePartialCode: String? = null,

    @SerialName("fatPartEffDes")
    val invoiceEffectivePartialDescription: String? = null,

    @SerialName("domPartEffCod")
    val domicileEffectivePartialCode: String? = null,

    @SerialName("domPartEffDes")
    val domicileEffectivePartialDescription: String? = null,

    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    @SerialName("partEffDesEng")
    val effectivePartialDescriptionEnglish: String? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("tipoDidDesEng")
    val didacticTypeDescriptionEnglish: String? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sedeDesEng")
    val siteDescriptionEnglish: String? = null,

    @SerialName("dataModLog")
    val logModificationDate: String? = null
)
