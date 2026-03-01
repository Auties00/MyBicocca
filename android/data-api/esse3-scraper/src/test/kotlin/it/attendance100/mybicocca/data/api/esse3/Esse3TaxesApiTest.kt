package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.dto.esse3.Esse3PaymentStatus
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
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
        for(bill in taxBills) {
            val detail = api.taxes.getTaxBillDetail(bill)
            assertNotNull(detail.bill)
            assertNotNull(detail.items)
            assertNotNull(detail.paymentMethod)
        }
    }

    @Test
    suspend fun downloadPaymentReceipt() {
        val taxBills = api.taxes.getTaxBills()
        for(bill in taxBills) {
            if(bill.paymentStatus is Esse3PaymentStatus.PaidConfirmed || bill.paymentStatus is Esse3PaymentStatus.PaidPending) {
                val detail = api.taxes.getTaxBillDetail(bill)
                val pdfChannel = api.taxes.downloadPaymentReceipt(detail)
                assertTrue(pdfChannel == null || pdfChannel.isPdf())
            }
        }
    }

    @Test
    suspend fun refreshPaymentStatus() {
        val refreshedBills = api.taxes.refreshPaymentStatus()
        assertNotNull(refreshedBills)
    }
}
