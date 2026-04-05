package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3InvoiceItem(
    /** data di scadenza della voce */
    @SerialName("dataScadenza")
    val expirationDate: String = "",

    /** Importo della voce. */
    @SerialName("importo")
    val amount: Double = 0.0,

    /** Rata a cui si referisce la voce. */
    @SerialName("rataId")
    val installmentId: Long = 0L,

    /** Codice della voce. */
    @SerialName("voceCod")
    val itemCode: String = "",

    /** nota aggiuntiva. */
    @SerialName("nota")
    val note: String? = null
)

@Serializable
data class Esse3TuitionCalculationResponse(
    /** Codice della procedura */
    @SerialName("procCod")
    val procedureCode: String? = null,

    /** Elenco delle tasse pre-calcolo */
    @SerialName("tassePre")
    val preTaxes: List<Esse3TuitionCalculationDetail> = emptyList(),

    /** Esito della ricalcolo tasse */
    @SerialName("esitoRicTax")
    val taxSearchOutcome: String? = null,

    /** Messaggio di errore della ricalcolo tasse */
    @SerialName("errRicTax")
    val taxSearchError: String? = null,

    /** Esito della generazione del bollettino */
    @SerialName("esitoGenBoll")
    val bulletinGenerationOutcome: String? = null,

    /** Messaggio di errore della generazione del bollettino */
    @SerialName("errGenBoll")
    val bulletinGenerationError: String? = null,

    /** Esiti della generazione delle fatture */
    @SerialName("esitoFatt")
    val invoiceOutcome: List<Esse3InvoiceOutcome> = emptyList(),

    /** Elenco delle tasse post-calcolo */
    @SerialName("tassePost")
    val postTaxes: List<Esse3TuitionCalculationDetail> = emptyList()
)

@Serializable
data class Esse3InvoiceOutcome(
    /** Identificativo fattura */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** Esito dell'emissione della fattura */
    @SerialName("esito")
    val outcome: String? = null,

    /** Dettaglio errore emissione fattura */
    @SerialName("errFatt")
    val invoiceError: String? = null
)

@Serializable
data class Esse3Payments(
    /** Nome della persona debitrice. */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome della persona debitrice. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale della persona debitrice. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Anno accademico dell'addebito. */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Data di scadenza della fattura. */
    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    /** Importo della fattura. */
    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    /** Identificativo della fattura. */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav1")
    val mav1Description: String? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav2")
    val mav2Description: String? = null,

    /** Identificativo interno del pagamento. */
    @SerialName("pagId")
    val paymentId: Long? = null,

    /** Importo pagato. */
    @SerialName("importoPag")
    val paidAmount: Double? = null,

    /** Data di pagamento. */
    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    /** Data di notifica. */
    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    /** Data di accredito. */
    @SerialName("dataAccredito")
    val creditDate: String? = null,

    /** Modalità di pagamento associata all'incasso. */
    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    /** iuv della fattura pagabile su Nodo Pagamenti. */
    @SerialName("iuv")
    val iuv: String? = null,

    /** codice avviso della fattura pagabile su Nodo Pagamenti. */
    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    /** iur della fattura pagabile su Nodo Pagamenti. */
    @SerialName("iur")
    val iur: String? = null,

    /** Numero bollettino di pagamento. */
    @SerialName("nBollettino")
    val bulletinNumber: String? = null,

    /** Rendiconto di caricamento incassi. */
    @SerialName("rendicontoId")
    val reportId: Long? = null,

    /** Flag che indica se il pagamento è stato acquisito manualmente. */
    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    /** Flag che indica che non bisogna addebitare more sulla fattura anche se pagato in ritardo.. */
    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    /** Data di valorizzazione del flag di non addebito mora. */
    @SerialName("noAddebMoreData")
    val noMoreChargeData: String? = null,

    /** Nota legata all'inibizione della mora. */
    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    /** Utente di inibizione. */
    @SerialName("noAddebMoreUsrId")
    val noMoreChargeUserId: String? = null,

    /** Identificativo della fattura con cui la mora è stata emessa. */
    @SerialName("fattMoraId")
    val lateFeeInvoiceId: Long? = null,

    /** Flag che indica la presenza di mora addebitabile. */
    @SerialName("moraAddFlg")
    val lateFeeAdditionFlag: Int? = null,

    /** Numero di more addebitate. */
    @SerialName("moraCount")
    val lateFeeCount: Long? = null
)

@Serializable
data class Esse3Refunds(
    /** Nome della persona debitrice. */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome della persona debitrice. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale della persona debitrice. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Anno accademico dell'addebito. */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Importo della fattura. */
    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    /** Data scadenza fattura. */
    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    /** Codice di elaborazione da associare al rimborso. */
    @SerialName("codElabRimb")
    val refundProcessingCode: String? = null,

    /** Numero di mandato alla banca per il pagamento del rimborso. */
    @SerialName("numMandatoRimb")
    val refundMandateNumber: String? = null,

    /** Causale di rimborso. */
    @SerialName("cauRimbCod")
    val refundReasonCode: String? = null,

    /** Identificativo della fattura. */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** Data emissione fattura. */
    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    /** Data elaborazione fattura. */
    @SerialName("dataElab")
    val processingDate: String? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav1")
    val mav1Description: String? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav2")
    val mav2Description: String? = null,

    /** Per i bollettini correttivi derivanti da errato pagamento, indica il bollettino originale al quale la correzione fa riferimento. */
    @SerialName("fattErrataId")
    val erroneousInvoiceId: Long? = null,

    /** Flag di annullamento della fattura. Se uguale a 0 la fattura risulta valida, in caso contrario risulta annullata. */
    @SerialName("fattAnnullata")
    val canceledInvoice: Long? = null,

    /** Identificativo interno del pagamento. */
    @SerialName("pagId")
    val paymentId: Long? = null,

    /** Importo pagato. */
    @SerialName("importoPag")
    val paidAmount: Double? = null,

    /** Data di pagamento. */
    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    /** Data di notifica. */
    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    /** Data di accredito. */
    @SerialName("dataAccredito")
    val creditDate: String? = null,

    /** Modalità di pagamento associata all'incasso. */
    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    /** Flag che indica se il pagamento è stato acquisito manualmente. */
    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    /** Eventuale nota legata al rimborso. */
    @SerialName("notaRimb")
    val refundNote: String? = null,

    /** Flag che indica che è stato fatto il rimborso o meno. */
    @SerialName("rimborsatoFlg")
    val refundedFlag: Int? = null
)

@Serializable
data class Esse3ExemptionData(
    /** Anno accademico. */
    @SerialName("annoAccademico")
    val academicYear: Long = 0L,

    /** Codice fiscale dello studente. */
    @SerialName("codFis")
    val fiscalCode: String = "",

    /** Matricola dello studente. */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Identificativo dello studente. */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** Codice del corso di studio. */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** Codice della facoltà. */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** esonero da inserire. */
    @SerialName("esonero")
    val exemption: String = "",

    /** causale esonero. */
    @SerialName("causaleEsonero")
    val exemptionReason: String? = null
)

@Serializable
data class Esse3ExemptionResponse(
    @SerialName("datiEsonero")
    val exemptionData: Esse3ExemptionData? = null,

    @SerialName("errori")
    val errors: List<Esse3ExemptionDataError> = emptyList()
)

@Serializable
data class Esse3MultibenefitPayment(
    /** Nome della persona debitrice. */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome della persona debitrice. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** ragione sociale. */
    @SerialName("ragioneSociale")
    val companyName: String? = null,

    /** Indirizzo. */
    @SerialName("indirizzo")
    val address: String? = null,

    /** Civico. */
    @SerialName("civico")
    val streetNumber: String? = null,

    /** Cap. */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Localita. */
    @SerialName("localita")
    val locality: String? = null,

    /** provincia. */
    @SerialName("provincia")
    val province: String? = null,

    /** Nazione. */
    @SerialName("nazione")
    val nation: String? = null,

    /** Anno del debito. */
    @SerialName("aaDebito")
    val academicYearDebt: Long? = null,

    /** Codice dominio dell'ente principale. */
    @SerialName("codDominioPrincipale")
    val mainDomainCode: String? = null,

    /** Codice applicazione. */
    @SerialName("codApplicazione")
    val applicationCode: String? = null,

    /** iuv della fattura pagata sul Nodo Pagamenti. */
    @SerialName("iuv")
    val iuv: String? = null,

    /** codice avviso della fattura pagata sul Nodo Pagamenti. */
    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    /** Codice versamento ente. */
    @SerialName("codiceVersamentoEnte")
    val entityPaymentCode: Long? = null,

    /** Importo del versamento. */
    @SerialName("importoVersamento")
    val paymentAmount: Double? = null,

    /** Causale versamento. */
    @SerialName("causaleVersamento")
    val paymentReason: String? = null,

    /** Codice unità operativa. */
    @SerialName("codUoVersamento")
    val paymentUnitCode: String? = null,

    /** Importo del singolo versamento. */
    @SerialName("importoSingoloVersamento")
    val singlePaymentAmount: Double? = null,

    /** Causale singolo versamento. */
    @SerialName("causaleSingoloVersamento")
    val singlePaymentReason: String? = null,

    /** Codice della tassonomia. */
    @SerialName("codTassonomia")
    val taxonomyCode: String? = null,

    /** Descrizione della tassonomia. */
    @SerialName("desTassonomia")
    val taxonomyDescription: String? = null,

    /** Codice unità operativa del singolo versamento. */
    @SerialName("codUoSingoloVersamento")
    val singlePaymentUnitCode: String? = null,

    /** Data di pagamento. */
    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    /** iur della fattura pagata sul Nodo Pagamenti. */
    @SerialName("iur")
    val iur: String? = null,

    /** Codice dominio del singolo versamento. */
    @SerialName("codDominioSingoloVersamento")
    val singlePaymentDomainCode: String? = null,

    /** Iban di accredito del pagamento. */
    @SerialName("ibanAccredito")
    val creditIban: String? = null,

    /** Iban di appoggio del pagamento. */
    @SerialName("ibanAppoggio")
    val supportIban: String? = null,

    /** Codice del flusso di rendicontazione. */
    @SerialName("codFlussoRendicontazione")
    val reportingFlowCode: String? = null
)

