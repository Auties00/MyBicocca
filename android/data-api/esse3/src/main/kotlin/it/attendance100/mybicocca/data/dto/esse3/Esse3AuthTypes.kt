package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3ChangePasswordResult(
    /** risultato dell'operazione, può valere: * OK - operazione andata a buon fine * NO_PERMISSIONS - l'utenza tecnica non ha i diritti per la funzione SI_18_008 * EMPTY_PARAM - la password fornita è vuota * AUTH_FAILURE - la password passata nel parametro old_password non è corretta, oppure old_password non è fornito ed il parametro PWD_CHANGE_LOW_SECURITY non è popolato correttamente * INVALID_NEW_PASSWORD - la password non soddisfa i requisiti minimi configurati nel sistema * ERR_1118 - la password coincide con la password precedentemente inserita nel sistema * ERR_1003 - l'utente specificato non è presenete nel sistema */
    @SerialName("result")
    val result: String? = null
)

@Serializable
data class Esse3User(
    /** nome dell'utente */
    @SerialName("firstName")
    val firstName: String = "",

    /** cognome dell'utente */
    @SerialName("lastName")
    val lastName: String = "",

    /** sesso dell'utente */
    @SerialName("sex")
    val sex: String? = null,

    /** codice fiscale della persona */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** idAb relativo alle anagrafiche comuni di U-Gov */
    @SerialName("idAb")
    val abbreviatedId: Long? = null,

    /** identificativo della persona, viene valorizzato solo per determinati gruppi (vedi campo grpId) - 2 : gruppo didattica - 4 : gruppo studenti in ipotesi - 6 : gruppo studenti - 8 : gruppo preiscritti - 9 : gruppo registrati */
    @SerialName("persId")
    val personId: Long? = null,

    /** identificativo del docente, viene valorizzato solo per determinati gruppi (vedi campo grpId) - 7 : gruppo docenti */
    @SerialName("docenteId")
    val lecturerId: Long? = null,

    /** identificativo del soggetto esterno, viene valorizzato solo per determinati gruppi (vedi campo grpId) */
    @SerialName("soggEstId")
    val externalSubjectId: Long? = null,

    /** Id identificativo univoco dell'utente */
    @SerialName("id")
    val id: Long = 0L,

    /** id del gruppo utente, viene utilizzato per descriminare l'anagrafica da utilizzare */
    @SerialName("grpId")
    val groupId: Int = 0,

    /** indica se il gruppo dell' utente e' un gruppo di utenti tecnici */
    @SerialName("grpUserTecnicoFlg")
    val technicalUserGroupFlag: Int = 0,

    /** indica se il gruppo dell' utente e' un gruppo di personale tecnico amministrativo */
    @SerialName("grpPta")
    val ptaGroup: Int = 0,

    /** descrizione del gruppo dell'utente */
    @SerialName("grpDes")
    val groupDescription: String = "",

    /** timeout di sessione in minuti impostato per il gruppo utente */
    @SerialName("sessionTimeout")
    val sessionTimeout: Int? = null,

    /** userId con il quale l'utente ha effettuato il login */
    @SerialName("userId")
    val userId: String = "",

    /** id del tipo firma collegato all'utente */
    @SerialName("tipoFirmaId")
    val signatureTypeId: Int? = null,

    /** id del tipo firma autmatica collegato all'utente */
    @SerialName("tipoFirmaFaId")
    val faSignatureTypeId: Int? = null,

    /** nome alias dell'utente */
    @SerialName("aliasName")
    val aliasName: String? = null,

    @SerialName("trattiCarriera")
    val careerSegments: List<Esse3CareerSegmentKeys> = emptyList()
)

@Serializable
data class Esse3JWKModel(
    @SerialName("keys")
    val keys: List<Esse3JWKKey> = emptyList()
)

