package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3InvoiceItem(
    @SerialName("dataScadenza")
    val expirationDate: String,

    @SerialName("importo")
    val amount: Double,

    @SerialName("rataId")
    val installmentId: Long,

    @SerialName("voceCod")
    val itemCode: String,

    @SerialName("nota")
    val note: String? = null
)

@Serializable
data class Esse3TuitionCalculationResponse(
    @SerialName("procCod")
    val procedureCode: String? = null,

    @SerialName("tassePre")
    val preTaxes: List<Esse3TuitionCalculationDetail> = emptyList(),

    @SerialName("esitoRicTax")
    val taxSearchOutcome: String? = null,

    @SerialName("errRicTax")
    val taxSearchError: String? = null,

    @SerialName("esitoGenBoll")
    val bulletinGenerationOutcome: String? = null,

    @SerialName("errGenBoll")
    val bulletinGenerationError: String? = null,

    @SerialName("esitoFatt")
    val invoiceOutcome: List<Esse3InvoiceOutcome> = emptyList(),

    @SerialName("tassePost")
    val postTaxes: List<Esse3TuitionCalculationDetail> = emptyList()
)

@Serializable
data class Esse3InvoiceOutcome(
    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("esito")
    val outcome: String? = null,

    @SerialName("errFatt")
    val invoiceError: String? = null
)

@Serializable
data class Esse3Payments(
    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("desMav1")
    val mav1Description: String? = null,

    @SerialName("desMav2")
    val mav2Description: String? = null,

    @SerialName("pagId")
    val paymentId: Long? = null,

    @SerialName("importoPag")
    val paidAmount: Double? = null,

    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    @SerialName("dataAccredito")
    val creditDate: String? = null,

    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    @SerialName("iuv")
    val iuv: String? = null,

    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    @SerialName("iur")
    val iur: String? = null,

    @SerialName("nBollettino")
    val bulletinNumber: String? = null,

    @SerialName("rendicontoId")
    val reportId: Long? = null,

    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    @SerialName("noAddebMoreData")
    val noMoreChargeData: String? = null,

    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    @SerialName("noAddebMoreUsrId")
    val noMoreChargeUserId: String? = null,

    @SerialName("fattMoraId")
    val lateFeeInvoiceId: Long? = null,

    @SerialName("moraAddFlg")
    val lateFeeAdditionFlag: Int? = null,

    @SerialName("moraCount")
    val lateFeeCount: Long? = null
)

@Serializable
data class Esse3Refunds(
    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    @SerialName("codElabRimb")
    val refundProcessingCode: String? = null,

    @SerialName("numMandatoRimb")
    val refundMandateNumber: String? = null,

    @SerialName("cauRimbCod")
    val refundReasonCode: String? = null,

    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    @SerialName("dataElab")
    val processingDate: String? = null,

    @SerialName("desMav1")
    val mav1Description: String? = null,

    @SerialName("desMav2")
    val mav2Description: String? = null,

    @SerialName("fattErrataId")
    val erroneousInvoiceId: Long? = null,

    @SerialName("fattAnnullata")
    val canceledInvoice: Long? = null,

    @SerialName("pagId")
    val paymentId: Long? = null,

    @SerialName("importoPag")
    val paidAmount: Double? = null,

    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    @SerialName("dataAccredito")
    val creditDate: String? = null,

    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    @SerialName("notaRimb")
    val refundNote: String? = null,

    @SerialName("rimborsatoFlg")
    val refundedFlag: Int? = null
)

@Serializable
data class Esse3ExemptionData(
    @SerialName("annoAccademico")
    val academicYear: Long,

    @SerialName("codFis")
    val fiscalCode: String,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("esonero")
    val exemption: String,

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
    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("ragioneSociale")
    val companyName: String? = null,

    @SerialName("indirizzo")
    val address: String? = null,

    @SerialName("civico")
    val streetNumber: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("localita")
    val locality: String? = null,

    @SerialName("provincia")
    val province: String? = null,

    @SerialName("nazione")
    val nation: String? = null,

    @SerialName("aaDebito")
    val academicYearDebt: Long? = null,

    @SerialName("codDominioPrincipale")
    val mainDomainCode: String? = null,

    @SerialName("codApplicazione")
    val applicationCode: String? = null,

    @SerialName("iuv")
    val iuv: String? = null,

    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    @SerialName("codiceVersamentoEnte")
    val entityPaymentCode: Long? = null,

    @SerialName("importoVersamento")
    val paymentAmount: Double? = null,

    @SerialName("causaleVersamento")
    val paymentReason: String? = null,

    @SerialName("codUoVersamento")
    val paymentUnitCode: String? = null,

    @SerialName("importoSingoloVersamento")
    val singlePaymentAmount: Double? = null,

    @SerialName("causaleSingoloVersamento")
    val singlePaymentReason: String? = null,

    @SerialName("codTassonomia")
    val taxonomyCode: String? = null,

    @SerialName("desTassonomia")
    val taxonomyDescription: String? = null,

    @SerialName("codUoSingoloVersamento")
    val singlePaymentUnitCode: String? = null,

    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    @SerialName("iur")
    val iur: String? = null,

    @SerialName("codDominioSingoloVersamento")
    val singlePaymentDomainCode: String? = null,

    @SerialName("ibanAccredito")
    val creditIban: String? = null,

    @SerialName("ibanAppoggio")
    val supportIban: String? = null,

    @SerialName("codFlussoRendicontazione")
    val reportingFlowCode: String? = null
)

