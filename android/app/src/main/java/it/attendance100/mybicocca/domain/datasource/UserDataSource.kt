package it.attendance100.mybicocca.domain.datasource

import it.attendance100.mybicocca.domain.model.*

interface UserDataSource {
  /**
   * Retrieves user profile
   */
  suspend fun getProfile(): User

  /**
   * Syncs user profile from the server
   */
  suspend fun syncProfile()


  /**
   * Retrieves career statistics
   */
    suspend fun getCareerStats(): CareerStats

  /**
   * Syncs career statistics from the server
   */
  suspend fun syncCareerStats()


  /**
   * Retrieves payments
   */
  suspend fun getPayments(): List<Payment>

  /**
   * Syncs payments from the server
   */
  suspend fun syncPayments()


  /**
   * Retrieves registrations
   */
  suspend fun getRegistrations(): List<Registration>

  /**
   * Syncs registrations from the server
   */
  suspend fun syncRegistrations()
}