@Serializable
data class Esse3CareerSegmentKeys(
    /** id interno della carriera a cui appartiene il tratto di carriera dello studente */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** id interno del tratto di carriera dello studente */
    @SerialName("matId")
    val matId: Long = 0L,

    /** codice matricola associato al tratto di carriera dello studente */
    @SerialName("matricola")
    val matricola: String = "",

    /** id del corso di studio a cui fa riferimento il tratto di carriera */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** descrizione del corso di studio a cui fa riferimento il tratto di carriera */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String = "",

    /** codice dello stato della carriera a cui è associato il tratto. Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("staStuCod")
    val studentStatusCode: String = "",

    /** descrizione dello stato della carriera a cui è associato il tratto. Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("staStuDes")
    val studentStatusDescription: String = "",

    /** codice del motivo dello stato della carriera. Fornisce un dettaglio sulla motivazione dello stato della carriera Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** descrizione del motivo dello stato della carriera. Fornisce un dettaglio sulla motivazione dello stato della carriera Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("motStastuDes")
    val statusReasonDescription: String? = null,

    /** codice dello stato del tratto di carriera Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("staMatCod")
    val matStatusCode: String = "",

    /** descrizione dello stato del tratto di carriera Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("staMatDes")
    val matStatusDescription: String = "",

    @SerialName("dettaglioTratto")
    val segmentDetail: Esse3CareerPortion? = null
)

@Serializable
data class Esse3UserSession(
    @SerialName("user")
    val user: Esse3User,

    /** contiene il token di autenticazione (jsessionid) della sessione web */
    @SerialName("authToken")
    val authToken: String = "",

    /** token di autenticazione (sessionid) del backend di esse3 */
    @SerialName("internalAuthToken")
    val internalAuthToken: String? = null,

    /** indica se la password risulta scaduta */
    @SerialName("expPwd")
    val passwordExpiration: Boolean = false,

    @SerialName("credentials")
    val credentials: Esse3AuthenticationCredentials? = null,

    /** popolato solo se richiesto tramite l'apposito parametro, rappresenta un token JWT rilasciato da esse3' */
    @SerialName("jwt")
    val jwt: String? = null,

    @SerialName("profili")
    val profiles: List<Esse3UserProfile> = emptyList()
)

@Serializable
data class Esse3ChangeUserPasswordParameters(
    /** username dell'utente a cui cambiare la password */
    @SerialName("username")
    val username: String = "",

    /** vecchia password */
    @SerialName("oldPassword")
    val oldPassword: String? = null,

    /** nuova password */
    @SerialName("newPassword")
    val newPassword: String = ""
)

@Serializable
data class Esse3UserProfile(
    /** id interno del profilo utente */
    @SerialName("grpId")
    val groupId: Long? = null,

    /** descrizione profilo */
    @SerialName("des")
    val description: String? = null,

    /** userId dell'utente */
    @SerialName("userId")
    val userId: String? = null
)

@Serializable
data class Esse3JWKKey(
    /** key type */
    @SerialName("kty")
    val kty: String? = null,

    /** key ID */
    @SerialName("kid")
    val kid: String? = null,

    /** algoritm */
    @SerialName("alg")
    val algorithm: String? = null,

    @SerialName("n")
    val n: String? = null,

    @SerialName("e")
    val e: String? = null
)

@Serializable
data class Esse3SessionLanguage(
    /** codice ISO6392 per la definizione della lingua */
    @SerialName("linguaCod")
    val languageCode: String = ""
)

@Serializable
data class Esse3AuthenticationCredentials(
    /** tipo di autenticazione */
    @SerialName("kind")
    val kind: Esse3Kind,

    /** profilo utente */
    @SerialName("profile")
    val profile: Esse3Profile? = null,

    /** keyId definito nel token di autenticazione JWT */
    @SerialName("jwtKeyId")
    val jwtKeyId: String? = null,

    /** userId di autenticazione nel caso di BASIC */
    @SerialName("user")
    val user: String? = null
)

