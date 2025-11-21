package it.attendance100.mybicocca.data.datasources.user

import it.attendance100.mybicocca.data.mocks.*
import it.attendance100.mybicocca.domain.model.*
import javax.inject.*

class MockUserDataSource @Inject constructor() : UserDataSource {
  override suspend fun getUser(): User = UserMockData.user
  override suspend fun getCareerStats(): CareerStats = UserMockData.careerStats
}
