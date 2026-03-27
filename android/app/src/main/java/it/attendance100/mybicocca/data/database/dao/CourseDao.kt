package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.course.Course
import it.attendance100.mybicocca.data.model.course.CourseGrade
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun observeAll(): Flow<List<Course>>

    @Upsert
    suspend fun upsertAll(courses: List<Course>)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()

    @Query("SELECT * FROM course_grades")
    fun observeGrades(): Flow<List<CourseGrade>>

    @Upsert
    suspend fun upsertGrades(grades: List<CourseGrade>)

    @Query("DELETE FROM course_grades")
    suspend fun deleteAllGrades()
}