@Serializable
data class Esse3InvoiceInsert(
    /** anno accademico a cui si referisce la fattura. */
    @SerialName("aaId")
    val academicYearId: Int = 0,

    /** Id dello studente a cui si referisce la fattura. */
    @SerialName("stuId")
    val studentId: Long = 0L,

    /** Indica se escludere lo studente dal ricalcolo tasse. */
    @SerialName("escludiDaRicalcolo")
    val excludeFromRecalculation: Boolean? = null,

    /** nota sulla escusione dal ricalcolo tasse. */
    @SerialName("notaEsclusioneRicalcolo")
    val exclusionFromRecalculationNote: String? = null,

    @SerialName("tasse")
    val taxes: List<Esse3TuitionFee> = emptyList()
)

@Serializable
data class Esse3TrafficLight(
    /** E' il semaforo tasse che riporta i valori ROSSO quando esistono addebiti studente di tasse bloccanti scaduti oltre i gg di tolleranza, GIALLO se esistono more addebitabili, VERDE se lo studente è in regola con le tasse. */
    @SerialName("semaforo")
    val trafficLight: String? = null,

    /** Rappresenta l'importo che deve essere ancora pagato per essere in regola con le tasse (semaforo VERDE). */
    @SerialName("importoDovuto")
    val dueAmount: Double? = null,

    @SerialName("tasseScadute")
    val expiredTaxes: List<Esse3TuitionFees> = emptyList(),

    @SerialName("tasseDovute")
    val dueTaxes: List<Esse3TuitionFees> = emptyList()
)

@Serializable
data class Esse3TuitionFees(
    /** identificativo della fattura */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** identificativo della tassa */
    @SerialName("tassaId")
    val taxId: Long? = null,

    /** codice della tassa */
    @SerialName("tassaCod")
    val taxCode: String? = null,

    /** descrizione della tassa */
    @SerialName("tassaDes")
    val taxDescription: String? = null,

    /** identificativo della voce */
    @SerialName("voceId")
    val itemId: Long? = null,

    /** codice della voce */
    @SerialName("voceCod")
    val itemCode: String? = null,

    /** descrizione della voce */
    @SerialName("voceDes")
    val itemDescription: String? = null,

    /** importo della voce */
    @SerialName("importoVoce")
    val itemAmount: Double? = null,

    /** data di scadenza */
    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    /** la data massima di tolleranza per il pagamento */
    @SerialName("dataPagTollerataMax")
    val maximumToleratedPaymentDate: String? = null
)

@Serializable
data class Esse3TuitionFee(
    /** Codice della tassa da adebbitare */
    @SerialName("tassaCod")
    val taxCode: String = "",

    @SerialName("voci")
    val items: List<Esse3InvoiceItem> = emptyList()
)

@Serializable
data class Esse3SelfCertificationComponents(
    /** Cognome del componente del nucleo familiare. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome del componente del nucleo familiare. */
    @SerialName("nome")
    val name: String? = null,

    /** Codice fiscale del componente del nucleo familiare a cui si riferiscono i redditi dichiarati. */
    @SerialName("codFiscale")
    val fiscalCode: String? = null,

    /** Padre, Madre, Fratello, Sorella, Moglie, Marito, Nessuna parentela. */
    @SerialName("parentela")
    val relationship: String? = null,

    @SerialName("redditi")
    val incomes: List<Esse3SelfCertificationIncome> = emptyList()
)

@Serializable
data class Esse3CancelInvoiceResponse(
    /** Codice di ritorno dell'annullamento. */
    @SerialName("esitoAnnullamento")
    val cancellationOutcome: Long? = null,

    /** Messaggio che contiene una descrizione dell'esito. */
    @SerialName("returnMessage")
    val returnMessage: String? = null
)

@Serializable
data class Esse3StudentDebit(
    /** Flag che indica che è stato fatto il rimborso o meno. */
    @SerialName("rimborsatoFlg")
    val refundedFlag: Int? = null,

    /** Eventuale nota legata al rimborso. */
    @SerialName("notaRimb")
    val refundNote: String? = null,

    /** Numero di more addebitate. */
    @SerialName("moraCount")
    val lateFeeCount: Long? = null,

    /** Flag che indica la presenza di mora addebitabile. */
    @SerialName("moraAddFlg")
    val lateFeeAdditionFlag: Int? = null,

    /** Identificativo della fattura con cui la mora è stata emessa. */
    @SerialName("fattMoraId")
    val lateFeeInvoiceId: Long? = null,

    /** Utente di inibizione. */
    @SerialName("noAddebMoreUsrId")
    val noMoreChargeUserId: String? = null,

    /** Nota legata all'inibizione della mora. */
    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    /** Data di valorizzazione del flag di non addebito mora. */
    @SerialName("noAddebMoreData")
    val noMoreChargeData: String? = null,

    /** Flag che indica che non bisogna addebitare more sulla fattura anche se pagato in ritardo.. */
    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    /** Flag che indica se il pagamento è stato acquisito manualmente. */
    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    /** Rendiconto di caricamento incassi. */
    @SerialName("rendicontoId")
    val reportId: Long? = null,

    /** Numero bollettino di pagamento. */
    @SerialName("nBollettino")
    val bulletinNumber: String? = null,

    /** iur della fattura pagabile su Nodo Pagamenti. */
    @SerialName("iur")
    val iur: String? = null,

    /** codice avviso della fattura pagabile su Nodo Pagamenti. */
    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    /** iuv della fattura pagabile su Nodo Pagamenti. */
    @SerialName("iuv")
    val iuv: String? = null,

    /** Modalità di pagamento associata all'incasso. */
    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    /** Data di accredito. */
    @SerialName("dataAccredito")
    val creditDate: String? = null,

    /** Data di notifica. */
    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    /** Data di pagamento. */
    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    /** Flag che indica la presenza di un pagamento. */
    @SerialName("pagatoFlg")
    val paidFlag: Int? = null,

    /** Importo pagato. */
    @SerialName("importoPag")
    val paidAmount: Double? = null,

    /** Identificativo interno del pagamento. */
    @SerialName("pagId")
    val paymentId: Long? = null,

    /** Flag di annullamento della fattura. Se uguale a 0 la fattura risulta valida, in caso contrario risulta annullata. */
    @SerialName("fattAnnullata")
    val canceledInvoice: Long? = null,

    /** Per i bollettini correttivi derivanti da errato pagamento, indica il bollettino originale al quale la correzione fa riferimento. */
    @SerialName("fattErrataId")
    val erroneousInvoiceId: Long? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav2")
    val mav2Description: String? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav1")
    val mav1Description: String? = null,

    /** Data elaborazione fattura. */
    @SerialName("dataElab")
    val processingDate: String? = null,

    /** Data emissione fattura. */
    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    /** Identificativo della fattura. */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** Importo della fattura. */
    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    /** Flag che indica che la fattura è scaduta. */
    @SerialName("fattScadutaFlg")
    val expiredInvoiceFlag: Int? = null,

    /** Data di scadenza della fattura. */
    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    /** Codice associato alla fattura. */
    @SerialName("fattCod")
    val invoiceCode: String? = null,

    /** Causale di rimborso. */
    @SerialName("cauRimbCod")
    val refundReasonCode: String? = null,

    /** Numero di mandato alla banca per il pagamento del rimborso. */
    @SerialName("numMandatoRimb")
    val refundMandateNumber: String? = null,

    /** Codice di elaborazione da associare al rimborso. */
    @SerialName("codElabRimb")
    val refundProcessingCode: String? = null,

    /** Flag di annullamento della voce. Se 0 la voce non è annullata, se 1 voce cancellata logicamente. */
    @SerialName("annullataFlg")
    val canceledFlag: Int? = null,

    /** Nota di calcolo dell'addebito. Riporta le combinazioni applicate e le descrizioni delle regole di variazione importi. */
    @SerialName("notaCalcolo")
    val calculationNote: String? = null,

    /** Note legate all'addebito. */
    @SerialName("note")
    val notes: String? = null,

    /** Tipologia di pagamento/rimborso selezionata nella fattura. */
    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    /** Flag che indica la scadenza dell'addebito. */
    @SerialName("scadutoFlg")
    val expiredFlag: Int? = null,

    /** Data scadenza addebito. */
    @SerialName("scadenzaAddebito")
    val chargeExpiration: String? = null,

    /** Importo della voce. */
    @SerialName("importoVoce")
    val itemAmount: Double? = null,

    /** Descrizione rata. */
    @SerialName("rataDes")
    val installmentDescription: String? = null,

    /** Identificativo rata. */
    @SerialName("rataId")
    val installmentId: Long? = null,

    /** Descrizione voce. */
    @SerialName("voceDes")
    val itemDescription: String? = null,

    /** Codice della voce. */
    @SerialName("voceCod")
    val itemCode: String? = null,

    /** Identificativo della voce. */
    @SerialName("voceId")
    val itemId: Long? = null,

    /** Tipologia di voce. */
    @SerialName("tipoVoceCod")
    val itemTypeCode: String? = null,

    /** Descrizione della combinazione. */
    @SerialName("combDes")
    val combinationDescription: String? = null,

    /** Codice della combinazione. */
    @SerialName("combCod")
    val combinationCode: String? = null,

    /** Identificativo della combinazione. */
    @SerialName("combId")
    val combinationId: Long? = null,

    /** Descrizione della tassa. */
    @SerialName("tassaDes")
    val taxDescription: String? = null,

    /** Codice della tassa. */
    @SerialName("tassaCod")
    val taxCode: String? = null,

    /** Tipologia di tassa. */
    @SerialName("tipoTaxCod")
    val taxTypeCode: String? = null,

    /** Identificativo della tassa. */
    @SerialName("tassaId")
    val taxId: Long? = null,

    /** Identificativo della persona. */
    @SerialName("persId")
    val personId: Long? = null,

    /** Anno accademico dell'addebito. */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Codice fiscale della persona debitrice. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Cognome della persona debitrice. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome della persona debitrice. */
    @SerialName("nome")
    val name: String? = null,

    /** Indica la tipologia di addebito. Può assumere i valori STU per indicare addebito tasse studente, PER per indicare addebiti tasse persona/esami di stato, AMM per indicare tasse legate a concorsi di ammissione. */
    @SerialName("tipoAd")
    val teachingActivityType: String? = null,

    /** Flag che indica se la fattura dev'essere visibile da interfaccia web */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** Data di caricamento del versamento. */
    @SerialName("paDtVersamento")
    val paPaymentDate: String? = null,

    /** Email di ateneo dello studente. */
    @SerialName("emailAte")
    val universityEmail: String? = null,

    /** Numero MAV atribuito della banca al bolletino. */
    @SerialName("numeroMav")
    val mavNumber: String? = null,

    /** Numero fattura di conguaglio che ha generato questa riga di integrazione o rimborso. */
    @SerialName("fattConguaglioId")
    val adjustmentInvoiceId: Long? = null,

    /** Flag che indica se la pro-forma (fattura di Esse3) risulta fatturata in contabilità */
    @SerialName("fattContab")
    val accountingInvoice: Int? = null,

    /** Riporta l'importo totale da pagare per riportare il semaforo nel valore VERDE. */
    @SerialName("dovuto")
    val due: Double? = null,

    /** Semaforo tasse. Riporta i valori ROSSO quando esistono addebiti studente di tasse bloccanti scaduti oltre i giorni di tolleranza, GIALLO se esistono more addebitabili, VERDE se lo studente è in regola con le tasse. */
    @SerialName("semaforo")
    val trafficLight: String? = null,

    /** Riporta la matricola attiva o in ipotesi solo per righe di addebito con tipo addebito STU. */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Identificativo dello studente. */
    @SerialName("stuId")
    val studentId: Long? = null
)

