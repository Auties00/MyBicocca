package it.attendance100.mybicocca.data.local.library

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryCrowdingSlot
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Persistence coverage for [LibraryCrowdingHistoryStore]: JSON round-trip per library/weekday,
 * closed days, corrupt JSON tolerance, 28-day expiry ignore & prune, and initial-collection flags.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LibraryCrowdingHistoryStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var dataStoreFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: LibraryCrowdingHistoryStore

    private val rome = ZoneId.of("Europe/Rome")

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), rome)
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStoreFile = context.preferencesDataStoreFile("test_library_crowding_${System.nanoTime()}")
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { dataStoreFile },
        )
        store = LibraryCrowdingHistoryStore(dataStore)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.systemDefaultZone()
        dataStoreFile.delete()
    }

    @Test
    fun `upsert round-trips open and closed weekday entries and overwrites same weekday`() = testScope.runTest {
        val thursdayV1 = LibraryWeekdayCrowding(
            weekday = DayOfWeek.THURSDAY,
            date = LocalDate.of(2026, 6, 18),
            slots = listOf(LibraryCrowdingSlot(LocalTime.of(8, 30), LocalTime.of(9, 30), 25)),
            isClosed = false,
        )
        val sundayClosed = LibraryWeekdayCrowding(
            weekday = DayOfWeek.SUNDAY,
            date = LocalDate.of(2026, 6, 21),
            slots = emptyList(),
            isClosed = true,
        )
        store.upsert(LibraryCatalog.CENTRALE_ID, thursdayV1)
        store.upsert(LibraryCatalog.CENTRALE_ID, sundayClosed)

        val initial = store.getHistory(LibraryCatalog.CENTRALE_ID)
        assertThat(initial).hasSize(2)
        assertThat(initial[DayOfWeek.THURSDAY]).isEqualTo(thursdayV1)
        assertThat(initial[DayOfWeek.SUNDAY]).isEqualTo(sundayClosed)

        val thursdayV2 = thursdayV1.copy(
            date = LocalDate.of(2026, 6, 25),
            slots = listOf(LibraryCrowdingSlot(LocalTime.of(8, 30), LocalTime.of(10, 0), 55)),
        )
        store.upsert(LibraryCatalog.CENTRALE_ID, thursdayV2)

        val updated = store.getHistory(LibraryCatalog.CENTRALE_ID)
        assertThat(updated).hasSize(2)
        assertThat(updated[DayOfWeek.THURSDAY]).isEqualTo(thursdayV2)
    }

    @Test
    fun `a later partial fetch for the same date keeps the earlier part of the day`() = testScope.runTest {
        fun slot(from: String, to: String, occupancy: Int) =
            LibraryCrowdingSlot(LocalTime.parse(from), LocalTime.parse(to), occupancy)
        val morning = LibraryWeekdayCrowding(
            weekday = DayOfWeek.THURSDAY,
            date = LocalDate.of(2026, 6, 25),
            slots = listOf(
                slot("08:30", "09:30", 10),
                slot("09:30", "12:30", 40),
                slot("12:30", "18:30", 60),
            ),
            isClosed = false,
        )
        val evening = morning.copy(slots = listOf(slot("11:00", "18:30", 75)))

        store.upsert(LibraryCatalog.CENTRALE_ID, morning)
        store.upsert(LibraryCatalog.CENTRALE_ID, evening)

        assertThat(store.getHistory(LibraryCatalog.CENTRALE_ID)[DayOfWeek.THURSDAY]?.slots)
            .containsExactly(
                slot("08:30", "09:30", 10),
                slot("09:30", "11:00", 40),
                slot("11:00", "18:30", 75),
            ).inOrder()
    }

    @Test
    fun `corrupt JSON payload reads as empty and recovers on next upsert`() = testScope.runTest {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("${LibraryCrowdingHistoryStore.HISTORY_KEY_PREFIX}${LibraryCatalog.CENTRALE_ID}")] =
                "{corrupt-json"
        }

        assertThat(store.getHistory(LibraryCatalog.CENTRALE_ID)).isEmpty()

        val valid = LibraryWeekdayCrowding(
            weekday = DayOfWeek.MONDAY,
            date = LocalDate.of(2026, 6, 29),
            slots = listOf(LibraryCrowdingSlot(LocalTime.of(9, 0), LocalTime.of(10, 0), 30)),
            isClosed = false,
        )
        store.upsert(LibraryCatalog.CENTRALE_ID, valid)
        assertThat(store.getHistory(LibraryCatalog.CENTRALE_ID)[DayOfWeek.MONDAY]).isEqualTo(valid)
    }

    @Test
    fun `entries older than 28 days are ignored on read and pruned on write`() = testScope.runTest {
        // Store two entries while clock is on June 2.
        AppClock.clock = Clock.fixed(Instant.parse("2026-06-02T10:00:00Z"), rome)
        val june1 = LibraryWeekdayCrowding(
            weekday = DayOfWeek.MONDAY,
            date = LocalDate.of(2026, 6, 1),
            slots = listOf(LibraryCrowdingSlot(LocalTime.of(8, 30), LocalTime.of(9, 30), 20)),
            isClosed = false,
        )
        val june2 = LibraryWeekdayCrowding(
            weekday = DayOfWeek.TUESDAY,
            date = LocalDate.of(2026, 6, 2),
            slots = listOf(LibraryCrowdingSlot(LocalTime.of(8, 30), LocalTime.of(9, 30), 30)),
            isClosed = false,
        )
        store.upsert(LibraryCatalog.CENTRALE_ID, june1)
        store.upsert(LibraryCatalog.CENTRALE_ID, june2)

        // Advance clock to June 30: June 1 is 29 days old (>28 -> ignored); June 2 is 28 days old (kept).
        AppClock.clock = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), rome)
        val read = store.getHistory(LibraryCatalog.CENTRALE_ID)
        assertThat(read.keys).containsExactly(DayOfWeek.TUESDAY)

        // Prune removes June 1 from disk permanently even if clock moved back.
        store.pruneExpired()
        val rawJson = dataStore.data.first()[
            stringPreferencesKey("${LibraryCrowdingHistoryStore.HISTORY_KEY_PREFIX}${LibraryCatalog.CENTRALE_ID}"),
        ]
        assertThat(rawJson).doesNotContain("2026-06-01")
        assertThat(rawJson).contains("2026-06-02")
    }

    @Test
    fun `firstCollectionRunAt records once and initialCollectionDone persists`() = testScope.runTest {
        assertThat(store.firstCollectionRunAt()).isNull()
        assertThat(store.isInitialCollectionDone()).isFalse()

        val first = Instant.parse("2026-06-01T08:00:00Z")
        val second = Instant.parse("2026-06-05T08:00:00Z")
        store.recordFirstCollectionRunAt(first)
        store.recordFirstCollectionRunAt(second)

        assertThat(store.firstCollectionRunAt()).isEqualTo(first)

        store.markInitialCollectionDone()
        assertThat(store.isInitialCollectionDone()).isTrue()
    }
}
