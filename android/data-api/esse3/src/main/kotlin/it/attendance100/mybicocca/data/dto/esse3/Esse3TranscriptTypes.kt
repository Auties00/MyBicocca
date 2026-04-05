package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3AttendanceParameters(
    /** stato dell'attività didattica F imposta la frequenza P la rimuove */
    @SerialName("staSceCod")
    val choiceStatusCode: String = "",

    /** anno di acquisizione della frequenza */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** percentuale di completamento della frequenza */
    @SerialName("percentualePresenza")
    val presencePercentage: Float? = null,

    /** percentuale ore di frequnza */
    @SerialName("percentualeOrePresenza")
    val presenceHoursPercentage: Float? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList()
)

@Serializable
data class Esse3TranscriptAverage(
    /** Tipo di definizione della media recuperando il valore della base dall''ordinamento (CDSORD) oppure in base 110 (CDS) */
    @SerialName("definizioneBase")
    val baseDefinition: Esse3BaseDefinition,

    /** descrizione della tipologia di media prevista Aritmetica o Ponderata */
    @SerialName("tipoMediaCod")
    val averageTypeCode: Esse3AverageTypeCode,

    /** Base su cui viene calcolata la media */
    @SerialName("base")
    val base: Int = 0,

    /** valore impostato a 1 se il tipo di media è coerente con quella prevista dalle regole */
    @SerialName("tipoOk")
    val okType: Int = 0,

    /** media calcolata */
    @SerialName("media")
    val average: Float = 0f
)

@Serializable
data class Esse3SingleAttendanceParameters(
    /** stato dell'attività didattica F imposta la frequenza P la rimuove */
    @SerialName("staSceCod")
    val choiceStatusCode: String = "",

    /** anno di acquisizione della frequenza */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** percentuale di completamento della frequenza */
    @SerialName("percentualePresenza")
    val presencePercentage: Float? = null,

    /** percentuale ore di frequnza */
    @SerialName("percentualeOrePresenza")
    val presenceHoursPercentage: Float? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList(),

    /** anno della rilevazione, se non passato viene preso il DR_CARR */
    @SerialName("aaRilevazioneId")
    val academicYearSurveyId: Int? = null,

    /** codice fiscale del docente nel caso il servizio venga invocato come utente tecnico */
    @SerialName("codFisDocenteRilevazione")
    val lecturerFiscalCodeDetection: String? = null,

    /** codice fiscale del docente nel caso il servizio venga invocato come utente tecnico, per evitare il controllo passare stringa vuota */
    @SerialName("codFisDocenteControllo")
    val lecturerFiscalCodeCheck: String? = null,

    /** totale rilevazioni */
    @SerialName("totaleRilevazioni")
    val totalDetections: Int? = null,

    /** ore totali di rilevazioni */
    @SerialName("totaleOreRilevazioni")
    val totalDetectionHours: Float? = null
)

@Serializable
data class Esse3RecognitionConversionSegmentParameters(
    /** id del segmento per il quale convalidare i CFU */
    @SerialName("segsceId")
    val segmentChoiceId: Long = 0L,

    /** peso riconosciuto/convalidato del segmento */
    @SerialName("peso")
    val weight: Float? = null
)

@Serializable
data class Esse3TranscriptStats(
    /** id del tratto di carriera su cui calcolare le statistiche */
    @SerialName("matId")
    val matId: Long? = null,

    /** codice tipo di unità di misura del corso a cui è iscritto lo studente, le statistiche sono relative a questa unità di misura */
    @SerialName("umPesoCod")
    val measurementUnitWeightCode: String? = null,

    /** descrizione tipo di unità di misura del corso a cui è iscritto lo studente */
    @SerialName("umPesoDes")
    val measurementUnitWeightDescription: String? = null,

    /** peso minimo da ottenere per il superamento del titolo */
    @SerialName("umPesoMin")
    val minMeasurementUnitWeight: Float? = null,

    /** peso massimo da ottenere per il superamento del titolo */
    @SerialName("umPesoMax")
    val maxMeasurementUnitWeight: Float? = null,

    /** peso delle AD superate nel libretto, rappresenta la somma dei pesi delle attività superate non sovrannumerarie */
    @SerialName("umPesoSuperato")
    val passedMeasurementUnitWeight: Float? = null,

    /** peso delle AD frequentate nel libretto, rappresenta la somma dei pesi delle attività frequentate non sovrannumerarie */
    @SerialName("umPesoFrequentato")
    val attendedMeasurementUnitWeight: Float? = null,

    /** peso delle AD pianificate nel libretto, rappresenta la somma dei pesi delle attività pianificate non sovrannumerarie */
    @SerialName("umPesoPianificato")
    val plannedMeasurementUnitWeight: Float? = null,

    /** peso delle AD superate collegate al piano approvato */
    @SerialName("umPesoPiano")
    val studyPlanMeasurementUnitWeight: Float? = null,

    /** peso delle AD superate nel libretto secondo le regole di percorso, vengono scartati i pesi dei segmenti non compatibili con le regole di percorso */
    @SerialName("umPesoCalcolato")
    val calculatedMeasurementUnitWeight: Float? = null,

    /** peso delle AD convalidate nel libretto secondo le regole di percorso */
    @SerialName("umPesoConvalidato")
    val validatedMeasurementUnitWeight: Float? = null,

    /** numero di attività present nel libretto e valide ai fini del conseguimento del titolo */
    @SerialName("numAdLibretto")
    val bookletTeachingActivityNumber: Int? = null,

    /** numero di attività previste nel piano ai fini del conseguimento del titolo */
    @SerialName("numAdPiano")
    val studyPlanTeachingActivityNumber: Int? = null,

    /** numero di attività superate nel libretto (non sovrannumerarie) */
    @SerialName("numAdSuperate")
    val passedTeachingActivityNumber: Int? = null,

    /** numero di attività frequentate nel libretto (non sovrannumerarie) */
    @SerialName("numAdFrequentate")
    val attendedTeachingActivityNumber: Int? = null,

    /** numero di attività in stato pianificato nel libretto (non sovrannumerarie) */
    @SerialName("numAdPianificate")
    val plannedTeachingActivityNumber: Int? = null,

    @SerialName("gruppoVoto")
    val gradeGroup: Esse3VoteGroup? = null,

    @SerialName("medie")
    val averages: List<Esse3TranscriptAverage> = emptyList()
)

