package it.attendance100.mybicocca.domain.model

import java.time.*

data class ExamAppeal(
	val examCode: String,
	val examName: String,
	val date: LocalDateTime,
	val room: String?,
	val building: String?,
	val description: String?,
	val registrationStart: LocalDateTime?,
	val registrationEnd: LocalDateTime?,
	val isRegistered: Boolean = false,
)
