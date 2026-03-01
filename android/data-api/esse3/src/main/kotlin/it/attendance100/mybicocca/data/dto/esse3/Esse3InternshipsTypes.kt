package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3InternshipApplicationAttachments(
    @SerialName("domTiroId")
    val domicileInternshipId: Long? = null,

    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("dimensione")
    val size: Long? = null,

    @SerialName("titolo")
    val title: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("filename")
    val fileName: String? = null,

    @SerialName("grpId")
    val groupId: Long? = null,

    @SerialName("userMod")
    val modificationUser: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("userInsId")
    val insertionUserId: String? = null,

    @SerialName("isOwner")
    val isOwner: Int? = null,

    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null
)

@Serializable
data class Esse3InternshipApplicationDetail(
    @SerialName("domTiroId")
    val domicileInternshipId: Long? = null,

    @SerialName("domTiroPrg")
    val domicileInternshipProgram: Int? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("sdrId")
    val siteId: Long? = null,

    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    @SerialName("cnvzDes")
    val conventionDescription: String? = null,

    @SerialName("sdrCnvzId")
    val conventionSiteId: Long? = null,

    @SerialName("dataIniCnvz")
    val conventionStartDate: String? = null,

    @SerialName("dataFinCnvz")
    val conventionEndDate: String? = null,

    @SerialName("defaultFlg")
    val defaultFlag: Int? = null,

    @SerialName("oppTitolo")
    val opportunityTitle: String? = null,

    @SerialName("oppDes")
    val opportunityDescription: String? = null,

    @SerialName("oppSede")
    val opportunitySite: String? = null,

    @SerialName("tipoPeriodoCod")
    val periodTypeCode: String? = null,

    @SerialName("tipoPeriodoDes")
    val periodTypeDescription: String? = null,

    @SerialName("dataIniTiro")
    val internshipStartDate: String? = null,

    @SerialName("dataFinTiro")
    val internshipEndDate: String? = null,

    @SerialName("orarioPrevisto")
    val expectedSchedule: String? = null,

    @SerialName("respUffCognome")
    val officeResponsibleSurname: String? = null,

    @SerialName("respUffNome")
    val officeResponsibleName: String? = null,

    @SerialName("tutDoceId")
    val tutorLecturerId: Long? = null,

    @SerialName("tutDoceMatricola")
    val tutorLecturerMatricola: String? = null,

    @SerialName("tutDoceCognome")
    val tutorLecturerSurname: String? = null,

    @SerialName("tutDoceNome")
    val tutorLecturerName: String? = null,

    @SerialName("tutSoggId")
    val tutorSubjectId: Long? = null,

    @SerialName("tutSoggCodFis")
    val tutorSubjectFiscalCode: String? = null,

    @SerialName("tutSoggCognome")
    val tutorSubjectSurname: String? = null,

    @SerialName("tutSoggNome")
    val tutorSubjectName: String? = null,

    @SerialName("delTutMatricola")
    val tutorStudentIdDelete: String? = null,

    @SerialName("delTutCognome")
    val tutorSurnameDelete: String? = null,

    @SerialName("delTutNome")
    val tutorNameDelete: String? = null,

    @SerialName("tipoStageCod")
    val stageTypeCode: String? = null,

    @SerialName("tipiStageDes")
    val stageTypesDescription: String? = null,

    @SerialName("areeSettId")
    val areasSectorsId: Long? = null,

    @SerialName("areeSettDes")
    val areasSectorsDescription: String? = null,

    @SerialName("areaDes")
    val areaDescription: String? = null,

    @SerialName("abilRicCfu")
    val cFURecognitionAuthorization: Int? = null,

    @SerialName("abilValfin")
    val finalValidationAuthorization: Int? = null,

    @SerialName("dataIniValfin")
    val finalEvaluationStartDate: String? = null,

    @SerialName("dataFinValfin")
    val finalEvaluationEndDate: String? = null,

    @SerialName("numGgIniValfin")
    val finalEvaluationStartDaysNumber: Int? = null,

    @SerialName("numGgFinValfin")
    val finalEvaluationEndDaysNumber: Int? = null,

    @SerialName("abilValmt")
    val midtermValidationAuthorization: Int? = null,

    @SerialName("dataIniValmt")
    val mtEvaluationStartDate: String? = null,

    @SerialName("dataFinValmt")
    val mtEvaluationEndDate: String? = null,

    @SerialName("numGgIniValmt")
    val mtEvaluationStartDaysNumber: Int? = null,

    @SerialName("numGgFinValmt")
    val mtEvaluationEndDaysNumber: Int? = null,

    @SerialName("abilRelfin")
    val finalRelationAuthorization: Int? = null,

    @SerialName("dataIniRelfin")
    val finalRelationStartDate: String? = null,

    @SerialName("dataFinRelfin")
    val finalRelationEndDate: String? = null,

    @SerialName("numGgIniRelfin")
    val finalRelationStartDaysNumber: Int? = null,

    @SerialName("numGgFinRelfin")
    val finalRelationEndDaysNumber: Int? = null,

    @SerialName("statoPfCod")
    val pfStateCode: String? = null,

    @SerialName("pfDes")
    val pfDescription: String? = null,

    @SerialName("pfAccStu")
    val studentPfAcceptance: Int? = null,

    @SerialName("dataPfAccStu")
    val studentPfAcceptanceDate: String? = null,

    @SerialName("pfAccAzienda")
    val companyPfAcceptance: Int? = null,

    @SerialName("dataPfAccAzienda")
    val companyPfAcceptanceDate: String? = null,

    @SerialName("accManleva")
    val liabilityWaiverAcceptance: Int? = null,

    @SerialName("dataAccManleva")
    val indemnityAcceptanceDate: String? = null,

    @SerialName("firmCognome")
    val signatorySurname: String? = null,

    @SerialName("firmNome")
    val signatoryName: String? = null,

    @SerialName("firmRuolo")
    val signatoryRole: String? = null,

    @SerialName("durataOre")
    val durationHours: Long? = null,

    @SerialName("durataEffettiva")
    val effectiveDuration: Long? = null,

    @SerialName("durataMesi")
    val durationMonths: Long? = null,

    @SerialName("durataSett")
    val durationWeeks: Long? = null,

    @SerialName("durataGiorni")
    val durationDays: Long? = null,

    @SerialName("numGiorniTiroSett")
    val internshipWeeklyDaysNumber: Long? = null,

    @SerialName("fasciaAddettiStageCod")
    val stageStaffBandCode: String? = null,

    @SerialName("fasciaDes")
    val bandDescription: String? = null,

    @SerialName("numMaxStagisti")
    val maxInternsNumber: Long? = null,

    @SerialName("numAddetti")
    val staffNumber: Long? = null,

    @SerialName("numTirocinanti")
    val internNumber: Long? = null,

    @SerialName("numTirocinantiExtcurr")
    val extraCurricularInternNumber: Long? = null,

    @SerialName("numTiroTutCurr")
    val currentTutoredInternshipNumber: Long? = null,

    @SerialName("numTiroTutExtcurr")
    val extraCurricularTutoredInternshipNumber: Long? = null,

    @SerialName("contattiAmmCognome")
    val adminContactsSurname: String? = null,

    @SerialName("contattiAmmNome")
    val adminContactsName: String? = null,

    @SerialName("tipiInsLavId")
    val workInsertionTypesId: Long? = null,

    @SerialName("insLavDes")
    val workInsertionDescription: String? = null,

    @SerialName("attSvolteDes")
    val activitiesCarriedOutDescription: String? = null,

    @SerialName("competenzeAcqDes")
    val acquiredSkillsDescription: String? = null,

    @SerialName("contFormGen")
    val generalTrainingContent: String? = null,

    @SerialName("contFormSpec")
    val specificTrainingContent: String? = null,

    @SerialName("contFormazione")
    val trainingContent: String? = null,

    @SerialName("facilitazioni")
    val facilitations: String? = null,

    @SerialName("obiettFormDes")
    val trainingObjectiveDescription: String? = null,

    @SerialName("compAttese")
    val pendingComponents: String? = null,

    @SerialName("modVerifApprend")
    val learningVerificationMode: String? = null,

    @SerialName("numOreFormGen")
    val generalTrainingHoursNumber: Long? = null,

    @SerialName("numOreFormSpec")
    val specificTrainingHoursNumber: Long? = null,

    @SerialName("obiettFormDesTut")
    val trainingObjectiveDescriptionTutor: String? = null,

    @SerialName("attSvolteDesTut")
    val activitiesCarriedOutDescriptionTutor: String? = null,

    @SerialName("compAtteseTut")
    val pendingComponentsTutor: String? = null,

    @SerialName("competenzeAcqDesTut")
    val acquiredSkillsDescriptionTutor: String? = null,

    @SerialName("contFormGenTut")
    val generalTrainingContentTutor: String? = null,

    @SerialName("contFormSpecTut")
    val specificTrainingContentTutor: String? = null,

    @SerialName("contFormazioneTut")
    val trainingContentTutor: String? = null,

    @SerialName("modVerifApprendTut")
    val learningVerificationModeTutor: String? = null
)

