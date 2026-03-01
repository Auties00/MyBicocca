package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3AttendanceParameters(
    @SerialName("staSceCod")
    val choiceStatusCode: String,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("percentualePresenza")
    val presencePercentage: Float? = null,

    @SerialName("percentualeOrePresenza")
    val presenceHoursPercentage: Float? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList()
)

@Serializable
data class Esse3TranscriptAverage(
    @Serializable(with = Esse3EnumValueSerializer::class)
    @SerialName("definizioneBase")
    val baseDefinition: String,

    @Serializable(with = Esse3EnumValueSerializer::class)
    @SerialName("tipoMediaCod")
    val averageTypeCode: String,

    @SerialName("base")
    val base: Int,

    @SerialName("tipoOk")
    val okType: Int,

    @SerialName("media")
    val average: Float
)

@Serializable
data class Esse3SingleAttendanceParameters(
    @SerialName("staSceCod")
    val choiceStatusCode: String,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("percentualePresenza")
    val presencePercentage: Float? = null,

    @SerialName("percentualeOrePresenza")
    val presenceHoursPercentage: Float? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList(),

    @SerialName("aaRilevazioneId")
    val academicYearSurveyId: Int? = null,

    @SerialName("codFisDocenteRilevazione")
    val lecturerFiscalCodeDetection: String? = null,

    @SerialName("codFisDocenteControllo")
    val lecturerFiscalCodeCheck: String? = null,

    @SerialName("totaleRilevazioni")
    val totalDetections: Int? = null,

    @SerialName("totaleOreRilevazioni")
    val totalDetectionHours: Float? = null
)

@Serializable
data class Esse3RecognitionConversionSegmentParameters(
    @SerialName("segsceId")
    val segmentChoiceId: Long,

    @SerialName("peso")
    val weight: Float? = null
)

@Serializable
data class Esse3TranscriptStats(
    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    @SerialName("umPesoMin")
    val minMeasurementUnitWeight: Float? = null,

    @SerialName("umPesoMax")
    val maxMeasurementUnitWeight: Float? = null,

    @SerialName("umPesoSuperato")
    val passedMeasurementUnitWeight: Float? = null,

    @SerialName("umPesoFrequentato")
    val attendedMeasurementUnitWeight: Float? = null,

    @SerialName("umPesoPianificato")
    val plannedMeasurementUnitWeight: Float? = null,

    @SerialName("umPesoPiano")
    val studyPlanMeasurementUnitWeight: Float? = null,

    @SerialName("umPesoCalcolato")
    val calculatedMeasurementUnitWeight: Float? = null,

    @SerialName("umPesoConvalidato")
    val validatedMeasurementUnitWeight: Float? = null,

    @SerialName("numAdLibretto")
    val bookletTeachingActivityNumber: Int? = null,

    @SerialName("numAdPiano")
    val studyPlanTeachingActivityNumber: Int? = null,

    @SerialName("numAdSuperate")
    val passedTeachingActivityNumber: Int? = null,

    @SerialName("numAdFrequentate")
    val attendedTeachingActivityNumber: Int? = null,

    @SerialName("numAdPianificate")
    val plannedTeachingActivityNumber: Int? = null,

    @SerialName("gruppoVoto")
    val gradeGroup: Esse3VoteGroup? = null,

    @SerialName("medie")
    val averages: List<Esse3TranscriptAverage> = emptyList()
)

@Serializable
data class Esse3TranscriptSegmentInsertionAttributes(
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String,

    @SerialName("taf")
    val taf: String? = null,

    @SerialName("ambId")
    val environmentId: Long? = null,

    @SerialName("settCod")
    val sectorCode: String,

    @SerialName("discCod")
    val disciplineCode: String? = null,

    @SerialName("peso")
    val weight: Float,

    @SerialName("durata")
    val duration: Float? = null,

    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    @SerialName("oreRegFreq")
    val registeredAttendanceHours: Float? = null,

    @SerialName("dataRegFreq")
    val attendanceRegistrationDate: String? = null,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null
)

@Serializable
data class Esse3RecognitionActCareerRecognitionParameters(
    @SerialName("progettoFormativo")
    val trainingProject: String? = null,

    @SerialName("stage")
    val stage: Esse3RecognitionActCareerInternshipRecognitionParameters? = null
)

@Serializable
data class Esse3BulkAttendanceRejectionResult(
    @SerialName("staSceCod")
    val choiceStatusCode: String,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("percentualePresenza")
    val presencePercentage: Float? = null,

    @SerialName("percentualeOrePresenza")
    val presenceHoursPercentage: Float? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList(),

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("retCode")
    val returnCode: Int? = null,

    @SerialName("errMsg")
    val errorMessage: String? = null
)

