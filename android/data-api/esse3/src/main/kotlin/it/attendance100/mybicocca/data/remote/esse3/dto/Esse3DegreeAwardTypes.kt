package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3SupervisorsStatisticsSummary(
    /** identificativo del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** identificativo del soggetto esterno */
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
    /** codice modalità consultazione tesi */
    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    /** cdescrizione modalità consultazione tesi */
    @SerialName("des")
    val description: String? = null,

    /** abil flg */
    @SerialName("abilFlg")
    val authorizationFlag: Long? = null,

    /** codice modalità consultazione tesi */
    @SerialName("modAutAccTesiCod")
    val thesisAccessAuthorizationModeCode: String? = null,

    /** giorni di embargo della tesi */
    @SerialName("giorniEmbargo")
    val embargoDays: Int? = null,

    /** autorizzazione accesso tesi */
    @SerialName("autAccessoTesi")
    val thesisAccessAuthorization: String? = null
)

@Serializable
data class Esse3SupervisorTypeRegulation(
    /** identificativo del docente */
    @SerialName("regCtId")
    val committeeRegulationId: Long? = null,

    /** tipologia di relatore */
    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    /** descrizione tipologia del relatore */
    @SerialName("tipoRelDes")
    val relationTypeDescription: String? = null,

    /** numero minimo */
    @SerialName("numMin")
    val minNumber: Int? = null,

    /** numero massimo */
    @SerialName("numMax")
    val maxNumber: Int? = null,

    /** identificativo del docente */
    @SerialName("docenti")
    val lecturers: Int? = null,

    /** identificativo del soggetto esterno */
    @SerialName("soggEst")
    val externalSubject: Int? = null
)

@Serializable
data class Esse3TeachingDomainCancellation(
    /** identificativo della domanda conseguimento titolo */
    @SerialName("domCtId")
    val domicileCommitteeId: Long = 0L,

    /** identificativo dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** boolean che gestisce il controllo delle scadenze. True il controllo è stato effettuato altrimenti false. */
    @SerialName("checkScadenze")
    val deadlinesCheck: Boolean = false,

    /** stato della domanda. */
    @SerialName("statoDomanda")
    val applicationState: String? = null
)

@Serializable
data class Esse3CheckTeachingDomainCancellation(
    /** identificativo domanda */
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    /** identificativo anno concorso */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** identificativo concorso */
    @SerialName("testId")
    val testId: Long? = null,

    /** descrizione concorso */
    @SerialName("concorsoDes")
    val competitionDescription: String? = null,

    /** codice corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** codice corso di studio */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** anno iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    /** anno iscrizione */
    @SerialName("titAccAmmId")
    val adminTitleAccessId: Long? = null
)

@Serializable
data class Esse3ThesisAttachmentInsertMetadata(
    @SerialName("chiaviCollegamento")
    val linkKeys: List<Esse3LinkKey> = emptyList(),

    @SerialName("proprietaAggiuntive")
    val additionalProperties: List<Esse3AdditionalProperty> = emptyList(),

    /** nome del file */
    @SerialName("filename")
    val fileName: String? = null,

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String? = null,

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null
)

@Serializable
data class Esse3SupervisorsInsert(
    /** identificativo del relatore di tesi */
    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    /** identificativo del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** identificativo del soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** nota della tesi */
    @SerialName("notaTesi")
    val thesisNote: String? = null,

    /** richiesta eccellenza flag */
    @SerialName("ricEccellenzaFlg")
    val excellenceRequestFlag: Long = 0L,

    /** data richiesta eccellenza */
    @SerialName("dataRicEccellenza")
    val excellenceRequestDate: String? = null,

    /** punti proposti */
    @SerialName("puntiProp")
    val proposedPoints: Int? = null,

    /** motivazione proposta punti */
    @SerialName("motPuntiProp")
    val proposedPointsReason: String? = null
)

@Serializable
data class Esse3SupervisorsStatistics(
    /** identificativo del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** identificativo del soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** codice tipo relatore */
    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    /** numero di tesi associate al relatore */
    @SerialName("numTesi")
    val thesisNumber: Long? = null
)

@Serializable
data class Esse3TeachingCommission(
    /** identificativo docente commissione conseguimento titolo */
    @SerialName("docSedCtId")
    val committeeSessionDocumentId: Long? = null,

    /** idetificativo della commissione */
    @SerialName("commCtId")
    val committeeCallId: Long? = null,

    /** appellativo docente */
    @SerialName("appellativo")
    val title: String? = null,

    /** nome del docente */
    @SerialName("nome")
    val name: String? = null,

    /** cognome del docente */
    @SerialName("cognome")
    val surname: String? = null,

    /** codice del ruolo docente */
    @SerialName("ruoloCod")
    val roleCode: String? = null,

    /** descrizione del ruolo docente */
    @SerialName("ruoloDes")
    val roleDescription: String? = null,

    /** idetificativo AB */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** codice fiscale docente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** idetificativo docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** idetificativo soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null
)

@Serializable
data class Esse3SupervisorsDataInsert(
    /** Identificativo della tesi (pre) */
    @SerialName("preTesiId")
    val preThesisId: Long? = null,

    /** Identificativo del docente relatore */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** Identificativo del soggetto esterno relatore */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** Indica il codice che identifica la tipologia di relatore */
    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    /** Identificativo del dipartimento di afferenza del relatore. Valorizzato in automatico in fase di scelta del docente. */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** vengono riportate le annotazioni del relatore sulla tesi */
    @SerialName("notaTesi")
    val thesisNote: String? = null
)

