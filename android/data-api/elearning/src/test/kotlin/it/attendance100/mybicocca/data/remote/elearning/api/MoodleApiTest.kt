package it.attendance100.mybicocca.data.remote.elearning.api

import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleCourseSection
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleEnrolledCourse
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleOption
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleTokenAuth
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

/**
 * Read-only calls of the generated Moodle API against the live site, which check that the
 * dumped descriptions decode real responses. Needs a web service token in `MOODLE_TOKEN`.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("live")
class MoodleApiTest {
    private val api = MoodleApi()
    private lateinit var auth: MoodleTokenAuth
    private var userId = 0
    private lateinit var courses: List<MoodleEnrolledCourse>

    @BeforeAll
    fun setUp() = runBlocking {
        val token = System.getenv("MOODLE_TOKEN")
        assumeTrue(!token.isNullOrBlank(), "Set MOODLE_TOKEN to run the Moodle live tests")
        auth = MoodleTokenAuth(token)
        userId = api.site.getSiteInfo(auth = auth).userId
        courses = api.course.getUsersCourses(userId, auth = auth)
    }

    @AfterAll
    fun tearDown() = api.close()

    private fun courseIds(): List<Int> = courses.map { it.id }.take(10)

    @Test
    fun `site info names the user`(): Unit = runBlocking {
        val info = api.site.getSiteInfo(auth = auth)
        assertTrue(info.userId > 0)
        assertTrue(info.functions.isNotEmpty(), "The token's service should expose functions")
    }

    @Test
    fun `invalid tokens are errors`() {
        val error = assertThrows<ElearningException> { runBlocking { api.site.getSiteInfo(auth = MoodleTokenAuth("0".repeat(32))) } }
        assertEquals("invalidtoken", error.errorCode)
    }

    @Test
    fun `enrolled courses and their contents decode`(): Unit = runBlocking {
        assumeTrue(courses.isNotEmpty(), "The user has no courses")
        courses.forEach { assertTrue(it.fullName.isNotBlank()) }
        var sections: List<MoodleCourseSection> = emptyList()
        for (course in courses.take(5)) {
            sections = api.course.getContents(course.id, auth = auth)
            if (sections.isNotEmpty()) break
        }
        assertTrue(sections.isNotEmpty(), "Some course should have sections")
        val filtered = api.course.getContents(
            courses.first().id,
            options = listOf(MoodleOption("excludecontents", "1")),
            auth = auth,
        )
        filtered.flatMap { it.modules }.forEach { assertTrue(it.contents.isEmpty(), "excludecontents should drop module contents") }
    }

    @Test
    fun `courses by field carry the course sheet custom fields`(): Unit = runBlocking {
        val course = api.course.getCoursesByField(field = "id", value = "60702", auth = auth).courses.single()
        val fields = course.customFields.associate { it.shortName to it.value }
        assertEquals("INF/01", fields["settore"])
        assertEquals("8", fields["cfu"])
    }

    @Test
    fun `groups, profiles and users decode`(): Unit = runBlocking {
        assumeTrue(courses.isNotEmpty(), "The user has no courses")
        api.course.getCourseUserGroups(courseId = courses.first().id, userId = userId, auth = auth)
        val users = api.user.getUsersByField("id", listOf(userId.toString()), auth = auth)
        assertEquals(userId, users.single().id)
        api.user.getUserPreferences(auth = auth)
        api.user.getPrivateFilesInfo(auth = auth)
    }

    @Test
    fun `grades decode`(): Unit = runBlocking {
        api.grade.getCourseGrades(auth = auth)
        assumeTrue(courses.isNotEmpty(), "The user has no courses")
        api.grade.getGradeItems(courses.first().id, userId = userId, auth = auth)
    }

    @Test
    fun `badges, calendar and completion decode`(): Unit = runBlocking {
        api.badge.getUserBadges(userId = userId, auth = auth)
        api.calendar.getActionEventsByTimesort(auth = auth)
        api.calendar.getCalendarUpcomingView(auth = auth)
        val now = java.time.LocalDate.now()
        val month = api.calendar.getCalendarMonthlyView(now.year, now.monthValue, auth = auth)
        assertTrue(month.weeks.isNotEmpty())
        assumeTrue(courses.isNotEmpty(), "The user has no courses")
        api.calendar.getActionEventsByCourse(courses.first().id, auth = auth)
        api.completion.getActivitiesCompletionStatus(courses.first().id, userId, auth = auth)
    }

    @Test
    fun `assignments decode`(): Unit = runBlocking {
        val response = api.assign.getAssignments(courseIds = courseIds(), auth = auth)
        val assignment = response.courses.flatMap { it.assignments }.firstOrNull()
        assumeTrue(assignment != null, "No assignment in the user's courses")
        api.assign.getSubmissionStatus(assignment!!.id, auth = auth)
    }

    @Test
    fun `forums decode`(): Unit = runBlocking {
        val forums = api.forum.getForumsByCourses(courseIds = courseIds(), auth = auth)
        assumeTrue(forums.isNotEmpty(), "No forum in the user's courses")
        for (forum in forums.take(5)) {
            val discussions = api.forum.getForumDiscussions(forum.id, auth = auth).discussions
            val discussion = discussions.firstOrNull() ?: continue
            val posts = api.forum.getDiscussionPosts(discussion.discussion, auth = auth).posts
            assertTrue(posts.isNotEmpty())
            api.forum.canAddDiscussion(forum.id, auth = auth)
            return@runBlocking
        }
    }

    @Test
    fun `quizzes decode`(): Unit = runBlocking {
        val quizzes = api.quiz.getQuizzesByCourses(courseIds = courseIds(), auth = auth).quizzes
        assumeTrue(quizzes.isNotEmpty(), "No quiz in the user's courses")
        val quiz = quizzes.first()
        api.quiz.getUserAttempts(quiz.id, status = "all", includePreviews = true, auth = auth)
        api.quiz.getUserBestGrade(quiz.id, auth = auth)
        api.quiz.getQuizAccessInformation(quiz.id, auth = auth)
    }

    @Test
    fun `messages decode`(): Unit = runBlocking {
        api.message.getConversations(userId, auth = auth)
        api.message.getUnreadConversationCounts(userId, auth = auth)
    }
}
