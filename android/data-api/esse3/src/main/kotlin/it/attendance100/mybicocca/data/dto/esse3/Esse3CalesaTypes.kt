package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3CareerErrorDetail(
    @SerialName("errcarrtstId")
    val careerTestErrorId: Long? = null,

    @SerialName("progId")
    val programId: Int? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("entitaCarrCod")
    val careerEntityCode: String? = null,

    @SerialName("valNum")
    val numericValue: Long? = null,

    @SerialName("tipoErr")
    val errorType: Int? = null
)

@Serializable
data class Esse3ExamSessionsStatsByStatus(
    @SerialName("stato")
    val state: String? = null,

    @SerialName("statoDes")
    val stateDescription: String? = null,

    @SerialName("numApp")
    val callNumber: Int? = null,

    @SerialName("numAppSenzaIscr")
    val callWithoutEnrollmentNumber: Int? = null
)

@Serializable
data class Esse3BookingManagementHeaderType(
    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("listaStudentiFlg")
    val studentListFlag: Int? = null,

    @SerialName("regAppFlg")
    val applicationRegistrationFlag: Int? = null,

    @SerialName("chkCancPren")
    val checkCancelBooking: Int? = null
)

@Serializable
data class Esse3ShiftCommissionTeacher(
    @SerialName("ruoloDes")
    val roleDescription: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("ruoloCod")
    val roleCode: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("appLogId")
    val callLogId: Int? = null
)

@Serializable
data class Esse3ExamSessionEnrollmentParameters(
    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("tipoIscrStu")
    val studentEnrollmentType: String? = null,

    @SerialName("notaStu")
    val studentNote: String? = null,

    @SerialName("appLogId")
    val callLogId: Int? = null,

    @SerialName("tagCod")
    val tagCode: String? = null,

    @SerialName("attoreCod")
    val actorCode: String? = null,

    @SerialName("tipoSvolgimentoEsame")
    val examType: String? = null,

    @SerialName("misureCompensative")
    val compensatoryMeasures: List<Esse3ExamSessionEnrollmentCompensatoryMeasuresParameters> = emptyList()
)

