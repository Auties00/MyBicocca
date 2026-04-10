package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.data.model.studyplan.StudyPlanHeader
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plan_headers WHERE studentId = :studentId")
    fun observeHeaders(studentId: Long): Flow<List<StudyPlanHeader>>

    @Upsert
    suspend fun upsertAllHeaders(headers: List<StudyPlanHeader>)

    @Query("DELETE FROM study_plan_headers WHERE studentId = :studentId")
    suspend fun deleteHeadersByStudentId(studentId: Long)

    @Query("SELECT * FROM planned_courses WHERE planId = :planId")
    fun observeCourses(planId: Long): Flow<List<PlannedCourse>>

    @Query("SELECT * FROM study_plan_headers WHERE studentId = :studentId")
    suspend fun getHeadersByStudentId(studentId: Long): List<StudyPlanHeader>

    @Query("SELECT * FROM planned_courses WHERE planId = :planId")
    suspend fun getCoursesByPlanId(planId: Long): List<PlannedCourse>

    @Upsert
    suspend fun upsertAllCourses(courses: List<PlannedCourse>)

    @Query("DELETE FROM planned_courses WHERE planId = :planId")
    suspend fun deleteCoursesByPlanId(planId: Long)
}
