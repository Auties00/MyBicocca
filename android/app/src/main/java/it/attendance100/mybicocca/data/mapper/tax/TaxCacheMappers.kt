package it.attendance100.mybicocca.data.mapper.tax

import it.attendance100.mybicocca.data.local.tax.IseeDeclarationEntity
import it.attendance100.mybicocca.data.local.tax.RefundEntity
import it.attendance100.mybicocca.data.local.tax.TaxChargeItemEntity
import it.attendance100.mybicocca.data.local.tax.TaxInvoiceEntity
import it.attendance100.mybicocca.data.local.tax.TaxSummaryEntity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.model.tax.TaxChargeItem
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxLight
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.domain.model.tax.TaxSummary
import java.time.LocalDate

// Entity <-> domain mapping for the offline tax mirrors. An invoice maps to a parent row plus one
// child charge-item row per voce (its item_order preserving list position); enums round-trip by
// name with an UNKNOWN fallback; dates round-trip through ISO-8601 strings, with unparseable
// values dropped to null.

fun TaxInvoice.toEntity(careerId: CareerId, order: Int): TaxInvoiceEntity = TaxInvoiceEntity(
    careerId = careerId.value,
    invoiceId = id.value,
    cacheOrder = order,
    academicYear = academicYear,
    title = title,
    amount = amount,
    paidAmount = paidAmount,
    status = status.name,
    issueDate = issueDate?.toString(),
    expiration = expiration?.toString(),
    paymentDate = paymentDate?.toString(),
    pagoPaEnabled = pagoPaEnabled,
    pagoPaImmediate = pagoPaImmediate,
    pagoPaNotice = pagoPaNotice,
    iuv = iuv,
    noticeCode = noticeCode,
)

fun TaxInvoice.toChargeItemEntities(careerId: CareerId): List<TaxChargeItemEntity> =
    items.mapIndexed { index, item -> item.toEntity(careerId, id, index) }

fun TaxInvoiceEntity.toDomain(items: List<TaxChargeItem>): TaxInvoice = TaxInvoice(
    id = InvoiceId(invoiceId),
    academicYear = academicYear,
    title = title,
    amount = amount,
    paidAmount = paidAmount,
    status = status.toTaxStatus(),
    issueDate = issueDate.toLocalDateOrNull(),
    expiration = expiration.toLocalDateOrNull(),
    paymentDate = paymentDate.toLocalDateOrNull(),
    pagoPaEnabled = pagoPaEnabled,
    pagoPaImmediate = pagoPaImmediate,
    pagoPaNotice = pagoPaNotice,
    iuv = iuv,
    noticeCode = noticeCode,
    items = items,
)

fun TaxChargeItem.toEntity(careerId: CareerId, invoiceId: InvoiceId, order: Int): TaxChargeItemEntity =
    TaxChargeItemEntity(
        careerId = careerId.value,
        invoiceId = invoiceId.value,
        itemOrder = order,
        description = description,
        amount = amount,
        installmentDescription = installmentDescription,
        expiration = expiration?.toString(),
    )

fun TaxChargeItemEntity.toDomain(): TaxChargeItem = TaxChargeItem(
    description = description,
    amount = amount,
    installmentDescription = installmentDescription,
    expiration = expiration.toLocalDateOrNull(),
)

fun TaxSummary.toEntity(careerId: CareerId): TaxSummaryEntity = TaxSummaryEntity(
    careerId = careerId.value,
    light = light.name,
    dueAmount = dueAmount,
    expiredCount = expiredCount,
    dueCount = dueCount,
)

fun TaxSummaryEntity.toDomain(): TaxSummary = TaxSummary(
    light = light.toTaxLight(),
    dueAmount = dueAmount,
    expiredCount = expiredCount,
    dueCount = dueCount,
)

fun IseeDeclaration.toEntity(careerId: CareerId, order: Int): IseeDeclarationEntity = IseeDeclarationEntity(
    careerId = careerId.value,
    cacheOrder = order,
    academicYearEnrollmentId = academicYearEnrollmentId,
    courseDescription = courseDescription,
    isee = isee,
    iseeThreshold = iseeThreshold,
    exemptionDescription = exemptionDescription,
)

fun IseeDeclarationEntity.toDomain(): IseeDeclaration = IseeDeclaration(
    academicYearEnrollmentId = academicYearEnrollmentId,
    courseDescription = courseDescription,
    isee = isee,
    iseeThreshold = iseeThreshold,
    exemptionDescription = exemptionDescription,
)

fun Refund.toEntity(careerId: CareerId, order: Int): RefundEntity = RefundEntity(
    careerId = careerId.value,
    cacheOrder = order,
    invoiceId = invoiceId,
    academicYear = academicYear,
    amount = amount,
    description = description,
    reasonCode = reasonCode,
    mandateNumber = mandateNumber,
    refunded = refunded,
    note = note,
    collectedBy = collectedBy,
    issueDate = issueDate?.toString(),
    processingDate = processingDate?.toString(),
    paymentDate = paymentDate?.toString(),
    creditDate = creditDate?.toString(),
)

fun RefundEntity.toDomain(): Refund = Refund(
    invoiceId = invoiceId,
    academicYear = academicYear,
    amount = amount,
    description = description,
    reasonCode = reasonCode,
    mandateNumber = mandateNumber,
    refunded = refunded,
    note = note,
    collectedBy = collectedBy,
    issueDate = issueDate.toLocalDateOrNull(),
    processingDate = processingDate.toLocalDateOrNull(),
    paymentDate = paymentDate.toLocalDateOrNull(),
    creditDate = creditDate.toLocalDateOrNull(),
)

private fun String.toTaxStatus(): TaxStatus =
    runCatching { TaxStatus.valueOf(this) }.getOrDefault(TaxStatus.PENDING)

private fun String.toTaxLight(): TaxLight =
    runCatching { TaxLight.valueOf(this) }.getOrDefault(TaxLight.UNKNOWN)

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
