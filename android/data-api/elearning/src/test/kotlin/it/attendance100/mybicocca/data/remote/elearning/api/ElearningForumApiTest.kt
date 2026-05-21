package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningForumApiTest : ElearningTestApiBase() {

    @Test
    suspend fun getForums() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val forumsResponse = api.forums.getForums(session.wsToken, courseIds)
            assertNotNull(forumsResponse)
        }
    }

    @Test
    suspend fun getForumsForCourse() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        for (course in coursesResponse.courses) {
            val forumsResponse = api.forums.getForumsForCourse(session.wsToken, course.id)
            assertNotNull(forumsResponse)
        }
    }

    @Test
    suspend fun getForumDiscussions() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        for (course in coursesResponse.courses) {
            val forumsResponse = api.forums.getForumsForCourse(session.wsToken, course.id)
            for (forum in forumsResponse.forums) {
                val discussionsResponse = api.forums.getForumDiscussions(session.wsToken, forum.id)
                assertNotNull(discussionsResponse)
                assertNotNull(discussionsResponse.discussions)
            }
        }
    }

    @Test
    suspend fun getDiscussionPosts() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        for (course in coursesResponse.courses) {
            val forumsResponse = api.forums.getForumsForCourse(session.wsToken, course.id)
            for (forum in forumsResponse.forums) {
                val discussionsResponse = api.forums.getForumDiscussions(session.wsToken, forum.id)
                for (discussion in discussionsResponse.discussions) {
                    val postsResponse = api.forums.getDiscussionPosts(session.wsToken, discussion.id)
                    assertNotNull(postsResponse)
                    assertNotNull(postsResponse.posts)
                }
            }
        }
    }
}
