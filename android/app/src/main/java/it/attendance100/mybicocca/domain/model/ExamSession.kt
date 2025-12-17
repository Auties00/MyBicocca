package it.attendance100.mybicocca.domain.model

import java.time.*

data class ExamSession(
  val id: String,
  val name: String, // nome insegnamento
  val code: String, // codice insegnamento
  val description: String, // descrizione appello
  val date: LocalDate,
  val registrationStartDate: LocalDate?,
  val registrationEndDate: LocalDate?,
  val type: String, // tipo esame (scritto/orale)
  val classroom: String?,
)
