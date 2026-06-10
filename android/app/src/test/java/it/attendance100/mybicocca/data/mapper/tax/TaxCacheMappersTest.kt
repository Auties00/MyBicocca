package it.attendance100.mybicocca.data.mapper.tax

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.tax.TaxInvoiceEntity
import it.attendance100.mybicocca.data.local.tax.TaxSummaryEntity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.tax.InvoiceId
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.domain.model.tax.PaymentOutcome
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.model.tax.TaxChargeItem
import it.attendance100.mybicocca.domain.model.tax.TaxInvoice
import it.attendance100.mybicocca.domain.model.tax.TaxLight
import it.attendance100.mybicocca.domain.model.tax.TaxStatus
import it.attendance100.mybicocca.domain.model.tax.TaxSummary
import org.junit.Test
import java.time.LocalDate

/**
 * Round-trip checks for the offline tax mirror mappers: enums round-trip by name with the
 * documented UNKNOWN/PENDING fallback, dates round-trip through ISO-8601 strings (unparseable
 * dropped to null), value classes unwrap, and child charge rows keep their list order.
 */
class TaxCacheMappersTest {

    private val careerId = CareerId(555L)

    private fun invoice(
        items: List<TaxChargeItem> = emptyList(),
        status: TaxStatus = TaxStatus.PENDING,
        issueDate: LocalDate? = LocalDate.of(2024, 1, 15),
    ) = TaxInvoice(
        id = InvoiceId(77L),
        academicYear = 2024,
        title = "Tasse",
        amount = 300.0,
        paidAmount = 150.0,
        status = status,
        issueDate = issueDate,
        expiration = LocalDate.of(2024, 6, 30),
        paymentDate = null,
        pagoPaEnabled = true,
        pagoPaImmediate = false,
        pagoPaNotice = true,
        iuv = "IUV1",
        noticeCode = "NC1",
        items = items,
    )

    @Test
    fun `invoice round-trips scalar fields enum status and dates`() {
        val domain = invoice(status = TaxStatus.PAID)
        val entity = domain.toEntity(careerId, order = 3)

        assertThat(entity.careerId).isEqualTo(555L)
        assertThat(entity.invoiceId).isEqualTo(77L)
        assertThat(entity.cacheOrder).isEqualTo(3)
        assertThat(entity.status).isEqualTo("PAID")
        assertThat(entity.issueDate).isEqualTo("2024-01-15")

        val back = entity.toDomain(items = emptyList())
        assertThat(back.id).isEqualTo(InvoiceId(77L))
        assertThat(back.status).isEqualTo(TaxStatus.PAID)
        assertThat(back.issueDate).isEqualTo(LocalDate.of(2024, 1, 15))
        assertThat(back.expiration).isEqualTo(LocalDate.of(2024, 6, 30))
        assertThat(back.paymentDate).isNull()
        assertThat(back.pagoPaEnabled).isTrue()
        assertThat(back.pagoPaNotice).isTrue()
        assertThat(back.iuv).isEqualTo("IUV1")
    }

    @Test
    fun `invoice entity with unknown status name falls back to PENDING`() {
        val entity = invoice().toEntity(careerId, 0).copy(status = "GONE")
        assertThat(entity.toDomain(emptyList()).status).isEqualTo(TaxStatus.PENDING)
    }

    @Test
    fun `invoice entity with unparseable date drops it to null`() {
        val entity = invoice().toEntity(careerId, 0).copy(issueDate = "not-a-date")
        assertThat(entity.toDomain(emptyList()).issueDate).isNull()
    }

    @Test
    fun `null issue date round-trips as null`() {
        val entity = invoice(issueDate = null).toEntity(careerId, 0)
        assertThat(entity.issueDate).isNull()
        assertThat(entity.toDomain(emptyList()).issueDate).isNull()
    }

    @Test
    fun `charge item entities preserve their list order`() {
        val items = listOf(
            TaxChargeItem("Prima", 100.0, "Rata 1", LocalDate.of(2024, 3, 1)),
            TaxChargeItem("Seconda", 200.0, null, null),
        )
        val entities = invoice(items = items).toChargeItemEntities(careerId)

        assertThat(entities).hasSize(2)
        assertThat(entities[0].itemOrder).isEqualTo(0)
        assertThat(entities[0].description).isEqualTo("Prima")
        assertThat(entities[0].expiration).isEqualTo("2024-03-01")
        assertThat(entities[1].itemOrder).isEqualTo(1)
        assertThat(entities[1].expiration).isNull()
        assertThat(entities.all { it.invoiceId == 77L && it.careerId == 555L }).isTrue()
    }

    @Test
    fun `charge item round-trips through entity`() {
        val item = TaxChargeItem("Voce", 99.5, "Rata", LocalDate.of(2024, 9, 9))
        val back = item.toEntity(careerId, InvoiceId(77L), 0).toDomain()
        assertThat(back).isEqualTo(item)
    }

    @Test
    fun `summary round-trips light by name`() {
        val summary = TaxSummary(TaxLight.YELLOW, dueAmount = 42.0, expiredCount = 2, dueCount = 3)
        val entity = summary.toEntity(careerId)
        assertThat(entity.light).isEqualTo("YELLOW")
        assertThat(entity.toDomain()).isEqualTo(summary)
    }

    @Test
    fun `summary entity with unknown light falls back to UNKNOWN`() {
        val entity = TaxSummaryEntity(careerId.value, "PURPLE", 0.0, 0, 0)
        assertThat(entity.toDomain().light).isEqualTo(TaxLight.UNKNOWN)
    }

    @Test
    fun `isee declaration round-trips`() {
        val declaration = IseeDeclaration(
            academicYearEnrollmentId = 11L,
            courseDescription = "Matematica",
            isee = 12_000.0,
            iseeThreshold = 20_000.0,
            exemptionDescription = "Banda 1",
        )
        val back = declaration.toEntity(careerId, order = 4).toDomain()
        assertThat(back).isEqualTo(declaration)
    }

    @Test
    fun `refund round-trips including dates and null amount`() {
        val refund = Refund(
            invoiceId = 3L,
            academicYear = 2023,
            amount = null,
            description = "Rimborso",
            reasonCode = "R1",
            mandateNumber = "M1",
            refunded = true,
            note = "nota",
            collectedBy = "Ufficio",
            issueDate = LocalDate.of(2024, 2, 1),
            processingDate = null,
            paymentDate = LocalDate.of(2024, 2, 10),
            creditDate = null,
        )
        val entity = refund.toEntity(careerId, order = 1)
        assertThat(entity.issueDate).isEqualTo("2024-02-01")
        assertThat(entity.processingDate).isNull()
        assertThat(entity.toDomain()).isEqualTo(refund)
    }

    @Test
    fun `invoice entity reattaches its mapped charge items`() {
        val items = listOf(TaxChargeItem("X", 1.0, null, null))
        val entity: TaxInvoiceEntity = invoice().toEntity(careerId, 0)
        assertThat(entity.toDomain(items).items).isEqualTo(items)
    }

    /** Guards that the unrelated PaymentOutcome enum still has its four documented entries. */
    @Test
    fun `payment outcome enum has the four coarse states`() {
        assertThat(PaymentOutcome.entries).containsExactly(
            PaymentOutcome.Completed,
            PaymentOutcome.Pending,
            PaymentOutcome.Failed,
            PaymentOutcome.Unknown,
        )
    }
}
