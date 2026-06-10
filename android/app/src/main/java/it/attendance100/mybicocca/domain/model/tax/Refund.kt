package it.attendance100.mybicocca.domain.model.tax

import java.time.LocalDate

/**
 * A fee refund ("rimborso") on the student's account, sourced live from the Esse3 refunds
 * list. Listed on the registry "Rimborsi" sub-screen.
 *
 * @property invoiceId Esse3 invoice the refund originates from, when any.
 * @property academicYear Starting solar year of the academic year the refund belongs to.
 * @property amount Refund amount in euro.
 * @property description Human-readable refund description.
 * @property reasonCode Esse3 code of the refund reason.
 * @property mandateNumber Payment mandate ("mandato") number, once issued.
 * @property refunded Whether the refund has been disbursed.
 * @property note Free-text note attached by the registry office.
 * @property collectedBy Name of the person who collected the refund, when recorded.
 * @property issueDate Date the refund was issued.
 * @property processingDate Date the refund was processed.
 * @property paymentDate Date the refund was paid out.
 * @property creditDate Date the amount was credited.
 */
data class Refund(
    val invoiceId: Long?,
    val academicYear: Int?,
    val amount: Double?,
    val description: String?,
    val reasonCode: String?,
    val mandateNumber: String?,
    val refunded: Boolean,
    val note: String?,
    val collectedBy: String?,
    val issueDate: LocalDate?,
    val processingDate: LocalDate?,
    val paymentDate: LocalDate?,
    val creditDate: LocalDate?,
)