@Serializable
data class Esse3InvoiceInsert(
    @SerialName("aaId")
    val academicYearId: Int,

    @SerialName("stuId")
    val studentId: Long,

    @SerialName("escludiDaRicalcolo")
    val excludeFromRecalculation: Boolean? = null,

    @SerialName("notaEsclusioneRicalcolo")
    val exclusionFromRecalculationNote: String? = null,

    @SerialName("tasse")
    val taxes: List<Esse3TuitionFee> = emptyList()
)

@Serializable
data class Esse3TrafficLight(
    @SerialName("semaforo")
    val trafficLight: String? = null,

    @SerialName("importoDovuto")
    val dueAmount: Double? = null,

    @SerialName("tasseScadute")
    val expiredTaxes: List<Esse3TuitionFees> = emptyList(),

    @SerialName("tasseDovute")
    val dueTaxes: List<Esse3TuitionFees> = emptyList()
)

@Serializable
data class Esse3TuitionFees(
    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("tassaId")
    val taxId: Long? = null,

    @SerialName("tassaCod")
    val taxCode: String? = null,

    @SerialName("tassaDes")
    val taxDescription: String? = null,

    @SerialName("voceId")
    val itemId: Long? = null,

    @SerialName("voceCod")
    val itemCode: String? = null,

    @SerialName("voceDes")
    val itemDescription: String? = null,

    @SerialName("importoVoce")
    val itemAmount: Double? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    @SerialName("dataPagTollerataMax")
    val maximumToleratedPaymentDate: String? = null
)

@Serializable
data class Esse3TuitionFee(
    @SerialName("tassaCod")
    val taxCode: String,

    @SerialName("voci")
    val items: List<Esse3InvoiceItem> = emptyList()
)

@Serializable
data class Esse3SelfCertificationComponents(
    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFiscale")
    val fiscalCode: String? = null,

    @SerialName("parentela")
    val relationship: String? = null,

    @SerialName("redditi")
    val incomes: List<Esse3SelfCertificationIncome> = emptyList()
)

@Serializable
data class Esse3CancelInvoiceResponse(
    @SerialName("esitoAnnullamento")
    val cancellationOutcome: Long? = null,

    @SerialName("returnMessage")
    val returnMessage: String? = null
)

@Serializable
data class Esse3StudentDebit(
    @SerialName("rimborsatoFlg")
    val refundedFlag: Int? = null,

    @SerialName("notaRimb")
    val refundNote: String? = null,

    @SerialName("moraCount")
    val lateFeeCount: Long? = null,

    @SerialName("moraAddFlg")
    val lateFeeAdditionFlag: Int? = null,

    @SerialName("fattMoraId")
    val lateFeeInvoiceId: Long? = null,

    @SerialName("noAddebMoreUsrId")
    val noMoreChargeUserId: String? = null,

    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    @SerialName("noAddebMoreData")
    val noMoreChargeData: String? = null,

    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    @SerialName("rendicontoId")
    val reportId: Long? = null,

    @SerialName("nBollettino")
    val bulletinNumber: String? = null,

    @SerialName("iur")
    val iur: String? = null,

    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    @SerialName("iuv")
    val iuv: String? = null,

    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    @SerialName("dataAccredito")
    val creditDate: String? = null,

    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    @SerialName("pagatoFlg")
    val paidFlag: Int? = null,

    @SerialName("importoPag")
    val paidAmount: Double? = null,

    @SerialName("pagId")
    val paymentId: Long? = null,

    @SerialName("fattAnnullata")
    val canceledInvoice: Long? = null,

    @SerialName("fattErrataId")
    val erroneousInvoiceId: Long? = null,

    @SerialName("desMav2")
    val mav2Description: String? = null,

    @SerialName("desMav1")
    val mav1Description: String? = null,

    @SerialName("dataElab")
    val processingDate: String? = null,

    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    @SerialName("fattScadutaFlg")
    val expiredInvoiceFlag: Int? = null,

    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    @SerialName("fattCod")
    val invoiceCode: String? = null,

    @SerialName("cauRimbCod")
    val refundReasonCode: String? = null,

    @SerialName("numMandatoRimb")
    val refundMandateNumber: String? = null,

    @SerialName("codElabRimb")
    val refundProcessingCode: String? = null,

    @SerialName("annullataFlg")
    val canceledFlag: Int? = null,

    @SerialName("notaCalcolo")
    val calculationNote: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    @SerialName("scadutoFlg")
    val expiredFlag: Int? = null,

    @SerialName("scadenzaAddebito")
    val chargeExpiration: String? = null,

    @SerialName("importoVoce")
    val itemAmount: Double? = null,

    @SerialName("rataDes")
    val installmentDescription: String? = null,

    @SerialName("rataId")
    val installmentId: Long? = null,

    @SerialName("voceDes")
    val itemDescription: String? = null,

    @SerialName("voceCod")
    val itemCode: String? = null,

    @SerialName("voceId")
    val itemId: Long? = null,

    @SerialName("tipoVoceCod")
    val itemTypeCode: String? = null,

    @SerialName("combDes")
    val combinationDescription: String? = null,

    @SerialName("combCod")
    val combinationCode: String? = null,

    @SerialName("combId")
    val combinationId: Long? = null,

    @SerialName("tassaDes")
    val taxDescription: String? = null,

    @SerialName("tassaCod")
    val taxCode: String? = null,

    @SerialName("tipoTaxCod")
    val taxTypeCode: String? = null,

    @SerialName("tassaId")
    val taxId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("tipoAd")
    val teachingActivityType: String? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("paDtVersamento")
    val paPaymentDate: String? = null,

    @SerialName("emailAte")
    val universityEmail: String? = null,

    @SerialName("numeroMav")
    val mavNumber: String? = null,

    @SerialName("fattConguaglioId")
    val adjustmentInvoiceId: Long? = null,

    @SerialName("fattContab")
    val accountingInvoice: Int? = null,

    @SerialName("dovuto")
    val due: Double? = null,

    @SerialName("semaforo")
    val trafficLight: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("stuId")
    val studentId: Long? = null
)