@Serializable
data class Esse3CompanyPostOutput(
    @SerialName("enteId")
    val entityId: Long? = null,

    @SerialName("sdrId")
    val siteId: Long? = null
)

@Serializable
data class Esse3InternshipApplicationHeader(
    @SerialName("domTiroId")
    val domicileInternshipId: Long? = null,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("domTiroPrg")
    val domicileInternshipProgram: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("desAa")
    val academicYearDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("tipoTirocCod")
    val internshipTypeCode: String? = null,

    @SerialName("tipoTirocDes")
    val internshipTypeDescription: String? = null,

    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoDomTiroCod")
    val internshipApplicationStateCode: String? = null,

    @SerialName("statoDomTiroDes")
    val internshipApplicationStateDescription: String? = null,

    @SerialName("candVisEnteFlg")
    val candidateEntityViewFlag: Int? = null,

    @SerialName("abilRicCfu")
    val cFURecognitionAuthorization: Int? = null,

    @SerialName("enteId")
    val entityId: Long? = null,

    @SerialName("enteDes")
    val entityDescription: String? = null,

    @SerialName("duns")
    val duns: String? = null,

    @SerialName("piva")
    val vatNumber: String? = null,

    @SerialName("cf")
    val fiscalCode: String? = null,

    @SerialName("sdrId")
    val siteId: Long? = null,

    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    @SerialName("cnvzDes")
    val conventionDescription: String? = null,

    @SerialName("sdrCnvzId")
    val conventionSiteId: Long? = null,

    @SerialName("oppTitolo")
    val opportunityTitle: String? = null,

    @SerialName("oppDes")
    val opportunityDescription: String? = null
)

