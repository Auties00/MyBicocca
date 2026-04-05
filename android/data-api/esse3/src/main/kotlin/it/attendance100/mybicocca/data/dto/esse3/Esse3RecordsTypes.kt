package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ImportBatch(
    /** nome del file batch */
    @SerialName("nomeFileBatch")
    val batchFileName: String? = null,

    /** numero del file batch da caricare sul campo batchNum, se non popolato viene inserito il batchId */
    @SerialName("batchNum")
    val batchNumber: Int? = null,

    /** indica se il batch è di tipo INS (non sono stati creati i verbali) oppure AGG (i verbali sono già presenti a sistema) In base al valore va popolato l'insieme corrispondente di verbali */
    @SerialName("tipo")
    val type: String = "",

    @SerialName("verbaliInserimento")
    val insertionMinutes: List<Esse3InsertionRecordImport> = emptyList(),

    @SerialName("verbaliAggiornamento")
    val updateMinutes: List<Esse3UpdateRecordImport> = emptyList()
)

@Serializable
data class Esse3BatchRecord(
    /** id del batch creato */
    @SerialName("batchId")
    val batchId: Long? = null,

    /** numero del batch assegnato in fase di importazione */
    @SerialName("batchNum")
    val batchNumber: Long? = null,

    /** descrizione libera */
    @SerialName("des")
    val description: String? = null,

    /** id dell'attivita' didattica associata al batch */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al batch */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al batch */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al batch */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al batch */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al batch */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** data di acquisizione del batch */
    @SerialName("dataAcq")
    val acquisitionDate: String? = null,

    /** data dell'appello */
    @SerialName("dataApp")
    val callDate: String? = null
)

@Serializable
data class Esse3BatchWithDetails(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** id dell'attivita' didattica associata al lotto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al lotto */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al lotto */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al lotto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al lotto */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al lotto */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice di verbalizzazione associato al lotto */
    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    /** stato del lotto */
    @SerialName("statoLotto")
    val batchState: String? = null,

    /** descrizione dello stato del lotto */
    @SerialName("statoLottoDes")
    val batchStateDescription: String? = null,

    /** data di verbalizzazione */
    @SerialName("dataApp")
    val callDate: String? = null,

    /** id del docente che ha generato il verbale */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** nome del docente che ha generato il verbale */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente che ha generato il verbale */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** codice fiscale del docente che ha generato il verbale */
    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    /** id della lingua nel caso di verbali che prevedono il livello di lingua */
    @SerialName("linguaId")
    val languageId: Long? = null,

    /** codice della lingua nel caso di verbali che prevedono il livello di lingua */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** motivo di rifirma del verbale, nel caso di lotti di correzione e/o revoca */
    @SerialName("motivoRifirma")
    val resigningReason: String? = null,

    /** progressivo di rifirma del verbale, indica il numero di rifirma rispetto all'originale. Nel caso di lotti di correzione e/o revoca */
    @SerialName("progRifirma")
    val resigningProgram: Int? = null,

    @SerialName("commissione")
    val committee: List<Esse3CommissionBatch> = emptyList(),

    @SerialName("transizioniStato")
    val stateTransitions: List<Esse3BatchTransactionStatus> = emptyList()
)

