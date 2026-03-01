package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3PostPlanRule(
    @SerialName("ordNum")
    val orderNumber: Int,

    @SerialName("des")
    val description: String,

    @SerialName("annoCorso")
    val courseYear: Int,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @SerialName("unitaMisura")
    val measurementUnit: String,

    @SerialName("tipoRegola")
    val ruleType: String,

    @SerialName("maxUnt")
    val maxTeachingUnit: Int,

    @SerialName("minUnt")
    val minTeachingUnit: Int,

    @SerialName("opzFlg")
    val optionalFlag: Int,

    @SerialName("sovranFlg")
    val overrideFlag: Int,

    @SerialName("taf")
    val taf: String? = null,

    @SerialName("interateCodeUn")
    val integratedUnifiedCode: String? = null
)

@Serializable
data class Esse3PostPhDPlanActivity(
    @SerialName("desAd")
    val teachingActivityDescription: String,

    @SerialName("tipoInsCod")
    val insertionTypeCode: String,

    @SerialName("codCatNazione")
    val categoryCountryCode: String? = null,

    @SerialName("dataPartenzaPrev")
    val expectedDepartureDate: String? = null,

    @SerialName("dataArrivoPrev")
    val expectedArrivalDate: String? = null,

    @SerialName("soggettoErogante")
    val providerSubject: String? = null,

    @SerialName("destinazione")
    val destination: String? = null,

    @SerialName("linkPubblicazioni")
    val publicationsLink: List<String> = emptyList()
)

@Serializable
data class Esse3PutInteruniversityPlanOptionalLocation(
    @SerialName("attuaPiano")
    val implementPlan: Boolean? = null,

    @SerialName("attivita")
    val activity: List<Esse3PostPlanActivity> = emptyList(),

    @SerialName("pianiInterateneo")
    val interuniversityPlans: List<Esse3PostInteruniversityPlanPortion> = emptyList(),

    @SerialName("attivitaInterateneo")
    val interUniversityActivities: List<Esse3PostInteruniversityPlanActivity> = emptyList(),

    @SerialName("segmentiInterateneo")
    val interuniversitySegments: List<Esse3PostInteruniversityPlanSegment> = emptyList()
)

@Serializable
data class Esse3EntityToUpdate(
    @SerialName("entita")
    val entity: String? = null,

    @SerialName("interateId")
    val integratedId: Int? = null,

    @SerialName("itmId")
    val itemId: Int? = null,

    @SerialName("staPianoInterateCod")
    val integratedStudyPlanStatusCode: String? = null,

    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String? = null,

    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    @SerialName("msg")
    val message: String? = null
)

@Serializable
data class Esse3PostInteruniversityPlanPortion(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("staPianoInterateCod")
    val integratedStudyPlanStatusCode: String,

    @SerialName("sedeOpPianoId")
    val operationalSitePlanId: Int,

    @SerialName("sedeOpStaPianoCod")
    val operationalSitePlanStatusCode: String? = null,

    @SerialName("sedeOpValutazione")
    val operationalSiteEvaluation: Int,

    @SerialName("msgSync")
    val syncMessage: String? = null,

    @SerialName("msgSyncDett")
    val syncMessageDetail: String? = null,

    @SerialName("userControlloCognome")
    val controlUserSurname: String? = null,

    @SerialName("userControllonome")
    val controlUserName: String? = null,

    @SerialName("userControlloMatricola")
    val controlUserMatricola: String? = null,

    @SerialName("notaControllo")
    val controlNote: String? = null,

    @SerialName("userValutatoreCognome")
    val evaluatorUserSurname: String? = null,

    @SerialName("userValutatorenome")
    val evaluatorUserName: String? = null,

    @SerialName("userValutatoreMatricola")
    val evaluatorUserMatricola: String? = null,

    @SerialName("notaValutatore")
    val evaluatorNote: String? = null,

    @SerialName("interateId")
    val integratedId: Int,

    @SerialName("regoleInterateneo")
    val interuniversityRules: List<Esse3PostInteruniversityPlanPortionRule> = emptyList()
)