@Serializable
data class Esse3CompanyData(
    @SerialName("aziendaId")
    val companyId: Long? = null,

    @SerialName("aziendaCod")
    val companyCode: String? = null,

    @SerialName("aziendaDes")
    val companyDescription: String? = null,

    @SerialName("cf")
    val fiscalCode: String? = null,

    @SerialName("piva")
    val vatNumber: String? = null,

    @SerialName("pivaGruppo")
    val groupVatNumber: String? = null,

    @SerialName("duns")
    val duns: String? = null,

    @SerialName("tipoAziendaCod")
    val companyTypeCode: String? = null,

    @SerialName("tipoAziendaDes")
    val companyTypeDescription: String? = null,

    @SerialName("settAziendaCod")
    val companySectorCode: String? = null,

    @SerialName("settAziendaDes")
    val companySectorDescription: String? = null,

    @SerialName("privatoFlg")
    val privateFlag: Int? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("sdrId")
    val siteId: Long? = null,

    @SerialName("tipoSdrCod")
    val siteTypeCode: String? = null,

    @SerialName("tipoSdrDes")
    val siteTypeDescription: String? = null,

    @SerialName("sdrCod")
    val siteCode: String? = null,

    @SerialName("sdrDes")
    val siteDescription: String? = null,

    @SerialName("statoAziendaCod")
    val companyStateCode: String? = null,

    @SerialName("statoAziendaDes")
    val companyStateDescription: String? = null,

    @SerialName("provenienza")
    val origin: String? = null,

    @SerialName("fasciaDipCod")
    val departmentBandCode: String? = null,

    @SerialName("fasciaDipDes")
    val departmentBandDescription: String? = null,

    @SerialName("assCatFlg")
    val associationCategoryFlag: Int? = null,

    @SerialName("assCatAziendaId")
    val companyCategoryAssociationId: Long? = null,

    @SerialName("assCatAziendaCod")
    val companyCategoryAssociationCode: String? = null,

    @SerialName("assCatAziendaDes")
    val companyCategoryAssociationDescription: String? = null,

    @SerialName("desEstesa")
    val extendedDescription: String? = null,

    @SerialName("autPrivacyFlg")
    val privacyAuthorizationFlag: Int? = null,

    @SerialName("gruppoAppart")
    val belongingGroup: String? = null,

    @SerialName("codiceAssociativo")
    val associativeCode: String? = null,

    @SerialName("fatturato")
    val invoiced: String? = null,

    @SerialName("settAtecoId")
    val atecoSectorId: Long? = null,

    @SerialName("settAtecoCod")
    val atecoSectorCode: String? = null,

    @SerialName("settAtecoDes")
    val atecoSectorDescription: String? = null,

    @SerialName("profiloAziendaId")
    val companyProfileId: Long? = null,

    @SerialName("profiloAziendaDes")
    val companyProfileDescription: String? = null,

    @SerialName("catAtecoId")
    val atecoCategoryId: Long? = null,

    @SerialName("catAtecoDes")
    val atecoCategoryDescription: String? = null,

    @SerialName("profiloPermessiId")
    val permissionsProfileId: Long? = null,

    @SerialName("profiloPermessiDes")
    val permissionsProfileDescription: String? = null,

    @SerialName("profiloPermessiPadreId")
    val permissionsProfileParentId: Long? = null,

    @SerialName("oppEvidenzaFlg")
    val opportunityHighlightFlag: Int? = null,

    @SerialName("crmCod")
    val crmCode: String? = null,

    @SerialName("associazioneImprenditoriale")
    val businessAssociation: String? = null,

    @SerialName("vincIniStageCod")
    val initialStageWinnerCode: String? = null,

    @SerialName("vincIniStageDes")
    val initialStageWinnerDescription: String? = null,

    @SerialName("respPtaId")
    val ptaResponsibleId: Long? = null,

    @SerialName("respPtaCognome")
    val ptaResponsibleSurname: String? = null,

    @SerialName("respPtaNome")
    val ptaResponsibleName: String? = null,

    @SerialName("crmSyncFlg")
    val crmSyncFlag: Int? = null,

    @SerialName("regAziendaId")
    val companyRegistrationId: Long? = null,

    @SerialName("notaInterna")
    val internalNote: String? = null,

    @SerialName("prodotti")
    val products: String? = null,

    @SerialName("lingueLavoro")
    val workLanguages: String? = null,

    @SerialName("lingueLavoroGruppo")
    val workLanguagesGroup: String? = null,

    @SerialName("tipoSelCod")
    val selectionTypeCode: String? = null,

    @SerialName("tipoSelDes")
    val selectionTypeDescription: String? = null,

    @SerialName("contrAziendaCod")
    val companyContractCode: String? = null,

    @SerialName("contrAziendaDes")
    val companyContractDescription: String? = null,

    @SerialName("convenzEsse3Pa")
    val conventionEsse3Pa: Int? = null,

    @SerialName("dataIniConvenzEsse3Pa")
    val esse3PaConventionStartDate: String? = null,

    @SerialName("dataFinConvenzEsse3Pa")
    val esse3PaConventionEndDate: String? = null,

    @SerialName("schedaAccId")
    val accessCardId: Long? = null,

    @SerialName("schedaAccCod")
    val accessCardCode: String? = null,

    @SerialName("schedaAccDes")
    val accessCardDescription: String? = null,

    @SerialName("notaAzienda")
    val companyNote: String? = null,

    @SerialName("iataCod")
    val iataCode: String? = null,

    @SerialName("nomeAeroporto")
    val airportName: String? = null,

    @SerialName("hasValidCnvz")
    val hasValidConvention: Int? = null,

    @SerialName("hasValidOpportunita")
    val hasValidOpportunity: Int? = null
)

