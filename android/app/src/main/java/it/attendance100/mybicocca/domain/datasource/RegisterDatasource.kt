package it.attendance100.mybicocca.domain.datasource

import it.attendance100.mybicocca.domain.model.*

interface RegisterDataSource {
  /**
   * Retrieves available exams
   */
  suspend fun getAvailableExams(): List<ExamSession>

  /**
   * Retrieves booked exams
   */
  suspend fun getBookedExams(): List<ExamSession>


  /**
   * Retrieves passed exams
   */
  suspend fun getPassedExams(): List<Exam>


  /**
   * Retrieves payments
   */
  suspend fun getPayments(): List<Payment>

  /**
   * Retrieves career statistics
   */
  suspend fun getCareerStats(): CareerStats

  /**
   * Syncs register data from the server
   */
  suspend fun syncRegister()
}