@Serializable
data class Esse3RecordRoot(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** Il del verbale all'interno del lotto */
    @SerialName("verbId")
    val minutesId: Long? = null,

    /** Il del batch di importazione */
    @SerialName("batchId")
    val batchId: Long? = null,

    /** id della carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id del tratto di carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("matId")
    val matId: Long? = null,

    /** id dell'appello collegato al verbale */
    @SerialName("appId")
    val callId: Long? = null,

    /** id del turno collegato al verbale */
    @SerialName("appLogId")
    val callLogId: Long? = null,

    /** numero del verbale */
    @SerialName("verbNum")
    val minutesNumber: String? = null,

    /** stato del verbale */
    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    /** tipo di errore impostato */
    @SerialName("errNum")
    val errorNumber: Int? = null,

    /** stato di warning del verbale (0=nessun warning, 1=warning impostato, 2=warning validato), Nel caso di 1 l'acquisizione del verbale in carriera è bloccata */
    @SerialName("statoWarn")
    val warningState: Int? = null,

    /** tipo di warning impostato */
    @SerialName("warnNum")
    val warningNumber: Int? = null,

    /** codice della tipologia di verbale (STD, REV, REV_ERR) */
    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    /** id del lotto collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    /** id del verbale collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** nome dello studente */
    @SerialName("nome")
    val name: String? = null,

    /** cogome dello studente */
    @SerialName("cognome")
    val surname: String? = null,

    /** id dell'attivita' didattica associata al lotto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al lotto */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al lotto */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al lotto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al lotto */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al lotto */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id dell'attivita' didattica associata all'attivita' dello studente */
    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    /** codice Attivita' didattica associata allo studente */
    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    /** descrizione Attivita' didattica associata allo studente */
    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    /** id del corso di studio associato all'attivita' dello studente */
    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    /** codice Corso di studio associato all'attività allo studente */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    /** descrizione Corso di studio associata all'attività allo studente */
    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    /** voto del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("voto")
    val grade: Int? = null,

    /** causale del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("causale")
    val reason: Int? = null,

    /** data del verbale */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** data dell'appello collegato al verbale */
    @SerialName("dataApp")
    val callDate: String? = null,

    /** livello di uscita della lingua */
    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    /** codice del tipo svolgimento esame */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    /** id dell'allegato con il quale recuperare il blob del verbale */
    @SerialName("imgId")
    val imageId: Long? = null
)

@Serializable
data class Esse3BatchRecordWithDetails(
    /** id del batch creato */
    @SerialName("batchId")
    val batchId: Long? = null,

    /** numero del batch assegnato in fase di importazione */
    @SerialName("batchNum")
    val batchNumber: Long? = null,

    /** descrizione libera */
    @SerialName("des")
    val description: String? = null,

    /** id dell'attivita' didattica associata al batch */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al batch */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al batch */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al batch */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al batch */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al batch */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** data di acquisizione del batch */
    @SerialName("dataAcq")
    val acquisitionDate: String? = null,

    /** data dell'appello */
    @SerialName("dataApp")
    val callDate: String? = null,

    @SerialName("verbali")
    val minutes: List<Esse3RecordWithUploadUrl> = emptyList()
)

@Serializable
data class Esse3AdditionalFieldRecordImport(
    /** nome del campo aggiuntivo */
    @SerialName("nome")
    val name: String = "",

    /** valore del campo aggiuntivo */
    @SerialName("valore")
    val value: String = ""
)

