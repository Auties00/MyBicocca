package it.attendance100.mybicocca.data.local.tax

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Read/replace access to the offline tax mirrors. Each cached read is replaced wholesale on a
 * successful fetch (delete-then-insert in one transaction) and read back ordered by the
 * preserved server position; reads are consulted only when a live call fails offline. Invoices
 * own a child charge-item table, so their replace clears and re-inserts both tables atomically,
 * and the charge lines are read with a sibling career-scoped query the mapper joins onto its
 * invoices.
 */
@Dao
interface TaxCacheDao {

    @Query("SELECT * FROM cached_tax_invoice WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getInvoices(careerId: Long): List<TaxInvoiceEntity>

    @Query("SELECT * FROM cached_tax_charge_item WHERE career_id = :careerId ORDER BY item_order")
    suspend fun getChargeItems(careerId: Long): List<TaxChargeItemEntity>

    @Query("DELETE FROM cached_tax_invoice WHERE career_id = :careerId")
    suspend fun clearInvoices(careerId: Long)

    @Query("DELETE FROM cached_tax_charge_item WHERE career_id = :careerId")
    suspend fun clearChargeItems(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(rows: List<TaxInvoiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargeItems(rows: List<TaxChargeItemEntity>)

    @Transaction
    suspend fun replaceInvoices(
        careerId: Long,
        invoices: List<TaxInvoiceEntity>,
        chargeItems: List<TaxChargeItemEntity>,
    ) {
        clearChargeItems(careerId)
        clearInvoices(careerId)
        insertInvoices(invoices)
        insertChargeItems(chargeItems)
    }

    @Query("SELECT * FROM cached_tax_summary WHERE career_id = :careerId")
    suspend fun getSummary(careerId: Long): TaxSummaryEntity?

    @Query("DELETE FROM cached_tax_summary WHERE career_id = :careerId")
    suspend fun clearSummary(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(row: TaxSummaryEntity)

    @Transaction
    suspend fun replaceSummary(careerId: Long, row: TaxSummaryEntity) {
        clearSummary(careerId)
        insertSummary(row)
    }

    @Query("SELECT * FROM cached_tax_isee WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getIseeDeclarations(careerId: Long): List<IseeDeclarationEntity>

    @Query("DELETE FROM cached_tax_isee WHERE career_id = :careerId")
    suspend fun clearIseeDeclarations(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIseeDeclarations(rows: List<IseeDeclarationEntity>)

    @Transaction
    suspend fun replaceIseeDeclarations(careerId: Long, rows: List<IseeDeclarationEntity>) {
        clearIseeDeclarations(careerId)
        insertIseeDeclarations(rows)
    }

    @Query("SELECT * FROM cached_tax_refund WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getRefunds(careerId: Long): List<RefundEntity>

    @Query("DELETE FROM cached_tax_refund WHERE career_id = :careerId")
    suspend fun clearRefunds(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefunds(rows: List<RefundEntity>)

    @Transaction
    suspend fun replaceRefunds(careerId: Long, rows: List<RefundEntity>) {
        clearRefunds(careerId)
        insertRefunds(rows)
    }
}
