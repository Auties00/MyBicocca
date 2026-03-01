package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3CompetitionEnrolledDetail(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("persId")
    val personId: Long,

    @SerialName("posId")
    val positionId: Long,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("modTest")
    val testMode: String? = null,

    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    @SerialName("tipoTestDesEng")
    val testTypeDescriptionEnglish: String? = null,

    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    @SerialName("dataIniDomRipescaggio")
    val recoveryApplicationStartDate: String? = null,

    @SerialName("dataFinDomRipescaggio")
    val recoveryApplicationEndDate: String? = null,

    @SerialName("concNazionale")
    val nationalCompetition: Long? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("concorsoNotaEng")
    val competitionNoteEnglish: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("taxPagFlg")
    val taxPaymentFlag: Int? = null,

    @SerialName("linguaConcorso")
    val competitionLanguage: List<Esse3CompetitionLanguage> = emptyList(),

    @SerialName("dettaglioSede")
    val siteDetail: List<Esse3LocationDetail> = emptyList(),

    @SerialName("dettaglioPds")
    val pdsDetail: List<Esse3StudyPlanDetail> = emptyList(),

    @SerialName("borse")
    val scholarships: List<Esse3ScholarshipCompetition> = emptyList(),

    @SerialName("dettaglioTest")
    val testDetail: List<Esse3TestDetail> = emptyList()
)

@Serializable
data class Esse3PreferencesDetailInsert(
    @SerialName("dettTestId")
    val testDetailId: Long,

    @SerialName("prefOrd")
    val orderPreferences: Long
)

@Serializable
data class Esse3CompetitionTestsTeacher(
    @SerialName("provaId")
    val examTestId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("livello")
    val level: String? = null,

    @SerialName("dataProva")
    val testDate: String? = null,

    @SerialName("dataEsiti")
    val outcomesDate: String? = null,

    @SerialName("dataFinEsiti")
    val outcomesEndDate: String? = null,

    @SerialName("studentidavalutare")
    val studentIdToEvaluate: Int? = null,

    @SerialName("studentiValutati")
    val evaluatedStudents: Int? = null,

    @SerialName("commConcId")
    val competitionCommitteeId: Long? = null,

    @SerialName("urliscritti")
    val enrolledUrl: String? = null,

    @SerialName("provaCod")
    val testCode: String? = null,

    @SerialName("provaDes")
    val testDescription: String? = null
)

@Serializable
data class Esse3Preferences(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("prefOrd")
    val orderPreferences: Int? = null,

    @SerialName("posId")
    val positionId: Long? = null
)

@Serializable
data class Esse3StudentTestsList(
    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    @SerialName("provaCod")
    val testCode: String? = null,

    @SerialName("provaDes")
    val testDescription: String? = null,

    @SerialName("livello")
    val level: String? = null,

    @SerialName("dataProva")
    val testDate: String? = null,

    @SerialName("orarioProva")
    val testSchedule: String? = null,

    @SerialName("ordine")
    val order: Int? = null,

    @SerialName("visStu")
    val studentVisible: Int? = null,

    @SerialName("dataEsiti")
    val outcomesDate: String? = null,

    @SerialName("dataFinEsiti")
    val outcomesEndDate: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("giudizio")
    val judgment: String? = null,

    @SerialName("puntiMin")
    val minimumPoints: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    @SerialName("esito")
    val outcome: String? = null
)

@Serializable
data class Esse3SubjectsRanking(
    @SerialName("classifDettId")
    val classificationDetailId: Long? = null,

    @SerialName("classificaMateriaId")
    val subjectRankingId: Long? = null,

    @SerialName("turniConcId")
    val competitionShiftsId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("posId")
    val positionId: Long? = null,

    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    @SerialName("giudizio")
    val judgment: String? = null,

    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    @SerialName("materiaFlg")
    val subjectFlag: Long? = null
)

