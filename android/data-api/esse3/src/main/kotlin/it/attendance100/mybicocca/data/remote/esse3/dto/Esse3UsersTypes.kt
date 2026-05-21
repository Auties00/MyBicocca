package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3SignatureImportData(
    @SerialName("datiFirma")
    val signatureData: List<Esse3SignatureData> = emptyList()
)

@Serializable
data class Esse3SignatureResponse(
    /** codice fiscale dell'utente */
    @SerialName("codFis")
    val fiscalCode: String = "",

    /** tipo di provider per la firma remota */
    @SerialName("tipoFirmaId")
    val signatureTypeId: Int? = null,

    /** prefisso di firma da associare alla firma remota */
    @SerialName("hsmPrefix")
    val hsmPrefix: String? = null,

    /** tipo di provider per la firma automatica */
    @SerialName("tipoFirmaFaId")
    val faSignatureTypeId: Int? = null,

    /** prefisso di firma da associare alla firma automatica */
    @SerialName("faPrefix")
    val faPrefix: String? = null,

    /** codice di ritorno sull'import della riga */
    @SerialName("retCod")
    val returnCode: Int? = null,

    @SerialName("errMsg")
    val errorMessage: String? = null
)

@Serializable
data class Esse3ExtendedCareerPortion(
    /** id univoco che consente di individuare la persona */
    @SerialName("persId")
    val personId: Long = 0L,

    /** id univoco che consente di individuare la carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** chiave del corso di studio di erogazione dell''attività didattica */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** anno di ordinamento del corso di iscrizione dello studente */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** chiave del percorso di studio di erogazione dell''attività didattica */
    @SerialName("pdsId")
    val studyPlanId: Long = 0L,

    /** id univoco che consente di individuare il tratto di carriera dello studente */
    @SerialName("matId")
    val matId: Long = 0L,

    /** matricola dello studente, è il numero assegnato dalle segreterie allo studente per identificarlo nel sistema, una matricola potrebbe non identificare univocamente un libretto collegato ad un tratto di carriera */
    @SerialName("matricola")
    val matricola: String = "",

    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String = "",

    /** id univoco che consente di individuare la lingua di navigazione */
    @SerialName("linguaId")
    val languageId: Long? = null,

    /** codice della lingua di navigazione */
    @SerialName("iso6392Cod")
    val iso6392Code: String? = null,

    /** codice dello stato dello studente */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** descrizione dello stato dello studente */
    @SerialName("staStuDes")
    val studentStatusDescription: String? = null,

    /** codice del motivo dello stato dello studente */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** descrizione del motivo dello stato dello studente */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** codice del tipo di corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoCorsoDes")
    val courseTypeDescription: String? = null,

    /** codice del tipo di corso di studio */
    @SerialName("tipoTititCod")
    val titleCategoryCode: String? = null,

    /** descrizione del tipo di corso di studio */
    @SerialName("tipoTititDes")
    val titleTypeDescription: String? = null,

    /** codice del corso di studio di iscrizione dello studente */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** descrizione del corso di iscrizione dello studente */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** codice dell'ordinamento di studio di iscrizione dello studente */
    @SerialName("ordCod")
    val orderCode: String? = null,

    /** descrizione dell'ordinamento di iscrizione dello studente */
    @SerialName("ordDes")
    val orderDescription: String? = null,

    /** Numero ciclo corso di studio */
    @SerialName("ordNumCiclo")
    val orderCycleNumber: Long? = null,

    /** codice del percorso di iscrizione dello studente */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** descrizione del percorso di iscrizione dello studente */
    @SerialName("pdsDes")
    val studyPlanDescription: String? = null,

    /** anno accademico di ingresso/immatricolazione in ateneo */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** anno accademico di inizio carriera */
    @SerialName("aaImm1")
    val academicYearImm1: Int? = null,

    /** anno accademico di prima immatricolazione al sistema universitario */
    @SerialName("aaImmSu")
    val academicYearImmigrationSu: Int? = null,

    /** data ingresso/immatricolazione in ateneo */
    @SerialName("dataImm")
    val matriculationDate: String? = null,

    /** data inizio carriera */
    @SerialName("dataImm1")
    val enrollmentDate1: String? = null,

    /** data prima immatricolazione nel sistema universitario */
    @SerialName("dataImmSu")
    val enrollmentDateOn: String? = null,

    /** data chiusura della carriera */
    @SerialName("dataChiusura")
    val closingDate: String? = null,

    /** anno di coorte dello studente */
    @SerialName("aaRegId")
    val academicYearRegulationId: Int? = null,

    /** codice istat dell ateneo */
    @SerialName("codiceLettore")
    val readerCode: String? = null,

    /** rappresentazione numerica del tipo titolo di studio */
    @SerialName("titoloStudio")
    val degreeTitle: Long? = null,

    /** sigla del tipo titolo (sd, PE, SP, SM, SF) */
    @SerialName("tipoLettore")
    val readerType: String? = null,

    /** autorizzazione ai dati personali (sar� sempre si altrimenti non sarebbe possibile conservare i dati della persona) */
    @SerialName("autDatiPersonali")
    val personalDataAuthorization: String? = null,

    /** rappresentazione numerica dello stato del pagamento delle tasse */
    @SerialName("statoTasse")
    val taxesState: Long? = null,

    /** anno accademico di ultima iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    /** data di ultima iscrizione */
    @SerialName("dataIscr")
    val enrollmentDate: String? = null,

    /** SSD. Valorizzato solo se il codice del tipo di corso è dottorato */
    @SerialName("ssd")
    val ssd: String? = null,

    /** SSD dell'area. Valorizzato solo se il codice del tipo di corso è dottorato */
    @SerialName("ssdArea")
    val ssdArea: String? = null,

    /** SDR scuola di dottorato. Valorizzato solo se il codice del tipo di corso è dottorato */
    @SerialName("sdrDott")
    val phdSite: String? = null,

    @SerialName("struttResponsabileCds")
    val courseOfStudyResponsibleStructure: Esse3CourseStructure? = null,

    @SerialName("tutor")
    val tutor: Esse3TutorData? = null,

    /** identificativo della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null,

    /** descrizione della tipologia di categoria amministrativa */
    @SerialName("tipoCatAmmDes")
    val administrativeCategoryTypeDescription: String? = null,

    /** Codice del settore associato alla prima iscrizione in ateneo */
    @SerialName("settCod")
    val sectorCode: String? = null,

    /** Descrizione del settore associato alla prima iscrizione in ateneo */
    @SerialName("settDes")
    val sectorDescription: String? = null,

    /** identificativo U-gov */
    @SerialName("idAb")
    val abbreviatedId: Int? = null,

    /** codice fiscale della personae */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Codice esterno carriera */
    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    /** Codice area scientifico/disciplinare */
    @SerialName("areaCod")
    val areaCode: String? = null,

    /** Descrizione area scientifico/disciplinare */
    @SerialName("areaDes")
    val areaDescription: String? = null
)