@Serializable
data class Esse3CourseOrConditionFilter(
    @SerialName("andConditions")
    val termsAndConditions: List<Esse3CourseAndConditionFilter> = emptyList()
)

@Serializable
data class Esse3InteruniversityPlanSegmentWithDetails(
    @SerialName("sedeOffertaCodeUn")
    val offerSiteUnifiedCode: String,

    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String? = null,

    @SerialName("tipoSegInterateCod")
    val integratedSegmentTypeCode: String,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("ambId")
    val environmentId: Long? = null,

    @SerialName("ambDes")
    val environmentDescription: String? = null,

    @SerialName("tipoAfCod")
    val teachingActivityTypeCode: String? = null,

    @SerialName("tipoAfDes")
    val teachingActivityTypeDescription: String? = null,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    @SerialName("peso")
    val weight: Float,

    @SerialName("pesoConv")
    val conversionWeight: Float? = null,

    @SerialName("durata")
    val duration: Float? = null,

    @SerialName("durataStudioIndividuale")
    val individualStudyDuration: Float? = null,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("pianoId")
    val planId: Int,

    @SerialName("itmId")
    val itemId: Int,

    @SerialName("udId")
    val teachingUnitId: Long,

    @SerialName("segId")
    val segmentId: Long
)

@Serializable
data class Esse3InteruniversityPlanActivityResult(
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataSup")
    val supDate: String? = null,

    @SerialName("aaSupId")
    val academicYearSupervisorId: Int? = null,

    @SerialName("modValCod")
    val evaluationModeCode: String? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("lode")
    val cumLaude: Int? = null,

    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    @SerialName("tipoGiudDes")
    val judgmentTypeDescription: String? = null,

    @SerialName("ricId")
    val searchId: Int,

    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    @SerialName("livelloLinguaCod")
    val languageLevelCode: String? = null,

    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null
)

@Serializable
data class Esse3StudyPlanActivity(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("pianoId")
    val planId: Int? = null,

    @SerialName("scePianoId")
    val choicePlanId: Int? = null,

    @SerialName("itmId")
    val itemId: Int? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Int? = null,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adsceAttId")
    val choiceActivityId: Long? = null,

    @SerialName("sceltaFlg")
    val choiceFlag: Int? = null,

    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    @SerialName("ragId")
    val groupId: Int? = null,

    @SerialName("adragoffId")
    val activityRaggruppamentoOfferId: Long? = null,

    @SerialName("tipoRagCod")
    val groupingTypeCode: String? = null,

    @SerialName("adLibCod")
    val activityTranscriptCode: String? = null,

    @SerialName("adLibDes")
    val activityTranscriptDescription: String? = null,

    @SerialName("peso")
    val weight: Float? = null,

    @SerialName("pesoAdVis")
    val teachingActivityVisibleWeight: Float? = null,

    @SerialName("linguaSceCod")
    val chosenLanguageCode: String? = null,

    @SerialName("linguaSceNum")
    val chosenLanguageNumber: Long? = null,

    @SerialName("udSelezionate")
    val selectedTeachingUnits: List<Esse3StudyPlanTeachingUnit> = emptyList(),

    @SerialName("conteggiabileFlg")
    val countableFlag: Int? = null,

    @SerialName("deliberaFlg")
    val resolutionFlag: Int? = null,

    @SerialName("statoAttuazione")
    val implementationState: Int? = null,

    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: String? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null,

    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null,

    @SerialName("grpAdlogCod")
    val teachingActivityLogGroupCode: String? = null,

    @SerialName("grpAdlogDes")
    val teachingActivityLogGroupDescription: String? = null,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @SerialName("infoDottorati")
    val phdInfo: Esse3PhDStudyPlanActivityInfo? = null
)

