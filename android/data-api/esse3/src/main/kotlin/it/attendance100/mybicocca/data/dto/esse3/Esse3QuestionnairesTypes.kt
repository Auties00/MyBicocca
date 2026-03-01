package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3CompiledQuestionnaires(
    @SerialName("questionarioId")
    val questionnaireId: Long,

    @SerialName("questCompId")
    val questionComponentId: Long,

    @SerialName("ordVis")
    val orderVisible: Int,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("statoQuestCod")
    val questionStateCode: String? = null,

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

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("parentQuesitoId")
    val parentQuestionId: Long? = null,

    @SerialName("tipoFormatoCod")
    val formatTypeCode: String? = null,

    @SerialName("tipoFormatoDes")
    val formatTypeDescription: String? = null,

    @SerialName("quesitoPunteggio")
    val questionScore: Int? = null,

    @SerialName("obbligatorioFlg")
    val mandatoryFlag: Int? = null,

    @SerialName("quesitoNote")
    val questionNote: String? = null,

    @SerialName("tagCod")
    val tagCode: String? = null,

    @SerialName("categCod")
    val categoryCode: String? = null,

    @SerialName("tipoElemCod")
    val elementTypeCode: String? = null,

    @SerialName("elementiNota")
    val elementsNote: String? = null,

    @SerialName("rispostaId")
    val answerId: Long? = null,

    @SerialName("testoLibero")
    val freeText: String? = null,

    @SerialName("rispostaDataIns")
    val answerInsertionDate: String? = null,

    @SerialName("rispostaDataMod")
    val answerModificationDate: String? = null
)

@Serializable
data class Esse3UserCompiledEventTagsTeacherAvailability(
    @SerialName("userCompId")
    val userComponentId: Long? = null,

    @SerialName("questConfigId")
    val questionConfigId: Long? = null,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("idUser")
    val userId: Long? = null,

    @SerialName("dataIniComp")
    val completionStartDate: String? = null,

    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    @SerialName("questCompId")
    val questionComponentId: Long,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

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

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("domPartAdCod")
    val domicilePartialTeachingActivityCode: String? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("dataOra")
    val dateTime: String? = null
)

@Serializable
data class Esse3SummaryPages(
    @SerialName("paginaId")
    val pageId: Int? = null,

    @SerialName("p02QuesitiNote")
    val p02QuestionsNote: String? = null,

    @SerialName("p02QuesitiParentQuesitoId")
    val p02QuestionsParentQuestionId: String? = null,

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("elementiNote")
    val elementsNotes: String? = null,

    @SerialName("paragrafiRiepilogo")
    val summaryParagraphs: List<Esse3SummaryParagraph> = emptyList()
)

@Serializable
data class Esse3PhDTranscriptRowInfo(
    @SerialName("soggettoErogante")
    val providerSubject: String? = null,

    @SerialName("destinazione")
    val destination: String? = null,

    @SerialName("dataPartenza")
    val departureDate: String? = null,

    @SerialName("dataArrivo")
    val arrivalDate: String? = null,

    @SerialName("noteStu")
    val studentNotes: String? = null,

    @SerialName("adFuoriOffFlg")
    val activityOutsideOfferFlag: Int? = null,

    @SerialName("missioneFlg")
    val missionFlag: Int? = null,

    @SerialName("ricercaFlg")
    val searchFlag: Int? = null,

    @SerialName("periodoEsteroFlg")
    val foreignPeriodFlag: Int? = null,

    @SerialName("aziendaFlg")
    val companyFlag: Int? = null
)

@Serializable
data class Esse3SummaryParagraph(
    @SerialName("paginaId")
    val pageId: Int? = null,

    @SerialName("paragrafoId")
    val paragraphId: Int? = null,

    @SerialName("p02QuesitiElemCod")
    val p02QuestionsElementCode: String? = null,

    @SerialName("p02QuesitiNote")
    val p02QuestionsNote: String? = null,

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("elementiNota")
    val elementsNote: String? = null,

    @SerialName("domandeRiepilogo")
    val applicationsSummary: List<Esse3ApplicationsSummary> = emptyList(),

    @SerialName("risposteRiepilogo")
    val answersSummary: List<Esse3AnswersSummary> = emptyList()
)

