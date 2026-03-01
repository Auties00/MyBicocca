package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3SupervisorsStatisticsSummary(
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("totals")
    val totals: List<Esse3SupervisorsStatistics> = emptyList()
)

@Serializable
data class Esse3TeachingDomainInsert(
    @SerialName("domanda")
    val application: Esse3ApplicationDataInsert? = null,

    @SerialName("tesi")
    val thesis: Esse3ThesisDataInsert? = null
)

@Serializable
data class Esse3ThesisConsultationMode(
    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Long? = null,

    @SerialName("modAutAccTesiCod")
    val thesisAccessAuthorizationModeCode: String? = null,

    @SerialName("giorniEmbargo")
    val embargoDays: Int? = null,

    @SerialName("autAccessoTesi")
    val thesisAccessAuthorization: String? = null
)

@Serializable
data class Esse3SupervisorTypeRegulation(
    @SerialName("regCtId")
    val committeeRegulationId: Long? = null,

    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    @SerialName("tipoRelDes")
    val relationTypeDescription: String? = null,

    @SerialName("numMin")
    val minNumber: Int? = null,

    @SerialName("numMax")
    val maxNumber: Int? = null,

    @SerialName("docenti")
    val lecturers: Int? = null,

    @SerialName("soggEst")
    val externalSubject: Int? = null
)

@Serializable
data class Esse3TeachingDomainCancellation(
    @SerialName("domCtId")
    val domicileCommitteeId: Long,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("checkScadenze")
    val deadlinesCheck: Boolean,

    @SerialName("statoDomanda")
    val applicationState: String? = null
)

@Serializable
data class Esse3CheckTeachingDomainCancellation(
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    @SerialName("titAccAmmId")
    val adminTitleAccessId: Long? = null
)

@Serializable
data class Esse3ThesisAttachmentInsertMetadata(
    @SerialName("chiaviCollegamento")
    val linkKeys: List<Esse3LinkKey> = emptyList(),

    @SerialName("proprietaAggiuntive")
    val additionalProperties: List<Esse3AdditionalProperty> = emptyList(),

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("descrizione")
    val description: String? = null,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null
)

@Serializable
data class Esse3SupervisorsInsert(
    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("notaTesi")
    val thesisNote: String? = null,

    @SerialName("ricEccellenzaFlg")
    val excellenceRequestFlag: Long,

    @SerialName("dataRicEccellenza")
    val excellenceRequestDate: String? = null,

    @SerialName("puntiProp")
    val proposedPoints: Int? = null,

    @SerialName("motPuntiProp")
    val proposedPointsReason: String? = null
)

@Serializable
data class Esse3SupervisorsStatistics(
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    @SerialName("numTesi")
    val thesisNumber: Long? = null
)

@Serializable
data class Esse3TeachingCommission(
    @SerialName("docSedCtId")
    val committeeSessionDocumentId: Long? = null,

    @SerialName("commCtId")
    val committeeCallId: Long? = null,

    @SerialName("appellativo")
    val title: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("ruoloCod")
    val roleCode: String? = null,

    @SerialName("ruoloDes")
    val roleDescription: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null
)

@Serializable
data class Esse3SupervisorsDataInsert(
    @SerialName("preTesiId")
    val preThesisId: Long? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("notaTesi")
    val thesisNote: String? = null
)

