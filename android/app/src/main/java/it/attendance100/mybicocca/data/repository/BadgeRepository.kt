package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.BadgeDao
import it.attendance100.mybicocca.data.datasource.badge.ElearningBadgeDataSource
import it.attendance100.mybicocca.data.model.badge.Badge
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeRepository @Inject constructor(
    private val dataSource: ElearningBadgeDataSource,
    private val dao: BadgeDao,
) {
    fun observeAll(): Flow<List<Badge>> = dao.observeAll()

    suspend fun refresh(): Result<Unit> = runCatching {
        val badges = dataSource.getBadges()
        dao.upsertAll(badges)
    }
}
