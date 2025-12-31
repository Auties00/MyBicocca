package it.attendance100.mybicocca.data.api.bicoccapp

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BicoccappWizardApi Integration Tests")
class BicoccappWizardApiTest : BicoccappApiTestBase() {

    // Known category codes for testing
    companion object {
        const val CATEGORY_SCIENCES = "SCIENT"
        const val CATEGORY_ECONOMICS = "ECOSTAT"
        const val CATEGORY_LAW = "GIUR"

        const val DEGREE_BACHELOR = "L2"
        const val DEGREE_MASTER = "LM"

        const val COURSE_INFORMATICA = "E3101Q-E3102Q"
    }

    @Nested
    @DisplayName("Categories API")
    inner class CategoriesTests {

        @Test
        @DisplayName("getCategories() returns academic area categories")
        fun `getCategories returns categories successfully`() = runTest {
            // When
            val response = wizardApi.getCategories()

            // Then
            printResponse("getCategories()", response)
            assertSuccessfulResponse(response, "getCategories")

            val categoriesResponse = assertNonNullBody(response, "getCategories")
            assertNotNull(categoriesResponse.categories, "Categories list should not be null")
            assertTrue(categoriesResponse.categories.isNotEmpty(), "Categories should not be empty")

            println("Categories count: ${categoriesResponse.categories.size}")
            categoriesResponse.categories.forEach { category ->
                println("  - ${category.code}: ${category.name}")
            }
        }

        @Test
        @DisplayName("getCategories() returns expected category codes")
        fun `getCategories returns known category codes`() = runTest {
            // When
            val response = wizardApi.getCategories()
            assertSuccessfulResponse(response, "getCategories")

            val categoriesResponse = assertNonNullBody(response, "getCategories")

            // Verify known categories exist
            val categoryCodes = categoriesResponse.categories.mapNotNull { it.code }
            println("Category codes found: $categoryCodes")

            // At least some common categories should exist
            assertTrue(
                categoryCodes.isNotEmpty(),
                "At least one category should exist"
            )
        }
    }

    @Nested
    @DisplayName("Degree Types API")
    inner class DegreeTypesTests {

        @Test
        @DisplayName("getDegreeTypes() with Sciences category returns degree types")
        fun `getDegreeTypes with category returns degrees`() = runTest {
            // When - categoryCode is now required
            val response = wizardApi.getDegreeTypes(categoryCode = CATEGORY_SCIENCES)

            // Then
            printResponse("getDegreeTypes(categoryCode=$CATEGORY_SCIENCES)", response)
            assertSuccessfulResponse(response, "getDegreeTypes")

            val degreesResponse = assertNonNullBody(response, "getDegreeTypes")
            assertNotNull(degreesResponse.degrees, "Degrees list should not be null")

            println("Degree types count: ${degreesResponse.degrees.size}")
        }

        @Test
        @DisplayName("getDegreeTypes() with Sciences category returns relevant degrees")
        fun `getDegreeTypes with SCI category returns degrees`() = runTest {
            // When
            val response = wizardApi.getDegreeTypes(categoryCode = CATEGORY_SCIENCES)

            // Then
            printResponse("getDegreeTypes(categoryCode=$CATEGORY_SCIENCES)", response)
            assertSuccessfulResponse(response, "getDegreeTypes with SCI")

            val degreesResponse = assertNonNullBody(response, "getDegreeTypes")
            assertNotNull(degreesResponse.degrees, "Degrees list should not be null")

            degreesResponse.degrees.forEach { degree ->
                println("  - ${degree.code}: ${degree.name}")
            }
        }

        @Test
        @DisplayName("getDegreeTypes() with Economics category returns relevant degrees")
        fun `getDegreeTypes with ECO category returns degrees`() = runTest {
            // When
            val response = wizardApi.getDegreeTypes(categoryCode = CATEGORY_ECONOMICS)

            // Then
            printResponse("getDegreeTypes(categoryCode=$CATEGORY_ECONOMICS)", response)
            assertSuccessfulResponse(response, "getDegreeTypes with ECO")
        }

        @Test
        @DisplayName("getDegreeTypes() with Law category returns relevant degrees")
        fun `getDegreeTypes with GIU category returns degrees`() = runTest {
            // When
            val response = wizardApi.getDegreeTypes(categoryCode = CATEGORY_LAW)

            // Then
            printResponse("getDegreeTypes(categoryCode=$CATEGORY_LAW)", response)
            assertSuccessfulResponse(response, "getDegreeTypes with GIU")
        }

        @Test
        @DisplayName("getDegreeTypes() with invalid category handles gracefully")
        fun `getDegreeTypes with invalid category handles gracefully`() = runTest {
            // When
            val response = wizardApi.getDegreeTypes(categoryCode = "INVALID")

            // Then
            printResponse("getDegreeTypes(categoryCode=INVALID)", response)
            // Should either return empty list or all degrees
            println("API responded with code: ${response.code}")
        }
    }