@Serializable
data class Esse3PersonSubjectsRanking(
    @SerialName("classifDettId")
    val classificationDetailId: Long? = null,

    @SerialName("classificaMateriaId")
    val subjectRankingId: Long? = null,

    @SerialName("turniConcId")
    val competitionShiftsId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("posId")
    val positionId: Long? = null,

    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    @SerialName("giudizio")
    val judgment: String? = null,

    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    @SerialName("materiaFlg")
    val subjectFlag: Long? = null
)

@Serializable
data class Esse3ScholarshipPreferencesDetail(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("concBorseId")
    val scholarshipCompetitionId: Long? = null,

    @SerialName("tipoBorsaCod")
    val scholarshipTypeCode: String? = null,

    @SerialName("tipoBorsaDes")
    val scholarshipTypeDescription: String? = null,

    @SerialName("numBorse")
    val scholarshipNumber: Long? = null
)

@Serializable
data class Esse3Subjects(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("materiaId")
    val subjectId: Long? = null,

    @SerialName("provaDes")
    val testDescription: String? = null,

    @SerialName("provaDesEng")
    val testDescriptionEnglish: String? = null,

    @SerialName("dataProva")
    val testDate: String? = null,

    @SerialName("orario")
    val schedule: String? = null,

    @SerialName("dataIniEsiti")
    val outcomesStartDate: String? = null,

    @SerialName("dataFinEsiti")
    val outcomesEndDate: String? = null,

    @SerialName("ordVis")
    val orderVisible: Long? = null,

    @SerialName("proveConcId")
    val competitionTestsId: Long? = null
)

