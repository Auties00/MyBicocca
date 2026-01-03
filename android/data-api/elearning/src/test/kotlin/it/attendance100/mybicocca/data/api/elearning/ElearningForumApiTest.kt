package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningForumApiTest : ElearningTestBase() {

    @Test
    fun `forum operations work`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val coursesResponse = api.courses.getUserCourses(wsToken, siteInfo.userId)

        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val forumsResponse = api.forums.getForums(wsToken, courseIds)
            
            assertNotNull(forumsResponse)
            println("Found ${forumsResponse.forums.size} forums")
        }
    }

    @Test
    fun `getForumsForCourse works for all enrolled courses`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val coursesResponse = api.courses.getUserCourses(wsToken, siteInfo.userId)

        for (course in coursesResponse.courses) {
            println("Getting forums for course: ${course.fullName} (${course.id})")
            val forumsResponse = api.forums.getForumsForCourse(wsToken, course.id)
            assertNotNull(forumsResponse)
            println("Found ${forumsResponse.forums.size} forums for course ${course.id}")

            for (forum in forumsResponse.forums) {
                println("  Getting discussions for forum: ${forum.name} (${forum.id})")
                val discussionsResponse = api.forums.getForumDiscussions(wsToken, forum.id)
                assertNotNull(discussionsResponse)
                assertNotNull(discussionsResponse.discussions)
                println("  Found ${discussionsResponse.discussions.size} discussions")

                for (discussion in discussionsResponse.discussions) {
                    println("    Getting posts for discussion: ${discussion.name} (${discussion.id})")
                    // In ElearningForumDiscussion, 'id' is the discussion id.
                    val postsResponse = api.forums.getDiscussionPosts(wsToken, discussion.id)
                    assertNotNull(postsResponse)
                    assertNotNull(postsResponse.posts)
                    println("    Found ${postsResponse.posts.size} posts")
                }
            }
        }
    }
}
