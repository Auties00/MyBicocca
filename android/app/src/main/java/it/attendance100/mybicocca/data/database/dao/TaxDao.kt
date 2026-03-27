package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.tax.Invoice
import it.attendance100.mybicocca.data.model.tax.TaxCharge
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxDao {
    @Query("SELECT * FROM tax_charges WHERE careerId = :careerId")
    fun observeCharges(careerId: Long): Flow<List<TaxCharge>>

    @Upsert
    suspend fun upsertAllCharges(charges: List<TaxCharge>)

    @Query("DELETE FROM tax_charges WHERE careerId = :careerId")
    suspend fun deleteChargesByCareerId(careerId: Long)

    @Query("SELECT * FROM invoices WHERE careerId = :careerId")
    fun observeInvoices(careerId: Long): Flow<List<Invoice>>

    @Upsert
    suspend fun upsertAllInvoices(invoices: List<Invoice>)

    @Query("DELETE FROM invoices WHERE careerId = :careerId")
    suspend fun deleteInvoicesByCareerId(careerId: Long)
}