@Serializable
data class Esse3ExamSessionTranscriptFast(
    @SerialName("config")
    val config: Esse3ExamSessionConfig? = null,

    @SerialName("datacalId")
    val calendarDateId: Long? = null,

    @SerialName("capostipiteId")
    val rootId: Long? = null,

    @SerialName("commPianId")
    val committeePlanId: Long? = null,

    @SerialName("indexId")
    val indexId: Long? = null,

    @SerialName("periodoId")
    val periodId: Long? = null,

    @SerialName("numVerbaliGen")
    val generalMinutesNumber: Int? = null,

    @SerialName("numVerbaliCar")
    val careerMinutesNumber: Int? = null,

    @SerialName("numPubblicazioni")
    val publicationsNumber: Int? = null,

    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    @SerialName("statoLog")
    val logState: String? = null,

    @SerialName("statoAperturaApp")
    val callOpeningState: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoVerb")
    val minutesState: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoPubblEsiti")
    val outcomesPublicationState: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoInsEsiti")
    val outcomesInsertionState: String? = null,

    @SerialName("statoDes")
    val stateDescription: String? = null,

    @SerialName("stato")
    val state: String? = null,

    @SerialName("presidenteNome")
    val presidentName: String? = null,

    @SerialName("presidenteCognome")
    val presidentSurname: String? = null,

    @SerialName("presidenteId")
    val presidentId: Long? = null,

    @SerialName("tipoGestPrenDes")
    val bookingManagementTypeDescription: String? = null,

    @SerialName("tipoGestAppDes")
    val callManagementTypeDescription: String? = null,

    @SerialName("tipoDefAppDes")
    val defaultCallTypeDescription: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("tipoAppCod")
    val callTypeCode: String? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("appelloId")
    val examCallId: Long? = null,

    @SerialName("aaCalId")
    val academicYearCalendarId: Int? = null,

    @SerialName("noteSistLog")
    val systemLogNotes: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("tagTemplId")
    val tagTemplateId: Long? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("gruppoVotoId")
    val gradeGroupId: Long? = null,

    @SerialName("condId")
    val conditionId: Long? = null,

    @SerialName("tipoSceltaTurno")
    val shiftChoiceType: Int? = null,

    @SerialName("riservatoFlg")
    val reservedFlag: Int? = null,

    @SerialName("oraEsa")
    val graduationTime: String? = null,

    @SerialName("dataInizioApp")
    val callStartDate: String? = null,

    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    @SerialName("tipoDefAppCod")
    val defaultCallTypeCode: String? = null,

    @SerialName("desApp")
    val callDescription: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    @SerialName("staSceCod")
    val choiceStatusCode: String? = null
)

@Serializable
data class Esse3RecognitionParameters(
    @SerialName("ricId")
    val searchId: Int,

    @SerialName("modValCod")
    val evaluationModeCode: String? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int,

    @SerialName("aaSupId")
    val academicYearSupervisorId: Int? = null,

    @SerialName("aaCompId")
    val academicYearComponentId: Int? = null,

    @SerialName("dataComp")
    val completionDate: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("adIntegrativa")
    val integrativeActivity: Esse3RecognitionConversionInternalActivityParameters? = null,

    @SerialName("tipoRicCod")
    val requestTypeCode: String,

    @SerialName("linguaCod")
    val languageCode: String? = null,

    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    @SerialName("stage")
    val stage: Esse3RecognitionActCareerRecognitionParameters? = null
)

@Serializable
data class Esse3VoteGroup(
    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("minPunti")
    val minPoints: Int? = null,

    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null
)

@Serializable
data class Esse3RecognitionConversionInternalActivityParameters(
    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    @SerialName("cod")
    val code: String,

    @SerialName("des")
    val description: String,

    @SerialName("mappingSegmenti")
    val segmentsMapping: List<Esse3RecognitionConversionInternalSegmentParameters> = emptyList()
)

@Serializable
data class Esse3TranscriptSegmentAttributes(
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("tipoCreCod")
    val creditTypeCode: String,

    @SerialName("taf")
    val taf: String? = null,

    @SerialName("ambId")
    val environmentId: Long? = null,

    @SerialName("settCod")
    val sectorCode: String,

    @SerialName("discCod")
    val disciplineCode: String? = null,

    @SerialName("peso")
    val weight: Float,

    @SerialName("durata")
    val duration: Float? = null,

    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    @SerialName("oreRegFreq")
    val registeredAttendanceHours: Float? = null,

    @SerialName("dataRegFreq")
    val attendanceRegistrationDate: String? = null,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    @SerialName("tafDes")
    val tafDescription: String? = null,

    @SerialName("ambitoDes")
    val scopeDescription: String? = null,

    @SerialName("settDes")
    val sectorDescription: String? = null,

    @SerialName("discDes")
    val disciplineDescription: String? = null
)