@Serializable
data class Esse3ThesisAttachment(
    /** valore dell'ENUM ALLEGATI_TYPE collegata al tipo di allegato */
    @SerialName("allegatoTypeCod")
    val attachmentCategoryCode: String? = null,

    /** id dell'allegato */
    @SerialName("allegatoId")
    val attachmentId: Long? = null,

    /** identificativo della della domanda associata alla tesi */
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    /** identificativo della della domanda associata alla tesi */
    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    /** titolo dell'allegato */
    @SerialName("titolo")
    val title: String? = null,

    /** descrizione dell'allegato */
    @SerialName("allegatoDes")
    val attachmentDescription: String? = null,

    /** nome del file allegato */
    @SerialName("filename")
    val fileName: String? = null,

    /** estensione del file allegato */
    @SerialName("estensione")
    val extension: String? = null,

    /** flg che indica se l'allegato è definitivo o meno */
    @SerialName("defFlg")
    val definitionFlag: Int? = null,

    /** id dell'utente che ha inserito l'allegato */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** data modifica dati allegato */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** stato dell'allegato di tesi - A (approvato), R (rifiutato), I (inserito) */
    @SerialName("statoAllTesiCod")
    val thesisAttachmentStateCode: String? = null,

    /** motivazione relativa allo stato dell'allegato */
    @SerialName("motivazione")
    val reason: String? = null,

    /** data di approvazione o rfiuto dell'allegato */
    @SerialName("dataAppRif")
    val referenceCallDate: String? = null,

    /** descrizione dello stato dell'allegato di tesi - A (approvato), R (rifiutato), I (inserito) */
    @SerialName("statoAllTesiDes")
    val thesisAttachmentStateDescription: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipoAllegatoCod")
    val attachmentTypeCode: String? = null,

    /** codice tipologia allegato */
    @SerialName("tipologiaDes")
    val typologyDescription: String? = null,

    /** des des */
    @SerialName("autUsoAll")
    val attachmentUsageAuthorization: Int? = null,

    /** des des */
    @SerialName("dataAutUsoAll")
    val attachmentUsageAuthorizationDate: String? = null,

    /** riferimento antiplagio */
    @SerialName("indxAntiplagio")
    val antiplagiarismIndex: String? = null,

    /** link antiplagio */
    @SerialName("linkAntiplagio")
    val antiplagiarismLink: String? = null,

    /** nota antiplagio */
    @SerialName("notaAntiplagio")
    val antiplagiarismNote: String? = null,

    /** modalità consltazione tesi */
    @SerialName("modConsTesi")
    val thesisDiscussionMode: String? = null,

    /** autorizzazione accesso tesi */
    @SerialName("autAccessoTesi")
    val thesisAccessAuthorization: String? = null,

    /** giorni embargo */
    @SerialName("giorniEmbargo")
    val embargoDays: Int? = null,

    /** data fine embargo */
    @SerialName("dataFineEmbargo")
    val embargoEndDate: String? = null
)

@Serializable
data class Esse3ThesisIntoTeachingDomainInsert(
    /** idetificativo della domanda conseguimento titolo */
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    @SerialName("tesi")
    val thesis: Esse3ThesisDataInsert? = null
)

@Serializable
data class Esse3ThesisDiscussionModeCodeInsert(
    /** identificativo della tesi */
    @SerialName("tesiId")
    val thesisId: Long = 0L,

    /** codice modalità consultazione tesi */
    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null
)

@Serializable
data class Esse3ExamSessionLocation(
    /** identificativo della sessione */
    @SerialName("sesCtId")
    val committeeSessionId: Long? = null,

    /** identificativo dell'appello */
    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    /** progressivo della seduta */
    @SerialName("sedCtPrg")
    val committeeSessionProgram: Long? = null,

    /** identificativo dell'aula */
    @SerialName("aulaId")
    val classroomId: Long? = null,

    /** descrizione dell'aula */
    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    /** identificativo della commissione */
    @SerialName("commCtId")
    val committeeCallId: Long? = null,

    /** codice della seduta */
    @SerialName("sedutaCod")
    val sessionCode: String? = null,

    /** orario della seduta di laurea (HH24:MI) */
    @SerialName("orarioSeduta")
    val sessionSchedule: String? = null,

    /** data della seduta */
    @SerialName("dataSeduta")
    val sessionDate: String? = null,

    /** identificativo dell' anno */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** descrizione appello */
    @SerialName("desAppello")
    val examCallDescription: String? = null,

    /** descrizione sessione */
    @SerialName("desSessione")
    val sessionDescription: String? = null,

    /** dipartimento */
    @SerialName("dipartimento")
    val department: String? = null,

    /** descrizione dipartimento */
    @SerialName("desDipartimento")
    val departmentDescription: String? = null,

    @SerialName("membriCommissione")
    val committeeMembers: List<Esse3TeachingCommission> = emptyList()
)