@Serializable
data class Esse3ApplicationsSummary(
    @SerialName("paragrafoId")
    val paragraphId: Int? = null,

    @SerialName("domandaId")
    val applicationId: Int? = null,

    @SerialName("elemCod")
    val elementCode: String? = null,

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("elementiNota")
    val elementsNote: String? = null,

    @SerialName("tipoFormatoCod")
    val formatTypeCode: String? = null,

    @SerialName("note")
    val notes: String? = null
)

@Serializable
data class Esse3WebSummaryTags(
    @SerialName("questCompId")
    val questionComponentId: Int? = null,

    @SerialName("tagCod")
    val tagCode: String? = null,

    @SerialName("tagValAlfa")
    val tagAlphanumericValue: String? = null,

    @SerialName("tagValId")
    val tagValueId: String? = null,

    @SerialName("tagValNum")
    val tagNumericValue: Float? = null,

    @SerialName("tagValData")
    val tagDateValue: String? = null,

    @SerialName("visFlg")
    val visibleFlag: Int? = null
)

@Serializable
data class Esse3Result(
    @Serializable(with = Esse3EnumValueSerializer::class)
    @SerialName("modValCod")
    val evaluationModeCode: String,

    @SerialName("supEsaFlg")
    val supGraduationFlag: Int,

    @SerialName("voto")
    val grade: Float? = null,

    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    @SerialName("tipoGiudDes")
    val judgmentTypeDescription: String? = null,

    @SerialName("dataEsa")
    val graduationDate: String? = null,

    @SerialName("aaSupId")
    val academicYearSupervisorId: Int? = null
)

@Serializable
data class Esse3QuestionnaireCompiledEventTagsPostLogin(
    @SerialName("questCompId")
    val questionComponentId: Long? = null,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("aaId")
    val academicYearId: Int,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("annoNascita")
    val birthYear: Int? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    @SerialName("dataOra")
    val dateTime: String? = null
)

@Serializable
data class Esse3TeachingUnitLogStudyPlanWebList(
    @SerialName("adsceId")
    val activityChoiceId: Int? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Int? = null,

    @SerialName("aaOrd")
    val academicYearOrder: Int? = null,

    @SerialName("pdsId")
    val studyPlanId: Int? = null,

    @SerialName("adId")
    val activityId: Int? = null,

    @SerialName("udId")
    val teachingUnitId: Int? = null,

    @SerialName("partCod")
    val partialCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("docenteId")
    val lecturerId: Int? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("tipoCredCod")
    val creditTypeCode: String? = null,

    @SerialName("tipoCredDes")
    val creditTypeDescription: String? = null,

    @SerialName("docentiCognome")
    val lecturersSurname: String? = null,

    @SerialName("docentiNome")
    val lecturersName: String? = null,

    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    @SerialName("statoLink")
    val linkState: Int? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("cfu")
    val credits: Double? = null,

    @SerialName("tagsValdid")
    val tagsValidationDid: String? = null
)

@Serializable
data class Esse3InteruniversityTranscriptRowInfo(
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Long? = null,

    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("ateneoId")
    val universityId: Long? = null,

    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    @SerialName("pdsAdDes")
    val studyPlanTeachingActivityDescription: String? = null
)

