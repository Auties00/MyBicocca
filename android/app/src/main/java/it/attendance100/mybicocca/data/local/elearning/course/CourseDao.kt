package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query(
        "SELECT * FROM elearning_enrolled_courses " +
            "WHERE account_id = :accountId " +
            "ORDER BY sort_order, full_name"
    )
    fun observeEnrolled(accountId: String): Flow<List<EnrolledCourseEntity>>

    @Query("SELECT * FROM elearning_enrolled_courses WHERE account_id = :accountId AND course_id = :courseId")
    fun observeEnrolledOne(accountId: String, courseId: Int): Flow<EnrolledCourseEntity?>

    @Query(
        "SELECT * FROM elearning_course_sections " +
            "WHERE account_id = :accountId AND course_id = :courseId " +
            "ORDER BY section_number"
    )
    fun observeSections(accountId: String, courseId: Int): Flow<List<CourseSectionEntity>>

    @Query(
        "SELECT * FROM elearning_course_modules " +
            "WHERE account_id = :accountId AND course_id = :courseId " +
            "ORDER BY section_id, sort_order"
    )
    fun observeModules(accountId: String, courseId: Int): Flow<List<CourseModuleEntity>>

    @Query(
        "SELECT * FROM elearning_course_staff " +
            "WHERE account_id = :accountId AND course_id = :courseId " +
            "ORDER BY row_index"
    )
    fun observeStaff(accountId: String, courseId: Int): Flow<List<CourseStaffEntity>>

    @Query("SELECT * FROM elearning_course_syllabus WHERE account_id = :accountId AND course_id = :courseId")
    fun observeSyllabus(accountId: String, courseId: Int): Flow<CourseSyllabusEntity?>

    @Query(
        "SELECT * FROM elearning_activity_completion " +
            "WHERE account_id = :accountId AND course_id = :courseId"
    )
    fun observeCompletion(accountId: String, courseId: Int): Flow<List<ActivityCompletionEntity>>

    @Upsert
    suspend fun upsertEnrolled(rows: List<EnrolledCourseEntity>)

    @Upsert
    suspend fun upsertSections(rows: List<CourseSectionEntity>)

    @Upsert
    suspend fun upsertModules(rows: List<CourseModuleEntity>)

    @Upsert
    suspend fun upsertStaff(rows: List<CourseStaffEntity>)

    @Upsert
    suspend fun upsertSyllabus(row: CourseSyllabusEntity)

    @Upsert
    suspend fun upsertCompletion(row: ActivityCompletionEntity)

    @Upsert
    suspend fun upsertCompletionAll(rows: List<ActivityCompletionEntity>)

    @Query("DELETE FROM elearning_enrolled_courses WHERE account_id = :accountId")
    suspend fun deleteEnrolledForAccount(accountId: String)

    @Query("DELETE FROM elearning_course_sections WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun deleteSectionsForCourse(accountId: String, courseId: Int)

    @Query("DELETE FROM elearning_course_modules WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun deleteModulesForCourse(accountId: String, courseId: Int)

    @Query("DELETE FROM elearning_course_staff WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun deleteStaffForCourse(accountId: String, courseId: Int)

    @Query("DELETE FROM elearning_course_syllabus WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun deleteSyllabusForCourse(accountId: String, courseId: Int)

    @Query("UPDATE elearning_enrolled_courses SET is_favourite = :favourite WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun setFavourite(accountId: String, courseId: Int, favourite: Boolean)

    @Query("UPDATE elearning_enrolled_courses SET hidden = :hidden WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun setHidden(accountId: String, courseId: Int, hidden: Boolean)

    @Transaction
    suspend fun replaceEnrolled(accountId: String, rows: List<EnrolledCourseEntity>) {
        deleteEnrolledForAccount(accountId)
        if (rows.isNotEmpty()) upsertEnrolled(rows)
    }

    @Transaction
    suspend fun replaceCourseStructure(
        accountId: String,
        courseId: Int,
        sections: List<CourseSectionEntity>,
        modules: List<CourseModuleEntity>,
    ) {
        deleteSectionsForCourse(accountId, courseId)
        deleteModulesForCourse(accountId, courseId)
        if (sections.isNotEmpty()) upsertSections(sections)
        if (modules.isNotEmpty()) upsertModules(modules)
    }

    @Transaction
    suspend fun replaceCourseStaff(
        accountId: String,
        courseId: Int,
        staff: List<CourseStaffEntity>,
    ) {
        deleteStaffForCourse(accountId, courseId)
        if (staff.isNotEmpty()) upsertStaff(staff)
    }

    @Query("DELETE FROM elearning_activity_completion WHERE account_id = :accountId")
    suspend fun clearCompletionForAccount(accountId: String)

    @Query("DELETE FROM elearning_course_sections WHERE account_id = :accountId")
    suspend fun deleteAllSectionsForAccount(accountId: String)

    @Query("DELETE FROM elearning_course_modules WHERE account_id = :accountId")
    suspend fun deleteAllModulesForAccount(accountId: String)

    @Query("DELETE FROM elearning_course_staff WHERE account_id = :accountId")
    suspend fun deleteAllStaffForAccount(accountId: String)

    @Query("DELETE FROM elearning_course_syllabus WHERE account_id = :accountId")
    suspend fun deleteAllSyllabusForAccount(accountId: String)

    @Transaction
    suspend fun clearAllForAccount(accountId: String) {
        deleteEnrolledForAccount(accountId)
        deleteAllSectionsForAccount(accountId)
        deleteAllModulesForAccount(accountId)
        deleteAllStaffForAccount(accountId)
        deleteAllSyllabusForAccount(accountId)
        clearCompletionForAccount(accountId)
    }
}
