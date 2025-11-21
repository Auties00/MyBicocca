package it.attendance100.mybicocca.data.datasources.user

import it.attendance100.mybicocca.domain.model.*

interface UserDataSource {
  suspend fun getUser(): User
  suspend fun getCareerStats(): CareerStats
}