@Serializable
data class Esse3PutInteruniversityPlanAdmissionLocation(
    @SerialName("attuaFlg")
    val implementationFlag: Boolean? = null,

    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String? = null,

    @SerialName("stato")
    val state: String,

    @SerialName("notaControllo")
    val controlNote: String? = null,

    @SerialName("userControlloCognome")
    val controlUserSurname: String? = null,

    @SerialName("userControlloNome")
    val controlUserName: String? = null,

    @SerialName("userControlloMatricola")
    val controlUserMatricola: String? = null,

    @SerialName("notaValutatore")
    val evaluatorNote: String? = null,

    @SerialName("userValutatoreCognome")
    val evaluatorUserSurname: String? = null,

    @SerialName("userValutatoreNome")
    val evaluatorUserName: String? = null,

    @SerialName("userValutatoreMatricola")
    val evaluatorUserMatricola: String? = null
)

@Serializable
data class Esse3PostPlanBody(
    @SerialName("tipo")
    val type: String,

    @SerialName("stato")
    val state: String,

    @SerialName("attuaFlg")
    val implementationFlag: Boolean,

    @SerialName("annullaPianoValidoFlg")
    val cancelValidPlanFlag: Boolean,

    @SerialName("tipoOrigModTaf")
    val tafModificationOriginType: String? = null,

    @SerialName("pdsSceCod")
    val studyPlanChoiceCode: String? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("tipoRegsce")
    val choiceRegulationType: Int,

    @SerialName("interateneo")
    val interuniversity: Esse3PostInteruniversityPlan? = null,

    @SerialName("regole")
    val rules: List<Esse3PostPlanRule> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3PostPlanActivity> = emptyList()
)

@Serializable
data class Esse3FiltroCdsInterateBody(
    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("filtroCds")
    val courseOfStudyFilter: Esse3CourseOrConditionsFilter
)

@Serializable
data class Esse3InteruniversityPlanPortionRule(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("tipoRegolaInterateneo")
    val interuniversityRuleType: String,

    @SerialName("viewStudente")
    val studentView: Int,

    @SerialName("viewDocente")
    val lecturerView: Int,

    @SerialName("viewSegr")
    val secretariatView: Int
)

@Serializable
data class Esse3IntegratedCoursesFilterResponse(
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsSettFlg")
    val courseOfStudySectorFlag: Int? = null,

    @SerialName("cdsRifFlg")
    val courseOfStudyReferenceFlag: Int? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null
)

@Serializable
data class Esse3InteruniversityPlan(
    @SerialName("sedeAmmCodeUn")
    val adminSiteUnifiedCode: String
)

@Serializable
data class Esse3PostPlanActivity(
    @SerialName("itmId")
    val itemId: Int,

    @SerialName("itmPadreId")
    val parentItemId: Int? = null,

    @SerialName("genConvItmId")
    val generateConventionItemId: Int? = null,

    @SerialName("ordNum")
    val orderNumber: Int? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    @SerialName("ordAdId")
    val orderTeachingActivityId: Int? = null,

    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    @SerialName("tipoInsAdPadreDottorato")
    val phdParentTeachingActivityInsertionType: String? = null,

    @SerialName("dottorati")
    val phdPrograms: Esse3PostPhDPlanActivity? = null
)

@Serializable
data class Esse3PostInteruniversityPlanActivity(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String,

    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    @SerialName("tipoAzioneRicCod")
    val researchActionTypeCode: String? = null,

    @SerialName("adsceIntComplId")
    val choiceInternalComplementaryId: Long? = null,

    @SerialName("adIntComplCod")
    val internalComplementaryActivityCode: String? = null,

    @SerialName("adIntComplDes")
    val internalComplementaryActivityDescription: String? = null,

    @SerialName("dottTipoInsFlags")
    val phdEnrollmentTypeFlags: String? = null,

    @SerialName("esito")
    val outcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("esitoIntCompl")
    val integratedCompletionOutcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("itmId")
    val itemId: Int,

    @SerialName("msgSync")
    val syncMessage: String? = null
)

@Serializable
data class Esse3PostInteruniversityPlan(
    @SerialName("sedeAmmCodeUn")
    val adminSiteUnifiedCode: String,

    @SerialName("pianiInterateneo")
    val interuniversityPlans: List<Esse3PostInteruniversityPlanPortion> = emptyList(),

    @SerialName("attivitaInterateneo")
    val interUniversityActivities: List<Esse3PostInteruniversityPlanActivity> = emptyList(),

    @SerialName("segmentiInterateneo")
    val interuniversitySegments: List<Esse3PostInteruniversityPlanSegment> = emptyList()
)