@Serializable
data class Esse3TranscriptSegmentInsertionAttributes(
    /** codice dell''unità didattica a cui appartiene il semgento */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione dell''unità didattica a cui appartiene il segmento */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** tipo di credito previsto per il segmento */
    @SerialName("tipoCreCod")
    val creditTypeCode: String = "",

    /** Tipo Attività formativa */
    @SerialName("taf")
    val taf: String? = null,

    /** id ambito */
    @SerialName("ambId")
    val environmentId: Long? = null,

    /** codice settore scientifico disciplinare */
    @SerialName("settCod")
    val sectorCode: String = "",

    /** tipo di disciplina prevista, valida se il CDSORD non è definto a Settori */
    @SerialName("discCod")
    val disciplineCode: String? = null,

    /** peso del segmento secondo l''unità di misura prevista dalla riga di libretto */
    @SerialName("peso")
    val weight: Float = 0f,

    /** durata in ore del segmento */
    @SerialName("durata")
    val duration: Float? = null,

    /** Numero minimo di ore  di frequenza richieste per il segmento. Viene riempito se la frequenza è obbligatoria */
    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    /** ore di frequenza registrate */
    @SerialName("oreRegFreq")
    val registeredAttendanceHours: Float? = null,

    /** data di registrazione della frequenza, il formato con cui deve essere passata la data è DD/MM/YYYY */
    @SerialName("dataRegFreq")
    val attendanceRegistrationDate: String? = null,

    /** segmento obbligatorio, l''attività ha frequenza obbiligatoria se esiste alemno un segmento obbligatorio */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null
)

@Serializable
data class Esse3RecognitionActCareerRecognitionParameters(
    /** descrizione del progetto formativo collegato al riconoscimento */
    @SerialName("progettoFormativo")
    val trainingProject: String? = null,

    @SerialName("stage")
    val stage: Esse3RecognitionActCareerInternshipRecognitionParameters? = null
)

@Serializable
data class Esse3BulkAttendanceRejectionResult(
    /** stato dell'attività didattica F imposta la frequenza P la rimuove */
    @SerialName("staSceCod")
    val choiceStatusCode: String = "",

    /** anno di acquisizione della frequenza */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** percentuale di completamento della frequenza */
    @SerialName("percentualePresenza")
    val presencePercentage: Float? = null,

    /** percentuale ore di frequnza */
    @SerialName("percentualeOrePresenza")
    val presenceHoursPercentage: Float? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList(),

    /** id del tratto di carriera dove assengare la frequnenza */
    @SerialName("matId")
    val matId: Long? = null,

    /** codice matricola studente, utilizzato in alternativa al campo matId, viene recuperato il matId relativo al tratto attivo della carriera attiva dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** id della riga di libretto, utilizzata in alternativa al campo adCod oppure se l'attività risulta reiterabile */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** codice di ritorno del caricamento della rilevazione */
    @SerialName("retCode")
    val returnCode: Int? = null,

    /** messaggio di errore per il caricamento della rilevazione */
    @SerialName("errMsg")
    val errorMessage: String? = null
)

@Serializable
data class Esse3ExamSessionTranscriptFast(
    @SerialName("config")
    val config: Esse3ExamSessionConfig? = null,

    /** id della data di pianificazione utilizzata per la generazione dell'appello (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("datacalId")
    val calendarDateId: Long? = null,

    /** id del codice guida che ha generato l'appello. (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("capostipiteId")
    val rootId: Long? = null,

    /** id della commissione della pianicazione collegata all'appello. (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("commPianId")
    val committeePlanId: Long? = null,

    /** id dell'index relativo al gestore collegato all'appello. (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("indexId")
    val indexId: Long? = null,

    /** id del periodo collegato all'appello (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("periodoId")
    val periodId: Long? = null,

    /** numero di verbali generati non ancora caricati in carriera */
    @SerialName("numVerbaliGen")
    val generalMinutesNumber: Int? = null,

    /** numero di verbali caricati */
    @SerialName("numVerbaliCar")
    val careerMinutesNumber: Int? = null,

    /** numero di pubblicazion effettuate */
    @SerialName("numPubblicazioni")
    val publicationsNumber: Int? = null,

    /** numero di studenti iscritti all'appello */
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    /** stato di avanzamento del processo di definizione della logista dell'appello. I relaviti valori sono (G=generato, C=Consolidato, I=Inviato, R=Ritornato, A=Attivato) enum: . G - C - I - R - A */
    @SerialName("statoLog")
    val logState: String? = null,

    /** stato di apertura dell'appello. I relaviti Valori sono (C=da iniziare, A=in fase di svolgimento, F= concluso) */
    @SerialName("statoAperturaApp")
    val callOpeningState: String? = null,

    /** stato del processo di verbalizzazione degli esiti. I relaviti Valori sono (C=da iniziare, A=in fase di svolgimento, F= concluso) */
    @SerialName("statoVerb")
    val minutesState: Esse3MinutesState? = null,

    /** stato del processo di pubblicazione esiti. I relaviti Valori sono (C=da iniziare, A=in fase di svolgimento, F= concluso) */
    @SerialName("statoPubblEsiti")
    val outcomesPublicationState: Esse3OutcomesPublicationState? = null,

    /** stato del processo di inserimento esiti. I relaviti Valori sono (C=da iniziare, A=in fase di svolgimento, F= concluso) */
    @SerialName("statoInsEsiti")
    val outcomesInsertionState: Esse3OutcomesInsertionState? = null,

    /** descrizione dello stato dell'appello, indica con una descrizione lo stato dei vari sottoprocessi */
    @SerialName("statoDes")
    val stateDescription: String? = null,

    /** Stato dell'appello, i valori dipendono dallo stato dei sottoProcessi di inserimento esiti, pubblicazione e verbalizzazione. Per lo stato dei singoli sottoprocessi consultare i relativi stati */
    @SerialName("stato")
    val state: String? = null,

    /** nome del docente presidente della commissione dell'appello */
    @SerialName("presidenteNome")
    val presidentName: String? = null,

    /** cognome del docente presidente della commissione dell'appello */
    @SerialName("presidenteCognome")
    val presidentSurname: String? = null,

    /** id del docente presidente nella commissione dell'appello */
    @SerialName("presidenteId")
    val presidentId: Long? = null,

    /** descrizione modalità di prenotazione dell'appello */
    @SerialName("tipoGestPrenDes")
    val bookingManagementTypeDescription: String? = null,

    /** descrizione modalità di verbalizzazione dell'appello */
    @SerialName("tipoGestAppDes")
    val callManagementTypeDescription: String? = null,

    /** descrizione modalità di definizione dell'appello */
    @SerialName("tipoDefAppDes")
    val defaultCallTypeDescription: String? = null,

    /** descrizione dell'attività didattica di erogazione dell'appello */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** codice dell'attività didattica di erogazione dell'appello */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** codice del corso di studio di erogazione dell'appello */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice del corso di studio di erogazione dell'appello */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** modalità dell'esame definita nell'appello (valorizzata se il par_conf=CONTR_TIPO_ESA_APP=0), i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale Congiunto=SOC, Scritto e Orale Separato=SOS). */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: Esse3GraduationTypeCode? = null,

    /** modalità di iscrizione definita nell'appello, i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale=SO). */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: Esse3EnrollmentTypeCode? = null,

    /** tipo di appello (PF=Prova Finale, PP=Prova Parziale) */
    @SerialName("tipoAppCod")
    val callTypeCode: String? = null,

    /** id progressivo dell'appello rispetto alla coppia cds_id,ad_id */
    @SerialName("appId")
    val callId: Int? = null,

    /** chiave assoluta che identifca un singolo appello */
    @SerialName("appelloId")
    val examCallId: Long? = null,

    /** anno di calendario dell'appello, viene utilizzato per agganciare una eventuale definizione di esame comune */
    @SerialName("aaCalId")
    val academicYearCalendarId: Int? = null,

    /** note da inviare al sistema di logistica esterno */
    @SerialName("noteSistLog")
    val systemLogNotes: String? = null,

    /** note associate all'appello */
    @SerialName("note")
    val notes: String? = null,

    /** id del grupop di tag associati all'appello. Se presente, viene richiesta la selezione del tag in fase di prenotazione (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("tagTemplId")
    val tagTemplateId: Long? = null,

    /** id della sede associata all'appello. Se valorizzata viene controllato durante la prenotazione che la sede coincida con quella dello studente nell'anno di sessione (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** Id del gruppo voto nel caso gli ordinamenti collegati ai cds dell'appello abbiano gruppi voto differenti (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("gruppoVotoId")
    val gradeGroupId: Long? = null,

    /** id della condizione SQL associata all'appello */
    @SerialName("condId")
    val conditionId: Long? = null,

    /** tipo di scelta turno, i possibili valori sono i seguenti (0 - Calcolato dal sistema, viene associato il primo turno disponibile; 1 - Selezionabile dall'utente tra i turni compatibili liberi; 2 - Selezionato dall'utente prendendo un turno libero (anche non compatibile) */
    @SerialName("tipoSceltaTurno")
    val shiftChoiceType: Int? = null,

    /** appello riservato invisible agli studenti */
    @SerialName("riservatoFlg")
    val reservedFlag: Int? = null,

    /** ora del turno minimo associato all' appello (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("oraEsa")
    val graduationTime: String? = null,

    /** data inizio appello (DD/MM/YYYY) */
    @SerialName("dataInizioApp")
    val callStartDate: String? = null,

    /** data fine iscrizioni (DD/MM/YYYY) */
    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    /** data inizio iscrizioni (DD/MM/YYYY) */
    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    /** codice modalità di prenotazione dell'appello */
    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    /** codice modalità di verbalizzazione dell'appello */
    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    /** codice modalità di definizione dell'appello */
    @SerialName("tipoDefAppCod")
    val defaultCallTypeCode: String? = null,

    /** descrizione libera dell'appello */
    @SerialName("desApp")
    val callDescription: String? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Long? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Id della testata del libretto */
    @SerialName("matId")
    val matId: Long? = null,

    /** Id della riga del libretto */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** codice dell'attività didattica del libretto */
    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    /** descrizione dell'attività didattica del libretto */
    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    /** Stato dell\'attività didattica (codice) */
    @SerialName("staSceCod")
    val choiceStatusCode: String? = null
)

