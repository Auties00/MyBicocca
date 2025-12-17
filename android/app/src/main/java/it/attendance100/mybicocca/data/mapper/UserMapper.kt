package it.attendance100.mybicocca.data.mapper

import it.attendance100.mybicocca.data.remote.dto.bicoccapp.*
import it.attendance100.mybicocca.domain.model.*

fun UserProfile.toDomain(): User {
  val userDto = this.user
  val careerDto = this.careers?.firstOrNull() // Taking the first career or the selected one logic might be needed

  return User(
    name = userDto?.name ?: "",
    surname = userDto?.surname ?: "",
    matricola = careerDto?.matricCode ?: "",
    course = "", // Need to get this from UserCareer
    year = "", // Need to get this from UserCareer
    email = userDto?.email ?: ""
  )
}

fun UserCareer.toDomain(): CareerStats {
  val c = this.career
  // Mapping logic to be refined based on available fields in UserCareer DTO
  // Assuming placeholder values if fields are missing in DTO for now, need to check UserCareer DTO deeply.
  return CareerStats(
    mediaAritmetica = c?.mediaAritmetica?.toFloatOrNull() ?: 0f,
    mediaPonderata = c?.mediaPonderata?.toFloatOrNull() ?: 0f,
    esamiSostenuti = c?.examsCount ?: 0,
    esamiTotali = 0, // Not directly in summary
    cfuAcquisiti = c?.cfuPar?.toIntOrNull() ?: 0,
    cfuTotali = c?.cfuTot?.toIntOrNull() ?: 0,
    grades = emptyList(), // Needs processing from exams
    passedExams = emptyList(),
    remainingExams = emptyList()
  )
}

fun UserExams.toDomain(): List<Exam> {
  // Map exams
  return emptyList() // Placeholder until I see UserExams DTO structure
}