@Serializable
data class Esse3CompanyContactData(
    @SerialName("aziendaId")
    val companyId: Long? = null,

    @SerialName("contattoAziendaId")
    val companyContactId: Long? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("appellativo")
    val title: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("matCodfis")
    val matFiscalCode: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("tel")
    val phone: String? = null,

    @SerialName("attivoFlg")
    val activeFlag: Int? = null,

    @SerialName("ruolo")
    val role: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("nazioneNascita")
    val birthNation: String? = null,

    @SerialName("comuneNascita")
    val birthMunicipality: String? = null,

    @SerialName("siglaNasc")
    val birthAbbreviation: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("crmCod")
    val crmCode: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("linguaInfoId")
    val languageInfoId: Long? = null,

    @SerialName("linguaInfoDes")
    val languageInfoDescription: String? = null,

    @SerialName("regioneAlbo")
    val registerRegion: String? = null,

    @SerialName("numIscrAlbo")
    val registerEnrollmentNumber: String? = null,

    @SerialName("dataIscrAlbo")
    val enrollmentRegisterDate: String? = null
)

@Serializable
data class Esse3StudentInternshipEligibilityData(
    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("tipo_servizio")
    val serviceType: String? = null,

    @SerialName("esito_cod")
    val outcomeCode: String? = null,

    @SerialName("esito_des")
    val outcomeDescription: String? = null
)

