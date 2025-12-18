package it.attendance100.mybicocca.domain.model

import androidx.room.*
import java.time.*

@Entity(tableName = "taxes")
data class Tax(
	@PrimaryKey val id: Int,
	@ColumnInfo(name = "description") val description: String,
	@ColumnInfo(name = "amount") val amount: Float,
	@ColumnInfo(name = "amount_paid") val amountPaid: Float,
	@ColumnInfo(name = "due_date") val dueDate: LocalDate?,
	@ColumnInfo(name = "payment_date") val paymentDate: LocalDate?,
	@ColumnInfo(name = "is_paid") val isPaid: Boolean,
	@ColumnInfo(name = "is_expired") val isExpired: Boolean,
)