@Serializable
data class Esse3RecordWithDetails(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** Il del verbale all'interno del lotto */
    @SerialName("verbId")
    val minutesId: Long? = null,

    /** Il del batch di importazione */
    @SerialName("batchId")
    val batchId: Long? = null,

    /** id della carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id del tratto di carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("matId")
    val matId: Long? = null,

    /** id dell'appello collegato al verbale */
    @SerialName("appId")
    val callId: Long? = null,

    /** id del turno collegato al verbale */
    @SerialName("appLogId")
    val callLogId: Long? = null,

    /** numero del verbale */
    @SerialName("verbNum")
    val minutesNumber: String? = null,

    /** stato del verbale */
    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    /** tipo di errore impostato */
    @SerialName("errNum")
    val errorNumber: Int? = null,

    /** stato di warning del verbale (0=nessun warning, 1=warning impostato, 2=warning validato), Nel caso di 1 l'acquisizione del verbale in carriera è bloccata */
    @SerialName("statoWarn")
    val warningState: Int? = null,

    /** tipo di warning impostato */
    @SerialName("warnNum")
    val warningNumber: Int? = null,

    /** codice della tipologia di verbale (STD, REV, REV_ERR) */
    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    /** id del lotto collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    /** id del verbale collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** nome dello studente */
    @SerialName("nome")
    val name: String? = null,

    /** cogome dello studente */
    @SerialName("cognome")
    val surname: String? = null,

    /** id dell'attivita' didattica associata al lotto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al lotto */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al lotto */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al lotto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al lotto */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al lotto */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id dell'attivita' didattica associata all'attivita' dello studente */
    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    /** codice Attivita' didattica associata allo studente */
    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    /** descrizione Attivita' didattica associata allo studente */
    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    /** id del corso di studio associato all'attivita' dello studente */
    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    /** codice Corso di studio associato all'attività allo studente */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    /** descrizione Corso di studio associata all'attività allo studente */
    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    /** voto del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("voto")
    val grade: Int? = null,

    /** causale del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("causale")
    val reason: Int? = null,

    /** data del verbale */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** data dell'appello collegato al verbale */
    @SerialName("dataApp")
    val callDate: String? = null,

    /** livello di uscita della lingua */
    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    /** codice del tipo svolgimento esame */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    /** id dell'allegato con il quale recuperare il blob del verbale */
    @SerialName("imgId")
    val imageId: Long? = null,

    @SerialName("modifiche")
    val modifications: List<Esse3RecordModificationLog> = emptyList(),

    /** id della prentoazione a cui risulta associato il verbale */
    @SerialName("applistaId")
    val applicationListId: Long? = null,

    /** id della riga di libretto cui risulta associato il verbale, per i verbali non collegati a prenotazione è valorizzato solo per gli stati 3 e 4 */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** id della prova della riga di libretto cui risulta associato il verbale, , per i verbali non collegati a prenotazione è valorizzato solo per gli stati 3 e 4 */
    @SerialName("adregId")
    val activityRegulationId: Long? = null,

    /** domande d'esame */
    @SerialName("domandeEsame")
    val examApplications: String? = null,

    /** descrizione del tipo di errore impostato */
    @SerialName("errDes")
    val errorDescription: String? = null,

    /** descrizione del tipo di warning impostato */
    @SerialName("warnDes")
    val warningDescription: String? = null,

    /** descrizione della tipologia di verbale (STD=verbale standard, REV=verbale di revoca, REV_ERR=verbale di correzione per errore materiale) */
    @SerialName("tipoVerbDes")
    val minutesTypeDescription: String? = null,

    /** codice fiscale dello studente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** matricola del presidente della commissione del lotto */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** codice fiscale del presidente della commissione del lotto */
    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    /** nome del presidente della commissione del lotto */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cogome del presidente della commissione del lotto */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** CFU previsti per il verbale */
    @SerialName("cfu")
    val credits: Float? = null,

    /** decodifica della coppia (voto,causale) */
    @SerialName("esito")
    val outcome: String? = null
)

@Serializable
data class Esse3InsertionRecordImport(
    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String = "",

    /** data di registrazione del verbale */
    @SerialName("dataEsa")
    val graduationDate: String = "",

    /** data di registrazione del verbale */
    @SerialName("dataApp")
    val callDate: String = "",

    /** votazione del verbale, definisce insieme a causale tutti i possibili valori (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("voto")
    val grade: Int = 0,

    /** causale del verbale, definisce insieme a causale tutti i possibili valori (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("causale")
    val reason: Int? = null,

    /** richiede la generazione di un uploadId (fornito nella response) per poter effettuare il caricamento del blob con il nome file indicato */
    @SerialName("blobFileName")
    val blobFileName: String? = null,

    @SerialName("correzioni")
    val corrections: List<Esse3CorrectionRecordImport> = emptyList(),

    @SerialName("campiAggiuntivi")
    val additionalFields: List<Esse3AdditionalFieldRecordImport> = emptyList(),

    /** codice AD che lo studente deve riportare nel proprio libretto */
    @SerialName("adStuCod")
    val studentActivityCode: String = "",

    /** codice CDS dove è eroagtata l'AD che lo studente deve riportare nel proprio libretto */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String = ""
)

