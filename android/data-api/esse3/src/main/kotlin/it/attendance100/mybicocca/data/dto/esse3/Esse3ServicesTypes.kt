package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3IdentityDocumentType(
    @SerialName("docIdentTipoCod")
    val identityDocumentTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3AddressType(
    @SerialName("tipoIndirizCod")
    val addressTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3PaymentRefundType(
    @SerialName("tipoRimbPagCod")
    val paymentRefundTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("rimbPag")
    val refundPayment: Int? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("visEmissFlg")
    val issuanceVisibleFlag: Int? = null,

    @SerialName("visAcqPagFlg")
    val paymentAcquisitionVisibleFlag: Int? = null,

    @SerialName("tipoRimbPagCodAte")
    val atePaymentRefundTypeCode: String? = null,

    @SerialName("stampabileFlg")
    val printableFlag: Int? = null,

    @SerialName("stampabileDaWebFlg")
    val webPrintableFlag: Int? = null,

    @SerialName("numBollModifFlg")
    val modifiableBulletinNumberFlag: Int? = null,

    @SerialName("elabEmissFlg")
    val processingIssuanceFlag: Int? = null,

    @SerialName("baseNumeroLottoMav")
    val mavBatchNumberBase: String? = null,

    @SerialName("maxProgrLottoMav")
    val maxMavBatchProgress: Long? = null,

    @SerialName("webFlg")
    val webFlag: Int? = null,

    @SerialName("abilInsDettBancaWeb")
    val webBankDetailsInsertionAuthorization: Int? = null,

    @SerialName("ugovCdModPag")
    val uGovPaymentMethodCode: String? = null,

    @SerialName("defElabFlgPagMan")
    val manualPaymentProcessingDefinitionFlag: Int? = null,

    @SerialName("abilInsDettBanca")
    val bankDetailsInsertionAuthorization: Int? = null,

    @SerialName("tipologiaLottoMav")
    val mavBatchTypology: String? = null,

    @SerialName("pagNodoFlg")
    val paymentNodeFlag: Int? = null,

    @SerialName("pagEnteEstFlg")
    val externalEntityPaymentFlag: Int? = null,

    @SerialName("cartaDocFlg")
    val documentCardFlag: Int? = null,

    @SerialName("esclEntratelFlg")
    val excludeEntratelFlag: Int? = null,

    @SerialName("codServizio")
    val serviceCode: String? = null,

    @SerialName("codSottoservizio")
    val subServiceCode: String? = null,

    @SerialName("paCcId")
    val paCurrentAccountId: Long? = null,

    @SerialName("abilPagRest")
    val remainingPaymentAuthorization: Int? = null
)

@Serializable
data class Esse3CodeToIdTranslatorRequestObject(
    @SerialName("type")
    val type: String,

    @SerialName("cod")
    val code: String,

    @SerialName("cods")
    val codes: List<String> = emptyList()
)

@Serializable
data class Esse3ConfigurationParameter(
    @SerialName("parCod")
    val parameterCode: String,

    @SerialName("modulo")
    val module: String? = null,

    @SerialName("prodotto")
    val product: String? = null,

    @SerialName("descrizione")
    val description: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("valNum")
    val numericValue: Long? = null,

    @SerialName("valAlfa")
    val alphanumericValue: String? = null
)

@Serializable
data class Esse3MaritalStatusType(
    @SerialName("statoCivileCod")
    val maritalStatusCode: String? = null,

    @SerialName("des")
    val description: String? = null
)

@Serializable
data class Esse3Users(
    @SerialName("userId")
    val userId: String? = null,

    @SerialName("alias")
    val alias: String? = null,

    @SerialName("grpName")
    val groupName: String? = null,

    @SerialName("disableFlg")
    val disableFlag: Int? = null,

    @SerialName("userName")
    val userName: String? = null
)

@Serializable
data class Esse3AliasReturn(
    @SerialName("codiceRitorno")
    val returnCode: Int? = null
)

@Serializable
data class Esse3CodeToIdTranslatorAdditionalId(
    @SerialName("id")
    val id: Long? = null,

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
    @SerialName("linguaId")
    val languageId: Long? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dsFlg")
    val dsFlag: Int? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("iso6392Codice")
    val iso6392Code: String? = null,

    @SerialName("mobilFlg")
    val mobileFlag: Int? = null,

    @SerialName("webMlFlg")
    val webMlFlag: Int? = null,

    @SerialName("mobildes")
    val mobilityDescription: String? = null,

    @SerialName("madrelinguaFlg")
    val motherTongueFlag: Int? = null,

    @SerialName("certFlg")
    val certificateFlag: Int? = null,

    @SerialName("iso6391Codice")
    val iso6391Code: String? = null
)

@Serializable
data class Esse3UserGroup(
    @SerialName("grpId")
    val groupId: Int? = null,

    @SerialName("utenteTecnicoFlg")
    val technicalUserFlag: Boolean? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("tabAna")
    val anaTab: String? = null,

    @SerialName("fieldAna")
    val anaField: String? = null,

    @SerialName("httpSessionTimeout")
    val httpSessionTimeout: Int? = null,

    @SerialName("requestWindowLimit")
    val requestWindowLimit: Int? = null,

    @SerialName("maxRequestWindow")
    val maxRequestWindow: Int? = null
)

@Serializable
data class Esse3RecognitionType(
    @SerialName("tipoRicCod")
    val requestTypeCode: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("sysFlg")
    val systemFlag: Int? = null,

    @SerialName("straFlg")
    val foreignFlag: Int? = null,

    @SerialName("abbrFlg")
    val abbreviationFlag: Int? = null,

    @SerialName("ingrFlg")
    val entryFlag: Int? = null,

    @SerialName("ansTipoRicCod")
    val answerResearchTypeCode: String? = null,

    @SerialName("nota")
    val note: String? = null,

    @SerialName("notaCertFlg")
    val certificateNoteFlag: Int? = null,

    @SerialName("bonusLaurea")
    val graduationBonus: Double? = null,

    @SerialName("dsTipoRicCod")
    val dsResearchTypeCode: String? = null,

    @SerialName("interateFlg")
    val integratedFlag: Int? = null
)

@Serializable
data class Esse3VersionInfo(
    @SerialName("remoteAddress")
    val remoteAddress: String? = null,

    @SerialName("encoding")
    val encoding: String? = null,

    @SerialName("univName")
    val universityName: String? = null,

    @SerialName("ambienteType")
    val environmentType: Esse3EnvironmentType? = null,

    @SerialName("buildId")
    val buildId: String? = null,

    @SerialName("buildTag")
    val buildTag: String? = null,

    @SerialName("buildVersion")
    val buildVersion: String? = null,

    @SerialName("ate")
    val ate: Int? = null,

    @SerialName("servletcontainerInfo")
    val servletContainerInfo: String? = null,

    @SerialName("webappStartTime")
    val webappStartTime: String? = null,

    @SerialName("jmvStartTime")
    val jmvStartTime: String? = null,

    @SerialName("versione")
    val version: String? = null
)

@Serializable
data class Esse3ProcessedLists(
    @SerialName("codice")
    val code: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("aaId")
    val academicYearId: Long? = null,

    @SerialName("tipoElencoCod")
    val listTypeCode: String? = null,

    @SerialName("dataGeneraz")
    val generationDate: String? = null,

    @SerialName("statoElenco")
    val listState: String? = null,

    @SerialName("stuId")
    val studentId: Long? = null,

    @SerialName("persId")
    val personId: String? = null,

    @SerialName("elimFlg")
    val deleteFlag: Int? = null,

    @SerialName("stampaFlg")
    val printFlag: Int? = null
)

@Serializable
data class Esse3Alias(
    @SerialName("userId")
    val userId: String? = null,

    @SerialName("alias")
    val alias: String? = null,

    @SerialName("dataScadenza")
    val expirationDate: String? = null
)

@Serializable
data class Esse3ReferenceYear(
    @SerialName("tipoDataRifCod")
    val referenceDateTypeCode: String,

    @SerialName("tipoCorsoCod")
    val courseTypeCode: String? = null,

    @SerialName("aaId")
    val academicYearId: Int? = null,

    @SerialName("dataRif")
    val referenceDate: String? = null
)

@Serializable
data class Esse3CodeToIdTranslatorResponseObject(
    @SerialName("type")
    val type: String,

    @SerialName("cod")
    val code: String,

    @SerialName("cods")
    val codes: List<String> = emptyList(),

    @SerialName("id")
    val id: Long? = null,

    @SerialName("chiaveComposta")
    val compositeKey: List<Esse3CodeToIdTranslatorAdditionalId> = emptyList(),

    @SerialName("err")
    val error: String? = null
)