@Serializable
data class Esse3CompanyPutInput(
    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("statoEnteCod")
    val entityStateCode: String? = null,

    @SerialName("settAtecoId")
    val atecoSectorId: Long? = null,

    @SerialName("catAtecoId")
    val atecoCategoryId: Long? = null,

    @SerialName("duns")
    val duns: String? = null,

    @SerialName("crmCod")
    val crmCode: String? = null,

    @SerialName("fatturato")
    val invoiced: String? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("user")
    val user: String? = null
)

@Serializable
data class Esse3CompanyLocationsData(
    @SerialName("sedeAziendaId")
    val companySiteId: Long? = null,

    @SerialName("aziendaId")
    val companyId: Long? = null,

    @SerialName("sedeAziendaDes")
    val companySiteDescription: String? = null,

    @SerialName("tipoSedeCod")
    val siteTypeCode: String? = null,

    @SerialName("tipiSedeDes")
    val siteTypesDescription: String? = null,

    @SerialName("via")
    val street: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("citta")
    val city: String? = null,

    @SerialName("provSigla")
    val provinceAbbreviation: String? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("nazioneDes")
    val nationDescription: String? = null,

    @SerialName("sedeNumTel")
    val sitePhoneNumber: String? = null,

    @SerialName("sedeFax")
    val siteFax: String? = null,

    @SerialName("indirizzoCompleto")
    val fullAddress: String? = null,

    @SerialName("emailVisWeb")
    val webVisibleEmail: Int? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("disattiva")
    val deactivate: Int? = null,

    @SerialName("iataCod")
    val iataCode: String? = null,

    @SerialName("nomeAeroporto")
    val airportName: String? = null
)