@Serializable
data class Esse3PhDStudyPlanActivityInfo(
    @SerialName("soggettoErogante")
    val providerSubject: String? = null,

    @SerialName("destinazione")
    val destination: String? = null,

    @SerialName("dataPartenza")
    val departureDate: String? = null,

    @SerialName("dataArrivo")
    val arrivalDate: String? = null,

    @SerialName("noteAd")
    val teachingActivityNotes: String? = null,

    @SerialName("missioneFlg")
    val missionFlag: Int? = null,

    @SerialName("ricercaFlg")
    val searchFlag: Int? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null
)

@Serializable
data class Esse3PutIntegratedRecognitionPlanRejectionResponse(
    @SerialName("attivita")
    val activity: Esse3PutInteruniversityRecognitionPlanActivity? = null,

    @SerialName("errMsg")
    val errorMessage: String? = null,

    @SerialName("errCode")
    val errorCode: Int? = null
)

@Serializable
data class Esse3PostInteruniversityPlanPortionRule(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("tipoRegolaInterateneo")
    val interuniversityRuleType: String,

    @SerialName("viewStudente")
    val studentView: Int,

    @SerialName("viewDocente")
    val lecturerView: Int,

    @SerialName("viewSegr")
    val secretariatView: Int,

    @SerialName("ordNum")
    val orderNumber: Int
)

@Serializable
data class Esse3StudyPlansStatistics(
    @SerialName("tipPiano")
    val planType: String? = null,

    @SerialName("statuFlg")
    val statusFlag: Int? = null,

    @SerialName("tipoRegsce")
    val choiceRegulationType: Int? = null,

    @SerialName("num")
    val number: Int? = null,

    @SerialName("staPianoCod")
    val studyPlanStatusCode: String? = null,

    @SerialName("staPianoDes")
    val studyPlanStatusDescription: String? = null
)

@Serializable
data class Esse3InteruniversityPlanActivity(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String,

    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    @SerialName("tipoAzioneRicCod")
    val researchActionTypeCode: String? = null,

    @SerialName("adsceIntComplId")
    val choiceInternalComplementaryId: Long? = null,

    @SerialName("adIntComplCod")
    val internalComplementaryActivityCode: String? = null,

    @SerialName("adIntComplDes")
    val internalComplementaryActivityDescription: String? = null,

    @SerialName("dottTipoInsFlags")
    val phdEnrollmentTypeFlags: String? = null,

    @SerialName("esito")
    val outcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("esitoIntCompl")
    val integratedCompletionOutcome: Esse3InteruniversityPlanActivityResult? = null
)

@Serializable
data class Esse3InteruniversityPlanPortionWithDetails(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("staPianoInterateCod")
    val integratedStudyPlanStatusCode: String,

    @SerialName("sedeOpPianoId")
    val operationalSitePlanId: Int,

    @SerialName("sedeOpStaPianoCod")
    val operationalSitePlanStatusCode: String? = null,

    @SerialName("sedeOpValutazione")
    val operationalSiteEvaluation: Int,

    @SerialName("msgSync")
    val syncMessage: String? = null,

    @SerialName("msgSyncDett")
    val syncMessageDetail: String? = null,

    @SerialName("userControlloCognome")
    val controlUserSurname: String? = null,

    @SerialName("userControllonome")
    val controlUserName: String? = null,

    @SerialName("userControlloMatricola")
    val controlUserMatricola: String? = null,

    @SerialName("notaControllo")
    val controlNote: String? = null,

    @SerialName("userValutatoreCognome")
    val evaluatorUserSurname: String? = null,

    @SerialName("userValutatorenome")
    val evaluatorUserName: String? = null,

    @SerialName("userValutatoreMatricola")
    val evaluatorUserMatricola: String? = null,

    @SerialName("notaValutatore")
    val evaluatorNote: String? = null,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("pianoId")
    val planId: Int,

    @SerialName("interateId")
    val integratedId: Int,

    @SerialName("regoleInterateneo")
    val interuniversityRules: List<Esse3InteruniversityPlanPortionRuleWithDetails> = emptyList()
)