@Serializable
data class Esse3TuitionCalculationRequest(
    /** Anno accademico. */
    @SerialName("aaId")
    val academicYearId: Int = 0,

    /** Codice del processo. */
    @SerialName("procCod")
    val procedureCode: String = "",

    /** Identificativo del corso di studi. */
    @SerialName("cdsId")
    val courseOfStudyId: Long = 0L,

    /** Identificativo del percorso di studi. */
    @SerialName("pdsId")
    val studyPlanId: Long = 0L,

    /** Anno di ordinamento. */
    @SerialName("aaOrdId")
    val academicYearOrderId: Int = 0,

    /** Numero totale delle rate. */
    @SerialName("numRate")
    val installmentNumber: Int? = null,

    /** Flag per forzare l'addebito. */
    @SerialName("forzaAddebito")
    val forceCharge: Int? = null,

    /** Flag per la simulazione del calcolo. */
    @SerialName("simulaFlg")
    val simulationFlag: Int? = null,

    /** Flag per lo storno. */
    @SerialName("stornaFlg")
    val reverseFlag: Int? = null,

    /** Flag per includere carriera chiusa. */
    @SerialName("carrChiusaFlg")
    val careerClosedFlag: Int? = null,

    /** Flag per il salvataggio dei dettagli del calcolo. */
    @SerialName("salvaDettCalcFlg")
    val saveCalculationDetailFlag: Int? = null,

    /** Anno di riconoscimento. */
    @SerialName("anniRic")
    val researchYears: Int? = null,

    /** Anno di riferimento per la tassazione. */
    @SerialName("aaRegTax")
    val academicYearTaxRegulation: Int? = null,

    /** Data di scadenza delle rate sospese. */
    @SerialName("dataScadRateSosp")
    val suspendedInstallmentDeadline: String? = null
)

@Serializable
data class Esse3ExemptionsList(
    /** Codice dell'esonero. */
    @SerialName("esoneroCod")
    val exemptionCode: String? = null,

    /** Descrizione dell'esonero. */
    @SerialName("esoneroDes")
    val exemptionDescription: String? = null,

    /** Peso dell'esonero. */
    @SerialName("priorita")
    val priority: Long? = null,

    /** Codice di riferimento della domanda di esonero. */
    @SerialName("codiceRifDom")
    val domicileReferenceCode: String? = null,

    /** Data domanda di esonero. */
    @SerialName("dataDomanda")
    val applicationDate: String? = null,

    /** Indica se lo studente ha ottenuto l'esonero. */
    @SerialName("ottenuto")
    val obtained: String? = null,

    /** Data di ottenimento dell'esonero. */
    @SerialName("dataOttenimento")
    val obtainmentDate: String? = null,

    /** Indica se la domanda di esonero è stata annullata. */
    @SerialName("annullato")
    val canceled: String? = null,

    /** Data di annullamento della domanda di esonero. */
    @SerialName("dataAnnullamento")
    val cancellationDate: String? = null,

    /** Indica se la domanda di esonero è stata respinta. */
    @SerialName("respinto")
    val rejected: String? = null,

    /** Data di respingimento della domanda di esonero. */
    @SerialName("dataRespingimento")
    val rejectionDate: String? = null,

    /** Motivo del respingimento della domanda di esonero. */
    @SerialName("motivoRespingimento")
    val rejectionReason: String? = null,

    /** Codice dello stato di un ricorso studente dovuto ad una valutazione negativa della domanda di esonero. */
    @SerialName("statoRicorsoCod")
    val appealStateCode: String? = null,

    /** Descrizione dello stato di un ricorso studente dovuto ad una valutazione negativa della domanda di esonero. */
    @SerialName("statoRicorsoDes")
    val appealStateDescription: String? = null,

    /** Motivo dello stato di un ricorso studente dovuto ad una valutazione negativa della domanda di esonero. */
    @SerialName("motivoStatoRic")
    val requestStateReason: String? = null,

    /** Data di presentazione di un ricorso studente dovuto ad una valutazione negativa della domanda di esonero. */
    @SerialName("dataRicorso")
    val appealDate: String? = null,

    /** Data presentazione documentazione cartacea da parte dello studente. */
    @SerialName("dataPresDoc")
    val documentPresenceDate: String? = null,

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

    /** Ultimo utente che ha forzato l'esito della domanda di esonero. */
    @SerialName("usrForzatura")
    val forceUser: String? = null,

    /** Data dell'ultima forzatura manuale dell'esito della domanda di esonero. */
    @SerialName("dataForzatura")
    val forceDate: String? = null
)

@Serializable
data class Esse3SpgTransactionStatusData(
    /** data del evento notificato in formato ISO */
    @SerialName("eventDate")
    val eventDate: String = "",

    /** Canale di pagamento scelto. */
    @SerialName("channel")
    val channel: String = "",

    @SerialName("cart")
    val cart: Esse3Cart
)

@Serializable
data class Esse3MessageOutcomeResponse(
    /** Codice di ritorno del processo. */
    @SerialName("esito")
    val outcome: Int? = null,

    /** Messaggio che contiene una descrizione dell'esito. */
    @SerialName("returnMessage")
    val returnMessage: String? = null
)

@Serializable
data class Esse3CumulativeExemptions(
    /** Codice dell'esonero cumulato. */
    @SerialName("esoneroCod")
    val exemptionCode: String? = null,

    /** Descrizione dell'esonero cumulato. */
    @SerialName("esoneroDes")
    val exemptionDescription: String? = null
)

@Serializable
data class Esse3RejectExemption(
    /** Anno accademico. */
    @SerialName("annoAccademico")
    val academicYear: Long = 0L,

    /** Codice fiscale dello studente. */
    @SerialName("codFis")
    val fiscalCode: String = "",

    /** Matricola dello studente. */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Identificativo dello studente. */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** Codice del corso di studio. */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** Codice della facoltà. */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** esonero da inserire. */
    @SerialName("esonero")
    val exemption: String = "",

    /** motivazione del respigimento dell'esonero. */
    @SerialName("motivazione")
    val reason: String? = null
)

@Serializable
data class Esse3CartItem(
    /** Anno accademico. */
    @SerialName("cartItemNum")
    val cartItemNumber: Long? = null,

    /** Identificativo del pagamento. */
    @SerialName("cartItemExtId")
    val cartItemExternalId: Long? = null,

    /** Status del singolo pagamento. */
    @SerialName("cartItemStatusCod")
    val cartItemStatusCode: String? = null,

    /** Descrizione dello status del singol pagamento. */
    @SerialName("cartItemStatusDes")
    val cartItemStatusDescription: String? = null,

    /** Importo della voce. */
    @SerialName("amount")
    val amount: Double? = null,

    /** Valuta dell'importo. */
    @SerialName("currency")
    val currency: String? = null,

    /** data del pagamento in formato ISO */
    @SerialName("paymentDate")
    val paymentDate: String? = null,

    /** Identificativo del pagamento nel canale scelto. */
    @SerialName("channelPaymentId")
    val paymentChannelId: String? = null,

    /** Indica se l'item ha cambiato stato con rispetto alla ultima chiamata fatta. */
    @SerialName("statusChanged")
    val statusChanged: Boolean? = null
)

