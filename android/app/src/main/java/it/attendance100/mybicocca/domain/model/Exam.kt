package it.attendance100.mybicocca.domain.model

import java.time.*

data class Exam(
	val code: String,
	val name: String,
	val cfu: Float,
	val grade: String?,
	val date: LocalDate?,
	val status: String?, // "S" for Superata, "F" for Frequentata
	val isPassed: Boolean,
)