@Serializable
data class Esse3RecognitionParameters(
    /** 0 - Nessun ric/conv, 1 - R/F 2 - R/A, 3 - C/F, 4 - C/A */
    @SerialName("ricId")
    val searchId: Int = 0,

    /** Tipo di modalità con cui viene valutato l'esame. Può assumere i valori V,G,N se il valore è V allora al momento del superamento viene valorizzato il campo voto, altrimenti se il valore è G viene valorizzato il campo tipo_giud_cod */
    @SerialName("modValCod")
    val evaluationModeCode: String? = null,

    /** voto, valorizzato se modValCod è V. Gli esiti delle prove finali (cioè quelle che prevedono il caricamento nella riga di libretto) sono INTERI, gli esti di prove parziali invece possono avere 2 cifre decimali */
    @SerialName("voto")
    val grade: Float? = null,

    /** flag che indica la lode, impostato a 1 solo per modValCod è V e la lode deve essere impostata */
    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    /** codice che indica il tipo di giudizio utilizzato, valorizzato solo se modValCod è G */
    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    /** data della prova, il formato con cui deve essere definita la data è DD/MM/YYYY */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** anno di superamento della prova */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int = 0,

    /** anno di superamento della prova */
    @SerialName("aaSupId")
    val academicYearSupervisorId: Int? = null,

    /** anno di competenza, , se non valorizzata allora viene calcolato dal sistema */
    @SerialName("aaCompId")
    val academicYearComponentId: Int? = null,

    /** data di competenza, se non valorizzata allora viene calcolata dal sistema */
    @SerialName("dataComp")
    val completionDate: String? = null,

    /** tipo di svolgimento esame da assegnare */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("adIntegrativa")
    val integrativeActivity: Esse3RecognitionConversionInternalActivityParameters? = null,

    /** tipo di riconoscimento, obbligatrorio per i riconoscimenti di attività (R/A) e per quelli di frequenza (R/F) */
    @SerialName("tipoRicCod")
    val requestTypeCode: String = "",

    /** codice della lingua collegata al livello di uscita, valido solo per i riconoscimenti */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** codice del livello di uscita della lingua */
    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    @SerialName("stage")
    val stage: Esse3RecognitionActCareerRecognitionParameters? = null
)

@Serializable
data class Esse3VoteGroup(
    /** codice del gruppo di voto */
    @SerialName("cod")
    val code: String? = null,

    /** descrizione del gruppo di voto */
    @SerialName("des")
    val description: String? = null,

    /** minimo punteggio positivo della scala voti */
    @SerialName("minPunti")
    val minPoints: Int? = null,

    /** massimo punteggio della scala voti */
    @SerialName("maxPunti")
    val maxPoints: Int? = null,

    /** flag che indica se è prevista la lode */
    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null
)

@Serializable
data class Esse3RecognitionConversionInternalActivityParameters(
    /** tipo di riconoscimento per l'attività integrativa (valorizzato solo se differente rispetto alla parte riconosciuta) */
    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    /** codice dell'attività integrativa */
    @SerialName("cod")
    val code: String = "",

    /** descrizione dell'attività integrativa */
    @SerialName("des")
    val description: String = "",

    @SerialName("mappingSegmenti")
    val segmentsMapping: List<Esse3RecognitionConversionInternalSegmentParameters> = emptyList()
)