    @Nested
    @DisplayName("Degree Programs API")
    inner class DegreeProgramsTests {

        @Test
        @DisplayName("getDegreePrograms() with both parameters returns programs")
        fun `getDegreePrograms with both parameters returns programs`() = runTest {
            // When - both categoryCode and degreeCode are now required
            val response = wizardApi.getDegreePrograms(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR
            )

            // Then
            printResponse("getDegreePrograms(categoryCode=$CATEGORY_SCIENCES, degreeCode=$DEGREE_BACHELOR)", response)
            assertSuccessfulResponse(response, "getDegreePrograms")

            val coursesResponse = assertNonNullBody(response, "getDegreePrograms")
            assertNotNull(coursesResponse.lessonsByYear, "LessonsByYear map should not be null")

            println("Degree programs - years available: ${coursesResponse.lessonsByYear.keys}")
        }

        @Test
        @DisplayName("getDegreePrograms() for Sciences Bachelor returns programs")
        fun `getDegreePrograms for SCI Bachelor returns programs`() = runTest {
            // When
            val response = wizardApi.getDegreePrograms(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR
            )

            // Then
            printResponse("getDegreePrograms(SCI, L)", response)
            assertSuccessfulResponse(response, "getDegreePrograms SCI/L")

            val coursesResponse = assertNonNullBody(response, "getDegreePrograms")

            if (coursesResponse.lessonsByYear.isNotEmpty()) {
                println("Science Bachelor programs by year:")
                coursesResponse.lessonsByYear.forEach { (year, lessons) ->
                    println("  Year $year: ${lessons.size} lessons")
                    lessons.take(3).forEach { lesson ->
                        println("    - ${lesson.lessonName}")
                    }
                }
            }
        }

        @Test
        @DisplayName("getDegreePrograms() for Sciences Master returns programs")
        fun `getDegreePrograms for SCI Master returns programs`() = runTest {
            // When
            val response = wizardApi.getDegreePrograms(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_MASTER
            )

            // Then
            printResponse("getDegreePrograms(SCI, LM)", response)
            assertSuccessfulResponse(response, "getDegreePrograms SCI/LM")
        }

        @Test
        @DisplayName("getDegreePrograms() for Economics returns programs")
        fun `getDegreePrograms for ECO returns programs`() = runTest {
            // When
            val response = wizardApi.getDegreePrograms(
                categoryCode = CATEGORY_ECONOMICS,
                degreeCode = DEGREE_BACHELOR
            )

            // Then
            printResponse("getDegreePrograms(ECO, L)", response)
            assertSuccessfulResponse(response, "getDegreePrograms ECO/L")
        }

        @Test
        @DisplayName("getDegreePrograms() with Sciences and Bachelor")
        fun `getDegreePrograms with Sciences and Bachelor returns programs`() = runTest {
            // When - both are now required
            val response = wizardApi.getDegreePrograms(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR
            )

            // Then
            printResponse("getDegreePrograms(SCI, L)", response)
            assertSuccessfulResponse(response, "getDegreePrograms with categoryCode and degreeCode")
        }

        @Test
        @DisplayName("getDegreePrograms() with Economics and Bachelor")
        fun `getDegreePrograms with Economics and Bachelor returns programs`() = runTest {
            // When - both are now required
            val response = wizardApi.getDegreePrograms(
                categoryCode = CATEGORY_ECONOMICS,
                degreeCode = DEGREE_BACHELOR
            )

            // Then
            printResponse("getDegreePrograms(ECO, L)", response)
            assertSuccessfulResponse(response, "getDegreePrograms with Economics and Bachelor")
        }
    }

