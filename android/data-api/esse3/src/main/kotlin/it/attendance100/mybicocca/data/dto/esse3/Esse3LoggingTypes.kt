package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3TransactionLogSession(
    @SerialName("sessionid")
    val sessionId: String? = null,

    @SerialName("llevel")
    val llevel: Long? = null,

    @SerialName("lcode")
    val lcode: String? = null,

    @SerialName("ldesc")
    val ldesc: String? = null,

    @SerialName("addTransactionInfo")
    val addTransactionInfo: Int? = null,

    @SerialName("llevelMinutesTimeout")
    val llevelMinutesTimeout: Double? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null
)

@Serializable
data class Esse3TransactionLogSessionBody(
    @SerialName("llevel")
    val llevel: Long,

    @SerialName("addTransactionInfo")
    val addTransactionInfo: Int,

    @SerialName("llevelMinutesTimeout")
    val llevelMinutesTimeout: Double
)

@Serializable
data class Esse3TransactionLogViewDetailParameters(
    @SerialName("parName")
    val parameterName: String? = null,

    @SerialName("parValue")
    val parameterValue: String? = null
)

@Serializable
data class Esse3TransactionLogViewParameters(
    @SerialName("ctxParamsId")
    val contextParamsId: Long? = null,

    @SerialName("transactionId")
    val transactionId: String? = null,

    @SerialName("sessionid")
    val sessionId: String? = null,

    @SerialName("des")
    val description: String? = null,

    @SerialName("dataIns")
    val insertionDate: String? = null,

    @SerialName("usrInsId")
    val insertionUserId: String? = null,

    @SerialName("vTlogParamsDett")
    val vTeachingLogParametersDetail: List<Esse3TransactionLogViewDetailParameters> = emptyList()
)