@Serializable
data class Esse3StudentExemptions(
    /** Identificativo della persona. */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo dell'iscrizione. */
    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    /** Identificativo dello studente. */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** Identificativo dello studente utilizzato in applicazioni esterne. */
    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    /** Identificativo del corso di studio. */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Identificativo dell'anno di ordinamento. */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** Identificativo del percorso di studio. */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** Identificativo dell'anno di iscrizione. */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    /** Anno di corso. */
    @SerialName("annoCorso")
    val courseYear: Long? = null,

    /** Anni fuori corso. */
    @SerialName("anniFc")
    val fcYears: Long? = null,

    /** Codice tipo iscrizione (IC, FC, RI). */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Codice dell'esonero legato all'iscrizione (esonero prioritario). */
    @SerialName("esoneroIscrCod")
    val enrollmentExemptionCode: String? = null,

    /** Descrizione dell'esonero legato all'iscrizione (esonero prioritario). */
    @SerialName("esoneroIscrDes")
    val enrollmentExemptionDescription: String? = null,

    /** Identificativo della fascia di reddito dello studente. */
    @SerialName("fasciaRedditoId")
    val incomeBandId: Long? = null,

    /** Numero della fascia di reddito dello studente. */
    @SerialName("fasciaRedditoNum")
    val incomeBandNumber: Long? = null,

    /** Descrizione della fascia di reddito dello studente. */
    @SerialName("fasciaRedditoDes")
    val incomeBandDescription: String? = null,

    /** Indica se lo studente ha un'iscrizione part-time. */
    @SerialName("partTime")
    val partTime: String? = null,

    @SerialName("listaEsoneri")
    val exemptionList: List<Esse3ExemptionsList> = emptyList()
)

@Serializable
data class Esse3SelfCertification(
    /** Numero univoco della persona. */
    @SerialName("persId")
    val personId: Long? = null,

    /** Numero univoco della autocertificazione. */
    @SerialName("autocertId")
    val selfCertificationId: Long? = null,

    /** Anno accademico. */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Data ufficiale di presentazione definitiva. */
    @SerialName("dataPresentazione")
    val presentationDate: String? = null,

    /** Data di primo inserimento. */
    @SerialName("dataCaricamento")
    val uploadDate: String? = null,

    /** Data di ultima modifica di un qualunque elemento dell'autocertificazione. */
    @SerialName("dataUltimaModif")
    val lastModificationDate: String? = null,

    /** Valore Indicatore Situazione Economica Equivalente. */
    @SerialName("isee")
    val isee: Double? = null,

    /** Valore Indicatore Situazione Patrimoniale Equivalente. */
    @SerialName("ispe")
    val ispe: Double? = null,

    /** Corrisponde alla chiave unica della fascia dichiarata dallo studente. */
    @SerialName("fasciaId")
    val bandId: Long? = null,

    /** Peso attribuito alla fascia. Il valore piu' basso e' il piu' favorevole per lo studente. */
    @SerialName("fasciaNum")
    val bandNumber: Long? = null,

    /** Descrizione della fascia. */
    @SerialName("fasciaDes")
    val bandDescription: String? = null,

    /** Data in cui l'operatore dell'università ha ricevuto il cartaceo dell'autocertificazione presentata, tale DATA può coincidere con la dataPresentazione. */
    @SerialName("dataCartaceo")
    val paperDate: String? = null,

    /** Indica se trattasi di autocertificazione presentata dallo studente, o verificata dall'università. */
    @SerialName("versione")
    val version: String? = null,

    /** Indica se lo studente non dichiara redditi. */
    @SerialName("reddNondichFlg")
    val undeclaredIncomeFlag: Int? = null,

    /** Indica se si è negato il consenso alla verifica dei dati reddituali. */
    @SerialName("noVerifReddFlg")
    val noIncomeVerificationFlag: Int? = null,

    /** Indica se l'autocertificazione è stata modificata dopo aver inserito la data di presentazione. */
    @SerialName("modDopoPresFlg")
    val postPresenceModeFlag: Int? = null,

    /** Indica se lo studente costituisce nucleo familiare autonomo. */
    @SerialName("nuclAutonomoFlg")
    val autonomousNucleusFlag: Int? = null,

    /** Data di stampa del verbale di accertamento. */
    @SerialName("dataStampaVerb")
    val minutesPrintDate: String? = null,

    /** Identificativo della dichiarazione del flusso XML inserito dal CAAF. */
    @SerialName("iseeuDichId")
    val iseeuDeclarationId: Long? = null,

    /** Indica se l'autocertificazione non debba più essere assoggettata al controllo di mora per tardata consegna. */
    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    /** Motivo di esclusione dal calcolo delle more. */
    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    /** Data sottoscrizione. */
    @SerialName("dataSottoscr")
    val subscriptionDate: String? = null,

    /** Errore nel caricamento della dichiarazione sulle tabelle di autocertificazione di ESSE3. */
    @SerialName("errataFlg")
    val errataFlag: Int? = null,

    /** Identificativo protocollo interno CAAF. */
    @SerialName("protCaaf")
    val caafProtocol: String? = null,

    /** Tipologia di ISEE scaricato dall'ente. O -Ordinario R- Ristretto U- Universitario Z- Universitario definito Non calcolabile. */
    @SerialName("tipologiaIsee")
    val iseeTypology: String? = null,

    /** Indica se la dichiarazione da cui è stato determinato l'indicatore ISEE contiene almeno una difformità. */
    @SerialName("difformitaFlg")
    val discrepancyFlag: Int? = null,

    /** Numero di prodotocollo legato al rilascio dell'ISEE da INPS. */
    @SerialName("protIsee")
    val iseeProtocol: String? = null,

    /** Data di rilascio dell'ISEE da INPS. */
    @SerialName("dataRilIsee")
    val iseeReleaseDate: String? = null,

    /** Indica che l'isee inserito è di tipo corrente. */
    @SerialName("iseeCorrenteFlg")
    val currentIseeFlag: Int? = null,

    /** Contiene la data del protocollo sostitutivo per isee corrente. */
    @SerialName("dataRilIseeSost")
    val substituteIseeReleaseDate: String? = null,

    /** Contiene il protocollo sostitutivo per isee corrente. */
    @SerialName("protDsuSost")
    val substituteDsuProtocol: String? = null,

    /** Identificatore alfanumerico del flusso inviato. */
    @SerialName("identificatoreFlusso")
    val flowIdentifier: String? = null,

    /** Identificativo forzatura. */
    @SerialName("forzaturaId")
    val forceId: Int? = null,

    /** Descrizione forzatura. */
    @SerialName("forzaturaDes")
    val forceDescription: String? = null,

    @SerialName("componenti")
    val components: List<Esse3SelfCertificationComponents> = emptyList()
)

@Serializable
data class Esse3Item(
    /** Codice tassonomia. */
    @SerialName("codTassonomia")
    val taxonomyCode: String? = null,

    /** iban di accredito della voce. */
    @SerialName("ibanAccredito")
    val creditIban: String? = null,

    /** iban di appoggio. */
    @SerialName("ibanAppoggio")
    val supportIban: String? = null,

    /** Importo della voce. */
    @SerialName("importoVoce")
    val itemAmount: Double? = null,

    /** Codice della voce. */
    @SerialName("voceCod")
    val itemCode: String? = null,

    /** Descrizione della voce. */
    @SerialName("voceDes")
    val itemDescription: String? = null
)

@Serializable
data class Esse3InvoiceAttachmentMetadata(
    /** nome del file */
    @SerialName("filename")
    val fileName: String = "",

    /** autore del file */
    @SerialName("autore")
    val author: String? = null,

    /** titolo del file */
    @SerialName("titolo")
    val title: String = "",

    /** descrizione libera */
    @SerialName("descrizione")
    val description: String = "",

    /** tipologia dell'allegato relativo all'entità p17_tipologia_allegati */
    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    /** flag che indica se l'allegato deve già risultare validato o meno */
    @SerialName("validoFlg")
    val validFlag: Int = 0,

    /** flag che indica se l'allegato deve essere visibile da web oppure no */
    @SerialName("abilVisWeb")
    val webVisibility: Int = 0
)

@Serializable
data class Esse3CollectionData(
    /** id della transazione. */
    @SerialName("idTransazione")
    val transactionId: Long = 0L,

    /** id della fattura pagata. */
    @SerialName("fattId")
    val invoiceId: Long = 0L,

    /** Importo pagato. */
    @SerialName("importo")
    val amount: Double = 0.0,

    /** data in cui è stato fatto il pagamento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataPagamento")
    val paymentDate: String = "",

    /** Codice della modalità di pagamento che è stata configurata in Esse3 per l'inserimento degli incassi da servizio esterno. */
    @SerialName("provenienza")
    val origin: String = ""
)

@Serializable
data class Esse3Transaction(
    /** Cognome. */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome. */
    @SerialName("nome")
    val name: String? = null,

    /** Codice fiscale. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Identificativo del pagamento. */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** Identificativo Univoco del Versamento. */
    @SerialName("iuv")
    val iuv: String? = null,

    /** codice avviso della fattura. */
    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    /** Importo totale del versamento. */
    @SerialName("importo")
    val amount: Double? = null,

    /** data della transazione */
    @SerialName("dataTransazione")
    val transactionDate: String? = null,

    /** esito della transazione */
    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    /** descrizione dell'esito della transazione */
    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    /** stato della transazione */
    @SerialName("stato")
    val state: String? = null,

    /** Indica se lo stato della transazione è finale.. */
    @SerialName("statoFinale")
    val finalState: Int? = null,

    /** esito della transazione */
    @SerialName("esitoTransazione")
    val transactionOutcome: String? = null,

    @SerialName("esitoMessaggio")
    val messageOutcome: List<Esse3MessageOutcome> = emptyList(),

    /** Identificativo del pagamento. */
    @SerialName("iur")
    val iur: String? = null,

    /** Importo effettivamento pagato. */
    @SerialName("importoPagato")
    val paidAmount: Double? = null,

    /** data di pagamento */
    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    /** Indica se esiste un incasso collegato alla transazione. */
    @SerialName("esisteIncasso")
    val collectionExists: Int? = null,

    /** Indica se l'incasso è da gestire. */
    @SerialName("incassoDaGestire")
    val collectionToManage: Int? = null,

    /** Descrizione dell'errore in caso di un incasso da gestire. */
    @SerialName("erroreIncasso")
    val collectionError: String? = null,

    /** Codice contesto pagamento. */
    @SerialName("codiceContestoPagamento")
    val paymentContextCode: String? = null,

    /** Anno accademico del debito. */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Indica se il versamento è stato pagato. */
    @SerialName("pagatoFlg")
    val paidFlag: Int? = null,

    /** Indica se la quietanza è stampabile. */
    @SerialName("quietanzaStampabile")
    val printableReceipt: Int? = null
)