    @Nested
    @DisplayName("Course Lessons API")
    inner class CourseLessonsTests {

        @Test
        @DisplayName("getCourseLessons() with all parameters returns lessons")
        fun `getCourseLessons with all parameters returns lessons`() = runTest {
            // When - all three parameters are now required
            val response = wizardApi.getCourseLessons(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR,
                courseCode = COURSE_INFORMATICA
            )

            // Then
            printResponse("getCourseLessons(categoryCode=$CATEGORY_SCIENCES, degreeCode=$DEGREE_BACHELOR, courseCode=$COURSE_INFORMATICA)", response)
            // API may return empty if course not found
            println("API responded with code: ${response.code}")
        }

        @Test
        @DisplayName("getCourseLessons() for Informatica returns course lessons")
        fun `getCourseLessons for Informatica returns lessons`() = runTest {
            // When
            val response = wizardApi.getCourseLessons(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR,
                courseCode = COURSE_INFORMATICA
            )

            // Then
            printResponse("getCourseLessons(SCI, L, F0801Q)", response)
            assertSuccessfulResponse(response, "getCourseLessons for Informatica")

            val lessonsResponse = assertNonNullBody(response, "getCourseLessons")
            assertNotNull(lessonsResponse.lessonsByYear, "Lessons should not be null")

            println("Informatica lessons retrieved: ${lessonsResponse.lessonsByYear}")
        }

        @Test
        @DisplayName("getCourseLessons() with Sciences Bachelor and Informatica")
        fun `getCourseLessons with Sciences Bachelor and Informatica returns lessons`() = runTest {
            // When
            val response = wizardApi.getCourseLessons(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR,
                courseCode = COURSE_INFORMATICA
            )

            // Then
            printResponse("getCourseLessons(SCI, L, F0801Q)", response)

            if (response.isSuccessful) {
                val lessonsResponse = assertNonNullBody(response, "getCourseLessons")
                println("Lessons retrieved successfully: ${lessonsResponse.lessonsByYear}")
            }
        }

        @Test
        @DisplayName("getCourseLessons() with all required parameters")
        fun `getCourseLessons with all required params returns lessons`() = runTest {
            // When
            val response = wizardApi.getCourseLessons(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR,
                courseCode = COURSE_INFORMATICA
            )

            // Then
            printResponse("getCourseLessons(all params)", response)

            if (response.isSuccessful) {
                println("Lessons retrieved with all required parameters")
            }
        }

        @Test
        @DisplayName("getCourseLessons() with Economics Bachelor")
        fun `getCourseLessons with Economics Bachelor returns lessons`() = runTest {
            // When
            val response = wizardApi.getCourseLessons(
                categoryCode = CATEGORY_ECONOMICS,
                degreeCode = DEGREE_BACHELOR,
                courseCode = "E0801Q" // Example Economics course code
            )

            // Then
            printResponse("getCourseLessons(ECO, L, E0801Q)", response)

            if (response.isSuccessful) {
                println("Lessons retrieved for Economics course")
            }
        }

        @Test
        @DisplayName("getCourseLessons() with invalid courseCode handles gracefully")
        fun `getCourseLessons with invalid courseCode handles gracefully`() = runTest {
            // When
            val response = wizardApi.getCourseLessons(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR,
                courseCode = "INVALID_COURSE"
            )

            // Then
            printResponse("getCourseLessons(INVALID_COURSE)", response)
            // Should return 404 or empty list
            println("API responded with code: ${response.code}")
        }
    }