@Serializable
data class Esse3DigitalIdentities(
    /** id univoco che consente di individuare l'account utente */
    @SerialName("userId")
    val userId: String? = null,

    /** Identificativo credenziali SPID */
    @SerialName("spidCode")
    val spidCode: String? = null,

    /** Identificativo credenziali CIE */
    @SerialName("cieCode")
    val cieCode: String? = null
)

@Serializable
data class Esse3CourseStructure(
    /** codice struttura */
    @SerialName("cod")
    val code: String? = null,

    /** descrizione della struttura */
    @SerialName("des")
    val description: String? = null,

    /** codice csa della struttura */
    @SerialName("csaCod")
    val csaCode: String? = null
)

@Serializable
data class Esse3SignatureData(
    /** codice fiscale dell'utente */
    @SerialName("codFis")
    val fiscalCode: String = "",

    /** tipo di provider per la firma remota */
    @SerialName("tipoFirmaId")
    val signatureTypeId: Int? = null,

    /** prefisso di firma da associare alla firma remota */
    @SerialName("hsmPrefix")
    val hsmPrefix: String? = null,

    /** tipo di provider per la firma automatica */
    @SerialName("tipoFirmaFaId")
    val faSignatureTypeId: Int? = null,

    /** prefisso di firma da associare alla firma automatica */
    @SerialName("faPrefix")
    val faPrefix: String? = null
)

@Serializable
data class Esse3FunctionalUser(
    /** codice funzione */
    @SerialName("funName")
    val functionName: String? = null,

    /** id funzione */
    @SerialName("funId")
    val functionId: Long? = null,

    /** descrizione funzione */
    @SerialName("funDescr")
    val functionDescription: String? = null
)

@Serializable
data class Esse3UserAlias(
    /** alias dell'utente */
    @SerialName("alias")
    val alias: String? = null,

    /** data di scadenza dell'alias */
    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    /** tipologia dell'alias */
    @SerialName("tipologia")
    val typology: String? = null
)