@Serializable
data class Esse3ExamSession(
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

    @SerialName("statoVerb")
    val minutesState: Esse3MinutesState? = null,

    @SerialName("statoPubblEsiti")
    val outcomesPublicationState: Esse3OutcomesPublicationState? = null,

    @SerialName("statoInsEsiti")
    val outcomesInsertionState: Esse3OutcomesInsertionState? = null,

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

    @SerialName("tipoEsaCod")
    val graduationTypeCode: Esse3GraduationTypeCode? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: Esse3EnrollmentTypeCode? = null,

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
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3ExamSessionModificationCompensatoryMeasuresParameters(
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String,

    @SerialName("desLiberaMisura")
    val freeMeasureDescription: String? = null,

    @SerialName("deleteFlg")
    val deleteFlag: Boolean? = null
)

@Serializable
data class Esse3ExamSessionBookingConfigDetails(
    @SerialName("key")
    val key: String? = null,

    @SerialName("value")
    val value: String? = null
)

@Serializable
data class Esse3UpdateExamSession(
    @SerialName("invioCom")
    val committeeSending: Int? = null,

    @SerialName("linkAppello")
    val callLink: List<Esse3UpdateExamSessionLink> = emptyList(),

    @SerialName("commissione")
    val committee: List<Esse3UpdateTeacherCommission> = emptyList(),

    @SerialName("sessioni")
    val sessions: List<Esse3SessionInsertUpdate> = emptyList(),

    @SerialName("turni")
    val shifts: List<Esse3UpdateShift> = emptyList(),

    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("appId")
    val callId: Long? = null,

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
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3SystemLogCommitment(
    @SerialName("eventoCod")
    val eventCode: String,

    @SerialName("impegnoCod")
    val commitmentCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("adId")
    val activityId: Long,

    @SerialName("appId")
    val callId: Long,

    @SerialName("dataEvento")
    val eventDate: String? = null,

    @SerialName("dataImpegno")
    val commitmentDate: String,

    @SerialName("oraInizioImpegno")
    val commitmentStartTime: String,

    @SerialName("oraFineImpegno")
    val commitmentEndTime: String,

    @SerialName("extAulaCod")
    val externalClassroomCode: String,

    @SerialName("posti")
    val seats: Int,

    @SerialName("tolleranza")
    val tolerance: Int,

    @SerialName("desTurno")
    val shiftDescription: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null
)

@Serializable
data class Esse3SystemLogEventTestExport(
    @SerialName("elabId")
    val processingId: Long? = null,

    @SerialName("chiavePacchetto")
    val packageKey: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("facCodFiglio")
    val childFacultyCode: String? = null,

    @SerialName("cdsCodFiglio")
    val childCourseOfStudyCode: String? = null,

    @SerialName("adCodFiglio")
    val childActivityCode: String? = null,

    @SerialName("tipoAttivitaCod")
    val activityTypeCode: String? = null,

    @SerialName("annoAccademico")
    val academicYear: Int? = null,

    @SerialName("descrizione")
    val description: String? = null,

    @SerialName("descrizioneEng")
    val descriptionEnglish: String? = null,

    @SerialName("presidenteComm")
    val committeePresident: String? = null,

    @SerialName("commissioneAllExport")
    val committeeAllExport: String? = null,

    @SerialName("commissioneCorrente")
    val currentCommittee: List<Esse3ExamSessionCommissionTeacher> = emptyList(),

    @SerialName("maxAaSesId")
    val maxAcademicYearSessionId: Long? = null,

    @SerialName("chiaveMaxSessione")
    val maxSessionKey: String? = null,

    @SerialName("numPostiTotale")
    val totalSeatsNumber: Long? = null,

    @SerialName("esameComune")
    val commonExam: List<Esse3SystemLogEventRowExport> = emptyList(),

    @SerialName("noteSistLog")
    val systemLogNotes: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("sedeProgDidId")
    val didacticProgramSiteId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("tipoDiff")
    val differenceType: Int? = null
)

@Serializable
data class Esse3TeacherAuthorizations(
    @SerialName("aaAbilDocId")
    val academicYearTeacherAuthorizationId: Int,

    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("cdsDefAppCod")
    val courseOfStudyDefaultCallCode: String? = null,

    @SerialName("adId")
    val activityId: Int,

    @SerialName("adDefAppCod")
    val activityExamDefinitionCode: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("defApp")
    val defaultCall: Int,

    @SerialName("visApp")
    val callVisible: Int,

    @SerialName("minAaSesId")
    val minAcademicYearSessionId: Int? = null,

    @SerialName("maxAaSesId")
    val maxAcademicYearSessionId: Int? = null,

    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    @SerialName("gruppoVotoCod")
    val gradeGroupCode: String? = null,

    @SerialName("figliEsacom")
    val esacomChildren: List<Esse3SharedExamAuthorization> = emptyList()
)

@Serializable
data class Esse3SharedExamInsert(
    @SerialName("mutFlg")
    val mutualFlag: Int,

    @SerialName("logCondFlg")
    val logConditionFlag: Int
)

@Serializable
data class Esse3UpdateShift(
    @SerialName("commissione")
    val committee: List<Esse3UpdateTeacherCommission> = emptyList(),

    @SerialName("appLogId")
    val callLogId: Int,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    @SerialName("edificioId")
    val buildingId: Int? = null,

    @SerialName("aulaId")
    val classroomId: Int? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null
)

@Serializable
data class Esse3SharedExamSession(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("adId")
    val activityId: Int,

    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long,

    @SerialName("adFiglioId")
    val childActivityId: Long,

    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null,

    @SerialName("appId")
    val callId: Int
)

@Serializable
data class Esse3ExamSessionEnrollmentErrors(
    @SerialName("applistaId")
    val applicationListId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("appLogId")
    val callLogId: Int? = null,

    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("tipoErrore")
    val errorType: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3Esse3SystemLogCommitment(
    @SerialName("impegnoCod")
    val commitmentCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("appLogId")
    val callLogId: Long? = null,

    @SerialName("appelloId")
    val examCallId: Long? = null,

    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    @SerialName("dataInizioApp")
    val callStartDate: String? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("oraEsa")
    val graduationTime: String? = null,

    @SerialName("matricolaPres")
    val presidentMatricola: String? = null,

    @SerialName("cntIscritti")
    val enrolledCount: Int? = null
)

@Serializable
data class Esse3UpdateSystemLogCommitments(
    @SerialName("invioCom")
    val committeeSending: Int,

    @SerialName("impegni")
    val commitments: List<Esse3UpdateSystemLogCommitment> = emptyList()
)

@Serializable
data class Esse3UpdateSystemLogCommitment(
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("eventoCod")
    val eventCode: String,

    @SerialName("impegnoCod")
    val commitmentCode: String,

    @SerialName("dataImpegno")
    val commitmentDate: String? = null,

    @SerialName("oraInizioImpegno")
    val commitmentStartTime: String? = null,

    @SerialName("oraFineImpegno")
    val commitmentEndTime: String? = null,

    @SerialName("extAulaCod")
    val externalClassroomCode: String? = null,

    @SerialName("posti")
    val seats: Int? = null,

    @SerialName("tolleranza")
    val tolerance: Int? = null,

    @SerialName("desTurno")
    val shiftDescription: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("operazione")
    val operation: String
)

@Serializable
data class Esse3PublicationParameters(
    @SerialName("emailMittente")
    val senderEmail: String? = null,

    @SerialName("nomeMittente")
    val senderName: String? = null,

    @SerialName("dataPubbl")
    val publicationDate: String,

    @SerialName("dataUltimoRif")
    val lastReferenceDate: String? = null,

    @SerialName("notaMailStu")
    val studentMailNote: String? = null,

    @SerialName("notaFrom")
    val noteFrom: String? = null,

    @SerialName("inviaComFlg")
    val sendCommitteeFlag: Int,

    @SerialName("stuDaPubblicare")
    val studentToPublish: List<Int> = emptyList()
)

@Serializable
data class Esse3ShiftWithCommission(
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    @SerialName("extAulaCod")
    val externalClassroomCode: String? = null,

    @SerialName("aulaCod")
    val classroomCode: String? = null,

    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    @SerialName("edificioCod")
    val buildingCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    @SerialName("edificioId")
    val buildingId: Int? = null,

    @SerialName("aulaId")
    val classroomId: Int? = null,

    @SerialName("appLogId")
    val callLogId: Int? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("commissione")
    val committee: List<Esse3ShiftCommissionTeacher> = emptyList()
)

@Serializable
data class Esse3SystemLogEventRowExport(
    @SerialName("elabId")
    val processingId: Long? = null,

    @SerialName("chiavePacchetto")
    val packageKey: String? = null,

    @SerialName("cdsCodFiglio")
    val childCourseOfStudyCode: String? = null,

    @SerialName("adCodFiglio")
    val childActivityCode: String? = null,

    @SerialName("facCodFiglio")
    val childFacultyCode: String? = null,

    @SerialName("flgLogicoMaster")
    val masterLogicalFlag: Int? = null,

    @SerialName("numPosti")
    val seatsNumber: Long? = null
)

@Serializable
data class Esse3SystemLogCommitmentImportError(
    @SerialName("eventoCod")
    val eventCode: String,

    @SerialName("impegnoCod")
    val commitmentCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("adId")
    val activityId: Long,

    @SerialName("appId")
    val callId: Long,

    @SerialName("dataEvento")
    val eventDate: String? = null,

    @SerialName("dataImpegno")
    val commitmentDate: String,

    @SerialName("oraInizioImpegno")
    val commitmentStartTime: String,

    @SerialName("oraFineImpegno")
    val commitmentEndTime: String,

    @SerialName("extAulaCod")
    val externalClassroomCode: String,

    @SerialName("posti")
    val seats: Int,

    @SerialName("tolleranza")
    val tolerance: Int,

    @SerialName("desTurno")
    val shiftDescription: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("desErrore")
    val errorDescription: String? = null,

    @SerialName("codErrore")
    val errorCode: Int? = null
)

@Serializable
data class Esse3CompensatoryMeasureEnrollment(
    @SerialName("applistaId")
    val applicationListId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("appLogId")
    val callLogId: Int? = null,

    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    @SerialName("desLiberaFlg")
    val freeDescriptionFlag: Int? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("statoMisComp")
    val compensatoryMeasureState: String? = null,

    @SerialName("statoMisCompDes")
    val compensatoryMeasureStateDescription: String? = null
)

@Serializable
data class Esse3BookingModificationParameters(
    @SerialName("attoreCod")
    val actorCode: String? = null,

    @SerialName("tipoSvolgimentoEsame")
    val examType: String? = null,

    @SerialName("misureCompensative")
    val compensatoryMeasures: List<Esse3ExamSessionModificationCompensatoryMeasuresParameters> = emptyList()
)

@Serializable
data class Esse3ExamSessionLinkInsert(
    @SerialName("templvotoRelId")
    val voteTemplateRelationId: Long? = null,

    @SerialName("cdsRelId")
    val courseOfStudyRelationId: Long? = null,

    @SerialName("adRelId")
    val activityRelationId: Long? = null,

    @SerialName("appRelId")
    val callRelationId: Long? = null,

    @SerialName("tipoLinkRelAppCod")
    val relationCallLinkTypeCode: String? = null
)

@Serializable
data class Esse3EnrollmentTag(
    @SerialName("tagCod")
    val tagCode: String? = null,

    @SerialName("tagDes")
    val tagDescription: String? = null,

    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null,

    @SerialName("livCertLinUscitaCod")
    val exitLanguageCertificationLevelCode: String? = null,

    @SerialName("livCertLinUscitaDes")
    val exitLanguageCertificationLevelDescription: String? = null
)

@Serializable
data class Esse3ExamType(
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("tipoSvolgimentoEsameDes")
    val examTypeDescription: String? = null,

    @SerialName("attoreCod")
    val actorCode: String? = null,

    @SerialName("webFlg")
    val webFlag: Int? = null
)

@Serializable
data class Esse3UpdateResult(
    @SerialName("docenteImpersId")
    val lecturerImpersonalId: Long? = null,

    @SerialName("sovrascritturaFlg")
    val overwriteFlag: Int,

    @SerialName("voto")
    val grade: Int? = null,

    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    @SerialName("assenteFlg")
    val absentFlag: Int,

    @SerialName("ritiratoFlg")
    val withdrawnFlag: Int,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("domandeEsame")
    val examApplications: String? = null,

    @SerialName("notaPubbl")
    val publicNote: String? = null,

    @SerialName("presaVisione")
    val acknowledgmentOfReceipt: Esse3AcknowledgmentOfReceipt? = null,

    @SerialName("appCollegato")
    val linkedCall: Esse3ExamSessionLinkedToList? = null,

    @SerialName("tipoSvolgimentoEsame")
    val examType: String? = null
)

@Serializable
data class Esse3Session(
    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaSesId")
    val academicYearSessionId: Int? = null,

    @SerialName("sesId")
    val sessionId: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("sesCod")
    val sessionCode: String? = null,

    @SerialName("sesDes")
    val sessionDescription: String? = null,

    @SerialName("straFlg")
    val foreignFlag: Int? = null,

    @SerialName("tipoSesCod")
    val sessionTypeCode: String? = null,

    @SerialName("tipoSesDes")
    val sessionTypeDescription: String? = null,

    @SerialName("umCod")
    val measurementUnitCode: Esse3MeasurementUnitCode? = null,

    @SerialName("maxNormali")
    val maxNormal: Int? = null,

    @SerialName("maxLab")
    val maxLab: Int? = null,

    @SerialName("vincFlg")
    val winnerFlag: Int? = null,

    @SerialName("tipoValSes")
    val sessionEvaluationType: Esse3SessionEvaluationType? = null,

    @SerialName("numGgIniIscr")
    val enrollmentStartDaysNumber: Int? = null,

    @SerialName("grpCondSqlId")
    val groupSqlConditionId: Long? = null
)

@Serializable
data class Esse3ExamSessionEnrollmentCompensatoryMeasuresParameters(
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String,

    @SerialName("desLiberaMisura")
    val freeMeasureDescription: String? = null
)

@Serializable
data class Esse3SystemLogSessionsExport(
    @SerialName("elabId")
    val processingId: Long? = null,

    @SerialName("chiaveSessione")
    val sessionKey: String? = null,

    @SerialName("sesCod")
    val sessionCode: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("aaSesId")
    val academicYearSessionId: Int? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("dataFine")
    val endDate: String? = null
)

@Serializable
data class Esse3ShiftInsert(
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("aulaId")
    val classroomId: Int? = null,

    @SerialName("edificioId")
    val buildingId: Int? = null,

    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3SessionInsertUpdate(
    @SerialName("aaSesId")
    val academicYearSessionId: Int,

    @SerialName("sesId")
    val sessionId: Int
)

@Serializable
data class Esse3SharedExam(
    @SerialName("cdsEsaId")
    val courseOfStudyGraduationId: Long? = null,

    @SerialName("adEsaId")
    val activityExamId: Int? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("cdsEsaCod")
    val courseOfStudyGraduationCode: String? = null,

    @SerialName("cdsEsaDes")
    val courseOfStudyGraduationDescription: String? = null,

    @SerialName("adEsaCod")
    val activityExamCode: String? = null,

    @SerialName("adEsaDes")
    val activityExamDescription: String? = null,

    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long? = null,

    @SerialName("adFiglioId")
    val childActivityId: Long? = null,

    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null,

    @SerialName("mutFlg")
    val mutualFlag: Int,

    @SerialName("logCondFlg")
    val logConditionFlag: Int
)

@Serializable
data class Esse3ExamSessionSession(
    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("dataInizio")
    val startDate: String? = null,

    @SerialName("sesDes")
    val sessionDescription: String? = null,

    @SerialName("sesId")
    val sessionId: Int? = null,

    @SerialName("aaSesId")
    val academicYearSessionId: Int? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3CareerError(
    @SerialName("errcarrtstId")
    val careerTestErrorId: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("entitaErrCarrCod")
    val careerErrorEntityCode: String? = null,

    @SerialName("valAlfa")
    val alphanumericValue: String? = null,

    @SerialName("valNum")
    val numericValue: Long? = null,

    @SerialName("tipoErr")
    val errorType: Int? = null,

    @SerialName("dettagli")
    val details: List<Esse3CareerErrorDetail> = emptyList()
)

@Serializable
data class Esse3SystemLogExport(
    @SerialName("elabId")
    val processingId: Long? = null,

    @SerialName("diffElabId")
    val processingDifferenceId: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("tipoElab")
    val processingType: Long? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null
)

@Serializable
data class Esse3ExamSessionShift(
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    @SerialName("extAulaCod")
    val externalClassroomCode: String? = null,

    @SerialName("aulaCod")
    val classroomCode: String? = null,

    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    @SerialName("edificioCod")
    val buildingCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    @SerialName("edificioId")
    val buildingId: Int? = null,

    @SerialName("aulaId")
    val classroomId: Int? = null,

    @SerialName("appLogId")
    val callLogId: Int? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3ExamSessionWithDetails(
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

    @SerialName("statoVerb")
    val minutesState: Esse3MinutesState? = null,

    @SerialName("statoPubblEsiti")
    val outcomesPublicationState: Esse3OutcomesPublicationState? = null,

    @SerialName("statoInsEsiti")
    val outcomesInsertionState: Esse3OutcomesInsertionState? = null,

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

    @SerialName("tipoEsaCod")
    val graduationTypeCode: Esse3GraduationTypeCode? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: Esse3EnrollmentTypeCode? = null,

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

    @SerialName("turni")
    val shifts: List<Esse3ShiftWithCommission> = emptyList(),

    @SerialName("sessioni")
    val sessions: List<Esse3ExamSessionSession> = emptyList(),

    @SerialName("commissione")
    val committee: List<Esse3ExamSessionCommissionTeacher> = emptyList(),

    @SerialName("esameComune")
    val commonExam: List<Esse3SharedExamSession> = emptyList(),

    @SerialName("links")
    val links: List<Esse3ExamSessionLink> = emptyList(),

    @SerialName("tipiSvolgimentoEsame")
    val examTypes: List<Esse3ExamType> = emptyList()
)

@Serializable
data class Esse3ExamSessionEnrollment(
    @SerialName("applistaId")
    val applicationListId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("appLogId")
    val callLogId: Int? = null,

    @SerialName("stuId")
    val studentId: Int? = null,

    @SerialName("adregId")
    val activityRegulationId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    @SerialName("cdsAdStuCod")
    val courseOfStudyTeachingActivityStudentCode: String? = null,

    @SerialName("cdsAdStuDes")
    val courseOfStudyTeachingActivityStudentDescription: String? = null,

    @SerialName("cdsAdIdStu")
    val courseOfStudyTeachingActivityStudentId: Long? = null,

    @SerialName("desAppello")
    val examCallDescription: String? = null,

    @SerialName("desTurno")
    val shiftDescription: String? = null,

    @SerialName("aulaCod")
    val classroomCode: String? = null,

    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    @SerialName("edificioCod")
    val buildingCode: String? = null,

    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("dataOraTurno")
    val shiftDateTime: String? = null,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("statoAdsce")
    val teachingActivityChoiceState: Esse3State? = null,

    @SerialName("pesoAd")
    val teachingActivityWeight: Float? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("nomeStudente")
    val studentName: String? = null,

    @SerialName("nomeAlias")
    val aliasName: String? = null,

    @SerialName("cognomeStudente")
    val studentSurname: String? = null,

    @SerialName("codFisStudente")
    val studentFiscalCode: String? = null,

    @SerialName("dataNascitaStudente")
    val studentBirthDate: String? = null,

    @SerialName("sessoStudente")
    val studentGender: Esse3StudentGender? = null,

    @SerialName("comuNascCodIstat")
    val birthMunicipalityIstatCode: String? = null,

    @SerialName("cittStraNasc")
    val birthForeignCitizenship: String? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    @SerialName("cdsIdStu")
    val courseOfStudyStudentId: Long? = null,

    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Int? = null,

    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    @SerialName("pdsStuDes")
    val studyPlanStudentDescription: String? = null,

    @SerialName("pdsIdStu")
    val studyPlanStudentId: Long? = null,

    @SerialName("pubblId")
    val publicationId: Long? = null,

    @SerialName("presaVisione")
    val acknowledgmentOfReceipt: Esse3AcknowledgmentOfReceipt? = null,

    @SerialName("userIdPresaVisione")
    val userAcknowledgmentId: String? = null,

    @SerialName("userGrpPresaVisione")
    val userGroupAcknowledgment: Long? = null,

    @SerialName("dataRifEsito")
    val outcomeReferenceDate: String? = null,

    @SerialName("dataRifEsitoStu")
    val studentOutcomeReferenceDate: String? = null,

    @SerialName("notaPubbl")
    val publicNote: String? = null,

    @SerialName("gruppoVotoCod")
    val gradeGroupCode: String? = null,

    @SerialName("gruppoVotoMaxPunti")
    val gradeGroupMaxPoints: Int? = null,

    @SerialName("esito")
    val outcome: kotlinx.serialization.json.JsonObject? = null,

    @SerialName("manualeFlg")
    val manualFlag: Int? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("domandeEsame")
    val examApplications: String? = null,

    @SerialName("notaStudente")
    val studentNote: String? = null,

    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("tipoSvolgimentoEsameDes")
    val examTypeDescription: String? = null,

    @SerialName("tipoSvolgimentoEsameRichiestaFlg")
    val examTypeRequestFlag: String? = null,

    @SerialName("tagCod")
    val tagCode: String? = null,

    @SerialName("autoTagCod")
    val autoTagCode: String? = null,

    @SerialName("livUscitaCod")
    val exitLevelCode: String? = null,

    @SerialName("linguaUscitaCod")
    val exitLanguageCode: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("tipoDefAppCod")
    val defaultCallTypeCode: String? = null,

    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    @SerialName("tipoAppCod")
    val callTypeCode: String? = null,

    @SerialName("posiz")
    val position: Int? = null,

    @SerialName("posizApp")
    val applicationPosition: Int? = null,

    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @SerialName("aaCalId")
    val academicYearCalendarId: Int? = null,

    @SerialName("aaSesId")
    val academicYearSessionId: Int? = null,

    @SerialName("sesDes")
    val sessionDescription: String? = null,

    @SerialName("misureCompensative")
    val compensatoryMeasures: List<Esse3CompensatoryMeasureEnrollment> = emptyList(),

    @SerialName("warnings")
    val warnings: List<Esse3ExamSessionEnrollmentErrors> = emptyList()
)

@Serializable
data class Esse3UpdateExamSessionLink(
    @SerialName("tipoLinkRelAppCod")
    val relationCallLinkTypeCode: String? = null,

    @SerialName("appRelId")
    val callRelationId: Long? = null,

    @SerialName("adRelId")
    val activityRelationId: Long? = null,

    @SerialName("cdsRelId")
    val courseOfStudyRelationId: Long? = null,

    @SerialName("templvotoRelId")
    val voteTemplateRelationId: Long? = null
)

@Serializable
data class Esse3SystemLogImport(
    @SerialName("impegni")
    val commitments: List<Esse3SystemLogCommitment> = emptyList()
)

@Serializable
data class Esse3UpdateTeacherCommission(
    @SerialName("ordineVisNum")
    val orderVisibleNumber: Int? = null,

    @SerialName("ruoloCod")
    val roleCode: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long
)

@Serializable
data class Esse3SharedExamResult(
    @SerialName("forceFlg")
    val forceFlag: Int? = null,

    @SerialName("errMsg")
    val errorMessage: String? = null,

    @SerialName("dettaglioErrore")
    val errorDetail: Esse3CareerError? = null
)

@Serializable
data class Esse3ActivitiesPerExamSession(
    @SerialName("cdsDefAppId")
    val courseOfStudyDefaultCallId: Long,

    @SerialName("adDefAppId")
    val activityExamDefinitionId: Int,

    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("matId")
    val matId: Long? = null
)

@Serializable
data class Esse3ExamSessionConfig(
    @SerialName("tipoGestApp")
    val callManagementType: Esse3ExamSessionManagementHeaderType? = null,

    @SerialName("tipoGestPren")
    val bookingManagementType: Esse3BookingManagementHeaderType? = null,

    @SerialName("tipoGestPrenAttore")
    val bookingManagementActorType: List<Esse3ExamSessionBookingConfigDetails> = emptyList(),

    @SerialName("tipoGestAppDett")
    val callManagementTypeDetail: List<Esse3ExamSessionTgaConfigDetails> = emptyList()
)

@Serializable
data class Esse3SharedExamGeneral(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("adId")
    val activityId: Int,

    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long,

    @SerialName("adFiglioId")
    val childActivityId: Long,

    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null
)

@Serializable
data class Esse3ShiftTeacherAuthorization(
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    @SerialName("extAulaCod")
    val externalClassroomCode: String? = null,

    @SerialName("aulaCod")
    val classroomCode: String? = null,

    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    @SerialName("edificioCod")
    val buildingCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    @SerialName("edificioId")
    val buildingId: Int? = null,

    @SerialName("aulaId")
    val classroomId: Int? = null,

    @SerialName("appLogId")
    val callLogId: Int? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null
)

@Serializable
data class Esse3ExamSessionManagementHeaderType(
    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3ExamSessionCommissionTeacherInsert(
    @SerialName("docenteId")
    val lecturerId: Long,

    @SerialName("ruoloCod")
    val roleCode: String
)

@Serializable
data class Esse3ExamSessionTgaConfigDetails(
    @SerialName("key")
    val key: String? = null,

    @SerialName("value")
    val value: String? = null
)

@Serializable
data class Esse3ExamSessionLinkedToList(
    @SerialName("opType")
    val operationType: Esse3OperationType,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("appLogId")
    val callLogId: Long? = null,

    @SerialName("notaStu")
    val studentNote: String? = null,

    @SerialName("tagCod")
    val tagCode: String? = null
)

@Serializable
data class Esse3ExamSessionLink(
    @SerialName("desRelApp")
    val relationCallDescription: String? = null,

    @SerialName("tipoIscrRelCod")
    val enrollmentRelationTypeCode: String? = null,

    @SerialName("dataInizioRelApp")
    val relationCallStartDate: String? = null,

    @SerialName("adRelCod")
    val activityRelationCode: String? = null,

    @SerialName("cdsRelCod")
    val courseOfStudyRelationCode: String? = null,

    @SerialName("tipoLinkAppCod")
    val callLinkTypeCode: String? = null,

    @SerialName("tipoLinkRelAppCod")
    val relationCallLinkTypeCode: String? = null,

    @SerialName("appRelId")
    val callRelationId: Long? = null,

    @SerialName("adRelId")
    val activityRelationId: Long? = null,

    @SerialName("cdsRelId")
    val courseOfStudyRelationId: Long? = null,

    @SerialName("templvotoRelId")
    val voteTemplateRelationId: Long? = null,

    @SerialName("appId")
    val callId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("templvotoId")
    val voteTemplateId: Long? = null
)

@Serializable
data class Esse3ExamSessionInsert(
    @SerialName("links")
    val links: List<Esse3ExamSessionLinkInsert> = emptyList(),

    @SerialName("commissione")
    val committee: List<Esse3ExamSessionCommissionTeacherInsert> = emptyList(),

    @SerialName("sessioni")
    val sessions: List<Esse3SessionInsertUpdate> = emptyList(),

    @SerialName("turni")
    val shifts: List<Esse3ShiftInsert> = emptyList(),

    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("templateTurnoCod")
    val shiftTemplateCode: String? = null,

    @SerialName("aaCalId")
    val academicYearCalendarId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("desApp")
    val callDescription: String? = null,

    @SerialName("tipoDefAppCod")
    val defaultCallTypeCode: String? = null,

    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    @SerialName("dataInizioApp")
    val callStartDate: String? = null,

    @SerialName("oraEsa")
    val graduationTime: String? = null,

    @SerialName("riservatoFlg")
    val reservedFlag: Int? = null,

    @SerialName("tipoSceltaTurno")
    val shiftChoiceType: Int? = null,

    @SerialName("condId")
    val conditionId: Long? = null,

    @SerialName("gruppoVotoId")
    val gradeGroupId: Long? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("tagTemplId")
    val tagTemplateId: Long? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("noteSistLog")
    val systemLogNotes: String? = null
)

@Serializable
data class Esse3ExamSessionCommissionTeacher(
    @SerialName("ruoloDes")
    val roleDescription: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    @SerialName("ruoloCod")
    val roleCode: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("appId")
    val callId: Int? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3ExamSessionTeacherAuthorization(
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

    @SerialName("statoVerb")
    val minutesState: Esse3MinutesState? = null,

    @SerialName("statoPubblEsiti")
    val outcomesPublicationState: Esse3OutcomesPublicationState? = null,

    @SerialName("statoInsEsiti")
    val outcomesInsertionState: Esse3OutcomesInsertionState? = null,

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

    @SerialName("tipoEsaCod")
    val graduationTypeCode: Esse3GraduationTypeCode? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: Esse3EnrollmentTypeCode? = null,

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

    @SerialName("turni")
    val shifts: List<Esse3ShiftTeacherAuthorization> = emptyList(),

    @SerialName("docenteId")
    val lecturerId: Long? = null
)

@Serializable
data class Esse3SharedExamAuthorization(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("adId")
    val activityId: Int,

    @SerialName("aaOffId")
    val academicYearOfferId: Int,

    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long,

    @SerialName("adFiglioId")
    val childActivityId: Long,

    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long
)
