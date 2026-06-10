package it.attendance100.mybicocca.data.local.questionnaire

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [QuestionnaireCacheDao] against a real in-memory Room database
 * (Robolectric). Exercises the two offline VAL_DID mirrors: the career-wide activities list
 * (whole-career replace, read back ordered by `cache_order`) and the per-(career, activity)
 * questionnaire header plus its units (the header row and the unit list replaced together in one
 * transaction, units read back ordered by `unit_order`). Each replace must scope its wipe so
 * sibling careers and sibling activities survive. All tables key on plain `career_id` /
 * `activity_choice_id` Long columns with no foreign keys, so no parent account/career rows are
 * required.
 *
 * Wave 2 (Android-runtime) test: Robolectric drives the actual Room-generated `_Impl`, pinned to
 * a Robolectric-supported SDK because the module compiles against a newer one.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuestionnaireCacheDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: QuestionnaireCacheDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.questionnaireCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `replaceActivities stores rows read back ordered by cache_order`() = runTest {
        dao.replaceActivities(
            careerId = 1L,
            rows = listOf(
                activity(1L, activityChoiceId = 30L, cacheOrder = 2, name = "Reti"),
                activity(1L, activityChoiceId = 10L, cacheOrder = 0, name = "Analisi"),
                activity(1L, activityChoiceId = 20L, cacheOrder = 1, name = "Algebra"),
            ),
        )

        assertThat(dao.getActivities(1L).map { it.activityName })
            .containsExactly("Analisi", "Algebra", "Reti").inOrder()
    }

    @Test
    fun `getActivities is empty for a career with no cached rows`() = runTest {
        dao.replaceActivities(1L, listOf(activity(1L, activityChoiceId = 10L, cacheOrder = 0, name = "Analisi")))

        assertThat(dao.getActivities(2L)).isEmpty()
    }

    @Test
    fun `replaceActivities swaps the prior career slice wholesale`() = runTest {
        dao.replaceActivities(
            careerId = 1L,
            rows = listOf(
                activity(1L, activityChoiceId = 10L, cacheOrder = 0, name = "old-a"),
                activity(1L, activityChoiceId = 20L, cacheOrder = 1, name = "old-b"),
            ),
        )

        dao.replaceActivities(1L, listOf(activity(1L, activityChoiceId = 99L, cacheOrder = 0, name = "fresh")))

        assertThat(dao.getActivities(1L).map { it.activityName }).containsExactly("fresh")
    }

    @Test
    fun `replaceActivities for one career leaves another career's rows intact`() = runTest {
        dao.replaceActivities(2L, listOf(activity(2L, activityChoiceId = 10L, cacheOrder = 0, name = "keep")))

        dao.replaceActivities(1L, listOf(activity(1L, activityChoiceId = 10L, cacheOrder = 0, name = "other")))

        assertThat(dao.getActivities(2L).map { it.activityName }).containsExactly("keep")
        assertThat(dao.getActivities(1L).map { it.activityName }).containsExactly("other")
    }

    @Test
    fun `getActivities round-trips every scalar field including nullable ones`() = runTest {
        dao.replaceActivities(
            careerId = 1L,
            rows = listOf(
                QuestionnaireActivityEntity(
                    careerId = 1L,
                    activityChoiceId = 10L,
                    cacheOrder = 0,
                    activityCode = null,
                    activityName = "Analisi Matematica",
                    credits = 9.0f,
                    courseYear = 1,
                    attendanceYear = null,
                    status = "ConfigurationError",
                ),
            ),
        )

        val stored = dao.getActivities(1L).single()
        assertThat(stored.activityCode).isNull()
        assertThat(stored.activityName).isEqualTo("Analisi Matematica")
        assertThat(stored.credits).isEqualTo(9.0f)
        assertThat(stored.courseYear).isEqualTo(1)
        assertThat(stored.attendanceYear).isNull()
        assertThat(stored.status).isEqualTo("ConfigurationError")
    }

    @Test
    fun `replaceActivityQuestionnaires stores the header and its units ordered by unit_order`() = runTest {
        dao.replaceActivityQuestionnaires(
            careerId = 1L,
            activityChoiceId = 10L,
            parentRow = header(1L, activityChoiceId = 10L, name = "Valutazione didattica"),
            unitRows = listOf(
                unit(1L, activityChoiceId = 10L, unitOrder = 2, name = "Lab"),
                unit(1L, activityChoiceId = 10L, unitOrder = 0, name = "Teoria"),
                unit(1L, activityChoiceId = 10L, unitOrder = 1, name = "Esercitazioni"),
            ),
        )

        assertThat(dao.getActivityQuestionnaires(1L, 10L)!!.questionnaireName)
            .isEqualTo("Valutazione didattica")
        assertThat(dao.getUnits(1L, 10L).map { it.teachingUnitName })
            .containsExactly("Teoria", "Esercitazioni", "Lab").inOrder()
    }

    @Test
    fun `getActivityQuestionnaires is null when the activity has no cached header`() = runTest {
        dao.replaceActivityQuestionnaires(
            careerId = 1L,
            activityChoiceId = 10L,
            parentRow = header(1L, activityChoiceId = 10L, name = "Valutazione"),
            unitRows = emptyList(),
        )

        assertThat(dao.getActivityQuestionnaires(1L, 99L)).isNull()
        assertThat(dao.getUnits(1L, 99L)).isEmpty()
    }

    @Test
    fun `replaceActivityQuestionnaires swaps the prior header and units for that activity`() = runTest {
        dao.replaceActivityQuestionnaires(
            careerId = 1L,
            activityChoiceId = 10L,
            parentRow = header(1L, activityChoiceId = 10L, name = "old"),
            unitRows = listOf(
                unit(1L, activityChoiceId = 10L, unitOrder = 0, name = "old-u1"),
                unit(1L, activityChoiceId = 10L, unitOrder = 1, name = "old-u2"),
            ),
        )

        dao.replaceActivityQuestionnaires(
            careerId = 1L,
            activityChoiceId = 10L,
            parentRow = header(1L, activityChoiceId = 10L, name = "new"),
            unitRows = listOf(unit(1L, activityChoiceId = 10L, unitOrder = 0, name = "new-u1")),
        )

        assertThat(dao.getActivityQuestionnaires(1L, 10L)!!.questionnaireName).isEqualTo("new")
        assertThat(dao.getUnits(1L, 10L).map { it.teachingUnitName }).containsExactly("new-u1")
    }

    @Test
    fun `replaceActivityQuestionnaires scopes its wipe to one activity within the same career`() = runTest {
        dao.replaceActivityQuestionnaires(
            careerId = 1L,
            activityChoiceId = 20L,
            parentRow = header(1L, activityChoiceId = 20L, name = "keep"),
            unitRows = listOf(unit(1L, activityChoiceId = 20L, unitOrder = 0, name = "keep-u")),
        )

        dao.replaceActivityQuestionnaires(
            careerId = 1L,
            activityChoiceId = 10L,
            parentRow = header(1L, activityChoiceId = 10L, name = "other"),
            unitRows = listOf(unit(1L, activityChoiceId = 10L, unitOrder = 0, name = "other-u")),
        )

        assertThat(dao.getActivityQuestionnaires(1L, 20L)!!.questionnaireName).isEqualTo("keep")
        assertThat(dao.getUnits(1L, 20L).map { it.teachingUnitName }).containsExactly("keep-u")
    }

    @Test
    fun `replaceActivityQuestionnaires scopes its wipe to one career for the same activity id`() = runTest {
        dao.replaceActivityQuestionnaires(
            careerId = 2L,
            activityChoiceId = 10L,
            parentRow = header(2L, activityChoiceId = 10L, name = "career-two"),
            unitRows = listOf(unit(2L, activityChoiceId = 10L, unitOrder = 0, name = "c2-u")),
        )

        dao.replaceActivityQuestionnaires(
            careerId = 1L,
            activityChoiceId = 10L,
            parentRow = header(1L, activityChoiceId = 10L, name = "career-one"),
            unitRows = listOf(unit(1L, activityChoiceId = 10L, unitOrder = 0, name = "c1-u")),
        )

        assertThat(dao.getActivityQuestionnaires(2L, 10L)!!.questionnaireName).isEqualTo("career-two")
        assertThat(dao.getUnits(2L, 10L).map { it.teachingUnitName }).containsExactly("c2-u")
    }

    @Test
    fun `header and units round-trip their nullable fields`() = runTest {
        dao.replaceActivityQuestionnaires(
            careerId = 1L,
            activityChoiceId = 10L,
            parentRow = ActivityQuestionnairesEntity(
                careerId = 1L,
                activityChoiceId = 10L,
                questionnaireId = null,
                questionnaireConfigId = null,
                questionnaireName = null,
                anonymous = true,
            ),
            unitRows = listOf(
                QuestionnaireUnitEntity(
                    careerId = 1L,
                    activityChoiceId = 10L,
                    unitOrder = 0,
                    teachingUnitName = "Teoria",
                    lecturerName = null,
                    partitionName = null,
                    completed = false,
                    tags = "tag-token",
                ),
            ),
        )

        val storedHeader = dao.getActivityQuestionnaires(1L, 10L)!!
        assertThat(storedHeader.questionnaireId).isNull()
        assertThat(storedHeader.questionnaireConfigId).isNull()
        assertThat(storedHeader.questionnaireName).isNull()
        assertThat(storedHeader.anonymous).isTrue()

        val storedUnit = dao.getUnits(1L, 10L).single()
        assertThat(storedUnit.lecturerName).isNull()
        assertThat(storedUnit.partitionName).isNull()
        assertThat(storedUnit.completed).isFalse()
        assertThat(storedUnit.tags).isEqualTo("tag-token")
    }

    private fun activity(careerId: Long, activityChoiceId: Long, cacheOrder: Int, name: String) =
        QuestionnaireActivityEntity(
            careerId = careerId,
            activityChoiceId = activityChoiceId,
            cacheOrder = cacheOrder,
            activityCode = "E3$activityChoiceId",
            activityName = name,
            credits = 6.0f,
            courseYear = 1,
            attendanceYear = 2024,
            status = "Compilable",
        )

    private fun header(careerId: Long, activityChoiceId: Long, name: String) =
        ActivityQuestionnairesEntity(
            careerId = careerId,
            activityChoiceId = activityChoiceId,
            questionnaireId = 500,
            questionnaireConfigId = 600,
            questionnaireName = name,
            anonymous = false,
        )

    private fun unit(careerId: Long, activityChoiceId: Long, unitOrder: Int, name: String) =
        QuestionnaireUnitEntity(
            careerId = careerId,
            activityChoiceId = activityChoiceId,
            unitOrder = unitOrder,
            teachingUnitName = name,
            lecturerName = "Prof. Bianchi",
            partitionName = null,
            completed = false,
            tags = "tags-$unitOrder",
        )
}
