package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.reference.AcademicYear
import it.attendance100.mybicocca.data.model.reference.CourseType
import it.attendance100.mybicocca.data.model.reference.DidacticStructure
import it.attendance100.mybicocca.data.model.reference.StudyProgram
import it.attendance100.mybicocca.data.model.reference.TeachingArea
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferenceDao {
    // Academic years
    @Query("SELECT * FROM academic_years ORDER BY startYear DESC")
    fun observeAcademicYears(): Flow<List<AcademicYear>>

    @Upsert
    suspend fun upsertAcademicYears(years: List<AcademicYear>)

    @Query("DELETE FROM academic_years")
    suspend fun deleteAllAcademicYears()

    // Study programs
    @Query("SELECT * FROM study_programs ORDER BY name")
    fun observeStudyPrograms(): Flow<List<StudyProgram>>

    @Query("SELECT * FROM study_programs WHERE teachingAreaCode = :areaCode ORDER BY name")
    fun observeStudyProgramsByArea(areaCode: String): Flow<List<StudyProgram>>

    @Query("SELECT * FROM study_programs WHERE code = :code")
    suspend fun getStudyProgramByCode(code: String): StudyProgram?

    @Upsert
    suspend fun upsertStudyPrograms(programs: List<StudyProgram>)

    @Query("DELETE FROM study_programs")
    suspend fun deleteAllStudyPrograms()

    // Teaching areas
    @Query("SELECT * FROM teaching_areas ORDER BY name")
    fun observeTeachingAreas(): Flow<List<TeachingArea>>

    @Upsert
    suspend fun upsertTeachingAreas(areas: List<TeachingArea>)

    @Query("DELETE FROM teaching_areas")
    suspend fun deleteAllTeachingAreas()

    // Course types
    @Query("SELECT * FROM course_types ORDER BY description")
    fun observeCourseTypes(): Flow<List<CourseType>>

    @Upsert
    suspend fun upsertCourseTypes(types: List<CourseType>)

    @Query("DELETE FROM course_types")
    suspend fun deleteAllCourseTypes()

    // Didactic structures
    @Query("SELECT * FROM didactic_structures ORDER BY description")
    fun observeDidacticStructures(): Flow<List<DidacticStructure>>

    @Upsert
    suspend fun upsertDidacticStructures(structures: List<DidacticStructure>)

    @Query("DELETE FROM didactic_structures")
    suspend fun deleteAllDidacticStructures()
}