@Serializable
data class Esse3TuitionCalculationRequest(
    @SerialName("aaId")
    val academicYearId: Int,

    @SerialName("procCod")
    val procedureCode: String,

    @SerialName("cdsId")
    val courseOfStudyId: Long,

    @SerialName("pdsId")
    val studyPlanId: Long,

    @SerialName("aaOrdId")
    val academicYearOrderId: Int,

    @SerialName("numRate")
    val installmentNumber: Int? = null,

    @SerialName("forzaAddebito")
    val forceCharge: Int? = null,

    @SerialName("simulaFlg")
    val simulationFlag: Int? = null,

    @SerialName("stornaFlg")
    val reverseFlag: Int? = null,

    @SerialName("carrChiusaFlg")
    val careerClosedFlag: Int? = null,

    @SerialName("salvaDettCalcFlg")
    val saveCalculationDetailFlag: Int? = null,

    @SerialName("anniRic")
    val researchYears: Int? = null,

    @SerialName("aaRegTax")
    val academicYearTaxRegulation: Int? = null,

    @SerialName("dataScadRateSosp")
    val suspendedInstallmentDeadline: String? = null
)

@Serializable
data class Esse3ExemptionsList(
    @SerialName("esoneroCod")
    val exemptionCode: String? = null,

    @SerialName("esoneroDes")
    val exemptionDescription: String? = null,

    @SerialName("priorita")
    val priority: Long? = null,

    @SerialName("codiceRifDom")
    val domicileReferenceCode: String? = null,

    @SerialName("dataDomanda")
    val applicationDate: String? = null,

    @SerialName("ottenuto")
    val obtained: String? = null,

    @SerialName("dataOttenimento")
    val obtainmentDate: String? = null,

    @SerialName("annullato")
    val canceled: String? = null,

    @SerialName("dataAnnullamento")
    val cancellationDate: String? = null,

    @SerialName("respinto")
    val rejected: String? = null,

    @SerialName("dataRespingimento")
    val rejectionDate: String? = null,

    @SerialName("motivoRespingimento")
    val rejectionReason: String? = null,

    @SerialName("statoRicorsoCod")
    val appealStateCode: String? = null,

    @SerialName("statoRicorsoDes")
    val appealStateDescription: String? = null,

    @SerialName("motivoStatoRic")
    val requestStateReason: String? = null,

    @SerialName("dataRicorso")
    val appealDate: String? = null,

    @SerialName("dataPresDoc")
    val documentPresenceDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null,

    @SerialName("usrForzatura")
    val forceUser: String? = null,

    @SerialName("dataForzatura")
    val forceDate: String? = null
)

@Serializable
data class Esse3SpgTransactionStatusData(
    @SerialName("eventDate")
    val eventDate: String,

    @SerialName("channel")
    val channel: String,

    @SerialName("cart")
    val cart: Esse3Cart
)

@Serializable
data class Esse3MessageOutcomeResponse(
    @SerialName("esito")
    val outcome: Int? = null,

    @SerialName("returnMessage")
    val returnMessage: String? = null
)

@Serializable
data class Esse3CumulativeExemptions(
    @SerialName("esoneroCod")
    val exemptionCode: String? = null,

    @SerialName("esoneroDes")
    val exemptionDescription: String? = null
)

@Serializable
data class Esse3RejectExemption(
    @SerialName("annoAccademico")
    val academicYear: Long,

    @SerialName("codFis")
    val fiscalCode: String,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("esonero")
    val exemption: String,

    @SerialName("motivazione")
    val reason: String? = null
)

@Serializable
data class Esse3CartItem(
    @SerialName("cartItemNum")
    val cartItemNumber: Long? = null,

    @SerialName("cartItemExtId")
    val cartItemExternalId: Long? = null,

    @SerialName("cartItemStatusCod")
    val cartItemStatusCode: String? = null,

    @SerialName("cartItemStatusDes")
    val cartItemStatusDescription: String? = null,

    @SerialName("amount")
    val amount: Double? = null,

    @SerialName("currency")
    val currency: String? = null,

    @SerialName("paymentDate")
    val paymentDate: String? = null,

    @SerialName("channelPaymentId")
    val paymentChannelId: String? = null,

    @SerialName("statusChanged")
    val statusChanged: Boolean? = null
)

