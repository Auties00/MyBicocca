package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3CareerErrorDetail(
    /** id dell'errore di carriera */
    @SerialName("errcarrtstId")
    val careerTestErrorId: Long? = null,

    /** id del messaggio di errore progressivo per l'errore di carriera */
    @SerialName("progId")
    val programId: Int? = null,

    /** codice dell'errore */
    @SerialName("cod")
    val code: String? = null,

    /** descrizione dell'errore */
    @SerialName("des")
    val description: String? = null,

    /** tipo di entità di carriera collegata all'errore */
    @SerialName("entitaCarrCod")
    val careerEntityCode: String? = null,

    /** valore numerico variabile */
    @SerialName("valNum")
    val numericValue: Long? = null,

    /** tipo di errore (0 - bloccante, 1 - warning) */
    @SerialName("tipoErr")
    val errorType: Int? = null
)

@Serializable
data class Esse3ExamSessionsStatsByStatus(
    /** stato dell'appello su cui effettuare il conteggio */
    @SerialName("stato")
    val state: String? = null,

    /** descruzione dello stato dell'appello su cui effettuare il conteggio */
    @SerialName("statoDes")
    val stateDescription: String? = null,

    /** numero di appelli nello stato indicato e nella finestra selezionata */
    @SerialName("numApp")
    val callNumber: Int? = null,

    /** numero di appelli senza iscrizioni nello stato indicato e nella finestra selezionata */
    @SerialName("numAppSenzaIscr")
    val callWithoutEnrollmentNumber: Int? = null
)

@Serializable
data class Esse3BookingManagementHeaderType(
    /** codice che identifica il tipoGestPren */
    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    /** descrizione libera */
    @SerialName("des")
    val description: String? = null,

    /** indica se è prevista la gestione delle liste studenti in fase di prenotazione */
    @SerialName("listaStudentiFlg")
    val studentListFlag: Int? = null,

    /** indica se è prevista la gestione delle regole appelli */
    @SerialName("regAppFlg")
    val applicationRegistrationFlag: Int? = null,

    /** imposta la modalità di cancellazione delle prenotazioni */
    @SerialName("chkCancPren")
    val checkCancelBooking: Int? = null
)

@Serializable
data class Esse3ShiftCommissionTeacher(
    /** descrizione del ruolo del docente in commissione. Il Ruolo P (Presidente) può essere associato ad un solo docente */
    @SerialName("ruoloDes")
    val roleDescription: String? = null,

    /** cognome del docente */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** nome del docente */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** Id address book della persona in UGOV */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** codice del ruolo del docente in commissione. Il Ruolo P (Presidente) può essere associato ad un solo docente */
    @SerialName("ruoloCod")
    val roleCode: String? = null,

    /** id del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id progressivo del turno rispetto alla terna (cds_id,ad_id,app_id), viene valorizzato solo se il docente è collegato ad una commissione di un turno */
    @SerialName("appLogId")
    val callLogId: Int? = null
)

@Serializable
data class Esse3ExamSessionEnrollmentParameters(
    /** id della riga di libretto da prenotare */
    @SerialName("adsceId")
    val activityChoiceId: Long = 0L,

    /** tipo di iscrizione dello studente */
    @SerialName("tipoIscrStu")
    val studentEnrollmentType: String? = null,

    /** nota dello studente inserita in fase di prenotazione */
    @SerialName("notaStu")
    val studentNote: String? = null,

    /** eventuale turno a cui prenotare lo studente, se vuoto viene assegnato dal sistema */
    @SerialName("appLogId")
    val callLogId: Int? = null,

    /** tag selezionato dallo studente in fase di prenotazione */
    @SerialName("tagCod")
    val tagCode: String? = null,

    /** eventuale tipo di attore con cui si vuole effettuare la preotazione, valido solo se l'utente che effettua la chiamata è un utente tecnico */
    @SerialName("attoreCod")
    val actorCode: String? = null,

    /** tipo di svolgimento esame, se null viene imposato il default previsto nella tipi_gest_app, altrimenti viene inserito il valore richiesto. Lo studente può selezionare solo il default oppure un valore con richiesta_flg = 1 */
    @SerialName("tipoSvolgimentoEsame")
    val examType: String? = null,

    @SerialName("misureCompensative")
    val compensatoryMeasures: List<Esse3ExamSessionEnrollmentCompensatoryMeasuresParameters> = emptyList()
)

