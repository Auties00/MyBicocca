package it.attendance100.mybicocca.data.api.bicoccapp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BicoccappTaxesApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getTaxes() {
        val taxes = api.taxes.getTaxes(profile.personId, profile.enrollmentId)
        assertNotNull(taxes)
        assertTrue(taxes.isNotEmpty())
    }
}