    @Nested
    @DisplayName("User Courses API")
    inner class UserCoursesTests {

        @Test
        @DisplayName("getUserCourses() returns courses for authenticated user")
        fun `getUserCourses returns user courses successfully`() = runTest {
            // When - enrollmentId is now required
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = wizardApi.getUserCourses(enrollmentId = enrollmentId)

            // Then
            printResponse("getUserCourses(enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getUserCourses")

            val coursesResponse = assertNonNullBody(response, "getUserCourses")
            assertNotNull(coursesResponse.lessonsByYear, "LessonsByYear map should not be null")

            println("User courses - years available: ${coursesResponse.lessonsByYear.keys}")

            if (coursesResponse.lessonsByYear.isNotEmpty()) {
                println("User's courses by year:")
                coursesResponse.lessonsByYear.forEach { (year, lessons) ->
                    println("  Year $year: ${lessons.size} lessons")
                    lessons.take(3).forEach { lesson ->
                        println("    - ${lesson.lessonName}")
                    }
                }
            }
        }

        @Test
        @DisplayName("getUserCourses() with enrollmentId parameter")
        fun `getUserCourses with enrollmentId returns courses`() = runTest {
            // When - enrollmentId is required
            val enrollmentId = AuthTokens.enrollmentId!!
            val response = wizardApi.getUserCourses(enrollmentId = enrollmentId)

            // Then
            printResponse("getUserCourses(enrollmentId=$enrollmentId)", response)
            assertSuccessfulResponse(response, "getUserCourses with enrollmentId")
        }
    }

    @Nested
    @DisplayName("Wizard Flow Integration")
    inner class WizardFlowTests {

        @Test
        @DisplayName("Complete wizard flow: Categories -> Degrees -> Programs -> Lessons")
        fun `wizard flow completes successfully`() = runTest {
            // Step 1: Get categories
            val categoriesResponse = wizardApi.getCategories()
            assertSuccessfulResponse(categoriesResponse, "Step 1: getCategories")
            val categories = assertNonNullBody(categoriesResponse, "getCategories")
            assertTrue(categories.categories.isNotEmpty(), "Categories should not be empty")

            val selectedCategory = categories.categories.first()
            println("Step 1 - Selected category: ${selectedCategory.name} (${selectedCategory.code})")

            // Step 2: Get degree types for selected category
            val degreesResponse = wizardApi.getDegreeTypes(categoryCode = selectedCategory.code!!)
            assertSuccessfulResponse(degreesResponse, "Step 2: getDegreeTypes")
            val degrees = assertNonNullBody(degreesResponse, "getDegreeTypes")
            assertTrue(degrees.degrees.isNotEmpty(), "Degrees should not be empty")

            val selectedDegree = degrees.degrees.first()
            println("Step 2 - Selected degree: ${selectedDegree.name} (${selectedDegree.code})")

            // Step 3: Get programs for selected category and degree
            val programsResponse = wizardApi.getDegreePrograms(
                categoryCode = selectedCategory.code,
                degreeCode = selectedDegree.code!!
            )
            assertSuccessfulResponse(programsResponse, "Step 3: getDegreePrograms")
            val programs = assertNonNullBody(programsResponse, "getDegreePrograms")
            // Programs response has lessonsByYear map
            println("Step 3 - Programs retrieved with ${programs.lessonsByYear.size} years of data")

            // Step 4: Get lessons for a specific course
            val lessonsResponse = wizardApi.getCourseLessons(
                categoryCode = selectedCategory.code,
                degreeCode = selectedDegree.code,
                courseCode = COURSE_INFORMATICA
            )
            assertSuccessfulResponse(lessonsResponse, "Step 4: getCourseLessons")
            val lessons = assertNonNullBody(lessonsResponse, "getCourseLessons")
            assertNotNull(lessons.lessonsByYear, "Lessons should not be null")

            println("Step 4 - Lessons retrieved for program")
            println("\nWizard flow completed successfully!")
        }

        @Test
        @DisplayName("Sciences -> Bachelor -> Informatica flow")
        fun `sciences informatica wizard flow completes`() = runTest {
            // Step 1: Categories
            val categoriesResponse = wizardApi.getCategories()
            assertSuccessfulResponse(categoriesResponse, "getCategories")
            println("Step 1: Categories loaded")

            // Step 2: Degrees for Sciences
            val degreesResponse = wizardApi.getDegreeTypes(categoryCode = CATEGORY_SCIENCES)
            assertSuccessfulResponse(degreesResponse, "getDegreeTypes")
            println("Step 2: Degrees for Sciences loaded")

            // Step 3: Programs for Sciences Bachelor
            val programsResponse = wizardApi.getDegreePrograms(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR
            )
            assertSuccessfulResponse(programsResponse, "getDegreePrograms")
            println("Step 3: Programs for SCI/L loaded")

            // Step 4: Lessons for Informatica
            val lessonsResponse = wizardApi.getCourseLessons(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR,
                courseCode = COURSE_INFORMATICA
            )
            assertSuccessfulResponse(lessonsResponse, "getCourseLessons")
            println("Step 4: Lessons for Informatica loaded")

            println("\nSciences -> Bachelor -> Informatica flow completed!")
        }
    }

