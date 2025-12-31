package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicoccappTax(
    @SerialName("yearId")
    val yearId: Int? = null,

    @SerialName("fiscalCode")
    val fiscalCode: String? = null,

    @SerialName("invoiceId")
    val invoiceId: Int? = null,

    @SerialName("paymentId")
    val paymentId: Int? = null,

    @SerialName("paidFlag")
    val paidFlag: Int? = null, // 0 = Unpaid, 1 = Paid

    @SerialName("description")
    val description: String? = null,

    @SerialName("releaseDate")
    val releaseDate: String? = null,

    @SerialName("paymentDate")
    val paymentDate: String? = null, // Note: JSON contains "null" string literal or date "dd/MM/yyyy"

    @SerialName("mavDescription")
    val mavDescription: String? = null,

    @SerialName("mavDescriptionNext")
    val mavDescriptionNext: String? = null,

    @SerialName("invoiceCanceled")
    val invoiceCanceled: Int? = null,

    @SerialName("invoiceWrong")
    val invoiceWrong: String? = null,

    @SerialName("invoiceArrearId")
    val invoiceArrearId: Int? = null,

    @SerialName("invoiceOverdue")
    val invoiceOverdue: String? = null,

    @SerialName("invoiceAmount")
    val invoiceAmount: String? = null, // Money comes as String "1074.02"

    @SerialName("invoiceAmountPaid")
    val invoiceAmountPaid: String? = null,

    @SerialName("cashedBy")
    val cashedBy: String? = null,

    @SerialName("arrearAddFlag")
    val arrearAddFlag: Int? = null,

    @SerialName("arrearCount")
    val arrearCount: Int? = null,

    @SerialName("personId")
    val personId: Int? = null,

    @SerialName("manuallyRegisteredFlag")
    val manuallyRegisteredFlag: Int? = null,

    @SerialName("reportId")
    val reportId: String? = null,

    @SerialName("invoiceExpiration")
    val invoiceExpiration: String? = null
)