@Serializable
data class Esse3TranscriptPartition(
    @SerialName("adpartId")
    val activityPartitionId: Long,

    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("matId")
    val matId: Long,

    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null,

    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("cognomeDocTit")
    val lecturerSurnameTitle: String? = null,

    @SerialName("nomeDoctit")
    val titleLecturerName: String? = null,

    @SerialName("ruoloDocTit")
    val lecturerRoleTitle: String? = null,

    @SerialName("appellativoDocTit")
    val lecturerTitle: String? = null,

    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null
)

@Serializable
data class Esse3SyllabusTeachingUnitTranscript(
    @SerialName("matId")
    val matId: Long,

    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    @SerialName("udLogId")
    val teachingUnitLogId: Long,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("desUdPubblFlg")
    val teachingUnitPublicationFlag: Int,

    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    @SerialName("prerequisiti")
    val prerequisites: String? = null,

    @SerialName("testiRiferimento")
    val referenceTexts: String? = null
)

@Serializable
data class Esse3PrerequisitesCheck(
    @SerialName("esito")
    val outcome: Int? = null
)

@Serializable
data class Esse3StudentPresenceReleaseParameters(
    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList()
)

@Serializable
data class Esse3ExamSessionTranscript(
    @SerialName("config")
    val config: Esse3ExamSessionConfig? = null,

    @SerialName("datacalId")
    val calendarDateId: Long? = null,

    @SerialName("capostipiteId")
    val rootId: Long? = null,

    @SerialName("commPianId")
    val committeePlanId: Long? = null,

    @SerialName("indexId")
    val indexId: Long? = null,

    @SerialName("periodoId")
    val periodId: Long? = null,

    @SerialName("numVerbaliGen")
    val generalMinutesNumber: Int? = null,

    @SerialName("numVerbaliCar")
    val careerMinutesNumber: Int? = null,

    @SerialName("numPubblicazioni")
    val publicationsNumber: Int? = null,

    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    @SerialName("statoLog")
    val logState: String? = null,

    @SerialName("statoAperturaApp")
    val callOpeningState: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoVerb")
    val minutesState: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoPubblEsiti")
    val outcomesPublicationState: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoInsEsiti")
    val outcomesInsertionState: String? = null,

    @SerialName("statoDes")
    val stateDescription: String? = null,

    @SerialName("stato")
    val state: String? = null,

    @SerialName("presidenteNome")
    val presidentName: String? = null,

    @SerialName("presidenteCognome")
    val presidentSurname: String? = null,

    @SerialName("presidenteId")
    val presidentId: Long? = null,

    @SerialName("tipoGestPrenDes")
    val bookingManagementTypeDescription: String? = null,

    @SerialName("tipoGestAppDes")
    val callManagementTypeDescription: String? = null,

    @SerialName("tipoDefAppDes")
    val defaultCallTypeDescription: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("tipoAppCod")
    val callTypeCode: String? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("appelloId")
    val examCallId: Long? = null,

    @SerialName("aaCalId")
    val academicYearCalendarId: Int? = null,

    @SerialName("noteSistLog")
    val systemLogNotes: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("tagTemplId")
    val tagTemplateId: Long? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("gruppoVotoId")
    val gradeGroupId: Long? = null,

    @SerialName("condId")
    val conditionId: Long? = null,

    @SerialName("tipoSceltaTurno")
    val shiftChoiceType: Int? = null,

    @SerialName("riservatoFlg")
    val reservedFlag: Int? = null,

    @SerialName("oraEsa")
    val graduationTime: String? = null,

    @SerialName("dataInizioApp")
    val callStartDate: String? = null,

    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    @SerialName("tipoDefAppCod")
    val defaultCallTypeCode: String? = null,

    @SerialName("desApp")
    val callDescription: String? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    @SerialName("staSceCod")
    val choiceStatusCode: String? = null
)

@Serializable
data class Esse3StudentSurveysParameters(
    @SerialName("idRilevazione")
    val detectionId: String,

    @SerialName("dataLezione")
    val lessonDate: String? = null,

    @SerialName("durata")
    val duration: Float? = null,

    @SerialName("codFisDocente")
    val lecturerFiscalCode: String? = null,

    @SerialName("statoPresenza")
    val presenceState: String? = null,

    @SerialName("minutiAssenza")
    val absenceMinutes: Int? = null
)