@Serializable
data class Esse3StudentExemptions(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("iscrId")
    val enrollmentId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("extStuCod")
    val externalStudentCode: String? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    @SerialName("annoCorso")
    val courseYear: Long? = null,

    @SerialName("anniFc")
    val fcYears: Long? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("esoneroIscrCod")
    val enrollmentExemptionCode: String? = null,

    @SerialName("esoneroIscrDes")
    val enrollmentExemptionDescription: String? = null,

    @SerialName("fasciaRedditoId")
    val incomeBandId: Long? = null,

    @SerialName("fasciaRedditoNum")
    val incomeBandNumber: Long? = null,

    @SerialName("fasciaRedditoDes")
    val incomeBandDescription: String? = null,

    @SerialName("partTime")
    val partTime: String? = null,

    @SerialName("listaEsoneri")
    val exemptionList: List<Esse3ExemptionsList> = emptyList()
)

@Serializable
data class Esse3SelfCertification(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("autocertId")
    val selfCertificationId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("dataPresentazione")
    val presentationDate: String? = null,

    @SerialName("dataCaricamento")
    val uploadDate: String? = null,

    @SerialName("dataUltimaModif")
    val lastModificationDate: String? = null,

    @SerialName("isee")
    val isee: Double? = null,

    @SerialName("ispe")
    val ispe: Double? = null,

    @SerialName("fasciaId")
    val bandId: Long? = null,

    @SerialName("fasciaNum")
    val bandNumber: Long? = null,

    @SerialName("fasciaDes")
    val bandDescription: String? = null,

    @SerialName("dataCartaceo")
    val paperDate: String? = null,

    @SerialName("versione")
    val version: String? = null,

    @SerialName("reddNondichFlg")
    val undeclaredIncomeFlag: Int? = null,

    @SerialName("noVerifReddFlg")
    val noIncomeVerificationFlag: Int? = null,

    @SerialName("modDopoPresFlg")
    val postPresenceModeFlag: Int? = null,

    @SerialName("nuclAutonomoFlg")
    val autonomousNucleusFlag: Int? = null,

    @SerialName("dataStampaVerb")
    val minutesPrintDate: String? = null,

    @SerialName("iseeuDichId")
    val iseeuDeclarationId: Long? = null,

    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    @SerialName("dataSottoscr")
    val subscriptionDate: String? = null,

    @SerialName("errataFlg")
    val errataFlag: Int? = null,

    @SerialName("protCaaf")
    val caafProtocol: String? = null,

    @SerialName("tipologiaIsee")
    val iseeTypology: String? = null,

    @SerialName("difformitaFlg")
    val discrepancyFlag: Int? = null,

    @SerialName("protIsee")
    val iseeProtocol: String? = null,

    @SerialName("dataRilIsee")
    val iseeReleaseDate: String? = null,

    @SerialName("iseeCorrenteFlg")
    val currentIseeFlag: Int? = null,

    @SerialName("dataRilIseeSost")
    val substituteIseeReleaseDate: String? = null,

    @SerialName("protDsuSost")
    val substituteDsuProtocol: String? = null,

    @SerialName("identificatoreFlusso")
    val flowIdentifier: String? = null,

    @SerialName("forzaturaId")
    val forceId: Int? = null,

    @SerialName("forzaturaDes")
    val forceDescription: String? = null,

    @SerialName("componenti")
    val components: List<Esse3SelfCertificationComponents> = emptyList()
)

@Serializable
data class Esse3Item(
    @SerialName("codTassonomia")
    val taxonomyCode: String? = null,

    @SerialName("ibanAccredito")
    val creditIban: String? = null,

    @SerialName("ibanAppoggio")
    val supportIban: String? = null,

    @SerialName("importoVoce")
    val itemAmount: Double? = null,

    @SerialName("voceCod")
    val itemCode: String? = null,

    @SerialName("voceDes")
    val itemDescription: String? = null
)

@Serializable
data class Esse3InvoiceAttachmentMetadata(
    @SerialName("filename")
    val fileName: String,

    @SerialName("autore")
    val author: String? = null,

    @SerialName("titolo")
    val title: String,

    @SerialName("descrizione")
    val description: String,

    @SerialName("tipologiaAllegato")
    val attachmentTypology: String? = null,

    @SerialName("validoFlg")
    val validFlag: Int,

    @SerialName("abilVisWeb")
    val webVisibility: Int
)

@Serializable
data class Esse3CollectionData(
    @SerialName("idTransazione")
    val transactionId: Long,

    @SerialName("fattId")
    val invoiceId: Long,

    @SerialName("importo")
    val amount: Double,

    @SerialName("dataPagamento")
    val paymentDate: String,

    @SerialName("provenienza")
    val origin: String
)