@Serializable
data class Esse3PutInteruniversityPlanOptionalLocationResponse(
    @SerialName("piano")
    val plan: Esse3StudyPlan? = null,

    @SerialName("entitaDaAggiornare")
    val entityToUpdate: List<Esse3EntityToUpdate> = emptyList()
)

@Serializable
data class Esse3InteruniversityPlanSegment(
    @SerialName("sedeOffertaCodeUn")
    val offerSiteUnifiedCode: String,

    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String? = null,

    @SerialName("tipoSegInterateCod")
    val integratedSegmentTypeCode: String,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("ambId")
    val environmentId: Long? = null,

    @SerialName("ambDes")
    val environmentDescription: String? = null,

    @SerialName("tipoAfCod")
    val teachingActivityTypeCode: String? = null,

    @SerialName("tipoAfDes")
    val teachingActivityTypeDescription: String? = null,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    @SerialName("peso")
    val weight: Float,

    @SerialName("pesoConv")
    val conversionWeight: Float? = null,

    @SerialName("durata")
    val duration: Float? = null,

    @SerialName("durataStudioIndividuale")
    val individualStudyDuration: Float? = null
)

@Serializable
data class Esse3CourseAndConditionFilter(
    @SerialName("filtroCod")
    val filterCode: String,

    @SerialName("valore")
    val value: String,

    @SerialName("notFlg")
    val noteFlag: Int
)

@Serializable
data class Esse3InteruniversityPlanActivityWithDetails(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String,

    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    @SerialName("tipoAzioneRicCod")
    val researchActionTypeCode: String? = null,

    @SerialName("adsceIntComplId")
    val choiceInternalComplementaryId: Long? = null,

    @SerialName("adIntComplCod")
    val internalComplementaryActivityCode: String? = null,

    @SerialName("adIntComplDes")
    val internalComplementaryActivityDescription: String? = null,

    @SerialName("dottTipoInsFlags")
    val phdEnrollmentTypeFlags: String? = null,

    @SerialName("esito")
    val outcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("esitoIntCompl")
    val integratedCompletionOutcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("pianoId")
    val planId: Int,

    @SerialName("itmId")
    val itemId: Int
)

@Serializable
data class Esse3PostInteruniversityPlanSegment(
    @SerialName("sedeOffertaCodeUn")
    val offerSiteUnifiedCode: String,

    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String? = null,

    @SerialName("tipoSegInterateCod")
    val integratedSegmentTypeCode: String,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String? = null,

    @SerialName("tipoCreDes")
    val creditTypeDescription: String? = null,

    @SerialName("settCod")
    val sectorCode: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("ambId")
    val environmentId: Long? = null,

    @SerialName("ambDes")
    val environmentDescription: String? = null,

    @SerialName("tipoAfCod")
    val teachingActivityTypeCode: String? = null,

    @SerialName("tipoAfDes")
    val teachingActivityTypeDescription: String? = null,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    @SerialName("peso")
    val weight: Float,

    @SerialName("pesoConv")
    val conversionWeight: Float? = null,

    @SerialName("durata")
    val duration: Float? = null,

    @SerialName("durataStudioIndividuale")
    val individualStudyDuration: Float? = null,

    @SerialName("itmId")
    val itemId: Int,

    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("segId")
    val segmentId: Long? = null
)

@Serializable
data class Esse3PutInteruniversityRecognitionPlanBody(
    @SerialName("attuaFlg")
    val implementationFlag: Boolean,

    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("attivita")
    val activity: List<Esse3PutInteruniversityRecognitionPlanActivity> = emptyList()
)

@Serializable
data class Esse3InteruniversityPlanWithDetails(
    @SerialName("sedeAmmCodeUn")
    val adminSiteUnifiedCode: String,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("pianoId")
    val planId: Int,

    @SerialName("pianiInterateneo")
    val interuniversityPlans: List<Esse3InteruniversityPlanPortionWithDetails> = emptyList(),

    @SerialName("attivitaInterateneo")
    val interUniversityActivities: List<Esse3InteruniversityPlanActivityWithDetails> = emptyList(),

    @SerialName("segmentiInterateneo")
    val interuniversitySegments: List<Esse3InteruniversityPlanSegmentWithDetails> = emptyList()
)