@Serializable
data class Esse3ThesisSummary(
    /** identificativo della tesi di laurea */
    @SerialName("tesiId")
    val thesisId: Long? = null,

    /** identificativo dello studente */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** identificativo dell'appello a cui è legata la domanda */
    @SerialName("appCtId")
    val callCommitteeId: Int? = null,

    /** titolo della tesi di laurea */
    @SerialName("titoloTesiIta")
    val thesisTitleItalian: String? = null,

    /** Abstract del titolo della tesi di laurea */
    @SerialName("abstractTesiIta")
    val thesisAbstractItalian: String? = null,

    /** titolo della tesi di laurea in inglese */
    @SerialName("titoloTesiEng")
    val thesisTitleEnglish: String? = null,

    /** Abstract del titolo della tesi di laurea in inglese */
    @SerialName("abstractTesiEng")
    val thesisAbstractEnglish: String? = null,

    /** descrizione del tipo della tesi di laurea */
    @SerialName("TipoTesiDes")
    val thesisTypeDescription: String? = null,

    /** descrizione dell'attività didattica di tesi di laurea */
    @SerialName("AdGenDes")
    val genericActivityDescription: String? = null,

    /** descrizione dell'attività didattica di tesi di laurea cablata */
    @SerialName("AdTesiDes")
    val thesisActivityDescription: String? = null,

    /** indica se si tratta di una tesi di gruppo */
    @SerialName("TesiDiGruppo")
    val groupThesis: Int? = null,

    /** data deposito della tesi */
    @SerialName("dataDepositoTesi")
    val thesisDepositDate: String? = null,

    /** descrizione dello stato della tesi di laurea */
    @SerialName("StatoTesi")
    val thesisStatus: String? = null,

    /** identificativo della della domanda associata alla tesi */
    @SerialName("DomCtId")
    val teachingDomainId: Int? = null,

    /** descrizione lingua della tesi */
    @SerialName("linguaTesi")
    val thesisLanguage: String? = null,

    /** descrizione della lingua di discussione della tesi */
    @SerialName("linguaDiscussioneTesi")
    val thesisDiscussionLanguage: String? = null,

    /** identificativo lingua */
    @SerialName("linguaId")
    val languageId: Int? = null,

    /** codice identificativo lingua */
    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null,

    /** codice modalità consultazione tesi */
    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    @SerialName("allegatiTesi")
    val thesisAttachments: List<Esse3ThesisAttachment> = emptyList(),

    @SerialName("relatori")
    val supervisors: List<Esse3ThesisSupervisors> = emptyList()
)

@Serializable
data class Esse3SupervisorTeacher(
    /** id anagrafica del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** Codice settore scientifico disciplinare */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** matricola del docente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Cognome del docente */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome del docente */
    @SerialName("nome")
    val name: String? = null,

    /** Numero badge docente */
    @SerialName("badge")
    val badge: String? = null,

    /** Indirizzo di posta elettronica pubblica, ossia visualizzato nel web e stampato nei vari documenti (es. Guida studente) */
    @SerialName("email")
    val email: String? = null,

    /** Codice del dipartimento di afferenza del Docente */
    @SerialName("dipartimentoCod")
    val departmentCode: String? = null,

    /** Descrizione del dipartimento di afferenza del Docente */
    @SerialName("dipartimentoDes")
    val departmentDescription: String? = null,

    /** Codice della Facoltà di afferenza del Docente */
    @SerialName("facoltaCod")
    val facultyCode: String? = null,

    /** Descrizione della Facoltà di afferenza del Docente */
    @SerialName("facoltaDes")
    val facultyDescription: String? = null,

    /** Codice ruolo del docente */
    @SerialName("ruoloDocCod")
    val lecturerRoleCode: String? = null,

    /** Descrizione ruolo del docente */
    @SerialName("ruoloDocDes")
    val lecturerRoleDescription: String? = null,

    /** id dipartimento */
    @SerialName("dipId")
    val departmentId: Long? = null,

    /** Data di fine attivazione del docente. Se non valorizzato si considera il docente ancora attivo. */
    @SerialName("dataFinAtt")
    val activityEndDate: String? = null,

    /** identificativo lingua */
    @SerialName("linguaId")
    val languageId: Int? = null
)

