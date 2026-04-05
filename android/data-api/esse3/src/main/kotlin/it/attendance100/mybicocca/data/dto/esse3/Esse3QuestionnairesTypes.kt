package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3CompiledQuestionnaires(
    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long = 0L,

    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long = 0L,

    /** ordine di visualizzazione interno al questionario dell'elemento corrente */
    @SerialName("ordVis")
    val orderVisible: Int = 0,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** stato del questionario configurato */
    @SerialName("statoQuestCod")
    val questionStateCode: Esse3QuestionStateCode? = null,

    /** note del questionario configurato */
    @SerialName("questionarioNote")
    val questionnaireNote: String? = null,

    /** codice del contesto associato al questionario configurato */
    @SerialName("questContCod")
    val questionContentCode: String? = null,

    /** descrizione del contesto associato al questionario configurato */
    @SerialName("questContDes")
    val questionContentDescription: String? = null,

    /** Data inserimento del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("questDataIns")
    val questionInsertionDate: String? = null,

    /** Data modifica del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("questDataMod")
    val questionModificationDate: String? = null,

    /** ID dell'elemento del questionario configurato */
    @SerialName("quesitoId")
    val questionId: Long? = null,

    /** codice dell'elemento del questionario configurato */
    @SerialName("elemCod")
    val elementCode: String? = null,

    /** descrizione dell'elemento del questionario configurato */
    @SerialName("elementiDes")
    val elementsDescription: String? = null,

    /** ID dell'elemento padre dell'elemento del questionario configurato */
    @SerialName("parentQuesitoId")
    val parentQuestionId: Long? = null,

    /** codice del formato dell'elemento. Vale per domande e risposte */
    @SerialName("tipoFormatoCod")
    val formatTypeCode: String? = null,

    /** descrizione del formato dell'elemento. Vale per domande e risposte */
    @SerialName("tipoFormatoDes")
    val formatTypeDescription: String? = null,

    /** punteggio numerico associato all'elemento */
    @SerialName("quesitoPunteggio")
    val questionScore: Int? = null,

    /** Flag che indica se l'elento corrente domanda � obbligatoria oppure no */
    @SerialName("obbligatorioFlg")
    val mandatoryFlag: Int? = null,

    /** note dell'elemento del questionario configurato */
    @SerialName("quesitoNote")
    val questionNote: String? = null,

    /** codice del tag associato all'elemento del questionario */
    @SerialName("tagCod")
    val tagCode: String? = null,

    /** codice della categoria associata all'elemento del questionario */
    @SerialName("categCod")
    val categoryCode: String? = null,

    /** codice del tipo dell'elemento. */
    @SerialName("tipoElemCod")
    val elementTypeCode: String? = null,

    /** note dellelemento del questionario configurato */
    @SerialName("elementiNota")
    val elementsNote: String? = null,

    /** ID della risposta del questionario compilato */
    @SerialName("rispostaId")
    val answerId: Long? = null,

    /** testo della risposta nel caso di riposta libera */
    @SerialName("testoLibero")
    val freeText: String? = null,

    /** Data inserimento della risposta. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("rispostaDataIns")
    val answerInsertionDate: String? = null,

    /** Data modifica della risposta. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("rispostaDataMod")
    val answerModificationDate: String? = null
)

@Serializable
data class Esse3UserCompiledEventTagsTeacherAvailability(
    /** ID log accesso utente per compilazione questionario */
    @SerialName("userCompId")
    val userComponentId: Long? = null,

    /** ID configurazione questionario */
    @SerialName("questConfigId")
    val questionConfigId: Long? = null,

    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** ID utente per compilazione questionario */
    @SerialName("idUser")
    val userId: Long? = null,

    /** Data e ora dell'inizio compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniComp")
    val completionStartDate: String? = null,

    /** Data e ora della fine compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long = 0L,

    /** Id del docente che fa lezione */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** matricola del docente che fa lezione */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** nome del docente che fa lezione */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente che fa lezione */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** Id address book del docente che fa lezione */
    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null,

    /** anno di offerta di erogazione dell'attività didattica */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** Id del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    /** chiave del percorso di studio di erogazione dell'attività didattica */
    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    /** chiave dell'attività didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell''attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** chiave dell'unità didattica */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** codice dell'unità didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione dell'unità didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** codice del dominio di partizione */
    @SerialName("domPartAdCod")
    val domicilePartialTeachingActivityCode: String? = null,

    /** ID logistica a livello di attività didattica */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** ID logistica a livello di unità didattica */
    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

    /** anno di compilazione del questionario */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Data e ora della compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
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
    /** soggetto che eroga l'attività */
    @SerialName("soggettoErogante")
    val providerSubject: String? = null,

    /** luogo di erogazione dell'attività */
    @SerialName("destinazione")
    val destination: String? = null,

    /** data di partenza */
    @SerialName("dataPartenza")
    val departureDate: String? = null,

    /** data di arrivo */
    @SerialName("dataArrivo")
    val arrivalDate: String? = null,

    /** note dello studente sull'attività */
    @SerialName("noteStu")
    val studentNotes: String? = null,

    /** flag che indica se l'attività fa parte delle AD di dottorato */
    @SerialName("adFuoriOffFlg")
    val activityOutsideOfferFlag: Int? = null,

    /** indica che l'attività richiede una missione */
    @SerialName("missioneFlg")
    val missionFlag: Int? = null,

    /** indica che l'attività è di ricerca */
    @SerialName("ricercaFlg")
    val searchFlag: Int? = null,

    /** indica che l'attività è un periodo all'estero */
    @SerialName("periodoEsteroFlg")
    val foreignPeriodFlag: Int? = null,

    /** indica che l'attività è in azienda */
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
    /** Tipo di modalità con cui viene valutato l'esame. Può assumere i valori V,G,N se il valore è V allora al momento del superamento viene valorizzato il campo voto, altrimenti se il valore è G viene valorizzato il campo tipo_giud_cod */
    @SerialName("modValCod")
    val evaluationModeCode: Esse3EvaluationModeCode,

    /** flag che indica se l''esito è positivo */
    @SerialName("supEsaFlg")
    val supGraduationFlag: Int = 0,

    /** voto, valorizzato se modValCod è V. Gli esiti delle prove finali (cioè quelle che prevedono il caricamento nella riga di libretto) sono INTERI, gli esti di prove parziali invece possono avere 2 cifre decimali */
    @SerialName("voto")
    val grade: Float? = null,

    /** flag che indica la lode, impostato a 1 solo per modValCod è V e la lode deve essere impostata */
    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    /** codice che indica il tipo di giudizio utilizzato, valorizzato solo se modValCod è G */
    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    /** descrizione che indica il tipo di giudizio utilizzato, valorizzato solo se modValCod è G */
    @SerialName("tipoGiudDes")
    val judgmentTypeDescription: String? = null,

    /** data della prova, il formato con cui deve essere definita la data è DD/MM/YYYY */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** anno di superamento della prova */
    @SerialName("aaSupId")
    val academicYearSupervisorId: Int? = null
)