@Serializable
data class Esse3StudyPlan(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("pianoId")
    val planId: Int? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("regsceId")
    val choiceRegulationId: Long? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("finregsceId")
    val finalRegulationChoiceId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("stato")
    val state: String? = null,

    @SerialName("statoDes")
    val stateDescription: String? = null,

    @SerialName("dataUltimaVarStato")
    val lastStateChangeDate: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoPiano")
    val planType: String? = null,

    @SerialName("statutarioFlg")
    val statutoryFlag: Int? = null,

    @SerialName("coorte")
    val cohort: Int? = null,

    @SerialName("aaRevisioneId")
    val academicYearRevisionId: Int? = null,

    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Long? = null,

    @SerialName("pdsStuId")
    val studyPlanStudentId: Long? = null,

    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("pdsSceId")
    val studyPlanChoiceId: Long? = null,

    @SerialName("pdsSceCod")
    val studyPlanChoiceCode: String? = null,

    @SerialName("pdsSceDes")
    val studyPlanChoiceDescription: String? = null,

    @SerialName("aaDefId")
    val academicYearDefinitionId: Int? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    @SerialName("aptId")
    val aptId: Long? = null,

    @SerialName("aptCod")
    val aptCode: String? = null,

    @SerialName("aptDes")
    val aptDescription: String? = null,

    @SerialName("aaUltimaAttuazioneId")
    val academicYearLastImplementationId: Int? = null,

    @SerialName("dataUltimaAttuazione")
    val lastImplementationDate: String? = null,

    @SerialName("userUltimaAttuazione")
    val lastImplementationUser: String? = null,

    @SerialName("userControllo")
    val controlUser: String? = null,

    @SerialName("notaControllo")
    val controlNote: String? = null,

    @SerialName("noteSistema")
    val systemNotes: String? = null,

    @SerialName("noteUtente")
    val userNotes: String? = null,

    @SerialName("extCod")
    val externalCode: String? = null,

    @SerialName("regole")
    val rules: List<Esse3StudyPlanChoice> = emptyList(),

    @SerialName("attivita")
    val activity: List<Esse3StudyPlanActivity> = emptyList(),

    @SerialName("interateneo")
    val interuniversity: Esse3InteruniversityPlanWithDetails? = null
)

@Serializable
data class Esse3InteruniversityPlanPortionRuleWithDetails(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("tipoRegolaInterateneo")
    val interuniversityRuleType: String,

    @SerialName("viewStudente")
    val studentView: Int,

    @SerialName("viewDocente")
    val lecturerView: Int,

    @SerialName("viewSegr")
    val secretariatView: Int,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("pianoId")
    val planId: Int,

    @SerialName("interateId")
    val integratedId: Int,

    @SerialName("scePianoId")
    val choicePlanId: Long
)

@Serializable
data class Esse3CourseOrConditionsFilter(
    @SerialName("orConditions")
    val orConditions: List<Esse3CourseOrConditionFilter> = emptyList()
)

@Serializable
data class Esse3StudyPlanTeachingUnit(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("pianoId")
    val planId: Int? = null,

    @SerialName("itmId")
    val itemId: Int? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("scePianoId")
    val choicePlanId: Int? = null,

    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("sceltaAdId")
    val teachingActivityChoiceId: Int? = null,

    @SerialName("udId")
    val teachingUnitId: Int? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("peso")
    val weight: Float? = null
)