@Serializable
data class Esse3UpdateRecordImport(
    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String = "",

    /** data di registrazione del verbale */
    @SerialName("dataEsa")
    val graduationDate: String = "",

    /** data di registrazione del verbale */
    @SerialName("dataApp")
    val callDate: String = "",

    /** votazione del verbale, definisce insieme a causale tutti i possibili valori (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("voto")
    val grade: Int = 0,

    /** causale del verbale, definisce insieme a causale tutti i possibili valori (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("causale")
    val reason: Int? = null,

    /** richiede la generazione di un uploadId (fornito nella response) per poter effettuare il caricamento del blob con il nome file indicato */
    @SerialName("blobFileName")
    val blobFileName: String? = null,

    @SerialName("correzioni")
    val corrections: List<Esse3CorrectionRecordImport> = emptyList(),

    @SerialName("campiAggiuntivi")
    val additionalFields: List<Esse3AdditionalFieldRecordImport> = emptyList(),

    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long = 0L,

    /** Il del verbale all'interno del lotto */
    @SerialName("verbId")
    val minutesId: Long = 0L,

    /** numero del verbale */
    @SerialName("verbNum")
    val minutesNumber: String = "",

    /** cognome dello studente */
    @SerialName("cognome")
    val surname: String = "",

    /** nome dello studente */
    @SerialName("nome")
    val name: String = "",

    /** codice AD del lotto, il codice AD dello studente viene calcolato dal sistema */
    @SerialName("adCod")
    val activityCode: String = "",

    /** codice CDS del lotto, il codice CDS dell'AD dello studente viene calcolato dal sistema */
    @SerialName("cdsCod")
    val courseOfStudyCode: String = "",

    /** descrizione AD del lotto, la descrizione AD dello studente viene calcolato dal sistema */
    @SerialName("adDes")
    val activityDescription: String = "",

    /** descrizione CDS del lotto, la descrizione CDS dell'AD dello studente viene calcolato dal sistema */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String = ""
)

@Serializable
data class Esse3Batch(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** id dell'attivita' didattica associata al lotto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al lotto */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al lotto */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al lotto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al lotto */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al lotto */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice di verbalizzazione associato al lotto */
    @SerialName("tipoGestAppCod")
    val callManagementTypeCode: String? = null,

    /** stato del lotto */
    @SerialName("statoLotto")
    val batchState: String? = null,

    /** descrizione dello stato del lotto */
    @SerialName("statoLottoDes")
    val batchStateDescription: String? = null,

    /** data di verbalizzazione */
    @SerialName("dataApp")
    val callDate: String? = null,

    /** id del docente che ha generato il verbale */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** nome del docente che ha generato il verbale */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** cognome del docente che ha generato il verbale */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** codice fiscale del docente che ha generato il verbale */
    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    /** id della lingua nel caso di verbali che prevedono il livello di lingua */
    @SerialName("linguaId")
    val languageId: Long? = null,

    /** codice della lingua nel caso di verbali che prevedono il livello di lingua */
    @SerialName("linguaCod")
    val languageCode: String? = null,

    /** motivo di rifirma del verbale, nel caso di lotti di correzione e/o revoca */
    @SerialName("motivoRifirma")
    val resigningReason: String? = null,

    /** progressivo di rifirma del verbale, indica il numero di rifirma rispetto all'originale. Nel caso di lotti di correzione e/o revoca */
    @SerialName("progRifirma")
    val resigningProgram: Int? = null
)

@Serializable
data class Esse3CorrectionRecordImport(
    /** nome del campo modificato, controllare i valori sulla tabella TIPI_VERB_LOG_MOD */
    @SerialName("campo")
    val field: String = "",

    /** valore del campo prima della modifica */
    @SerialName("valoreVecchio")
    val oldValue: String = "",

    /** valore del campo dopèo della modifica */
    @SerialName("valoreNuovo")
    val newValue: String = "",

    /** data di modifica del verbale */
    @SerialName("dataModifica")
    val modificationDate: String = "",

    /** utente che ha effettuato la modifica */
    @SerialName("utente")
    val user: String = ""
)

