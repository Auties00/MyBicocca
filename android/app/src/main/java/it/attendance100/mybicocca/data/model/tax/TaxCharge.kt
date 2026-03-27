package it.attendance100.mybicocca.data.model.tax

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tax_charges")
data class TaxCharge(
    @PrimaryKey val id: Long,
    val careerId: Long,
    val description: String,
    val amount: Double,
    val paidAmount: Double,
    val dueDate: String? = null,
    val paymentDate: String? = null,
    val status: String,
    val invoiceNumber: String? = null,
    val bulletinCode: String? = null,
    val modalita: String? = null,
    val rptStatus: String? = null,
    val isPagoPaEnabled: Boolean = true,
)
