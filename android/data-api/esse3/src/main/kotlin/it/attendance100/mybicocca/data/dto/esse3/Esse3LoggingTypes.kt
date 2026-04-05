package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3TransactionLogSession(
    /** id della sessione */
    @SerialName("sessionid")
    val sessionId: String? = null,

    /** id del livello di log */
    @SerialName("llevel")
    val llevel: Long? = null,

    /** codice del livello di log */
    @SerialName("lcode")
    val lcode: String? = null,

    /** descrizione del livello di log */
    @SerialName("ldesc")
    val ldesc: String? = null,

    /** abilitazione inserimento transazioni. 0 -> non abilitato; 1 -> abilitato */
    @SerialName("addTransactionInfo")
    val addTransactionInfo: Int? = null,

    /** timeout del livello di override */
    @SerialName("llevelMinutesTimeout")
    val llevelMinutesTimeout: Double? = null,

    /** user di inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    /** data di inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null
)

@Serializable
data class Esse3TransactionLogSessionBody(
    /** id del livello di log */
    @SerialName("llevel")
    val llevel: Long = 0L,

    /** abilitazione inserimento transazioni. 0 -> non abilitato; 1 -> abilitato */
    @SerialName("addTransactionInfo")
    val addTransactionInfo: Int = 0,

    /** timeout del livello di override */
    @SerialName("llevelMinutesTimeout")
    val llevelMinutesTimeout: Double = 0.0
)

@Serializable
data class Esse3TransactionLogViewDetailParameters(
    /** nome del parametro */
    @SerialName("parName")
    val parameterName: String? = null,

    /** valore del parametro */
    @SerialName("parValue")
    val parameterValue: String? = null
)

@Serializable
data class Esse3TransactionLogViewParameters(
    /** ctx param id */
    @SerialName("ctxParamsId")
    val contextParamsId: Long? = null,

    /** id della transazione */
    @SerialName("transactionId")
    val transactionId: String? = null,

    /** id della descrizione */
    @SerialName("sessionid")
    val sessionId: String? = null,

    /** descrizione */
    @SerialName("des")
    val description: String? = null,

    /** data inserimento */
    @SerialName("dataIns")
    val insertionDate: String? = null,

    /** user inserimento */
    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("vTlogParamsDett")
    val vTeachingLogParametersDetail: List<Esse3TransactionLogViewDetailParameters> = emptyList()
)
