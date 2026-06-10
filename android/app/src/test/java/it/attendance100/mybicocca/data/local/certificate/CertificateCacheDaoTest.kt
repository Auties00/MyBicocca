package it.attendance100.mybicocca.data.local.certificate

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
 * Behaviour coverage for [CertificateCacheDao] against a real in-memory Room database
 * (Robolectric). Exercises the wholesale per-owner replace (delete-then-insert in one
 * transaction), the `cache_order` read ordering that preserves the server list position, and
 * the person scoping that keeps one student's offline certificates from disturbing another's.
 * The table keys on a plain `owner_id` Long (Esse3 personId) with no foreign keys, so no parent
 * account/career rows are required.
 *
 * Wave 2 (Android-runtime) test: Robolectric drives the actual Room-generated `_Impl`, pinned to
 * a Robolectric-supported SDK because the module compiles against a newer one.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CertificateCacheDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: CertificateCacheDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.certificateCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `replaceCertificates stores the rows read back ordered by cache_order`() = runTest {
        dao.replaceCertificates(
            ownerId = 7L,
            rows = listOf(
                certificate(7L, certificateId = "c-late", cacheOrder = 2),
                certificate(7L, certificateId = "c-early", cacheOrder = 0),
                certificate(7L, certificateId = "c-mid", cacheOrder = 1),
            ),
        )

        assertThat(dao.getCertificates(7L).map { it.certificateId })
            .containsExactly("c-early", "c-mid", "c-late").inOrder()
    }

    @Test
    fun `getCertificates is empty for an owner with no cached rows`() = runTest {
        dao.replaceCertificates(7L, listOf(certificate(7L, certificateId = "c-1", cacheOrder = 0)))

        assertThat(dao.getCertificates(99L)).isEmpty()
    }

    @Test
    fun `replaceCertificates swaps the prior owner slice wholesale`() = runTest {
        dao.replaceCertificates(
            ownerId = 7L,
            rows = listOf(
                certificate(7L, certificateId = "old-1", cacheOrder = 0),
                certificate(7L, certificateId = "old-2", cacheOrder = 1),
            ),
        )

        dao.replaceCertificates(7L, listOf(certificate(7L, certificateId = "fresh", cacheOrder = 0)))

        assertThat(dao.getCertificates(7L).map { it.certificateId }).containsExactly("fresh")
    }

    @Test
    fun `replaceCertificates for one owner leaves another owner's rows intact`() = runTest {
        dao.replaceCertificates(8L, listOf(certificate(8L, certificateId = "keep", cacheOrder = 0)))

        dao.replaceCertificates(7L, listOf(certificate(7L, certificateId = "other", cacheOrder = 0)))

        assertThat(dao.getCertificates(8L).map { it.certificateId }).containsExactly("keep")
        assertThat(dao.getCertificates(7L).map { it.certificateId }).containsExactly("other")
    }

    @Test
    fun `replaceCertificates with an empty list clears the owner slice`() = runTest {
        dao.replaceCertificates(7L, listOf(certificate(7L, certificateId = "c-1", cacheOrder = 0)))

        dao.replaceCertificates(7L, emptyList())

        assertThat(dao.getCertificates(7L)).isEmpty()
    }

    @Test
    fun `getCertificates round-trips every scalar field`() = runTest {
        dao.replaceCertificates(
            ownerId = 7L,
            rows = listOf(
                CertificateEntity(
                    ownerId = 7L,
                    certificateId = "iscrizione",
                    cacheOrder = 0,
                    description = "Certificato di iscrizione",
                    type = "Enrollment",
                    solarYear = 2024,
                    digitallySigned = true,
                ),
            ),
        )

        val stored = dao.getCertificates(7L).single()
        assertThat(stored.description).isEqualTo("Certificato di iscrizione")
        assertThat(stored.type).isEqualTo("Enrollment")
        assertThat(stored.solarYear).isEqualTo(2024)
        assertThat(stored.digitallySigned).isTrue()
    }

    private fun certificate(ownerId: Long, certificateId: String, cacheOrder: Int) = CertificateEntity(
        ownerId = ownerId,
        certificateId = certificateId,
        cacheOrder = cacheOrder,
        description = "Certificato",
        type = "Generic",
        solarYear = null,
        digitallySigned = false,
    )
}
