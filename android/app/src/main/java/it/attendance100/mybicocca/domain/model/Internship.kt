package it.attendance100.mybicocca.domain.model

import androidx.room.*
import java.time.*

@Entity(tableName = "internships")
data class Internship(
	@PrimaryKey val id: String, // Company ID or unique internship ID
	@ColumnInfo(name = "company_name") val companyName: String,
	@ColumnInfo(name = "tutor_name") val tutorName: String?,
	@ColumnInfo(name = "status") val status: String, // Active, Completed, etc.
	@ColumnInfo(name = "start_date") val startDate: LocalDate?,
	@ColumnInfo(name = "end_date") val endDate: LocalDate?,
	@ColumnInfo(name = "cfu") val cfu: Int?,
)