@Serializable
data class Esse3ThesisAttachment(
    @SerialName("allegatoTypeCod")
    val attachmentCategoryCode: String? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("allegatoDes")
    val attachmentDescription: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("estensione")
    val extension: String? = null,

    @SerialName("defFlg")
    val definitionFlag: Int? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("statoAllTesiCod")
    val thesisAttachmentStateCode: String? = null,

    @SerialName("motivazione")
    val reason: String? = null,

    @SerialName("dataAppRif")
    val referenceCallDate: String? = null,

    @SerialName("statoAllTesiDes")
    val thesisAttachmentStateDescription: String? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    @SerialName("tipologiaDes")
    val typologyDescription: String? = null,

    @SerialName("autUsoAll")
    val attachmentUsageAuthorization: Int? = null,

    @SerialName("dataAutUsoAll")
    val attachmentUsageAuthorizationDate: String? = null,

    @SerialName("indxAntiplagio")
    val antiplagiarismIndex: String? = null,

    @SerialName("linkAntiplagio")
    val antiplagiarismLink: String? = null,

    @SerialName("notaAntiplagio")
    val antiplagiarismNote: String? = null,

    @SerialName("modConsTesi")
    val thesisDiscussionMode: String? = null,

    @SerialName("autAccessoTesi")
    val thesisAccessAuthorization: String? = null,

    @SerialName("giorniEmbargo")
    val embargoDays: Int? = null,

    @SerialName("dataFineEmbargo")
    val embargoEndDate: String? = null
)

@Serializable
data class Esse3ThesisIntoTeachingDomainInsert(
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("tesi")
    val thesis: Esse3ThesisDataInsert? = null
)

@Serializable
data class Esse3ThesisDiscussionModeCodeInsert(
    @SerialName("tesiId")
    val thesisId: Long,

    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null
)

@Serializable
data class Esse3ExamSessionLocation(
    @SerialName("sesCtId")
    val committeeSessionId: Long? = null,

    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    @SerialName("sedCtPrg")
    val committeeSessionProgram: Long? = null,

    @SerialName("aulaId")
    val classroomId: Long? = null,

    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    @SerialName("commCtId")
    val committeeCallId: Long? = null,

    @SerialName("sedutaCod")
    val sessionCode: String? = null,

    @SerialName("orarioSeduta")
    val sessionSchedule: String? = null,

    @SerialName("dataSeduta")
    val sessionDate: String? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("desAppello")
    val examCallDescription: String? = null,

    @SerialName("desSessione")
    val sessionDescription: String? = null,

    @SerialName("dipartimento")
    val department: String? = null,

    @SerialName("desDipartimento")
    val departmentDescription: String? = null,

    @SerialName("membriCommissione")
    val committeeMembers: List<Esse3TeachingCommission> = emptyList()
)

@Serializable
data class Esse3ThesisSummary(
    @SerialName("tesiId")
    val thesisId: Long? = null,

    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("appCtId")
    val callCommitteeId: Int? = null,

    @SerialName("titoloTesiIta")
    val thesisTitleItalian: String? = null,

    @SerialName("abstractTesiIta")
    val thesisAbstractItalian: String? = null,

    @SerialName("titoloTesiEng")
    val thesisTitleEnglish: String? = null,

    @SerialName("abstractTesiEng")
    val thesisAbstractEnglish: String? = null,

    @SerialName("TipoTesiDes")
    val thesisTypeDescription: String? = null,

    @SerialName("AdGenDes")
    val genericActivityDescription: String? = null,

    @SerialName("AdTesiDes")
    val thesisActivityDescription: String? = null,

    @SerialName("TesiDiGruppo")
    val groupThesis: Int? = null,

    @SerialName("dataDepositoTesi")
    val thesisDepositDate: String? = null,

    @SerialName("StatoTesi")
    val thesisStatus: String? = null,

    @SerialName("DomCtId")
    val teachingDomainId: Int? = null,

    @SerialName("linguaTesi")
    val thesisLanguage: String? = null,

    @SerialName("linguaDiscussioneTesi")
    val thesisDiscussionLanguage: String? = null,

    @SerialName("linguaId")
    val languageId: Int? = null,

    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null,

    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    @SerialName("allegatiTesi")
    val thesisAttachments: List<Esse3ThesisAttachment> = emptyList(),

    @SerialName("relatori")
    val supervisors: List<Esse3ThesisSupervisors> = emptyList()
)

@Serializable
data class Esse3SupervisorTeacher(
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("badge")
    val badge: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("dipartimentoCod")
    val departmentCode: String? = null,

    @SerialName("dipartimentoDes")
    val departmentDescription: String? = null,

    @SerialName("facoltaCod")
    val facultyCode: String? = null,

    @SerialName("facoltaDes")
    val facultyDescription: String? = null,

    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    @SerialName("linguaId")
    val languageId: Int? = null
)