@Serializable
data class Esse3CommissionBatch(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** Il del  docente */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** nome del docente */
    @SerialName("nome")
    val name: String? = null,

    /** cognome del docente */
    @SerialName("cognome")
    val surname: String? = null,

    /** codice fiscale del docente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** codice del ruolo del docente nella commissione */
    @SerialName("ruoloCod")
    val roleCode: String? = null,

    /** descrizione del ruolo del docente nella commissione */
    @SerialName("ruoloDes")
    val roleDescription: String? = null,

    /** progressivo di visualizzazione del docente all'interno della commissione */
    @SerialName("ordineVisNum")
    val orderVisibleNumber: Int? = null
)

@Serializable
data class Esse3BatchTransactionStatus(
    /** Il del  docente */
    @SerialName("lottoTransStatoId")
    val batchTransactionStateId: Long? = null,

    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** stato di partenza del lotto */
    @SerialName("statoLottoOld")
    val oldBatchState: String? = null,

    /** stato di arrivo del lotto */
    @SerialName("statoLottoNew")
    val newBatchState: String? = null,

    /** descrizione dello stato di partenza del lotto */
    @SerialName("statoLottoOldDes")
    val oldBatchStateDescription: String? = null,

    /** descrizione dello stato di arrivo del lotto */
    @SerialName("statoLottoNewDes")
    val newBatchStateDescription: String? = null,

    /** data di variazione dello stato */
    @SerialName("dataIns")
    val insertionDate: String? = null
)

@Serializable
data class Esse3ResultRecordImport(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** Il del verbale all'interno del lotto */
    @SerialName("verbId")
    val minutesId: Long? = null,

    /** Il del batch di importazione */
    @SerialName("batchId")
    val batchId: Long? = null,

    /** id della carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id del tratto di carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("matId")
    val matId: Long? = null,

    /** id dell'appello collegato al verbale */
    @SerialName("appId")
    val callId: Long? = null,

    /** id del turno collegato al verbale */
    @SerialName("appLogId")
    val callLogId: Long? = null,

    /** numero del verbale */
    @SerialName("verbNum")
    val minutesNumber: String? = null,

    /** stato del verbale */
    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    /** tipo di errore impostato */
    @SerialName("errNum")
    val errorNumber: Int? = null,

    /** stato di warning del verbale (0=nessun warning, 1=warning impostato, 2=warning validato), Nel caso di 1 l'acquisizione del verbale in carriera è bloccata */
    @SerialName("statoWarn")
    val warningState: Int? = null,

    /** tipo di warning impostato */
    @SerialName("warnNum")
    val warningNumber: Int? = null,

    /** codice della tipologia di verbale (STD, REV, REV_ERR) */
    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    /** id del lotto collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    /** id del verbale collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** nome dello studente */
    @SerialName("nome")
    val name: String? = null,

    /** cogome dello studente */
    @SerialName("cognome")
    val surname: String? = null,

    /** id dell'attivita' didattica associata al lotto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al lotto */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al lotto */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al lotto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al lotto */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al lotto */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id dell'attivita' didattica associata all'attivita' dello studente */
    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    /** codice Attivita' didattica associata allo studente */
    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    /** descrizione Attivita' didattica associata allo studente */
    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    /** id del corso di studio associato all'attivita' dello studente */
    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    /** codice Corso di studio associato all'attività allo studente */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    /** descrizione Corso di studio associata all'attività allo studente */
    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    /** voto del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("voto")
    val grade: Int? = null,

    /** causale del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("causale")
    val reason: Int? = null,

    /** data del verbale */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** data dell'appello collegato al verbale */
    @SerialName("dataApp")
    val callDate: String? = null,

    /** livello di uscita della lingua */
    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    /** codice del tipo svolgimento esame */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    /** id dell'allegato con il quale recuperare il blob del verbale */
    @SerialName("imgId")
    val imageId: Long? = null,

    /** indica se l'import è andato a buon fine */
    @SerialName("risultatoImport")
    val importResult: Boolean? = null,

    /** descrizione dell'errore incontrato in fase di import */
    @SerialName("risultatoImportErrMsg")
    val importResultErrorMessage: String? = null
)