@Serializable
data class Esse3TeachingDomainSummary(
    /** id della domanda di laurea */
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    /** identificativo dello studente */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** identificativo dello studente */
    @SerialName("matId")
    val matId: Int? = null,

    /** data domanda di laurea */
    @SerialName("dataDomandaCt")
    val committeeApplicationDate: String? = null,

    /** id anno accademico della domanda */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** stato della domanda conseguimento titolo */
    @SerialName("stato")
    val state: String? = null,

    /** sessione dell'appello scelto */
    @SerialName("sessioneCtDes")
    val committeeSessionDescription: String? = null,

    /** data inizio della sessione di laurea scelta per la domanda */
    @SerialName("dataInizioSessioneCt")
    val committeeSessionStartDate: String? = null,

    /** data inizio della sessione di laurea scelta per la domanda */
    @SerialName("dataFineSessioneCt")
    val committeeSessionEndDate: String? = null,

    /** appello selezionato in fase di presentazione della domanda conseguimento titolo */
    @SerialName("appelloCtDes")
    val callCommitteeDescription: String? = null,

    /** data appello selezionato in fase di presentazione della domanda conseguimento titolo */
    @SerialName("dataAppelloCt")
    val committeeCallDate: String? = null,

    /** data seduta di laurea scelta per la domanda */
    @SerialName("dataSedutaCt")
    val committeeSessionDate: String? = null,

    /** orario della seduta di laurea (HH24:MI) */
    @SerialName("orarioSedutaCt")
    val committeeSessionSchedule: String? = null,

    /** descrizione */
    @SerialName("EdificioDes")
    val buildingDescription: String? = null,

    /** descrizione */
    @SerialName("AulaDes")
    val classroomDescription: String? = null,

    /** identificativo della tesi di laurea associata alla domanda */
    @SerialName("tesiId")
    val thesisId: Int? = null,

    /** titolo della tesi di laurea */
    @SerialName("titoloTesi")
    val thesisTitle: String? = null,

    /** Abstract del titolo della tesi di laurea */
    @SerialName("abstractTesi")
    val thesisAbstract: String? = null,

    /** data deposito della tesi */
    @SerialName("dataDepositoTesi")
    val thesisDepositDate: String? = null,

    /** descrizione del tipo della tesi di laurea */
    @SerialName("TipoTesiDes")
    val thesisTypeDescription: String? = null,

    /** descrizione dell'attività didattica di tesi di laurea */
    @SerialName("AdGenDes")
    val genericActivityDescription: String? = null,

    /** descrizione dell'attività didattica di tesi di laurea libero */
    @SerialName("AdTesiDes")
    val thesisActivityDescription: String? = null,

    /** flag che indica se la tesi associata alla domanda è di Gruppo */
    @SerialName("TesiDiGruppo")
    val groupThesis: Int? = null,

    /** 1a parola chiave di almalaurea */
    @SerialName("KeywordAlma1")
    val almaKeyword1: String? = null,

    /** 2a parola chiave di almalaurea */
    @SerialName("KeywordAlma2")
    val almaKeyword2: String? = null,

    /** 3a parola chiave di almalaurea */
    @SerialName("KeywordAlma3")
    val almaKeyword3: String? = null,

    /** 4a parola chiave di almalaurea */
    @SerialName("KeywordAlma4")
    val almaKeyword4: String? = null,

    /** 5a parola chiave di almalaurea */
    @SerialName("KeywordAlma5")
    val almaKeyword5: String? = null,

    /** codice identificativo dello stato della tesi di laurea */
    @SerialName("StatoTesiCod")
    val thesisStatusCode: String? = null,

    /** data conseguimento titolo */
    @SerialName("dataCt")
    val committeeDate: String? = null,

    /** flag che Indica se la tesi è stata consegnata in formato elettronico */
    @SerialName("tesiElettronica")
    val electronicThesis: Int? = null,

    /** numero del MAV di pagamento relativo alle tasse del processo CTIT */
    @SerialName("numeroMavCtit")
    val titleMavNumber: String? = null,

    /** data di pagamento del mav relativo alle tasse del processo CTIT */
    @SerialName("dataPagamentoCtit")
    val titlePaymentDate: String? = null,

    /** numero del MAV di pagamento relativo alle tasse del processo PERGA */
    @SerialName("numeroMavPerga")
    val parchmentMavNumber: String? = null,

    /** data di pagamento del mav relativo alle tasse del processo CTIT */
    @SerialName("dataPagamentoPerga")
    val parchmentPaymentDate: String? = null,

    /** identificativo lingua */
    @SerialName("linguaId")
    val languageId: Int? = null,

    /** codice identificativo lingua */
    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null,

    /** voto finale */
    @SerialName("votoFinale")
    val finalGrade: Long? = null,

    /** lode flg */
    @SerialName("lodeFlg")
    val cumLaudeFlag: Long? = null,

    /** punti aggiuntivi */
    @SerialName("puntiAgg")
    val additionalPoints: Double? = null,

    /** descrizione giudizio */
    @SerialName("tipiGiudProFinDes")
    val finalProJudgmentTypesDescription: String? = null,

    /** lode flg */
    @SerialName("menzioneFlg")
    val mentionFlag: Long? = null,

    /** lode flg */
    @SerialName("encomioFlg")
    val commendationFlag: Long? = null,

    /** cfu finali */
    @SerialName("cfuFinali")
    val finalCredits: Double? = null
)

@Serializable
data class Esse3ThesisTypes(
    /** codice rappresentante la tipologia di tesi */
    @SerialName("tipoTesiCod")
    val thesisTypeCode: String? = null,

    /** descrizione della tipologia di tesi */
    @SerialName("des")
    val description: String? = null,

    /** descrizione della sessione */
    @SerialName("miurTipoTesiCod")
    val miurThesisTypeCode: String? = null,

    /** identificativo delle regole di conseguimento titolo */
    @SerialName("regCtId")
    val committeeRegulationId: Int? = null
)

