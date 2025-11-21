package it.attendance100.mybicocca.data.mocks

import it.attendance100.mybicocca.domain.model.*

object UserMockData {
  val user = User(
    name = "Mario",
    surname = "Rossi",
    matricola = "123456",
    course = "Informatica",
    year = "3",
    email = "m.rossi@campus.unimib.it"
  )

  val careerStats = CareerStats(
    mediaAritmetica = 27.5f,
    mediaPonderata = 28.2f,
    esamiSostenuti = 18,
    esamiTotali = 24,
    cfuAcquisiti = 144,
    cfuTotali = 180,
    grades = listOf(28f, 30f, 26f, 29f, 27f, 30f, 28f, 25f, 29f, 30f, 27f, 28f, 30f, 26f, 29f, 27f, 28f, 30f, 31f, 19f)
  )
}