@Serializable
data class Esse3TeachingDomainSummary(
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("matId")
    val matId: Int? = null,

    @SerialName("dataDomandaCt")
    val committeeApplicationDate: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("stato")
    val state: String? = null,

    @SerialName("sessioneCtDes")
    val committeeSessionDescription: String? = null,

    @SerialName("dataInizioSessioneCt")
    val committeeSessionStartDate: String? = null,

    @SerialName("dataFineSessioneCt")
    val committeeSessionEndDate: String? = null,

    @SerialName("appelloCtDes")
    val callCommitteeDescription: String? = null,

    @SerialName("dataAppelloCt")
    val committeeCallDate: String? = null,

    @SerialName("dataSedutaCt")
    val committeeSessionDate: String? = null,

    @SerialName("orarioSedutaCt")
    val committeeSessionSchedule: String? = null,

    @SerialName("EdificioDes")
    val buildingDescription: String? = null,

    @SerialName("AulaDes")
    val classroomDescription: String? = null,

    @SerialName("tesiId")
    val thesisId: Int? = null,

    @SerialName("titoloTesi")
    val thesisTitle: String? = null,

    @SerialName("abstractTesi")
    val thesisAbstract: String? = null,

    @SerialName("dataDepositoTesi")
    val thesisDepositDate: String? = null,

    @SerialName("TipoTesiDes")
    val thesisTypeDescription: String? = null,

    @SerialName("AdGenDes")
    val genericActivityDescription: String? = null,

    @SerialName("AdTesiDes")
    val thesisActivityDescription: String? = null,

    @SerialName("TesiDiGruppo")
    val groupThesis: Int? = null,

    @SerialName("KeywordAlma1")
    val almaKeyword1: String? = null,

    @SerialName("KeywordAlma2")
    val almaKeyword2: String? = null,

    @SerialName("KeywordAlma3")
    val almaKeyword3: String? = null,

    @SerialName("KeywordAlma4")
    val almaKeyword4: String? = null,

    @SerialName("KeywordAlma5")
    val almaKeyword5: String? = null,

    @SerialName("StatoTesiCod")
    val thesisStatusCode: String? = null,

    @SerialName("dataCt")
    val committeeDate: String? = null,

    @SerialName("tesiElettronica")
    val electronicThesis: Int? = null,

    @SerialName("numeroMavCtit")
    val titleMavNumber: String? = null,

    @SerialName("dataPagamentoCtit")
    val titlePaymentDate: String? = null,

    @SerialName("numeroMavPerga")
    val parchmentMavNumber: String? = null,

    @SerialName("dataPagamentoPerga")
    val parchmentPaymentDate: String? = null,

    @SerialName("linguaId")
    val languageId: Int? = null,

    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null,

    @SerialName("votoFinale")
    val finalGrade: Long? = null,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Long? = null,

    @SerialName("puntiAgg")
    val additionalPoints: Double? = null,

    @SerialName("tipiGiudProFinDes")
    val finalProJudgmentTypesDescription: String? = null,

    @SerialName("menzioneFlg")
    val mentionFlag: Long? = null,

    @SerialName("encomioFlg")
    val commendationFlag: Long? = null,

    @SerialName("cfuFinali")
    val finalCredits: Double? = null
)

@Serializable
data class Esse3ThesisTypes(
    @SerialName("tipoTesiCod")
    val thesisTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("miurTipoTesiCod")
    val miurThesisTypeCode: String? = null,

    @SerialName("regCtId")
    val committeeRegulationId: Int? = null
)

@Serializable
data class Esse3ThesisSupervisorsDepartments(
    @SerialName("dipartimentoCod")
    val departmentCode: String? = null,

    @SerialName("dipartimentoDes")
    val departmentDescription: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("relatori")
    val supervisors: List<Esse3SupervisorsStatisticsSummary> = emptyList()
)

