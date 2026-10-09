package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.badge.Badge
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningBadgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoElearningBadgeRepository @Inject constructor() : ElearningBadgeRepository {

    private val badges = listOf(
        Badge(
            id = 801,
            name = "Sicurezza nei laboratori",
            description = "Corso di formazione sulla sicurezza completato.",
            imageUrl = null,
            issuedAt = DemoClock.instant(DemoClock.now.minusMonths(20)),
            courseId = null,
        ),
        Badge(
            id = 802,
            name = "SQL Practitioner",
            description = "Tutte le esercitazioni SQL consegnate entro la scadenza.",
            imageUrl = null,
            issuedAt = DemoClock.instant(DemoClock.now.minusDays(12)),
            courseId = CourseId(DemoCourses.databases.moodleCourseId),
        ),
    )

    override fun observeBadges(accountId: AccountId, courseId: CourseId?): Flow<Loadable<List<Badge>>> =
        flowOf(Loadable.Loaded(if (courseId == null) badges else badges.filter { it.courseId == courseId }))

    override suspend fun refreshBadges(accountId: AccountId, courseId: CourseId?, force: Boolean) = Unit

    override suspend fun clearForAccount(accountId: AccountId) = Unit
}