@Serializable
data class Esse3RecordWithUploadUrl(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** Il del verbale all'interno del lotto */
    @SerialName("verbId")
    val minutesId: Long? = null,

    /** Il del batch di importazione */
    @SerialName("batchId")
    val batchId: Long? = null,

    /** id della carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id del tratto di carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("matId")
    val matId: Long? = null,

    /** id dell'appello collegato al verbale */
    @SerialName("appId")
    val callId: Long? = null,

    /** id del turno collegato al verbale */
    @SerialName("appLogId")
    val callLogId: Long? = null,

    /** numero del verbale */
    @SerialName("verbNum")
    val minutesNumber: String? = null,

    /** stato del verbale */
    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    /** tipo di errore impostato */
    @SerialName("errNum")
    val errorNumber: Int? = null,

    /** stato di warning del verbale (0=nessun warning, 1=warning impostato, 2=warning validato), Nel caso di 1 l'acquisizione del verbale in carriera è bloccata */
    @SerialName("statoWarn")
    val warningState: Int? = null,

    /** tipo di warning impostato */
    @SerialName("warnNum")
    val warningNumber: Int? = null,

    /** codice della tipologia di verbale (STD, REV, REV_ERR) */
    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    /** id del lotto collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    /** id del verbale collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** nome dello studente */
    @SerialName("nome")
    val name: String? = null,

    /** cogome dello studente */
    @SerialName("cognome")
    val surname: String? = null,

    /** id dell'attivita' didattica associata al lotto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al lotto */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al lotto */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al lotto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al lotto */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al lotto */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id dell'attivita' didattica associata all'attivita' dello studente */
    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    /** codice Attivita' didattica associata allo studente */
    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    /** descrizione Attivita' didattica associata allo studente */
    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    /** id del corso di studio associato all'attivita' dello studente */
    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    /** codice Corso di studio associato all'attività allo studente */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    /** descrizione Corso di studio associata all'attività allo studente */
    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    /** voto del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("voto")
    val grade: Int? = null,

    /** causale del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("causale")
    val reason: Int? = null,

    /** data del verbale */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** data dell'appello collegato al verbale */
    @SerialName("dataApp")
    val callDate: String? = null,

    /** livello di uscita della lingua */
    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    /** codice del tipo svolgimento esame */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    /** id dell'allegato con il quale recuperare il blob del verbale */
    @SerialName("imgId")
    val imageId: Long? = null,

    /** url sulla quale effettuare l'upload del blob del verbale */
    @SerialName("uploadUrl")
    val uploadUrl: String? = null
)

@Serializable
data class Esse3RecordModificationLog(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** Il del verbale all'interno del lotto */
    @SerialName("verbId")
    val minutesId: Long? = null,

    /** id del tipo di origine della modifica del verbale */
    @SerialName("verbOrigLogId")
    val originalLogMinutesId: Long? = null,

    /** descrizione del tipo di origine della modifica del verbale */
    @SerialName("verbOrigLogDes")
    val originalLogMinutesDescription: String? = null,

    /** nome del campo modificato */
    @SerialName("origineLogCod")
    val logOriginCode: String? = null,

    /** valore del campo prima della modifica */
    @SerialName("valoreVecchio")
    val oldValue: String? = null,

    /** valore del campo dopo della modifica */
    @SerialName("valoreNuovo")
    val newValue: String? = null,

    /** data di modifica del valore */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** userId dell'utente che ha modificato il valore */
    @SerialName("usrInsId")
    val insertionUserId: String? = null
)

