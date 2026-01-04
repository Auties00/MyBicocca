package it.attendance100.mybicocca.data.api.esse3

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Integration tests for [Esse3TaxesApi].
 */
class Esse3TaxesApiTest : Esse3TestBase() {

    @Test
    fun `getTaxBills returns payment list`() {
        runBlocking {
            val bills = api.taxes.getTaxBills()

            assertNotNull(bills, "Tax bills should not be null")

            println("Tax bills retrieved: ${bills.size}")
            bills.take(10).forEach { bill ->
                println("  - ${bill.description}")
                println("    Invoice: ${bill.invoiceNumber}")
                println("    Amount: ${bill.amount} ${bill.currency}")
                println("    Status: ${bill.paymentStatus}")
                bill.dueDate?.let { println("    Due: $it") }
                if (bill.pagoPaAvailable) println("    PagoPA available")
            }

            if (bills.size > 10) {
                println("  ... and ${bills.size - 10} more bills")
            }
        }
    }

    @Test
    fun `getTaxBillDetail returns bill information`() {
        runBlocking {
            val bills = api.taxes.getTaxBills()

            if (bills.isNotEmpty()) {
                val firstBill = bills.first()
                val details = api.taxes.getTaxBillDetail(firstBill.id)

                println("Bill details for: ${details.bill.description}")
                println("  Invoice: ${details.bill.invoiceNumber}")
                println("  Total Amount: ${details.bill.amount} ${details.bill.currency}")
                println("  Status: ${details.bill.paymentStatus}")
                details.paymentMethod?.let { println("  Payment Method: $it") }
                details.iuv?.let { println("  IUV: $it") }
                println("  Items: ${details.bill.items.size}")
                details.bill.items.forEach { item ->
                    println("    - ${item.description}: ${item.amount}")
                    item.academicYear?.let { println("      Year: $it") }
                    item.installment?.let { println("      Installment: $it") }
                }
            } else {
                println("No bills available for detail test")
            }
        }
    }

    @Test
    fun `refreshPaymentStatus updates bill list`() {
        runBlocking {
            val bills = api.taxes.refreshPaymentStatus()

            assertNotNull(bills, "Refreshed bills should not be null")

            println("Payment status refreshed, ${bills.size} bills returned")
        }
    }
}
