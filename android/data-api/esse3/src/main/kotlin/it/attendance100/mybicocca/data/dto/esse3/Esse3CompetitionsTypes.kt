package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3CompetitionEnrolledDetail(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo persona iscritta */
    @SerialName("persId")
    val personId: Long = 0L,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long = 0L,

    /** Tipo di concorso: A = test di ammissione ad un corso di studio E = Esame di stato S = concorso per accedere ad una SSIS V = test di valutazione conoscenze del candidato */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Indica la modalità di svolgimento del concorso, test di valutazione, esame di stato: - E: ad esaurimento posti (ordine di presentazione della domanda di immatricolazione) - U: basato sul superamento di una prova unica - M: basato sul superamento di prove multiple - D: basato sull´ordine di presentazione della domanda di ammissione al concorso. */
    @SerialName("modTest")
    val testMode: String? = null,

    /** Descrizione tipologia concorso */
    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    /** Descrizione in lingua inglese della tipologia concorso */
    @SerialName("tipoTestDesEng")
    val testTypeDescriptionEnglish: String? = null,

    /** Descrizione del concorso. */
    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    /** Descrizione in lingua inglese del concorso. */
    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    /** Data di inizio del periodo valido per il ripescaggio al concorso. */
    @SerialName("dataIniDomRipescaggio")
    val recoveryApplicationStartDate: String? = null,

    /** Data di fine del periodo valido per il ripescaggio al concorso. */
    @SerialName("dataFinDomRipescaggio")
    val recoveryApplicationEndDate: String? = null,

    /** flag che indica se il concorso è nazionale o meno */
    @SerialName("concNazionale")
    val nationalCompetition: Long? = null,

    /** Nota associata al concorso */
    @SerialName("nota")
    val note: String? = null,

    /** Nota associata al concorso. */
    @SerialName("concorsoNotaEng")
    val competitionNoteEnglish: String? = null,

    /** Tipo di corso di studio a cui è associato il concorso, test di valutazione, esame di stato. Per gli esami di stato assume valori nulli. */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Descrizione tipologia corso di studio associata ala concorso */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** Descrizione in lingua inglese della tipologia corso di studio associata ala concorso */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** Indica se il candidato ha pagato o meno la tassa di iscrizione al concorso */
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
    /** Identificativo univoco dettaglio preferenza */
    @SerialName("dettTestId")
    val testDetailId: Long = 0L,

    /** Ordine di preferenza. */
    @SerialName("prefOrd")
    val orderPreferences: Long = 0L
)

@Serializable
data class Esse3CompetitionTestsTeacher(
    /** Identificativo della prova */
    @SerialName("provaId")
    val examTestId: Long? = null,

    /** Identificativo anno */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** Identificativo del soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** livello della prova */
    @SerialName("livello")
    val level: String? = null,

    /** Data della prova */
    @SerialName("dataProva")
    val testDate: String? = null,

    /** Data esiti */
    @SerialName("dataEsiti")
    val outcomesDate: String? = null,

    /** Data fine esiti */
    @SerialName("dataFinEsiti")
    val outcomesEndDate: String? = null,

    /** numero di studenti da valutare */
    @SerialName("studentidavalutare")
    val studentIdToEvaluate: Int? = null,

    /** numero di studenti valutabili */
    @SerialName("studentiValutati")
    val evaluatedStudents: Int? = null,

    /** Identificativo della commissione */
    @SerialName("commConcId")
    val competitionCommitteeId: Long? = null,

    /** url degli iscritti alla prova */
    @SerialName("urliscritti")
    val enrolledUrl: String? = null,

    /** Codice prova concorso */
    @SerialName("provaCod")
    val testCode: String? = null,

    /** Descrizione prova concorso */
    @SerialName("provaDes")
    val testDescription: String? = null
)

@Serializable
data class Esse3Preferences(
    /** Identificativo della persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo dell dettaglio del concorso */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    @SerialName("prefOrd")
    val orderPreferences: Int? = null,

    /** Identificativo della scelta */
    @SerialName("posId")
    val positionId: Long? = null
)