@Serializable
data class Esse3RecordImportRoot(
    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String = "",

    /** data di registrazione del verbale */
    @SerialName("dataEsa")
    val graduationDate: String = "",

    /** data di registrazione del verbale */
    @SerialName("dataApp")
    val callDate: String = "",

    /** votazione del verbale, definisce insieme a causale tutti i possibili valori (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("voto")
    val grade: Int = 0,

    /** causale del verbale, definisce insieme a causale tutti i possibili valori (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("causale")
    val reason: Int? = null,

    /** richiede la generazione di un uploadId (fornito nella response) per poter effettuare il caricamento del blob con il nome file indicato */
    @SerialName("blobFileName")
    val blobFileName: String? = null,

    @SerialName("correzioni")
    val corrections: List<Esse3CorrectionRecordImport> = emptyList(),

    @SerialName("campiAggiuntivi")
    val additionalFields: List<Esse3AdditionalFieldRecordImport> = emptyList()
)

@Serializable
data class Esse3RecordsImportResponse(
    /** esito dell'operazione di import */
    @SerialName("esito")
    val outcome: Boolean? = null,

    @SerialName("batchCorretti")
    val correctBatches: List<Esse3BatchRecordWithDetails> = emptyList(),

    @SerialName("verbaliErrati")
    val erroneousMinutes: List<Esse3ResultRecordImport> = emptyList(),

    /** log delle elaborazioni effettuate */
    @SerialName("logElaborazione")
    val processingLog: List<String> = emptyList()
)

