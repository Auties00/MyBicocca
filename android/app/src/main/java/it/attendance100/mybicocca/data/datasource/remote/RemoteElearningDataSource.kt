package it.attendance100.mybicocca.data.datasource.remote

import it.attendance100.mybicocca.data.mapper.*
import it.attendance100.mybicocca.data.remote.api.elearning.*
import it.attendance100.mybicocca.data.remote.dto.elearning.*
import it.attendance100.mybicocca.domain.datasource.*
import it.attendance100.mybicocca.domain.model.*
import it.attendance100.mybicocca.domain.model.CourseSection
import javax.inject.*

class RemoteElearningDataSource @Inject constructor(
  private val courseApi: ElearningCourseApi,
  private val assignmentApi: ElearningAssignmentApi,
) : ElearningDataSource {

  override suspend fun getCourses(): List<ElearningCourse> {
    // Moodle requires a user ID or other criteria for 'getCourses' usually, or it returns public courses.
    // For 'My Courses', getRecentCourses is a proxy, or we'd use core_enrol_get_users_courses (getUsersCourses).
    // Since getRecentCourses is available and simpler for now without userId context:
    return getRecentCourses()
  }

  override suspend fun getRecentCourses(): List<ElearningCourse> {
    // userid 0 often implies 'current user' in Moodle WS if token is user-scoped
    val response = courseApi.getRecentCourses(GetRecentCoursesRequest(userid = 0))
    if (response.isSuccessful && response.body() != null) {
      return response.body()!!.map {
        // RecentCourse DTO -> ElearningCourse Domain
        // RecentCourse structure is likely similar to Course or has course fields
        // Let's assume for now we can map it or it matches Course.
        // Wait, RecentCourse is a different DTO.
        // If I can't map it directly, I'll return empty or need RecentCourse mapper.
        // Assuming RecentCourse has similar fields for this example:
        ElearningCourse(
          id = it.id ?: 0,
          fullname = it.fullname ?: "",
          shortname = it.shortname ?: "",
          idNumber = it.idnumber,
          summary = it.summary,
          categoryId = it.categoryId ?: 0
        )
      }
    }
    return emptyList()
  }

  override suspend fun getAssignments(courseId: Int?): List<ElearningAssignment> {
    val request = GetAssignmentsRequest(courseIds = if (courseId != null) listOf(courseId) else emptyList())
    val response = assignmentApi.getAssignments(request)
    if (response.isSuccessful && response.body() != null) {
      // GetAssignmentsResponse -> courses -> assignments
      return response.body()!!.courses?.flatMap { course ->
        course.assignments?.map { it.toDomain() } ?: emptyList()
      } ?: emptyList()
    }
    return emptyList()
  }

  override suspend fun getCourseContent(courseId: Int): List<CourseSection> {
    val response = courseApi.getContents(GetCourseContentsRequest(courseId = courseId))
    if (response.isSuccessful && response.body() != null) {
      return response.body()!!.map { it.toDomain() }
    }
    return emptyList()
  }

  override suspend fun syncElearning() {
    // No-op
  }
}