@Serializable
data class Esse3ExamSession(
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
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3ExamSessionModificationCompensatoryMeasuresParameters(
    /** codice della misura compensativa */
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String = "",

    /** descrizione libera della misura compensativa, va valorizzato solo se la misura ha una descrizone libera. */
    @SerialName("desLiberaMisura")
    val freeMeasureDescription: String? = null,

    /** consente di cancellare la misura compensativa indicata. Il controllo della misura avviene tramite il codcie de descrizione se Libera */
    @SerialName("deleteFlg")
    val deleteFlag: Boolean? = null
)

@Serializable
data class Esse3ExamSessionBookingConfigDetails(
    /** codice identificativo del controllo di prenotazione */
    @SerialName("key")
    val key: String? = null,

    /** valore del controllo */
    @SerialName("value")
    val value: String? = null
)

@Serializable
data class Esse3UpdateExamSession(
    /** flag che consente di inviare una comunicazione agli iscritti se lo condizioni di modifica lo richiedono */
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

    /** modalità dell'esame definita nell'appello (valorizzata se il par_conf=CONTR_TIPO_ESA_APP=0), i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale Congiunto=SOC, Scritto e Orale Separato=SOS). */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    /** modalità di iscrizione definita nell'appello, i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale=SO). */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** id progressivo dell'appello rispetto alla coppia cds_id,ad_id */
    @SerialName("appId")
    val callId: Long? = null,

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
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3SystemLogCommitment(
    /** Codice Evento */
    @SerialName("eventoCod")
    val eventCode: String = "",

    /** codice univoco dell'impegno */
    @SerialName("impegnoCod")
    val commitmentCode: String? = null,

    /** chiave dell'appello da collegare all'impegno (se non specificata viene decodificata da eventoCod) */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** chiave dell'appello da collegare all'impegno (se non specificata viene decodificata da eventoCod) */
    @SerialName("adId")
    val activityId: Long = 0L,

    /** chiave dell'appello da collegare all'impegno (se non specificata viene decodificata da eventoCod) */
    @SerialName("appId")
    val callId: Long = 0L,

    /** data dell'evento in formato DD/MM/YYYY */
    @SerialName("dataEvento")
    val eventDate: String? = null,

    /** data dell'impegno in formato DD/MM/YYYY */
    @SerialName("dataImpegno")
    val commitmentDate: String = "",

    /** data/ora di inizio dell'impegno in formato DD/MM/YYYY HH24:MI:SS */
    @SerialName("oraInizioImpegno")
    val commitmentStartTime: String = "",

    /** data/ora di inizio dell'impegno in formato DD/MM/YYYY HH24:MI:SS */
    @SerialName("oraFineImpegno")
    val commitmentEndTime: String = "",

    /** codice dell'aula associato all'impegno */
    @SerialName("extAulaCod")
    val externalClassroomCode: String = "",

    /** numero di posti collegati all'aula */
    @SerialName("posti")
    val seats: Int = 0,

    /** tolleranza di posti in eccedenza rispetto ai posti specificati nel campo posti */
    @SerialName("tolleranza")
    val tolerance: Int = 0,

    /** descrizione del turno */
    @SerialName("desTurno")
    val shiftDescription: String? = null,

    /** fattore di partzione da associare al turno4 */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** dominio di partzione da associare al turno4 */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null
)

@Serializable
data class Esse3SystemLogEventTestExport(
    /** identificativo dell'elaborazione */
    @SerialName("elabId")
    val processingId: Long? = null,

    /** identificativo del pacchetto */
    @SerialName("chiavePacchetto")
    val packageKey: String? = null,

    /** cds_id del corso di studio del pacchetto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** ad_id dell'AD del pacchetto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** app_id dell'appello del pacchetto */
    @SerialName("appId")
    val callId: Long? = null,

    /** codice della facoltà del corso di studio del figlio dell'appello */
    @SerialName("facCodFiglio")
    val childFacultyCode: String? = null,

    /** codice corso di studio del figlio dell'appello */
    @SerialName("cdsCodFiglio")
    val childCourseOfStudyCode: String? = null,

    /** codice attività del figlio dell'appello */
    @SerialName("adCodFiglio")
    val childActivityCode: String? = null,

    /** Tipo di iscrizione dell'appello */
    @SerialName("tipoAttivitaCod")
    val activityTypeCode: String? = null,

    /** anno in cui ricade la data dell'esame */
    @SerialName("annoAccademico")
    val academicYear: Int? = null,

    /** descrizione del pacchetto */
    @SerialName("descrizione")
    val description: String? = null,

    /** descrizione del pacchetto in inglese */
    @SerialName("descrizioneEng")
    val descriptionEnglish: String? = null,

    /** matricola del docente responsabile */
    @SerialName("presidenteComm")
    val committeePresident: String? = null,

    /** concatenazione dei docenti della commissione (nome e cognome) */
    @SerialName("commissioneAllExport")
    val committeeAllExport: String? = null,

    @SerialName("commissioneCorrente")
    val currentCommittee: List<Esse3ExamSessionCommissionTeacher> = emptyList(),

    /** massimo anno di sessione previsto per l'appello */
    @SerialName("maxAaSesId")
    val maxAcademicYearSessionId: Long? = null,

    /** chiave massima sessione */
    @SerialName("chiaveMaxSessione")
    val maxSessionKey: String? = null,

    /** numero iscritti totali all'appello */
    @SerialName("numPostiTotale")
    val totalSeatsNumber: Long? = null,

    @SerialName("esameComune")
    val commonExam: List<Esse3SystemLogEventRowExport> = emptyList(),

    /** note verso il sistema di logistica esterno */
    @SerialName("noteSistLog")
    val systemLogNotes: String? = null,

    /** Fattore di partizione del turno di default dell'appello */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** Dominio di partizione del turno di default dell'appello */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** id della sede dell'evento */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** id della sede di U-GOV dell'evento */
    @SerialName("sedeProgDidId")
    val didacticProgramSiteId: Long? = null,

    /** descrizione della sede dell'evento */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** Tipologia di modifica rispetto al medesimo pacchetto sulla elaborazione di diff indicata in testata. */
    @SerialName("tipoDiff")
    val differenceType: Int? = null
)

@Serializable
data class Esse3TeacherAuthorizations(
    /** anno di abilitazione del docente */
    @SerialName("aaAbilDocId")
    val academicYearTeacherAuthorizationId: Int = 0,

    /** id del corso di studio di erogazione dell'esame comune */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** codice del corso di studio che eroga l'esame comune */
    @SerialName("cdsDefAppCod")
    val courseOfStudyDefaultCallCode: String? = null,

    /** id dell'attività didattica su cui è possibile inserire appelli */
    @SerialName("adId")
    val activityId: Int = 0,

    /** codice dell'attività didattica che eroga l'esame comune */
    @SerialName("adDefAppCod")
    val activityExamDefinitionCode: String? = null,

    /** id del docente */
    @SerialName("docenteId")
    val lecturerId: Long = 0L,

    /** cognome del docente */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** nome del docente */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** modalità di defnizinoe degli appelli */
    @SerialName("defApp")
    val defaultCall: Int = 0,

    /** modalità di visualizzazione degli appelli */
    @SerialName("visApp")
    val callVisible: Int = 0,

    /** minimo anno di sessione dell'appello per cui risulta valida l'abilitazione */
    @SerialName("minAaSesId")
    val minAcademicYearSessionId: Int? = null,

    /** massimo anno di sessione dell'appello per cui risulta valida l'abilitazione */
    @SerialName("maxAaSesId")
    val maxAcademicYearSessionId: Int? = null,

    /** se abilita la gestione imposta il filtro sulla modalità di valutazione per l'inserimento esiti nell'area web docente; indica il tipo di valutazione giudizio da utilizzare */
    @SerialName("gruppoGiudCod")
    val judgmentGroupCode: String? = null,

    /** se abilita la gestione imposta il filtro sulla modalità di valutazione per l'inserimento esiti nell'area web docente; indica il tipo di votazione a voto da utilizzare per la coppia AD-CDS impostata. SE non è valorizzato questo campo controllare il campo relativo ai giudizii gruppoGiudCod. */
    @SerialName("gruppoVotoCod")
    val gradeGroupCode: String? = null,

    /** coppie AD/CDS figlie di un eventuale esame comune, in questo caso il campo appId risulta nullo */
    @SerialName("figliEsacom")
    val esacomChildren: List<Esse3SharedExamAuthorization> = emptyList()
)

@Serializable
data class Esse3SharedExamInsert(
    /** flag che indica se l'esame comune è collegato ad una mutuazione */
    @SerialName("mutFlg")
    val mutualFlag: Int = 0,

    /** flag che indica se l'esame comune è collegato ad una condivisione logistica */
    @SerialName("logCondFlg")
    val logConditionFlag: Int = 0
)

@Serializable
data class Esse3UpdateShift(
    @SerialName("commissione")
    val committee: List<Esse3UpdateTeacherCommission> = emptyList(),

    /** id progressivo del turno rispetto alla terna (cds_id,ad_id,app_id) */
    @SerialName("appLogId")
    val callLogId: Int = 0,

    /** descrizione del turno */
    @SerialName("des")
    val description: String? = null,

    /** data e ora del turno (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    /** id dell'edificio associato al turno */
    @SerialName("edificioId")
    val buildingId: Int? = null,

    /** id del'aula associata al turno. NB: l'aula e definita all'interno di un edificio; Nel caso questo campo sia valorizzato occorre anche valorizzare edificioId */
    @SerialName("aulaId")
    val classroomId: Int? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione del dominio di partizione, nel caso questo campo sia valorizzato occorre valorizzare anche fatPartCod poichè il codice è relativo ad un fattore di partizione dato */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null
)

@Serializable
data class Esse3SharedExamSession(
    /** id del corso di studio di definizione dell'esame comune, se appId valorizzato coincide con il cds di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** id dell'attività didattica di definizione dell'esame comune, se appId valorizzato coincide con l'attività di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int = 0,

    /** anno di riferimento dell'esame comune */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    /** id del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long = 0L,

    /** id dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioId")
    val childActivityId: Long = 0L,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int = 0
)

@Serializable
data class Esse3ExamSessionEnrollmentErrors(
    /** identificativo univoco della prenotazione */
    @SerialName("applistaId")
    val applicationListId: Long? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id progressivo del turno rispetto alla terna (cds_id,ad_id,app_id) */
    @SerialName("appLogId")
    val callLogId: Int? = null,

    /** id della carriera dello studente che ha effettato la prenotazione */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** tipo di errore */
    @SerialName("tipoErrore")
    val errorType: String? = null,

    /** descrizione dell'errore */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3Esse3SystemLogCommitment(
    /** codice univoco dell'impegno */
    @SerialName("impegnoCod")
    val commitmentCode: String? = null,

    /** chiave del turno collegata all'impegno */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** chiave del turno collegata all'impegno */
    @SerialName("adId")
    val activityId: Long? = null,

    /** chiave del turno collegata all'impegno */
    @SerialName("appId")
    val callId: Long? = null,

    /** chiave del turno collegata all'impegno */
    @SerialName("appLogId")
    val callLogId: Long? = null,

    /** chiave dell'appello collegato all'impegno */
    @SerialName("appelloId")
    val examCallId: Long? = null,

    /** data di inizio delle iscrizioni dell'appello in formato DD/MM/YYYY */
    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    /** data di fine delle iscrizioni dell'appello in formato DD/MM/YYYY */
    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    /** data di inizio dell'appello in formato DD/MM/YYYY */
    @SerialName("dataInizioApp")
    val callStartDate: String? = null,

    /** data del turno collegato all' impegno in formato DD/MM/YYYY */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** ora del turno collegato all' impegno in formato HH24/MI */
    @SerialName("oraEsa")
    val graduationTime: String? = null,

    /** matricola del presidente dell'appello */
    @SerialName("matricolaPres")
    val presidentMatricola: String? = null,

    /** conteggio degli iscritti al turno collegato all'impegno */
    @SerialName("cntIscritti")
    val enrolledCount: Int? = null
)

@Serializable
data class Esse3UpdateSystemLogCommitments(
    /** parametro che regola l'invio delle comunicazioni agli studenti: 0 - nessuna comunicazione, 1 - comunicazione abilitata, -1 - blocco nel caso di studenti */
    @SerialName("invioCom")
    val committeeSending: Int = 0,

    @SerialName("impegni")
    val commitments: List<Esse3UpdateSystemLogCommitment> = emptyList()
)

@Serializable
data class Esse3UpdateSystemLogCommitment(
    /** ID della corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** ID dell'attività didattica */
    @SerialName("adId")
    val activityId: Long? = null,

    /** ID dell'appello */
    @SerialName("appId")
    val callId: Long? = null,

    /** Codice Evento */
    @SerialName("eventoCod")
    val eventCode: String = "",

    /** codice univoco dell'impegno */
    @SerialName("impegnoCod")
    val commitmentCode: String = "",

    /** data dell'impegno in formato DD/MM/YYYY */
    @SerialName("dataImpegno")
    val commitmentDate: String? = null,

    /** data/ora di inizio dell'impegno in formato DD/MM/YYYY HH24:MI:SS */
    @SerialName("oraInizioImpegno")
    val commitmentStartTime: String? = null,

    /** data/ora di inizio dell'impegno in formato DD/MM/YYYY HH24:MI:SS */
    @SerialName("oraFineImpegno")
    val commitmentEndTime: String? = null,

    /** codice dell'aula associato all'impegno */
    @SerialName("extAulaCod")
    val externalClassroomCode: String? = null,

    /** numero di posti collegati all'aula */
    @SerialName("posti")
    val seats: Int? = null,

    /** tolleranza di posti in eccedenza rispetto ai posti specificati nel campo posti */
    @SerialName("tolleranza")
    val tolerance: Int? = null,

    /** descrizione del turno */
    @SerialName("desTurno")
    val shiftDescription: String? = null,

    /** fattore di partzione da associare al turno4 */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** dominio di partzione da associare al turno4 */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** indica l'operazione da applicare all'impegno */
    @SerialName("operazione")
    val operation: String = ""
)

@Serializable
data class Esse3PublicationParameters(
    /** Email del mittente della mail che viene inviata agli studenti, se valorizzato deve essere presente anche il nome del mittente */
    @SerialName("emailMittente")
    val senderEmail: String? = null,

    /** Nome del mittente della mail che viene inviata agli studenti, se valorizzato deve essere presente anche la mail del mittente */
    @SerialName("nomeMittente")
    val senderName: String? = null,

    /** Data pubblicazione (DD/MM/YYYY) data di pubblicazione degli esiti (data della pubblicazione deve essere valorizzata con la data odierna) */
    @SerialName("dataPubbl")
    val publicationDate: String = "",

    /** Data ultimo rifiuto data di ultimo rifiuto per gli studenti (opzionale a seconda della configurazione impostata su esse3) */
    @SerialName("dataUltimoRif")
    val lastReferenceDate: String? = null,

    /** Nota mail (Parametro facoltativo che, se valorizzato, viene utilizzato nel template dell'invio della comunicazione) */
    @SerialName("notaMailStu")
    val studentMailNote: String? = null,

    /** Nota from (Parametro facoltativo che, se valorizzato, viene utilizzato nel template dell'invio della comunicazione) */
    @SerialName("notaFrom")
    val noteFrom: String? = null,

    /** Flag che indica la volontà  di inviare la comunicazione */
    @SerialName("inviaComFlg")
    val sendCommitteeFlag: Int = 0,

    /** lista degli applistaId da pubblicare, se nullo vengono pubblicati tutti i possibili studenti */
    @SerialName("stuDaPubblicare")
    val studentToPublish: List<Int> = emptyList()
)

@Serializable
data class Esse3ShiftWithCommission(
    /** numero di studenti iscritti al turno */
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    /** descrizione del dominio di partizione, nel caso questo campo sia valorizzato occorre valorizzare anche fatPartCod poichè il codice è relativo ad un fattore di partizione dato */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione dell'aula */
    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    /** codice dell'aula sul sistema di logistica esterno */
    @SerialName("extAulaCod")
    val externalClassroomCode: String? = null,

    /** codice dell'aula */
    @SerialName("aulaCod")
    val classroomCode: String? = null,

    /** descrizione dell'edificio */
    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    /** codice dell'edificio */
    @SerialName("edificioCod")
    val buildingCode: String? = null,

    /** descrizione del turno */
    @SerialName("des")
    val description: String? = null,

    /** data e ora del turno (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    /** id dell'edificio associato al turno */
    @SerialName("edificioId")
    val buildingId: Int? = null,

    /** id del'aula associata al turno. NB: l'aula e definita all'interno di un edificio; Nel caso questo campo sia valorizzato occorre anche valorizzare edificioId */
    @SerialName("aulaId")
    val classroomId: Int? = null,

    /** id progressivo del turno rispetto alla terna (cds_id,ad_id,app_id) */
    @SerialName("appLogId")
    val callLogId: Int? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("commissione")
    val committee: List<Esse3ShiftCommissionTeacher> = emptyList()
)

@Serializable
data class Esse3SystemLogEventRowExport(
    /** identificativo dell'elaborazione */
    @SerialName("elabId")
    val processingId: Long? = null,

    /** identificativo del pacchetto */
    @SerialName("chiavePacchetto")
    val packageKey: String? = null,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsCodFiglio")
    val childCourseOfStudyCode: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adCodFiglio")
    val childActivityCode: String? = null,

    /** codice della facoltà figlia dell'esame comune */
    @SerialName("facCodFiglio")
    val childFacultyCode: String? = null,

    /** indica qual è la contestualizzazione su cui è definito l'appello sulla p10_app */
    @SerialName("flgLogicoMaster")
    val masterLogicalFlag: Int? = null,

    /** numero iscritti all`appello */
    @SerialName("numPosti")
    val seatsNumber: Long? = null
)

@Serializable
data class Esse3SystemLogCommitmentImportError(
    /** Codice Evento */
    @SerialName("eventoCod")
    val eventCode: String = "",

    /** codice univoco dell'impegno */
    @SerialName("impegnoCod")
    val commitmentCode: String? = null,

    /** chiave dell'appello da collegare all'impegno (se non specificata viene decodificata da eventoCod) */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** chiave dell'appello da collegare all'impegno (se non specificata viene decodificata da eventoCod) */
    @SerialName("adId")
    val activityId: Long = 0L,

    /** chiave dell'appello da collegare all'impegno (se non specificata viene decodificata da eventoCod) */
    @SerialName("appId")
    val callId: Long = 0L,

    /** data dell'evento in formato DD/MM/YYYY */
    @SerialName("dataEvento")
    val eventDate: String? = null,

    /** data dell'impegno in formato DD/MM/YYYY */
    @SerialName("dataImpegno")
    val commitmentDate: String = "",

    /** data/ora di inizio dell'impegno in formato DD/MM/YYYY HH24:MI:SS */
    @SerialName("oraInizioImpegno")
    val commitmentStartTime: String = "",

    /** data/ora di inizio dell'impegno in formato DD/MM/YYYY HH24:MI:SS */
    @SerialName("oraFineImpegno")
    val commitmentEndTime: String = "",

    /** codice dell'aula associato all'impegno */
    @SerialName("extAulaCod")
    val externalClassroomCode: String = "",

    /** numero di posti collegati all'aula */
    @SerialName("posti")
    val seats: Int = 0,

    /** tolleranza di posti in eccedenza rispetto ai posti specificati nel campo posti */
    @SerialName("tolleranza")
    val tolerance: Int = 0,

    /** descrizione del turno */
    @SerialName("desTurno")
    val shiftDescription: String? = null,

    /** fattore di partzione da associare al turno4 */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** dominio di partzione da associare al turno4 */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** descrizione dell'errore */
    @SerialName("desErrore")
    val errorDescription: String? = null,

    /** codice dell'errore */
    @SerialName("codErrore")
    val errorCode: Int? = null
)

@Serializable
data class Esse3CompensatoryMeasureEnrollment(
    /** identificativo univoco della prenotazione */
    @SerialName("applistaId")
    val applicationListId: Long? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id progressivo del turno rispetto alla terna (cds_id,ad_id,app_id) */
    @SerialName("appLogId")
    val callLogId: Int? = null,

    /** id della carriera dello studente che ha effettato la prenotazione */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** codice della misura compensativa */
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String? = null,

    /** indca se la misura compensativa ha una descrizione libera */
    @SerialName("desLiberaFlg")
    val freeDescriptionFlag: Int? = null,

    /** indica se la misura compensativa è visibile da web */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** descrizione della misura compensativa */
    @SerialName("des")
    val description: String? = null,

    /** stato della misura compensativa */
    @SerialName("statoMisComp")
    val compensatoryMeasureState: String? = null,

    /** descrizione dello stato della misura compensativa */
    @SerialName("statoMisCompDes")
    val compensatoryMeasureStateDescription: String? = null
)

@Serializable
data class Esse3BookingModificationParameters(
    /** eventuale tipo di attore con cui si vuole modificare la preotazione, valido solo se l'utente che effettua la chiamata è un utente tecnico */
    @SerialName("attoreCod")
    val actorCode: String? = null,

    /** tipo di svolgimento esame, se null viene imposato il default previsto nella tipi_gest_app, altrimenti viene inserito il valore richiesto. Lo studente può selezionare solo il default oppure un valore con richiesta_flg = 1 */
    @SerialName("tipoSvolgimentoEsame")
    val examType: String? = null,

    @SerialName("misureCompensative")
    val compensatoryMeasures: List<Esse3ExamSessionModificationCompensatoryMeasuresParameters> = emptyList()
)

@Serializable
data class Esse3ExamSessionLinkInsert(
    /** Id del template voto dell'appello collegato */
    @SerialName("templvotoRelId")
    val voteTemplateRelationId: Long? = null,

    /** chiave dell'appello collegato (cds_id) */
    @SerialName("cdsRelId")
    val courseOfStudyRelationId: Long? = null,

    /** chiave dell'appello collegato (ad_id) */
    @SerialName("adRelId")
    val activityRelationId: Long? = null,

    /** chiave dell'appello collegato (app_id) */
    @SerialName("appRelId")
    val callRelationId: Long? = null,

    /** tipo di relazione tra l'appello e il relativo link. */
    @SerialName("tipoLinkRelAppCod")
    val relationCallLinkTypeCode: String? = null
)

@Serializable
data class Esse3EnrollmentTag(
    /** codice del tag dell'appello */
    @SerialName("tagCod")
    val tagCode: String? = null,

    /** descrizione del tag dell'appello */
    @SerialName("tagDes")
    val tagDescription: String? = null,

    /** codice della lingua a cui si riferiscono i tag indicati */
    @SerialName("linguaIso6392Cod")
    val languageIso6392Code: String? = null,

    /** codice del livello di uscita della lingua al quale si riferisce il tag */
    @SerialName("livCertLinUscitaCod")
    val exitLanguageCertificationLevelCode: String? = null,

    /** descrizione del livello di uscita della lingua al quale si riferisce il tag */
    @SerialName("livCertLinUscitaDes")
    val exitLanguageCertificationLevelDescription: String? = null
)

@Serializable
data class Esse3ExamType(
    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** codice del tipo svolgimento esame */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    /** descrizione del tipo svolgimento esame */
    @SerialName("tipoSvolgimentoEsameDes")
    val examTypeDescription: String? = null,

    /** eventuale tipo di attore per cui si vogliono visualizzare i tipi svolgimento compatibili, valido solo se l'utente che effettua la chiamata è un utente tecnico */
    @SerialName("attoreCod")
    val actorCode: String? = null,

    /** indica se il tipo svolgimento esame è visibile da web */
    @SerialName("webFlg")
    val webFlag: Int? = null
)

@Serializable
data class Esse3UpdateResult(
    /** nel caso l'oerazione venga effettauta da un utente tecnico indica il docenteId che effettua l'operazione. Non richiesto se l'utente che effettua l'operazione è un docente */
    @SerialName("docenteImpersId")
    val lecturerImpersonalId: Long? = null,

    /** flag che indica se sovrascrivere un esito, se impostato a 0 l'esito viene inserito solo se non presente */
    @SerialName("sovrascritturaFlg")
    val overwriteFlag: Int = 0,

    /** voto, valorizzato se modValCod è V. Gli esiti delle prove finali (cioè quelle che prevedono il caricamento nella riga di libretto) sono INTERI, gli esti di prove parziali invece possono avere 2 cifre decimali. Il voto con lode si esprime con la votazione massima + 1 (31= Trenta e lode). La votazione insufficiente viene impostata con 0 */
    @SerialName("voto")
    val grade: Int? = null,

    /** codice che indica il tipo di giudizio utilizzato */
    @SerialName("tipoGiudCod")
    val judgmentTypeCode: String? = null,

    /** flag che indica l'assenza dello studente, impostato a 1 solo se il campo voto, giudizio e ritirato risultano vuoti */
    @SerialName("assenteFlg")
    val absentFlag: Int = 0,

    /** flag che indica il ritiro dello studente, impostato a 1 solo se il campo voto, giudizio e assente risultano vuoti */
    @SerialName("ritiratoFlg")
    val withdrawnFlag: Int = 0,

    /** data della prova, il formato con cui deve essere definita la data è DD/MM/YYYY, valorizzata solo se diversa dalla data del turno */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** contiene le domande d'esame */
    @SerialName("domandeEsame")
    val examApplications: String? = null,

    /** contiene la nota da visualizzare allo studente sulla pubblicazione */
    @SerialName("notaPubbl")
    val publicNote: String? = null,

    /** valore della presa visione da impostare */
    @SerialName("presaVisione")
    val acknowledgmentOfReceipt: Esse3AcknowledgmentOfReceipt? = null,

    @SerialName("appCollegato")
    val linkedCall: Esse3ExamSessionLinkedToList? = null,

    /** codice del tipo di svolgimento esame, se viene passato */
    @SerialName("tipoSvolgimentoEsame")
    val examType: String? = null
)

@Serializable
data class Esse3Session(
    /** id della facoltà/dipartimento collegata al corso di studio a cui si rifersce la sessione */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** codice della facoltà/dipartimento collegata al corso di studio a cui si rifersce la sessione */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione della facoltà/dipartimento collegata al corso di studio a cui si rifersce la sessione */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** id corso di studio a cui si rifersce la sessione */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice del corso di studio a cui si rifersce la sessione */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di studio a cui si rifersce la sessione */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** anno di sessione */
    @SerialName("aaSesId")
    val academicYearSessionId: Int? = null,

    /** progressivo della sessione all'interno di un corso e di un anno di sessione */
    @SerialName("sesId")
    val sessionId: Int? = null,

    /** descrizione libera della sessione */
    @SerialName("des")
    val description: String? = null,

    /** data inizio sessione (DD/MM/YYYY) */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** data fine sessione (DD/MM/YYYY) */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** codice della sessione */
    @SerialName("sesCod")
    val sessionCode: String? = null,

    /** descrizione del codice della sessione */
    @SerialName("sesDes")
    val sessionDescription: String? = null,

    /** Flag che indica se la sessione è straordinaria (straFlg=1), cioè aperta solo agli studenti che hanno seguito il l´attività nello stesso anno della sessione (Anticipata) o nell´anno immediatamente precedente (Ritardato), come specificato meglio da TIPO_SES_COD. Se STRA_FLG = 0, allora TIPO_SES_COD non ha significato */
    @SerialName("straFlg")
    val foreignFlag: Int? = null,

    /** codice tipo di sessione associata al controllo del parametro straFlg */
    @SerialName("tipoSesCod")
    val sessionTypeCode: String? = null,

    /** descrizione tipo di sessione associata al controllo del parametro straFlg */
    @SerialName("tipoSesDes")
    val sessionTypeDescription: String? = null,

    /** unità di misura per il controllo delle prove sostenibili all'interno della sessione dei corsi estivi */
    @SerialName("umCod")
    val measurementUnitCode: Esse3MeasurementUnitCode? = null,

    /** Numero massimo di AD o CFU totalizzabili all'interno della sessione dei corsi estivi. */
    @SerialName("maxNormali")
    val maxNormal: Int? = null,

    /** Numero massimo di AD o CFU totalizzabili all'interno della sessione dei corsi estivi. */
    @SerialName("maxLab")
    val maxLab: Int? = null,

    /** il flag indica se all'interno della sessione si svolgeranno gli esami per i corsi estivi */
    @SerialName("vincFlg")
    val winnerFlag: Int? = null,

    /** sessione riservata per determinate prove (parziali o finali) se null la sessione vale per tutti i tipi di prove */
    @SerialName("tipoValSes")
    val sessionEvaluationType: Esse3SessionEvaluationType? = null,

    /** delta di giorni entro cui aprire le liste iscritti per gli appelli che vengono associati a questa sessione */
    @SerialName("numGgIniIscr")
    val enrollmentStartDaysNumber: Int? = null,

    /** id del gruppo di condizioni SQL da applicare alle prenotazioni effettuate con questa sessione */
    @SerialName("grpCondSqlId")
    val groupSqlConditionId: Long? = null
)

@Serializable
data class Esse3ExamSessionEnrollmentCompensatoryMeasuresParameters(
    /** codice della misura compensativa */
    @SerialName("misuraCompensativaCod")
    val compensatoryMeasureCode: String = "",

    /** descrizione libera della misura compensativa, va valorizzato solo se la misura ha una descrizone libera. */
    @SerialName("desLiberaMisura")
    val freeMeasureDescription: String? = null
)

@Serializable
data class Esse3SystemLogSessionsExport(
    /** identificativo dell'elaborazione */
    @SerialName("elabId")
    val processingId: Long? = null,

    /** chiave sessione */
    @SerialName("chiaveSessione")
    val sessionKey: String? = null,

    /** codice della sessione */
    @SerialName("sesCod")
    val sessionCode: String? = null,

    /** Codice mnemonico del corso di studio */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione della sessione */
    @SerialName("des")
    val description: String? = null,

    /** Id della sessione */
    @SerialName("aaSesId")
    val academicYearSessionId: Int? = null,

    /** data inizio */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** data fine */
    @SerialName("dataFine")
    val endDate: String? = null
)

@Serializable
data class Esse3ShiftInsert(
    /** descrizione del dominio di partizione, nel caso questo campo sia valorizzato occorre valorizzare anche fatPartCod poichè il codice è relativo ad un fattore di partizione dato */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** id del'aula associata al turno. NB: l'aula e definita all'interno di un edificio; Nel caso questo campo sia valorizzato occorre anche valorizzare edificioId */
    @SerialName("aulaId")
    val classroomId: Int? = null,

    /** id dell'edificio associato al turno */
    @SerialName("edificioId")
    val buildingId: Int? = null,

    /** data e ora del turno (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    /** descrizione del turno */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3SessionInsertUpdate(
    /** anno della sessione */
    @SerialName("aaSesId")
    val academicYearSessionId: Int = 0,

    /** id progressivo della sessione  rispetto alla coppia (cdsId,aaSesId) */
    @SerialName("sesId")
    val sessionId: Int = 0
)

@Serializable
data class Esse3SharedExam(
    /** id del corso di studio di definizione dell'esame comune, se appId valorizzato coincide con il cds di erogazione dell'appello */
    @SerialName("cdsEsaId")
    val courseOfStudyGraduationId: Long? = null,

    /** id dell'attività didattica di definizione dell'esame comune, se appId valorizzato coincide con l'attività di erogazione dell'appello */
    @SerialName("adEsaId")
    val activityExamId: Int? = null,

    /** anno di riferimento dell'esame comune */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** codice del corso di studio padre dell'esame comune */
    @SerialName("cdsEsaCod")
    val courseOfStudyGraduationCode: String? = null,

    /** codice del corso di studio padre dell'esame comune */
    @SerialName("cdsEsaDes")
    val courseOfStudyGraduationDescription: String? = null,

    /** codice dell'attività didattica padre dell'esame comune */
    @SerialName("adEsaCod")
    val activityExamCode: String? = null,

    /** codice dell'attività didattica padre dell'esame comune */
    @SerialName("adEsaDes")
    val activityExamDescription: String? = null,

    /** id del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long? = null,

    /** id dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioId")
    val childActivityId: Long? = null,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null,

    /** flag che indica se l'esame comune è collegato ad una mutuazione */
    @SerialName("mutFlg")
    val mutualFlag: Int = 0,

    /** flag che indica se l'esame comune è collegato ad una condivisione logistica */
    @SerialName("logCondFlg")
    val logConditionFlag: Int = 0
)

@Serializable
data class Esse3ExamSessionSession(
    /** data fine sessione (DD/MM/YYYY) */
    @SerialName("dataFine")
    val endDate: String? = null,

    /** data inizio sessione (DD/MM/YYYY) */
    @SerialName("dataInizio")
    val startDate: String? = null,

    /** descrizione della sessione */
    @SerialName("sesDes")
    val sessionDescription: String? = null,

    /** id progressivo della sessione  rispetto alla coppia (cdsId,aaSesId) */
    @SerialName("sesId")
    val sessionId: Int? = null,

    /** anno della sessione */
    @SerialName("aaSesId")
    val academicYearSessionId: Int? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3CareerError(
    /** id dell'errore di carriera */
    @SerialName("errcarrtstId")
    val careerTestErrorId: Long? = null,

    /** descrizione dell'errore */
    @SerialName("des")
    val description: String? = null,

    /** tipo di entità di carriera collegata all'errore */
    @SerialName("entitaErrCarrCod")
    val careerErrorEntityCode: String? = null,

    /** valore alfanumerico variabile */
    @SerialName("valAlfa")
    val alphanumericValue: String? = null,

    /** valore numerico variabile */
    @SerialName("valNum")
    val numericValue: Long? = null,

    /** tipo di errore (0 - bloccante, 1 - warning) */
    @SerialName("tipoErr")
    val errorType: Int? = null,

    @SerialName("dettagli")
    val details: List<Esse3CareerErrorDetail> = emptyList()
)

@Serializable
data class Esse3SystemLogExport(
    /** identificativo dell'elaborazione */
    @SerialName("elabId")
    val processingId: Long? = null,

    /** elab_id su cui viene fatto il diff per marcare i pacchetti come modificati */
    @SerialName("diffElabId")
    val processingDifferenceId: Long? = null,

    /** descrizione dell'elaborazione */
    @SerialName("des")
    val description: String? = null,

    /** tipo di elaborazione 0 - Puntuale 1 - Massivo */
    @SerialName("tipoElab")
    val processingType: Long? = null,

    /** data di inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** identificativo dell'utente che ha fatto l'inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null
)

@Serializable
data class Esse3ExamSessionShift(
    /** numero di studenti iscritti al turno */
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    /** descrizione del dominio di partizione, nel caso questo campo sia valorizzato occorre valorizzare anche fatPartCod poichè il codice è relativo ad un fattore di partizione dato */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione dell'aula */
    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    /** codice dell'aula sul sistema di logistica esterno */
    @SerialName("extAulaCod")
    val externalClassroomCode: String? = null,

    /** codice dell'aula */
    @SerialName("aulaCod")
    val classroomCode: String? = null,

    /** descrizione dell'edificio */
    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    /** codice dell'edificio */
    @SerialName("edificioCod")
    val buildingCode: String? = null,

    /** descrizione del turno */
    @SerialName("des")
    val description: String? = null,

    /** data e ora del turno (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    /** id dell'edificio associato al turno */
    @SerialName("edificioId")
    val buildingId: Int? = null,

    /** id del'aula associata al turno. NB: l'aula e definita all'interno di un edificio; Nel caso questo campo sia valorizzato occorre anche valorizzare edificioId */
    @SerialName("aulaId")
    val classroomId: Int? = null,

    /** id progressivo del turno rispetto alla terna (cds_id,ad_id,app_id) */
    @SerialName("appLogId")
    val callLogId: Int? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3ExamSessionWithDetails(
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
    /** identificativo univoco della prenotazione */
    @SerialName("applistaId")
    val applicationListId: Long? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id progressivo del turno rispetto alla terna (cds_id,ad_id,app_id) */
    @SerialName("appLogId")
    val callLogId: Int? = null,

    /** id della carriera dello studente che ha effettato la prenotazione */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** id della prova collegata alla preontazione */
    @SerialName("adregId")
    val activityRegulationId: Long? = null,

    /** id della riga di libretto collegata alla prenotazione */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** id della matricola collegata alla prenotazione */
    @SerialName("matId")
    val matId: Long? = null,

    /** codice dell'attività prenotata dallo studente */
    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    /** descrizione dell'attività prenotata dallo studente */
    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    /** codice del corso di studio prenotato dallo studente */
    @SerialName("cdsAdStuCod")
    val courseOfStudyTeachingActivityStudentCode: String? = null,

    /** descrizione del corso di studio prenotato dallo studente */
    @SerialName("cdsAdStuDes")
    val courseOfStudyTeachingActivityStudentDescription: String? = null,

    /** id del corso di studio prenotato dallo studente */
    @SerialName("cdsAdIdStu")
    val courseOfStudyTeachingActivityStudentId: Long? = null,

    /** descrizione dell'appello */
    @SerialName("desAppello")
    val examCallDescription: String? = null,

    /** descrizione del turno a cui è iscritto lo studente */
    @SerialName("desTurno")
    val shiftDescription: String? = null,

    /** codice dell' aula a cui è iscritto lo studente */
    @SerialName("aulaCod")
    val classroomCode: String? = null,

    /** descrizione dell' aula a cui è iscritto lo studente */
    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    /** codice dell' edificio a cui è iscritto lo studente */
    @SerialName("edificioCod")
    val buildingCode: String? = null,

    /** descrizione dell' edificio a cui è iscritto lo studente */
    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    /** id della sede dell'appello */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** descrizione della sede dell'appello */
    @SerialName("sedeDes")
    val siteDescription: String? = null,

    /** data/ora del turno a cui è iscritto lo studente */
    @SerialName("dataOraTurno")
    val shiftDateTime: String? = null,

    /** anno di frequenza dell'attività */
    @SerialName("aaFreqId")
    val academicYearAttendanceId: Int? = null,

    /** Stato dell'attività didattica (codice) */
    @SerialName("statoAdsce")
    val teachingActivityChoiceState: Esse3State? = null,

    /** peso dell''attività didattica, il peso prevede due decimali opzionali */
    @SerialName("pesoAd")
    val teachingActivityWeight: Float? = null,

    /** userId attivo dello studente */
    @SerialName("userId")
    val userId: String? = null,

    /** codice matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** nome dello studente */
    @SerialName("nomeStudente")
    val studentName: String? = null,

    /** nome alias dello studente */
    @SerialName("nomeAlias")
    val aliasName: String? = null,

    /** cognome dello studente */
    @SerialName("cognomeStudente")
    val studentSurname: String? = null,

    /** codice fiscale dello studente */
    @SerialName("codFisStudente")
    val studentFiscalCode: String? = null,

    /** data di nascita dello studente (DD/MM/YYYY) */
    @SerialName("dataNascitaStudente")
    val studentBirthDate: String? = null,

    /** sesso dello studente */
    @SerialName("sessoStudente")
    val studentGender: Esse3StudentGender? = null,

    /** codice istat del comune di nascita dello studente */
    @SerialName("comuNascCodIstat")
    val birthMunicipalityIstatCode: String? = null,

    /** codice della cittadinanza straniera di nascita dello studente */
    @SerialName("cittStraNasc")
    val birthForeignCitizenship: String? = null,

    /** codice della cittadinanza dello studente */
    @SerialName("cittCod")
    val citizenshipCode: String? = null,

    /** codice corso di studio di iscrizione dello studente */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    /** descrizione corso di studio di iscrizione dello studente */
    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    /** id del corso di studio di iscrizione dello studente */
    @SerialName("cdsIdStu")
    val courseOfStudyStudentId: Long? = null,

    /** anno di ordinamento di iscrizione dello studente */
    @SerialName("aaOrdStuId")
    val academicYearOrderStudentId: Int? = null,

    /** codice percorso di studio di iscrizione dello studente */
    @SerialName("pdsStuCod")
    val studyPlanStudentCode: String? = null,

    /** descrizione percorso di studio di iscrizione dello studente */
    @SerialName("pdsStuDes")
    val studyPlanStudentDescription: String? = null,

    /** id del percorso di studio di iscrizione dello studente */
    @SerialName("pdsIdStu")
    val studyPlanStudentId: Long? = null,

    /** id della pubblicazione dell'esito */
    @SerialName("pubblId")
    val publicationId: Long? = null,

    /** stato di presa visione dell'esito */
    @SerialName("presaVisione")
    val acknowledgmentOfReceipt: Esse3AcknowledgmentOfReceipt? = null,

    /** utente che ha effettuato l'ultimo cambio di stato della presa visione */
    @SerialName("userIdPresaVisione")
    val userAcknowledgmentId: String? = null,

    /** gruppo dell'utente che ha effettuato l'ultimo cambio di stato della presa visione */
    @SerialName("userGrpPresaVisione")
    val userGroupAcknowledgment: Long? = null,

    /** data di ultimo rifiuto applicata al docente (DD/MM/YYYY) */
    @SerialName("dataRifEsito")
    val outcomeReferenceDate: String? = null,

    /** data di ultimo rifiuto applicata allo studente (DD/MM/YYYY) */
    @SerialName("dataRifEsitoStu")
    val studentOutcomeReferenceDate: String? = null,

    /** nota inserita dal docente durante la pubblicazione */
    @SerialName("notaPubbl")
    val publicNote: String? = null,

    /** codice della scala voti del libretto dello studente */
    @SerialName("gruppoVotoCod")
    val gradeGroupCode: String? = null,

    /** massimo voto disponibile per la scala voti */
    @SerialName("gruppoVotoMaxPunti")
    val gradeGroupMaxPoints: Int? = null,

    @SerialName("esito")
    val outcome: kotlinx.serialization.json.JsonObject? = null,

    /** flag che indica ci ha effettuato la prenotazione (es. attori SEG, DOC manuale_flg=1; attore STU manuale_flg=0) */
    @SerialName("manualeFlg")
    val manualFlag: Int? = null,

    /** data di sostenimento della prova (DD/MM/YYYY), se non valorizzata vale la data del turno */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** domade d'esame effettuate durante la prova */
    @SerialName("domandeEsame")
    val examApplications: String? = null,

    /** Nota inserita dallo studente in fase di prenotazione appello */
    @SerialName("notaStudente")
    val studentNote: String? = null,

    /** codice del tipo svolgimento esame dello studente */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    /** descrizione del tipo svolgimento esame dello studente */
    @SerialName("tipoSvolgimentoEsameDes")
    val examTypeDescription: String? = null,

    /** indica se il tipo di svolgimento esame è una richiesta dell'utente, cioè va convertita in un valore definitivo */
    @SerialName("tipoSvolgimentoEsameRichiestaFlg")
    val examTypeRequestFlag: String? = null,

    /** tag selezionato dallo studente in fase di prenotazione */
    @SerialName("tagCod")
    val tagCode: String? = null,

    /** classe di prenotazione associata allo studente in fase di prenotazione */
    @SerialName("autoTagCod")
    val autoTagCode: String? = null,

    /** livello di uscita della lingua se l'appello prevede le lingue, da valorizzare assieme a linguaUscitaCod */
    @SerialName("livUscitaCod")
    val exitLevelCode: String? = null,

    /** codice ISO6392 della lingua a cui si riferisce il livello di uscita, da valorizzare assieme a livUscitaCod */
    @SerialName("linguaUscitaCod")
    val exitLanguageCode: String? = null,

    /** data di prenotazione (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** codice che identifica il tipoDefApp dell'appello collegato alla prenotazione */
    @SerialName("tipoDefAppCod")
    val defaultCallTypeCode: String? = null,

    /** codice che identifica il tipoGestPren dell'appello collegato alla prenotazione */
    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    /** codice che identifica il tipoGestApp dell'appello collegato alla prenotazione */
    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    /** tipo di appello (PF=Prova Finale, PP=Prova Parziale) */
    @SerialName("tipoAppCod")
    val callTypeCode: String? = null,

    /** progressivo di prenotazione all'interno della lista iscritti */
    @SerialName("posiz")
    val position: Int? = null,

    /** progressivo di prenotazione all'interno della lista iscritti in ordine di data_ins */
    @SerialName("posizApp")
    val applicationPosition: Int? = null,

    /** data inizio iscrizioni (DD/MM/YYYY) */
    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    /** data fine iscrizioni (DD/MM/YYYY) */
    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    /** modalità di iscrizione definita nell'appello, i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale=SO). */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** modalità di iscrizione definita nell'appello, i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale Separato=SOS, Scritto e Orale Congiunto=SOC). */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    /** Anno di calendario dell'appello */
    @SerialName("aaCalId")
    val academicYearCalendarId: Int? = null,

    /** anno della sessione */
    @SerialName("aaSesId")
    val academicYearSessionId: Int? = null,

    /** descrizione della sessione */
    @SerialName("sesDes")
    val sessionDescription: String? = null,

    @SerialName("misureCompensative")
    val compensatoryMeasures: List<Esse3CompensatoryMeasureEnrollment> = emptyList(),

    @SerialName("warnings")
    val warnings: List<Esse3ExamSessionEnrollmentErrors> = emptyList()
)

@Serializable
data class Esse3UpdateExamSessionLink(
    /** tipo di relazione tra l'appello e il relativo link. */
    @SerialName("tipoLinkRelAppCod")
    val relationCallLinkTypeCode: String? = null,

    /** chiave dell'appello collegato (app_id) */
    @SerialName("appRelId")
    val callRelationId: Long? = null,

    /** chiave dell'appello collegato (ad_id) */
    @SerialName("adRelId")
    val activityRelationId: Long? = null,

    /** chiave dell'appello collegato (cds_id) */
    @SerialName("cdsRelId")
    val courseOfStudyRelationId: Long? = null,

    /** Id del template voto dell'appello collegato */
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
    /** ordine di visualizzazione del docente all'interno della commissione */
    @SerialName("ordineVisNum")
    val orderVisibleNumber: Int? = null,

    /** codice del ruolo del docente in commissione. Il Ruolo P (Presidente) può essere associato ad un solo docente */
    @SerialName("ruoloCod")
    val roleCode: String? = null,

    /** id del docente */
    @SerialName("docenteId")
    val lecturerId: Long = 0L
)

@Serializable
data class Esse3SharedExamResult(
    /** indica se l'esito può essere forzato */
    @SerialName("forceFlg")
    val forceFlag: Int? = null,

    /** errore durante la richiesta di modifica, se forceFlg = 1 allora l'esito può essere forzato */
    @SerialName("errMsg")
    val errorMessage: String? = null,

    @SerialName("dettaglioErrore")
    val errorDetail: Esse3CareerError? = null
)

@Serializable
data class Esse3ActivitiesPerExamSession(
    /** id del corso di studio su cui è possibile inserire appelli */
    @SerialName("cdsDefAppId")
    val courseOfStudyDefaultCallId: Long = 0L,

    /** id dell'attività didattica su cui è possibile inserire appelli */
    @SerialName("adDefAppId")
    val activityExamDefinitionId: Int = 0,

    /** ultimo anno di offerta in cui risulta erogata la coppia corso di studio / attività didattica */
    @SerialName("aaOffId")
    val academicYearOfferId: Int? = null,

    /** codice dell'attività didattica di erogazione dell'appello */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** codice dell'attività didattica di erogazione dell'appello */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del tratto di carriera dello studente collegato alla coppia CDS/AD */
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
    /** id del corso di studio di definizione dell'esame comune, se appId valorizzato coincide con il cds di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** id dell'attività didattica di definizione dell'esame comune, se appId valorizzato coincide con l'attività di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int = 0,

    /** anno di riferimento dell'esame comune */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    /** id del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long = 0L,

    /** id dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioId")
    val childActivityId: Long = 0L,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null
)

@Serializable
data class Esse3ShiftTeacherAuthorization(
    /** numero di studenti iscritti al turno */
    @SerialName("numIscritti")
    val enrolledNumber: Int? = null,

    /** descrizione del dominio di partizione, nel caso questo campo sia valorizzato occorre valorizzare anche fatPartCod poichè il codice è relativo ad un fattore di partizione dato */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** codice del fattore di partizione */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** descrizione dell'aula */
    @SerialName("aulaDes")
    val classroomDescription: String? = null,

    /** codice dell'aula sul sistema di logistica esterno */
    @SerialName("extAulaCod")
    val externalClassroomCode: String? = null,

    /** codice dell'aula */
    @SerialName("aulaCod")
    val classroomCode: String? = null,

    /** descrizione dell'edificio */
    @SerialName("edificioDes")
    val buildingDescription: String? = null,

    /** codice dell'edificio */
    @SerialName("edificioCod")
    val buildingCode: String? = null,

    /** descrizione del turno */
    @SerialName("des")
    val description: String? = null,

    /** data e ora del turno (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataOraEsa")
    val graduationDateTime: String? = null,

    /** id dell'edificio associato al turno */
    @SerialName("edificioId")
    val buildingId: Int? = null,

    /** id del'aula associata al turno. NB: l'aula e definita all'interno di un edificio; Nel caso questo campo sia valorizzato occorre anche valorizzare edificioId */
    @SerialName("aulaId")
    val classroomId: Int? = null,

    /** id progressivo del turno rispetto alla terna (cds_id,ad_id,app_id) */
    @SerialName("appLogId")
    val callLogId: Int? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Id del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null
)

@Serializable
data class Esse3ExamSessionManagementHeaderType(
    /** codice che identifica il tipoGestApp */
    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    /** descrizione libera */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3ExamSessionCommissionTeacherInsert(
    /** id del docente */
    @SerialName("docenteId")
    val lecturerId: Long = 0L,

    /** codice del ruolo del docente in commissione. Il Ruolo P (Presidente) può essere associato ad un solo docente */
    @SerialName("ruoloCod")
    val roleCode: String = ""
)

@Serializable
data class Esse3ExamSessionTgaConfigDetails(
    /** codice identificativo del parametro di configurazione della tipi_gest_app */
    @SerialName("key")
    val key: String? = null,

    /** valore del controllo */
    @SerialName("value")
    val value: String? = null
)

@Serializable
data class Esse3ExamSessionLinkedToList(
    /** tipo di operazione da applicare alla prenotazione sull'appello collegato */
    @SerialName("opType")
    val operationType: Esse3OperationType,

    /** id del corso di studio di erogazione dell'appello collegato */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id dell'attività didattica di erogazione dell'appello collegato */
    @SerialName("adId")
    val activityId: Long? = null,

    /** id dell'appello collegato */
    @SerialName("appId")
    val callId: Long? = null,

    /** id del turno nel caso di scelta turno per la prenotazione all'appello collegato */
    @SerialName("appLogId")
    val callLogId: Long? = null,

    /** nota dello studente per la prenotazione all'appello collegato */
    @SerialName("notaStu")
    val studentNote: String? = null,

    /** livello di uscita della lingua per la prenotazione all'appello collegato */
    @SerialName("tagCod")
    val tagCode: String? = null
)

@Serializable
data class Esse3ExamSessionLink(
    /** descrizione dell'appello collegato */
    @SerialName("desRelApp")
    val relationCallDescription: String? = null,

    /** tipologia di iscrizione dell'appello */
    @SerialName("tipoIscrRelCod")
    val enrollmentRelationTypeCode: String? = null,

    /** data di inizio dell'appello collegato in formato DD/MM/YYYY */
    @SerialName("dataInizioRelApp")
    val relationCallStartDate: String? = null,

    /** codice dell'attività didattica dell'appello collegato */
    @SerialName("adRelCod")
    val activityRelationCode: String? = null,

    /** codice del corso di studio dell'appello collegato */
    @SerialName("cdsRelCod")
    val courseOfStudyRelationCode: String? = null,

    /** tipo di link appelli, identifica la tipologia di link; la relazione invece specifica anche il verso della relazione */
    @SerialName("tipoLinkAppCod")
    val callLinkTypeCode: String? = null,

    /** tipo di relazione tra l'appello e il relativo link. */
    @SerialName("tipoLinkRelAppCod")
    val relationCallLinkTypeCode: String? = null,

    /** chiave dell'appello collegato (app_id) */
    @SerialName("appRelId")
    val callRelationId: Long? = null,

    /** chiave dell'appello collegato (ad_id) */
    @SerialName("adRelId")
    val activityRelationId: Long? = null,

    /** chiave dell'appello collegato (cds_id) */
    @SerialName("cdsRelId")
    val courseOfStudyRelationId: Long? = null,

    /** Id del template voto dell'appello collegato */
    @SerialName("templvotoRelId")
    val voteTemplateRelationId: Long? = null,

    /** parte della chiave dell'appello da collegare, in alternativa è possibile specificare in templvotoId. Valori positivi indicano un inserimento/aggiornamento, valori negativi indicano la cancellazione dell'id con il medeismo valore assoluto */
    @SerialName("appId")
    val callId: Long? = null,

    /** parte della chiave dell'appello da collegare, in alternativa è possibile specificare in templvotoId. Valori positivi indicano un inserimento/aggiornamento, valori negativi indicano la cancellazione dell'id con il medeismo valore assoluto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** parte della chiave dell'appello da collegare, in alternativa è possibile specificare in templvotoId. Valori positivi indicano un inserimento/aggiornamento, valori negativi indicano la cancellazione dell'id con il medeismo valore assoluto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id del template voto dell'appello da collegare, in alternativa è possibile specificare la chiave dell'appello (cdsId,adId,appId). Valori positivi indicano un inserimento/aggiornamento, valori negativi indicano la cancellazione dell'id con il medeismo valore assoluto */
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

    /** modalità dell'esame definita nell'appello (valorizzata se il par_conf=CONTR_TIPO_ESA_APP=0), i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale Congiunto=SOC, Scritto e Orale Separato=SOS). */
    @SerialName("tipoEsaCod")
    val graduationTypeCode: String? = null,

    /** modalità di iscrizione definita nell'appello, i possibili valori sono ( Scritto=S, Orale=O, Scritto e Orale=SO). */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** codice del template turni per l'inserimento automatico di una lista di turni */
    @SerialName("templateTurnoCod")
    val shiftTemplateCode: String? = null,

    /** anno di calendario dell'appello, viene utilizzato per agganciare una eventuale definizione di esame comune */
    @SerialName("aaCalId")
    val academicYearCalendarId: Int? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Long? = null,

    /** descrizione libera dell'appello */
    @SerialName("desApp")
    val callDescription: String? = null,

    /** codice modalità di definizione dell'appello */
    @SerialName("tipoDefAppCod")
    val defaultCallTypeCode: String? = null,

    /** codice modalità di verbalizzazione dell'appello */
    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    /** codice modalità di prenotazione dell'appello */
    @SerialName("tipoGestPrenCod")
    val bookingManagementTypeCode: String? = null,

    /** data inizio iscrizioni (DD/MM/YYYY) */
    @SerialName("dataInizioIscr")
    val enrollmentStartDate: String? = null,

    /** data fine iscrizioni (DD/MM/YYYY) */
    @SerialName("dataFineIscr")
    val enrollmentEndDate: String? = null,

    /** data inizio appello (DD/MM/YYYY) */
    @SerialName("dataInizioApp")
    val callStartDate: String? = null,

    /** ora del turno minimo associato all' appello (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("oraEsa")
    val graduationTime: String? = null,

    /** appello riservato invisible agli studenti */
    @SerialName("riservatoFlg")
    val reservedFlag: Int? = null,

    /** tipo di scelta turno, i possibili valori sono i seguenti (0 - Calcolato dal sistema, viene associato il primo turno disponibile; 1 - Selezionabile dall'utente tra i turni compatibili liberi; 2 - Selezionato dall'utente prendendo un turno libero (anche non compatibile) */
    @SerialName("tipoSceltaTurno")
    val shiftChoiceType: Int? = null,

    /** id della condizione SQL associata all'appello */
    @SerialName("condId")
    val conditionId: Long? = null,

    /** Id del gruppo voto nel caso gli ordinamenti collegati ai cds dell'appello abbiano gruppi voto differenti (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("gruppoVotoId")
    val gradeGroupId: Long? = null,

    /** id della sede associata all'appello. Se valorizzata viene controllato durante la prenotazione che la sede coincida con quella dello studente nell'anno di sessione (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("sedeId")
    val siteId: Long? = null,

    /** id del grupop di tag associati all'appello. Se presente, viene richiesta la selezione del tag in fase di prenotazione (NB: il campo è relativo ad una gestione particolare utilizzata solo da alcuni atenei) */
    @SerialName("tagTemplId")
    val tagTemplateId: Long? = null,

    /** note associate all'appello */
    @SerialName("note")
    val notes: String? = null,

    /** note da inviare al sistema di logistica esterno */
    @SerialName("noteSistLog")
    val systemLogNotes: String? = null
)

@Serializable
data class Esse3ExamSessionCommissionTeacher(
    /** descrizione del ruolo del docente in commissione. Il Ruolo P (Presidente) può essere associato ad un solo docente */
    @SerialName("ruoloDes")
    val roleDescription: String? = null,

    /** cognome del docente */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** nome del docente */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** Id address book della persona in UGOV */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** codice del ruolo del docente in commissione. Il Ruolo P (Presidente) può essere associato ad un solo docente */
    @SerialName("ruoloCod")
    val roleCode: String? = null,

    /** id del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** id progressivo dell'appello rispetto alla coppia (cds_id,ad_id) */
    @SerialName("appId")
    val callId: Int? = null,

    /** id dell'attività didattica di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int? = null,

    /** id del corso di studio di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null
)

@Serializable
data class Esse3ExamSessionTeacherAuthorization(
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

    @SerialName("turni")
    val shifts: List<Esse3ShiftTeacherAuthorization> = emptyList(),

    /** Id del docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null
)

@Serializable
data class Esse3SharedExamAuthorization(
    /** id del corso di studio di definizione dell'esame comune, se appId valorizzato coincide con il cds di erogazione dell'appello */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** id dell'attività didattica di definizione dell'esame comune, se appId valorizzato coincide con l'attività di erogazione dell'appello */
    @SerialName("adId")
    val activityId: Int = 0,

    /** anno di riferimento dell'esame comune */
    @SerialName("aaOffId")
    val academicYearOfferId: Int = 0,

    /** id del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioId")
    val childCourseOfStudyId: Long = 0L,

    /** id dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioId")
    val childActivityId: Long = 0L,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioCod")
    val childCourseOfStudyCode: String? = null,

    /** codice del corso di studio figlio dell'esame comune */
    @SerialName("cdsFiglioDes")
    val childCourseOfStudyDescription: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioCod")
    val childActivityCode: String? = null,

    /** codice dell'attività didattica figlia dell'esame comune */
    @SerialName("adFiglioDes")
    val childActivityDescription: String? = null,

    /** id del docente */
    @SerialName("docenteId")
    val lecturerId: Long = 0L
)
