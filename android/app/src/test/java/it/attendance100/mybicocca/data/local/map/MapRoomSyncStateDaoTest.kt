package it.attendance100.mybicocca.data.local.map

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
 * Behaviour coverage for [MapRoomSyncStateDao] against a real in-memory Room database (Robolectric).
 * Exercises the per-building freshness round-trip and the upsert overwrite that records a newer
 * sync timestamp, which drives the TTL check that skips re-fetching a building's rooms while fresh.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MapRoomSyncStateDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: MapRoomSyncStateDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.mapRoomSyncStateDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getState returns null before any sync is recorded`() = runTest {
        assertThat(dao.getState("U01")).isNull()
    }

    @Test
    fun `upsertState then getState round-trips the timestamp`() = runTest {
        dao.upsertState(MapRoomSyncStateEntity("U01", lastRefreshedAtMs = 1_000L))

        val state = dao.getState("U01")

        assertThat(state).isNotNull()
        assertThat(state!!.lastRefreshedAtMs).isEqualTo(1_000L)
    }

    @Test
    fun `upsertState overwrites the timestamp for the same building`() = runTest {
        dao.upsertState(MapRoomSyncStateEntity("U01", lastRefreshedAtMs = 1_000L))

        dao.upsertState(MapRoomSyncStateEntity("U01", lastRefreshedAtMs = 9_000L))

        assertThat(dao.getState("U01")!!.lastRefreshedAtMs).isEqualTo(9_000L)
    }

    @Test
    fun `getState is scoped to the requested building`() = runTest {
        dao.upsertState(MapRoomSyncStateEntity("U01", lastRefreshedAtMs = 1_000L))
        dao.upsertState(MapRoomSyncStateEntity("U02", lastRefreshedAtMs = 2_000L))

        assertThat(dao.getState("U01")!!.lastRefreshedAtMs).isEqualTo(1_000L)
        assertThat(dao.getState("U02")!!.lastRefreshedAtMs).isEqualTo(2_000L)
        assertThat(dao.getState("U03")).isNull()
    }
}
