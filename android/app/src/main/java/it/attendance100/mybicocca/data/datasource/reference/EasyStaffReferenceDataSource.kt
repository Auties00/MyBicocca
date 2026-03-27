package it.attendance100.mybicocca.data.datasource.reference

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.model.reference.AcademicYear
import it.attendance100.mybicocca.data.model.reference.StudyProgram
import it.attendance100.mybicocca.data.model.reference.TeachingArea
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffReferenceDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getAcademicYears(): List<AcademicYear> = withContext(ioDispatcher) {
        easyStaffApi.core.getAcademicYears().map { dto ->
            AcademicYear(startYear = dto.startYear)
        }
    }

    suspend fun getTeachingAreas(): List<TeachingArea> = withContext(ioDispatcher) {
        easyStaffApi.core.getTeachingAreas().map { dto ->
            TeachingArea(code = dto.code, name = dto.name)
        }
    }

    suspend fun getStudyPrograms(academicYearStart: Int, teachingAreaCode: String? = null): List<StudyProgram> =
        withContext(ioDispatcher) {
            val academicYears = easyStaffApi.core.getAcademicYears()
            val academicYear = academicYears.firstOrNull { it.startYear == academicYearStart }
                ?: return@withContext emptyList()

            easyStaffApi.core.getStudyPrograms(
                academicYear = academicYear,
                teachingAreaCode = teachingAreaCode,
            ).map { dto ->
                StudyProgram(
                    code = dto.code,
                    name = dto.name,
                    degreeType = dto.degreeType,
                    teachingAreaCode = dto.teachingAreaCode,
                    yearCount = dto.years.size,
                )
            }
        }
}