@Serializable
data class Esse3PostCollectionResponse(
    /** Codice di ritorno dell'inserimento. */
    @SerialName("returnCode")
    val returnCode: Long? = null,

    /** Messaggio che contiene una descrizione del returnCode. */
    @SerialName("returnMessage")
    val returnMessage: String? = null
)

@Serializable
data class Esse3PagoPATransactionResponse(
    /** url da usare per redirigere allo studente al portale di pagamento. */
    @SerialName("redirectUrlPagoPA")
    val pagopaRedirectUrl: String? = null
)

@Serializable
data class Esse3Cart(
    /** Id della transazione. */
    @SerialName("transId")
    val transactionId: String? = null,

    /** Status del carrello. */
    @SerialName("cartStatusCod")
    val cartStatusCode: String? = null,

    /** Descrizione dello status del carrello. */
    @SerialName("cartStatusDes")
    val cartStatusDescription: String? = null,

    @SerialName("cartItem")
    val cartItem: List<Esse3CartItem> = emptyList()
)

@Serializable
data class Esse3TuitionCalculationDetail(
    /** Identificativo dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** Identificativo della fattura */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** Anno accademico */
    @SerialName("aaId")
    val academicYearId: Int? = null,

    /** Codice combinazione tassa */
    @SerialName("combCod")
    val combinationCode: String? = null,

    /** Codice tassa */
    @SerialName("tassaCod")
    val taxCode: String? = null,

    /** Codice voce tassa */
    @SerialName("voceCod")
    val itemCode: String? = null,

    /** Data di scadenza del pagamento */
    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    /** Importo della tassa */
    @SerialName("importo")
    val amount: Double? = null,

    /** Identificativo della rata */
    @SerialName("rataId")
    val installmentId: Int? = null,

    /** Nota tassa */
    @SerialName("nota")
    val note: String? = null,

    /** Data di pagamento della tassa */
    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    /** Flag di storno */
    @SerialName("stornoflg")
    val reverseFlag: Int? = null
)

@Serializable
data class Esse3InvoicePaymentData(
    /** id della fattura da pagare. */
    @SerialName("fattId")
    val invoiceId: Long = 0L,

    /** Importo pagato. */
    @SerialName("importo")
    val amount: Double = 0.0,

    /** data in cui è stato fatto il pagamento (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataPagamento")
    val paymentDate: String = "",

    /** Tipo pagamento. */
    @SerialName("tipoPagamento")
    val paymentType: String = "",

    /** Numero di bolletino. */
    @SerialName("numBolletino")
    val bulletinNumber: String? = null,

    /** 1 se si vuole fare la ripartizione automatica del errato pagamento. 0 altrimenti. */
    @SerialName("ripartAutoErrPag")
    val autoRepartitionErrorPayment: Int? = null,

    /** 1 se si vuole fare la convalida del pagamento. 0 altrimenti. */
    @SerialName("convalidaPagamento")
    val paymentValidation: Int? = null,

    /** data di accredito del pagamento (DD/MM/YYYY HH24:MI:SS). Obbligatorio se convalidaPagamento = 1. */
    @SerialName("dataAccredito")
    val creditDate: String? = null,

    /** Anno finanziario. Obbligatorio se convalidaPagamento = 1. */
    @SerialName("annoFinanziario")
    val financialYear: Long? = null,

    /** Id del conto corrente in cui si accrediterà il pagamento. Obbligatorio se il parconf USA_CC_MULTIPLI = 1. */
    @SerialName("contoCorrenteId")
    val currentAccountId: Long? = null
)

@Serializable
data class Esse3StudentEnrollment(
    /** Indica se lo studente è in regola con il pagamento della prima rata della tassa di iscrizione. Valorizzato con 'S' se lo stato dell'iscrizione annuale è 'A', con 'N' se lo stato dell'iscrizione è in 'S' con motivazione 'T'. */
    @SerialName("iscrReg")
    val regularEnrollment: String? = null,

    /** Tipo di corso al quale risulta iscritto lo studente nell'A.A. di riferimento. Valorizzato con il codice del tipo laurea MIUR recuperato a partire dal tipo di corso. */
    @SerialName("idTipoLaurea")
    val degreeTypeId: String? = null,

    /** Indica il tipo di corso di specializzazione obbligatorio ai fini dell'esercizio della professione. Valorizzato di default con 'N'. */
    @SerialName("corsoSpecObb")
    val mandatorySpecializationCourse: String? = null,

    /** Indica se il corso di specializzazione è dell'area medica di cui al d.lgs.n.368 del 04/08/99. Valorizzato con 'S' se il tipo di specializzazione del corso è 'MEDI', con 'N' altrimenti. */
    @SerialName("corsoSpecMed")
    val medicalSpecializationCourse: String? = null,

    /** Indica se lo studente è un dottorando assegnatario di borsa di studio attivata ai sensi dell'art.4 del d.lgs.n.210 del 03/07/1998. Possibili valori 'S', 'N'. */
    @SerialName("dottBorsa")
    val phdScholarship: String? = null,

    /** Descrizione del dipartimento al quale risulta iscritto lo studente nell'A.A. di riferimento. */
    @SerialName("dipDes")
    val departmentDescription: String? = null,

    /** Codice del corso di studio al quale risulta iscritto lo studente nell'A.A. di riferimento. Viene completato a sinistra con degli '0' fino al raggiungimento dei 10 caratteri. */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** Descrizione del corso di studio al quale risulta iscritto lo studente nell'A.A. di riferimento. */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** Codice della classe ministeriale del corso di studio al quale si risulta iscritti per l'A.A. di riferimento */
    @SerialName("cdsClasseCod")
    val courseOfStudyClassCode: String? = null,

    /** Descrizione della classe ministeriale del corso di studio al quale si risulta iscritti per l'A.A. di riferimento */
    @SerialName("cdsClasseDes")
    val courseOfStudyClassDescription: String? = null,

    /** Descrizione del comune nel quale è ubicata la sede del corso di studio al quale risulta iscritto lo studente nell'A.A. di riferimento. Viene considerata la sede definita come default amministrativo. */
    @SerialName("comuneSedeDes")
    val seatMunicipalityDescription: String? = null,

    /** Codice belfiore del comune nel quale è ubicata la sede del corso di studio frequentato */
    @SerialName("comuneSedeBelfioreCod")
    val seatMunicipalityBelfioreCode: String? = null,

    /** Numero di matricola. Vengono recuperati solo i tratti di carriera in stato 'A' o 'I'. */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Anno di corso a cui risulta iscritto lo studente nell'A.A. di riferimento. */
    @SerialName("annoCorso")
    val courseYear: Long? = null,

    /** Stato dell'iscrizione. */
    @SerialName("staIscrCod")
    val enrollmentStatusCode: String? = null,

    /** Tipo di iscrizione. Possibili valori 'IC', 'FC', 'RI'. */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Numero di anni fuori corso. */
    @SerialName("anniFc")
    val fcYears: Long? = null,

    /** A.A. di prima iscrizione al corso di studio al quale risulta iscritto lo studente nell'A.A. di riferimento. */
    @SerialName("aaPrimaIscr")
    val academicYearFirstEnrollment: Long? = null,

    /** Numero di cfu conseguiti alla prima data di riferimento, ad esempio 10/08/(A.A. di riferimento). */
    @SerialName("nCfuDr1")
    val dr1CfuNumber: Double? = null,

    /** Media ponderata dei crediti conseguiti alla prima data di riferimento, ad esempio 10/08/(A.A. di riferimento). */
    @SerialName("mediaPesataDr1")
    val weightedAverageDr1: Double? = null,

    /** Stato della matricola. */
    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    /** Motivo dello stato della matricola. */
    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    /** Numero di cfu conseguiti alla prima data di riferimento, ad esempio 10/08/(A.A. di riferimento), convalidati da tratto di carriera precedente. */
    @SerialName("nCfuConvDr1")
    val dr1ConversionCfuNumber: Double? = null,

    /** A.A. di prima iscrizione al tratto di carriera precedente a quello attivo per l'A.A. di riferimento dal quale sono stati convalidati crediti conseguiti alla prima data di riferimento, ad esempio 10/08/(A.A. di riferimento). Viene recuperato il tratto di carriera più vecchio dal quale risultano convalidati dei cfu. */
    @SerialName("aaCfuConvDr1")
    val academicYearCFUConversionDr1: Long? = null,

    /** Tipo di corso del tratto di carriera precedente a quello attivo per l'A.A. di riferimento dal quale sono stati convalidati crediti conseguiti alla prima data di riferimento, ad esempio 10/08/(A.A. di riferimento). Valorizzato con il codice del tipo laurea MIUR recuperato a partire dal tipo di corso. */
    @SerialName("tcCfuConvDr1")
    val tcDr1ConversionCfu: String? = null,

    /** A.A. in cui lo studente ha conseguito un titolo di livello pari a quello del corso di studio a cui risulta iscritto nell'A.A. di riferimento. Vengono considerati di pari livello i titoli relativi ai tipi corso ('L2', 'LM5', 'LM6', 'LC5', 'LC6') oppure ('LM', 'LS') oppure ('S1', 'SP2', 'SP3', 'SP4', 'SP5', 'SP6', 'D1', 'D2'). */
    @SerialName("aaConsTitEq")
    val academicYearEquivalentTitleConsent: Long? = null,

    /** Tipo di corso su cui lo studente ha conseguito un titolo di livello pari a quello del corso di studio a cui risulta iscritto nell'A.A. di riferimento. Vengono considerati di pari livello i titoli relativi ai tipi corso ('L2', 'LM5', 'LM6', 'LC5', 'LC6') oppure ('LM', 'LS') oppure ('S1', 'SP2', 'SP3', 'SP4', 'SP5', 'SP6', 'D1', 'D2'). */
    @SerialName("tipoTitMiurEq")
    val miurEquivalentTitleType: String? = null,

    /** Indica se lo studente ha conseguito il titolo di pari livello presso lo stesso Ateneo a cui risulta iscritto nell'A.A. di riferimento. Possibili valori 'S', 'N'. */
    @SerialName("titEqConsInAte")
    val equivalentTitleConsolidatedInAte: String? = null,

    /** Indica se lo studente ha un'iscrizione attiva sull'A.A. di riferimento su un corso FIT (CS24). Possibili valori 'S', 'N' */
    @SerialName("iscrCorsoFit")
    val fitCourseEnrollment: String? = null,

    /** Indica se lo studente è stato candidato o ammesso ad un bando di mobilità internazionale outgoing sull'A.A. precedente a quello di riferimento. Possibili valori 'S', 'N'. */
    @SerialName("bandoMobint")
    val mobilityCall: String? = null,

    /** Indica se lo studente ha una carriera chiusa per un qualsiasi motivo o un tratto di carriera precedente su un corso di studio dello stesso livello di quello a cui si è iscritti nell'A.A. di riferimento. Possibili valori 'S', 'N'. */
    @SerialName("variazioneCarriera")
    val careerVariation: String? = null,

    /** Numero di cfu conseguiti alla seconda data di riferimento, ad esempio 30/04/(A.A. di riferimento + 1). */
    @SerialName("nCfuDr2")
    val dr2CfuNumber: Double? = null,

    /** Numero di cfu conseguiti alla terza data di riferimento, ad esempio 10/08/(A.A. di riferimento + 1). */
    @SerialName("nCfuDr3")
    val dr3CfuNumber: Double? = null,

    /** Numero di cfu conseguiti alla quarta data di riferimento, ad esempio 30/11/(A.A. di riferimento + 1). */
    @SerialName("nCfuDr4")
    val dr4CfuNumber: Double? = null,

    /** A.A. in cui lo studente ha conseguito il titolo relativo al corso di studio a cui risulta iscritto nell'A.A. di riferimento. */
    @SerialName("aaConsTit")
    val academicYearTitleConsent: Long? = null,

    /** Indica se lo studente è iscritto ad un corso STEM nell'A.A. di riferimento. */
    @SerialName("iscrCorsoStem")
    val stemCourseEnrollment: String? = null,

    /** Durata del corso di studi. */
    @SerialName("durataCorso")
    val courseDuration: Long? = null,

    /** Stato della carriera. */
    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    /** Motivo dello stato della carriera. */
    @SerialName("motStaStuCod")
    val studentStatusReasonCode: String? = null,

    /** A.A. di inizio carriera. */
    @SerialName("aaInizioCarriera")
    val academicYearCareerStart: Long? = null,

    /** A.A. di fine carriera. */
    @SerialName("aaFineCarriera")
    val academicYearCareerEnd: Long? = null,

    /** Data chiusura carriera. */
    @SerialName("dataChiusuraCarriera")
    val careerClosingDate: String? = null,

    /** Data conseguimento titolo. */
    @SerialName("dataConsTit")
    val titleDeliveryDate: String? = null,

    /** Indica se lo studente si è laureato nei termini. */
    @SerialName("laureatoNeiTermini")
    val graduatedWithinTerms: String? = null
)