@Serializable
data class Esse3QuestionnaireCompiledEventTagsPostLogin(
    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long? = null,

    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String = "",

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** anno di compilazione del questionario */
    @SerialName("aaId")
    val academicYearId: Int = 0,

    /** chiave del corso di studio di iscrizione dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di iscrizione dello studente */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di iscrizione dello studente */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** anno di ordinamento del corso di studio di iscrizione dello studente */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** chiave del percorso di studio di iscrizione dello studente */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** anno di corso dello studente */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** sesso dello studente */
    @SerialName("sesso")
    val gender: String? = null,

    /** anno di nascita dello studente */
    @SerialName("annoNascita")
    val birthYear: Int? = null,

    /** codice della cittadinanza dello studente */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** ID codice MIUR della nazione di cittadinanza dello studente */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** descrizione della cittadinanza dello studente */
    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    /** descrizione della provincia dello studente */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Id del comune di residenza dello studente */
    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    /** codice di 4 cifre (Lettera + 3 numeri) che � utilizzato nel codice fiscale per indicare il comune di residenza dello studente */
    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    /** descrizione del comune di residenza dello studente */
    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    /** Id della nazione di residenza dello studente */
    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    /** ID codice MIUR della nazione di residenza dello studente */
    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    /** descrizione della nazione di residenza dello studente */
    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    /** tipo del titolo di studio dello studente */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** numero totale di crediti acquisiti dallo studente */
    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    /** Data e ora della compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
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

    /** 0 questionari non presenti 1 questionari compilati 2 alcuni questionari da compilare 3 questionari da compilare */
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
    /** anno di offerta di erogazione nell''interateneo */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Long? = null,

    /** anno di ordinamento di erogazione nell''interateneo */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Long? = null,

    /** codice dell''attivita di erogazione nell`interateneo */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell''attivita di erogazione nell`interateneo */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** riga di libretto della sede operativa dove è stata caricata l''attività didattica collegata alla riga di libretto */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** id dell''ateneo dove è erogata l''attività didattica */
    @SerialName("ateneoId")
    val universityId: Long? = null,

    /** codice del corso di erogazione nell''interateneo */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione nell''interateneo */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** codice del percorso di erogazione nell''interateneo */
    @SerialName("pdsAdCod")
    val studyPlanTeachingActivityCode: String? = null,

    /** descrizione del percorso di erogazione nell''interateneo */
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

    /** Pagina id della pagina precedente, se è null significa che si è all'inizio della compilazione (Questi campi sono valorizzati solo con lo spostamento tra le pagine) */
    @SerialName("pPaginaPrecId")
    val previousPageId: Int? = null,

    /** Pagina id della pagina successiva, se è null significa che si è alla fine della compilazione ed è necessario chiamare il riepilogo (Questi campi sono valorizzati solo con lo spostamento tra le pagine) */
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

    /** l'id dell'operando sql che viene utilizzato per generare la lista dei valori selezionabili nella domanda ti dipo DOM_VAR */
    @SerialName("operandoDomId")
    val domicileOperandId: Int? = null,

    /** numero minimo che si può dare a una risposta numerica */
    @SerialName("limMin")
    val minLimit: Int? = null,

    /** numero massimo che si può dare a una risposta numerica */
    @SerialName("limMax")
    val maxLimit: Int? = null,

    /** numero massimo di risposte che si possono dare a una domanda con scelte multiple. */
    @SerialName("numMaxValori")
    val maxValuesNumber: Int? = null,

    @SerialName("dominioRisposte")
    val answersDomain: List<Esse3AnswerDomains> = emptyList()
)

@Serializable
data class Esse3ContextualizedActivityKey(
    /** chiave del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** codice dell''ordinamento di erogazione dell'attività didattica */
    @SerialName("aaOrdCod")
    val academicYearOrderCode: String? = null,

    /** descrizione dell''ordinamento di erogazione dell'attività didattica */
    @SerialName("aaOrdDes")
    val academicYearOrderDescription: String? = null,

    /** chiave del percorso di studio di erogazione dell'attività didattica */
    @SerialName("pdsId")
    val studyPlanId: Long = 0L,

    /** codice del percorso di erogazione dell'attività didattica */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di erogazione dell'attività didattica */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** anno di offerta di erogazione dell'attività didattica */
    @SerialName("aaOffId")
    val academicYearOfferId: Long = 0L,

    /** chiave dell'attività didattica */
    @SerialName("adId")
    val activityId: Long = 0L,

    /** codice dell''attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell''attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id della afId proveniente da U-Gov Didattica */
    @SerialName("afId")
    val activityFunctionId: Long? = null
)

@Serializable
data class Esse3QuestionnaireCompiledEventTagsGeneralQuestionnaire(
    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long = 0L,

    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** anno di compilazione del questionario */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** chiave del corso di studio di iscrizione dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di iscrizione dello studente */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di iscrizione dello studente */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** anno di ordinamento del corso di studio di iscrizione dello studente */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** chiave del percorso di studio di iscrizione dello studente */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** anno di corso dello studente */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** sesso dello studente */
    @SerialName("sesso")
    val gender: String? = null,

    /** anno di nascita dello studente */
    @SerialName("annoNascita")
    val birthYear: Int? = null,

    /** codice della cittadinanza dello studente */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** ID codice MIUR della nazione di cittadinanza dello studente */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** descrizione della cittadinanza dello studente */
    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    /** descrizione della provincia dello studente */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Id del comune di residenza dello studente */
    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    /** codice di 4 cifre (Lettera + 3 numeri) che � utilizzato nel codice fiscale per indicare il comune di residenza dello studente */
    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    /** descrizione del comune di residenza dello studente */
    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    /** Id della nazione di residenza dello studente */
    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    /** ID codice MIUR della nazione di residenza dello studente */
    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    /** descrizione della nazione di residenza dello studente */
    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    /** tipo del titolo di studio dello studente */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** numero totale di crediti acquisiti dallo studente */
    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    /** Data e ora della compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
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
    /** ID log accesso utente per compilazione questionario */
    @SerialName("userCompId")
    val userComponentId: Long? = null,

    /** ID configurazione questionario */
    @SerialName("questConfigId")
    val questionConfigId: Long? = null,

    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** ID utente per compilazione questionario */
    @SerialName("idUser")
    val userId: Long? = null,

    /** Data e ora dell'inizio compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniComp")
    val completionStartDate: String? = null,

    /** Data e ora della fine compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long = 0L,

    /** Id dello studente che compila il quesitonario */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** anno di compilazione del questionario */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** chiave del corso di studio di iscrizione dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di iscrizione dello studente */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di iscrizione dello studente */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** anno di ordinamento del corso di studio di iscrizione dello studente */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** chiave del percorso di studio di iscrizione dello studente */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** anno di corso dello studente */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** sesso dello studente */
    @SerialName("sesso")
    val gender: String? = null,

    /** anno di nascita dello studente */
    @SerialName("annoNascita")
    val birthYear: Int? = null,

    /** codice della cittadinanza dello studente */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** ID codice MIUR della nazione di cittadinanza dello studente */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** descrizione della cittadinanza dello studente */
    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    /** descrizione della provincia dello studente */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Id del comune di residenza dello studente */
    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    /** codice di 4 cifre (Lettera + 3 numeri) che � utilizzato nel codice fiscale per indicare il comune di residenza dello studente */
    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    /** descrizione del comune di residenza dello studente */
    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    /** Id della nazione di residenza dello studente */
    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    /** ID codice MIUR della nazione di residenza dello studente */
    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    /** descrizione della nazione di residenza dello studente */
    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    /** tipo del titolo di studio dello studente */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** numero totale di crediti acquisiti dallo studente */
    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    /** Data e ora della compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
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
    /** id dell'attivita' didattica di libretto */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** 0 questionario da compilare. 1 questionario completo o non presente. */
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

    /** Se il flag è a 1 non sarà salvato nessun legame tra il questionario e chi l'ha compilato. */
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
    /** id univoco che consente di individuare il tratto di carriera dello studente e il relativo libretto collegato */
    @SerialName("matId")
    val matId: Long = 0L,

    /** progressivo di ordinamento delle attività, calcolato tenendo conto dei raggruppamenti e degli ordinamenti previsti a sistema */
    @SerialName("ord")
    val order: Int = 0,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    /** id univoco che consente di individuare la carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** id progressivo del piano di studio collegato tramite attuazione al libretto. */
    @SerialName("pianoId")
    val planId: Long? = null,

    /** id progressivo dell''attività del piano collegata con la riga di libretto */
    @SerialName("itmId")
    val itemId: Long? = null,

    /** se l''attività appartiene ad un raggruppamento contiene l''adsceID del padre del raggruppamento */
    @SerialName("ragId")
    val groupId: Long? = null,

    /** contiene la tipologia del tipo di raggruppamento, valorizzato solo sul padre del raggruppamento (cioà quando ragId=adsceId) */
    @SerialName("raggEsaTipo")
    val graduationGroupType: Esse3GraduationGroupType? = null,

    /** codice dell''attività didattica presente nel libretto. Il codice à copiato in ogni singolo libretto e, di norma, coincide con il codice previsto nell''offerta didattica alla quale l''attività didattica si riferisce */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell''attività didattica presente nel libretto, come il codice, la descrizione risulta copiata dall''offerta didattica, ma puà essere modificato */
    @SerialName("adDes")
    val activityDescription: String = "",

    /** anno di corso al quale à prevista l''attività didattica */
    @SerialName("annoCorso")
    val courseYear: Int = 0,

    /** Stato dell\'attività didattica (codice) */
    @SerialName("stato")
    val state: Esse3State,

    /** descrizione dello stato dell\'attività didattica */
    @SerialName("statoDes")
    val stateDescription: String = "",

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    /** codice del tipo di esame previsto per l''attività didattica */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    /** descrizione del tipo di esame previsto per l''attività didattica */
    @SerialName("tipoEsaDes")
    val graduationTypeDescription: String? = null,

    /** codice tipo di insegnamento previsto per l''attività didattica */
    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    /** descrizione tipo di insegnamento previsto per l''attività didattica */
    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null,

    /** Presenza di un riconoscimento o convalida. 0 = Nessun Riconoscimento 1 = RF (Riconoscimento di frequenza)  2 = RA (Riconoscimento di attività) 3 = CF (Convalida di frequenza) 4 = CA (Convalida di attività) */
    @SerialName("ricId")
    val searchId: Int = 0,

    /** codice del tipo di riconoscimento previsto per l''attività didattica */
    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    /** peso dell'attività didattica, calcolato come somma dei pesi dei segmenti, il peso prevede due decimali opzionali */
    @SerialName("peso")
    val weight: Float = 0f,

    /** anno di frequenza, valorizzato nel caso lo stato dell''attività sia F oppure S */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza, se valorizzata indica la data di riferimento dalla quale la frequenza risulta acquisita, il formato con cui deve essere definita la data à DD/MM/YYYY */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** frequenza assegnata di ufficio */
    @SerialName("freqUffFlg")
    val officialAttendanceFlag: Int = 0,

    /** frequenza obbligatoria */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int = 0,

    /** data di scadenza dell'iscirzione, l''attività non puà essere sostenuta dopo questa data, il formato con cui deve essere definita la data è DD/MM/YYYY */
    @SerialName("dataScadIscr")
    val enrollmentDeadline: String? = null,

    /** id della tipologia di voto utilizzata, indica quale range di voti à definito sull''attività didattica, il voto minimo positivo, il voto massimo e l''eventuale presenza della lode */
    @SerialName("gruppoVotoId")
    val gradeGroupId: Int? = null,

    /** indica il minimo voto positivo per la scala di voti selezionata */
    @SerialName("gruppoVotoMinVoto")
    val gradeGroupMinGrade: Int? = null,

    /** indica il massimo voto positivo per la scala di voti selezionata */
    @SerialName("gruppoVotoMaxVoto")
    val gradeGroupMaxGrade: Int? = null,

    /** indica se la scala di voti selezionata prevede la lode */
    @SerialName("gruppoVotoLodeFlg")
    val gradeGroupLodeFlag: Int? = null,

    /** codice del gruppo di giudizi a cui appartiene il giudizio presente nell''esito */
    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    /** descrizione del gruppo di giudizi a cui appartiene il giudizio selezionato */
    @SerialName("gruppoGiudDes")
    val judgmentGroupDescription: String? = null,

    @SerialName("esito")
    val outcome: Esse3Result? = null,

    /** attività sovrannumeraria */
    @SerialName("sovranFlg")
    val overrideFlag: Int = 0,

    /** note associate alla riga di libretto */
    @SerialName("note")
    val notes: String? = null,

    /** attività di debito */
    @SerialName("debitoFlg")
    val debtFlag: Int = 0,

    /** attività OFA */
    @SerialName("ofaFlg")
    val ofaFlag: Int? = null,

    /** anno corso di anticipo dell'attività, se valorizzato indica l'anno di corso a cui è stata anticipata l'attività */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** attività inserita automaticamente da una procedura diversa dalla attuazione piani carriera o dalla gestione libretto (prenotazione appelli, registrazione prove, ecc). */
    @SerialName("genAutoFlg")
    val autoGenerationFlag: Int = 0,

    /** attività inserita automaticamente realtiva al riconoscimento crediti nel caso di carriere specialistiche (3+2), contiene tutti i segmenti del tratto triennale della laurea specialistica. */
    @SerialName("genRicSpecFlg")
    val generateSpecialRequestFlag: Int = 0,

    /** indica il tipo di origine della ad del libretto 0 = AD non collegata a nessuna delibera 1 = AD inserita automaticamente in fase di attuazione e con delibera pendente (in questo caso l''AD non è collegata al piano) 2 = AD collegata alla delibera e inserita automaticamente dall'attivazione 3 = AD collegata alla delibera (passaggio di stato 1->3 dopo che la delibera viene approvata) 4 = AD relativa a delibera annullata, in questo caso l''ad non è collegata al piano */
    @SerialName("tipoOrigEvecar")
    val careerEventOriginType: Int? = null,

    /** url della pagina web dell'insegnamento */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("infoDottorati")
    val phdInfo: Esse3PhDTranscriptRowInfo? = null,

    @SerialName("rilFreq")
    val attendanceRelease: List<Esse3AttendanceRelease> = emptyList(),

    /** stato della missione */
    @SerialName("statoMissione")
    val missionState: String? = null,

    /** descrizione stato della missione */
    @SerialName("statoMissioneDes")
    val missionStateDescription: String? = null,

    /** contiene il numero di appelli prenotabili alla data di sistema per la riga di libretto */
    @SerialName("numAppelliPrenotabili")
    val bookableCallsNumber: Int? = null,

    /** attività superata, comprende anche il caso di prove verbalizzate ma non caricate in carriera per errori durante la procedura carica_prove */
    @SerialName("superataFlg")
    val passedFlag: Int? = null,

    /** contiene il numero di prenotazioni pendenti collegate alla riga di libretto. */
    @SerialName("numPrenotazioni")
    val bookingNumber: Int? = null,

    /** Indica se l´attività didattica è associata al conseguimento di una particolare abilitazione prevista dal corso di studio. */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    /** adsce_id orgine della convalida per le AD che sono di completamento. */
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
    /** id del tratto di carriera su cui calcolare le statistiche */
    @SerialName("matId")
    val matId: Long? = null,

    /** identificativo del gruppo di rilevazioni */
    @SerialName("adsceRilId")
    val choiceReleaseId: Long? = null,

    /** anno di rilevazione */
    @SerialName("aaRilevazioneId")
    val academicYearSurveyId: Int? = null,

    /** identificativo univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** codice del tipo corso di studio dello studente */
    @SerialName("stuTipoCorsoCod")
    val studentCourseTypeCode: String? = null,

    /** stato della rilevazione (A, B, X) */
    @SerialName("statoAdsceRil")
    val teachingActivityChoiceReleaseState: String? = null,

    /** anno di frequenza impostato dalla rilevazione */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** stato dell'attività didattica impostata dalla rilevazione */
    @SerialName("staSceCod")
    val choiceStatusCode: String? = null,

    /** totale in ore delle rilevazioni */
    @SerialName("totOrePerFreq")
    val totalHoursForAttendance: Float? = null,

    /** numero totale di rilevazioni */
    @SerialName("totRilPerFreq")
    val totalReleasedForAttendance: Int? = null,

    /** percentuale di presenza */
    @SerialName("percPresPerFreq")
    val presencePercentageForAttendance: Float? = null,

    /** percentuale di ore di presenza */
    @SerialName("percOrePresPerFreq")
    val presenceHoursPercentageForAttendance: Float? = null,

    /** numero totale di rilevazioni calcolate */
    @SerialName("numRil")
    val releaseNumber: Int? = null,

    /** totale in ore delle rilevazioni calcolate */
    @SerialName("oreRil")
    val releasedHours: Float? = null,

    /** numero totale di assenze calcolate */
    @SerialName("numAss")
    val absenceNumber: Int? = null,

    /** numero totale di presenze calcolate */
    @SerialName("numPres")
    val presenceNumber: Int? = null,

    /** totale in ore di assenze calcolate */
    @SerialName("oreAss")
    val absenceHours: Float? = null,

    /** totale in ore di presenze calcolate */
    @SerialName("orePres")
    val presenceHours: Float? = null,

    /** ore di frequenza previste per l'attività didattica */
    @SerialName("oreTotFreqAd")
    val totalTeachingActivityAttendanceHours: Float? = null,

    /** numero di lezioni previste per l'attività didattica (in base al par_conf RIL_FREQ_DURATA_RIL che indica durata media di una lezione) */
    @SerialName("numTotFreqAd")
    val totalTeachingActivityAttendanceNumber: Float? = null,

    /** massima data di rilevazione calcolata su un insieme di rilevazioni */
    @SerialName("dataFreqRilFreqDett")
    val attendanceReleaseDetailDate: String? = null,

    /** data fine del periodo didattico */
    @SerialName("dataFreqAdLog")
    val teachingActivityAttendanceLogDate: String? = null
)

@Serializable
data class Esse3ExtraTranscriptRow(
    /** id del tratto di carriera su cui calcolare le statistiche */
    @SerialName("matId")
    val matId: Long? = null,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** data prevista di inizio lezioni */
    @SerialName("dataInizioLezioni")
    val lessonsStartDate: String? = null,

    /** data prevista di fine lezioni */
    @SerialName("dataFineLezioni")
    val lessonsEndDate: String? = null,

    /** matricola del titolare della partizione dello studente */
    @SerialName("titMatricola")
    val titleMatricola: String? = null,

    /** nome del titolare della partizione dello studente */
    @SerialName("titNome")
    val titleName: String? = null,

    /** cognome del titolare della partizione dello studente */
    @SerialName("titCognome")
    val titleSurname: String? = null,

    /** matricola del titolare della partizione dello studente */
    @SerialName("titCodFis")
    val titleFiscalCode: String? = null,

    /** fattore di partizionamento dell' attività didiattica */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** classe assegnata allo studente */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** regola del piano collegata alla riga del libretto, valida solo se il piano è attivo */
    @SerialName("tipiSceCod")
    val choiceTypesCode: String? = null,

    /** descrizione del tipo di regola del piano collegata alla riga del libretto, valida solo se il piano è attivo */
    @SerialName("tipiSceDes")
    val choiceTypesDescription: String? = null,

    /** descrizione della regola del piano collegata alla riga del libretto, valida solo se il piano è attivo */
    @SerialName("sceDes")
    val choiceDescription: String? = null,

    /** attività con frequenza obbliggatoria */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null
)

@Serializable
data class Esse3UserCompiledEventTagsGeneralQuestionnaire(
    /** ID log accesso utente per compilazione questionario */
    @SerialName("userCompId")
    val userComponentId: Long? = null,

    /** ID configurazione questionario */
    @SerialName("questConfigId")
    val questionConfigId: Long? = null,

    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** ID utente per compilazione questionario */
    @SerialName("idUser")
    val userId: Long? = null,

    /** Data e ora dell'inizio compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniComp")
    val completionStartDate: String? = null,

    /** Data e ora della fine compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long = 0L,

    /** Id dello studente che compila il quesitonario */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** anno di compilazione del questionario */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** chiave del corso di studio di iscrizione dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di iscrizione dello studente */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di iscrizione dello studente */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** anno di ordinamento del corso di studio di iscrizione dello studente */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** chiave del percorso di studio di iscrizione dello studente */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** anno di corso dello studente */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** sesso dello studente */
    @SerialName("sesso")
    val gender: String? = null,

    /** anno di nascita dello studente */
    @SerialName("annoNascita")
    val birthYear: Int? = null,

    /** codice della cittadinanza dello studente */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** ID codice MIUR della nazione di cittadinanza dello studente */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** descrizione della cittadinanza dello studente */
    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    /** descrizione della provincia dello studente */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Id del comune di residenza dello studente */
    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    /** codice di 4 cifre (Lettera + 3 numeri) che � utilizzato nel codice fiscale per indicare il comune di residenza dello studente */
    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    /** descrizione del comune di residenza dello studente */
    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    /** Id della nazione di residenza dello studente */
    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    /** ID codice MIUR della nazione di residenza dello studente */
    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    /** descrizione della nazione di residenza dello studente */
    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    /** tipo del titolo di studio dello studente */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** numero totale di crediti acquisiti dallo studente */
    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    /** Data e ora della compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataOra")
    val dateTime: String? = null
)

@Serializable
data class Esse3UserCompiledEventTagsTeachingEvaluation(
    /** ID log accesso utente per compilazione questionario */
    @SerialName("userCompId")
    val userComponentId: Long = 0L,

    /** ID configurazione questionario */
    @SerialName("questConfigId")
    val questionConfigId: Long? = null,

    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** ID utente per compilazione questionario */
    @SerialName("idUser")
    val userId: Long? = null,

    /** Data e ora dell'inizio compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataIniComp")
    val completionStartDate: String? = null,

    /** Data e ora della fine compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFineComp")
    val completionEndDate: String? = null,

    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long? = null,

    /** Id dello studente che compila il quesitonario */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** anno di offerta di erogazione dell'attivit� didattica */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** Id del corso di studio di erogazione dell'attivit�� didattica */
    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    /** codice del corso di studio di erogazione dell'attivit� didattica */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione dell'attivit� didattica */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attivit� didattica */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    /** chiave del percorso di studio di erogazione dell'attivit� didattica */
    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    /** chiave dell'attivit� didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell''attivit� didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attivit� didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** chiave dell'unit� didattica */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** codice dell'unit� didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione dell'unit� didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** Id del docente che fa lezione */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** matricola del docente che fa lezione */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** nome del docente che fa lezione */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente che fa lezione */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** Id address book del docente che fa lezione */
    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null,

    /** codice del dominio di partizione */
    @SerialName("domPartAdCod")
    val domicilePartialTeachingActivityCode: String? = null,

    /** codice della partizione dell'anno accademico */
    @SerialName("partAdCod")
    val partialTeachingActivityCode: String? = null,

    /** chiave del corso di studio di iscrizione dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di iscrizione dello studente */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di iscrizione dello studente */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** sesso dello studente */
    @SerialName("sesso")
    val gender: String? = null,

    /** anno di nascita dello studente */
    @SerialName("annoNascita")
    val birthYear: Int? = null,

    /** Id del docente titolare */
    @SerialName("docenteTitId")
    val lecturerTitleId: Long? = null,

    /** matricola del docente titolare */
    @SerialName("docenteTitMatricola")
    val lecturerTitleMatricola: String? = null,

    /** nome del docente titolare */
    @SerialName("docenteTitNome")
    val lecturerTitleName: String? = null,

    /** cognome del docente titolare */
    @SerialName("docenteTitCognome")
    val lecturerTitleSurname: String? = null,

    /** Id address book del docente titolare */
    @SerialName("docenteTitIdAb")
    val lecturerTitleAbbreviatedId: Long? = null,

    /** codice della cittadinanza dello studente */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** ID codice MIUR della nazione di cittadinanza dello studente */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** descrizione della cittadinanza dello studente */
    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    /** descrizione della provincia dello studente */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Id del comune di residenza dello studente */
    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    /** codice di 4 cifre (Lettera + 3 numeri) che � utilizzato nel codice fiscale per indicare il comune di residenza dello studente */
    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    /** descrizione del comune di residenza dello studente */
    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    /** Id della nazione di residenza dello studente */
    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    /** ID codice MIUR della nazione di residenza dello studente */
    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    /** descrizione della nazione di residenza dello studente */
    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    /** tipo del titolo di studio dello studente */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** anno di acquisizione dei crediti dello studente (dato successivo) */
    @SerialName("aaCfuStu")
    val academicYearStudentCFU: Long? = null,

    /** numero di crediti acquisiti dallo studente nell'anno di acquisizione (dato precedente) */
    @SerialName("cfuAaStu")
    val studentAcademicYearCredits: Float? = null,

    /** numero totale di crediti acquisiti dallo studente */
    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    /** anno di compilazione del questionario */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** anno di corso della attivit� didattica */
    @SerialName("annoCorsoAd")
    val teachingActivityCourseYear: Int? = null,

    /** Data e ora della compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataOra")
    val dateTime: String? = null,

    /** Flag che indica se lo studente � frequentante */
    @SerialName("stuFreqFlg")
    val studentAttendanceFlag: Int? = null
)

@Serializable
data class Esse3TranscriptRowWithQuestionnaireStatus(
    /** id univoco che consente di individuare il tratto di carriera dello studente e il relativo libretto collegato */
    @SerialName("matId")
    val matId: Long = 0L,

    /** progressivo di ordinamento delle attività, calcolato tenendo conto dei raggruppamenti e degli ordinamenti previsti a sistema */
    @SerialName("ord")
    val order: Int = 0,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    /** id univoco che consente di individuare la carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** id progressivo del piano di studio collegato tramite attuazione al libretto. */
    @SerialName("pianoId")
    val planId: Long? = null,

    /** id progressivo dell''attività del piano collegata con la riga di libretto */
    @SerialName("itmId")
    val itemId: Long? = null,

    /** se l''attività appartiene ad un raggruppamento contiene l''adsceID del padre del raggruppamento */
    @SerialName("ragId")
    val groupId: Long? = null,

    /** contiene la tipologia del tipo di raggruppamento, valorizzato solo sul padre del raggruppamento (cioà quando ragId=adsceId) */
    @SerialName("raggEsaTipo")
    val graduationGroupType: Esse3GraduationGroupType? = null,

    /** codice dell''attività didattica presente nel libretto. Il codice à copiato in ogni singolo libretto e, di norma, coincide con il codice previsto nell''offerta didattica alla quale l''attività didattica si riferisce */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell''attività didattica presente nel libretto, come il codice, la descrizione risulta copiata dall''offerta didattica, ma puà essere modificato */
    @SerialName("adDes")
    val activityDescription: String = "",

    /** anno di corso al quale à prevista l''attività didattica */
    @SerialName("annoCorso")
    val courseYear: Int = 0,

    /** Stato dell\'attività didattica (codice) */
    @SerialName("stato")
    val state: Esse3State,

    /** descrizione dello stato dell\'attività didattica */
    @SerialName("statoDes")
    val stateDescription: String = "",

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    /** codice del tipo di esame previsto per l''attività didattica */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    /** descrizione del tipo di esame previsto per l''attività didattica */
    @SerialName("tipoEsaDes")
    val graduationTypeDescription: String? = null,

    /** codice tipo di insegnamento previsto per l''attività didattica */
    @SerialName("tipoInsCod")
    val insertionTypeCode: String? = null,

    /** descrizione tipo di insegnamento previsto per l''attività didattica */
    @SerialName("tipoInsDes")
    val insertionTypeDescription: String? = null,

    /** Presenza di un riconoscimento o convalida. 0 = Nessun Riconoscimento 1 = RF (Riconoscimento di frequenza)  2 = RA (Riconoscimento di attività) 3 = CF (Convalida di frequenza) 4 = CA (Convalida di attività) */
    @SerialName("ricId")
    val searchId: Int = 0,

    /** codice del tipo di riconoscimento previsto per l''attività didattica */
    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    /** peso dell'attività didattica, calcolato come somma dei pesi dei segmenti, il peso prevede due decimali opzionali */
    @SerialName("peso")
    val weight: Float = 0f,

    /** anno di frequenza, valorizzato nel caso lo stato dell''attività sia F oppure S */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza, se valorizzata indica la data di riferimento dalla quale la frequenza risulta acquisita, il formato con cui deve essere definita la data à DD/MM/YYYY */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** frequenza assegnata di ufficio */
    @SerialName("freqUffFlg")
    val officialAttendanceFlag: Int = 0,

    /** frequenza obbligatoria */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int = 0,

    /** data di scadenza dell'iscirzione, l''attività non puà essere sostenuta dopo questa data, il formato con cui deve essere definita la data è DD/MM/YYYY */
    @SerialName("dataScadIscr")
    val enrollmentDeadline: String? = null,

    /** id della tipologia di voto utilizzata, indica quale range di voti à definito sull''attività didattica, il voto minimo positivo, il voto massimo e l''eventuale presenza della lode */
    @SerialName("gruppoVotoId")
    val gradeGroupId: Int? = null,

    /** indica il minimo voto positivo per la scala di voti selezionata */
    @SerialName("gruppoVotoMinVoto")
    val gradeGroupMinGrade: Int? = null,

    /** indica il massimo voto positivo per la scala di voti selezionata */
    @SerialName("gruppoVotoMaxVoto")
    val gradeGroupMaxGrade: Int? = null,

    /** indica se la scala di voti selezionata prevede la lode */
    @SerialName("gruppoVotoLodeFlg")
    val gradeGroupLodeFlag: Int? = null,

    /** codice del gruppo di giudizi a cui appartiene il giudizio presente nell''esito */
    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    /** descrizione del gruppo di giudizi a cui appartiene il giudizio selezionato */
    @SerialName("gruppoGiudDes")
    val judgmentGroupDescription: String? = null,

    @SerialName("esito")
    val outcome: Esse3Result? = null,

    /** attività sovrannumeraria */
    @SerialName("sovranFlg")
    val overrideFlag: Int = 0,

    /** note associate alla riga di libretto */
    @SerialName("note")
    val notes: String? = null,

    /** attività di debito */
    @SerialName("debitoFlg")
    val debtFlag: Int = 0,

    /** attività OFA */
    @SerialName("ofaFlg")
    val ofaFlag: Int? = null,

    /** anno corso di anticipo dell'attività, se valorizzato indica l'anno di corso a cui è stata anticipata l'attività */
    @SerialName("annoCorsoAnticipo")
    val anticipatedCourseYear: Int? = null,

    /** attività inserita automaticamente da una procedura diversa dalla attuazione piani carriera o dalla gestione libretto (prenotazione appelli, registrazione prove, ecc). */
    @SerialName("genAutoFlg")
    val autoGenerationFlag: Int = 0,

    /** attività inserita automaticamente realtiva al riconoscimento crediti nel caso di carriere specialistiche (3+2), contiene tutti i segmenti del tratto triennale della laurea specialistica. */
    @SerialName("genRicSpecFlg")
    val generateSpecialRequestFlag: Int = 0,

    /** indica il tipo di origine della ad del libretto 0 = AD non collegata a nessuna delibera 1 = AD inserita automaticamente in fase di attuazione e con delibera pendente (in questo caso l''AD non è collegata al piano) 2 = AD collegata alla delibera e inserita automaticamente dall'attivazione 3 = AD collegata alla delibera (passaggio di stato 1->3 dopo che la delibera viene approvata) 4 = AD relativa a delibera annullata, in questo caso l''ad non è collegata al piano */
    @SerialName("tipoOrigEvecar")
    val careerEventOriginType: Int? = null,

    /** url della pagina web dell'insegnamento */
    @SerialName("urlSitoWeb")
    val websiteUrl: String? = null,

    @SerialName("infoDottorati")
    val phdInfo: Esse3PhDTranscriptRowInfo? = null,

    @SerialName("rilFreq")
    val attendanceRelease: List<Esse3AttendanceRelease> = emptyList(),

    /** stato della missione */
    @SerialName("statoMissione")
    val missionState: String? = null,

    /** descrizione stato della missione */
    @SerialName("statoMissioneDes")
    val missionStateDescription: String? = null,

    /** contiene il numero di appelli prenotabili alla data di sistema per la riga di libretto */
    @SerialName("numAppelliPrenotabili")
    val bookableCallsNumber: Int? = null,

    /** attività superata, comprende anche il caso di prove verbalizzate ma non caricate in carriera per errori durante la procedura carica_prove */
    @SerialName("superataFlg")
    val passedFlag: Int? = null,

    /** contiene il numero di prenotazioni pendenti collegate alla riga di libretto. */
    @SerialName("numPrenotazioni")
    val bookingNumber: Int? = null,

    /** Indica se l´attività didattica è associata al conseguimento di una particolare abilitazione prevista dal corso di studio. */
    @SerialName("abilFlg")
    val authorizationFlag: Int? = null,

    /** adsce_id orgine della convalida per le AD che sono di completamento. */
    @SerialName("genConvAdsceId")
    val generateConventionTeachingActivityChoiceId: Long? = null,

    @SerialName("infoInterateneo")
    val interuniversityInfo: Esse3InteruniversityTranscriptRowInfo? = null,

    @SerialName("extraInfo")
    val extraInfo: Esse3ExtraTranscriptRow? = null,

    /** __Stato dei questionari di valutazione, può assumere i seguenti valori__ 0. Questionari non presenti 1. Questionari compilati 2. Alcuni questionari da compilare 3. Questionari da compilare 4. Errore nella configurazione, sono presenti più questionari con stato A */
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
    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long = 0L,

    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** anno di offerta di erogazione dell'attivit� didattica */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** Id del corso di studio di erogazione dell'attivit�� didattica */
    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    /** codice del corso di studio di erogazione dell'attivit� didattica */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione dell'attivit� didattica */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attivit� didattica */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    /** chiave del percorso di studio di erogazione dell'attivit� didattica */
    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    /** chiave dell'attivit� didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell''attivit� didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attivit� didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** chiave dell'unit� didattica */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** codice dell'unit� didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione dell'unit� didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** Id del docente che fa lezione */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** matricola del docente che fa lezione */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** nome del docente che fa lezione */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente che fa lezione */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** Id address book del docente che fa lezione */
    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null,

    /** codice del dominio di partizione */
    @SerialName("domPartAdCod")
    val domicilePartialTeachingActivityCode: String? = null,

    /** codice della partizione dell'anno accademico */
    @SerialName("partAdCod")
    val partialTeachingActivityCode: String? = null,

    /** codice del tipo credito dell'anno accademico */
    @SerialName("tipoCreAdCod")
    val teachingActivityCreditTypeCode: String? = null,

    /** chiave della struttura di riferimento */
    @SerialName("facAdId")
    val facultyTeachingActivityId: Long? = null,

    /** codice della struttura di riferimento */
    @SerialName("facAdCod")
    val facultyTeachingActivityCode: String? = null,

    /** descrizione della struttura di riferimento */
    @SerialName("facAdDes")
    val facultyTeachingActivityDescription: String? = null,

    /** chiave della struttura di riferimento */
    @SerialName("dipAdId")
    val departmentTeachingActivityId: Long? = null,

    /** codice della struttura di riferimento */
    @SerialName("dipAdCod")
    val departmentTeachingActivityCode: String? = null,

    /** descrizione della struttura di riferimento */
    @SerialName("dipAdDes")
    val departmentTeachingActivityDescription: String? = null,

    /** chiave del corso di studio di iscrizione dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di iscrizione dello studente */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di iscrizione dello studente */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** chiave della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** codice della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione della struttura di riferimento di iscrizione dello studente */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** sesso dello studente */
    @SerialName("sesso")
    val gender: String? = null,

    /** anno di nascita dello studente */
    @SerialName("annoNascita")
    val birthYear: Int? = null,

    /** Id del docente titolare */
    @SerialName("docenteTitId")
    val lecturerTitleId: Long? = null,

    /** matricola del docente titolare */
    @SerialName("docenteTitMatricola")
    val lecturerTitleMatricola: String? = null,

    /** nome del docente titolare */
    @SerialName("docenteTitNome")
    val lecturerTitleName: String? = null,

    /** cognome del docente titolare */
    @SerialName("docenteTitCognome")
    val lecturerTitleSurname: String? = null,

    /** Id address book del docente titolare */
    @SerialName("docenteTitIdAb")
    val lecturerTitleAbbreviatedId: Long? = null,

    /** codice della cittadinanza dello studente */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** ID codice MIUR della nazione di cittadinanza dello studente */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** descrizione della cittadinanza dello studente */
    @SerialName("cittDes")
    val citizenshipDescription: String? = null,

    /** descrizione della provincia dello studente */
    @SerialName("provDes")
    val provinceDescription: String? = null,

    /** Id del comune di residenza dello studente */
    @SerialName("comResId")
    val residenceMunicipalityId: Long? = null,

    /** codice di 4 cifre (Lettera + 3 numeri) che � utilizzato nel codice fiscale per indicare il comune di residenza dello studente */
    @SerialName("comResCod")
    val residenceMunicipalityCode: String? = null,

    /** descrizione del comune di residenza dello studente */
    @SerialName("comResDes")
    val residenceMunicipalityDescription: String? = null,

    /** Id della nazione di residenza dello studente */
    @SerialName("nazResId")
    val residenceNationId: Long? = null,

    /** ID codice MIUR della nazione di residenza dello studente */
    @SerialName("nazResCod")
    val residenceNationCode: String? = null,

    /** descrizione della nazione di residenza dello studente */
    @SerialName("nazResDes")
    val residenceNationDescription: String? = null,

    /** tipo del titolo di studio dello studente */
    @SerialName("tipoTitoloCod")
    val titleTypeCode: String? = null,

    /** anno di acquisizione dei crediti dello studente (dato successivo) */
    @SerialName("aaCfuStu")
    val academicYearStudentCFU: Long? = null,

    /** numero di crediti acquisiti dallo studente nell'anno di acquisizione (dato precedente) */
    @SerialName("cfuAaStu")
    val studentAcademicYearCredits: Float? = null,

    /** numero totale di crediti acquisiti dallo studente */
    @SerialName("cfuAcqStu")
    val studentAcquiredCredits: Float? = null,

    /** anno di compilazione del questionario */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** anno di coorte dello studente */
    @SerialName("aaRegId")
    val academicYearRegulationId: Int? = null,

    /** anno di corso dello studente */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** anno di corso della attivit� didattica */
    @SerialName("annoCorsoAd")
    val teachingActivityCourseYear: Int? = null,

    /** Data e ora della compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataOra")
    val dateTime: String? = null,

    /** Flag che indica se lo studente � frequentante */
    @SerialName("stuFreqFlg")
    val studentAttendanceFlag: Int? = null
)

@Serializable
data class Esse3QuestionnaireCompiledEventTagsTeacherAvailability(
    /** ID questionario compilato */
    @SerialName("questCompId")
    val questionComponentId: Long = 0L,

    /** ID questionario configurato */
    @SerialName("questionarioId")
    val questionnaireId: Long? = null,

    /** codice del questionario configurato */
    @SerialName("questionarioCod")
    val questionnaireCode: String? = null,

    /** descrizione del questionario configurato */
    @SerialName("questionarioDes")
    val questionnaireDescription: String? = null,

    /** Id del docente che fa lezione */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** matricola del docente che fa lezione */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** nome del docente che fa lezione */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente che fa lezione */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** Id address book del docente che fa lezione */
    @SerialName("docenteIdAb")
    val lecturerAbbreviatedId: Long? = null,

    /** chiave della struttura di riferimento */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** codice della struttura di riferimento */
    @SerialName("dipCod")
    val departmentCode: String? = null,

    /** descrizione della struttura di riferimento */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** anno di offerta di erogazione dell'attività didattica */
    @SerialName("aaOffAdId")
    val academicYearOfferActivityId: Int? = null,

    /** Id del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsAdId")
    val courseOfStudyTeachingActivityId: Long? = null,

    /** codice del corso di studio di erogazione dell'attività didattica */
    @SerialName("cdsAdCod")
    val courseOfStudyTeachingActivityCode: String? = null,

    /** descrizione del corso di erogazione dell'attività didattica */
    @SerialName("cdsAdDes")
    val courseOfStudyTeachingActivityDescription: String? = null,

    /** anno di ordinamento del corso di studio di erogazione dell'attività didattica */
    @SerialName("aaOrdAdId")
    val academicYearOrderActivityId: Int? = null,

    /** chiave del percorso di studio di erogazione dell'attività didattica */
    @SerialName("pdsAdId")
    val studyPlanTeachingActivityId: Long? = null,

    /** chiave dell'attività didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell''attività didattica */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione dell'attività didattica */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** chiave dell'unità didattica */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** codice dell'unità didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione dell'unità didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** codice del dominio di partizione */
    @SerialName("domPartAdCod")
    val domicilePartialTeachingActivityCode: String? = null,

    /** ID logistica a livello di attività didattica */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** ID logistica a livello di unità didattica */
    @SerialName("udLogId")
    val teachingUnitLogId: Long? = null,

    /** anno di compilazione del questionario */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Data e ora della compilazione del questionario. Il formato con cui deve essere definita la data DD/MM/YYYY */
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