@Serializable
data class Esse3PutInteruniversityRecognitionPlanActivity(
    @SerialName("adCod")
    val activityCode: String,

    @SerialName("adDes")
    val activityDescription: String,

    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String,

    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int,

    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int,

    @SerialName("itmId")
    val itemId: Int? = null,

    @SerialName("azioneCod")
    val actionCode: String,

    @SerialName("tipoAzioneRicCod")
    val researchActionTypeCode: String? = null,

    @SerialName("minPuntiEsa")
    val minGraduationPoints: Int? = null,

    @SerialName("maxPuntiEsa")
    val maxGraduationPoints: Int? = null,

    @SerialName("adsceIntComplId")
    val choiceInternalComplementaryId: Long? = null,

    @SerialName("adIntComplCod")
    val internalComplementaryActivityCode: String? = null,

    @SerialName("adIntComplDes")
    val internalComplementaryActivityDescription: String? = null,

    @SerialName("staAdInterateCod")
    val integratedTeachingActivityStatusCode: String? = null,

    @SerialName("dettstaAdInterateCod")
    val integratedTeachingActivityStatusDetailCode: String? = null,

    @SerialName("esito")
    val outcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("esitoIntCompl")
    val integratedCompletionOutcome: Esse3InteruniversityPlanActivityResult? = null,

    @SerialName("segmenti")
    val segments: List<Esse3PostInteruniversityPlanSegment> = emptyList()
)

@Serializable
data class Esse3InteruniversityPlanPortion(
    @SerialName("sedeOpCodeUn")
    val operationalSiteUnifiedCode: String,

    @SerialName("staPianoInterateCod")
    val integratedStudyPlanStatusCode: String,

    @SerialName("sedeOpPianoId")
    val operationalSitePlanId: Int,

    @SerialName("sedeOpStaPianoCod")
    val operationalSitePlanStatusCode: String? = null,

    @SerialName("sedeOpValutazione")
    val operationalSiteEvaluation: Int,

    @SerialName("msgSync")
    val syncMessage: String? = null,

    @SerialName("msgSyncDett")
    val syncMessageDetail: String? = null,

    @SerialName("userControlloCognome")
    val controlUserSurname: String? = null,

    @SerialName("userControllonome")
    val controlUserName: String? = null,

    @SerialName("userControlloMatricola")
    val controlUserMatricola: String? = null,

    @SerialName("notaControllo")
    val controlNote: String? = null,

    @SerialName("userValutatoreCognome")
    val evaluatorUserSurname: String? = null,

    @SerialName("userValutatorenome")
    val evaluatorUserName: String? = null,

    @SerialName("userValutatoreMatricola")
    val evaluatorUserMatricola: String? = null,

    @SerialName("notaValutatore")
    val evaluatorNote: String? = null
)

@Serializable
data class Esse3PutIntegratedRecognitionPlanResponse(
    @SerialName("stato")
    val state: Int? = null,

    @SerialName("pianoDiStudio")
    val studyPlan: Esse3StudyPlan? = null,

    @SerialName("scarti")
    val discards: List<Esse3PutIntegratedRecognitionPlanRejectionResponse> = emptyList()
)

@Serializable
data class Esse3StudyPlanChoice(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("pianoId")
    val planId: Int? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("schemaId")
    val schemaId: Long? = null,

    @SerialName("scePianoId")
    val choicePlanId: Int? = null,

    @SerialName("sceltaId")
    val choiceId: Long? = null,

    @SerialName("ordNum")
    val orderNumber: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("ptSlotId")
    val ptSlotId: Long? = null,

    @SerialName("ptSlotCod")
    val ptSlotCode: String? = null,

    @SerialName("ptSlotDes")
    val ptSlotDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipSce")
    val choiceType: String? = null,

    @SerialName("tipSceDes")
    val choiceTypeDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipUnt")
    val teachingUnitType: String? = null,

    @SerialName("minUnt")
    val minTeachingUnit: Float? = null,

    @SerialName("maxUnt")
    val maxTeachingUnit: Float? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("modTAF")
    val tafMode: String? = null,

    @SerialName("opzFlg")
    val optionalFlag: Int? = null,

    @SerialName("tesorettoFlg")
    val treasureFlag: Int? = null,

    @SerialName("sovranFlg")
    val overrideFlag: Int? = null,

    @SerialName("azzeraCfuFlg")
    val resetCreditsFlag: Int? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("regolaSistemaFlg")
    val systemRuleFlag: Int? = null,

    @SerialName("extCod")
    val externalCode: String? = null
)
