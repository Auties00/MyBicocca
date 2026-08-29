package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseSyllabusPointer
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.SyllabusInfo
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * State coverage for the course detail Scheda (syllabus) tab — the pure-Compose overview page of
 * the course detail subscreen. Rendered directly with a stateless [CourseDetails], so no
 * ViewModel, collapsing header, pager or media surface is mounted. Asserts the empty marker shows
 * when the course publishes no syllabus and the content marker shows once a syllabus with an info
 * tile is present. Anchored on [CourseDetailTestTags] and wrapped in the production [BicoccaTheme].
 */
@RunWith(AndroidJUnit4::class)
class SyllabusContentTest {

    @get:Rule
    val compose = createComposeRule()

    private fun enrolledCourse(): EnrolledCourse = EnrolledCourse(
        id = CourseId(1),
        shortName = "S",
        fullName = "Course",
        displayName = "Course",
        idNumber = null,
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

    private fun details(syllabus: CourseSyllabusPointer?): CourseDetails = CourseDetails(
        enrolled = enrolledCourse(),
        sections = emptyList(),
        staff = emptyList(),
        syllabus = syllabus,
    )

    private fun setSyllabus(syllabus: CourseSyllabusPointer?) {
        compose.setBicoccaContent {
                SyllabusContent(
                    details = details(syllabus),
                    listState = rememberLazyListState(),
                )

        }
    }

    @Test
    fun a_course_with_no_published_syllabus_shows_the_empty_state() {
        setSyllabus(null)

        compose.onNodeWithTag(CourseDetailTestTags.SYLLABUS_EMPTY).assertIsDisplayed()
    }

    @Test
    fun a_syllabus_with_an_info_tile_and_objectives_renders_the_content() {
        val syllabus = CourseSyllabusPointer(
            language = "it",
            exportPdfUrl = null,
            fields = listOf(
                CourseSyllabusPointer.SyllabusField(title = "Obiettivi", htmlContent = "<p>Imparare</p>"),
            ),
            info = SyllabusInfo.Empty.copy(
                credits = 6,
                hours = 48,
                objectives = "Conoscere i fondamenti dell'algebra lineare.",
            ),
        )
        setSyllabus(syllabus)

        compose.onNodeWithTag(CourseDetailTestTags.SYLLABUS_CONTENT).assertIsDisplayed()
    }
}
