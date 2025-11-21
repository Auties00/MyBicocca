package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*
import javax.inject.*

class MockUserRepository @Inject constructor() : UserRepository {
  override fun getUser(): Flow<User> = flowOf(
    User(
      name = "Mario",
      surname = "Rossi",
      matricola = "123456",
      course = "Informatica",
      year = "3",
      email = "m.rossi@campus.unimib.it"
    )
  )

  override fun getCareerStats(): Flow<CareerStats> = flowOf(
    CareerStats(
      mediaAritmetica = 27.5f,
      mediaPonderata = 28.2f,
      esamiSostenuti = 18,
      esamiTotali = 24,
      cfuAcquisiti = 144,
      cfuTotali = 180,
      grades = listOf(28f, 30f, 26f, 29f, 27f, 30f, 28f, 25f, 29f, 30f, 27f, 28f, 30f, 26f, 29f, 27f, 28f, 30f, 31f, 19f)
    )
  )
}
