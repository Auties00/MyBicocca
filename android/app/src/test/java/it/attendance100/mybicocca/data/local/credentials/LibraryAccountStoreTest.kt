package it.attendance100.mybicocca.data.local.credentials

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import dagger.Lazy
import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryPendingValidation
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Persistence round-trip and corrupt-value resilience for [LibraryAccountStore]'s JSON-backed
 * pending validation and booking draft storage.
 */
class LibraryAccountStoreTest {

    private val fakePrefs = InMemorySharedPreferences()
    private val store = LibraryAccountStore(Lazy { fakePrefs })

    @Test
    fun `pendingValidation round-trips cleanly and clears`() = runTest {
        val pending = LibraryPendingValidation(
            email = "student@campus.unimib.it",
            requestUuid = "req-123",
            createdAt = Instant.parse("2026-06-15T10:15:30Z"),
        )

        store.setPendingValidation(pending)
        assertThat(store.pendingValidation()).isEqualTo(pending)

        store.clearPendingValidation()
        assertThat(store.pendingValidation()).isNull()
    }

    @Test
    fun `bookingDraft round-trips cleanly and clears`() = runTest {
        val draft = LibraryBookingDraft(
            libraryId = LibraryCatalog.CENTRALE_ID,
            zoneId = 2,
            date = LocalDate.of(2026, 6, 16),
            durationMinutes = 120,
            startTime = LocalTime.of(14, 30),
            seatResourceId = 205,
            note = "Studente Esempio",
            consentAccepted = true,
            savedAt = Instant.parse("2026-06-15T10:20:00Z"),
        )

        store.setBookingDraft(draft)
        assertThat(store.bookingDraft()).isEqualTo(draft)

        store.clearBookingDraft()
        assertThat(store.bookingDraft()).isNull()
    }

    @Test
    fun `corrupt pendingValidation JSON reads as null`() = runTest {
        fakePrefs.putRaw("affluences:pending_validation", "{not-valid-json")

        assertThat(store.pendingValidation()).isNull()
    }

    @Test
    fun `pendingValidation with unreadable timestamp reads as null`() = runTest {
        fakePrefs.putRaw(
            "affluences:pending_validation",
            """{"email":"student@campus.unimib.it","requestUuid":"req-1","createdAt":"not-an-instant"}""",
        )

        assertThat(store.pendingValidation()).isNull()
    }

    @Test
    fun `corrupt bookingDraft JSON reads as null`() = runTest {
        fakePrefs.putRaw("affluences:booking_draft", "corrupt")

        assertThat(store.bookingDraft()).isNull()
    }

    @Test
    fun `bookingDraft with unreadable date or time reads as null`() = runTest {
        fakePrefs.putRaw(
            "affluences:booking_draft",
            """{"libraryId":"lib-1","zoneId":1,"date":"bad-date","durationMinutes":60,"startTime":"10:00","seatResourceId":10,"note":"","consentAccepted":false,"savedAt":"2026-06-15T10:00:00Z"}""",
        )

        assertThat(store.bookingDraft()).isNull()
    }
}

private class InMemorySharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    fun putRaw(key: String, value: String) {
        map[key] = value
    }

    override fun getAll(): MutableMap<String, *> = map.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        map[key] as? MutableSet<String> ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private val removals = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String?, value: String?) = apply { if (key != null) pending[key] = value }
        override fun putStringSet(key: String?, values: MutableSet<String>?) = apply { if (key != null) pending[key] = values }
        override fun putInt(key: String?, value: Int) = apply { if (key != null) pending[key] = value }
        override fun putLong(key: String?, value: Long) = apply { if (key != null) pending[key] = value }
        override fun putFloat(key: String?, value: Float) = apply { if (key != null) pending[key] = value }
        override fun putBoolean(key: String?, value: Boolean) = apply { if (key != null) pending[key] = value }
        override fun remove(key: String?) = apply { if (key != null) removals.add(key) }
        override fun clear() = apply { clearAll = true }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearAll) map.clear()
            removals.forEach { map.remove(it) }
            map.putAll(pending)
        }
    }
}