@Serializable
data class Esse3MeritData(
    /** Codice Fiscale dello studente. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Cognome dello Studente. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome dello studente. */
    @SerialName("nome")
    val name: String? = null,

    /** Data di nascita dello studente. */
    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("stuIscr")
    val studentEnrollment: List<Esse3StudentEnrollment> = emptyList()
)

@Serializable
data class Esse3PagoPATransaction(
    /** Identificativo del pagamento. */
    @SerialName("fattId")
    val invoiceId: Long = 0L,

    /** url di ritorno dopo il pagamento. */
    @SerialName("returnURL")
    val returnURL: String = ""
)

@Serializable
data class Esse3MessageOutcome(
    /** lingua del messaggio in formato iso6392. */
    @SerialName("lingua")
    val language: String? = null,

    /** testo del messaggio. */
    @SerialName("testo")
    val text: String? = null
)

@Serializable
data class Esse3ScholarshipOutcomeResponse(
    @SerialName("datiEsitoBorsa")
    val scholarshipOutcomeData: Esse3ScholarshipData? = null,

    @SerialName("errori")
    val errors: List<Esse3ExemptionDataError> = emptyList()
)

@Serializable
data class Esse3PersonDebit(
    /** Indica la tipologia di addebito. Può assumere i valori STU per indicare addebito tasse studente, PER per indicare addebiti tasse persona/esami di stato, AMM per indicare tasse legate a concorsi di ammissione. */
    @SerialName("tipoAd")
    val teachingActivityType: String? = null,

    /** Nome della persona debitrice. */
    @SerialName("nome")
    val name: String? = null,

    /** Cognome della persona debitrice. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Codice fiscale della persona debitrice. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Anno accademico dell'addebito. */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Identificativo della persona. */
    @SerialName("persId")
    val personId: Long? = null,

    /** Identificativo della tassa. */
    @SerialName("tassaId")
    val taxId: Long? = null,

    /** Tipologia di tassa. */
    @SerialName("tipoTaxCod")
    val taxTypeCode: String? = null,

    /** Codice della tassa. */
    @SerialName("tassaCod")
    val taxCode: String? = null,

    /** Descrizione della tassa. */
    @SerialName("tassaDes")
    val taxDescription: String? = null,

    /** Identificativo della combinazione. */
    @SerialName("combId")
    val combinationId: Long? = null,

    /** Codice della combinazione. */
    @SerialName("combCod")
    val combinationCode: String? = null,

    /** Descrizione della combinazione. */
    @SerialName("combDes")
    val combinationDescription: String? = null,

    /** Tipologia di voce. */
    @SerialName("tipoVoceCod")
    val itemTypeCode: String? = null,

    /** Identificativo della voce. */
    @SerialName("voceId")
    val itemId: Long? = null,

    /** Codice della voce. */
    @SerialName("voceCod")
    val itemCode: String? = null,

    /** Descrizione voce. */
    @SerialName("voceDes")
    val itemDescription: String? = null,

    /** Identificativo rata. */
    @SerialName("rataId")
    val installmentId: Long? = null,

    /** Descrizione rata. */
    @SerialName("rataDes")
    val installmentDescription: String? = null,

    /** Importo della voce. */
    @SerialName("importoVoce")
    val itemAmount: Double? = null,

    /** Data scadenza addebito. */
    @SerialName("scadenzaAddebito")
    val chargeExpiration: String? = null,

    /** Flag che indica la scadenza dell'addebito. */
    @SerialName("scadutoFlg")
    val expiredFlag: Int? = null,

    /** Tipologia di pagamento/rimborso selezionata nella fattura. */
    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    /** Note legate all'addebito. */
    @SerialName("note")
    val notes: String? = null,

    /** Nota di calcolo dell'addebito. Riporta le combinazioni applicate e le descrizioni delle regole di variazione importi. */
    @SerialName("notaCalcolo")
    val calculationNote: String? = null,

    /** Flag di annullamento della voce. Se 0 la voce non è annullata, se 1 voce cancellata logicamente. */
    @SerialName("annullataFlg")
    val canceledFlag: Int? = null,

    /** Codice di elaborazione da associare al rimborso. */
    @SerialName("codElabRimb")
    val refundProcessingCode: String? = null,

    /** Numero di mandato alla banca per il pagamento del rimborso. */
    @SerialName("numMandatoRimb")
    val refundMandateNumber: String? = null,

    /** Causale di rimborso. */
    @SerialName("cauRimbCod")
    val refundReasonCode: String? = null,

    /** Codice associato alla fattura. */
    @SerialName("fattCod")
    val invoiceCode: String? = null,

    /** Data di scadenza della fattura. */
    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    /** Flag che indica che la fattura è scaduta. */
    @SerialName("fattScadutaFlg")
    val expiredInvoiceFlag: Int? = null,

    /** Importo della fattura. */
    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    /** Identificativo della fattura. */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** Data emissione fattura. */
    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    /** Data elaborazione fattura. */
    @SerialName("dataElab")
    val processingDate: String? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav1")
    val mav1Description: String? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav2")
    val mav2Description: String? = null,

    /** Per i bollettini correttivi derivanti da errato pagamento, indica il bollettino originale al quale la correzione fa riferimento. */
    @SerialName("fattErrataId")
    val erroneousInvoiceId: Long? = null,

    /** Flag di annullamento della fattura. Se uguale a 0 la fattura risulta valida, in caso contrario risulta annullata. */
    @SerialName("fattAnnullata")
    val canceledInvoice: Long? = null,

    /** Identificativo interno del pagamento. */
    @SerialName("pagId")
    val paymentId: Long? = null,

    /** Importo pagato. */
    @SerialName("importoPag")
    val paidAmount: Double? = null,

    /** Flag che indica la presenza di un pagamento. */
    @SerialName("pagatoFlg")
    val paidFlag: Int? = null,

    /** Data di pagamento. */
    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    /** Data di notifica. */
    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    /** Data di accredito. */
    @SerialName("dataAccredito")
    val creditDate: String? = null,

    /** Modalità di pagamento associata all'incasso. */
    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    /** iuv della fattura pagabile su Nodo Pagamenti. */
    @SerialName("iuv")
    val iuv: String? = null,

    /** codice avviso della fattura pagabile su Nodo Pagamenti. */
    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    /** iur della fattura pagabile su Nodo Pagamenti. */
    @SerialName("iur")
    val iur: String? = null,

    /** Numero bollettino di pagamento. */
    @SerialName("nBollettino")
    val bulletinNumber: String? = null,

    /** Rendiconto di caricamento incassi. */
    @SerialName("rendicontoId")
    val reportId: Long? = null,

    /** Flag che indica se il pagamento è stato acquisito manualmente. */
    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    /** Flag che indica che non bisogna addebitare more sulla fattura anche se pagato in ritardo.. */
    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    /** Data di valorizzazione del flag di non addebito mora. */
    @SerialName("noAddebMoreData")
    val noMoreChargeData: String? = null,

    /** Nota legata all'inibizione della mora. */
    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    /** Utente di inibizione. */
    @SerialName("noAddebMoreUsrId")
    val noMoreChargeUserId: String? = null,

    /** Identificativo della fattura con cui la mora è stata emessa. */
    @SerialName("fattMoraId")
    val lateFeeInvoiceId: Long? = null,

    /** Flag che indica la presenza di mora addebitabile. */
    @SerialName("moraAddFlg")
    val lateFeeAdditionFlag: Int? = null,

    /** Numero di more addebitate. */
    @SerialName("moraCount")
    val lateFeeCount: Long? = null,

    /** Eventuale nota legata al rimborso. */
    @SerialName("notaRimb")
    val refundNote: String? = null,

    /** Flag che indica che è stato fatto il rimborso o meno. */
    @SerialName("rimborsatoFlg")
    val refundedFlag: Int? = null,

    /** Flag che indica se la fattura dev'essere visibile da interfaccia web */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** Data di caricamento del versamento. */
    @SerialName("paDtVersamento")
    val paPaymentDate: String? = null,

    /** Numero MAV atribuito della banca al bolletino. */
    @SerialName("numeroMav")
    val mavNumber: String? = null
)