@Serializable
data class Esse3ThesisSupervisorsDepartments(
    /** codice dipartimento */
    @SerialName("dipartimentoCod")
    val departmentCode: String? = null,

    /** descrizione dipartimento */
    @SerialName("dipartimentoDes")
    val departmentDescription: String? = null,

    /** Data inizio. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** Data fine. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("dataFine")
    val endDate: String? = null,

    @SerialName("relatori")
    val supervisors: List<Esse3SupervisorsStatisticsSummary> = emptyList()
)

@Serializable
data class Esse3ApplicationDataInsert(
    /** identificativo dello studente */
    @SerialName("stuId")
    val studentId: Int = 0,

    /** Anno a cui è associata la sessione dell'appello */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** data presentazione della domanda di conseguimento titolo */
    @SerialName("dataDomCt")
    val committeeApplicationDate: String? = null,

    /** id dell'appello di laurea */
    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    /** id della sessione di laurea */
    @SerialName("sesCtId")
    val committeeSessionId: Long? = null,

    /** stato del questionario almalaurea */
    @SerialName("quesAlma")
    val almaQuestion: String? = null,

    /** tipologia della domanda di conseguimento titolo */
    @SerialName("tipoDomCt")
    val committeeApplicationType: String? = null,

    /** stage dello studente */
    @SerialName("stage")
    val stage: String? = null,

    /** stage dello studente in lingua inglese */
    @SerialName("stageEng")
    val stageEnglish: String? = null,

    /** Descrizione dell´eventuale project work effettuato ai fini del conseguimento titolo. */
    @SerialName("projectWork")
    val projectWork: String? = null,

    /** Descrizione dell´eventuale project work effettuato ai fini del conseguimento titolo in lingua inglese */
    @SerialName("projectWorkEng")
    val projectWorkEnglish: String? = null,

    /** id del Corso di Studio */
    @SerialName("cdsDicId")
    val courseOfStudyDictionaryId: Long? = null,

    /** id dell'anno di ordinamento */
    @SerialName("aaOrdDicId")
    val academicYearOrderDictionaryId: Long? = null,

    /** id del percorso di studio */
    @SerialName("pdsDicId")
    val studyPlanDictionaryId: Long? = null,

    /** indica se lo studente laureando in una laurea triennale proseguirà con una laurea specialistica */
    @SerialName("prosSpecFlg")
    val specialNextFlag: Int? = null,

    /** identificativo dello studente sulla tabella p18_reg_azioni_stu */
    @SerialName("regAzionistuId")
    val shareholderRegistrationId: Long? = null,

    /** id del Corso di Studio relativo all'iscrizione alla specialistica */
    @SerialName("cdsProsId")
    val courseOfStudyNextId: Long? = null,

    /** id dell'anno di ordinamento relativo all'iscrizione alla specialistica */
    @SerialName("aaOrdProsId")
    val academicYearOrderProspectId: Long? = null,

    /** id del percorso di studio relativo all'iscrizione alla specialistica */
    @SerialName("pdsProsId")
    val studyPlanNextId: Long? = null,

    /** id del Identificativo della lingua quadriennale seguita dallo studente. */
    @SerialName("linguaQuadId")
    val languageQuadId: Long? = null,

    /** Annotazioni */
    @SerialName("nota")
    val note: String? = null,

    /** indica il numero di telefono dello studente nel periodo di tesi */
    @SerialName("numTel")
    val phoneNumber: String? = null,

    /** identificativo della domanda di conseguimento titolo */
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    /** indica il numero di mesi di proroga per il conseguimento titolo di un dottorato */
    @SerialName("mesiProrogaCt")
    val committeeExtensionMonths: Int? = null,

    /** identificativo della regola di conseguimento titolo relativa alla domanda di conseguimento titolo */
    @SerialName("regCtId")
    val committeeRegulationId: Long? = null,

    /** flag che indica l'eventuale trasmissione dei dati della domanda di conseguimento titolo tramite la procedura Vulcano */
    @SerialName("consVulcanoFlg")
    val consentVulcanoFlag: Int? = null,

    /** flag che indica che lo studente che presenta la domanda di conseguimento titolo ha sostenuto tutti gli esami previsti dal suo piano di studio */
    @SerialName("esaSostFlg")
    val graduationSubstitutionFlag: Int? = null,

    /** flag di autorizzazione della pubblicazione su web dei dati relativi al conseguimento titolo */
    @SerialName("autWebFlg")
    val webAuthorizationFlag: Int? = null,

    /** indica il codice relativo al tipo esame conseguimento titolo */
    @SerialName("tipoEsactCod")
    val exactGraduationTypeCode: String? = null,

    /** Flag che indica se la tesi è di gruppo */
    @SerialName("tesiDiGruppoFlg")
    val groupThesisFlag: Int? = null,

    /** Conferma espicita dello studente. La conferma esplicita è una fase che può essere richiesta allo studente prima che la sua domanda sia effettivamente confermata dalla segreteria */
    @SerialName("confEsplicitaFlg")
    val explicitConfirmationFlag: Int? = null,

    /** Flag che Indica se lo studente ha fatto richiesta della pergamena di laurea in fase di conseguimento titolo */
    @SerialName("ricPergaFlg")
    val parchmentRequestFlag: Int? = null,

    /** Regole di riferimento da utilizzare nel motore controllo REGOLE e Piani per lo studente (0 Utilizzo delle regole di percorso (OFFF), 1 Utilizzo del RAD) */
    @SerialName("motCtrlRifreg")
    val regularReferenceControlReason: Int? = null,

    /** Flag che indica se lo studente ha già effettuato la registrazione sul sito di AlmaLaurea */
    @SerialName("regAlmaFlg")
    val almaRegistrationFlag: Int? = null,

    /** Identificativo del raggruppamento per stampa delle pergamene/minute */
    @SerialName("pergaNumRaggr")
    val parchmentGroupNumber: Long? = null
)

