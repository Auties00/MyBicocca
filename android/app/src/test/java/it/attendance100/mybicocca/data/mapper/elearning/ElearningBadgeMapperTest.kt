package it.attendance100.mybicocca.data.mapper.elearning

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.badge.BadgeEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUserBadge
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import org.junit.Test
import java.time.Instant

/**
 * Covers the Moodle badge DTO -> entity -> domain mapping: issue-date preference over
 * creation date, epoch-second normalization with the 0 sentinel, the course-id site-wide
 * collapse, and the entity round-trip into the domain model.
 */
class ElearningBadgeMapperTest {

    private val account = AccountId("acc-1")

    private fun badge(
        id: Int = 7,
        name: String = "Best Student",
        description: String? = "Awarded for excellence",
        badgeUrl: String? = "https://moodle/badge.png",
        timeCreated: Long? = 1_000L,
        dateIssued: Long? = 2_000L,
        courseId: Int? = 55,
    ) = ElearningUserBadge(
        id = id,
        name = name,
        description = description,
        timeCreated = timeCreated,
        dateIssued = dateIssued,
        courseId = courseId,
        badgeUrl = badgeUrl,
    )

    @Test
    fun `toEntity prefers the issue date over the creation date`() {
        val entity = badge(timeCreated = 1_000L, dateIssued = 2_000L).toEntity(account)
        assertThat(entity.issuedAtMs).isEqualTo(2_000_000L)
    }

    @Test
    fun `toEntity falls back to the creation date when issue date is absent`() {
        val entity = badge(timeCreated = 1_000L, dateIssued = null).toEntity(account)
        assertThat(entity.issuedAtMs).isEqualTo(1_000_000L)
    }

    @Test
    fun `toEntity reads a zero timestamp as absent`() {
        val entity = badge(timeCreated = 0L, dateIssued = 0L).toEntity(account)
        assertThat(entity.issuedAtMs).isNull()
    }

    @Test
    fun `toEntity reads both timestamps null as absent`() {
        val entity = badge(timeCreated = null, dateIssued = null).toEntity(account)
        assertThat(entity.issuedAtMs).isNull()
    }

    @Test
    fun `toEntity treats a non-positive course id as site-wide null`() {
        assertThat(badge(courseId = 0).toEntity(account).courseId).isNull()
        assertThat(badge(courseId = -3).toEntity(account).courseId).isNull()
    }

    @Test
    fun `toEntity keeps a positive course id`() {
        assertThat(badge(courseId = 55).toEntity(account).courseId).isEqualTo(55)
    }

    @Test
    fun `toEntity maps the identifying and presentation fields`() {
        val entity = badge().toEntity(account)
        assertThat(entity.accountId).isEqualTo("acc-1")
        assertThat(entity.badgeId).isEqualTo(7)
        assertThat(entity.name).isEqualTo("Best Student")
        assertThat(entity.description).isEqualTo("Awarded for excellence")
        assertThat(entity.imageUrl).isEqualTo("https://moodle/badge.png")
    }

    @Test
    fun `toDomain maps a site-wide badge with no issue date`() {
        val entity = BadgeEntity(
            accountId = "acc-1",
            badgeId = 9,
            name = "Welcome",
            description = null,
            imageUrl = null,
            issuedAtMs = null,
            courseId = null,
        )
        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(9)
        assertThat(domain.name).isEqualTo("Welcome")
        assertThat(domain.description).isNull()
        assertThat(domain.imageUrl).isNull()
        assertThat(domain.issuedAt).isNull()
        assertThat(domain.courseId).isNull()
    }

    @Test
    fun `toDomain wraps a present course id and issue instant`() {
        val entity = BadgeEntity(
            accountId = "acc-1",
            badgeId = 9,
            name = "Course Star",
            description = "desc",
            imageUrl = "url",
            issuedAtMs = 2_000_000L,
            courseId = 55,
        )
        val domain = entity.toDomain()
        assertThat(domain.issuedAt).isEqualTo(Instant.ofEpochMilli(2_000_000L))
        assertThat(domain.courseId).isEqualTo(CourseId(55))
    }
}
