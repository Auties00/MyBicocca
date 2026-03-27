package it.attendance100.mybicocca.data.model.tax

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val id: Long,
    val careerId: Long,
    val description: String,
    val amount: Double,
    val issueDate: String? = null,
    val paymentDate: String? = null,
    val status: String,
)