@Serializable
data class Esse3ActivitiesToInsert(
    @SerialName("annoCorso")
    val courseYear: Int,

    @SerialName("ofaFlg")
    val ofaFlag: Int? = null,

    @SerialName("debitoFlg")
    val debtFlag: Int? = null,

    @SerialName("tipoAttivita")
    val activityType: String,

    @SerialName("dettagliOfferta")
    val offerDetails: Esse3ActivityToInsertInOffer? = null,

    @SerialName("dettagliFuoriOfferta")
    val offOfferDetails: Esse3ActivityToInsertOutsideOffer? = null
)

@Serializable
data class Esse3BulkAttendanceStudentsDetailParameters(
    @SerialName("staSceCod")
    val choiceStatusCode: String,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("percentualePresenza")
    val presencePercentage: Float? = null,

    @SerialName("percentualeOrePresenza")
    val presenceHoursPercentage: Float? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList(),

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null
)

@Serializable
data class Esse3ActivityToInsertInOffer(
    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Int
)

@Serializable
data class Esse3RecognitionActCareerInternshipRecognitionParameters(
    @SerialName("azienda")
    val company: String? = null,

    @SerialName("sede")
    val site: String? = null,

    @SerialName("periodo")
    val period: String? = null,

    @SerialName("attivitaSvolte")
    val activitiesCarriedOut: String? = null
)

@Serializable
data class Esse3BulkAttendanceResult(
    @SerialName("retCode")
    val returnCode: Int? = null,

    @SerialName("errMsg")
    val errorMessage: String? = null,

    @SerialName("scarti")
    val discards: List<Esse3BulkAttendanceRejectionResult> = emptyList()
)

@Serializable
data class Esse3BulkPresenceReleaseParameters(
    @SerialName("studenti")
    val students: List<Esse3StudentPresenceReleaseParameters> = emptyList(),

    @SerialName("assegnaDataFreq")
    val assignAttendanceDate: Int? = null,

    @SerialName("percMinORil")
    val minimumReleasePercentage: Int? = null,

    @SerialName("percMinOre")
    val minimumHoursPercentage: Int? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("partCod")
    val partialCode: String,

    @SerialName("domPartCod")
    val domicilePartialCode: String,

    @SerialName("fatPartCod")
    val invoicePartialCode: String,

    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("pdsCod")
    val studyPlanCode: String,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("cdsCod")
    val courseOfStudyCode: String,

    @SerialName("adCod")
    val activityCode: String,

    @SerialName("aaRilevazioneId")
    val academicYearSurveyId: Int? = null,

    @SerialName("codFisDocenteControllo")
    val lecturerFiscalCodeCheck: String,

    @SerialName("codFisDocenteRilevazione")
    val lecturerFiscalCodeDetection: String,

    @SerialName("operazione")
    val operation: String
)

@Serializable
data class Esse3TranscriptRowPerActivityLog(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("emailAte")
    val ateEmail: String? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("adPartId")
    val activityPartitionId: Long? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("logPartCod")
    val partialLogCode: String? = null,

    @SerialName("logFatPartCod")
    val invoicePartialLogCode: String? = null,

    @SerialName("logDomPartCod")
    val domicilePartialLogCode: String? = null,

    @SerialName("logAaOffId")
    val academicYearOfferLogId: Int? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("staSceCod")
    val choiceStatusCode: String? = null,

    @SerialName("ricId")
    val searchId: Int? = null,

    @SerialName("peso")
    val weight: Float? = null,

    @SerialName("durata")
    val duration: Float? = null,

    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    @SerialName("freqFlg")
    val attendanceFlag: Int? = null,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("esito")
    val outcome: Esse3Result? = null,

    @SerialName("segmenti")
    val segments: List<Esse3TranscriptSegmentStudentClass> = emptyList()
)

@Serializable
data class Esse3PatchTranscriptRow(
    @SerialName("statoMissione")
    val missionState: String? = null
)

@Serializable
data class Esse3TranscriptSegmentStudentClass(
    @SerialName("segsceId")
    val segmentChoiceId: Long,

    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("matId")
    val matId: Long,

    @SerialName("attributi")
    val attributes: Esse3TranscriptSegmentAttributes,

    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null
)

@Serializable
data class Esse3AttendanceReleaseDetail(
    @SerialName("adsceRilId")
    val choiceReleaseId: Long? = null,

    @SerialName("adsceRilDettId")
    val choiceReleaseDetailId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("dataOraInizio")
    val startDateTime: String? = null,

    @SerialName("durata")
    val duration: Float? = null,

    @SerialName("stato")
    val state: String? = null,

    @SerialName("adLogDateId")
    val activityLogDateId: Long? = null,

    @SerialName("minutiAssenza")
    val absenceMinutes: Int? = null
)