@Serializable
data class Esse3Record(
    /** Il del lotto dei verbali */
    @SerialName("lottoId")
    val lotBatchId: Long? = null,

    /** Il del verbale all'interno del lotto */
    @SerialName("verbId")
    val minutesId: Long? = null,

    /** Il del batch di importazione */
    @SerialName("batchId")
    val batchId: Long? = null,

    /** id della carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** id del tratto di carriera dello studente a cui risulta associato il verbale, per i verbali non collegati a prenotazione  potrebbe non essere valorizzato */
    @SerialName("matId")
    val matId: Long? = null,

    /** id dell'appello collegato al verbale */
    @SerialName("appId")
    val callId: Long? = null,

    /** id del turno collegato al verbale */
    @SerialName("appLogId")
    val callLogId: Long? = null,

    /** numero del verbale */
    @SerialName("verbNum")
    val minutesNumber: String? = null,

    /** stato del verbale */
    @SerialName("statoVerbale")
    val minutesState: Int? = null,

    /** tipo di errore impostato */
    @SerialName("errNum")
    val errorNumber: Int? = null,

    /** stato di warning del verbale (0=nessun warning, 1=warning impostato, 2=warning validato), Nel caso di 1 l'acquisizione del verbale in carriera è bloccata */
    @SerialName("statoWarn")
    val warningState: Int? = null,

    /** tipo di warning impostato */
    @SerialName("warnNum")
    val warningNumber: Int? = null,

    /** codice della tipologia di verbale (STD, REV, REV_ERR) */
    @SerialName("tipoVerbCod")
    val minutesTypeCode: String? = null,

    /** id del lotto collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("lottoCollId")
    val batchCollectionId: Long? = null,

    /** id del verbale collegato al verbale nel caso di tipologie REV e REV_ERR */
    @SerialName("verbCollId")
    val minutesCollectionId: Long? = null,

    /** matricola dello studente */
    @SerialName("matricola")
    val matricola: String? = null,

    /** nome dello studente */
    @SerialName("nome")
    val name: String? = null,

    /** cogome dello studente */
    @SerialName("cognome")
    val surname: String? = null,

    /** id dell'attivita' didattica associata al lotto */
    @SerialName("adId")
    val activityId: Long? = null,

    /** codice Attivita' didattica associata al lotto */
    @SerialName("adCod")
    val activityCode: String? = null,

    /** descrizione Attivita' didattica associata al lotto */
    @SerialName("adDes")
    val activityDescription: String? = null,

    /** id del corso di studio associato al lotto */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** codice Corso di studio associato al lotto */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione Corso di studio associato al lotto */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** id dell'attivita' didattica associata all'attivita' dello studente */
    @SerialName("adStuId")
    val studentActivityId: Long? = null,

    /** codice Attivita' didattica associata allo studente */
    @SerialName("adStuCod")
    val studentActivityCode: String? = null,

    /** descrizione Attivita' didattica associata allo studente */
    @SerialName("adStuDes")
    val studentActivityDescription: String? = null,

    /** id del corso di studio associato all'attivita' dello studente */
    @SerialName("cdsStuId")
    val courseOfStudyStudentId: Long? = null,

    /** codice Corso di studio associato all'attività allo studente */
    @SerialName("cdsStuCod")
    val courseOfStudyStudentCode: String? = null,

    /** descrizione Corso di studio associata all'attività allo studente */
    @SerialName("cdsStuDes")
    val courseOfStudyStudentDescription: String? = null,

    /** voto del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("voto")
    val grade: Int? = null,

    /** causale del verbale, insieme a causale rappresenta tutte i possibili esiti (voti, giudizi, causali di non verbalizzazione) */
    @SerialName("causale")
    val reason: Int? = null,

    /** data del verbale */
    @SerialName("dataEsa")
    val graduationDate: String? = null,

    /** data dell'appello collegato al verbale */
    @SerialName("dataApp")
    val callDate: String? = null,

    /** livello di uscita della lingua */
    @SerialName("livelloUscitaLinguaCod")
    val exitLanguageLevelCode: String? = null,

    /** codice del tipo svolgimento esame */
    @SerialName("tipoSvolgimentoEsameCod")
    val examTypeCode: String? = null,

    /** id dell'allegato con il quale recuperare il blob del verbale */
    @SerialName("imgId")
    val imageId: Long? = null,

    /** decodifica della coppia (voto,causale) */
    @SerialName("esito")
    val outcome: String? = null,

    /** CFU previsti per il verbale */
    @SerialName("cfu")
    val credits: Float? = null,

    /** cogome del presidente della commissione del lotto */
    @SerialName("docenteCognome")
    val lecturerSurname: String? = null,

    /** nome del presidente della commissione del lotto */
    @SerialName("docenteNome")
    val lecturerName: String? = null,

    /** codice fiscale del presidente della commissione del lotto */
    @SerialName("docenteCodFis")
    val lecturerFiscalCode: String? = null,

    /** matricola del presidente della commissione del lotto */
    @SerialName("docenteMatricola")
    val lecturerMatricola: String? = null,

    /** codice fiscale dello studente */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** descrizione della tipologia di verbale (STD=verbale standard, REV=verbale di revoca, REV_ERR=verbale di correzione per errore materiale) */
    @SerialName("tipoVerbDes")
    val minutesTypeDescription: String? = null,

    /** descrizione del tipo di warning impostato */
    @SerialName("warnDes")
    val warningDescription: String? = null,

    /** descrizione del tipo di errore impostato */
    @SerialName("errDes")
    val errorDescription: String? = null,

    /** domande d'esame */
    @SerialName("domandeEsame")
    val examApplications: String? = null,

    /** id della prova della riga di libretto cui risulta associato il verbale, , per i verbali non collegati a prenotazione è valorizzato solo per gli stati 3 e 4 */
    @SerialName("adregId")
    val activityRegulationId: Long? = null,

    /** id della riga di libretto cui risulta associato il verbale, per i verbali non collegati a prenotazione è valorizzato solo per gli stati 3 e 4 */
    @SerialName("adsceId")
    val activityChoiceId: Long? = null,

    /** id della prentoazione a cui risulta associato il verbale */
    @SerialName("applistaId")
    val applicationListId: Long? = null
)