@Serializable
data class Esse3Transaction(
    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("iuv")
    val iuv: String? = null,

    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    @SerialName("importo")
    val amount: Double? = null,

    @SerialName("dataTransazione")
    val transactionDate: String? = null,

    @SerialName("esitoCod")
    val outcomeCode: String? = null,

    @SerialName("esitoDes")
    val outcomeDescription: String? = null,

    @SerialName("stato")
    val state: String? = null,

    @SerialName("statoFinale")
    val finalState: Int? = null,

    @SerialName("esitoTransazione")
    val transactionOutcome: String? = null,

    @SerialName("esitoMessaggio")
    val messageOutcome: List<Esse3MessageOutcome> = emptyList(),

    @SerialName("iur")
    val iur: String? = null,

    @SerialName("importoPagato")
    val paidAmount: Double? = null,

    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    @SerialName("esisteIncasso")
    val collectionExists: Int? = null,

    @SerialName("incassoDaGestire")
    val collectionToManage: Int? = null,

    @SerialName("erroreIncasso")
    val collectionError: String? = null,

    @SerialName("codiceContestoPagamento")
    val paymentContextCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("pagatoFlg")
    val paidFlag: Int? = null,

    @SerialName("quietanzaStampabile")
    val printableReceipt: Int? = null
)

@Serializable
data class Esse3PostCollectionResponse(
    @SerialName("returnCode")
    val returnCode: Long? = null,

    @SerialName("returnMessage")
    val returnMessage: String? = null
)

@Serializable
data class Esse3PagoPATransactionResponse(
    @SerialName("redirectUrlPagoPA")
    val pagopaRedirectUrl: String? = null
)

@Serializable
data class Esse3Cart(
    @SerialName("transId")
    val transactionId: String? = null,

    @SerialName("cartStatusCod")
    val cartStatusCode: String? = null,

    @SerialName("cartStatusDes")
    val cartStatusDescription: String? = null,

    @SerialName("cartItem")
    val cartItem: List<Esse3CartItem> = emptyList()
)

@Serializable
data class Esse3TuitionCalculationDetail(
    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("combCod")
    val combinationCode: String? = null,

    @SerialName("tassaCod")
    val taxCode: String? = null,

    @SerialName("voceCod")
    val itemCode: String? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null,

    @SerialName("importo")
    val amount: Double? = null,

    @SerialName("rataId")
    val installmentId: Int? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    @SerialName("stornoflg")
    val reverseFlag: Int? = null
)

@Serializable
data class Esse3InvoicePaymentData(
    @SerialName("fattId")
    val invoiceId: Long,

    @SerialName("importo")
    val amount: Double,

    @SerialName("dataPagamento")
    val paymentDate: String,

    @SerialName("tipoPagamento")
    val paymentType: String,

    @SerialName("numBolletino")
    val bulletinNumber: String? = null,

    @SerialName("ripartAutoErrPag")
    val autoRepartitionErrorPayment: Int? = null,

    @SerialName("convalidaPagamento")
    val paymentValidation: Int? = null,

    @SerialName("dataAccredito")
    val creditDate: String? = null,

    @SerialName("annoFinanziario")
    val financialYear: Long? = null,

    @SerialName("contoCorrenteId")
    val currentAccountId: Long? = null
)

@Serializable
data class Esse3StudentEnrollment(
    @SerialName("iscrReg")
    val regularEnrollment: String? = null,

    @SerialName("idTipoLaurea")
    val degreeTypeId: String? = null,

    @SerialName("corsoSpecObb")
    val mandatorySpecializationCourse: String? = null,

    @SerialName("corsoSpecMed")
    val medicalSpecializationCourse: String? = null,

    @SerialName("dottBorsa")
    val phdScholarship: String? = null,

    @SerialName("dipDes")
    val departmentDescription: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("cdsClasseCod")
    val courseOfStudyClassCode: String? = null,

    @SerialName("cdsClasseDes")
    val courseOfStudyClassDescription: String? = null,

    @SerialName("comuneSedeDes")
    val seatMunicipalityDescription: String? = null,

    @SerialName("comuneSedeBelfioreCod")
    val seatMunicipalityBelfioreCode: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("annoCorso")
    val courseYear: Long? = null,

    @SerialName("staIscrCod")
    val enrollmentStatusCode: String? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("anniFc")
    val fcYears: Long? = null,

    @SerialName("aaPrimaIscr")
    val academicYearFirstEnrollment: Long? = null,

    @SerialName("nCfuDr1")
    val dr1CfuNumber: Double? = null,

    @SerialName("mediaPesataDr1")
    val weightedAverageDr1: Double? = null,

    @SerialName("staMatCod")
    val matStatusCode: String? = null,

    @SerialName("motStamatCod")
    val matStatusReasonCode: String? = null,

    @SerialName("nCfuConvDr1")
    val dr1ConversionCfuNumber: Double? = null,

    @SerialName("aaCfuConvDr1")
    val academicYearCFUConversionDr1: Long? = null,

    @SerialName("tcCfuConvDr1")
    val tcDr1ConversionCfu: String? = null,

    @SerialName("aaConsTitEq")
    val academicYearEquivalentTitleConsent: Long? = null,

    @SerialName("tipoTitMiurEq")
    val miurEquivalentTitleType: String? = null,

    @SerialName("titEqConsInAte")
    val equivalentTitleConsolidatedInAte: String? = null,

    @SerialName("iscrCorsoFit")
    val fitCourseEnrollment: String? = null,

    @SerialName("bandoMobint")
    val mobilityCall: String? = null,

    @SerialName("variazioneCarriera")
    val careerVariation: String? = null,

    @SerialName("nCfuDr2")
    val dr2CfuNumber: Double? = null,

    @SerialName("nCfuDr3")
    val dr3CfuNumber: Double? = null,

    @SerialName("nCfuDr4")
    val dr4CfuNumber: Double? = null,

    @SerialName("aaConsTit")
    val academicYearTitleConsent: Long? = null,

    @SerialName("iscrCorsoStem")
    val stemCourseEnrollment: String? = null,

    @SerialName("durataCorso")
    val courseDuration: Long? = null,

    @SerialName("staStuCod")
    val studentStatusCode: String? = null,

    @SerialName("motStaStuCod")
    val studentStatusReasonCode: String? = null,

    @SerialName("aaInizioCarriera")
    val academicYearCareerStart: Long? = null,

    @SerialName("aaFineCarriera")
    val academicYearCareerEnd: Long? = null,

    @SerialName("dataChiusuraCarriera")
    val careerClosingDate: String? = null,

    @SerialName("dataConsTit")
    val titleDeliveryDate: String? = null,

    @SerialName("laureatoNeiTermini")
    val graduatedWithinTerms: String? = null
)

