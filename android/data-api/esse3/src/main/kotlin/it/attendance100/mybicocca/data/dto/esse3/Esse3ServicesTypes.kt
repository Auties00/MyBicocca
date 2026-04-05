package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3IdentityDocumentType(
    /** Codice tipo di documento di identità. */
    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String? = null,

    /** Descrizione tipo documento di identità */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3AddressType(
    /** Indica la tipologia di indirizzo riportata nell entità indirizzo: 1 Residenza 2 Domicilio */
    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    /** Descrizione tipologia di indirizzo */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3PaymentRefundType(
    /** Codice del tipo di rimborso e pagamento */
    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    /** Descrizione tipo documento di identità */
    @SerialName("des")
    val description: String? = null,

    /** 0 --> Rimborso e pagamento, 1 --> Rimborso, 2 --> Pagamento */
    @SerialName("rimbPag")
    val refundPayment: Int? = null,

    /** Indica se il record è di sistema */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Controlla la visibilità del tipo di pagamento in fase di emissione delle fatture */
    @SerialName("visEmissFlg")
    val issuanceVisibleFlag: Int? = null,

    /** Controlla la visibilità del tipo di pagamento in fase di acquisizione dei pagamenti manuali */
    @SerialName("visAcqPagFlg")
    val paymentAcquisitionVisibleFlag: Int? = null,

    /** Codifica di ateneo */
    @SerialName("tipoRimbPagCodAte")
    val atePaymentRefundTypeCode: String? = null,

    /** Indica se è prevista la stampa per il tipo di pagamento in oggetto */
    @SerialName("stampabileFlg")
    val printableFlag: Int? = null,

    /** Indica se è prevista la stampa da WEB per il tipo di pagamento in oggetto */
    @SerialName("stampabileDaWebFlg")
    val webPrintableFlag: Int? = null,

    /** Indica se il numero bollettino è modificabile dall'utente da inserimento pagamento manuale */
    @SerialName("numBollModifFlg")
    val modifiableBulletinNumberFlag: Int? = null,

    /** Indica se la fattura, di questa tipologia, è immediatamente da emettere (elaborare) in fase di creazione, senza attendere elaborazioni successive (file MAV, file attesi, ecc.) */
    @SerialName("elabEmissFlg")
    val processingIssuanceFlag: Int? = null,

    /** E' la base per la costruzione del numero identificativo relativo alle fatture di tipo MAV */
    @SerialName("baseNumeroLottoMav")
    val mavBatchNumberBase: String? = null,

    /** E' il progressivo massimo per la costruzione del numero identificativo relativo alle fatture di tipo MAV, in base al lotto messo a disposizione dalla banca */
    @SerialName("maxProgrLottoMav")
    val maxMavBatchProgress: Long? = null,

    /** Indica che il tipo di pagamento / rimborso è selezinabile dalla funzione web di inserimento dati bancari. */
    @SerialName("webFlg")
    val webFlag: Int? = null,

    /** Indica che il tipo di pagamento / rimborso abilita l`inserimento dei dettagli relativi al contro corrente bancario da inserimento dati bancari web. */
    @SerialName("abilInsDettBancaWeb")
    val webBankDetailsInsertionAuthorization: Int? = null,

    /** Codice modalità di pagamento da utilizzare nella replica dell'anagrafica verso UGov */
    @SerialName("ugovCdModPag")
    val uGovPaymentMethodCode: String? = null,

    /** Default da utilizzare sull'ELAB_FLG della P05_FATT all'atto dell'inserimento di un pagamento manuale: 0 = ELAB_FLG non viene modificato, 1 = ELAB_FLG viene alzato */
    @SerialName("defElabFlgPagMan")
    val manualPaymentProcessingDefinitionFlag: Int? = null,

    /** Indica che il tipo di pagamento / rimborso prevede l`inserimento dei dettagli relativi al contro corrente bancario. */
    @SerialName("abilInsDettBanca")
    val bankDetailsInsertionAuthorization: Int? = null,

    /** Indica la tipologia di numerazione fornita dalla Banca: N = Assente (lotto non configurato). A = Assoluta. La base del lotto viene utilizzata indipendentemente dall'anno di emissione del numero MAv. Y = Annuale. La base del lotto è riferita all'anno solare di emissione del numero MAv. */
    @SerialName("tipologiaLottoMav")
    val mavBatchTypology: String? = null,

    /** Indica se la modalità di pagamento è compatibile con l'utlizzo del Nodo dei pagamenti */
    @SerialName("pagNodoFlg")
    val paymentNodeFlag: Int? = null,

    /** Flag che indica se il pagamento sarà effettuato da enti esterni */
    @SerialName("pagEnteEstFlg")
    val externalEntityPaymentFlag: Int? = null,

    /** Flag che indica se il pagamento sarà effettuato con il bonus docenti */
    @SerialName("cartaDocFlg")
    val documentCardFlag: Int? = null,

    /** Flag che indica se il pagamento deve essere escluso dalla rendicontazione ENTRATEL */
    @SerialName("esclEntratelFlg")
    val excludeEntratelFlag: Int? = null,

    /** Codice servizio da utilizzare per il tipo pagamento */
    @SerialName("codServizio")
    val serviceCode: String? = null,

    /** Codice sottoservizio da utilizzare per il tipo pagamento */
    @SerialName("codSottoservizio")
    val subServiceCode: String? = null,

    /** Conto corrente ID di riferimento per le modalita' di pagmanento diverse da PA con gestione IUV abilitata */
    @SerialName("paCcId")
    val paCurrentAccountId: Long? = null,

    /** Indica se la modalità di pagamento è abilitata all'inserimento degli incassi da API rest */
    @SerialName("abilPagRest")
    val remainingPaymentAuthorization: Int? = null
)

@Serializable
data class Esse3CodeToIdTranslatorRequestObject(
    /** tipo di codice da tradurre */
    @SerialName("type")
    val type: String = "",

    /** valore del codice da tradurre */
    @SerialName("cod")
    val code: String = "",

    /** nel caso di chiavi composte nell'array sono presenti i codici che definiscono la chiave composta */
    @SerialName("cods")
    val codes: List<String> = emptyList()
)

@Serializable
data class Esse3ConfigurationParameter(
    /** codice del parametro di configurazione */
    @SerialName("parCod")
    val parameterCode: String = "",

    /** modulo del parametro di configurazione */
    @SerialName("modulo")
    val module: String? = null,

    /** prodotto del parametro di configurazione */
    @SerialName("prodotto")
    val product: String? = null,

    /** descrizione del parametro di configurazione */
    @SerialName("descrizione")
    val description: String? = null,

    /** nota del parametro di configurazione */
    @SerialName("nota")
    val note: String? = null,

    /** Valore numerico del parametro di configurazione */
    @SerialName("valNum")
    val numericValue: Long? = null,

    /** Valore numerico del parametro di configurazione */
    @SerialName("valAlfa")
    val alphanumericValue: String? = null
)

@Serializable
data class Esse3MaritalStatusType(
    /** Codice stato civile. */
    @SerialName("statoCivileCod")
    val maritalStatusCode: String? = null,

    /** Descrizione stato civile. */
    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3Users(
    /** Identificativo dell'utente */
    @SerialName("userId")
    val userId: String? = null,

    /** Alias */
    @SerialName("alias")
    val alias: String? = null,

    /** Nome del gruppo */
    @SerialName("grpName")
    val groupName: String? = null,

    /** Flag che indica se l'utenza è disabilitata o meno */
    @SerialName("disableFlg")
    val disableFlag: Int? = null,

    /** user name */
    @SerialName("userName")
    val userName: String? = null
)

@Serializable
data class Esse3AliasReturn(
    /** codice di ritorno */
    @SerialName("codiceRitorno")
    val returnCode: Int? = null
)

@Serializable
data class Esse3CodeToIdTranslatorAdditionalId(
    /** id aggiuntivo rispetto a quello principale (nel caso di chiave composta) */
    @SerialName("id")
    val id: Long? = null,

    /** nome del campo della chiave composta */
    @SerialName("name")
    val name: String? = null
)

@Serializable
data class Esse3CodeToIdTranslatorRequest(
    @SerialName("objs")
    val objects: List<Esse3CodeToIdTranslatorRequestObject> = emptyList()
)

@Serializable
data class Esse3Languages(
    /** identificativo della lingua */
    @SerialName("linguaId")
    val languageId: Long? = null,

    /** descrizione della lingua */
    @SerialName("des")
    val description: String? = null,

    /** flag per l'utilizzo della lingua per il Diploma Supplement */
    @SerialName("dsFlg")
    val dsFlag: Int? = null,

    /** indica se il record è di sistema oppure no */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** codice lingua ISO 639-2 */
    @SerialName("iso6392Codice")
    val iso6392Code: String? = null,

    /** indica se la lingua deve essere visualizzata nei bandi di Mobilità internazionale */
    @SerialName("mobilFlg")
    val mobileFlag: Int? = null,

    /** indica se la lingua è gestita dal modulo multilingua */
    @SerialName("webMlFlg")
    val webMlFlag: Int? = null,

    /** descrizione della lingua nei bandi di Mobilità internazionale */
    @SerialName("mobildes")
    val mobilityDescription: String? = null,

    /** indica che la lingua è selezionabile tra le lingue disponibili come madrelinguai */
    @SerialName("madrelinguaFlg")
    val motherTongueFlag: Int? = null,

    /** flag per l'utilizzo della lingua per la Stampa dei Certificati */
    @SerialName("certFlg")
    val certificateFlag: Int? = null,

    /** codice lingua ISO 639-1 */
    @SerialName("iso6391Codice")
    val iso6391Code: String? = null
)

@Serializable
data class Esse3UserGroup(
    /** id del gruppo */
    @SerialName("grpId")
    val groupId: Int? = null,

    /** flag che indica se il gruppo rappresenta degli utenti tecnici */
    @SerialName("utenteTecnicoFlg")
    val technicalUserFlag: Boolean? = null,

    /** nome del gruppo */
    @SerialName("name")
    val name: String? = null,

    /** tabella di anagrafica di esse3 collegata al gruppo */
    @SerialName("tabAna")
    val anaTab: String? = null,

    /** nome della colonna della chiave primaria della tabella di anagrafica */
    @SerialName("fieldAna")
    val anaField: String? = null,

    /** lunghezza di durata della sessione in minuti */
    @SerialName("httpSessionTimeout")
    val httpSessionTimeout: Int? = null,

    /** lunghezza della finestra per il calcolo del throtteling in secondi, se null vale il valore generale del par_conf */
    @SerialName("requestWindowLimit")
    val requestWindowLimit: Int? = null,

    /** numero massimo di richieste nella finestra, se null vale il valore generale del par_conf */
    @SerialName("maxRequestWindow")
    val maxRequestWindow: Int? = null
)

@Serializable
data class Esse3RecognitionType(
    /** Indica se la attività è stata acquisita direttamente o se è stata riconosciuta in seguito ad attività svolte dallo studente in una carriera pregressa o esternamente alla offerta didattica del corso. Codici predefiniti: NULL = Attività acquisita direttamente; T = Riconoscimento in seguito a trasferimento; P = Ric. per passaggio, opzione o abbreviazione di carriera; M = Ricon. per progetto di mobilità; S = Ricon. per tirocinio o stage */
    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    /** Descrizione tipo riconoscimento AD. */
    @SerialName("des")
    val description: String? = null,

    /** Indica se il record è di sistema oppure no. */
    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    /** Flag che individua una tipologia di riconoscimento per attività svolte all´Estero (non in Italia). */
    @SerialName("straFlg")
    val foreignFlag: Int? = null,

    /** Il flag deve essere valorizzato ad 1 in corrispondenza delle tipologie utilizzate per gestire riconoscimenti di attivita per abbreviazione di carriera. */
    @SerialName("abbrFlg")
    val abbreviationFlag: Int? = null,

    /** Il flag deve essere valorizzato ad 1 in corrispondenza delle tipologie utilizzate per gestire riconoscimenti di attivita al momento della immatricolazione dello studente per poterli distinguere da eventuali riconoscimenti effettuati in tempi successivi. */
    @SerialName("ingrFlg")
    val entryFlag: Int? = null,

    /** tipo riconoscimento AD ans */
    @SerialName("ansTipoRicCod")
    val answerResearchTypeCode: String? = null,

    /** note */
    @SerialName("nota")
    val note: String? = null,

    /** visualizza la nota nel cerificato */
    @SerialName("notaCertFlg")
    val certificateNoteFlag: Int? = null,

    /** bonus di laurea da applicare al riconoscimento */
    @SerialName("bonusLaurea")
    val graduationBonus: Double? = null,

    /** tipo riconoscimento AD ds */
    @SerialName("dsTipoRicCod")
    val dsResearchTypeCode: String? = null,

    /** tipo di riconoscimento utilizzato per riconoscere le attività interateneo sulla sede amministrativa. Utilizzato nei riconoscimenti automatici quando nella sede operativa viene superata una prova */
    @SerialName("interateFlg")
    val integratedFlag: Int? = null
)

@Serializable
data class Esse3VersionInfo(
    /** ip remoto da dove viene effettuata la connessione */
    @SerialName("remoteAddress")
    val remoteAddress: String? = null,

    /** encoding utilizzato */
    @SerialName("encoding")
    val encoding: String? = null,

    /** nome dell'università */
    @SerialName("univName")
    val universityName: String? = null,

    /** tipo di ambiente */
    @SerialName("ambienteType")
    val environmentType: Esse3EnvironmentType? = null,

    /** id univoco contentente i dettagli della build */
    @SerialName("buildId")
    val buildId: String? = null,

    /** tag univoco contentente i dettagli della build */
    @SerialName("buildTag")
    val buildTag: String? = null,

    /** versione di build di e3rest */
    @SerialName("buildVersion")
    val buildVersion: String? = null,

    /** ate_id dell'ateneo */
    @SerialName("ate")
    val ate: Int? = null,

    /** informazioni sul tipo di container */
    @SerialName("servletcontainerInfo")
    val servletContainerInfo: String? = null,

    /** data ora di deploy dell'app */
    @SerialName("webappStartTime")
    val webappStartTime: String? = null,

    /** data ora startup della jvm */
    @SerialName("jmvStartTime")
    val jmvStartTime: String? = null,

    /** contiene la versione di e3rest */
    @SerialName("versione")
    val version: String? = null
)

@Serializable
data class Esse3ProcessedLists(
    /** Codice testata elenco */
    @SerialName("codice")
    val code: String? = null,

    /** Descrizione testata elenco */
    @SerialName("des")
    val description: String? = null,

    /** anno accademico di generazione */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Codice tipologia elenco */
    @SerialName("tipoElencoCod")
    val listTypeCode: String? = null,

    /** Data di generazione dell'elenco */
    @SerialName("dataGeneraz")
    val generationDate: String? = null,

    /** Codice stato elenco */
    @SerialName("statoElenco")
    val listState: String? = null,

    /** Identificativo dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** Identificativo della persona */
    @SerialName("persId")
    val personId: String? = null,

    /** Indica se lo studente è eliminato dall'elenco */
    @SerialName("elimFlg")
    val deleteFlag: Int? = null,

    /** Indica, per lo studente, se è stato stampato un documento collegato all'elenco */
    @SerialName("stampaFlg")
    val printFlag: Int? = null
)

@Serializable
data class Esse3Alias(
    /** Identificativo dell'utente */
    @SerialName("userId")
    val userId: String? = null,

    /** l alias */
    @SerialName("alias")
    val alias: String? = null,

    /** Data di scadenza dell'alias */
    @SerialName("dataScadenza")
    val expirationDate: String? = null
)

@Serializable
data class Esse3ReferenceYear(
    /** codice del tipo data di riferimento */
    @SerialName("tipoDataRifCod")
    val referenceDateTypeCode: String = "",

    /** codice del tipo di corso di studio */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Anno accademico di output della funzione */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** data di riferimento */
    @SerialName("dataRif")
    val referenceDate: String? = null
)

@Serializable
data class Esse3CodeToIdTranslatorResponseObject(
    /** tipo di codice da tradurre */
    @SerialName("type")
    val type: String = "",

    /** valore del codice da tradurre */
    @SerialName("cod")
    val code: String = "",

    /** nel caso di chiavi composte nell'array sono presenti i codici che definiscono la chiave composta */
    @SerialName("cods")
    val codes: List<String> = emptyList(),

    /** id corrispondente al cod richiesto */
    @SerialName("id")
    val id: Long? = null,

    @SerialName("chiaveComposta")
    val compositeKey: List<Esse3CodeToIdTranslatorAdditionalId> = emptyList(),

    /** eventuale descrizione dell'errore se la traduzione non è possibile */
    @SerialName("err")
    val error: String? = null
)
