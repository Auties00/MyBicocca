package it.attendance100.mybicocca.data.datasource.exam

import it.attendance100.mybicocca.data.api.easystaff.EasyStaffApi
import it.attendance100.mybicocca.data.dto.easystaff.EasyStaffScheduledExam
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasyStaffExamDataSource @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getScheduledExams(
        programCode: String?,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<ExamCall> = withContext(ioDispatcher) {
        if (programCode == null) return@withContext emptyList()

        val academicYears = easyStaffApi.core.getAcademicYears()
        val currentAcademicYear = academicYears.firstOrNull() ?: return@withContext emptyList()

        val programs = easyStaffApi.core.getStudyPrograms(currentAcademicYear)
        val program = programs.firstOrNull { it.code == programCode } ?: return@withContext emptyList()

        val exams = easyStaffApi.exams.getExamsByProgram(
            studyProgram = program,
            yearsOfStudy = program.years,
            startDate = startDate,
            endDate = endDate,
        )

        exams.mapNotNull { exam ->
            val id = exam.eventId.toLongOrNull() ?: return@mapNotNull null
            ExamCall(
                id = id,
                careerId = 0L,
                activityName = exam.subjectName,
                date = exam.date,
                startTime = exam.startTime,
                endTime = exam.endTime,
                room = exam.room,
                building = exam.building,
                examinerEmails = exam.examiners.mapNotNull { it.email },
            )
        }
    }
}