@Serializable
data class Esse3SyllabusActivityTranscript(
    @SerialName("matId")
    val matId: Long,

    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    @SerialName("desAdPubblFlg")
    val teachingActivityPublicationFlag: Int,

    @SerialName("contenuti")
    val contents: String? = null,

    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    @SerialName("prerequisiti")
    val prerequisites: String? = null,

    @SerialName("metodiDidattici")
    val teachingMethods: String? = null,

    @SerialName("modalitaVerificaApprendimento")
    val learningVerificationMethod: String? = null,

    @SerialName("altreInfo")
    val otherInfo: String? = null,

    @SerialName("testiRiferimento")
    val referenceTexts: String? = null
)

@Serializable
data class Esse3RecognitionConversionInternalSegmentParameters(
    @SerialName("segsceId")
    val segmentChoiceId: Long,

    @SerialName("peso")
    val weight: Float? = null,

    @SerialName("settCod")
    val sectorCode: String? = null
)

@Serializable
data class Esse3ActivityToInsertOutsideOffer(
    @SerialName("cod")
    val code: String,

    @SerialName("des")
    val description: String,

    @SerialName("peso")
    val weight: Int? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("segmenti")
    val segments: List<Esse3TranscriptSegmentInsertionAttributes> = emptyList()
)

@Serializable
data class Esse3RecognitionConversionActivityDestinationParameters(
    @SerialName("ricId")
    val searchId: Int,

    @SerialName("modValCod")
    val evaluationModeCode: String? = null,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int,

    @SerialName("aaSupId")
    val academicYearSupervisorId: Int? = null,

    @SerialName("aaCompId")
    val academicYearComponentId: Int? = null,

    @SerialName("dataComp")
    val completionDate: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("adIntegrativa")
    val integrativeActivity: Esse3RecognitionConversionInternalActivityParameters? = null
)

@Serializable
data class Esse3TranscriptTest(
    @SerialName("adregId")
    val activityRegulationId: Long,

    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("matId")
    val matId: Long,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("staRegCod")
    val regulationStatusCode: String? = null,

    @SerialName("staRegDes")
    val regulationStatusDescription: String? = null,

    @SerialName("applistaId")
    val applicationListId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("tipoAppCod")
    val callTypeCode: String? = null,

    @SerialName("cdsEsaId")
    val courseOfStudyGraduationId: Long? = null,

    @SerialName("adEsaId")
    val activityExamId: Long? = null,

    @SerialName("sesId")
    val sessionId: Long? = null,

    @SerialName("sesDes")
    val sessionDescription: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("esitoFinale")
    val finalOutcome: Esse3Result? = null,

    @SerialName("esitoScr")
    val writingOutcome: Esse3Result? = null,

    @SerialName("esitoParziale")
    val partialOutcome: Esse3Result? = null,

    @SerialName("tipoNoSupCod")
    val noSupplementTypeCode: String? = null,

    @SerialName("tipiNosupDes")
    val noSupTypesDescription: String? = null,

    @SerialName("tipoNoCarCod")
    val noChargeTypeCode: String? = null,

    @SerialName("tipoNoCarDes")
    val noChargeTypeDescription: String? = null,

    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    @SerialName("verbId")
    val minutesId: Long? = null,

    @SerialName("errNum")
    val errorNumber: Long? = null,

    @SerialName("errDes")
    val errorDescription: String? = null,

    @SerialName("errDesWeb")
    val webErrorDescription: String? = null
)

@Serializable
data class Esse3TranscriptSegment(
    @SerialName("segsceId")
    val segmentChoiceId: Long,

    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("matId")
    val matId: Long,

    @SerialName("attributi")
    val attributes: Esse3TranscriptSegmentAttributes
)

@Serializable
data class Esse3BulkAttendanceParameters(
    @SerialName("studenti")
    val students: List<Esse3BulkAttendanceStudentsDetailParameters> = emptyList(),

    @SerialName("totaleOreRilevazioni")
    val totalDetectionHours: Float? = null,

    @SerialName("totaleRilevazioni")
    val totalDetections: Int? = null,

    @SerialName("codFisDocenteRilevazione")
    val lecturerFiscalCodeDetection: String? = null,

    @SerialName("codFisDocenteControllo")
    val lecturerFiscalCodeCheck: String? = null,

    @SerialName("aaRilevazioneId")
    val academicYearSurveyId: Int? = null,

    @SerialName("adCod")
    val activityCode: String,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

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