@Serializable
data class Esse3TranscriptSegmentAttributes(
    /** codice dell''unità didattica a cui appartiene il semgento */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione dell''unità didattica a cui appartiene il segmento */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** tipo di credito previsto per il segmento */
    @SerialName("tipoCreCod")
    val creditTypeCode: String = "",

    /** Tipo Attività formativa */
    @SerialName("taf")
    val taf: String? = null,

    /** id ambito */
    @SerialName("ambId")
    val environmentId: Long? = null,

    /** codice settore scientifico disciplinare */
    @SerialName("settCod")
    val sectorCode: String = "",

    /** tipo di disciplina prevista, valida se il CDSORD non è definto a Settori */
    @SerialName("discCod")
    val disciplineCode: String? = null,

    /** peso del segmento secondo l''unità di misura prevista dalla riga di libretto */
    @SerialName("peso")
    val weight: Float = 0f,

    /** durata in ore del segmento */
    @SerialName("durata")
    val duration: Float? = null,

    /** Numero minimo di ore  di frequenza richieste per il segmento. Viene riempito se la frequenza è obbligatoria */
    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    /** ore di frequenza registrate */
    @SerialName("oreRegFreq")
    val registeredAttendanceHours: Float? = null,

    /** data di registrazione della frequenza, il formato con cui deve essere passata la data è DD/MM/YYYY */
    @SerialName("dataRegFreq")
    val attendanceRegistrationDate: String? = null,

    /** segmento obbligatorio, l''attività ha frequenza obbiligatoria se esiste alemno un segmento obbligatorio */
    @SerialName("freqObbligFlg")
    val mandatoryAttendanceFlag: Int? = null,

    /** descrizione attività formativa, non utilizzato in fase di inserimento */
    @SerialName("tafDes")
    val tafDescription: String? = null,

    /** descrizione ambito, non utilizzato in fase di inserimento */
    @SerialName("ambitoDes")
    val scopeDescription: String? = null,

    /** descrizione settore scientifico disciplinare, non utilizzato in fase di inserimento */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** descrizione della disciplina, non utilizzato in fase di inserimento */
    @SerialName("discDes")
    val disciplineDescription: String? = null
)

@Serializable
data class Esse3TranscriptPartition(
    /** id della riga della partizione libretto */
    @SerialName("adpartId")
    val activityPartitionId: Long = 0L,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    /** id univoco che consente di individuare il libretto dello studente */
    @SerialName("matId")
    val matId: Long = 0L,

    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey? = null,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey? = null,

    /** codice della partizione dell''anno accademico effettivamente erogata. */
    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    /** descrizione della partizione dell''anno accademico effettivamente erogata. */
    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    /** id del docente che ha la titolarità sulla partizione assegnata allo studente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** cognome del docente titolare della partizione */
    @SerialName("cognomeDocTit")
    val lecturerSurnameTitle: String? = null,

    /** nome del docente titolare della partizione */
    @SerialName("nomeDoctit")
    val titleLecturerName: String? = null,

    /** codice del ruolo del docente titolare della partizione */
    @SerialName("ruoloDocTit")
    val lecturerRoleTitle: String? = null,

    /** appellativo del docente titolare della partizione */
    @SerialName("appellativoDocTit")
    val lecturerTitle: String? = null,

    /** id della lingua di erogazione della didattica */
    @SerialName("linguaDidId")
    val teachingLanguageId: Long? = null,

    /** codice ISO6392 della lingua di erogazione della didattica */
    @SerialName("linguaDidCod")
    val teachingLanguageCode: String? = null,

    /** descrizione della lingua di erogazione della didattica */
    @SerialName("linguaDidDes")
    val teachingLanguageDescription: String? = null
)

