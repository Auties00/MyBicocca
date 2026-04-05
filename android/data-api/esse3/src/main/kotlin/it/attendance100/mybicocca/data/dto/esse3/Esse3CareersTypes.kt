package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3EnrollmentClasses(
    /** id univoco che consente di individuare la carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** anno accademico di ultima iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    /** Codice lingua ISO 639-1 */
    @SerialName("lingueIso6391Cod")
    val languagesIso6391Code: String? = null,

    /** Descrizione lingua */
    @SerialName("lingueDes")
    val languagesDescription: String? = null,

    /** Codice dominio di partizione degli studenti all´interno di un fattore di partizione */
    @SerialName("domPartCod")
    val domicilePartialCode: String? = null,

    /** Codice fattore di partizione degli studenti. */
    @SerialName("fatPartCod")
    val invoicePartialCode: String? = null,

    /** Descrizione dominio di partizione (Es. Iniziali cognome A-K, Matricole dispari, ecc.). */
    @SerialName("domPartDes")
    val domicilePartialDescription: String? = null,

    /** Periodo effettiva di erogazione della didattica. */
    @SerialName("partEffCod")
    val effectivePartialCode: String? = null,

    /** Descrizione della partizione AA */
    @SerialName("partEffDes")
    val effectivePartialDescription: String? = null,

    /** Codice tipologia della didattica. */
    @SerialName("tipoDidCod")
    val didacticTypeCode: String? = null,

    /** Descrizione tipologia didattica */
    @SerialName("tipoDidDes")
    val didacticTypeDescription: String? = null
)

@Serializable
data class Esse3CareerNotes(
    /** Identificativo studente. */
    @SerialName("stuId")
    val studentId: Int? = null,

    /** Identificativo nota. */
    @SerialName("notaId")
    val noteId: Int? = null,

    /** Data della nota. Il formato con cui deve essere definita la data DD/MM/YYYY */
    @SerialName("data")
    val date: String? = null,

    /** tipo di nota . */
    @SerialName("tipo")
    val type: String? = null,

    /** Utente di inserimento. */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** Data di inserimento. */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** Utente di ultima modifica. */
    @SerialName("usrModId")
    val modificationUserId: String? = null,

    /** Data di ultima modifica. */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** ID univoco del tipo blocco note studente. */
    @SerialName("tipoContrNotaId")
    val contractNoteTypeId: Int? = null,

    /** data fine valenza */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** data inizio valenza */
    @SerialName("dataIniVal")
    val evaluationStartDate: String? = null,

    /** ID template nota */
    @SerialName("templateNotaId")
    val noteTemplateId: Int? = null,

    /** Indica il default di visibilità della nota associata allo studente con l'iindicazione di questo tipo di controllo nei processi on-line. */
    @SerialName("webVisNotaFlg")
    val webNoteVisibleFlag: Int? = null,

    /** testo della nota . */
    @SerialName("testoNota")
    val noteText: String? = null,

    /** codice del tipo blocco note studente . */
    @SerialName("cod")
    val code: String? = null,

    /** descrizione  del codice tipo blocco . */
    @SerialName("des")
    val description: String? = null,

    /** Indica se il controllo assocaito alla nota deve essere bloccante. */
    @SerialName("blocoFlg")
    val blockFlag: Int? = null,

    /** Indica se il controllo si applica alle funzioni dell´area amministrativa */
    @SerialName("amminFlg")
    val administrativeFlag: Int? = null,

    /** Indica se il controllo si applica alle funzioni dell´area carriera */
    @SerialName("carrFlg")
    val careerFlag: Int? = null,

    /** Indica se il controllo si applica alla emissione dei certificati */
    @SerialName("certFlg")
    val certificateFlag: Int? = null,

    /** Indica se il controllo si applica alle funzioni dell´area tasse */
    @SerialName("taxFlg")
    val taxFlag: Int? = null,

    /** Indica se il controllo si applica alla login del web. */
    @SerialName("webFlg")
    val webFlag: Int? = null,

    /** Indica che l'inserimento o modifica di una nota di questa tipologia scatena la replica amministrativa. */
    @SerialName("abilReplicheFlg")
    val replicasAuthorizationFlag: Int? = null,

    /** Indica che il tipo di nota associato a questo controllo è abilitato alla attribuzione massiva automatica di note. */
    @SerialName("abilNoteMassFlg")
    val massiveNoteAuthorizationFlag: Int? = null
)

