package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.datasources.notification.*
import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.flow.*
import javax.inject.*
import it.attendance100.mybicocca.domain.contracts.NotificationRepository as INotificationRepository

/**
 * Implements notification contract, uses datasource
 * Manages notification data
 */
class NotificationRepository @Inject constructor(
  private val dataSource: NotificationDataSource,
) : INotificationRepository {
  override fun getUnreadNotifications(): Flow<List<Notification>> = flow {
    emit(dataSource.getUnreadNotifications())
  }
}