@Serializable
data class Esse3SyllabusTeachingUnitTranscript(
    /** id univoco che consente di individuare il tratto di carriera dello studente e il relativo libretto collegato */
    @SerialName("matId")
    val matId: Long = 0L,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("chiaveAdContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    /** id che consente di individuale la partizione collegata alla UD */
    @SerialName("udLogId")
    val teachingUnitLogId: Long = 0L,

    /** codice Unità didattica */
    @SerialName("udCod")
    val teachingUnitCode: String? = null,

    /** descrizione Unità didattica */
    @SerialName("udDes")
    val teachingUnitDescription: String? = null,

    /** Flag che indica se le descrizioni della unità didattica à pubblicabile */
    @SerialName("desUdPubblFlg")
    val teachingUnitPublicationFlag: Int = 0,

    /** obiettivi formativi */
    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    /** prerequisiti */
    @SerialName("prerequisiti")
    val prerequisites: String? = null,

    /** aa */
    @SerialName("testiRiferimento")
    val referenceTexts: String? = null
)

@Serializable
data class Esse3PrerequisitesCheck(
    /** il valore 1 indica controllo superato, altrimenti il controllo non è superato */
    @SerialName("esito")
    val outcome: Int? = null
)

@Serializable
data class Esse3StudentPresenceReleaseParameters(
    /** codice matricola dello studente, recupera il tratto attivo della carriera attiva dello studente, nel caso di ambiguità è possibile utilizzare il campo matId */
    @SerialName("matricola")
    val matricola: String? = null,

    /** id del tratto di carriera dove assengare la frequnenza, se valorizzato viene controllata la coerenza con il campo matricola */
    @SerialName("matId")
    val matId: Long? = null,

    /** id della riga di libretto, utilizzata len caso l'attività indicata risulti reiterabile */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** anno di frequenza da assegnare allo studente, se null viene calcolato con l'anno corrente */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di assegnazione della frequenza dello studente, valida solo se aaFreqId è valorizzato */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList()
)

@Serializable
data class Esse3ExamSessionTranscript(
    @SerialName("config")
    val config: Esse3ExamSessionConfig? = null,

    /** id della data di pianificazione utilizzata per la generazione dell'appello (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("datacalId")
    val calendarDateId: Long? = null,

    /** id del codice guida che ha generato l'appello. (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("capostipiteId")
    val rootId: Long? = null,

    /** id della commissione della pianicazione collegata all'appello. (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("commPianId")
    val committeePlanId: Long? = null,

    /** id dell'index relativo al gestore collegato all'appello. (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("indexId")
    val indexId: Long? = null,

    /** id del periodo collegato all'appello (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("periodoId")
    val periodId: Long? = null,

    /** numero di verbali generati non ancora caricati in carriera */
    @SerialName("numVerbaliGen")
    val generalMinutesNumber: Int? = null,

    /** numero di verbali caricati */
    @SerialName("numVerbaliCar")
    val careerMinutesNumber: Int? = null,

    /** numero di pubblicazion effettuate */
    @SerialName("numPubblicazioni")
    val publicationsNumber: Int? = null,

    /** numero di studenti iscritti all'appello */
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    /** stato di avanzamento del processo di definizione della logista dell'appello. I relaviti valori sono (G=generato, C=Consolidato, I=Inviato, R=Ritornato, A=Attivato) enum: . G - C - I - R - A */
    @SerialName("statoLog")
    val logState: String? = null,

    /** stato di apertura dell'appello. I relaviti Valori sono (C=da iniziare, A=in fase di svolgimento, F= concluso) */
    @SerialName("statoAperturaApp")
    val callOpeningState: String? = null,

    /** stato del processo di verbalizzazione degli esiti. I relaviti Valori sono (C=da iniziare, A=in fase di svolgimento, F= concluso) */
    @SerialName("statoVerb")
    val minutesState: Esse3MinutesState? = null,

    /** stato del processo di pubblicazione esiti. I relaviti Valori sono (C=da iniziare, A=in fase di svolgimento, F= concluso) */
    @SerialName("statoPubblEsiti")
    val outcomesPublicationState: Esse3OutcomesPublicationState? = null,

    /** stato del processo di inserimento esiti. I relaviti Valori sono (C=da iniziare, A=in fase di svolgimento, F= concluso) */
    @SerialName("statoInsEsiti")
    val outcomesInsertionState: Esse3OutcomesInsertionState? = null,

    /** descrizione dello stato dell'appello, indica con una descrizione lo stato dei vari sottoprocessi */
    @SerialName("statoDes")
    val stateDescription: String? = null,

    /** Stato dell'appello, i valori dipendono dallo stato dei sottoProcessi di inserimento esiti, pubblicazione e verbalizzazione. Per lo stato dei singoli sottoprocessi consultare i relativi stati */
    @SerialName("stato")
    val state: String? = null,

    /** nome del docente presidente della commissione dell'appello */
    @SerialName("presidenteNome")
    val presidentName: String? = null,

    /** cognome del docente presidente della commissione dell'appello */
    @SerialName("presidenteCognome")
    val presidentSurname: String? = null,

    /** id del docente presidente nella commissione dell'appello */
    @SerialName("presidenteId")
    val presidentId: Long? = null,

    /** descrizione modalità di prenotazione dell'appello */
    @SerialName("tipoGestPrenDes")
    val bookingManagementTypeDescription: String? = null,

    /** descrizione modalità di verbalizzazione dell'appello */
    @SerialName("tipoGestAppDes")
    val callManagementTypeDescription: String? = null,

    /** descrizione modalità di definizione dell'appello */
    @SerialName("tipoDefAppDes")
    val defaultCallTypeDescription: String? = null,

    /** descrizione dell'attività didattica di erogazione dell'appello */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** codice dell'attività didattica di erogazione dell'appello */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** codice del corso di studio di erogazione dell'appello */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice del corso di studio di erogazione dell'appello */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** modalità dell'esame definita nell'appello (valorizzata se il par_conf=CONTR_TIPO_ESA_APP=0), i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale Congiunto=SOC, Scritto e Orale Separato=SOS). */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: Esse3GraduationTypeCode? = null,

    /** modalità di iscrizione definita nell'appello, i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale=SO). */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: Esse3EnrollmentTypeCode? = null,

    /** tipo di appello (PF=Prova Finale, PP=Prova Parziale) */
    @SerialName("tipoAppCod")
    val callTypeCode: String? = null,

    /** id progressivo dell'appello rispetto alla coppia cds_id,ad_id */
    @SerialName("appId")
    val callId: Int? = null,

    /** chiave assoluta che identifca un singolo appello */
    @SerialName("appelloId")
    val examCallId: Long? = null,

    /** anno di calendario dell'appello, viene utilizzato per agganciare una eventuale definizione di esame comune */
    @SerialName("aaCalId")
    val academicYearCalendarId: Int? = null,

    /** note da inviare al sistema di logistica esterno */
    @SerialName("noteSistLog")
    val systemLogNotes: String? = null,

    /** note associate all'appello */
    @SerialName("note")
    val notes: String? = null,

    /** id del grupop di tag associati all'appello. Se presente, viene richiesta la selezione del tag in fase di prenotazione (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("tagTemplId")
    val tagTemplateId: Long? = null,

    /** id della sede associata all'appello. Se valorizzata viene controllato durante la prenotazione che la sede coincida con quella dello studente nell'anno di sessione (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** Id del gruppo voto nel caso gli ordinamenti collegati ai cds dell'appello abbiano gruppi voto differenti (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("gruppoVotoId")
    val gradeGroupId: Long? = null,

    /** id della condizione SQL associata all'appello */
    @SerialName("condId")
    val conditionId: Long? = null,

    /** tipo di scelta turno, i possibili valori sono i seguenti (0 - Calcolato dal sistema, viene associato il primo turno disponibile; 1 - Selezionabile dall'utente tra i turni compatibili liberi; 2 - Selezionato dall'utente prendendo un turno libero (anche non compatibile) */
    @SerialName("tipoSceltaTurno")
    val shiftChoiceType: Int? = null,

    /** appello riservato invisible agli studenti */
    @SerialName("riservatoFlg")
    val reservedFlag: Int? = null,

    /** ora del turno minimo associato all' appello (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("oraEsa")
    val graduationTime: String? = null,

    /** data inizio appello (DD/MM/YYYY) */
    @SerialName("dataInizioApp")
    val callStartDate: String? = null,

    /** data fine iscrizioni (DD/MM/YYYY) */
    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    /** data inizio iscrizioni (DD/MM/YYYY) */
    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    /** codice modalità di prenotazione dell'appello */
    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    /** codice modalità di verbalizzazione dell'appello */
    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    /** codice modalità di definizione dell'appello */
    @SerialName("tipoDefAppCod")
    val defaultCallTypeCode: String? = null,

    /** descrizione libera dell'appello */
    @SerialName("desApp")
    val callDescription: String? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Long? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Id della testata del libretto */
    @SerialName("matId")
    val matId: Long? = null,

    /** Id della riga del libretto */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** codice dell'attività didattica del libretto */
    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    /** descrizione dell'attività didattica del libretto */
    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    /** Stato dell\'attività didattica (codice) */
    @SerialName("staSceCod")
    val choiceStatusCode: String? = null
)

@Serializable
data class Esse3StudentSurveysParameters(
    /** id univoco della rilevazione */
    @SerialName("idRilevazione")
    val detectionId: String = "",

    /** data della lezione */
    @SerialName("dataLezione")
    val lessonDate: String? = null,

    /** durata in ore dalla lezione */
    @SerialName("durata")
    val duration: Float? = null,

    /** codice fiscale del docente se differente rispetto all'utente loggato */
    @SerialName("codFisDocente")
    val lecturerFiscalCode: String? = null,

    /** stato di presenza dello studente (P presente, A assente) */
    @SerialName("statoPresenza")
    val presenceState: String? = null,

    /** minuti di assenza ad una lezione, il dato è valido solo se lo stato della rilevazione è P - presenza */
    @SerialName("minutiAssenza")
    val absenceMinutes: Int? = null
)

@Serializable
data class Esse3ActivitiesToInsert(
    /** anno di corso dell'attività didattica */
    @SerialName("annoCorso")
    val courseYear: Int = 0,

    /** flag che indica se l'attività deve essere considerata come un OFA , se non passato il default è 0 */
    @SerialName("ofaFlg")
    val ofaFlag: Int? = null,

    /** flag che indica se l'attività deve essere considerata un debito, se non passato il default è 0 */
    @SerialName("debitoFlg")
    val debtFlag: Int? = null,

    /** tipo di attività da inserire: - offerta. valorizzare la proprietà 'dettagliOfferta' - fuoriOfferta. valorizzare la prope 'dettagliFuoriOfferta' */
    @SerialName("tipoAttivita")
    val activityType: String = "",

    @SerialName("dettagliOfferta")
    val offerDetails: Esse3ActivityToInsertInOffer? = null,

    @SerialName("dettagliFuoriOfferta")
    val offOfferDetails: Esse3ActivityToInsertOutsideOffer? = null
)

