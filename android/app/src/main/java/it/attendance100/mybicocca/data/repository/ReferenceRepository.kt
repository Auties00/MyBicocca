package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.ReferenceDao
import it.attendance100.mybicocca.data.datasource.reference.EasyStaffReferenceDataSource
import it.attendance100.mybicocca.data.datasource.reference.Esse3ReferenceDataSource
import it.attendance100.mybicocca.data.model.reference.AcademicYear
import it.attendance100.mybicocca.data.model.reference.CourseType
import it.attendance100.mybicocca.data.model.reference.DidacticStructure
import it.attendance100.mybicocca.data.model.reference.StudyProgram
import it.attendance100.mybicocca.data.model.reference.TeachingArea
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferenceRepository @Inject constructor(
    private val easyStaffReference: EasyStaffReferenceDataSource,
    private val esse3Reference: Esse3ReferenceDataSource,
    private val dao: ReferenceDao,
) {
    // Observe
    fun observeAcademicYears(): Flow<List<AcademicYear>> = dao.observeAcademicYears()
    fun observeStudyPrograms(): Flow<List<StudyProgram>> = dao.observeStudyPrograms()
    fun observeStudyProgramsByArea(areaCode: String): Flow<List<StudyProgram>> = dao.observeStudyProgramsByArea(areaCode)
    fun observeTeachingAreas(): Flow<List<TeachingArea>> = dao.observeTeachingAreas()
    fun observeCourseTypes(): Flow<List<CourseType>> = dao.observeCourseTypes()
    fun observeDidacticStructures(): Flow<List<DidacticStructure>> = dao.observeDidacticStructures()

    suspend fun getStudyProgramByCode(code: String): StudyProgram? = dao.getStudyProgramByCode(code)

    // Refresh all reference data from both providers in parallel
    suspend fun refreshAll(academicYearStart: Int): Result<Unit> {
        val results = coroutineScope {
            listOf(
                async { runCatching { refreshSchedulingData(academicYearStart) } },
                async { runCatching { refreshStructureData() } },
            ).map { it.await() }
        }
        return if (results.any { it.isSuccess }) Result.success(Unit)
        else Result.failure(results.first { it.isFailure }.exceptionOrNull()!!)
    }

    private suspend fun refreshSchedulingData(academicYearStart: Int) {
        val years = easyStaffReference.getAcademicYears()
        dao.upsertAcademicYears(years)

        val areas = easyStaffReference.getTeachingAreas()
        dao.upsertTeachingAreas(areas)

        val programs = easyStaffReference.getStudyPrograms(academicYearStart)
        dao.upsertStudyPrograms(programs)
    }

    private suspend fun refreshStructureData() {
        val types = esse3Reference.getCourseTypes()
        dao.upsertCourseTypes(types)

        val structures = esse3Reference.getDidacticStructures()
        dao.upsertDidacticStructures(structures)
    }
}