@Serializable
data class Esse3InternshipApplicationQuestionnaireData(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("domTiroId")
    val domicileInternshipId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("cognomeStu")
    val studentSurname: String? = null,

    @SerialName("nomeStu")
    val studentName: String? = null,

    @SerialName("tipoQuestCod")
    val questionTypeCode: String? = null,

    @SerialName("completoFlg")
    val completeFlag: Int? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    @SerialName("compCod")
    val componentCode: String? = null,

    @SerialName("destCod")
    val destinationCode: String? = null,

    @SerialName("ordVis")
    val orderVisible: Int? = null,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questCompId")
    val questionComponentId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("statoQuestCod")
    val questionStateCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("questionarioNote")
    val questionnaireNote: String? = null,

    @SerialName("questContCod")
    val questionContentCode: String? = null,

    @SerialName("questContDes")
    val questionContentDescription: String? = null,

    @SerialName("questDataIns")
    val questionInsertionDate: String? = null,

    @SerialName("questDataMod")
    val questionModificationDate: String? = null,

    @SerialName("quesitoId")
    val questionId: Long? = null,

    @SerialName("elemCod")
    val elementCode: String? = null,

    @SerialName("parentQuesitoId")
    val parentQuestionId: Long? = null,

    @SerialName("tipoFormatoCod")
    val formatTypeCode: String? = null,

    @SerialName("tipoFormatoDes")
    val formatTypeDescription: String? = null,

    @SerialName("quesitoPunteggio")
    val questionScore: Float? = null,

    @SerialName("obbligatorioFlg")
    val mandatoryFlag: Int? = null,

    @SerialName("quesitoNote")
    val questionNote: String? = null,

    @SerialName("tagCod")
    val tagCode: String? = null,

    @SerialName("categCod")
    val categoryCode: String? = null,

    @SerialName("elemDes")
    val elementDescription: String? = null,

    @SerialName("tipoElemCod")
    val elementTypeCode: String? = null,

    @SerialName("elemNota")
    val elementNote: String? = null,

    @SerialName("quesitoContCod")
    val questionItemContentCode: String? = null,

    @SerialName("rispostaId")
    val answerId: Long? = null,

    @SerialName("testoLibero")
    val freeText: String? = null,

    @SerialName("rispostaPunteggio")
    val answerScore: Float? = null,

    @SerialName("rispostaDataIns")
    val answerInsertionDate: String? = null,

    @SerialName("rispostaDataMod")
    val answerModificationDate: String? = null
)

@Serializable
data class Esse3CompanyPostInput(
    @SerialName("tipoEnteCod")
    val entityTypeCode: String? = null,

    @SerialName("provenienza")
    val origin: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("statoEnteCod")
    val entityStateCode: String? = null,

    @SerialName("settAtecoId")
    val atecoSectorId: Long? = null,

    @SerialName("catAtecoId")
    val atecoCategoryId: Long? = null,

    @SerialName("duns")
    val duns: String? = null,

    @SerialName("crmCod")
    val crmCode: String? = null,

    @SerialName("fatturato")
    val invoiced: String? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("user")
    val user: String? = null,

    @SerialName("cod")
    val code: String? = null
)