@Serializable
data class Esse3MeritData(
    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("dataNascita")
    val birthDate: String? = null,

    @SerialName("stuIscr")
    val studentEnrollment: List<Esse3StudentEnrollment> = emptyList()
)

@Serializable
data class Esse3PagoPATransaction(
    @SerialName("fattId")
    val invoiceId: Long,

    @SerialName("returnURL")
    val returnURL: String
)

@Serializable
data class Esse3MessageOutcome(
    @SerialName("lingua")
    val language: String? = null,

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
    @SerialName("tipoAd")
    val teachingActivityType: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("tassaId")
    val taxId: Long? = null,

    @SerialName("tipoTaxCod")
    val taxTypeCode: String? = null,

    @SerialName("tassaCod")
    val taxCode: String? = null,

    @SerialName("tassaDes")
    val taxDescription: String? = null,

    @SerialName("combId")
    val combinationId: Long? = null,

    @SerialName("combCod")
    val combinationCode: String? = null,

    @SerialName("combDes")
    val combinationDescription: String? = null,

    @SerialName("tipoVoceCod")
    val itemTypeCode: String? = null,

    @SerialName("voceId")
    val itemId: Long? = null,

    @SerialName("voceCod")
    val itemCode: String? = null,

    @SerialName("voceDes")
    val itemDescription: String? = null,

    @SerialName("rataId")
    val installmentId: Long? = null,

    @SerialName("rataDes")
    val installmentDescription: String? = null,

    @SerialName("importoVoce")
    val itemAmount: Double? = null,

    @SerialName("scadenzaAddebito")
    val chargeExpiration: String? = null,

    @SerialName("scadutoFlg")
    val expiredFlag: Int? = null,

    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    @SerialName("note")
    val notes: String? = null,

    @SerialName("notaCalcolo")
    val calculationNote: String? = null,

    @SerialName("annullataFlg")
    val canceledFlag: Int? = null,

    @SerialName("codElabRimb")
    val refundProcessingCode: String? = null,

    @SerialName("numMandatoRimb")
    val refundMandateNumber: String? = null,

    @SerialName("cauRimbCod")
    val refundReasonCode: String? = null,

    @SerialName("fattCod")
    val invoiceCode: String? = null,

    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    @SerialName("fattScadutaFlg")
    val expiredInvoiceFlag: Int? = null,

    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    @SerialName("dataElab")
    val processingDate: String? = null,

    @SerialName("desMav1")
    val mav1Description: String? = null,

    @SerialName("desMav2")
    val mav2Description: String? = null,

    @SerialName("fattErrataId")
    val erroneousInvoiceId: Long? = null,

    @SerialName("fattAnnullata")
    val canceledInvoice: Long? = null,

    @SerialName("pagId")
    val paymentId: Long? = null,

    @SerialName("importoPag")
    val paidAmount: Double? = null,

    @SerialName("pagatoFlg")
    val paidFlag: Int? = null,

    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    @SerialName("dataAccredito")
    val creditDate: String? = null,

    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    @SerialName("iuv")
    val iuv: String? = null,

    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    @SerialName("iur")
    val iur: String? = null,

    @SerialName("nBollettino")
    val bulletinNumber: String? = null,

    @SerialName("rendicontoId")
    val reportId: Long? = null,

    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    @SerialName("noAddebMoreData")
    val noMoreChargeData: String? = null,

    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    @SerialName("noAddebMoreUsrId")
    val noMoreChargeUserId: String? = null,

    @SerialName("fattMoraId")
    val lateFeeInvoiceId: Long? = null,

    @SerialName("moraAddFlg")
    val lateFeeAdditionFlag: Int? = null,

    @SerialName("moraCount")
    val lateFeeCount: Long? = null,

    @SerialName("notaRimb")
    val refundNote: String? = null,

    @SerialName("rimborsatoFlg")
    val refundedFlag: Int? = null,

    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("paDtVersamento")
    val paPaymentDate: String? = null,

    @SerialName("numeroMav")
    val mavNumber: String? = null
)

