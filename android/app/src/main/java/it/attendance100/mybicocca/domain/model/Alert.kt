package it.attendance100.mybicocca.domain.model

import androidx.room.*
import java.time.*

@Entity(tableName = "alerts")
data class Alert(
	@PrimaryKey val id: Long,
	@ColumnInfo(name = "title") val title: String,
	@ColumnInfo(name = "date") val date: LocalDateTime,
	@ColumnInfo(name = "is_read") val isRead: Boolean,
	@ColumnInfo(name = "message") val message: String,
)