@Serializable
data class Esse3BulkAttendanceStudentsDetailParameters(
    /** stato dell'attività didattica F imposta la frequenza P la rimuove */
    @SerialName("staSceCod")
    val choiceStatusCode: String = "",

    /** anno di acquisizione della frequenza */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza */
    @SerialName("dataFreq")
    val attendanceDate: String? = null,

    /** percentuale di completamento della frequenza */
    @SerialName("percentualePresenza")
    val presencePercentage: Float? = null,

    /** percentuale ore di frequnza */
    @SerialName("percentualeOrePresenza")
    val presenceHoursPercentage: Float? = null,

    @SerialName("rilevazioni")
    val detections: List<Esse3StudentSurveysParameters> = emptyList(),

    /** codice matricola dello studente, recupera il tratto attivo della carriera attiva dello studente, nel caso di ambiguità è possibile utilizzare il campo matId */
    @SerialName("matricola")
    val matricola: String? = null,

    /** id del tratto di carriera dove assengare la frequnenza, se valorizzato viene controllata la coerenza con il campo matricola */
    @SerialName("matId")
    val matId: Long? = null,

    /** id della riga di libretto, utilizzata len caso l'attività indicata risulti reiterabile */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null
)

@Serializable
data class Esse3ActivityToInsertInOffer(
    /** id dell'attività didiattica da inserire (valorizzato in alternativa a adCod) uno dei due valori adId o adCod è obbligatorio */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice dell'attività didattica da inserire (valorizzato in alternativa a adId) uno dei due valori adId o adCod è obbligatorio */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** id del corso di studio di erogazione dell'attività didattica da inserire (valorizzato in alternativa a cdsCod), uno dei due valori cdsId o cdsCod è obbligatorio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio di erogazione dell'attività didattica da inserire (valorizzato in alternativa a cdsId), uno dei due valori cdsId o cdsCod è obbligatorio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** anno dell'ordinamento di corso di studio di erogazione dell'attività didattica da inserire, se non valorizzato viene calcolato dal sistema */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** id del percorso di studio di erogazione dell'attività didattica da inserire (valorizzato in alternativa a pdsCod), uno dei due valori pdsId o pdsCod è obbligatorio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** codice del percorso di studio di erogazione dell'attività didattica da inserire (valorizzato in alternativa a pdsId), uno dei due valori pdsId o pdsCod è obbligatorio */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** anno di offerta di erogazione dell'attività didattica da inserire, se non valorizzato viene calcolato dal sistema */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0
)

@Serializable
data class Esse3RecognitionActCareerInternshipRecognitionParameters(
    /** descrizione dell'azienda dove viene effettuato lo stage */
    @SerialName("azienda")
    val company: String? = null,

    /** descrizione della sede dove viene svolto lo stage */
    @SerialName("sede")
    val site: String? = null,

    /** descrizione del periodo nel quale viene sovolto lo stage */
    @SerialName("periodo")
    val period: String? = null,

    /** lorem ipsum ... */
    @SerialName("attivitaSvolte")
    val activitiesCarriedOut: String? = null
)

@Serializable
data class Esse3BulkAttendanceResult(
    /** codice di ritorno della funzione di caricamento delle frequenze massiva */
    @SerialName("retCode")
    val returnCode: Int? = null,

    /** messaggio di errore nel caso ci siano stati degli errori */
    @SerialName("errMsg")
    val errorMessage: String? = null,

    @SerialName("scarti")
    val discards: List<Esse3BulkAttendanceRejectionResult> = emptyList()
)

@Serializable
data class Esse3BulkPresenceReleaseParameters(
    @SerialName("studenti")
    val students: List<Esse3StudentPresenceReleaseParameters> = emptyList(),

    /** flag per l'assegnazione della data di frequenza */
    @SerialName("assegnaDataFreq")
    val assignAttendanceDate: Int? = null,

    /** percentuale mimima delle rilevazioni per l'assegnazione della frequenza */
    @SerialName("percMinORil")
    val minimumReleasePercentage: Int? = null,

    /** percentuale mimima delle ore per l'assegnazione della frequenza */
    @SerialName("percMinOre")
    val minimumHoursPercentage: Int? = null,

    /** id della condivisione logistica, se non passata viene considerata qualisiasi condivisione logistica */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** semestre della partizione logistica, se non passato viene considerata qualsiasi condivisione logistica */
    @SerialName("partCod")
    val partialCode: String = "",

    /** dominio di partizione della partizione logistica, se non passato viene considerata qualsiasi condivisione logistica */
    @SerialName("domPartCod")
    val domicilePartialCode: String = "",

    /** fattore di partizione della partizione logistica, se non passato viene considerata qualsiasi condivisione logistica */
    @SerialName("fatPartCod")
    val invoicePartialCode: String = "",

    /** anno di offerta dell'attività di cui assegnare la frequenza, se non passato viene considerato valido quasiasi anno di offerta */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    /** codice percorso di studio dove assegnare la frequenza, se non passato viene considerato valido qualsiasi pds */
    @SerialName("pdsCod")
    val studyPlanCode: String = "",

    /** anno di ordinamento del corso di studio dove assegnare la frequenza, se non passato viene considerato valido qualsiasi anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** codice corso di studio dove assegnare la frequenza, se non passato viene considerato valido qualsiasi corso di stuio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String = "",

    /** codice attività didattica dove assegnare la frequenza. */
    @SerialName("adCod")
    val activityCode: String = "",

    /** anno di rilevazione della frequenza, se non valorizzato viene preso il DR_CARR */
    @SerialName("aaRilevazioneId")
    val academicYearSurveyId: Int? = null,

    /** codice fiscale del docente per il quale controllare la coerenza tra le attività e la titolarità assegnata (solo ccodice AD). Per evitare il controllo passare stringa vuota */
    @SerialName("codFisDocenteControllo")
    val lecturerFiscalCodeCheck: String = "",

    /** codice fiscale del docente che ha effettuato la rilevazione */
    @SerialName("codFisDocenteRilevazione")
    val lecturerFiscalCodeDetection: String = "",

    /** tipo di operazione da effettuare */
    @SerialName("operazione")
    val operation: String = ""
)

