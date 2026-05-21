package it.attendance100.mybicocca.data.remote.bicoccapp.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappWizardCourse
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappWizardCoursesResponse
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappWizardDegree
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappWizardDepartmentsResponse
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappWizardDegreesResponse
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappWizardDepartment
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappWizardLesson
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappWizardLessonsResponse
import kotlinx.serialization.json.Json

/**
 * API client for course selection wizard operations.
 */
class BicoccappWizardApi(
    client: HttpClient,
    json: Json
) : BicoccappAbstractApi(client, json) {

    /**
     * Retrieves available wizard categories (departments/faculties).
     *
     * @return Categories list.
     */
    suspend fun getDepartments(

    ): List<BicoccappWizardDepartment> {
        val response = executeJsonGet<BicoccappWizardDepartmentsResponse>("/wizard/categories")
        return response.departments
    }

    /**
     * Retrieves available degree types for a department.
     *
     * @param department The department of the degrees.
     * @return Degrees list.
     */
    suspend fun getDegrees(
        department: BicoccappWizardDepartment
    ): List<BicoccappWizardDegree> {
        val response = executeJsonGet<BicoccappWizardDegreesResponse>("/wizard/degrees") {
            parameter("category_code", department.code)
        }
        return response.degrees
    }

    /**
     * Retrieves available degree programs for a degree.
     *
     * @param department The department of the degree.
     * @param degree The degree of the courses.
     * @return Courses list.
     */
    suspend fun getCourses(
        department: BicoccappWizardDepartment,
        degree: BicoccappWizardDegree
    ): List<BicoccappWizardCourse> {
        val response = executeJsonGet<BicoccappWizardCoursesResponse>("/wizard/courses") {
            parameter("category_code", department.code)
            parameter("degree_code", degree.code)
        }
        return response.courses
    }

    /**
     * Retrieves lessons for a degree program.
     *
     * @param department The department of the degree.
     * @param degree The degree of the course.
     * @param course The course of the lessons.
     * @return Lessons grouped by year.
     */
    suspend fun getLessons(
        department: BicoccappWizardDepartment,
        degree: BicoccappWizardDegree,
        course: BicoccappWizardCourse
    ): Map<String, List<BicoccappWizardLesson>> {
        val response = executeJsonGet<BicoccappWizardLessonsResponse>("/wizard/lessons") {
            parameter("category_code", department.code)
            parameter("degree_code", degree.code)
            parameter("course_code", course.code)
        }
        return response.lessonsByYear
    }

    /**
     * Retrieves user's enrolled courses.
     *
     * @param enrollmentId Enrollment number (matricola).
     * @return User's courses.
     */
    suspend fun getUserCourses(
        enrollmentId: String
    ): Map<String, List<BicoccappWizardLesson>> {
        val response = executeJsonGet<BicoccappWizardLessonsResponse>("/wizard/user_courses") {
            parameter("matricId", enrollmentId)
        }
        return response.lessonsByYear
    }
}
