package it.attendance100.mybicocca.data.local.tax

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [TaxCacheDao] against a real in-memory Room database (Robolectric).
 * Exercises every offline tax mirror: the invoice/charge-item pair (whose replace must clear and
 * re-insert both tables atomically and per career), the single-row summary (whole-row replace),
 * and the career-position-keyed ISEE and refund lists. The reads order invoices by `cache_order`
 * and charge items by `item_order`, and each replace must scope its wipe to one career, leaving
 * siblings intact. All tables key on a plain `career_id` Long with no foreign keys, so no parent
 * account/career rows are required.
 *
 * Wave 2 (Android-runtime) test: Robolectric drives the actual Room-generated `_Impl`, pinned to
 * a Robolectric-supported SDK because the module compiles against a newer one.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaxCacheDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: TaxCacheDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.taxCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `replaceInvoices stores invoices and their charge items ordered by their position columns`() = runTest {
        dao.replaceInvoices(
            careerId = 1L,
            invoices = listOf(
                invoice(1L, invoiceId = 200L, cacheOrder = 1),
                invoice(1L, invoiceId = 100L, cacheOrder = 0),
            ),
            chargeItems = listOf(
                chargeItem(1L, invoiceId = 100L, itemOrder = 1, description = "Bollo"),
                chargeItem(1L, invoiceId = 100L, itemOrder = 0, description = "Tassa regionale"),
            ),
        )

        assertThat(dao.getInvoices(1L).map { it.invoiceId }).containsExactly(100L, 200L).inOrder()
        assertThat(dao.getChargeItems(1L).map { it.description })
            .containsExactly("Tassa regionale", "Bollo").inOrder()
    }

    @Test
    fun `replaceInvoices wipes both the prior invoices and their charge items for the career`() = runTest {
        dao.replaceInvoices(
            careerId = 1L,
            invoices = listOf(invoice(1L, invoiceId = 100L, cacheOrder = 0)),
            chargeItems = listOf(chargeItem(1L, invoiceId = 100L, itemOrder = 0, description = "old")),
        )

        dao.replaceInvoices(
            careerId = 1L,
            invoices = listOf(invoice(1L, invoiceId = 300L, cacheOrder = 0)),
            chargeItems = listOf(chargeItem(1L, invoiceId = 300L, itemOrder = 0, description = "new")),
        )

        assertThat(dao.getInvoices(1L).map { it.invoiceId }).containsExactly(300L)
        assertThat(dao.getChargeItems(1L).map { it.description }).containsExactly("new")
    }

    @Test
    fun `replaceInvoices leaves another career's invoices and charge items intact`() = runTest {
        dao.replaceInvoices(
            careerId = 2L,
            invoices = listOf(invoice(2L, invoiceId = 500L, cacheOrder = 0)),
            chargeItems = listOf(chargeItem(2L, invoiceId = 500L, itemOrder = 0, description = "keep")),
        )

        dao.replaceInvoices(
            careerId = 1L,
            invoices = listOf(invoice(1L, invoiceId = 100L, cacheOrder = 0)),
            chargeItems = listOf(chargeItem(1L, invoiceId = 100L, itemOrder = 0, description = "other")),
        )

        assertThat(dao.getInvoices(2L).map { it.invoiceId }).containsExactly(500L)
        assertThat(dao.getChargeItems(2L).map { it.description }).containsExactly("keep")
    }

    @Test
    fun `replaceSummary replaces the single career row`() = runTest {
        dao.replaceSummary(1L, summary(1L, light = "Green", dueAmount = 0.0))

        dao.replaceSummary(1L, summary(1L, light = "Red", dueAmount = 1234.56))

        val stored = dao.getSummary(1L)
        assertThat(stored).isNotNull()
        assertThat(stored!!.light).isEqualTo("Red")
        assertThat(stored.dueAmount).isEqualTo(1234.56)
    }

    @Test
    fun `getSummary is null for a career with no cached summary`() = runTest {
        dao.replaceSummary(1L, summary(1L, light = "Green", dueAmount = 0.0))

        assertThat(dao.getSummary(2L)).isNull()
    }

    @Test
    fun `replaceSummary for one career does not disturb another's`() = runTest {
        dao.replaceSummary(1L, summary(1L, light = "Green", dueAmount = 0.0))
        dao.replaceSummary(2L, summary(2L, light = "Yellow", dueAmount = 50.0))

        dao.replaceSummary(1L, summary(1L, light = "Red", dueAmount = 99.0))

        assertThat(dao.getSummary(2L)!!.light).isEqualTo("Yellow")
    }

    @Test
    fun `replaceIseeDeclarations preserves order and scopes to the career`() = runTest {
        dao.replaceIseeDeclarations(2L, listOf(isee(2L, cacheOrder = 0, isee = 9000.0)))
        dao.replaceIseeDeclarations(
            careerId = 1L,
            rows = listOf(
                isee(1L, cacheOrder = 1, isee = 20000.0),
                isee(1L, cacheOrder = 0, isee = 15000.0),
            ),
        )

        assertThat(dao.getIseeDeclarations(1L).map { it.isee })
            .containsExactly(15000.0, 20000.0).inOrder()
        assertThat(dao.getIseeDeclarations(2L).map { it.isee }).containsExactly(9000.0)
    }

    @Test
    fun `replaceIseeDeclarations wipes the prior career slice`() = runTest {
        dao.replaceIseeDeclarations(1L, listOf(isee(1L, cacheOrder = 0, isee = 1.0), isee(1L, cacheOrder = 1, isee = 2.0)))

        dao.replaceIseeDeclarations(1L, listOf(isee(1L, cacheOrder = 0, isee = 3.0)))

        assertThat(dao.getIseeDeclarations(1L).map { it.isee }).containsExactly(3.0)
    }

    @Test
    fun `replaceRefunds preserves order and scopes to the career`() = runTest {
        dao.replaceRefunds(2L, listOf(refund(2L, cacheOrder = 0, amount = 11.0)))
        dao.replaceRefunds(
            careerId = 1L,
            rows = listOf(
                refund(1L, cacheOrder = 1, amount = 200.0),
                refund(1L, cacheOrder = 0, amount = 100.0),
            ),
        )

        assertThat(dao.getRefunds(1L).map { it.amount }).containsExactly(100.0, 200.0).inOrder()
        assertThat(dao.getRefunds(2L).map { it.amount }).containsExactly(11.0)
    }

    @Test
    fun `replaceRefunds wipes the prior career slice`() = runTest {
        dao.replaceRefunds(1L, listOf(refund(1L, cacheOrder = 0, amount = 1.0), refund(1L, cacheOrder = 1, amount = 2.0)))

        dao.replaceRefunds(1L, listOf(refund(1L, cacheOrder = 0, amount = 9.0)))

        assertThat(dao.getRefunds(1L).map { it.amount }).containsExactly(9.0)
    }

    private fun invoice(careerId: Long, invoiceId: Long, cacheOrder: Int) = TaxInvoiceEntity(
        careerId = careerId,
        invoiceId = invoiceId,
        cacheOrder = cacheOrder,
        academicYear = 2024,
        title = "Rata unica",
        amount = 500.0,
        paidAmount = null,
        status = "Unpaid",
        issueDate = "2024-09-01",
        expiration = "2024-10-15",
        paymentDate = null,
        pagoPaEnabled = true,
        pagoPaImmediate = false,
        pagoPaNotice = true,
        iuv = null,
        noticeCode = null,
    )

    private fun chargeItem(
        careerId: Long,
        invoiceId: Long,
        itemOrder: Int,
        description: String,
    ) = TaxChargeItemEntity(
        careerId = careerId,
        invoiceId = invoiceId,
        itemOrder = itemOrder,
        description = description,
        amount = 100.0,
        installmentDescription = null,
        expiration = null,
    )

    private fun summary(careerId: Long, light: String, dueAmount: Double) = TaxSummaryEntity(
        careerId = careerId,
        light = light,
        dueAmount = dueAmount,
        expiredCount = 0,
        dueCount = 0,
    )

    private fun isee(careerId: Long, cacheOrder: Int, isee: Double) = IseeDeclarationEntity(
        careerId = careerId,
        cacheOrder = cacheOrder,
        academicYearEnrollmentId = null,
        courseDescription = null,
        isee = isee,
        iseeThreshold = null,
        exemptionDescription = null,
    )

    private fun refund(careerId: Long, cacheOrder: Int, amount: Double) = RefundEntity(
        careerId = careerId,
        cacheOrder = cacheOrder,
        invoiceId = null,
        academicYear = null,
        amount = amount,
        description = null,
        reasonCode = null,
        mandateNumber = null,
        refunded = false,
        note = null,
        collectedBy = null,
        issueDate = null,
        processingDate = null,
        paymentDate = null,
        creditDate = null,
    )
}