@Serializable
data class Esse3CareerPortion(
    /** codice del profilo assegnato allo studente */
    @SerialName("profCod")
    val professionCode: String? = null,

    /** codice del dipartimento dello studente */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** facId del tratto attivo dello studente */
    @SerialName("facId")
    val facultyId: Long? = null,

    /** id interno della carriera a cui appartiene il tratto di carriera dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** matId dello studente */
    @SerialName("matId")
    val matId: Long? = null,

    /** codice del corso dello studente */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** cdsId del tratto attivo dello studente */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** anno di ordinamento del tratto attivo dello studente */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int? = null,

    /** codice del percorso dello studente */
    @SerialName("pdsCod")
    val studyPlanCode: String? = null,

    /** pdsId del tratto attivo dello studente */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** id dell'iscrizione dello studente */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** codice dello stato della carriera a cui è associato il tratto. Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** codice del motivo dello stato della carriera. Fornisce un dettaglio sulla motivazione dello stato della carriera Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("motStastuCod")
    val statusReasonCode: String? = null,

    /** codice dello stato del tratto di carriera Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** codice del motivo dello stato della matricola. Fornisce un dettaglio sulla motivazione dello stato della matricola Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** codice dello stato dell'iscrizione Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("staIscrCod")
    val enrollmentStatusCode: String? = null,

    /** codice del motivo dello stato della iscrizione. Fornisce un dettaglio sulla motivazione dello stato della iscrizione Per il dettaglio dei codici possibili consultare la documentazione di ESSE3 */
    @SerialName("motStaiscrCod")
    val enrollmentStatusReasonCode: String? = null,

    /** anno di corso dello studente */
    @SerialName("annoCorso")
    val courseYear: Int? = null,

    /** numero di anni fuori corso dello studente */
    @SerialName("anniFC")
    val fcYears: Int? = null,

    /** anno di iscrizione dello studente */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Int? = null,

    /** durata di anni legale del corso dello studente */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** flag che indica se l'anno di iscrizione è l'ultimo anno di corso */
    @SerialName("ultimoAnnoFlg")
    val lastYearFlag: Int? = null,

    /** flag che indica se l'iscrizione è condizionata */
    @SerialName("condFlg")
    val conditionFlag: Int? = null,

    /** flag che indica se lo studente ha presentato domanda di iscrizone */
    @SerialName("domiscrFlg")
    val domicileEnrollmentFlag: Int? = null,

    /** codice del tipo di corso del corso dello studente */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** anno di coorte dello studente */
    @SerialName("aaRegId")
    val academicYearRegulationId: Int? = null,

    /** codice del tipo di specializzazione del corso dello studente */
    @SerialName("tipoSpecCod")
    val specializationTypeCode: String? = null,

    /** codice del tipo di iscrizione dello studente */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** indica se lo studente ha richiesto un passaggio */
    @SerialName("passaggioFlg")
    val transitionFlag: Int? = null,

    /** id della nota bloccante se presente */
    @SerialName("notaBloccanteFlg")
    val blockingNoteFlag: Int? = null,

    /** flag che indica se il profilo è attivo su web per gli studenti di mobilità */
    @SerialName("mobilFlg")
    val mobileFlag: Int? = null,

    /** indica se lo studente è part time */
    @SerialName("ptFlg")
    val ptFlag: Int? = null,

    /** id del tipo di normativa */
    @SerialName("normId")
    val normId: Long? = null,

    /** cid del tipo di categoria amministrativa */
    @SerialName("tipoCatAmmId")
    val administrativeCategoryTypeId: Long? = null
)

@Serializable
data class Esse3CheckLoginResult(
    /** indica se la verifica delle credenziali ha avuto successp */
    @SerialName("ok")
    val ok: Boolean? = null,

    /** indica se l'utente deve cambiare la password (ad esempio perché scaduta) */
    @SerialName("changePassword")
    val changePassword: Boolean? = null
)

@Serializable
data class Esse3CacheInfo(
    /** indica se ablitare (1)  o disabilitare (0) la cache http tramite gli header sulla response */
    @SerialName("httpCacheEnable")
    val httpCacheEnable: Int = 0,

    /** indica se ablitare (1)  o disabilitare (0) la cache lato server */
    @SerialName("serverCacheEnable")
    val serverCacheEnable: Int = 0
)

@Serializable
data class Esse3JWTModel(
    /** jwt */
    @SerialName("jwt")
    val jwt: String? = null
)
