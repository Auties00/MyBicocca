package it.attendance100.mybicocca.data.datasource.remote

import it.attendance100.mybicocca.data.mapper.*
import it.attendance100.mybicocca.data.remote.api.bicoccapp.*
import it.attendance100.mybicocca.domain.datasource.*
import it.attendance100.mybicocca.domain.model.*
import javax.inject.*

class RemoteUserDataSource @Inject constructor(
  private val api: BicoccappUserApi,
) : UserDataSource {

  override suspend fun getProfile(): User {
    val response = api.getProfile()
    if (response.isSuccessful && response.body() != null) {
      return response.body()!!.toDomain()
    } else {
      throw Exception("Failed to fetch profile: ${response.code()}")
    }
  }

  override suspend fun syncProfile() {
    getProfile()
  }

  override suspend fun getCareerStats(): CareerStats {
    val response = api.getCareer()
    if (response.isSuccessful && response.body() != null) {
      return response.body()!!.toDomain()
    } else {
      throw Exception("Failed to fetch career stats: ${response.code()}")
    }
  }

  override suspend fun syncCareerStats() {
    getCareerStats()
  }

  override suspend fun getPayments(): List<Payment> {
    val response = api.getFees()
    if (response.isSuccessful && response.body() != null) {
      val feesList = response.body()!!.career?.fees
      return feesList.toPayments()
    } else {
      throw Exception("Failed to fetch fees: ${response.code()}")
    }
  }

  override suspend fun syncPayments() {
    getPayments()
  }

  override suspend fun getRegistrations(): List<Registration> {
    val response = api.getRegistrations()
    if (response.isSuccessful && response.body() != null) {
      val regsList = response.body()!!.career?.registrations
      return regsList.toRegistrations()
    } else {
      throw Exception("Failed to fetch registrations: ${response.code()}")
    }
  }

  override suspend fun syncRegistrations() {
    getRegistrations()
  }
}
