package it.attendance100.mybicocca.data.datasource.remote

import it.attendance100.mybicocca.data.mapper.*
import it.attendance100.mybicocca.data.remote.api.bicoccapp.*
import it.attendance100.mybicocca.data.remote.api.esse3.*
import it.attendance100.mybicocca.domain.datasource.*
import it.attendance100.mybicocca.domain.model.*
import javax.inject.*

class RemoteRegisterDataSource @Inject constructor(
  private val userApi: BicoccappUserApi,
  private val examsApi: Esse3ExamsApi,
) : RegisterDataSource {

  override suspend fun getAvailableExams(): List<ExamSession> {
    val response = userApi.getAppeals()
    if (response.isSuccessful && response.body() != null) {
      val available = response.body()!!.availableSessions
      return available.toExamSessions()
    }
    return emptyList()
  }

  override suspend fun getBookedExams(): List<ExamSession> {
    val response = userApi.getAppeals()
    if (response.isSuccessful && response.body() != null) {
      val registered = response.body()!!.registeredSessions
      return registered.toExamSessions()
    }
    return emptyList()
  }

  override suspend fun getPassedExams(): List<Exam> {
    val response = userApi.getExams()
    if (response.isSuccessful && response.body() != null) {
      return response.body()!!.toDomain()
    }
    return emptyList()
  }

  override suspend fun getPayments(): List<Payment> {
    val response = userApi.getFees()
    if (response.isSuccessful && response.body() != null) {
      val fees = response.body()!!.career?.fees
      return fees.toPayments()
    }
    return emptyList()
  }

  override suspend fun getCareerStats(): CareerStats {
    val response = userApi.getCareer()
    if (response.isSuccessful && response.body() != null) {
      return response.body()!!.toDomain()
    }
    throw Exception("Failed to fetch career stats: ${response.code()}")
  }

  override suspend fun syncRegister() {
    getAvailableExams()
    getBookedExams()
    getPassedExams()
    getPayments()
    getCareerStats()
  }
}