@Serializable
data class Esse3ValidExemptionsAcademicYear(
    /** Anno accademico dell'esonero. */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Codice esonero. */
    @SerialName("esoneroCod")
    val exemptionCode: String? = null,

    /** Descrizione dell'esonero. */
    @SerialName("esoneroDes")
    val exemptionDescription: String? = null,

    /** Peso dell'esonero. */
    @SerialName("priorita")
    val priority: Long? = null,

    /** Indica se l'esonero è richiedibile dallo studente da web. */
    @SerialName("richiedibile")
    val requestable: String? = null,

    /** Identificativo del prerequisito di richiedibilità. */
    @SerialName("prereqRichiedId")
    val prerequisiteRequiredId: Long? = null,

    /** Codice del prerequisito d richiedibilità. */
    @SerialName("prereqRichiedCod")
    val prerequisiteRequiredCode: String? = null,

    @SerialName("esoneriCumulati")
    val cumulatedExemptions: List<Esse3CumulativeExemptions> = emptyList(),

    /** Descrizione del beneficio legato all'esonero. */
    @SerialName("desBeneficio")
    val benefitDescription: String? = null,

    /** Documentazione da presentare per l'approvazione dell'esonero. */
    @SerialName("documentazione")
    val documentation: String? = null,

    /** Codice del tipo scadenza per la presentazione della domanda di esonero. */
    @SerialName("tipoScadPresDomanda")
    val applicationPresenceDeadlineType: String? = null,

    /** Codice dell'esonero usato in applicazioni esterne. */
    @SerialName("codiceEsterno")
    val externalCode: String? = null,

    /** Indica se l'esonero è annullabile da web, anche se già ottenuto. */
    @SerialName("esoneroAnnullabile")
    val cancellableExemption: String? = null,

    /** Indica il tipo di valutazione dell'esonero. */
    @SerialName("tipoValutazione")
    val evaluationType: String? = null,

    /** Per i soli esoneri ibridi, indica la frequenza di campionamento, ovvero ogni quante richieste trattate in automatico se ne deve trattare una come se fosse a valutazione manuale. */
    @SerialName("frequenzaCampionamento")
    val samplingFrequency: Long? = null,

    /** Per i soli esoneri ibridi, identificativo della condizione di applicabilità utilizzata per verificare se il tipo di valutazione dell'esonero deve cambiare da automatica a manuale. */
    @SerialName("condCambioValutazioneId")
    val gradeChangeConditionId: Long? = null,

    /** Per i soli esoneri ibridi, codice della condizione di applicabilità utilizzata per verificare se il tipo di valutazione dell'esonero deve cambiare da automatica a manuale. */
    @SerialName("condCambioValutazioneCod")
    val gradeChangeConditionCode: String? = null,

    /** ISEE massimo richiesto per l'ottenimento dell'esonero. */
    @SerialName("iseeMax")
    val maxIsee: Double? = null,

    /** ISPE massimo richiesto per l'ottenimento dell'esonero. */
    @SerialName("ispeMax")
    val maxIspe: Double? = null,

    /** Reddito equivalente richiesto per l'ottenimento dell'esonero. */
    @SerialName("redditoEquivalente")
    val equivalentIncome: Double? = null,

    /** Identificativo della condizione di applicabilità utilizzata per valutare gli esoneri a valutazione automatica. */
    @SerialName("condValutazioneId")
    val gradeConditionId: Long? = null,

    /** Codice della condizione di applicabilità utilizzata per valutare gli esoneri a valutazione automatica. */
    @SerialName("condValutazioneCod")
    val gradeConditionCode: String? = null,

    /** Identificativo della condizione di applicabilità alternativa utilizzata per valutare gli esoneri a valutazione automatica. */
    @SerialName("condValutazioneAltId")
    val alternativeGradeConditionId: Long? = null,

    /** Codice della condizione di applicabilità alternativa utilizzata per valutare gli esoneri a valutazione automatica. */
    @SerialName("condValutazioneAltCod")
    val alternativeGradeConditionCode: String? = null,

    /** Identificativo del livello di struttura richiesto per l'ottenimento dell'esonero. */
    @SerialName("grpLivStruttDidId")
    val groupDidacticStructureLevelId: Long? = null,

    /** Codice del livello di struttura richiesto per l'ottenimento dell'esonero. */
    @SerialName("grpLivStruttDidCod")
    val groupDidacticStructureLevelCode: String? = null,

    /** Descrizione del livello di struttura richiesto per l'ottenimento dell'esonero. */
    @SerialName("grpLivStruttDidDes")
    val groupDidacticStructureLevelDescription: String? = null,

    /** Identificativo della fascia di merito richiesta per l'ottenimento dell'esonero. */
    @SerialName("fasciaMeritoId")
    val meritBandId: Long? = null,

    /** Numero della fascia di merito richiesta per l'ottenimento dell'esonero. */
    @SerialName("fasciaMeritoNum")
    val meritBandNumber: Long? = null,

    /** Descrizione della fascia di merito richiesta per l'ottenimento dell'esonero. */
    @SerialName("fasciaMeritoDes")
    val meritBandDescription: String? = null,

    /** Identificativo della fascia di reddito richiesta per l'ottenimento dell'esonero. */
    @SerialName("fasciaRedditoId")
    val incomeBandId: Long? = null,

    /** Numero della fascia di reddito richiesta per l'ottenimento dell'esonero. */
    @SerialName("fasciaRedditoNum")
    val incomeBandNumber: Long? = null,

    /** Descrizione della fascia di reddito richiesta per l'ottenimento dell'esonero. */
    @SerialName("fasciaRedditoDes")
    val incomeBandDescription: String? = null,

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
    val modificationDate: String? = null
)

@Serializable
data class Esse3ScholarshipData(
    /** Codice fiscale dello studente. */
    @SerialName("codFis")
    val fiscalCode: String = "",

    /** Cognome dello studente. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome dello studente. */
    @SerialName("nome")
    val name: String? = null,

    /** Matricola dello studente. */
    @SerialName("matricola")
    val matricola: String? = null,

    /** Codice del corso di studio. */
    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    /** Descrizione del corso di studio. Massimo 60 caratteri. */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** Codice della facoltà. */
    @SerialName("facCod")
    val facultyCode: String? = null,

    /** Tipo di iscrizione. */
    @SerialName("tipoIscr")
    val enrollmentType: String? = null,

    /** data in cui è stato fatta la domanda (DD/MM/YYYY HH24:MI:SS) */
    @SerialName("dataDomanda")
    val applicationDate: String? = null,

    /** esito della domanda. */
    @SerialName("esito")
    val outcome: String = ""
)

@Serializable
data class Esse3Payment(
    /** Cognome. */
    @SerialName("cognome")
    val surname: String? = null,

    /** nome. */
    @SerialName("nome")
    val name: String? = null,

    /** Codice fiscale. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** ragione sociale. */
    @SerialName("ragioneSociale")
    val companyName: String? = null,

    /** Indirizzo. */
    @SerialName("indirizzo")
    val address: String? = null,

    /** Civico. */
    @SerialName("civico")
    val streetNumber: String? = null,

    /** Cap. */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Localita. */
    @SerialName("localita")
    val locality: String? = null,

    /** provincia. */
    @SerialName("provincia")
    val province: String? = null,

    /** Nazione. */
    @SerialName("nazione")
    val nation: String? = null,

    /** email. */
    @SerialName("email")
    val email: String? = null,

    /** telefono. */
    @SerialName("telefono")
    val phone: String? = null,

    /** cellulare. */
    @SerialName("cellulare")
    val mobilePhone: String? = null,

    /** Identificativo del pagamento. */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** codice avviso di pagamento. */
    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    /** Identificativo Univoco del Versamento. */
    @SerialName("iuv")
    val iuv: String? = null,

    /** Importo totale del versamento. */
    @SerialName("importoVersamento")
    val paymentAmount: Double? = null,

    /** Causale versamento. */
    @SerialName("causale")
    val reason: String? = null,

    /** data di scadenza della fattura */
    @SerialName("dataScadFatt")
    val invoiceDeadline: String? = null,

    /** Flag che indica se il versamento è annullato. */
    @SerialName("annullato")
    val canceled: Int? = null,

    /** Flag che indica se il versamento è pagato. */
    @SerialName("pagato")
    val paid: Int? = null,

    /** data di caricamento del versamento */
    @SerialName("dataVersamento")
    val paymentDate: String? = null,

    /** data di ultima verifica del versamento */
    @SerialName("dataVerificaVersamento")
    val paymentVerificationDate: String? = null,

    /** data di annullamento del versamento */
    @SerialName("dataAnnullamentoVersamento")
    val paymentCancellationDate: String? = null,

    /** data di notifica del pagamento */
    @SerialName("dataNotificaPagamento")
    val paymentNotificationDate: String? = null,

    /** data di stampa dell'avviso */
    @SerialName("dataStampaAvviso")
    val noticePrintDate: String? = null,

    /** Esito della stampa dell'avviso */
    @SerialName("esitoStampaAvviso")
    val noticePrintOutcome: String? = null,

    @SerialName("voci")
    val items: List<Esse3Item> = emptyList()
)