@Serializable
data class Esse3ApplicationDataInsert(
    @SerialName("stuId")
    val studentId: Int,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("dataDomCt")
    val committeeApplicationDate: String? = null,

    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    @SerialName("sesCtId")
    val committeeSessionId: Long? = null,

    @SerialName("quesAlma")
    val almaQuestion: String? = null,

    @SerialName("tipoDomCt")
    val committeeApplicationType: String? = null,

    @SerialName("stage")
    val stage: String? = null,

    @SerialName("stageEng")
    val stageEnglish: String? = null,

    @SerialName("projectWork")
    val projectWork: String? = null,

    @SerialName("projectWorkEng")
    val projectWorkEnglish: String? = null,

    @SerialName("cdsDicId")
    val courseOfStudyDictionaryId: Long? = null,

    @SerialName("aaOrdDicId")
    val academicYearOrderDictionaryId: Long? = null,

    @SerialName("pdsDicId")
    val studyPlanDictionaryId: Long? = null,

    @SerialName("prosSpecFlg")
    val specialNextFlag: Int? = null,

    @SerialName("regAzionistuId")
    val shareholderRegistrationId: Long? = null,

    @SerialName("cdsProsId")
    val courseOfStudyNextId: Long? = null,

    @SerialName("aaOrdProsId")
    val academicYearOrderProspectId: Long? = null,

    @SerialName("pdsProsId")
    val studyPlanNextId: Long? = null,

    @SerialName("linguaQuadId")
    val languageQuadId: Long? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("numTel")
    val phoneNumber: String? = null,

    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("mesiProrogaCt")
    val committeeExtensionMonths: Int? = null,

    @SerialName("regCtId")
    val committeeRegulationId: Long? = null,

    @SerialName("consVulcanoFlg")
    val consentVulcanoFlag: Int? = null,

    @SerialName("esaSostFlg")
    val graduationSubstitutionFlag: Int? = null,

    @SerialName("autWebFlg")
    val webAuthorizationFlag: Int? = null,

    @SerialName("tipoEsactCod")
    val exactGraduationTypeCode: String? = null,

    @SerialName("tesiDiGruppoFlg")
    val groupThesisFlag: Int? = null,

    @SerialName("confEsplicitaFlg")
    val explicitConfirmationFlag: Int? = null,

    @SerialName("ricPergaFlg")
    val parchmentRequestFlag: Int? = null,

    @SerialName("motCtrlRifreg")
    val regularReferenceControlReason: Int? = null,

    @SerialName("regAlmaFlg")
    val almaRegistrationFlag: Int? = null,

    @SerialName("pergaNumRaggr")
    val parchmentGroupNumber: Long? = null
)