@Serializable
data class Esse3TrainingProject(
    @SerialName("idTirocinio")
    val internshipId: Long? = null,

    @SerialName("idCarriera")
    val careerId: Int? = null,

    @SerialName("idTipoTirocinio")
    val internshipTypeId: Int? = null,

    @SerialName("tipoTirocinioDesc")
    val internshipDescriptionType: String? = null,

    @SerialName("codiceFiscale")
    val fiscalCode: String? = null,

    @SerialName("cfu")
    val credits: Float? = null,

    @SerialName("dataInizioProgettoFormativo")
    val trainingProjectStartDate: String? = null,

    @SerialName("dataFineProgettoFormativo")
    val trainingProjectEndDate: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("titoloDiStudioLauDip")
    val degreeGraduationDepartment: Int? = null,

    @SerialName("titoloDiStudioLauDipDesc")
    val degreeGraduationDepartmentDescription: String? = null,

    @SerialName("titoloDiStudioClasseAggr")
    val degreeClassAggregate: String? = null,

    @SerialName("titoloDiStudioClasseCodMinisteriale")
    val ministerialDegreeClassCode: String? = null,

    @SerialName("titoloDiStudioClasseDesc")
    val degreeClassDescription: String? = null,

    @SerialName("titoloDiStudioCorso")
    val degreeCourseTitle: String? = null,

    @SerialName("titoloDiStudioCodInterno")
    val internalDegreeCode: String? = null,

    @SerialName("codicione")
    val bigCode: String? = null,

    @SerialName("idFacolta")
    val facultyId: Int? = null,

    @SerialName("facolta")
    val faculty: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("nomeAzienda")
    val companyName: String? = null,

    @SerialName("partitaIvaAzienda")
    val companyVatNumber: String? = null,

    @SerialName("codFiscAzienda")
    val companyFiscalCode: String? = null,

    @SerialName("settoreAziendale")
    val businessSector: String? = null,

    @SerialName("codiceAteco")
    val atecoCode: String? = null,

    @SerialName("codiceAtecoDesc")
    val atecoCodeDescription: String? = null,

    @SerialName("stabilimentoRepartoUfficio")
    val plantDepartmentOffice: String? = null,

    @SerialName("sedeLegaleStato")
    val legalSeatState: Int? = null,

    @SerialName("sedeLegaleStatoDesc")
    val legalSeatStateDescription: String? = null,

    @SerialName("sedeLegaleProv")
    val legalSeatProvince: Int? = null,

    @SerialName("sedeLegaleProvDesc")
    val legalSeatProvinceDescription: String? = null,

    @SerialName("sedeLegaleComuneCodice")
    val legalSeatMunicipalityCode: Int? = null,

    @SerialName("sedeLegaleComuneDesc")
    val legalSeatMunicipalityDescription: String? = null,

    @SerialName("sedeLegaleIndirizzo")
    val legalSeatAddress: String? = null,

    @SerialName("sedeLegaleCap")
    val legalSeatPostalCode: String? = null,

    @SerialName("sedeOperativaStato")
    val operationalSeatState: Int? = null,

    @SerialName("sedeOperativaStatoDesc")
    val operationalSeatStateDescription: String? = null,

    @SerialName("sedeOperativaProv")
    val operationalSeatProvince: Int? = null,

    @SerialName("sedeOperativaProvDesc")
    val operationalSeatProvinceDescription: String? = null,

    @SerialName("sedeOperativaComuneCodice")
    val operationalSeatMunicipalityCode: Int? = null,

    @SerialName("sedeOperativaComuneDesc")
    val operationalSeatMunicipalityDescription: String? = null,

    @SerialName("sedeOperativaIndirizzo")
    val operationalSeatAddress: String? = null,

    @SerialName("sedeOperativaCap")
    val operationalSeatPostalCode: String? = null,

    @SerialName("tutorAziendaleNome")
    val companyTutorName: String? = null,

    @SerialName("tutorAziendaleCognome")
    val companyTutorSurname: String? = null,

    @SerialName("tutorAziendaleEmail")
    val companyTutorEmail: String? = null,

    @SerialName("tutorAziendaleRuolo")
    val companyTutorRole: String? = null,

    @SerialName("tutorAziendaleCompetenze")
    val companyTutorSkills: String? = null,

    @SerialName("tutorAziendaleInquadramento")
    val companyTutorPosition: String? = null,

    @SerialName("tutorAccademicoNome")
    val academicTutorName: String? = null,

    @SerialName("tutorAccademicoCognome")
    val academicTutorSurname: String? = null,

    @SerialName("tutorAccademicoEmail")
    val academicTutorEmail: String? = null,

    @SerialName("tutorAccademicoDipartimento")
    val academicTutorDepartment: String? = null,

    @SerialName("tutorAccademicoInquadramento")
    val academicTutorPosition: String? = null,

    @SerialName("tirocinioDurata")
    val internshipDuration: String? = null,

    @SerialName("tirocinioDurataTipo")
    val internshipDurationType: String? = null,

    @SerialName("oggettoTirocinio")
    val internshipObject: String? = null,

    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    @SerialName("attivita")
    val activity: String? = null
)

