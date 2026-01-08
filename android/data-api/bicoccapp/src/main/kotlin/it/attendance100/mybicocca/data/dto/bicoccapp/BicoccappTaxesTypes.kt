package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Payment status of a tax fee.
 */
@Serializable
enum class BicoccappPaymentStatus {
    @SerialName("0")
    UNPAID,
    @SerialName("1")
    PAID
}

/**
 * Response containing user tax/fee information.
 */
@Serializable
data class BicoccappTaxesResponse(
    /**
     * Career object containing tax fees.
     */
    @SerialName("career")
    val career: BicoccappUserTaxes
)

/**
 * Container for tax fees.
 */
@Serializable
data class BicoccappUserTaxes(
    /**
     * List of tax fees.
     */
    @SerialName("fees")
    val fees: List<BicoccappTax> = emptyList()
)

/**
 * Represents a specific tax or fee invoice.
 */
@Serializable
data class BicoccappTax(
    /**
     * Year ID associated with the tax.
     */
    @SerialName("yearId")
    val yearId: Int,

    /**
     * Fiscal Code.
     */
    @SerialName("fiscalCode")
    val fiscalCode: String,

    /**
     * Invoice ID.
     */
    @SerialName("invoiceId")
    val invoiceId: Int,

    /**
     * Payment ID.
     */
    @SerialName("paymentId")
    val paymentId: Int? = null,

    /**
     * Payment status.
     */
    @SerialName("paidFlag")
    val paymentStatus: BicoccappPaymentStatus,

    /**
     * Description of the fee.
     */
    @SerialName("description")
    val description: String,

    /**
     * Date the invoice was released/issued.
     */
    @SerialName("releaseDate")
    val releaseDate: String? = null,

    /**
     * Date the payment was made.
     */
    @SerialName("paymentDate")
    val paymentDate: String? = null,

    /**
     * MAV payment slip description.
     */
    @SerialName("mavDescription")
    val mavDescription: String? = null,

    /**
     * Next MAV description.
     */
    @SerialName("mavDescriptionNext")
    val mavDescriptionNext: String? = null,

    /**
     * Whether the invoice is canceled.
     */
    @SerialName("invoiceCanceled")
    @Serializable(with = BinaryBooleanSerializer::class)
    val isCanceled: Boolean = false,

    /**
     * Whether the invoice has errors.
     */
    @SerialName("invoiceWrong")
    @Serializable(with = YesNoBooleanSerializer::class)
    val hasErrors: Boolean = false,

    /**
     * ID for arrear invoice.
     */
    @SerialName("invoiceArrearId")
    val arrearId: Int = 0,

    /**
     * Whether the invoice is overdue.
     */
    @SerialName("invoiceOverdue")
    @Serializable(with = YesNoBooleanSerializer::class)
    val isOverdue: Boolean = false,

    /**
     * Total amount of the invoice.
     */
    @SerialName("invoiceAmount")
    val amount: String,

    /**
     * Amount paid so far.
     */
    @SerialName("invoiceAmountPaid")
    val amountPaid: String,

    /**
     * Entity that cashed the payment.
     */
    @SerialName("cashedBy")
    val cashedBy: String? = null,

    /**
     * Whether arrears have been added.
     */
    @SerialName("arrearAddFlag")
    @Serializable(with = BinaryBooleanSerializer::class)
    val hasArrearsAdded: Boolean = false,

    /**
     * Count of arrears.
     */
    @SerialName("arrearCount")
    val arrearCount: Int = 0,

    /**
     * Person ID.
     */
    @SerialName("personId")
    val personId: Int,

    /**
     * Whether manually registered.
     */
    @SerialName("manuallyRegisteredFlag")
    @Serializable(with = BinaryBooleanSerializer::class)
    val isManuallyRegistered: Boolean = false,

    /**
     * Report ID.
     */
    @SerialName("reportId")
    val reportId: String? = null,

    /**
     * Expiration date of the invoice.
     */
    @SerialName("invoiceExpiration")
    val expirationDate: String
)