@Serializable
data class Esse3ValidExemptionsAcademicYear(
    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("esoneroCod")
    val exemptionCode: String? = null,

    @SerialName("esoneroDes")
    val exemptionDescription: String? = null,

    @SerialName("priorita")
    val priority: Long? = null,

    @SerialName("richiedibile")
    val requestable: String? = null,

    @SerialName("prereqRichiedId")
    val prerequisiteRequiredId: Long? = null,

    @SerialName("prereqRichiedCod")
    val prerequisiteRequiredCode: String? = null,

    @SerialName("esoneriCumulati")
    val cumulatedExemptions: List<Esse3CumulativeExemptions> = emptyList(),

    @SerialName("desBeneficio")
    val benefitDescription: String? = null,

    @SerialName("documentazione")
    val documentation: String? = null,

    @SerialName("tipoScadPresDomanda")
    val applicationPresenceDeadlineType: String? = null,

    @SerialName("codiceEsterno")
    val externalCode: String? = null,

    @SerialName("esoneroAnnullabile")
    val cancellableExemption: String? = null,

    @SerialName("tipoValutazione")
    val evaluationType: String? = null,

    @SerialName("frequenzaCampionamento")
    val samplingFrequency: Long? = null,

    @SerialName("condCambioValutazioneId")
    val gradeChangeConditionId: Long? = null,

    @SerialName("condCambioValutazioneCod")
    val gradeChangeConditionCode: String? = null,

    @SerialName("iseeMax")
    val maxIsee: Double? = null,

    @SerialName("ispeMax")
    val maxIspe: Double? = null,

    @SerialName("redditoEquivalente")
    val equivalentIncome: Double? = null,

    @SerialName("condValutazioneId")
    val gradeConditionId: Long? = null,

    @SerialName("condValutazioneCod")
    val gradeConditionCode: String? = null,

    @SerialName("condValutazioneAltId")
    val alternativeGradeConditionId: Long? = null,

    @SerialName("condValutazioneAltCod")
    val alternativeGradeConditionCode: String? = null,

    @SerialName("grpLivStruttDidId")
    val groupDidacticStructureLevelId: Long? = null,

    @SerialName("grpLivStruttDidCod")
    val groupDidacticStructureLevelCode: String? = null,

    @SerialName("grpLivStruttDidDes")
    val groupDidacticStructureLevelDescription: String? = null,

    @SerialName("fasciaMeritoId")
    val meritBandId: Long? = null,

    @SerialName("fasciaMeritoNum")
    val meritBandNumber: Long? = null,

    @SerialName("fasciaMeritoDes")
    val meritBandDescription: String? = null,

    @SerialName("fasciaRedditoId")
    val incomeBandId: Long? = null,

    @SerialName("fasciaRedditoNum")
    val incomeBandNumber: Long? = null,

    @SerialName("fasciaRedditoDes")
    val incomeBandDescription: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrModId")
    val modificationUserId: String? = null,

    @SerialName("dataMod")
    val modificationDate: String? = null
)

@Serializable
data class Esse3ScholarshipData(
    @SerialName("codFis")
    val fiscalCode: String,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("matricola")
    val matricola: String? = null,

    @SerialName("cdsCod")
    val courseOfStudyCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("facCod")
    val facultyCode: String? = null,

    @SerialName("tipoIscr")
    val enrollmentType: String? = null,

    @SerialName("dataDomanda")
    val applicationDate: String? = null,

    @SerialName("esito")
    val outcome: String
)

@Serializable
data class Esse3Payment(
    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("ragioneSociale")
    val companyName: String? = null,

    @SerialName("indirizzo")
    val address: String? = null,

    @SerialName("civico")
    val streetNumber: String? = null,

    @SerialName("cap")
    val postalCode: String? = null,

    @SerialName("localita")
    val locality: String? = null,

    @SerialName("provincia")
    val province: String? = null,

    @SerialName("nazione")
    val nation: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("telefono")
    val phone: String? = null,

    @SerialName("cellulare")
    val mobilePhone: String? = null,

    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    @SerialName("iuv")
    val iuv: String? = null,

    @SerialName("importoVersamento")
    val paymentAmount: Double? = null,

    @SerialName("causale")
    val reason: String? = null,

    @SerialName("dataScadFatt")
    val invoiceDeadline: String? = null,

    @SerialName("annullato")
    val canceled: Int? = null,

    @SerialName("pagato")
    val paid: Int? = null,

    @SerialName("dataVersamento")
    val paymentDate: String? = null,

    @SerialName("dataVerificaVersamento")
    val paymentVerificationDate: String? = null,

    @SerialName("dataAnnullamentoVersamento")
    val paymentCancellationDate: String? = null,

    @SerialName("dataNotificaPagamento")
    val paymentNotificationDate: String? = null,

    @SerialName("dataStampaAvviso")
    val noticePrintDate: String? = null,

    @SerialName("esitoStampaAvviso")
    val noticePrintOutcome: String? = null,

    @SerialName("voci")
    val items: List<Esse3Item> = emptyList()
)

@Serializable
data class Esse3SelfCertificationIncome(
    @SerialName("tipoRedditoId")
    val incomeTypeId: Long? = null,

    @SerialName("tipiRedditiCod")
    val incomeTypesCode: String? = null,

    @SerialName("tipiRedditiDes")
    val incomeTypesDescription: String? = null,

    @SerialName("tipoDato")
    val dataType: String? = null,

    @SerialName("importo")
    val amount: Double? = null,

    @SerialName("valoreFlg")
    val valueFlag: Int? = null,

    @SerialName("valoreTesto")
    val textValue: String? = null,

    @SerialName("valoreData")
    val dateValue: String? = null
)