@Serializable
data class Esse3ThesisDataInsert(
    /** identificativo dello studente */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** identificativo della regola di conseguimento titolo relativa alla domanda di conseguimento titolo */
    @SerialName("regCtId")
    val committeeRegulationId: Long? = null,

    /** codice della tipologia della tesi */
    @SerialName("tipoTesiCod")
    val thesisTypeCode: String? = null,

    /** data deposito della tesi */
    @SerialName("dataDepositoTesi")
    val thesisDepositDate: String? = null,

    /** titolo della tesi di laurea */
    @SerialName("titoloTesiIta")
    val thesisTitleItalian: String? = null,

    /** Abstract del titolo della tesi di laurea */
    @SerialName("abstractTesiIta")
    val thesisAbstractItalian: String? = null,

    /** titolo della tesi di laurea in inglese */
    @SerialName("titoloTesiEng")
    val thesisTitleEnglish: String? = null,

    /** Abstract del titolo della tesi di laurea in inglese */
    @SerialName("abstractTesiEng")
    val thesisAbstractEnglish: String? = null,

    /** descrizione del tipo della tesi di laurea */
    @SerialName("TipoTesiDes")
    val thesisTypeDescription: String? = null,

    /** codice del settore dell'ad di tesi */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** Codice identificativo del settore scientifico disciplinare di riferimento */
    @SerialName("settDiscCod")
    val disciplinarySectorCode: String? = null,

    /** Codice identificativo della disciplina (all´interno del settore scientifico disciplinare) */
    @SerialName("discCod")
    val disciplineCode: String? = null,

    /** ID dell´attività didattica di tesi */
    @SerialName("adId")
    val activityId: Long? = null,

    /** ID dell´attività didattica. */
    @SerialName("udAdId")
    val teachingUnitTeachingActivityId: Long? = null,

    /** id del Corso di Studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id dell'anno accademico di offerta didattica */
    @SerialName("aaOffId")
    val academicYearOfferId: Long? = null,

    /** id unità didattica */
    @SerialName("udId")
    val teachingUnitId: Long? = null,

    /** descrizione dell'attività didattica di tesi di laurea */
    @SerialName("adTesiDes")
    val thesisActivityDescription: String? = null,

    /** identificativo della lingua della tesi */
    @SerialName("linguaTesi")
    val thesisLanguage: Long? = null,

    /** identificativo della  lingua di discussione della tesi */
    @SerialName("linguaDiscussioneTesi")
    val thesisDiscussionLanguage: Long? = null,

    /** 1a parola chiave di almalaurea */
    @SerialName("keywordAlma1")
    val almaKeyword1: String? = null,

    /** 2a parola chiave di almalaurea */
    @SerialName("keywordAlma2")
    val almaKeyword2: String? = null,

    /** 3a parola chiave di almalaurea */
    @SerialName("keywordAlma3")
    val almaKeyword3: String? = null,

    /** 4a parola chiave di almalaurea */
    @SerialName("keywordAlma4")
    val almaKeyword4: String? = null,

    /** 5a parola chiave di almalaurea */
    @SerialName("keywordAlma5")
    val almaKeyword5: String? = null,

    /** nota definita sulla tesi di laurea */
    @SerialName("nota")
    val note: String? = null,

    /** Flag che ndica se la tesi è stata consegnata in formato elettronico */
    @SerialName("tesiElettrFlg")
    val electronicThesisFlag: Int? = null,

    /** Codice della modalità di consenso alla consultazione della propria tesi */
    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    /** Codice della modalità di riproduzione della tesi */
    @SerialName("modRiprTesiCod")
    val thesisRestartModeCode: String? = null,

    /** Flag che indica se si tratta di una tesina */
    @SerialName("tesinaFlg")
    val shortThesisFlag: Int? = null,

    /** Flag che Indica se la preparazione della tesi è avvenuta all´estero */
    @SerialName("esteroFlg")
    val foreignFlag: Int? = null,

    /** Tipologia di recapito della tesi */
    @SerialName("tipoRecapitoCod")
    val contactTypeCode: String? = null,

    /** Recapito della tesi libero, utilizzabile nel caso in cui sia indicato ALTRO come tipologia di recapito tesi */
    @SerialName("altroRecapitoTesi")
    val otherContactThesis: String? = null,

    /** Tipologia di recapito della pergamena */
    @SerialName("tipoRecapitoPergaCod")
    val parchmentDeliveryContactTypeCode: String? = null,

    /** Recapito della pergamena libero, utilizzabile nel caso in cui sia indicato ALTRO come tipologia di recapito pergamena. */
    @SerialName("altroRecapitoPerga")
    val otherContactParchment: String? = null,

    /** Data minima della sessione di conseguimento titolo che potrà essere scelta dallo studente */
    @SerialName("dataMinSesCt")
    val minimumCommitteeSessionDate: String? = null,

    /** identificativo dello studente sulla tabella p18_reg_azioni_stu */
    @SerialName("regAzionistuId")
    val shareholderRegistrationId: Long? = null,

    /** identificativo della tesi dello studente */
    @SerialName("tesiId")
    val thesisId: Long? = null,

    /** 1a parola chiave di almalaurea in inglese */
    @SerialName("keywordAlmaEng1")
    val almaKeywordEnglish1: String? = null,

    /** 2a parola chiave di almalaurea in inglese */
    @SerialName("keywordAlmaEng2")
    val almaKeywordEnglish2: String? = null,

    /** 3a parola chiave di almalaurea in inglese */
    @SerialName("keywordAlmaEng3")
    val almaKeywordEnglish3: String? = null,

    /** 4a parola chiave di almalaurea in inglese */
    @SerialName("keywordAlmaEng4")
    val almaKeywordEnglish4: String? = null,

    /** 5a parola chiave di almalaurea in inglese */
    @SerialName("keywordAlmaEng5")
    val almaKeywordEnglish5: String? = null,

    /** Indica se lo studente ha confermato esplicitamente di aver caricato la tesi definitiva a sistema. */
    @SerialName("confTesiDefFlg")
    val thesisDefenseConfirmationFlag: Int? = null,

    /** Indica se l`attività di completamento tesi è stata effettuata almeno una volta da web */
    @SerialName("complTesi")
    val thesisCompletion: Int? = null,

    /** Mesi di embargo della tesi. */
    @SerialName("mesiEmbargo")
    val embargoMonths: Int? = null,

    /** Codice della tipologia di autorizzazione per la verifica dell'autenticità della tesi */
    @SerialName("tipoAutVerifTesiCod")
    val thesisVerificationAuthorizationTypeCode: String? = null,

    /** Indica la Nazione del recapito della pergamena libero */
    @SerialName("recPergaNazioneId")
    val parchmentDeliveryNationId: Long? = null,

    /** Indica il comune del recapito della pergamena libero */
    @SerialName("recPergaComuneId")
    val parchmentDeliveryMunicipalityId: Long? = null,

    /** Indica l'Indirizzo del recapito della pergamena libero */
    @SerialName("recPergaVia")
    val parchmentDeliveryStreet: String? = null,

    /** Indica il numero civico del recapito della pergamena libero */
    @SerialName("recPergaNumCiv")
    val parchmentDeliveryStreetNumber: String? = null,

    /** Indica il CAP del recapito della pergamena libero */
    @SerialName("recPergaCap")
    val parchmentDeliveryPostalCode: String? = null,

    /** Indica il CAP del recapito della pergamena libero */
    @SerialName("recPergaCitstra")
    val parchmentDeliveryForeignCity: String? = null,

    /** indica se si tratta di una tesi di gruppo */
    @SerialName("TesiDiGruppo")
    val groupThesis: Int? = null,

    @SerialName("relatori")
    val supervisors: List<Esse3SupervisorsDataInsert> = emptyList()
)

