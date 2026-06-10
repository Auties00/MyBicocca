package it.attendance100.mybicocca.data.mapper.elearning

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.grade.CourseGradeOverviewEntity
import it.attendance100.mybicocca.data.local.elearning.grade.GradeItemEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseGrade
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGradeItem
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItemType
import org.junit.Test
import java.time.Instant

/**
 * Covers the Moodle grade-item and course-overview mappers: name fallback to the activity
 * module, percentage parsing of Moodle's "NN %" strings (with comma decimals), the
 * itemtype default sentinel, epoch-second normalization, and the raw-grade string parse of
 * the overview endpoint.
 */
class ElearningGradeMapperTest {

    private val account = AccountId("acc-1")

    private fun gradeItem(
        id: Int = 100,
        itemName: String? = "Midterm",
        itemType: String? = "mod",
        itemModule: String? = "assign",
        gradeRaw: Double? = 27.0,
        gradeMax: Double? = 30.0,
        percentageFormatted: String? = "90,00 %",
        gradeFormatted: String? = "27,00",
        feedback: String? = "well done",
        gradeDateGraded: Long? = 1_500L,
    ) = ElearningGradeItem(
        id = id,
        itemName = itemName,
        itemType = itemType,
        itemModule = itemModule,
        gradeRaw = gradeRaw,
        gradeMax = gradeMax,
        percentageFormatted = percentageFormatted,
        gradeFormatted = gradeFormatted,
        feedback = feedback,
        gradeDateGraded = gradeDateGraded,
    )

    @Test
    fun `toEntity keeps a non-blank item name`() {
        assertThat(gradeItem(itemName = "Midterm").toEntity(account, 5, 0).name).isEqualTo("Midterm")
    }

    @Test
    fun `toEntity falls back to the activity module when name is blank`() {
        assertThat(gradeItem(itemName = "  ", itemModule = "quiz").toEntity(account, 5, 0).name)
            .isEqualTo("quiz")
    }

    @Test
    fun `toEntity falls back to the activity module when name is null`() {
        assertThat(gradeItem(itemName = null, itemModule = "assign").toEntity(account, 5, 0).name)
            .isEqualTo("assign")
    }

    @Test
    fun `toEntity uses empty name when both name and module are absent`() {
        assertThat(gradeItem(itemName = null, itemModule = null).toEntity(account, 5, 0).name)
            .isEqualTo("")
    }

    @Test
    fun `toEntity defaults a null itemtype to other`() {
        assertThat(gradeItem(itemType = null).toEntity(account, 5, 0).typeRaw).isEqualTo("other")
    }

    @Test
    fun `toEntity parses the percentage with a comma decimal separator`() {
        assertThat(gradeItem(percentageFormatted = "87,50 %").toEntity(account, 5, 0).percentage)
            .isEqualTo(87.5)
    }

    @Test
    fun `toEntity parses a percentage with a dot decimal separator`() {
        assertThat(gradeItem(percentageFormatted = "100.00 %").toEntity(account, 5, 0).percentage)
            .isEqualTo(100.0)
    }

    @Test
    fun `toEntity yields null percentage for an unparseable string`() {
        assertThat(gradeItem(percentageFormatted = "n/a").toEntity(account, 5, 0).percentage).isNull()
    }

    @Test
    fun `toEntity yields null percentage when the field is absent`() {
        assertThat(gradeItem(percentageFormatted = null).toEntity(account, 5, 0).percentage).isNull()
    }

    @Test
    fun `toEntity normalizes the graded timestamp from seconds`() {
        assertThat(gradeItem(gradeDateGraded = 1_500L).toEntity(account, 5, 0).gradedAtMs)
            .isEqualTo(1_500_000L)
    }

    @Test
    fun `toEntity reads a zero graded timestamp as absent`() {
        assertThat(gradeItem(gradeDateGraded = 0L).toEntity(account, 5, 0).gradedAtMs).isNull()
    }

    @Test
    fun `toEntity records the sort order, ids, grades and activity type`() {
        val entity = gradeItem(id = 100, itemModule = "assign").toEntity(account, courseId = 5, sortOrder = 3)
        assertThat(entity.accountId).isEqualTo("acc-1")
        assertThat(entity.courseId).isEqualTo(5)
        assertThat(entity.itemId).isEqualTo(100L)
        assertThat(entity.sortOrder).isEqualTo(3)
        assertThat(entity.activityType).isEqualTo("assign")
        assertThat(entity.grade).isEqualTo(27.0)
        assertThat(entity.maxGrade).isEqualTo(30.0)
    }

    @Test
    fun `grade item toDomain resolves the item type from the raw value`() {
        val entity = GradeItemEntity(
            accountId = "acc-1",
            courseId = 5,
            itemId = 100L,
            name = "Total",
            typeRaw = "course",
            activityType = null,
            grade = 28.0,
            maxGrade = 30.0,
            percentage = 93.3,
            gradeFormatted = "28,00",
            feedback = null,
            gradedAtMs = 1_500_000L,
            sortOrder = 0,
        )
        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(100L)
        assertThat(domain.type).isEqualTo(GradeItemType.Course)
        assertThat(domain.gradedAt).isEqualTo(Instant.ofEpochMilli(1_500_000L))
    }

    @Test
    fun `grade item toDomain collapses an unknown raw type to other`() {
        val entity = GradeItemEntity(
            accountId = "acc-1",
            courseId = 5,
            itemId = 1L,
            name = "X",
            typeRaw = "mystery",
            activityType = null,
            grade = null,
            maxGrade = null,
            percentage = null,
            gradeFormatted = null,
            feedback = null,
            gradedAtMs = null,
            sortOrder = 0,
        )
        assertThat(entity.toDomain().type).isEqualTo(GradeItemType.Other)
        assertThat(entity.toDomain().gradedAt).isNull()
    }

    @Test
    fun `course grade toEntity parses the raw value and stores caller name`() {
        val dto = ElearningCourseGrade(courseId = 5, grade = "27,5", rawGrade = "27.5", rank = 3)
        val entity = dto.toEntity(account, courseName = "Analisi")
        assertThat(entity.courseId).isEqualTo(5)
        assertThat(entity.courseName).isEqualTo("Analisi")
        assertThat(entity.grade).isEqualTo(27.5)
        assertThat(entity.maxGrade).isNull()
        assertThat(entity.gradeFormatted).isEqualTo("27,5")
    }

    @Test
    fun `course grade toEntity stores an empty name when none supplied`() {
        val dto = ElearningCourseGrade(courseId = 5, grade = "-", rawGrade = null)
        val entity = dto.toEntity(account, courseName = null)
        assertThat(entity.courseName).isEqualTo("")
        assertThat(entity.grade).isNull()
    }

    @Test
    fun `course grade toEntity yields null grade for an unparseable raw value`() {
        val dto = ElearningCourseGrade(courseId = 5, grade = "-", rawGrade = "n/a")
        assertThat(dto.toEntity(account, courseName = "X").grade).isNull()
    }

    @Test
    fun `course grade overview toDomain wraps the course id`() {
        val entity = CourseGradeOverviewEntity(
            accountId = "acc-1",
            courseId = 5,
            courseName = "Analisi",
            grade = 27.5,
            maxGrade = null,
            gradeFormatted = "27,5",
        )
        val domain = entity.toDomain()
        assertThat(domain.courseId).isEqualTo(CourseId(5))
        assertThat(domain.courseName).isEqualTo("Analisi")
        assertThat(domain.grade).isEqualTo(27.5)
        assertThat(domain.gradeFormatted).isEqualTo("27,5")
    }
}
