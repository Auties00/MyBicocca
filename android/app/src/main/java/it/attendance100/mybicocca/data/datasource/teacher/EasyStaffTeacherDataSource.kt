package it.attendance100.mybicocca.data.datasource.teacher

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.model.teacher.Teacher
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffTeacherDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getTeachers(): List<Teacher> = withContext(ioDispatcher) {
        val academicYears = easyStaffApi.core.getAcademicYears()
        val currentAcademicYear = academicYears.firstOrNull() ?: return@withContext emptyList()

        easyStaffApi.core.getTeachers(currentAcademicYear).map { teacher ->
            Teacher(
                code = teacher.code,
                fullName = teacher.fullName,
                email = null,
                phone = null,
                facultyCode = teacher.primaryFacultyCode,
            )
        }
    }
}
