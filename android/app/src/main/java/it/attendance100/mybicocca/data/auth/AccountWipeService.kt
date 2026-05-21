package it.attendance100.mybicocca.data.auth

import it.attendance100.mybicocca.data.local.calendar.CalendarDao
import it.attendance100.mybicocca.data.local.calendar.CalendarSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.assignment.AssignmentDao
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeDao
import it.attendance100.mybicocca.data.local.elearning.course.CourseDao
import it.attendance100.mybicocca.data.local.elearning.forum.ForumDao
import it.attendance100.mybicocca.data.local.elearning.grade.GradeDao
import it.attendance100.mybicocca.data.local.elearning.message.MessageDao
import it.attendance100.mybicocca.data.local.elearning.quiz.QuizDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptDao
import it.attendance100.mybicocca.data.local.transcript.TranscriptSyncStateDao
import it.attendance100.mybicocca.domain.model.account.AccountId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountWipeService @Inject constructor(
    private val calendarDao: CalendarDao,
    private val calendarSyncStateDao: CalendarSyncStateDao,
    private val courseDao: CourseDao,
    private val assignmentDao: AssignmentDao,
    private val quizDao: QuizDao,
    private val forumDao: ForumDao,
    private val gradeDao: GradeDao,
    private val messageDao: MessageDao,
    private val badgeDao: BadgeDao,
    private val elearningSyncStateDao: ElearningSyncStateDao,
    private val transcriptDao: TranscriptDao,
    private val transcriptSyncStateDao: TranscriptSyncStateDao,
) {
    suspend fun wipe(accountId: AccountId) {
        wipeEsse3(accountId)
        wipeElearning(accountId)
    }

    private suspend fun wipeEsse3(accountId: AccountId) {
        val id = accountId.value
        runCatching { calendarDao.deleteForAccount(id) }
        runCatching { calendarSyncStateDao.deleteForAccount(id) }
        runCatching { transcriptDao.deleteRowsForAccount(id) }
        runCatching { transcriptDao.deleteStatsForAccount(id) }
        runCatching { transcriptSyncStateDao.deleteForAccount(id) }
    }

    private suspend fun wipeElearning(accountId: AccountId) {
        val id = accountId.value
        runCatching { courseDao.clearAllForAccount(id) }
        runCatching { assignmentDao.deleteForAccount(id) }
        runCatching { quizDao.clearAllForAccount(id) }
        runCatching { forumDao.clearAllForAccount(id) }
        runCatching { gradeDao.clearAllForAccount(id) }
        runCatching { messageDao.clearAllForAccount(id) }
        runCatching { badgeDao.deleteForAccount(id) }
        runCatching { elearningSyncStateDao.deleteForAccount(id) }
    }
}