@Serializable
data class Esse3ThesisSupervisorsTeachingDomain(
    /** id della tesi a cui è legato il relatore */
    @SerialName("tesiId")
    val thesisId: Long? = null,

    /** tipologia di relatore */
    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    /** descrizione tipologia del relatore */
    @SerialName("tipoRelDes")
    val relationTypeDescription: String? = null,

    /** identificativo del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** identificativo della domanda */
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    /** identificativo dell'appello */
    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    /** identificativo del soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** nota della tesi */
    @SerialName("notaTesi")
    val thesisNote: String? = null,

    /** identificativo del soggetto esterno */
    @SerialName("ricEccellenzaFlg")
    val excellenceRequestFlag: Long? = null,

    /** data richiesta eccellenza */
    @SerialName("dataRicEccellenza")
    val excellenceRequestDate: String? = null,

    /** identificativo del docente */
    @SerialName("puntiProp")
    val proposedPoints: Int? = null,

    /** motivazione punteggio proposto */
    @SerialName("motPuntiProp")
    val proposedPointsReason: String? = null
)

@Serializable
data class Esse3ThesisTeachingDomainSummary(
    /** identificativo della tesi di laurea */
    @SerialName("tesiId")
    val thesisId: Long? = null,

    /** identificativo dello studente */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** identificativo dell'appello a cui è legata la domanda */
    @SerialName("appCtId")
    val callCommitteeId: Int? = null,

    /** titolo della tesi di laurea */
    @SerialName("titoloTesiIta")
    val thesisTitleItalian: String? = null,

    /** Abstract del titolo della tesi di laurea */
    @SerialName("abstractTesiIta")
    val thesisAbstractItalian: String? = null,

    /** titolo della tesi di laurea in inglese */
    @SerialName("titoloTesiEng")
    val thesisTitleEnglish: String? = null,

    /** Abstract del titolo della tesi di laurea in inglese */
    @SerialName("abstractTesiEng")
    val thesisAbstractEnglish: String? = null,

    /** descrizione del tipo della tesi di laurea */
    @SerialName("TipoTesiDes")
    val thesisTypeDescription: String? = null,

    /** descrizione dell'attività didattica di tesi di laurea */
    @SerialName("AdGenDes")
    val genericActivityDescription: String? = null,

    /** descrizione dell'attività didattica di tesi di laurea cablata */
    @SerialName("AdTesiDes")
    val thesisActivityDescription: String? = null,

    /** indica se si tratta di una tesi di gruppo */
    @SerialName("TesiDiGruppo")
    val groupThesis: Int? = null,

    /** data deposito della tesi */
    @SerialName("dataDepositoTesi")
    val thesisDepositDate: String? = null,

    /** descrizione dello stato della tesi di laurea */
    @SerialName("StatoTesi")
    val thesisStatus: String? = null,

    /** identificativo della della domanda associata alla tesi */
    @SerialName("DomCtId")
    val teachingDomainId: Int? = null,

    /** descrizione lingua della tesi */
    @SerialName("linguaTesi")
    val thesisLanguage: String? = null,

    /** descrizione della lingua di discussione della tesi */
    @SerialName("linguaDiscussioneTesi")
    val thesisDiscussionLanguage: String? = null,

    /** identificativo lingua */
    @SerialName("linguaId")
    val languageId: Int? = null,

    /** codice identificativo lingua */
    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null,

    /** codice modalità consultazione tesi */
    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    @SerialName("allegatiTesi")
    val thesisAttachments: List<Esse3ThesisAttachment> = emptyList(),

    @SerialName("relatori")
    val supervisors: List<Esse3ThesisSupervisorsTeachingDomain> = emptyList()
)