@Serializable
data class Esse3TranscriptRowPerActivityLog(
    /** id univoco della persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** codice fiscale della persona */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice utente */
    @SerialName("userId")
    val userId: String? = null,

    /** cognome dello studente */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome dello studente */
    @SerialName("nome")
    val name: String? = null,

    /** email personale dello studente */
    @SerialName("email")
    val email: String? = null,

    /** email di ateneo dello studente */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** Identificativo univoco dello Studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id univoco che consente di individuare il tratto di carriera dello studente e il relativo libretto collegato */
    @SerialName("matId")
    val matId: Long? = null,

    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** id univoco della riga di libretto collegata con la condivisione logisitca */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** id univoco della partizione del libretto collegata con la condivisione logisitca */
    @SerialName("adPartId")
    val activityPartitionId: Long? = null,

    /** id univoco della condivisione logisitca */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** codice del semestre collegato alla condivisione logisitca */
    @SerialName("logPartCod")
    val partialLogCode: String? = null,

    /** tipo di partzionamento previsto per la condivisone logistica */
    @SerialName("logFatPartCod")
    val invoicePartialLogCode: String? = null,

    /** dominio di partizione della singola partizione */
    @SerialName("logDomPartCod")
    val domicilePartialLogCode: String? = null,

    /** anno di offerta della condivisione logistica */
    @SerialName("logAaOffId")
    val academicYearOfferLogId: Int? = null,

    /** anno di corso dell'attività di libretto */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** Stato dell\'attività didattica (codice) */
    @SerialName("staSceCod")
    val choiceStatusCode: String? = null,

    /** Presenza di un riconoscimento o convalida. 0 = Nessun Riconoscimento 1 = RF (Riconoscimento di frequenza)  2 = RA (Riconoscimento di attività) 3 = CF (Convalida di frequenza) 4 = CA (Convalida di attività) */
    @SerialName("ricId")
    val searchId: Int? = null,

    /** peso dell'attività didattica, calcolato come somma dei pesi dei segmenti, il peso prevede due decimali opzionali */
    @SerialName("peso")
    val weight: Float? = null,

    /** durata in ore dell'attività didattica */
    @SerialName("durata")
    val duration: Float? = null,

    /** ore minime di frequenza previste per acquisire la frequenza sull'attività didattica */
    @SerialName("oreMinFreq")
    val minimumAttendanceHours: Float? = null,

    /** flag che indica se l'attività ha frequenza obbligatoria */
    @SerialName("freqFlg")
    val attendanceFlag: Int? = null,

    /** anno di frequenza, valorizzato nel caso lo stato dell''attività sia F oppure S */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** data di acquisizione della frequenza, se valorizzata indica la data di riferimento dalla quale la frequenza risulta acquisita, il formato con cui deve essere definita la data à DD/MM/YYYY */
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
    /** stato della missione */
    @SerialName("statoMissione")
    val missionState: String? = null
)

@Serializable
data class Esse3TranscriptSegmentStudentClass(
    /** id univoco del segmento che identifica le caratteristiche dell'attività didattica */
    @SerialName("segsceId")
    val segmentChoiceId: Long = 0L,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    /** id univoco che consente di individuare il libretto dello studente */
    @SerialName("matId")
    val matId: Long = 0L,

    @SerialName("attributi")
    val attributes: Esse3TranscriptSegmentAttributes,

    /** ID dell´anno accademico di offerta. */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** Codice dell´attività didattica. */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** Codice mnemonico del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** ID AA in cui viene attivato l'ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** Codice Percorso di Studio. Se TIPO_SPEC_COD del CDS è SSIS, il valore del campo COD deve coincidere con il valore del campo IND_ID */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** id univoco della condivisione logisitca */
    @SerialName("adLogId")
    val activityLogId: Long? = null,

    /** Identificativo univoco dello Studente */
    @SerialName("stuId")
    val studentId: Long? = null
)

@Serializable
data class Esse3AttendanceReleaseDetail(
    /** identificativo del gruppo di rilevazioni */
    @SerialName("adsceRilId")
    val choiceReleaseId: Long? = null,

    /** identificativo della rilevazione */
    @SerialName("adsceRilDettId")
    val choiceReleaseDetailId: Long? = null,

    /** id del tratto di carriera su cui calcolare le statistiche */
    @SerialName("matId")
    val matId: Long? = null,

    /** identificativo univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** codice fiscale del docente che ha effettuato la rilevazione */
    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    /** identificativo univoco del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** nome del docente */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** data e ora di inizio della rilevazione */
    @SerialName("dataOraInizio")
    val startDateTime: String? = null,

    /** durata in ore della rilevazione */
    @SerialName("durata")
    val duration: Float? = null,

    /** stato della rilevazione (A, P) */
    @SerialName("stato")
    val state: String? = null,

    /** identificativo univoco per le date delle lezioni/rilevazioni */
    @SerialName("adLogDateId")
    val activityLogDateId: Long? = null,

    /** minuti di assenza ad una lezione, il dato è valido solo se lo stato della rilevazione è P - presenza */
    @SerialName("minutiAssenza")
    val absenceMinutes: Int? = null
)

@Serializable
data class Esse3SyllabusActivityTranscript(
    /** id univoco che consente di individuare il tratto di carriera dello studente e il relativo libretto collegato */
    @SerialName("matId")
    val matId: Long = 0L,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    @SerialName("chiavePartizione")
    val partitionKey: Esse3PartitionKey,

    @SerialName("chiaveADContestualizzata")
    val contextualizedTeachingActivityKey: Esse3ContextualizedActivityKey,

    /** Flag che indica se le descrizioni delle attività didattiche sono pubblicabili */
    @SerialName("desAdPubblFlg")
    val teachingActivityPublicationFlag: Int = 0,

    /** contenuti del corso */
    @SerialName("contenuti")
    val contents: String? = null,

    /** obiettivi formativi */
    @SerialName("obiettiviFormativi")
    val trainingObjectives: String? = null,

    /** prerequisiti */
    @SerialName("prerequisiti")
    val prerequisites: String? = null,

    /** metodi didattici */
    @SerialName("metodiDidattici")
    val teachingMethods: String? = null,

    /** aa */
    @SerialName("modalitaVerificaApprendimento")
    val learningVerificationMethod: String? = null,

    /** altre informazioni */
    @SerialName("altreInfo")
    val otherInfo: String? = null,

    /** aa */
    @SerialName("testiRiferimento")
    val referenceTexts: String? = null
)

@Serializable
data class Esse3RecognitionConversionInternalSegmentParameters(
    /** id del segmento per il quale convalidare i CFU */
    @SerialName("segsceId")
    val segmentChoiceId: Long = 0L,

    /** peso riconosciuto/convalidato del segmento */
    @SerialName("peso")
    val weight: Float? = null,

    /** codice del SSD che va sovrascritto nella AD integrativa */
    @SerialName("settCod")
    val sectorCode: String? = null
)

@Serializable
data class Esse3ActivityToInsertOutsideOffer(
    /** codice dell''attività da inserire */
    @SerialName("cod")
    val code: String = "",

    /** descrizione dell''attività da inserire */
    @SerialName("des")
    val description: String = "",

    /** peso dell'attività nel caso non siano presenti dei segmenti, viceversa deve essere coerente con la somma dei segmenti stessi */
    @SerialName("peso")
    val weight: Int? = null,

    /** anno di offerta di riferimento dell'attività */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    @SerialName("segmenti")
    val segments: List<Esse3TranscriptSegmentInsertionAttributes> = emptyList()
)