@Serializable
data class Esse3ThesisDataInsert(
    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("regCtId")
    val committeeRegulationId: Long? = null,

    @SerialName("tipoTesiCod")
    val thesisTypeCode: String? = null,

    @SerialName("dataDepositoTesi")
    val thesisDepositDate: String? = null,

    @SerialName("titoloTesiIta")
    val thesisTitleItalian: String? = null,

    @SerialName("abstractTesiIta")
    val thesisAbstractItalian: String? = null,

    @SerialName("titoloTesiEng")
    val thesisTitleEnglish: String? = null,

    @SerialName("abstractTesiEng")
    val thesisAbstractEnglish: String? = null,

    @SerialName("TipoTesiDes")
    val thesisTypeDescription: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("settDiscCod")
    val disciplinarySectorCode: String? = null,

    @SerialName("discCod")
    val disciplineCode: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("udAdId")
    val teachingUnitTeachingActivityId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Long? = null,

    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("adTesiDes")
    val thesisActivityDescription: String? = null,

    @SerialName("linguaTesi")
    val thesisLanguage: Long? = null,

    @SerialName("linguaDiscussioneTesi")
    val thesisDiscussionLanguage: Long? = null,

    @SerialName("keywordAlma1")
    val almaKeyword1: String? = null,

    @SerialName("keywordAlma2")
    val almaKeyword2: String? = null,

    @SerialName("keywordAlma3")
    val almaKeyword3: String? = null,

    @SerialName("keywordAlma4")
    val almaKeyword4: String? = null,

    @SerialName("keywordAlma5")
    val almaKeyword5: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("tesiElettrFlg")
    val electronicThesisFlag: Int? = null,

    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    @SerialName("modRiprTesiCod")
    val thesisRestartModeCode: String? = null,

    @SerialName("tesinaFlg")
    val shortThesisFlag: Int? = null,

    @SerialName("esteroFlg")
    val foreignFlag: Int? = null,

    @SerialName("tipoRecapitoCod")
    val contactTypeCode: String? = null,

    @SerialName("altroRecapitoTesi")
    val otherContactThesis: String? = null,

    @SerialName("tipoRecapitoPergaCod")
    val parchmentDeliveryContactTypeCode: String? = null,

    @SerialName("altroRecapitoPerga")
    val otherContactParchment: String? = null,

    @SerialName("dataMinSesCt")
    val minimumCommitteeSessionDate: String? = null,

    @SerialName("regAzionistuId")
    val shareholderRegistrationId: Long? = null,

    @SerialName("tesiId")
    val thesisId: Long? = null,

    @SerialName("keywordAlmaEng1")
    val almaKeywordEnglish1: String? = null,

    @SerialName("keywordAlmaEng2")
    val almaKeywordEnglish2: String? = null,

    @SerialName("keywordAlmaEng3")
    val almaKeywordEnglish3: String? = null,

    @SerialName("keywordAlmaEng4")
    val almaKeywordEnglish4: String? = null,

    @SerialName("keywordAlmaEng5")
    val almaKeywordEnglish5: String? = null,

    @SerialName("confTesiDefFlg")
    val thesisDefenseConfirmationFlag: Int? = null,

    @SerialName("complTesi")
    val thesisCompletion: Int? = null,

    @SerialName("mesiEmbargo")
    val embargoMonths: Int? = null,

    @SerialName("tipoAutVerifTesiCod")
    val thesisVerificationAuthorizationTypeCode: String? = null,

    @SerialName("recPergaNazioneId")
    val parchmentDeliveryNationId: Long? = null,

    @SerialName("recPergaComuneId")
    val parchmentDeliveryMunicipalityId: Long? = null,

    @SerialName("recPergaVia")
    val parchmentDeliveryStreet: String? = null,

    @SerialName("recPergaNumCiv")
    val parchmentDeliveryStreetNumber: String? = null,

    @SerialName("recPergaCap")
    val parchmentDeliveryPostalCode: String? = null,

    @SerialName("recPergaCitstra")
    val parchmentDeliveryForeignCity: String? = null,

    @SerialName("TesiDiGruppo")
    val groupThesis: Int? = null,

    @SerialName("relatori")
    val supervisors: List<Esse3SupervisorsDataInsert> = emptyList()
)

@Serializable
data class Esse3ThesisSupervisorsTeachingDomain(
    @SerialName("tesiId")
    val thesisId: Long? = null,

    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    @SerialName("tipoRelDes")
    val relationTypeDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("notaTesi")
    val thesisNote: String? = null,

    @SerialName("ricEccellenzaFlg")
    val excellenceRequestFlag: Long? = null,

    @SerialName("dataRicEccellenza")
    val excellenceRequestDate: String? = null,

    @SerialName("puntiProp")
    val proposedPoints: Int? = null,

    @SerialName("motPuntiProp")
    val proposedPointsReason: String? = null
)