@Serializable
data class Esse3OpportunityData(
    @SerialName("titolo")
    val title: String? = null,

    @SerialName("azienda")
    val company: String? = null,

    @SerialName("dataIniIscr")
    val enrollmentStartDate: String? = null,

    @SerialName("dataFinIscr")
    val enrollmentEndDate: String? = null,

    @SerialName("tipoTirocCod")
    val internshipTypeCode: String? = null,

    @SerialName("tipo")
    val type: String? = null,

    @SerialName("tirocDesPub")
    val publicInternshipDescription: String? = null,

    @SerialName("descrizione")
    val description: String? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("reqCodFlg")
    val requiredCodeFlag: Int? = null,

    @SerialName("p06SediEntiEstDes")
    val p06ExternalEntitiesSitesDescription: String? = null,

    @SerialName("tipoSedeCod")
    val siteTypeCode: String? = null,

    @SerialName("tipoSedeDes")
    val siteTypeDescription: String? = null,

    @SerialName("p01NaziDes")
    val p01NationDescription: String? = null,

    @SerialName("p01ComuSigla")
    val p01MunicipalityAbbreviation: String? = null,

    @SerialName("p01ComuDes")
    val p01MunicipalityDescription: String? = null,

    @SerialName("p06SediEntiEstCap")
    val p06ExternalEntitiesSitesPostalCode: String? = null,

    @SerialName("p06SediEntiEstCitstra")
    val p06ExternalEntitiesSitesForeignCity: String? = null,

    @SerialName("p06SediEntiEstVia")
    val p06ExternalEntitiesSitesStreet: String? = null,

    @SerialName("sedePrefixInternaz")
    val siteInternationalPrefix: String? = null,

    @SerialName("sedeNumTel")
    val sitePhoneNumber: String? = null,

    @SerialName("sedeFax")
    val siteFax: String? = null,

    @SerialName("area")
    val area: String? = null,

    @SerialName("areaGeograficaCod")
    val geographicAreaCode: String? = null,

    @SerialName("settore")
    val sector: Long? = null,

    @SerialName("categProtetteFlg")
    val protectedCategoriesFlag: Int? = null,

    @SerialName("nazioneId")
    val nationId: Long? = null,

    @SerialName("settAtecoId")
    val atecoSectorId: Long? = null,

    @SerialName("catAtecoId")
    val atecoCategoryId: Long? = null,

    @SerialName("campagnaId")
    val campaignId: Long? = null,

    @SerialName("enteId")
    val entityId: Long? = null,

    @SerialName("dataIniTiro")
    val internshipStartDate: String? = null,

    @SerialName("durataMesi")
    val durationMonths: Long? = null
)

@Serializable
data class Esse3CompanyAgreementsData(
    @SerialName("sdrCnvzId")
    val conventionSiteId: Long? = null,

    @SerialName("sdrCnvzDes")
    val conventionSiteDescription: String? = null,

    @SerialName("sdrAziendaId")
    val siteCompanyId: Long? = null,

    @SerialName("aziendaId")
    val companyId: Long? = null,

    @SerialName("aziendaCod")
    val companyCode: String? = null,

    @SerialName("aziendaDes")
    val companyDescription: String? = null,

    @SerialName("statoAziendaCod")
    val companyStateCode: String? = null,

    @SerialName("statoCnvzCod")
    val conventionStateCode: String? = null,

    @SerialName("statoCnvzDes")
    val conventionStateDescription: String? = null,

    @SerialName("ruoloCnvzCod")
    val conventionRoleCode: String? = null,

    @SerialName("ruoloCnvzDes")
    val conventionRoleDescription: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("dataDeposito")
    val depositDate: String? = null,

    @SerialName("dataInvio")
    val sendingDate: String? = null,

    @SerialName("dataRestituzione")
    val returnDate: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("grpId")
    val groupId: Long? = null,

    @SerialName("livCod")
    val levelCode: String? = null,

    @SerialName("livDes")
    val levelDescription: String? = null,

    @SerialName("grpLivDes")
    val groupLevelDescription: String? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("tacitoRinnovoFlg")
    val tacitRenewalFlag: Int? = null,

    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    @SerialName("quadroFlg")
    val frameworkFlag: Int? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("testoLiberoCnvz")
    val freeTextConvention: String? = null,

    @SerialName("indirizzoSpedizione")
    val shippingAddress: String? = null,

    @SerialName("defaultFlg")
    val defaultFlag: Int? = null,

    @SerialName("stampaCnvzId")
    val conventionPrintId: Long? = null,

    @SerialName("docCod")
    val documentCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("tipoBolloCnvzCod")
    val conventionStampTypeCode: String? = null,

    @SerialName("tipoBolloCnvzDes")
    val conventionStampTypeDescription: String? = null
)