@Serializable
data class Esse3AttendanceDays(
    /** il corso di studio è a frequenza obbloigatoria. */
    @SerialName("cdsFreqObbl")
    val courseOfStudyMandatoryAttendance: Int? = null,

    /** Numero di giorni da recuperare. */
    @SerialName("nGiorniDaRec")
    val daysToRecoverNumber: Int? = null
)

@Serializable
data class Esse3CareerMinimalDataGDPR(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** cognome della persona */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome della persona */
    @SerialName("nome")
    val name: String? = null,

    /** nome alternativo della persona */
    @SerialName("nomeAlias")
    val aliasName: String? = null,

    /** data di nascita */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    /** sesso della persona */
    @SerialName("sesso")
    val gender: String? = null,

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** email personale dello studente */
    @SerialName("email")
    val email: String? = null,

    /** indirizzo email assegnato dall'ateneo allo studente */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** id univoco che consente di individuare la carriera */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** sigla che identifica lo stato della carriera */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** sigla che identifica il motivo dello stato della carriera */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** anno di immatricolazione */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** descrizione anno accademico */
    @SerialName("aaDes")
    val academicYearDescription: String? = null,

    /** data di immatricolazione */
    @SerialName("dataImm")
    val matriculationDate: String? = null,

    /** descrizione dello stato della carriera */
    @SerialName("statiStuDes")
    val studentStatesDescription: String? = null,

    /** descrizione del motivo della stato della carriera */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** numero protocollo */
    @SerialName("numProtocollo")
    val protocolNumber: String? = null,

    /** data di inserimento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** data di modifica (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataMod")
    val modificationDate: String? = null,

    /** Codice esterno carriera */
    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    /** anno iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** data iscrizione */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    /** matricola */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Indica lo stato della posizione della matricola. I valori di sistema sono:  A =  Attivo, S = Sospeso, I = Ipotesi */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** Causale dello stato della matricola. */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** Tipo di iscrizione all´anno di corso specificato: IC = In Corso, FC = Fuori Corso, RI = Ripetente */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Flag che indica se l´iscrizione è part-time (1) oppure full-time (0). */
    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    /** Flag che indica se nell´anno dell´iscrizione lo studente era sospeso e quindi se l´iscrizione era fittizia. */
    @SerialName("sospFlg")
    val suspensionFlag: Int? = null,

    /** Codice mnemonico del corso di studio */
    @SerialName("p06CdsCod")
    val p06CourseOfStudyCode: String? = null,

    /** Descrizione del corso di studio */
    @SerialName("p06CdsDes")
    val p06CourseOfStudyDescription: String? = null,

    /** id della sede */
    @SerialName("sedeId")
    val siteId: Int? = null,

    /** descrizione della sede */
    @SerialName("sediDes")
    val sitesDescription: String? = null,

    /** anno di corso */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** codice facoltà */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** descrizione facoltà */
    @SerialName("facDes")
    val facultyDescription: String? = null,

    /** codice csa della facoltà */
    @SerialName("facCsaCod")
    val facultyCsaCode: String? = null,

    /** identificativo corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Int? = null,

    /** identificativo della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** descrizione della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    /** Profilo studente */
    @SerialName("profstuCod")
    val studentProfessionCode: String? = null,

    /** descrizione profilo studente */
    @SerialName("profstuDes")
    val studentProfessionDescription: String? = null,

    /** tipologia corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** data di chiusura della carriera */
    @SerialName("dataChiusura")
    val closingDate: String? = null,

    /** id matricola */
    @SerialName("matId")
    val matId: Int? = null
)