@Serializable
data class Esse3SelfCertificationIncome(
    /** Riferimento all'identificativo unico della tipologia del reddito. */
    @SerialName("tipoRedditoId")
    val incomeTypeId: Long? = null,

    /** Codice utilizzato per il calcolo dei redditi. */
    @SerialName("tipiRedditiCod")
    val incomeTypesCode: String? = null,

    /** Descrizione del reddito. */
    @SerialName("tipiRedditiDes")
    val incomeTypesDescription: String? = null,

    /** Descrizione del reddito. */
    @SerialName("tipoDato")
    val dataType: String? = null,

    /** Importo del reddito dichiarato. */
    @SerialName("importo")
    val amount: Double? = null,

    /** Valorizzato se il tipoDato è Flag. */
    @SerialName("valoreFlg")
    val valueFlag: Int? = null,

    /** Valorizzato se il tipoDato è Testo. */
    @SerialName("valoreTesto")
    val textValue: String? = null,

    /** Valorizzato se il tipoDato è Data. */
    @SerialName("valoreData")
    val dateValue: String? = null
)

@Serializable
data class Esse3EnrollmentForTuition(
    /** identificativo della persona */
    @SerialName("persId")
    val personId: Long? = null,

    /** identificativo dello studente */
    @SerialName("stuId")
    val studentId: Long? = null,

    /** identificativo del corso di studio */
    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    /** Codice del corso */
    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    /** Descrizione del corso */
    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    /** identificativo dell'anno di ordinamento */
    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    /** identificativo del percorso di studio */
    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    /** identificativo dell'anno di iscrizione */
    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    /** anno corso */
    @SerialName("annoCorso")
    val courseYear: Long? = null,

    /** anni fuori corso */
    @SerialName("anniFc")
    val fcYears: Long? = null,

    /** codice tipo iscrizione */
    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    /** Descrizione del tipo iscrizione */
    @SerialName("tipoIscrCodDes")
    val enrollmentTypeCodeDescription: String? = null,

    /** identificativo della fascia */
    @SerialName("fasciaId")
    val bandId: Long? = null,

    /** indica la presenza del partime */
    @SerialName("partTime")
    val partTime: String? = null,

    /** isee */
    @SerialName("isee")
    val isee: Double? = null,

    /** durata anni */
    @SerialName("durataAnni")
    val durationYears: Int? = null,

    /** codice dell'esonero applicato */
    @SerialName("tipoEsoCod")
    val exemptionTypeCode: String? = null,

    /** descrizione dell'esonero attribuito */
    @SerialName("des")
    val description: String? = null,

    /** cfu maturati nell'ultimo anno dal 10/08 al 10/08 anno corrente */
    @SerialName("pesi")
    val weights: Double? = null,

    /** anni di iscrizione nella carriera, escluse le iscrizioni sospese, in atenei esterni e ricostruite da ricognizione */
    @SerialName("anniCarr")
    val careerYears: Double? = null,

    /** fascia di merito in base a iscrizione e cfu */
    @SerialName("merito")
    val merit: String? = null,

    /** diritto ai benefici in base agli anni di carriera */
    @SerialName("frequenza")
    val attendance: String? = null,

    /** soglia cfu applicabile allo studente in funzione del part time e dell'anno di carriera */
    @SerialName("sogliaCfu")
    val creditsThreshold: Double? = null,

    /** soglia di ISEE per le agevolazioni */
    @SerialName("sogliaIsee")
    val iseeThreshold: Double? = null
)

@Serializable
data class Esse3ScholarshipOutcomeData(
    /** Anno accademico. */
    @SerialName("annoAccademico")
    val academicYear: Long = 0L,

    /** Tipo richiesta. */
    @SerialName("tipoRichiesta")
    val requestType: Long = 0L,

    @SerialName("datiBorse")
    val scholarshipData: List<Esse3ScholarshipData> = emptyList()
)

@Serializable
data class Esse3ExemptionDataError(
    /** Messaggio che contiene una descrizione dell'errore. */
    @SerialName("errore")
    val error: String? = null
)

@Serializable
data class Esse3Invoices(
    /** Flag che indica se la fattura dev'essere visibile da interfaccia web */
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    /** Flag che indica se si può iniziare una transazione di pagamento immediata con pagoPA. */
    @SerialName("pagopaImmediato")
    val pagopaImmediate: Int? = null,

    /** Flag che indica se si può stampare l'avviso pagopa. */
    @SerialName("pagopaAvviso")
    val pagopaNotice: Int? = null,

    /** Flag che indica se i pagamenti tramite pagoPA sono permessi. */
    @SerialName("pagopaEnabled")
    val pagopaEnabled: Int? = null,

    /** Data di caricamento del versamento. */
    @SerialName("paDtVersamento")
    val paPaymentDate: String? = null,

    /** Informazioni aggiuntive legate alla fattura. */
    @SerialName("infoAggiuntive")
    val additionalInfo: String? = null,

    /** Numero MAV atribuito della banca al bolletino. */
    @SerialName("numeroMav")
    val mavNumber: String? = null,

    /** identificativo della persona. */
    @SerialName("persId")
    val personId: Long? = null,

    /** Flag che indica la presenza di un pagamento. */
    @SerialName("pagatoFlg")
    val paidFlag: Int? = null,

    /** Flag di annullamento della fattura. Se uguale a 0 la fattura risulta valida, in caso contrario risulta annullata. */
    @SerialName("fattAnnullata")
    val canceledInvoice: Long? = null,

    /** Per i bollettini correttivi derivanti da errato pagamento, indica il bollettino originale al quale la correzione fa riferimento. */
    @SerialName("fattErrataId")
    val erroneousInvoiceId: Long? = null,

    /** Data elaborazione fattura. */
    @SerialName("dataElab")
    val processingDate: String? = null,

    /** Data emissione fattura. */
    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    /** Flag che indica che la fattura è scaduta. */
    @SerialName("fattScadutaFlg")
    val expiredInvoiceFlag: Int? = null,

    /** Numero di more addebitate. */
    @SerialName("moraCount")
    val lateFeeCount: Long? = null,

    /** Flag che indica la presenza di mora addebitabile. */
    @SerialName("moraAddFlg")
    val lateFeeAdditionFlag: Int? = null,

    /** Identificativo della fattura con cui la mora è stata emessa. */
    @SerialName("fattMoraId")
    val lateFeeInvoiceId: Long? = null,

    /** Utente di inibizione. */
    @SerialName("noAddebMoreUsrId")
    val noMoreChargeUserId: String? = null,

    /** Nota legata all'inibizione della mora. */
    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    /** Data di valorizzazione del flag di non addebito mora. */
    @SerialName("noAddebMoreData")
    val noMoreChargeData: String? = null,

    /** Flag che indica che non bisogna addebitare more sulla fattura anche se pagato in ritardo.. */
    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    /** Flag che indica se il pagamento è stato acquisito manualmente. */
    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    /** Rendiconto di caricamento incassi. */
    @SerialName("rendicontoId")
    val reportId: Long? = null,

    /** Numero bollettino di pagamento. */
    @SerialName("nBollettino")
    val bulletinNumber: String? = null,

    /** iur della fattura pagabile su Nodo Pagamenti. */
    @SerialName("iur")
    val iur: String? = null,

    /** codice avviso della fattura pagabile su Nodo Pagamenti. */
    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    /** iuv della fattura pagabile su Nodo Pagamenti. */
    @SerialName("iuv")
    val iuv: String? = null,

    /** Modalità di pagamento associata all'incasso. */
    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    /** Data di accredito. */
    @SerialName("dataAccredito")
    val creditDate: String? = null,

    /** Data di notifica. */
    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    /** Data di pagamento. */
    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    /** Importo pagato. */
    @SerialName("importoPag")
    val paidAmount: Double? = null,

    /** Identificativo interno del pagamento. */
    @SerialName("pagId")
    val paymentId: Long? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav2")
    val mav2Description: String? = null,

    /** Descrizione utilizzata per la stampa MAV e stampa AVVISO. */
    @SerialName("desMav1")
    val mav1Description: String? = null,

    /** Identificativo della fattura. */
    @SerialName("fattId")
    val invoiceId: Long? = null,

    /** Importo della fattura. */
    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    /** Data di scadenza della fattura. */
    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    /** Anno accademico dell'addebito. */
    @SerialName("aaId")
    val academicYearId: Long? = null,

    /** Codice fiscale della persona debitrice. */
    @SerialName("codFis")
    val fiscalCode: String? = null,

    /** Cognome della persona debitrice. */
    @SerialName("cognome")
    val surname: String? = null,

    /** Nome della persona debitrice. */
    @SerialName("nome")
    val name: String? = null
)