@Serializable
data class Esse3AntiplagiarismDataInsert(
    /** identificativo allegato */
    @SerialName("allegatoId")
    val attachmentId: Long = 0L,

    /** riferimento antiplagio */
    @SerialName("indxAntiplagio")
    val antiplagiarismIndex: String? = null,

    /** link antiplagio */
    @SerialName("linkAntiplagio")
    val antiplagiarismLink: String? = null,

    /** nota antiplagio */
    @SerialName("notaAntiplagio")
    val antiplagiarismNote: String? = null,

    /** flag di approvazione allegato definitivo e quindi approvazione */
    @SerialName("approvazioneAllegFlg")
    val attachmentApprovalFlag: Long? = null
)

@Serializable
data class Esse3ExamSessionDeadlines(
    /** id dell'appello di laurea */
    @SerialName("appCtId")
    val callCommitteeId: Long? = null,

    /** tipo di scadenza */
    @SerialName("tipoScadenzaCod")
    val expirationTypeCode: String? = null,

    /** tipo di scadenza */
    @SerialName("tipoScadenzaDes")
    val expirationTypeDescription: String? = null,

    /** data inizio dell'intervallo di date della tipologia di scadenza configurata sull'appello di laurea */
    @SerialName("dataScaIni")
    val initialDeadline: String? = null,

    /** data fine dell'intervallo di date della tipologia di scadenza configurata sull'appello di laurea */
    @SerialName("dataScaFin")
    val finalDeadline: String? = null,

    /** identificativo lingua */
    @SerialName("linguaId")
    val languageId: Int? = null,

    /** codice identificativo lingua */
    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null
)

@Serializable
data class Esse3ThesisReplica(
    /** identificativo della tesi */
    @SerialName("tesiId")
    val thesisId: Long? = null,

    /** codice modalità consultazione tesi */
    @SerialName("modConsTesiCod")
    val thesisDiscussionModeCode: String? = null,

    /** codice modalità consultazione tesi */
    @SerialName("modConsTesiDes")
    val thesisDiscussionModeDescription: String? = null,

    /** autorizzazione accesso tesi */
    @SerialName("autAccessoTesi")
    val thesisAccessAuthorization: String? = null,

    /** giorni embargo */
    @SerialName("giorniEmbargo")
    val embargoDays: Int? = null,

    /** data fine embargo */
    @SerialName("dataFineEmbargo")
    val embargoEndDate: String? = null,

    /** identificativo del documento */
    @SerialName("idDocumento")
    val documentId: String? = null,

    /** data documento */
    @SerialName("dataDocumento")
    val documentDate: String? = null,

    /** identificativo Titulus */
    @SerialName("titulusId")
    val titulusId: String? = null,

    /** identificativo Iris */
    @SerialName("irisId")
    val irisId: String? = null,

    /** identificativo Unitesi */
    @SerialName("unitesiId")
    val integratedTeachingUnitId: Long? = null
)

@Serializable
data class Esse3ThesisSupervisors(
    /** id della tesi a cui è legato il relatore */
    @SerialName("tesiId")
    val thesisId: Long? = null,

    /** tipologia di relatore */
    @SerialName("tipoRelCod")
    val relationTypeCode: String? = null,

    /** descrizione tipologia del relatore */
    @SerialName("tipoRelDes")
    val relationTypeDescription: String? = null,

    /** identificativo del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** identificativo della domanda */
    @SerialName("domCtId")
    val domicileCommitteeId: Long? = null,

    /** identificativo del soggetto esterno */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** nota della tesi */
    @SerialName("notaTesi")
    val thesisNote: String? = null,

    /** identificativo del soggetto esterno */
    @SerialName("ricEccellenzaFlg")
    val excellenceRequestFlag: Long? = null,

    /** data richiesta eccellenza */
    @SerialName("dataRicEccellenza")
    val excellenceRequestDate: String? = null,

    /** identificativo del docente */
    @SerialName("puntiProp")
    val proposedPoints: Int? = null,

    /** motivazione punteggio proposto */
    @SerialName("motPuntiProp")
    val proposedPointsReason: String? = null
)

@Serializable
data class Esse3ImportSupervisorsResponse(
    /** eseguita con successo o fallita */
    @SerialName("esitoElaborazione")
    val processingOutcome: String? = null,

    @SerialName("elencoErrori")
    val errorList: List<kotlinx.serialization.json.JsonObject> = emptyList()
)