@Serializable
data class Esse3EnrollmentForTuition(
    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("cdsId")
    val courseOfStudyId: Long? = null,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("cdsDes")
    val courseOfStudyDescription: String? = null,

    @SerialName("aaOrdId")
    val academicYearOrderId: Long? = null,

    @SerialName("pdsId")
    val studyPlanId: Long? = null,

    @SerialName("aaIscrId")
    val academicYearEnrollmentId: Long? = null,

    @SerialName("annoCorso")
    val courseYear: Long? = null,

    @SerialName("anniFc")
    val fcYears: Long? = null,

    @SerialName("tipoIscrCod")
    val enrollmentTypeCode: String? = null,

    @SerialName("tipoIscrCodDes")
    val enrollmentTypeCodeDescription: String? = null,

    @SerialName("fasciaId")
    val bandId: Long? = null,

    @SerialName("partTime")
    val partTime: String? = null,

    @SerialName("isee")
    val isee: Double? = null,

    @SerialName("durataAnni")
    val durationYears: Int? = null,

    @SerialName("tipoEsoCod")
    val exemptionTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("pesi")
    val weights: Double? = null,

    @SerialName("anniCarr")
    val careerYears: Double? = null,

    @SerialName("merito")
    val merit: String? = null,

    @SerialName("frequenza")
    val attendance: String? = null,

    @SerialName("sogliaCfu")
    val creditsThreshold: Double? = null,

    @SerialName("sogliaIsee")
    val iseeThreshold: Double? = null
)

@Serializable
data class Esse3ScholarshipOutcomeData(
    @SerialName("annoAccademico")
    val academicYear: Long,

    @SerialName("tipoRichiesta")
    val requestType: Long,

    @SerialName("datiBorse")
    val scholarshipData: List<Esse3ScholarshipData> = emptyList()
)

@Serializable
data class Esse3ExemptionDataError(
    @SerialName("errore")
    val error: String? = null
)

@Serializable
data class Esse3Invoices(
    @SerialName("visWebFlg")
    val webVisibleFlag: Int? = null,

    @SerialName("pagopaImmediato")
    val pagopaImmediate: Int? = null,

    @SerialName("pagopaAvviso")
    val pagopaNotice: Int? = null,

    @SerialName("pagopaEnabled")
    val pagopaEnabled: Int? = null,

    @SerialName("paDtVersamento")
    val paPaymentDate: String? = null,

    @SerialName("infoAggiuntive")
    val additionalInfo: String? = null,

    @SerialName("numeroMav")
    val mavNumber: String? = null,

    @SerialName("persId")
    val personId: Long? = null,

    @SerialName("pagatoFlg")
    val paidFlag: Int? = null,

    @SerialName("fattAnnullata")
    val canceledInvoice: Long? = null,

    @SerialName("fattErrataId")
    val erroneousInvoiceId: Long? = null,

    @SerialName("dataElab")
    val processingDate: String? = null,

    @SerialName("dataEmissione")
    val issuanceDate: String? = null,

    @SerialName("fattScadutaFlg")
    val expiredInvoiceFlag: Int? = null,

    @SerialName("moraCount")
    val lateFeeCount: Long? = null,

    @SerialName("moraAddFlg")
    val lateFeeAdditionFlag: Int? = null,

    @SerialName("fattMoraId")
    val lateFeeInvoiceId: Long? = null,

    @SerialName("noAddebMoreUsrId")
    val noMoreChargeUserId: String? = null,

    @SerialName("noAddebMoreNota")
    val noMoreChargeNote: String? = null,

    @SerialName("noAddebMoreData")
    val noMoreChargeData: String? = null,

    @SerialName("noAddebMoreFlg")
    val noMoreChargeFlag: Int? = null,

    @SerialName("regManFlg")
    val manualRegistrationFlag: Int? = null,

    @SerialName("rendicontoId")
    val reportId: Long? = null,

    @SerialName("nBollettino")
    val bulletinNumber: String? = null,

    @SerialName("iur")
    val iur: String? = null,

    @SerialName("codiceAvviso")
    val noticeCode: String? = null,

    @SerialName("iuv")
    val iuv: String? = null,

    @SerialName("incassatoDa")
    val collectedBy: String? = null,

    @SerialName("dataAccredito")
    val creditDate: String? = null,

    @SerialName("dataNotifica")
    val notificationDate: String? = null,

    @SerialName("dataPagamento")
    val paymentDate: String? = null,

    @SerialName("importoPag")
    val paidAmount: Double? = null,

    @SerialName("pagId")
    val paymentId: Long? = null,

    @SerialName("desMav2")
    val mav2Description: String? = null,

    @SerialName("desMav1")
    val mav1Description: String? = null,

    @SerialName("fattId")
    val invoiceId: Long? = null,

    @SerialName("importoFattura")
    val invoiceAmount: Double? = null,

    @SerialName("scadFattura")
    val invoiceExpiration: String? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("codFis")
    val fiscalCode: String? = null,

    @SerialName("cognome")
    val surname: String? = null,

    @SerialName("nome")
    val name: String? = null
)