@Serializable
data class Esse3RecognitionConversionActivityDestinationParameters(
    /** 0 - Nessun ric/conv, 1 - R/F 2 - R/A, 3 - C/F, 4 - C/A */
    @SerialName("ricId")
    val searchId: Int = 0,

    /** Tipo di modalità con cui viene valutato l'esame. Può assumere i valori V,G,N se il valore è V allora al momento del superamento viene valorizzato il campo voto, altrimenti se il valore è G viene valorizzato il campo tipo_giud_cod */
    @SerialName("modValCod")
    val evaluationModeCode: String? = null,

    /** voto, valorizzato se modValCod è V. Gli esiti delle prove finali (cioè quelle che prevedono il caricamento nella riga di libretto) sono INTERI, gli esti di prove parziali invece possono avere 2 cifre decimali */
    @SerialName("voto")
    val grade: Float? = null,

    /** flag che indica la lode, impostato a 1 solo per modValCod è V e la lode deve essere impostata */
    @SerialName("lodeFlg")
    val cumLaudeFlag: Int? = null,

    /** codice che indica il tipo di giudizio utilizzato, valorizzato solo se modValCod è G */
    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    /** data della prova, il formato con cui deve essere definita la data è DD/MM/YYYY */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** anno di superamento della prova */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int = 0,

    /** anno di superamento della prova */
    @SerialName("aaSupId")
    val academicYearSupervisorId: Int? = null,

    /** anno di competenza, , se non valorizzata allora viene calcolato dal sistema */
    @SerialName("aaCompId")
    val academicYearComponentId: Int? = null,

    /** data di competenza, se non valorizzata allora viene calcolata dal sistema */
    @SerialName("dataComp")
    val completionDate: String? = null,

    /** tipo di svolgimento esame da assegnare */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    @SerialName("adIntegrativa")
    val integrativeActivity: Esse3RecognitionConversionInternalActivityParameters? = null
)

@Serializable
data class Esse3TranscriptTest(
    /** id univoco della prova relativa ad una attività didattica del libretto */
    @SerialName("adregId")
    val activityRegulationId: Long = 0L,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    /** id univoco che consente di individuare il libretto dello studente */
    @SerialName("matId")
    val matId: Long = 0L,

    /** codice dello stato della prova */
    @SerialName("staRegCod")
    val regulationStatusCode: Esse3RegulationStatusCode? = null,

    /** descrizione dello stato della prova */
    @SerialName("staRegDes")
    val regulationStatusDescription: String? = null,

    /** id univoco della prenotazione collegata alla prova di libretto */
    @SerialName("applistaId")
    val applicationListId: Long? = null,

    /** tipo di appello collegato alla prenotazione */
    @SerialName("tipoAppCod")
    val callTypeCode: Esse3SessionEvaluationType? = null,

    /** id del corso di studio che eroga l''esame (padre dell''esame comune), il corso coincide con il corso in cui è definita la sessione collegata al superamento dell''esame stesso */
    @SerialName("cdsEsaId")
    val courseOfStudyGraduationId: Long? = null,

    /** id dell'' attività didattica che eroga l''esame  (padre dell''esame comune) */
    @SerialName("adEsaId")
    val activityExamId: Long? = null,

    /** id della sessione che permette il superamento dell'esame (la chiave della sessione à composta da cdsAdId, sesId, esito.aaSupId) */
    @SerialName("sesId")
    val sessionId: Long? = null,

    /** descrizione della sessione che permette il superamento dell'esame */
    @SerialName("sesDes")
    val sessionDescription: String? = null,

    /** tipo di iscrizione prevista per la prova */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** data di dell''appello della prenotazione collegata alla prova, il formato con cui deve essere definita la data è DD/MM/YYYY */
    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("esitoFinale")
    val finalOutcome: Esse3Result? = null,

    @SerialName("esitoScr")
    val writingOutcome: Esse3Result? = null,

    @SerialName("esitoParziale")
    val partialOutcome: Esse3Result? = null,

    /** codice della causale di non superamento */
    @SerialName("tipoNoSupCod")
    val noSupplementTypeCode: String? = null,

    /** descrizione della causale di non superamento */
    @SerialName("tipiNosupDes")
    val noSupTypesDescription: String? = null,

    /** codice della causale di non caricamento */
    @SerialName("tipoNoCarCod")
    val noChargeTypeCode: String? = null,

    /** descrizione della causale di non caricamento */
    @SerialName("tipoNoCarDes")
    val noChargeTypeDescription: String? = null,

    /** id del lotto del verbale collegato alla prova */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** id del verbale contentuto all''interno del lotto che ha caricato la prova */
    @SerialName("verbId")
    val minutesId: Long? = null,

    /** numero di errore di caricamento del verbale */
    @SerialName("errNum")
    val errorNumber: Long? = null,

    /** descrizione dell''errore di caricamento del verbale */
    @SerialName("errDes")
    val errorDescription: String? = null,

    /** descrizione dell''errore di caricamento del verbale (adatto alla visualizzazione su WEB) */
    @SerialName("errDesWeb")
    val webErrorDescription: String? = null
)

@Serializable
data class Esse3TranscriptSegment(
    /** id univoco del segmento che identifica le caratteristiche dell'attività didattica */
    @SerialName("segsceId")
    val segmentChoiceId: Long = 0L,

    /** id univoco che consente di individuare una riga di libretto dello studente */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    /** id univoco che consente di individuare il libretto dello studente */
    @SerialName("matId")
    val matId: Long = 0L,

    @SerialName("attributi")
    val attributes: Esse3TranscriptSegmentAttributes
)

@Serializable
data class Esse3BulkAttendanceParameters(
    @SerialName("studenti")
    val students: List<Esse3BulkAttendanceStudentsDetailParameters> = emptyList(),

    /** ore totali di rilevazioni */
    @SerialName("totaleOreRilevazioni")
    val totalDetectionHours: Float? = null,

    /** totale rilevazioni */
    @SerialName("totaleRilevazioni")
    val totalDetections: Int? = null,

    /** codice fiscale del docente che ha effettuato la rilevazione */
    @SerialName("codFisDocenteRilevazione")
    val lecturerFiscalCodeDetection: String? = null,

    /** codice fiscale del docente per il quale controllare la coerenza tra le attività e la titolarità assegnata (solo ccodice AD). Per evitare il controllo passare stringa vuota */
    @SerialName("codFisDocenteControllo")
    val lecturerFiscalCodeCheck: String? = null,

    /** anno di rilevazione della frequenza, se non valorizzato viene preso il DR_CARR */
    @SerialName("aaRilevazioneId")
    val academicYearSurveyId: Int? = null,

    /** codice attività didattica dove assegnare la frequenza. */
    @SerialName("adCod")
    val activityCode: String = "",

    /** codice corso di studio dove assegnare la frequenza, se non passato viene considerato valido qualsiasi corso di stuio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** anno di ordinamento del corso di studio dove assegnare la frequenza, se non passato viene considerato valido qualsiasi anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** codice percorso di studio dove assegnare la frequenza, se non passato viene considerato valido qualsiasi pds */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** anno di offerta dell'attività di cui assegnare la frequenza, se non passato viene considerato valido quasiasi anno di offerta */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** fattore di partizione della partizione logistica, se non passato viene considerata qualsiasi condivisione logistica */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** dominio di partizione della partizione logistica, se non passato viene considerata qualsiasi condivisione logistica */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** semestre della partizione logistica, se non passato viene considerata qualsiasi condivisione logistica */
    @SerialName("partCod")
    val partialCode: String? = null,

    /** id della condivisione logistica, se non passata viene considerata qualisiasi condivisione logistica */
    @SerialName("adLogId")
    val activityLogId: Long? = null
)