@Serializable
data class Esse3StudentTestsList(
    /** Identificativo anno */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo della prova concorso */
    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    /** Codice prova concorso */
    @SerialName("provaCod")
    val testCode: String? = null,

    /** Codice prova concorso */
    @SerialName("provaDes")
    val testDescription: String? = null,

    /** Codice prova concorso */
    @SerialName("livello")
    val level: String? = null,

    /** Data della prova. */
    @SerialName("dataProva")
    val testDate: String? = null,

    /** Data della prova. */
    @SerialName("orarioProva")
    val testSchedule: String? = null,

    /** ordine della prova */
    @SerialName("ordine")
    val order: Int? = null,

    /** vis stu */
    @SerialName("visStu")
    val studentVisible: Int? = null,

    /** Data della prova. */
    @SerialName("dataEsiti")
    val outcomesDate: String? = null,

    /** Data della prova. */
    @SerialName("dataFinEsiti")
    val outcomesEndDate: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    /** giudizio */
    @SerialName("giudizio")
    val judgment: String? = null,

    @SerialName("puntiMin")
    val minimumPoints: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    /** giudizio */
    @SerialName("esito")
    val outcome: String? = null
)

@Serializable
data class Esse3SubjectsRanking(
    /** Identificativo della classifica */
    @SerialName("classifDettId")
    val classificationDetailId: Long? = null,

    /** Identificativo della prova */
    @SerialName("classificaMateriaId")
    val subjectRankingId: Long? = null,

    /** Identificativo del turno */
    @SerialName("turniConcId")
    val competitionShiftsId: Long? = null,

    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long? = null,

    /** Identificativo della prova padre */
    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Descrizione della prova */
    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    /** Descrizione della prova */
    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    /** giudizio */
    @SerialName("giudizio")
    val judgment: String? = null,

    /** giudizio in lingua inglese */
    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    /** indica se la prova è una materia o meno */
    @SerialName("materiaFlg")
    val subjectFlag: Long? = null
)

@Serializable
data class Esse3PersonSubjectsRanking(
    /** Identificativo della classifica */
    @SerialName("classifDettId")
    val classificationDetailId: Long? = null,

    /** Identificativo della prova */
    @SerialName("classificaMateriaId")
    val subjectRankingId: Long? = null,

    /** Identificativo del turno */
    @SerialName("turniConcId")
    val competitionShiftsId: Long? = null,

    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long? = null,

    /** Identificativo della prova padre */
    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Descrizione della prova */
    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    /** Descrizione della prova */
    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    /** giudizio */
    @SerialName("giudizio")
    val judgment: String? = null,

    /** giudizio in lingua inglese */
    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    /** indica se la prova è una materia o meno */
    @SerialName("materiaFlg")
    val subjectFlag: Long? = null
)

@Serializable
data class Esse3ScholarshipPreferencesDetail(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo preferenza borsa di studio */
    @SerialName("concBorseId")
    val scholarshipCompetitionId: Long? = null,

    /** Codice borsa di studio */
    @SerialName("tipoBorsaCod")
    val scholarshipTypeCode: String? = null,

    /** Descrizione borsa di studio */
    @SerialName("tipoBorsaDes")
    val scholarshipTypeDescription: String? = null,

    /** Numero di borse di studio disponibili */
    @SerialName("numBorse")
    val scholarshipNumber: Long? = null
)

@Serializable
data class Esse3Subjects(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo della prova */
    @SerialName("materiaId")
    val subjectId: Long? = null,

    /** Descrizione della prova */
    @SerialName("provaDes")
    val testDescription: String? = null,

    /** Dettaglio del concorso */
    @SerialName("provaDesEng")
    val testDescriptionEnglish: String? = null,

    /** Dettaglio lingua inglese */
    @SerialName("dataProva")
    val testDate: String? = null,

    /** orario della seduta di laurea (HH24:MI) */
    @SerialName("orario")
    val schedule: String? = null,

    /** Dettaglio lingua inglese */
    @SerialName("dataIniEsiti")
    val outcomesStartDate: String? = null,

    /** Dettaglio lingua inglese */
    @SerialName("dataFinEsiti")
    val outcomesEndDate: String? = null,

    /** Identificativo del dettaglio */
    @SerialName("ordVis")
    val orderVisible: Long? = null,

    /** Identificativo della prova padre */
    @SerialName("proveConcId")
    val competitionTestsId: Long? = null
)

