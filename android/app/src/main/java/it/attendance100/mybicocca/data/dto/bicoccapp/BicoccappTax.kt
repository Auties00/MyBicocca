package it.attendance100.mybicocca.data.dto.bicoccapp

import com.google.gson.annotations.SerializedName

data class BicoccappTax(
    @SerializedName("yearId")
    val yearId: Int? = null,

    @SerializedName("fiscalCode")
    val fiscalCode: String? = null,

    @SerializedName("invoiceId")
    val invoiceId: Int? = null,

    @SerializedName("paymentId")
    val paymentId: Int? = null,

    @SerializedName("paidFlag")
    val paidFlag: Int? = null, // 0 = Unpaid, 1 = Paid

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("releaseDate")
    val releaseDate: String? = null,

    @SerializedName("paymentDate")
    val paymentDate: String? = null, // Note: JSON contains "null" string literal or date "dd/MM/yyyy"

    @SerializedName("mavDescription")
    val mavDescription: String? = null,

    @SerializedName("mavDescriptionNext")
    val mavDescriptionNext: String? = null,

    @SerializedName("invoiceCanceled")
    val invoiceCanceled: Int? = null,

    @SerializedName("invoiceWrong")
    val invoiceWrong: String? = null,

    @SerializedName("invoiceArrearId")
    val invoiceArrearId: Int? = null,

    @SerializedName("invoiceOverdue")
    val invoiceOverdue: String? = null,

    @SerializedName("invoiceAmount")
    val invoiceAmount: String? = null, // Money comes as String "1074.02"

    @SerializedName("invoiceAmountPaid")
    val invoiceAmountPaid: String? = null,

    @SerializedName("cashedBy")
    val cashedBy: String? = null,

    @SerializedName("arrearAddFlag")
    val arrearAddFlag: Int? = null,

    @SerializedName("arrearCount")
    val arrearCount: Int? = null,

    @SerializedName("personId")
    val personId: Int? = null,

    @SerializedName("manuallyRegisteredFlag")
    val manuallyRegisteredFlag: Int? = null,

    @SerializedName("reportId")
    val reportId: String? = null,

    @SerializedName("invoiceExpiration")
    val invoiceExpiration: String? = null
)