@Serializable
data class Esse3QuestionnairePage(
    @SerialName("paginaId")
    val pageId: Int? = null,

    @SerialName("elemCod")
    val elementCode: String? = null,

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("elementiNota")
    val elementsNote: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("questCompId")
    val questionComponentId: Int? = null,

    @SerialName("userCompId")
    val userComponentId: Int? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("pPaginaPrecId")
    val previousPageId: Int? = null,

    @SerialName("pPaginaSuccId")
    val nextPageId: Int? = null,

    @SerialName("pQuestionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("pQuestionarioNote")
    val questionnaireNote: String? = null,

    @SerialName("questionarioId")
    val questionnaireId: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("numeroRisposte")
    val answersNumber: Int? = null,

    @SerialName("paragrafi")
    val paragraphs: List<Esse3Paragraphs> = emptyList(),

    @SerialName("tagsWeb")
    val webTags: List<Esse3WebTags> = emptyList()
)

@Serializable
data class Esse3InternshipQuestionnaires(
    @SerialName("configTiro")
    val internshipConfig: List<kotlinx.serialization.json.JsonObject> = emptyList(),

    @SerialName("domTiroTagComp")
    val domicileInternshipCompletionTag: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

@Serializable
data class Esse3AvailableAnswers(
    @SerialName("domandaId")
    val applicationId: Int? = null,

    @SerialName("rispostaId")
    val answerId: Int? = null,

    @SerialName("elemCod")
    val elementCode: String? = null,

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("elementiNota")
    val elementsNote: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("rispostaFormatoCod")
    val answerFormatCode: String? = null,

    @SerialName("domandaFormatoCod")
    val applicationFormatCode: String? = null,

    @SerialName("punteggio")
    val score: Int? = null,

    @SerialName("obbligatorioFlg")
    val mandatoryFlag: Int? = null,

    @SerialName("operandoDomId")
    val domicileOperandId: Int? = null,

    @SerialName("limMin")
    val minLimit: Int? = null,

    @SerialName("limMax")
    val maxLimit: Int? = null,

    @SerialName("numMaxValori")
    val maxValuesNumber: Int? = null,

    @SerialName("dominioRisposte")
    val answersDomain: List<Esse3AnswerDomains> = emptyList()
)

@Serializable
data class Esse3ContextualizedActivityKey(
    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("aaOrdCod")
    val academicYearOrderCode: String? = null,

    @SerialName("aaOrdDes")
    val academicYearOrderDescription: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Long,

    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    @SerialName("aaOffId")
    val academicYearOfferId: Long,

    @SerialName("adId")
    val activityId: Long,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("afId")
    val activityFunctionId: Long? = null
)

@Serializable
data class Esse3QuestionnaireCompiledEventTagsGeneralQuestionnaire(
    @SerialName("questCompId")
    val questionComponentId: Long,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("annoNascita")
    val birthYear: Int? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    @SerialName("dataOra")
    val dateTime: String? = null
)

@Serializable
data class Esse3WebTags(
    @SerialName("paginaId")
    val pageId: Int? = null,

    @SerialName("tagCod")
    val tagCode: String? = null,

    @SerialName("tagValAlfa")
    val tagAlphanumericValue: String? = null,

    @SerialName("tagValId")
    val tagValueId: String? = null,

    @SerialName("tagValNum")
    val tagNumericValue: Float? = null,

    @SerialName("tagValData")
    val tagDateValue: String? = null,

    @SerialName("visFlg")
    val visibleFlag: Int? = null
)

@Serializable
data class Esse3AnswersSummary(
    @SerialName("paragrafoId")
    val paragraphId: Int? = null,

    @SerialName("domandaId")
    val applicationId: Int? = null,

    @SerialName("rispostaCompilataId")
    val compiledAnswerId: Int? = null,

    @SerialName("elemCod")
    val elementCode: String? = null,

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("elementiNota")
    val elementsNote: String? = null,

    @SerialName("testoLibero")
    val freeText: String? = null,

    @SerialName("tipoFormatoCod")
    val formatTypeCode: String? = null,

    @SerialName("punteggio")
    val score: Int? = null,

    @SerialName("domandaTipoFormatoCod")
    val applicationFormatTypeCode: String? = null,

    @SerialName("note")
    val notes: String? = null
)

@Serializable
data class Esse3UserCompiledEventTagsPostLogin(
    @SerialName("userCompId")
    val userComponentId: Long? = null,

    @SerialName("questConfigId")
    val questionConfigId: Long? = null,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("idUser")
    val userId: Long? = null,

    @SerialName("dataIniComp")
    val completionStartDate: String? = null,

    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    @SerialName("questCompId")
    val questionComponentId: Long,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("annoNascita")
    val birthYear: Int? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    @SerialName("dataOra")
    val dateTime: String? = null
)

@Serializable
data class Esse3TagsList(
    @SerialName("tags")
    val tags: String? = null
)

@Serializable
data class Esse3TeachingUnitWithQuestionnaire(
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("stato")
    val state: Int? = null,

    @SerialName("questConfigId")
    val questionConfigId: Int? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("questionarioId")
    val questionnaireId: Int? = null,

    @SerialName("anonimoFlg")
    val anonymousFlag: Int? = null,

    @SerialName("udLogPdsListWeb")
    val teachingUnitLogWebStudyPlanList: List<Esse3TeachingUnitLogStudyPlanWebList> = emptyList()
)

@Serializable
data class Esse3CompiledAnswers(
    @SerialName("domandaId")
    val applicationId: Int? = null,

    @SerialName("quesitoId")
    val questionId: Int? = null,

    @SerialName("rispostaCompilataId")
    val compiledAnswerId: Int? = null,

    @SerialName("testoLibero")
    val freeText: String? = null,

    @SerialName("rispostaDominio")
    val answerDomain: Esse3AnswerDomains? = null
)

@Serializable
data class Esse3Applications(
    @SerialName("paragrafoId")
    val paragraphId: Int? = null,

    @SerialName("domandaId")
    val applicationId: Int? = null,

    @SerialName("elemCod")
    val elementCode: String? = null,

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("elementiNota")
    val elementsNote: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("obbligatorioFlg")
    val mandatoryFlag: Int? = null,

    @SerialName("numMaxSce")
    val maxChoiceNumber: Int? = null,

    @SerialName("tipoFormatoCod")
    val formatTypeCode: String? = null,

    @SerialName("rispDisponibili")
    val availableAnswers: List<Esse3AvailableAnswers> = emptyList(),

    @SerialName("rispComplete")
    val completeAnswers: List<Esse3CompiledAnswers> = emptyList()
)

@Serializable
data class Esse3TranscriptRow(
    @SerialName("matId")
    val matId: Long,

    @SerialName("ord")
    val order: Int,

    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("pianoId")
    val planId: Long? = null,

    @SerialName("itmId")
    val itemId: Long? = null,

    @SerialName("ragId")
    val groupId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("raggEsaTipo")
    val graduationGroupType: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String,

    @SerialName("annoCorso")
    val courseYear: Int,

    @Serializable(with = Esse3EnumValueSerializer::class)
    @SerialName("stato")
    val state: String,

    @SerialName("statoDes")
    val stateDescription: String,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @SerialName("tipoEsaDes")
    val graduationTypeDescription: String? = null,

    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null,

    @SerialName("ricId")
    val searchId: Int,

    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    @SerialName("peso")
    val weight: Float,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("freqUffFlg")
    val officialAttendanceFlag: Int,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int,

    @SerialName("dataScadIscr")
    val enrollmentDeadline: String? = null,

    @SerialName("gruppoVotoId")
    val gradeGroupId: Int? = null,

    @SerialName("gruppoVotoMinVoto")
    val gradeGroupMinGrade: Int? = null,

    @SerialName("gruppoVotoMaxVoto")
    val gradeGroupMaxGrade: Int? = null,

    @SerialName("gruppoVotoLodeFlg")
    val gradeGroupLodeFlag: Int? = null,

    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    @SerialName("gruppoGiudDes")
    val judgmentGroupDescription: String? = null,

    @SerialName("esito")
    val outcome: Esse3Result? = null,

    @SerialName("sovranFlg")
    val overrideFlag: Int,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("debitoFlg")
    val debtFlag: Int,

    @SerialName("ofaFlg")
    val ofaFlag: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @SerialName("genAutoFlg")
    val autoGenerationFlag: Int,

    @SerialName("genRicSpecFlg")
    val generateSpecialRequestFlag: Int,

    @SerialName("tipoOrigEvecar")
    val careerEventOriginType: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("infoDottorati")
    val phdInfo: Esse3PhDTranscriptRowInfo? = null,

    @SerialName("rilFreq")
    val attendanceRelease: List<Esse3AttendanceRelease> = emptyList(),

    @SerialName("statoMissione")
    val missionState: String? = null,

    @SerialName("statoMissioneDes")
    val missionStateDescription: String? = null,

    @SerialName("numAppelliPrenotabili")
    val bookableCallsNumber: Int? = null,

    @SerialName("superataFlg")
    val passedFlag: Int? = null,

    @SerialName("numPrenotazioni")
    val bookingNumber: Int? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("genConvAdsceId")
    val generateConventionTeachingActivityChoiceId: Long? = null,

    @SerialName("infoInterateneo")
    val interuniversityInfo: Esse3InteruniversityTranscriptRowInfo? = null,

    @SerialName("extraInfo")
    val extraInfo: Esse3ExtraTranscriptRow? = null
)

@Serializable
data class Esse3Answer(
    @SerialName("domandaId")
    val applicationId: Int? = null,

    @SerialName("rispostaId")
    val answerId: Int? = null,

    @SerialName("corpoRisposta")
    val responseBody: String? = null
)

@Serializable
data class Esse3AttendanceRelease(
    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adsceRilId")
    val choiceReleaseId: Long? = null,

    @SerialName("aaRilevazioneId")
    val academicYearSurveyId: Int? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("stuTipoCorsoCod")
    val studentCourseTypeCode: String? = null,

    @SerialName("statoAdsceRil")
    val teachingActivityChoiceReleaseState: String? = null,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("staSceCod")
    val choiceStatusCode: String? = null,

    @SerialName("totOrePerFreq")
    val totalHoursForAttendance: Float? = null,

    @SerialName("totRilPerFreq")
    val totalReleasedForAttendance: Int? = null,

    @SerialName("percPresPerFreq")
    val presencePercentageForAttendance: Float? = null,

    @SerialName("percOrePresPerFreq")
    val presenceHoursPercentageForAttendance: Float? = null,

    @SerialName("numRil")
    val releaseNumber: Int? = null,

    @SerialName("oreRil")
    val releasedHours: Float? = null,

    @SerialName("numAss")
    val absenceNumber: Int? = null,

    @SerialName("numPres")
    val presenceNumber: Int? = null,

    @SerialName("oreAss")
    val absenceHours: Float? = null,

    @SerialName("orePres")
    val presenceHours: Float? = null,

    @SerialName("oreTotFreqAd")
    val totalTeachingActivityAttendanceHours: Float? = null,

    @SerialName("numTotFreqAd")
    val totalTeachingActivityAttendanceNumber: Float? = null,

    @SerialName("dataFreqRilFreqDett")
    val attendanceReleaseDetailDate: String? = null,

    @SerialName("dataFreqAdLog")
    val teachingActivityAttendanceLogDate: String? = null
)

@Serializable
data class Esse3ExtraTranscriptRow(
    @SerialName("matId")
    val matId: Long? = null,

    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    @SerialName("dataInizioLezioni")
    val lessonsStartDate: String? = null,

    @SerialName("dataFineLezioni")
    val lessonsEndDate: String? = null,

    @SerialName("titMatricola")
    val titleMatricola: String? = null,

    @SerialName("titNome")
    val titleName: String? = null,

    @SerialName("titCognome")
    val titleSurname: String? = null,

    @SerialName("titCodFis")
    val titleFiscalCode: String? = null,

    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    @SerialName("tipiSceCod")
    val choiceTypesCode: String? = null,

    @SerialName("tipiSceDes")
    val choiceTypesDescription: String? = null,

    @SerialName("sceDes")
    val choiceDescription: String? = null,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null
)

@Serializable
data class Esse3UserCompiledEventTagsGeneralQuestionnaire(
    @SerialName("userCompId")
    val userComponentId: Long? = null,

    @SerialName("questConfigId")
    val questionConfigId: Long? = null,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("idUser")
    val userId: Long? = null,

    @SerialName("dataIniComp")
    val completionStartDate: String? = null,

    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    @SerialName("questCompId")
    val questionComponentId: Long,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("annoNascita")
    val birthYear: Int? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    @SerialName("dataOra")
    val dateTime: String? = null
)

@Serializable
data class Esse3UserCompiledEventTagsTeachingEvaluation(
    @SerialName("userCompId")
    val userComponentId: Long,

    @SerialName("questConfigId")
    val questionConfigId: Long? = null,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("idUser")
    val userId: Long? = null,

    @SerialName("dataIniComp")
    val completionStartDate: String? = null,

    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    @SerialName("questCompId")
    val questionComponentId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

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

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null,

    @SerialName("domPartAdCod")
    val domicilePartialTeachingActivityCode: String? = null,

    @SerialName("partAdCod")
    val partialTeachingActivityCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("annoNascita")
    val birthYear: Int? = null,

    @SerialName("docenteTitId")
    val lecturerTitleId: Long? = null,

    @SerialName("docenteTitMatricola")
    val lecturerTitleMatricola: String? = null,

    @SerialName("docenteTitNome")
    val lecturerTitleName: String? = null,

    @SerialName("docenteTitCognome")
    val lecturerTitleSurname: String? = null,

    @SerialName("docenteTitIdAb")
    val lecturerTitleAbbreviatedId: Long? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("aaCfuStu")
    val academicYearStudentCFU: Long? = null,

    @SerialName("cfuAaStu")
    val studentAcademicYearCredits: Float? = null,

    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("annoCorsoAd")
    val teachingActivityCourseYear: Int? = null,

    @SerialName("dataOra")
    val dateTime: String? = null,

    @SerialName("stuFreqFlg")
    val studentAttendanceFlag: Int? = null
)

@Serializable
data class Esse3TranscriptRowWithQuestionnaireStatus(
    @SerialName("matId")
    val matId: Long,

    @SerialName("ord")
    val order: Int,

    @SerialName("adsceId")
    val activityChoiceId: Long,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("pianoId")
    val planId: Long? = null,

    @SerialName("itmId")
    val itemId: Long? = null,

    @SerialName("ragId")
    val groupId: Long? = null,

    @Serializable(with = Esse3NullableEnumValueSerializer::class)
    @SerialName("raggEsaTipo")
    val graduationGroupType: String? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String,

    @SerialName("annoCorso")
    val courseYear: Int,

    @Serializable(with = Esse3EnumValueSerializer::class)
    @SerialName("stato")
    val state: String,

    @SerialName("statoDes")
    val stateDescription: String,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    @SerialName("tipoEsaDes")
    val graduationTypeDescription: String? = null,

    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null,

    @SerialName("ricId")
    val searchId: Int,

    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    @SerialName("peso")
    val weight: Float,

    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("freqUffFlg")
    val officialAttendanceFlag: Int,

    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int,

    @SerialName("dataScadIscr")
    val enrollmentDeadline: String? = null,

    @SerialName("gruppoVotoId")
    val gradeGroupId: Int? = null,

    @SerialName("gruppoVotoMinVoto")
    val gradeGroupMinGrade: Int? = null,

    @SerialName("gruppoVotoMaxVoto")
    val gradeGroupMaxGrade: Int? = null,

    @SerialName("gruppoVotoLodeFlg")
    val gradeGroupLodeFlag: Int? = null,

    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    @SerialName("gruppoGiudDes")
    val judgmentGroupDescription: String? = null,

    @SerialName("esito")
    val outcome: Esse3Result? = null,

    @SerialName("sovranFlg")
    val overrideFlag: Int,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("debitoFlg")
    val debtFlag: Int,

    @SerialName("ofaFlg")
    val ofaFlag: Int? = null,

    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    @SerialName("genAutoFlg")
    val autoGenerationFlag: Int,

    @SerialName("genRicSpecFlg")
    val generateSpecialRequestFlag: Int,

    @SerialName("tipoOrigEvecar")
    val careerEventOriginType: Int? = null,

    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("infoDottorati")
    val phdInfo: Esse3PhDTranscriptRowInfo? = null,

    @SerialName("rilFreq")
    val attendanceRelease: List<Esse3AttendanceRelease> = emptyList(),

    @SerialName("statoMissione")
    val missionState: String? = null,

    @SerialName("statoMissioneDes")
    val missionStateDescription: String? = null,

    @SerialName("numAppelliPrenotabili")
    val bookableCallsNumber: Int? = null,

    @SerialName("superataFlg")
    val passedFlag: Int? = null,

    @SerialName("numPrenotazioni")
    val bookingNumber: Int? = null,

    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    @SerialName("genConvAdsceId")
    val generateConventionTeachingActivityChoiceId: Long? = null,

    @SerialName("infoInterateneo")
    val interuniversityInfo: Esse3InteruniversityTranscriptRowInfo? = null,

    @SerialName("extraInfo")
    val extraInfo: Esse3ExtraTranscriptRow? = null,

    @SerialName("statoLink")
    val linkState: Int? = null
)

@Serializable
data class Esse3Paragraphs(
    @SerialName("paginaId")
    val pageId: Int? = null,

    @SerialName("paragrafoId")
    val paragraphId: Int? = null,

    @SerialName("elemCod")
    val elementCode: String? = null,

    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    @SerialName("elementiNota")
    val elementsNote: String? = null,

    @SerialName("obbligatorioFlg")
    val mandatoryFlag: Int? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("domande")
    val applications: List<Esse3Applications> = emptyList()
)

@Serializable
data class Esse3QuestionnaireCompiledEventTagsTeachingEvaluation(
    @SerialName("questCompId")
    val questionComponentId: Long,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

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

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null,

    @SerialName("domPartAdCod")
    val domicilePartialTeachingActivityCode: String? = null,

    @SerialName("partAdCod")
    val partialTeachingActivityCode: String? = null,

    @SerialName("tipoCreAdCod")
    val teachingActivityCreditTypeCode: String? = null,

    @SerialName("facAdId")
    val facultyTeachingActivityId: Long? = null,

    @SerialName("facAdCod")
    val facultyTeachingActivityCode: String? = null,

    @SerialName("facAdDes")
    val facultyTeachingActivityDescription: String? = null,

    @SerialName("dipAdId")
    val departmentTeachingActivityId: Long? = null,

    @SerialName("dipAdCod")
    val departmentTeachingActivityCode: String? = null,

    @SerialName("dipAdDes")
    val departmentTeachingActivityDescription: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("facId")
    val facultyId: Long? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("facDes")
    val facultyDescription: String? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("sesso")
    val gender: String? = null,

    @SerialName("annoNascita")
    val birthYear: Int? = null,

    @SerialName("docenteTitId")
    val lecturerTitleId: Long? = null,

    @SerialName("docenteTitMatricola")
    val lecturerTitleMatricola: String? = null,

    @SerialName("docenteTitNome")
    val lecturerTitleName: String? = null,

    @SerialName("docenteTitCognome")
    val lecturerTitleSurname: String? = null,

    @SerialName("docenteTitIdAb")
    val lecturerTitleAbbreviatedId: Long? = null,

    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    @SerialName("nazioneCod")
    val nationCode: String? = null,

    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    @SerialName("provDes")
    val provinceDescription: String? = null,

    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    @SerialName("aaCfuStu")
    val academicYearStudentCFU: Long? = null,

    @SerialName("cfuAaStu")
    val studentAcademicYearCredits: Float? = null,

    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("aaRegId")
    val academicYearRegulationId: Int? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("annoCorsoAd")
    val teachingActivityCourseYear: Int? = null,

    @SerialName("dataOra")
    val dateTime: String? = null,

    @SerialName("stuFreqFlg")
    val studentAttendanceFlag: Int? = null
)

@Serializable
data class Esse3QuestionnaireCompiledEventTagsTeacherAvailability(
    @SerialName("questCompId")
    val questionComponentId: Long,

    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    @SerialName("docenteNome")
    val lecturerName: String? = null,

    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null,

    @SerialName("dipId")
    val departmentId: Long? = null,

    @SerialName("dipCod")
    val departmentCode: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

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

    @SerialName("adId")
    val activityId: Long? = null,

    @SerialName("adCod")
    val activityCode: String? = null,

    @SerialName("adDes")
    val activityDescription: String? = null,

    @SerialName("udId")
    val teachingUnitId: Long? = null,

    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    @SerialName("domPartAdCod")
    val domicilePartialTeachingActivityCode: String? = null,

    @SerialName("adLogId")
    val activityLogId: Long? = null,

    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("dataOra")
    val dateTime: String? = null
)

@Serializable
data class Esse3QuestionnaireSummary(
    @SerialName("questCompId")
    val questionComponentId: Int? = null,

    @SerialName("questionarioId")
    val questionnaireId: Int? = null,

    @SerialName("stato")
    val state: String? = null,

    @SerialName("completoFlg")
    val completeFlag: String? = null,

    @SerialName("eventoCompCod")
    val eventComponentCode: String? = null,

    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    @SerialName("pQuestionarioDes")
    val questionnaireDescription: String? = null,

    @SerialName("pQuestionarioNote")
    val questionnaireNote: String? = null,

    @SerialName("pPrimaPaginaId")
    val firstPageId: Int? = null,

    @SerialName("tagsWeb")
    val webTags: List<Esse3WebSummaryTags> = emptyList(),

    @SerialName("pagine")
    val pages: List<Esse3SummaryPages> = emptyList()
)

@Serializable
data class Esse3AnswerDomains(
    @SerialName("quesitoId")
    val questionId: Int? = null,

    @SerialName("operandoDomId")
    val domicileOperandId: Int? = null,

    @SerialName("numMaxValori")
    val maxValuesNumber: Int? = null,

    @SerialName("questCompId")
    val questionComponentId: Int? = null,

    @SerialName("desValDom")
    val domicileEvaluationDescription: String? = null
)