@Serializable
data class Esse3RankingImport(
    @SerialName("prematr")
    val preMatricola: Long? = null,

    @SerialName("numCompito")
    val taskNumber: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("puntiTot")
    val totalPoints: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    @SerialName("esitoCod")
    val outcomeCode: Long? = null,

    @SerialName("puntiMaterie")
    val subjectPoints: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

@Serializable
data class Esse3CompetitionTests(
    @SerialName("aaId")
    val academicYearId: Long,

    @SerialName("testId")
    val testId: Long,

    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    @SerialName("provaDes")
    val testDescription: String? = null,

    @SerialName("provaDesEng")
    val testDescriptionEnglish: String? = null,

    @SerialName("dataProva")
    val testDate: String? = null,

    @SerialName("orario")
    val schedule: String? = null,

    @SerialName("dataIniEsiti")
    val outcomesStartDate: String? = null,

    @SerialName("dataFinEsiti")
    val outcomesEndDate: String? = null,

    @SerialName("ordVis")
    val orderVisible: Long? = null,

    @SerialName("materie")
    val subjects: List<Esse3Subjects> = emptyList()
)

@Serializable
data class Esse3ImportResponse(
    @SerialName("numeroRecordElaborati")
    val processedRecordsNumber: Long? = null,

    @SerialName("numeroErrori")
    val errorNumber: Long? = null,

    @SerialName("elencoErrori")
    val errorList: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

@Serializable
data class Esse3CompetitionCommissionTeacher(
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    @SerialName("commConcId")
    val competitionCommitteeId: Long? = null,

    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("testId")
    val testId: Int? = null,

    @SerialName("concDes")
    val competitionDescription: String? = null,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    @SerialName("urlprove")
    val testsUrl: String? = null,

    @SerialName("prove")
    val tests: List<Esse3CompetitionTestsTeacher> = emptyList()
)

@Serializable
data class Esse3PersonRanking(
    @SerialName("classifDettId")
    val classificationDetailId: Long? = null,

    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    @SerialName("turniConcId")
    val competitionShiftsId: Long? = null,

    @SerialName("persId")
    val personId: Long,

    @SerialName("posId")
    val positionId: Long,

    @SerialName("provaDestId")
    val testDestinationId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    @SerialName("giudizio")
    val judgment: String? = null,

    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    @SerialName("materiaFlg")
    val subjectFlag: Long? = null,

    @SerialName("esitoProvaOrigine")
    val originTestOutcome: List<Esse3PersonSubjectsRanking> = emptyList()
)

@Serializable
data class Esse3StudyPlanPreferencesDetail(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null
)

@Serializable
data class Esse3LocationPreferencesDetail(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("postiDettSedeId")
    val seatDetailSiteId: Long? = null
)

@Serializable
data class Esse3StaffOutput(
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("cdsConfig")
    val courseOfStudyConfig: Int? = null,

    @SerialName("listaConc")
    val competitionList: List<Esse3CompetitionStaffList> = emptyList()
)

@Serializable
data class Esse3CourseListForStaff(
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("claMId")
    val classMId: Int? = null,

    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    @SerialName("descrizioneClasse")
    val classDescription: String? = null
)

@Serializable
data class Esse3ScholarshipPreferencesInsert(
    @SerialName("concBorseId")
    val scholarshipCompetitionId: Long,

    @SerialName("prefOrd")
    val orderPreferences: Long
)

@Serializable
data class Esse3CompetitionStaffList(
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    @SerialName("dataScad")
    val deadline: String? = null,

    @SerialName("dataScadPreim")
    val preEnrollmentDeadline: String? = null,

    @SerialName("listaCds")
    val courseOfStudyList: List<Esse3CourseListForStaff> = emptyList()
)

@Serializable
data class Esse3Competition(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("modTest")
    val testMode: String? = null,

    @SerialName("numPrefMin")
    val minPreferenceNumber: Long? = null,

    @SerialName("numPrefMax")
    val maxPreferenceNumber: Long? = null,

    @SerialName("numPrefLingueMin")
    val minLanguagePreferenceNumber: Long? = null,

    @SerialName("numPrefLingue")
    val languagePreferenceNumber: Long? = null,

    @SerialName("numPrefBorseMin")
    val minScholarshipPreferenceNumber: Long? = null,

    @SerialName("numPrefBorse")
    val scholarshipPreferenceNumber: Long? = null,

    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("concorsoNotaEng")
    val competitionNoteEnglish: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("linkBando")
    val callAnnouncementLink: String? = null
)

@Serializable
data class Esse3CompetitionEnrolled(
    @SerialName("aaId")
    val academicYearId: Long,

    @SerialName("testId")
    val testId: Long,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("posId")
    val positionId: Long? = null,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("modTest")
    val testMode: String? = null,

    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    @SerialName("tipoTestDesEng")
    val testTypeDescriptionEnglish: String? = null,

    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    @SerialName("dataIniDomRipescaggio")
    val recoveryApplicationStartDate: String? = null,

    @SerialName("dataFinDomRipescaggio")
    val recoveryApplicationEndDate: String? = null,

    @SerialName("concNazionale")
    val nationalCompetition: Long? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("concorsoNotaEng")
    val competitionNoteEnglish: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("taxPagFlg")
    val taxPaymentFlag: Int? = null
)

@Serializable
data class Esse3CompetitionRanking(
    @SerialName("classifDettId")
    val classificationDetailId: Long? = null,

    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    @SerialName("turniConcId")
    val competitionShiftsId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("posId")
    val positionId: Long? = null,

    @SerialName("provaDestId")
    val testDestinationId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long,

    @SerialName("testId")
    val testId: Long,

    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    @SerialName("giudizio")
    val judgment: String? = null,

    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    @SerialName("materiaFlg")
    val subjectFlag: Long? = null,

    @SerialName("esitiProvaOrigine")
    val originTestOutcomes: List<Esse3SubjectsRanking> = emptyList()
)

@Serializable
data class Esse3CompetitionAdmissionInsert(
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Int? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("trattHandFlg")
    val handicapTreatmentFlag: Int? = null,

    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    @SerialName("noteHand")
    val handicapNotes: String? = null,

    @SerialName("percHand")
    val handicapPercentage: Float? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("prefLingue")
    val languagePreferences: List<Esse3LanguagePreferencesInsert> = emptyList(),

    @SerialName("prefBorse")
    val scholarshipPreferences: List<Esse3ScholarshipPreferencesInsert> = emptyList(),

    @SerialName("prefDett")
    val preferenceDetails: List<Esse3PreferencesDetailInsert> = emptyList()
)

@Serializable
data class Esse3LanguagePreferencesInsert(
    @SerialName("concLingueId")
    val languagesCompetitionId: Long,

    @SerialName("prefOrd")
    val orderPreferences: Long
)

@Serializable
data class Esse3CompetitionTestDetail(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("dettaglio")
    val detail: String? = null,

    @SerialName("dettaglioEng")
    val detailEnglish: String? = null,

    @SerialName("numPostiTot")
    val totalSeatsNumber: Long? = null,

    @SerialName("numPostiLiberi")
    val freeSeatsNumber: Long? = null,

    @SerialName("dataIniVisGradWeb")
    val webGraduationViewStartDate: String? = null,

    @SerialName("dataFinVisGradWeb")
    val webGraduationViewEndDate: String? = null,

    @SerialName("annoCorso")
    val courseYear: Long? = null,

    @SerialName("prefPds")
    val studyPlanPreferences: List<Esse3StudyPlanPreferencesDetail> = emptyList(),

    @SerialName("prefSedi")
    val sitePreferences: List<Esse3LocationPreferencesDetail> = emptyList()
)

@Serializable
data class Esse3LanguagePreferencesDetail(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("concLingueId")
    val languagesCompetitionId: Long? = null,

    @SerialName("linguaId")
    val languageId: Long? = null,

    @SerialName("linguaDes")
    val languageDescription: String? = null,

    @SerialName("iso6392Cod")
    val iso6392Code: String? = null
)

@Serializable
data class Esse3CompetitionWithDetails(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("modTest")
    val testMode: String? = null,

    @SerialName("numPrefMin")
    val minPreferenceNumber: Long? = null,

    @SerialName("numPrefMax")
    val maxPreferenceNumber: Long? = null,

    @SerialName("numPrefLingueMin")
    val minLanguagePreferenceNumber: Long? = null,

    @SerialName("numPrefLingue")
    val languagePreferenceNumber: Long? = null,

    @SerialName("numPrefBorseMin")
    val minScholarshipPreferenceNumber: Long? = null,

    @SerialName("numPrefBorse")
    val scholarshipPreferenceNumber: Long? = null,

    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("concorsoNotaEng")
    val competitionNoteEnglish: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    @SerialName("linkBando")
    val callAnnouncementLink: String? = null,

    @SerialName("dettTest")
    val testDetail: List<Esse3CompetitionTestDetail> = emptyList(),

    @SerialName("prefLingue")
    val languagePreferences: List<Esse3LanguagePreferencesDetail> = emptyList(),

    @SerialName("prefBorse")
    val scholarshipPreferences: List<Esse3ScholarshipPreferencesDetail> = emptyList()
)

@Serializable
data class Esse3CompetitionLanguage(
    @SerialName("persId")
    val personId: Long,

    @SerialName("posId")
    val positionId: Long,

    @SerialName("concLingueId")
    val languagesCompetitionId: Long? = null,

    @SerialName("prefOrd")
    val orderPreferences: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("linguaId")
    val languageId: Long? = null
)

@Serializable
data class Esse3CompetitionStudentList(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("posId")
    val positionId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("prematr")
    val preMatricola: Long? = null,

    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    @SerialName("dataScad")
    val deadline: String? = null,

    @SerialName("dataScadPreim")
    val preEnrollmentDeadline: String? = null,

    @SerialName("dataIniDomRipescaggio")
    val recoveryApplicationStartDate: String? = null,

    @SerialName("dataFinDomRipescaggio")
    val recoveryApplicationEndDate: String? = null,

    @SerialName("tassaFlg")
    val taxFlag: Int? = null,

    @SerialName("taxPagFlg")
    val taxPaymentFlag: Int? = null,

    @SerialName("listaCds")
    val courseOfStudyList: List<Esse3CourseListForStudents> = emptyList(),

    @SerialName("preferenze")
    val preferences: List<Esse3Preferences> = emptyList(),

    @SerialName("listaProve")
    val testList: List<Esse3StudentTestsList> = emptyList()
)

@Serializable
data class Esse3ScholarshipCompetition(
    @SerialName("persId")
    val personId: Long,

    @SerialName("posId")
    val positionId: Long,

    @SerialName("concBorseId")
    val scholarshipCompetitionId: Long? = null,

    @SerialName("tipoBorsaCod")
    val scholarshipTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3CourseListForStudents(
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("claMId")
    val classMId: Long? = null,

    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    @SerialName("descrizioneClasse")
    val classDescription: String? = null,

    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("pdsOrdDes")
    val studyPlanOrderDescription: String? = null,

    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("sedeDes")
    val siteDescription: String? = null,

    @SerialName("annoCorso")
    val courseYear: Int? = null,

    @SerialName("numeroPostiTot")
    val totalSeatsNumber: Int? = null,

    @SerialName("numeroPostiLiberi")
    val freeSeatsNumber: Int? = null,

    @SerialName("dataIniVisGradWeb")
    val webGraduationViewStartDate: String? = null,

    @SerialName("dataFinVisGradWeb")
    val webGraduationViewEndDate: String? = null,

    @SerialName("dataScad")
    val deadline: String? = null,

    @SerialName("dataScadPreim")
    val preEnrollmentDeadline: String? = null,

    @SerialName("dataIniDomRipescaggio")
    val recoveryApplicationStartDate: String? = null,

    @SerialName("dataFinDomRipescaggio")
    val recoveryApplicationEndDate: String? = null
)

@Serializable
data class Esse3RankingListImport(
    @SerialName("prematr")
    val preMatricola: Long? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("posiz")
    val position: Long? = null,

    @SerialName("puntiTot")
    val totalPoints: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    @SerialName("stato")
    val state: String? = null
)

@Serializable
data class Esse3TestDetail(
    @SerialName("persId")
    val personId: Long,

    @SerialName("posId")
    val positionId: Long,

    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("prefOrd")
    val orderPreferences: Long? = null,

    @SerialName("dettaglio")
    val detail: String? = null,

    @SerialName("dettaglioEng")
    val detailEnglish: String? = null
)

@Serializable
data class Esse3LocationDetail(
    @SerialName("persId")
    val personId: Long,

    @SerialName("posId")
    val positionId: Long,

    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("sedeId")
    val siteId: Long? = null,

    @SerialName("prefOrd")
    val orderPreferences: Long? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3StudyPlanDetail(
    @SerialName("persId")
    val personId: Long,

    @SerialName("posId")
    val positionId: Long,

    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("cod")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3CompetitionRankingList(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("testId")
    val testId: Long? = null,

    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("righeGradId")
    val graduationRowsId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("posId")
    val positionId: Long? = null,

    @SerialName("statoCod")
    val stateCode: String? = null,

    @SerialName("statoGradDes")
    val graduationStateDescription: String? = null,

    @SerialName("statoGradDesEng")
    val graduationStateDescriptionEnglish: String? = null,

    @SerialName("posiz")
    val position: Long? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    @SerialName("giudizio")
    val judgment: String? = null,

    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    @SerialName("presDomRipescaggio")
    val recoveryApplicationPresence: Long? = null,

    @SerialName("dataDomRipescaggio")
    val recoveryApplicationDate: String? = null,

    @SerialName("dataScadPosAm")
    val adminPositionDeadline: String? = null,

    @SerialName("dataScadPosPi")
    val piPositionDeadline: String? = null
)
