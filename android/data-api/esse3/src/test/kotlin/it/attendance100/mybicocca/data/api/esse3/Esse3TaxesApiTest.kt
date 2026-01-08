package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PaymentStatus
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class Esse3TaxesApiTest : Esse3TestBase() {

    @Test
    suspend fun getTaxBills() {
        val taxBills = api.taxes.getTaxBills()
        assertNotNull(taxBills)
    }

    @Test
    suspend fun getTaxBillDetail() {
        val taxBills = api.taxes.getTaxBills()
        if (taxBills.isEmpty()) return

        val bill = taxBills.first()
        val detail = api.taxes.getTaxBillDetail(bill)
        assertNotNull(detail.bill)
        assertNotNull(detail.items)
        assertNotNull(detail.paymentMethod)
    }

    @Test
    suspend fun downloadPaymentReceipt() {
        val taxBills = api.taxes.getTaxBills()
        if (taxBills.isEmpty()) return

        val paidBill = taxBills.firstOrNull {
            it.paymentStatus is Esse3PaymentStatus.PaidConfirmed ||
            it.paymentStatus is Esse3PaymentStatus.PaidPending
        }
        if (paidBill == null) return

        val detail = api.taxes.getTaxBillDetail(paidBill)
        if (detail.pagoPaInfo?.rptId == null) return

        val pdfChannel = api.taxes.downloadPaymentReceipt(detail)
        assertNotNull(pdfChannel)
    }

    @Test
    suspend fun refreshPaymentStatus() {
        val refreshedBills = api.taxes.refreshPaymentStatus()
        assertNotNull(refreshedBills)
    }
}
