package it.attendance100.mybicocca.domain.model.elearning.course

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import org.junit.Test

/**
 * A student with a bachelor's started in 2022 and a master's started in 2026 is enrolled in both
 * careers' Moodle courses; [CourseCareers] tells them apart from the idNumber alone.
 */
class CourseCareersTest {

    private val bachelor = career(id = 1, programCode = "E3101Q", startYear = 2022, level = CourseLevel.Bachelor)
    private val master = career(id = 2, programCode = "F1802Q", startYear = 2026, level = CourseLevel.Master)
    private val careers = CourseCareers(listOf(bachelor, master), selected = master.id)

    @Test
    fun aCourseOfTheCareersProgramme_belongsToThatCareer() {
        assertThat(careers.ownerOf(code("2627-1-F1802Q107"))).isEqualTo(master.id)
        assertThat(careers.ownerOf(code("2425-3-E3101Q113"))).isEqualTo(bachelor.id)
    }

    @Test
    fun aCourseOfAnotherProgramme_goesByItsCohort() {
        // Cohort 2022: a third-year course in 2024/25.
        assertThat(careers.ownerOf(code("2425-3-A1234B001"))).isEqualTo(bachelor.id)
        // Cohort 2026: a first-year course in 2026/27.
        assertThat(careers.ownerOf(code("2627-1-A1234B001"))).isEqualTo(master.id)
    }

    @Test
    fun aCourseTakenAfterTheStandardYears_staysWithItsCareer() {
        // A third-year course retaken in 2025/26 points to cohort 2023, still before the master's.
        assertThat(careers.ownerOf(code("2526-3-A1234B001"))).isEqualTo(bachelor.id)
    }

    @Test
    fun aCourseWithoutAYearOfStudy_belongsToNoCareer() {
        assertThat(careers.ownerOf(code("2526-E3102Q-TUTORATO"))).isNull()
        assertThat(careers.ownerOf(code(null))).isNull()
    }

    @Test
    fun aSingleCareer_ownsEveryCourseWithAYear() {
        val single = CourseCareers(listOf(master), selected = master.id)

        assertThat(single.ownerOf(code("2425-3-E3101Q113"))).isEqualTo(master.id)
    }

    @Test
    fun filterOptions_listTheSelectedCareersYearsAndTheOtherCareers() {
        val options = careers.filterOptions(
            listOf(
                course(1, "2627-1-F1802Q107"),
                course(2, "2425-3-E3101Q113"),
                course(3, "2324-2-E3101Q105"),
                course(4, "2526-E3102Q-TUTORATO"),
            ),
        )

        assertThat(options.years).containsExactly(StudyYear(1))
        assertThat(options.otherCareers).containsExactly(bachelor)
    }

    @Test
    fun resolve_fallsBackToAllOnceAChipIsGone() {
        val options = CourseFilterOptions(years = listOf(StudyYear(1)), otherCareers = listOf(bachelor))

        assertThat(options.resolve(CourseFilter.ByYear(StudyYear(1)))).isEqualTo(CourseFilter.ByYear(StudyYear(1)))
        assertThat(options.resolve(CourseFilter.ByYear(StudyYear(3)))).isEqualTo(CourseFilter.All)
        assertThat(options.resolve(CourseFilter.ByCareer(bachelor.id))).isEqualTo(CourseFilter.ByCareer(bachelor.id))
        assertThat(options.resolve(CourseFilter.ByCareer(master.id))).isEqualTo(CourseFilter.All)
    }

    private fun code(idNumber: String?) = CourseCode.parse(idNumber)

    private fun career(id: Long, programCode: String, startYear: Int, level: CourseLevel) = Career(
        id = CareerId(id),
        enrollmentTraitId = id,
        programId = id,
        easyStaffProgramCode = programCode,
        academicYearEnrollmentId = id,
        studentNumber = "00000$id",
        description = programCode,
        academicYear = startYear,
        status = CareerStatus.ACTIVE,
        level = level,
    )

    private fun course(id: Int, idNumber: String?) = EnrolledCourse(
        id = CourseId(id),
        shortName = "S$id",
        fullName = "Course $id",
        displayName = "Course $id",
        idNumber = idNumber,
        summary = null,
        courseImageUrl = null,
        format = null,
        language = null,
        categoryId = null,
        progress = null,
        completed = false,
        completionEnabled = false,
        startDate = null,
        endDate = null,
        lastAccessDate = null,
        isFavourite = false,
        hidden = false,
    )
}
