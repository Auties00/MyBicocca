package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.repository.ExamRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

// TEMPORARY: UI-testing fake for the Bacheca Esiti. Serves 3 canned outcomes and
// keeps accept/reject state in memory; everything else delegates to the real
// Esse3-backed impl. Bound in RepositoryModule — revert to ExamRepositoryImpl there.
@Singleton
class FakeExamRepository @Inject constructor(
    real: ExamRepositoryImpl,
) : ExamRepository by real {

    private val mutex = Mutex()
    private val results = mutableListOf(
        // Pending decision, plain numeric grade, with a teacher note.
        ExamResult(
            key = ExamCallKey(courseOfStudyId = 1L, activityId = 101L, callId = 1),
            applicationListId = 9_001L,
            publicationId = 5_001L,
            activityDescription = "ANALISI MATEMATICA I",
            examDateTime = LocalDateTime.now().minusDays(3).withHour(9).withMinute(30),
            grade = ExamGrade.Numeric(28),
            acknowledgment = AcknowledgmentStatus.NotViewed,
            publishedNote = "Ottima prova scritta, orale confermativo.",
            acknowledgmentDeadline = LocalDate.now().plusDays(5),
        ),
        // Pending decision, 31 = 30 e lode, deadline almost expired.
        ExamResult(
            key = ExamCallKey(courseOfStudyId = 1L, activityId = 102L, callId = 2),
            applicationListId = 9_002L,
            publicationId = 5_002L,
            activityDescription = "PROGRAMMAZIONE",
            examDateTime = LocalDateTime.now().minusDays(7).withHour(14).withMinute(0),
            grade = ExamGrade.Numeric(31),
            acknowledgment = AcknowledgmentStatus.Viewed,
            publishedNote = null,
            acknowledgmentDeadline = LocalDate.now().plusDays(1),
        ),
        // Not rejectable (no deadline) — renders without the accept/reject actions.
        ExamResult(
            key = ExamCallKey(courseOfStudyId = 1L, activityId = 103L, callId = 1),
            applicationListId = 9_003L,
            publicationId = 5_003L,
            activityDescription = "FISICA GENERALE",
            examDateTime = LocalDateTime.now().minusDays(1).withHour(11).withMinute(15),
            grade = ExamGrade.NotPassed,
            acknowledgment = AcknowledgmentStatus.NotViewed,
            publishedNote = "Prova insufficiente: rivedere la seconda parte.",
            acknowledgmentDeadline = null,
        ),
    )

    override suspend fun getExamResults(careerId: CareerId): List<ExamResult> {
        delay(600) // simulate network latency so loading states are visible
        return mutex.withLock { results.toList() }
    }

    override suspend fun acknowledgeExamResult(
        careerId: CareerId,
        applicationListId: Long,
        accept: Boolean,
    ) {
        delay(400)
        mutex.withLock { results.removeAll { it.applicationListId == applicationListId } }
    }
}
