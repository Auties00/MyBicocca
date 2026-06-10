package it.attendance100.mybicocca.data.local.document

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [DocumentCacheDao] against a real in-memory Room database (Robolectric).
 * Exercises the three offline document mirrors:
 * - the single-row student badge: whole-row replace plus the deliberate null-clear branch
 *   (`replaceBadge(careerId, null)` wipes the career's row so a "no card" live result is mirrored
 *   faithfully) and per-career scoping;
 * - the academic-titles list with its child attributes: replaced together in one transaction,
 *   titles read back ordered by `cache_order` and attributes by `title_id, attr_order`, each
 *   replace wiping only the targeted career;
 * - the side-keyed badge images: keyed by `(blob_id, side)` rather than by career, so the same
 *   blob is shared across careers and upsert overwrites a side in place.
 *
 * The badge and titles tables key on a plain `career_id` Long and the image table on
 * `(blob_id, side)`, none with foreign keys, so no parent account/career rows are required.
 *
 * Wave 2 (Android-runtime) test: Robolectric drives the actual Room-generated `_Impl`, pinned to
 * a Robolectric-supported SDK because the module compiles against a newer one.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DocumentCacheDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: DocumentCacheDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.documentCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `replaceBadge stores then overwrites the single career row`() = runTest {
        dao.replaceBadge(1L, badge(1L, badgeId = 100L, studentNumber = "111111"))

        dao.replaceBadge(1L, badge(1L, badgeId = 200L, studentNumber = "222222"))

        val stored = dao.getBadge(1L)
        assertThat(stored).isNotNull()
        assertThat(stored!!.badgeId).isEqualTo(200L)
        assertThat(stored.studentNumber).isEqualTo("222222")
    }

    @Test
    fun `replaceBadge with null clears the career's badge row`() = runTest {
        dao.replaceBadge(1L, badge(1L, badgeId = 100L, studentNumber = "111111"))

        dao.replaceBadge(1L, null)

        assertThat(dao.getBadge(1L)).isNull()
    }

    @Test
    fun `replaceBadge with null on an already-empty career stays null`() = runTest {
        dao.replaceBadge(1L, null)

        assertThat(dao.getBadge(1L)).isNull()
    }

    @Test
    fun `replaceBadge for one career leaves another career's badge intact`() = runTest {
        dao.replaceBadge(2L, badge(2L, badgeId = 500L, studentNumber = "keep"))

        dao.replaceBadge(1L, badge(1L, badgeId = 100L, studentNumber = "other"))
        dao.replaceBadge(1L, null)

        val kept = dao.getBadge(2L)
        assertThat(kept).isNotNull()
        assertThat(kept!!.studentNumber).isEqualTo("keep")
    }

    @Test
    fun `getBadge round-trips every nullable scalar field`() = runTest {
        dao.replaceBadge(
            careerId = 1L,
            row = StudentBadgeEntity(
                careerId = 1L,
                badgeId = 100L,
                blobId = null,
                hasFrontImage = true,
                hasRearImage = false,
                rfid = null,
                studentNumber = "123456",
                fullName = "Mario Rossi",
                courseDescription = "Informatica",
                facultyDescription = null,
                academicYear = 2024,
                delivered = true,
                cancelled = false,
                returned = false,
                createdOn = "2024-09-01",
                printedOn = null,
                deliveredOn = "2024-09-10",
            ),
        )

        val stored = dao.getBadge(1L)!!
        assertThat(stored.blobId).isNull()
        assertThat(stored.hasFrontImage).isTrue()
        assertThat(stored.hasRearImage).isFalse()
        assertThat(stored.rfid).isNull()
        assertThat(stored.fullName).isEqualTo("Mario Rossi")
        assertThat(stored.facultyDescription).isNull()
        assertThat(stored.academicYear).isEqualTo(2024)
        assertThat(stored.delivered).isTrue()
        assertThat(stored.printedOn).isNull()
        assertThat(stored.deliveredOn).isEqualTo("2024-09-10")
    }

    @Test
    fun `replaceTitles stores titles ordered by cache_order and attributes ordered by title and attr_order`() = runTest {
        dao.replaceTitles(
            careerId = 1L,
            titleRows = listOf(
                title(1L, titleId = "DIP-2", cacheOrder = 1, subject = "Diploma"),
                title(1L, titleId = "LAU-1", cacheOrder = 0, subject = "Laurea"),
            ),
            attrRows = listOf(
                attribute(1L, titleId = "LAU-1", attrOrder = 1, value = "second"),
                attribute(1L, titleId = "DIP-2", attrOrder = 0, value = "dip-first"),
                attribute(1L, titleId = "LAU-1", attrOrder = 0, value = "first"),
            ),
        )

        assertThat(dao.getTitles(1L).map { it.subject })
            .containsExactly("Laurea", "Diploma").inOrder()
        assertThat(dao.getTitleAttributes(1L).map { it.value })
            .containsExactly("dip-first", "first", "second").inOrder()
    }

    @Test
    fun `getTitles and getTitleAttributes are empty for a career with nothing cached`() = runTest {
        dao.replaceTitles(
            careerId = 1L,
            titleRows = listOf(title(1L, titleId = "LAU-1", cacheOrder = 0, subject = "Laurea")),
            attrRows = listOf(attribute(1L, titleId = "LAU-1", attrOrder = 0, value = "x")),
        )

        assertThat(dao.getTitles(2L)).isEmpty()
        assertThat(dao.getTitleAttributes(2L)).isEmpty()
    }

    @Test
    fun `replaceTitles swaps the prior titles and attributes for the career`() = runTest {
        dao.replaceTitles(
            careerId = 1L,
            titleRows = listOf(title(1L, titleId = "OLD", cacheOrder = 0, subject = "old")),
            attrRows = listOf(attribute(1L, titleId = "OLD", attrOrder = 0, value = "old-attr")),
        )

        dao.replaceTitles(
            careerId = 1L,
            titleRows = listOf(title(1L, titleId = "NEW", cacheOrder = 0, subject = "new")),
            attrRows = listOf(attribute(1L, titleId = "NEW", attrOrder = 0, value = "new-attr")),
        )

        assertThat(dao.getTitles(1L).map { it.subject }).containsExactly("new")
        assertThat(dao.getTitleAttributes(1L).map { it.value }).containsExactly("new-attr")
    }

    @Test
    fun `replaceTitles for one career leaves another career's titles and attributes intact`() = runTest {
        dao.replaceTitles(
            careerId = 2L,
            titleRows = listOf(title(2L, titleId = "KEEP", cacheOrder = 0, subject = "keep")),
            attrRows = listOf(attribute(2L, titleId = "KEEP", attrOrder = 0, value = "keep-attr")),
        )

        dao.replaceTitles(
            careerId = 1L,
            titleRows = listOf(title(1L, titleId = "OTHER", cacheOrder = 0, subject = "other")),
            attrRows = listOf(attribute(1L, titleId = "OTHER", attrOrder = 0, value = "other-attr")),
        )

        assertThat(dao.getTitles(2L).map { it.subject }).containsExactly("keep")
        assertThat(dao.getTitleAttributes(2L).map { it.value }).containsExactly("keep-attr")
    }

    @Test
    fun `replaceTitles with empty lists clears the career's titles and attributes`() = runTest {
        dao.replaceTitles(
            careerId = 1L,
            titleRows = listOf(title(1L, titleId = "LAU-1", cacheOrder = 0, subject = "Laurea")),
            attrRows = listOf(attribute(1L, titleId = "LAU-1", attrOrder = 0, value = "x")),
        )

        dao.replaceTitles(1L, emptyList(), emptyList())

        assertThat(dao.getTitles(1L)).isEmpty()
        assertThat(dao.getTitleAttributes(1L)).isEmpty()
    }

    @Test
    fun `upsertImage then getImage round-trips the bytes for the requested side`() = runTest {
        dao.upsertImage(image(blobId = 42L, side = "front", bytes = byteArrayOf(1, 2, 3)))

        val stored = dao.getImage(42L, "front")
        assertThat(stored).isNotNull()
        assertThat(stored!!.bytes).isEqualTo(byteArrayOf(1, 2, 3))
    }

    @Test
    fun `getImage distinguishes the two sides of the same blob`() = runTest {
        dao.upsertImage(image(blobId = 42L, side = "front", bytes = byteArrayOf(1)))
        dao.upsertImage(image(blobId = 42L, side = "rear", bytes = byteArrayOf(2)))

        assertThat(dao.getImage(42L, "front")!!.bytes).isEqualTo(byteArrayOf(1))
        assertThat(dao.getImage(42L, "rear")!!.bytes).isEqualTo(byteArrayOf(2))
    }

    @Test
    fun `upsertImage overwrites a side in place`() = runTest {
        dao.upsertImage(image(blobId = 42L, side = "front", bytes = byteArrayOf(1, 1)))

        dao.upsertImage(image(blobId = 42L, side = "front", bytes = byteArrayOf(9, 9, 9)))

        assertThat(dao.getImage(42L, "front")!!.bytes).isEqualTo(byteArrayOf(9, 9, 9))
    }

    @Test
    fun `getImage is null for an uncached blob or side`() = runTest {
        dao.upsertImage(image(blobId = 42L, side = "front", bytes = byteArrayOf(1)))

        assertThat(dao.getImage(99L, "front")).isNull()
        assertThat(dao.getImage(42L, "rear")).isNull()
    }

    private fun badge(careerId: Long, badgeId: Long, studentNumber: String) = StudentBadgeEntity(
        careerId = careerId,
        badgeId = badgeId,
        blobId = 7L,
        hasFrontImage = true,
        hasRearImage = true,
        rfid = "rf-id",
        studentNumber = studentNumber,
        fullName = "Mario Rossi",
        courseDescription = "Informatica",
        facultyDescription = "Scienze",
        academicYear = 2024,
        delivered = true,
        cancelled = false,
        returned = false,
        createdOn = "2024-09-01",
        printedOn = "2024-09-05",
        deliveredOn = "2024-09-10",
    )

    private fun title(careerId: Long, titleId: String, cacheOrder: Int, subject: String) = AcademicTitleEntity(
        careerId = careerId,
        titleId = titleId,
        cacheOrder = cacheOrder,
        category = "University",
        status = "Awarded",
        typeDescription = "Laurea triennale",
        subject = subject,
        institution = "Bicocca",
        country = "Italia",
        year = "2020",
        grade = "110L",
        cumLaude = true,
        valueDeclarationFiled = false,
    )

    private fun attribute(careerId: Long, titleId: String, attrOrder: Int, value: String) = TitleAttributeEntity(
        careerId = careerId,
        titleId = titleId,
        attrOrder = attrOrder,
        field = "Institution",
        value = value,
    )

    private fun image(blobId: Long, side: String, bytes: ByteArray) = BadgeImageEntity(
        blobId = blobId,
        side = side,
        bytes = bytes,
    )
}