@Serializable
data class Esse3RankingImport(
    /** Indentifica la prematricola */
    @SerialName("prematr")
    val preMatricola: Long? = null,

    /** Giudizio in lingua inglese */
    @SerialName("numCompito")
    val taskNumber: String? = null,

    /** Giudizio in lingua inglese */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Giudizio in lingua inglese */
    @SerialName("nome")
    val name: String? = null,

    /** Giudizio in lingua inglese */
    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("puntiTot")
    val totalPoints: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    /** Indentifica la prematricola */
    @SerialName("esitoCod")
    val outcomeCode: Long? = null,

    @SerialName("puntiMaterie")
    val subjectPoints: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

@Serializable
data class Esse3CompetitionTests(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long = 0L,

    /** Identificativo posizione */
    @SerialName("testId")
    val testId: Long = 0L,

    /** Identificativo della prova */
    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    /** Descrizione della prova */
    @SerialName("provaDes")
    val testDescription: String? = null,

    /** Dettaglio del concorso */
    @SerialName("provaDesEng")
    val testDescriptionEnglish: String? = null,

    /** Dettaglio lingua inglese */
    @SerialName("dataProva")
    val testDate: String? = null,

    /** orario della seduta di laurea (HH24:MI) */
    @SerialName("orario")
    val schedule: String? = null,

    /** Dettaglio lingua inglese */
    @SerialName("dataIniEsiti")
    val outcomesStartDate: String? = null,

    /** Dettaglio lingua inglese */
    @SerialName("dataFinEsiti")
    val outcomesEndDate: String? = null,

    /** Identificativo del dettaglio */
    @SerialName("ordVis")
    val orderVisible: Long? = null,

    @SerialName("materie")
    val subjects: List<Esse3Subjects> = emptyList()
)

@Serializable
data class Esse3ImportResponse(
    /** numero di record elaborati correttamente */
    @SerialName("numeroRecordElaborati")
    val processedRecordsNumber: Long? = null,

    /** numero di record che sono andati in errore */
    @SerialName("numeroErrori")
    val errorNumber: Long? = null,

    @SerialName("elencoErrori")
    val errorList: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

@Serializable
data class Esse3CompetitionCommissionTeacher(
    /** Identificativo docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** Identificativo della commissione */
    @SerialName("commConcId")
    val competitionCommitteeId: Long? = null,

    /** Identificativo del soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** Identificativo anno */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Identificativo concorso */
    @SerialName("testId")
    val testId: Int? = null,

    /** descrizione concorso */
    @SerialName("concDes")
    val competitionDescription: String? = null,

    /** Codice tipoligia concorso */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Descrizione tipoligia concorso */
    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    /** url web delle prove */
    @SerialName("urlprove")
    val testsUrl: String? = null,

    @SerialName("prove")
    val tests: List<Esse3CompetitionTestsTeacher> = emptyList()
)

@Serializable
data class Esse3PersonRanking(
    /** Identificativo della classifica */
    @SerialName("classifDettId")
    val classificationDetailId: Long? = null,

    /** Identificativo della prova */
    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    /** Identificativo del turno */
    @SerialName("turniConcId")
    val competitionShiftsId: Long? = null,

    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long = 0L,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long = 0L,

    /** Identificativo della prova padre */
    @SerialName("provaDestId")
    val testDestinationId: Long? = null,

    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Descrizione della prova */
    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    /** Descrizione della prova */
    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    /** giudizio */
    @SerialName("giudizio")
    val judgment: String? = null,

    /** giudizio in lingua inglese */
    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    /** indica se la prova è una materia o meno */
    @SerialName("materiaFlg")
    val subjectFlag: Long? = null,

    @SerialName("esitoProvaOrigine")
    val originTestOutcome: List<Esse3PersonSubjectsRanking> = emptyList()
)

@Serializable
data class Esse3StudyPlanPreferencesDetail(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo dettaglio della graduatoria del concorso. */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    /** Codice percorso di studio */
    @SerialName("cod")
    val code: String? = null,

    /** Descrizione percorso di studio */
    @SerialName("des")
    val description: String? = null,

    /** Identificativo corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Anno ordinamento corso di studio */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** Identificativo percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null
)

@Serializable
data class Esse3LocationPreferencesDetail(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo dettaglio della graduatoria del concorso. */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    /** Descrizione sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** Identificativo sede */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** Posti disponibili per la sede */
    @SerialName("postiDettSedeId")
    val seatDetailSiteId: Long? = null
)

@Serializable
data class Esse3StaffOutput(
    /** Codice della tipologia del corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Descrizione della tipologia del corso */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    @SerialName("cdsConfig")
    val courseOfStudyConfig: Int? = null,

    @SerialName("listaConc")
    val competitionList: List<Esse3CompetitionStaffList> = emptyList()
)

@Serializable
data class Esse3CourseListForStaff(
    /** Codice della tipologia del corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Descrizione della tipologia del corso */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo anno */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Codice del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** Descrizione del corso di studio */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** Identificativo del corso di studio */
    @SerialName("claMId")
    val classMId: Int? = null,

    /** Codice CLA MURST */
    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    /** Descrizione classe */
    @SerialName("descrizioneClasse")
    val classDescription: String? = null
)

@Serializable
data class Esse3ScholarshipPreferencesInsert(
    /** Identificativo univoco borsa di preferenza */
    @SerialName("concBorseId")
    val scholarshipCompetitionId: Long = 0L,

    /** Ordine di preferenza. */
    @SerialName("prefOrd")
    val orderPreferences: Long = 0L
)

@Serializable
data class Esse3CompetitionStaffList(
    /** Codice della tipologia del corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Codice raggruppamento tipo corso di studio */
    @SerialName("gruppoTcCod")
    val tcGroupCode: String? = null,

    /** Identificativo anno */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Descrizione del concorso */
    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    /** Descrizione del concorso */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Descrizione della tipologia del concorso */
    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    /** Data scadenza della posizione utile (Ammesso) in graduatoria ai fini della preimmatricolazione */
    @SerialName("dataScad")
    val deadline: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataScadPreim")
    val preEnrollmentDeadline: String? = null,

    @SerialName("listaCds")
    val courseOfStudyList: List<Esse3CourseListForStaff> = emptyList()
)

@Serializable
data class Esse3Competition(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Tipo di concorso: A = test di ammissione ad un corso di studio E = Esame di stato S = concorso per accedere ad una SSIS V = test di valutazione conoscenze del candidato */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Indica la modalità di svolgimento del concorso, test di valutazione, esame di stato: - E: ad esaurimento posti (ordine di presentazione della domanda di immatricolazione) - U: basato sul superamento di una prova unica - M: basato sul superamento di prove multiple - D: basato sull´ordine di presentazione della domanda di ammissione al concorso. */
    @SerialName("modTest")
    val testMode: String? = null,

    /** numero minimo preferenze */
    @SerialName("numPrefMin")
    val minPreferenceNumber: Long? = null,

    /** numero massimo preferenze */
    @SerialName("numPrefMax")
    val maxPreferenceNumber: Long? = null,

    /** numero minimo preferenze di lingue */
    @SerialName("numPrefLingueMin")
    val minLanguagePreferenceNumber: Long? = null,

    /** numero massimo preferenze di lingue */
    @SerialName("numPrefLingue")
    val languagePreferenceNumber: Long? = null,

    /** numero minimo borse di studio */
    @SerialName("numPrefBorseMin")
    val minScholarshipPreferenceNumber: Long? = null,

    /** numero massimo borse di studio */
    @SerialName("numPrefBorse")
    val scholarshipPreferenceNumber: Long? = null,

    /** Descrizione tipologia concorso */
    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    /** Descrizione del concorso. */
    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    /** Descrizione in lingua inglese del concorso. */
    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    /** Nota associata al concorso */
    @SerialName("nota")
    val note: String? = null,

    /** Nota associata al concorso. */
    @SerialName("concorsoNotaEng")
    val competitionNoteEnglish: String? = null,

    /** Tipo di corso di studio a cui è associato il concorso, test di valutazione, esame di stato. Per gli esami di stato assume valori nulli. */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Descrizione tipologia corso di studio associata ala concorso */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** Descrizione in lingua inglese della tipologia corso di studio associata ala concorso */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** Link al bando del concorso. */
    @SerialName("linkBando")
    val callAnnouncementLink: String? = null
)

@Serializable
data class Esse3CompetitionEnrolled(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long = 0L,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long = 0L,

    /** Identificativo persona iscritta */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long? = null,

    /** Tipo di concorso: A = test di ammissione ad un corso di studio E = Esame di stato S = concorso per accedere ad una SSIS V = test di valutazione conoscenze del candidato */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Indica la modalità di svolgimento del concorso, test di valutazione, esame di stato: - E: ad esaurimento posti (ordine di presentazione della domanda di immatricolazione) - U: basato sul superamento di una prova unica - M: basato sul superamento di prove multiple - D: basato sull´ordine di presentazione della domanda di ammissione al concorso. */
    @SerialName("modTest")
    val testMode: String? = null,

    /** Descrizione tipologia concorso */
    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    /** Descrizione in lingua inglese della tipologia concorso */
    @SerialName("tipoTestDesEng")
    val testTypeDescriptionEnglish: String? = null,

    /** Descrizione del concorso. */
    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    /** Descrizione in lingua inglese del concorso. */
    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    /** Data di inizio del periodo valido per il ripescaggio al concorso. */
    @SerialName("dataIniDomRipescaggio")
    val recoveryApplicationStartDate: String? = null,

    /** Data di fine del periodo valido per il ripescaggio al concorso. */
    @SerialName("dataFinDomRipescaggio")
    val recoveryApplicationEndDate: String? = null,

    /** flag che indica se il concorso è nazionale o meno */
    @SerialName("concNazionale")
    val nationalCompetition: Long? = null,

    /** Nota associata al concorso */
    @SerialName("nota")
    val note: String? = null,

    /** Nota associata al concorso. */
    @SerialName("concorsoNotaEng")
    val competitionNoteEnglish: String? = null,

    /** Tipo di corso di studio a cui è associato il concorso, test di valutazione, esame di stato. Per gli esami di stato assume valori nulli. */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Descrizione tipologia corso di studio associata ala concorso */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** Descrizione in lingua inglese della tipologia corso di studio associata ala concorso */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** Indica se il candidato ha pagato o meno la tassa di iscrizione al concorso */
    @SerialName("taxPagFlg")
    val taxPaymentFlag: Int? = null
)

@Serializable
data class Esse3CompetitionRanking(
    /** Identificativo della classifica */
    @SerialName("classifDettId")
    val classificationDetailId: Long? = null,

    /** Identificativo della prova */
    @SerialName("proveConcId")
    val competitionTestsId: Long? = null,

    /** Identificativo del turno */
    @SerialName("turniConcId")
    val competitionShiftsId: Long? = null,

    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long? = null,

    /** Identificativo della prova padre */
    @SerialName("provaDestId")
    val testDestinationId: Long? = null,

    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long = 0L,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long = 0L,

    /** Descrizione della prova */
    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    /** Descrizione della prova */
    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("punti")
    val points: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    /** giudizio */
    @SerialName("giudizio")
    val judgment: String? = null,

    /** giudizio in lingua inglese */
    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    /** indica se la prova è una materia o meno */
    @SerialName("materiaFlg")
    val subjectFlag: Long? = null,

    @SerialName("esitiProvaOrigine")
    val originTestOutcomes: List<Esse3SubjectsRanking> = emptyList()
)

@Serializable
data class Esse3CompetitionAdmissionInsert(
    /** Tipologia titolo italiano scelto */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** Categoria amministrativa. */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Int? = null,

    /** Tipologia di corso di studio scelto. */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Indica se il candidato ha richiesto o meno un trattamento particolare durante lo svolgimento delle prove del concorso in quanto portatore di handicap. */
    @SerialName("trattHandFlg")
    val handicapTreatmentFlag: Int? = null,

    /** Codice tipo handicap. */
    @SerialName("tipoHandicap")
    val handicapType: String? = null,

    /** Annotazioni dello studente relative alla propria posizione di handicap. */
    @SerialName("noteHand")
    val handicapNotes: String? = null,

    /** Numero compreso tra 0 e 100 che riporta la percentuale di handicap dichiarata dal candidato. */
    @SerialName("percHand")
    val handicapPercentage: Float? = null,

    /** Nota descrittiva. */
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
    /** Identificativo univoco lingua di preferenza */
    @SerialName("concLingueId")
    val languagesCompetitionId: Long = 0L,

    /** Ordine di preferenza. */
    @SerialName("prefOrd")
    val orderPreferences: Long = 0L
)

@Serializable
data class Esse3CompetitionTestDetail(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo dettaglio della graduatoria del concorso. */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    /** Identificativo corso di studio valorizzato a livello di dettaglio graduatoria */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Identificativo categoria amministrativa valorizzato a livello di dettaglio graduatoria */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** Descrizione del dettaglio graduatoria */
    @SerialName("dettaglio")
    val detail: String? = null,

    /** Descrizione in lingua inglesde del dettaglio graduatoria */
    @SerialName("dettaglioEng")
    val detailEnglish: String? = null,

    /** Numero totale posti dettaglio graduatoria */
    @SerialName("numPostiTot")
    val totalSeatsNumber: Long? = null,

    /** Numero posti liberi per il dettaglio graduatoria */
    @SerialName("numPostiLiberi")
    val freeSeatsNumber: Long? = null,

    /** Data inizio visibilità della graduatoria associata al dettaglio recuperato */
    @SerialName("dataIniVisGradWeb")
    val webGraduationViewStartDate: String? = null,

    /** Data fine visibilità della graduatoria associata al dettaglio recuperato */
    @SerialName("dataFinVisGradWeb")
    val webGraduationViewEndDate: String? = null,

    /** Anno corso per il quele è valido il dettaglio recuperato */
    @SerialName("annoCorso")
    val courseYear: Long? = null,

    @SerialName("prefPds")
    val studyPlanPreferences: List<Esse3StudyPlanPreferencesDetail> = emptyList(),

    @SerialName("prefSedi")
    val sitePreferences: List<Esse3LocationPreferencesDetail> = emptyList()
)

@Serializable
data class Esse3LanguagePreferencesDetail(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo dettaglio preferenza in lingua */
    @SerialName("concLingueId")
    val languagesCompetitionId: Long? = null,

    /** Identificativo lingua */
    @SerialName("linguaId")
    val languageId: Long? = null,

    /** Descrizione lingua */
    @SerialName("linguaDes")
    val languageDescription: String? = null,

    /** Codice iso6392 lingua */
    @SerialName("iso6392Cod")
    val iso6392Code: String? = null
)

@Serializable
data class Esse3CompetitionWithDetails(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Tipo di concorso: A = test di ammissione ad un corso di studio E = Esame di stato S = concorso per accedere ad una SSIS V = test di valutazione conoscenze del candidato */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Indica la modalità di svolgimento del concorso, test di valutazione, esame di stato: - E: ad esaurimento posti (ordine di presentazione della domanda di immatricolazione) - U: basato sul superamento di una prova unica - M: basato sul superamento di prove multiple - D: basato sull´ordine di presentazione della domanda di ammissione al concorso. */
    @SerialName("modTest")
    val testMode: String? = null,

    /** numero minimo preferenze */
    @SerialName("numPrefMin")
    val minPreferenceNumber: Long? = null,

    /** numero massimo preferenze */
    @SerialName("numPrefMax")
    val maxPreferenceNumber: Long? = null,

    /** numero minimo preferenze di lingue */
    @SerialName("numPrefLingueMin")
    val minLanguagePreferenceNumber: Long? = null,

    /** numero massimo preferenze di lingue */
    @SerialName("numPrefLingue")
    val languagePreferenceNumber: Long? = null,

    /** numero minimo borse di studio */
    @SerialName("numPrefBorseMin")
    val minScholarshipPreferenceNumber: Long? = null,

    /** numero massimo borse di studio */
    @SerialName("numPrefBorse")
    val scholarshipPreferenceNumber: Long? = null,

    /** Descrizione tipologia concorso */
    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    /** Descrizione del concorso. */
    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    /** Descrizione in lingua inglese del concorso. */
    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    /** Nota associata al concorso */
    @SerialName("nota")
    val note: String? = null,

    /** Nota associata al concorso. */
    @SerialName("concorsoNotaEng")
    val competitionNoteEnglish: String? = null,

    /** Tipo di corso di studio a cui è associato il concorso, test di valutazione, esame di stato. Per gli esami di stato assume valori nulli. */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Descrizione tipologia corso di studio associata ala concorso */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** Descrizione in lingua inglese della tipologia corso di studio associata ala concorso */
    @SerialName("tipoCorsoDesEng")
    val courseTypeDescriptionEnglish: String? = null,

    /** Link al bando del concorso. */
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
    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long = 0L,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long = 0L,

    /** Anno concorso */
    @SerialName("concLingueId")
    val languagesCompetitionId: Long? = null,

    /** Ordine di preferenza */
    @SerialName("prefOrd")
    val orderPreferences: Long? = null,

    /** Descrizione lingua */
    @SerialName("des")
    val description: String? = null,

    /** Identificativo della lingua del concorso */
    @SerialName("linguaId")
    val languageId: Long? = null
)

@Serializable
data class Esse3CompetitionStudentList(
    /** Identificativo del concorso */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo della scelta */
    @SerialName("posId")
    val positionId: Long? = null,

    /** Identificativo anno */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** prematricola studente */
    @SerialName("prematr")
    val preMatricola: Long? = null,

    /** Descrizione del concorso */
    @SerialName("concorsoDesEng")
    val competitionDescriptionEnglish: String? = null,

    /** Descrizione del concorso */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Descrizione della tipologia del concorso */
    @SerialName("tipoTestDes")
    val testTypeDescription: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniAmmWeb")
    val webAdminStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinAmmWeb")
    val webAdminEndDate: String? = null,

    /** Data scadenza della posizione utile (Ammesso) in graduatoria ai fini della preimmatricolazione */
    @SerialName("dataScad")
    val deadline: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataScadPreim")
    val preEnrollmentDeadline: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniDomRipescaggio")
    val recoveryApplicationStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinDomRipescaggio")
    val recoveryApplicationEndDate: String? = null,

    /** tassa flg */
    @SerialName("tassaFlg")
    val taxFlag: Int? = null,

    /** tassa flg */
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
    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long = 0L,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long = 0L,

    /** Anno concorso */
    @SerialName("concBorseId")
    val scholarshipCompetitionId: Long? = null,

    /** Anno concorso */
    @SerialName("tipoBorsaCod")
    val scholarshipTypeCode: String? = null,

    /** Descrizione borsa */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3CourseListForStudents(
    /** Identificativo del concorso */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    /** Identificativo anno */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Identificativo del concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Codice corso di studio */
    @SerialName("cod")
    val code: String? = null,

    /** Codice della tipologia del corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Descrizione del concorso */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** Identificativo classe */
    @SerialName("claMId")
    val classMId: Long? = null,

    /** Codice classe */
    @SerialName("claMurstCod")
    val classMurstCode: String? = null,

    /** Descrizione della classe */
    @SerialName("descrizioneClasse")
    val classDescription: String? = null,

    /** Descrizione del concorso */
    @SerialName("tipoTestCod")
    val testTypeCode: String? = null,

    /** Anno ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** Descrizione ordinamento corso di studio */
    @SerialName("cdsOrdDes")
    val courseOfStudyOrderDescription: String? = null,

    /** Identificativo percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** Descrizione ordinamento percorso di studio */
    @SerialName("pdsOrdDes")
    val studyPlanOrderDescription: String? = null,

    /** Identificativo tipologia categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** Descrizione tipologia categoria amministrativa */
    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    /** Identificativo della sede */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** Descrizione della sede */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** Anno corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** Numero posti totali */
    @SerialName("numeroPostiTot")
    val totalSeatsNumber: Int? = null,

    /** Numero posti totali */
    @SerialName("numeroPostiLiberi")
    val freeSeatsNumber: Int? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniVisGradWeb")
    val webGraduationViewStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinVisGradWeb")
    val webGraduationViewEndDate: String? = null,

    /** Data scadenza della posizione utile (Ammesso) in graduatoria ai fini della preimmatricolazione */
    @SerialName("dataScad")
    val deadline: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataScadPreim")
    val preEnrollmentDeadline: String? = null,

    /** Data d''inizio del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataIniDomRipescaggio")
    val recoveryApplicationStartDate: String? = null,

    /** Data di fine del periodo valido per la compilazione delle domande di ammissione al concorso, test di valutazione, esame di stato da web. */
    @SerialName("dataFinDomRipescaggio")
    val recoveryApplicationEndDate: String? = null
)

@Serializable
data class Esse3RankingListImport(
    /** Indentifica la prematricola */
    @SerialName("prematr")
    val preMatricola: Long? = null,

    /** Giudizio in lingua inglese */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Giudizio in lingua inglese */
    @SerialName("cognome")
    val surname: String? = null,

    /** Giudizio in lingua inglese */
    @SerialName("nome")
    val name: String? = null,

    @SerialName("posiz")
    val position: Long? = null,

    @SerialName("puntiTot")
    val totalPoints: Double? = null,

    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    /** Esito in graduatoria */
    @SerialName("stato")
    val state: String? = null
)

@Serializable
data class Esse3TestDetail(
    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long = 0L,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long = 0L,

    /** Identificativo del dettaglio */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    /** Ordine di preferenza */
    @SerialName("prefOrd")
    val orderPreferences: Long? = null,

    /** Dettaglio del concorso */
    @SerialName("dettaglio")
    val detail: String? = null,

    /** Dettaglio lingua inglese */
    @SerialName("dettaglioEng")
    val detailEnglish: String? = null
)

@Serializable
data class Esse3LocationDetail(
    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long = 0L,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long = 0L,

    /** Anno concorso */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    /** Anno concorso */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** Ordine di preferenza */
    @SerialName("prefOrd")
    val orderPreferences: Long? = null,

    /** Descrizione lingua */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3StudyPlanDetail(
    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long = 0L,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long = 0L,

    /** Anno concorso */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    /** Identificativo corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Anno ordinamento corso di studio */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** Identificativo percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** Codice del percorso di studio */
    @SerialName("cod")
    val code: String? = null,

    /** Descrizione pdel percorso di studi */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3CompetitionRankingList(
    /** Anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo posizione */
    @SerialName("testId")
    val testId: Long? = null,

    /** Identificativo dettaglio test */
    @SerialName("dettTestId")
    val testDetailId: Long? = null,

    /** Identificativo dettaglio della graduatoria del concorso. */
    @SerialName("righeGradId")
    val graduationRowsId: Long? = null,

    /** Identificativo della persona iscritta */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo posizione */
    @SerialName("posId")
    val positionId: Long? = null,

    /** Codice dello stato del candidato */
    @SerialName("statoCod")
    val stateCode: String? = null,

    /** Descrizione dello stato del candidato */
    @SerialName("statoGradDes")
    val graduationStateDescription: String? = null,

    /** Descrizione dello stato del candidato in lingua inglese */
    @SerialName("statoGradDesEng")
    val graduationStateDescriptionEnglish: String? = null,

    /** Identificativo posizione */
    @SerialName("posiz")
    val position: Long? = null,

    /** Identificativo posizione */
    @SerialName("punti")
    val points: Double? = null,

    /** Numero massimo di punti */
    @SerialName("maxPunti")
    val maxPoints: Double? = null,

    /** Giudizio */
    @SerialName("giudizio")
    val judgment: String? = null,

    /** Giudizio in lingua inglese */
    @SerialName("giudizioEng")
    val judgmentEnglish: String? = null,

    /** Indica se è presente la domanda di ripescaggio */
    @SerialName("presDomRipescaggio")
    val recoveryApplicationPresence: Long? = null,

    /** Data domanda ripescaggio */
    @SerialName("dataDomRipescaggio")
    val recoveryApplicationDate: String? = null,

    /** Data scadenza posizione ammesso */
    @SerialName("dataScadPosAm")
    val adminPositionDeadline: String? = null,

    /** Data scadenza posizione preimmatricolato */
    @SerialName("dataScadPosPi")
    val piPositionDeadline: String? = null
)