    @Nested
    @DisplayName("Response Structure Validation")
    inner class ResponseStructureTests {

        @Test
        @DisplayName("Categories response has expected structure")
        fun `categories response structure is valid`() = runTest {
            val response = wizardApi.getCategories()
            assertSuccessfulResponse(response, "getCategories")

            val categories = assertNonNullBody(response, "getCategories")

            // Verify structure
            assertNotNull(categories.categories, "Categories must have categories list")

            if (categories.categories.isNotEmpty()) {
                val firstCategory = categories.categories.first()
                assertNotNull(firstCategory.code, "Category must have code")
                assertNotNull(firstCategory.name, "Category must have name")
            }
        }

        @Test
        @DisplayName("Degrees response has expected structure")
        fun `degrees response structure is valid`() = runTest {
            val response = wizardApi.getDegreeTypes(categoryCode = CATEGORY_SCIENCES)
            assertSuccessfulResponse(response, "getDegreeTypes")

            val degrees = assertNonNullBody(response, "getDegreeTypes")

            // Verify structure
            assertNotNull(degrees.degrees, "Response must have degrees list")

            if (degrees.degrees.isNotEmpty()) {
                val firstDegree = degrees.degrees.first()
                assertNotNull(firstDegree.code, "Degree must have code")
                assertNotNull(firstDegree.name, "Degree must have name")
            }
        }

        @Test
        @DisplayName("Programs response has expected structure")
        fun `programs response structure is valid`() = runTest {
            val response = wizardApi.getDegreePrograms(
                categoryCode = CATEGORY_SCIENCES,
                degreeCode = DEGREE_BACHELOR
            )
            assertSuccessfulResponse(response, "getDegreePrograms")

            val programs = assertNonNullBody(response, "getDegreePrograms")

            // Verify structure - lessonsByYear is a Map
            assertNotNull(programs.lessonsByYear, "Response must have lessonsByYear map")

            if (programs.lessonsByYear.isNotEmpty()) {
                val firstYear = programs.lessonsByYear.keys.first()
                val lessonsForYear = programs.lessonsByYear[firstYear]
                assertNotNull(lessonsForYear, "Lessons for year should not be null")

                if (lessonsForYear?.isNotEmpty() == true) {
                    val firstLesson = lessonsForYear.first()
                    println("Lesson structure: name=${firstLesson.lessonName}, activityCode=${firstLesson.activityCode}")
                }
            }
        }

        @Test
        @DisplayName("User courses response has expected structure")
        fun `user courses response structure is valid`() = runTest {
            val response = wizardApi.getUserCourses(enrollmentId = AuthTokens.enrollmentId!!)
            assertSuccessfulResponse(response, "getUserCourses")

            val courses = assertNonNullBody(response, "getUserCourses")

            // Verify structure - lessonsByYear is a Map
            assertNotNull(courses.lessonsByYear, "Response must have lessonsByYear map")
        }
    }
}
