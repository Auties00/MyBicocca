package it.attendance100.mybicocca.domain.datasource

import it.attendance100.mybicocca.domain.model.*

interface ElearningDataSource {
  /**
   * Retrieves all elearning courses
   */
  suspend fun getCourses(): List<ElearningCourse>

  /**
   * Retrieves recent elearning courses
   */
  suspend fun getRecentCourses(): List<ElearningCourse>

  /**
   * Retrieves assignments, optionally filtered by course ID
   */
  suspend fun getAssignments(courseId: Int? = null): List<ElearningAssignment>

  /**
   * Retrieves the content sections of a specific course
   */
  suspend fun getCourseContent(courseId: Int): List<CourseSection>

  /**
   * Syncs elearning data from the server
   */
  suspend fun syncElearning()
}