@Serializable
data class Esse3ThesisTeachingDomainSummary(
    @SerialName("tesiId")
    val thesisId: Long? = null,

    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("appCtId")
    val callCommitteeId: Int? = null,

    @SerialName("titoloTesiIta")
    val thesisTitleItalian: String? = null,

    @SerialName("abstractTesiIta")
    val thesisAbstractItalian: String? = null,

    @SerialName("titoloTesiEng")
    val thesisTitleEnglish: String? = null,

    @SerialName("abstractTesiEng")
    val thesisAbstractEnglish: String? = null,

    @SerialName("TipoTesiDes")
    val thesisTypeDescription: String? = null,

    @SerialName("AdGenDes")
    val genericActivityDescription: String? = null,

    @SerialName("AdTesiDes")
    val thesisActivityDescription: String? = null,

    @SerialName("TesiDiGruppo")
    val groupThesis: Int? = null,

    @SerialName("dataDepositoTesi")
    val thesisDepositDate: String? = null,

    @SerialName("StatoTesi")
    val thesisStatus: String? = null,

    @SerialName("DomCtId")
    val teachingDomainId: Int? = null,

    @SerialName("linguaTesi")
    val thesisLanguage: String? = null,

    @SerialName("linguaDiscussioneTesi")
    val thesisDiscussionLanguage: String? = null,

    @SerialName("linguaId")
    val languageId: Int? = null,

    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null,

    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    @SerialName("allegatiTesi")
    val thesisAttachments: List<Esse3ThesisAttachment> = emptyList(),

    @SerialName("relatori")
    val supervisors: List<Esse3ThesisSupervisorsTeachingDomain> = emptyList()
)

@Serializable
data class Esse3AntiplagiarismDataInsert(
    @SerialName("allegatoId")
    val attachmentId: Long,

    @SerialName("indxAntiplagio")
    val antiplagiarismIndex: String? = null,

    @SerialName("linkAntiplagio")
    val antiplagiarismLink: String? = null,

    @SerialName("notaAntiplagio")
    val antiplagiarismNote: String? = null,

    @SerialName("approvazioneAllegFlg")
    val attachmentApprovalFlag: Long? = null
)

@Serializable
data class Esse3ExamSessionDeadlines(
    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    @SerialName("tipoScadenzaCod")
    val expirationTypeCode: String? = null,

    @SerialName("tipoScadenzaDes")
    val expirationTypeDescription: String? = null,

    @SerialName("dataScaIni")
    val initialDeadline: String? = null,

    @SerialName("dataScaFin")
    val finalDeadline: String? = null,

    @SerialName("linguaId")
    val languageId: Int? = null,

    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null
)

@Serializable
data class Esse3ThesisReplica(
    @SerialName("tesiId")
    val thesisId: Long? = null,

    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    @SerialName("modConsTesiDes")
    val thesisDiscussionModeDescription: String? = null,

    @SerialName("autAccessoTesi")
    val thesisAccessAuthorization: String? = null,

    @SerialName("giorniEmbargo")
    val embargoDays: Int? = null,

    @SerialName("dataFineEmbargo")
    val embargoEndDate: String? = null,

    @SerialName("idDocumento")
    val documentId: String? = null,

    @SerialName("dataDocumento")
    val documentDate: String? = null,

    @SerialName("titulusId")
    val titulusId: String? = null,

    @SerialName("irisId")
    val irisId: String? = null,

    @SerialName("unitesiId")
    val integratedTeachingUnitId: Long? = null
)

@Serializable
data class Esse3ThesisSupervisors(
    @SerialName("tesiId")
    val thesisId: Long? = null,

    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    @SerialName("tipoRelDes")
    val relationTypeDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("notaTesi")
    val thesisNote: String? = null,

    @SerialName("ricEccellenzaFlg")
    val excellenceRequestFlag: Long? = null,

    @SerialName("dataRicEccellenza")
    val excellenceRequestDate: String? = null,

    @SerialName("puntiProp")
    val proposedPoints: Int? = null,

    @SerialName("motPuntiProp")
    val proposedPointsReason: String? = null
)

@Serializable
data class Esse3ImportSupervisorsResponse(
    @SerialName("esitoElaborazione")
    val processingOutcome: String? = null,

    @SerialName("elencoErrori")
    val errorList: List<kotlinx.serialization.json.JsonObject> = emptyList()
)
