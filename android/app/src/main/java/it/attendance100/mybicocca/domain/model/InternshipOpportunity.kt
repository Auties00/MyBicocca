package it.attendance100.mybicocca.domain.model

import java.time.*

data class InternshipOpportunity(
	val id: String,
	val title: String,
	val companyName: String,
	val description: String?,
	val location: String?,
	val postedDate: LocalDate?,
	val deadline: LocalDate?,
	val isSaved: Boolean = false,
)
