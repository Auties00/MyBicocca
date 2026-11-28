package it.attendance100.mybicocca.data.local.credentials

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Lazy
import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.model.library.LibraryPendingValidation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Stores the Affluences "my reservations" account state: the per-install device id and api key
 * minted via check-in and, once the user validates an e-mail address, the session token and
 * that e-mail. The token grants access to the user's bookings, so everything lives in the
 * encrypted preferences shared with the credential store.
 */
@Singleton
class LibraryAccountStore @Inject constructor(
    @Named(CredentialsStore.EncryptedPrefsName) private val prefs: Lazy<SharedPreferences>,
) {

    /** Opaque device id sent on check-in; generated once per install and reused. */
    suspend fun deviceId(): String = withContext(Dispatchers.IO) {
        val store = prefs.get()
        store.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().replace("-", "").take(16)
            .also { store.edit { putString(KEY_DEVICE_ID, it) } }
    }

    suspend fun apiKey(): String? = withContext(Dispatchers.IO) { prefs.get().getString(KEY_API_KEY, null) }

    suspend fun setApiKey(value: String): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit { putString(KEY_API_KEY, value) }
    }

    suspend fun authToken(): String? = withContext(Dispatchers.IO) { prefs.get().getString(KEY_AUTH_TOKEN, null) }

    suspend fun email(): String? = withContext(Dispatchers.IO) { prefs.get().getString(KEY_EMAIL, null) }

    suspend fun setSession(email: String, authToken: String): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit {
            putString(KEY_EMAIL, email)
            putString(KEY_AUTH_TOKEN, authToken)
        }
    }

    suspend fun clearSession(): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit {
            remove(KEY_EMAIL)
            remove(KEY_AUTH_TOKEN)
        }
    }

    suspend fun pendingValidation(): LibraryPendingValidation? = withContext(Dispatchers.IO) {
        val raw = prefs.get().getString(KEY_PENDING_VALIDATION, null) ?: return@withContext null
        runCatching {
            val stored = json.decodeFromString<StoredPendingValidation>(raw)
            LibraryPendingValidation(
                email = stored.email,
                requestUuid = stored.requestUuid,
                createdAt = Instant.parse(stored.createdAt),
            )
        }.getOrNull()
    }

    suspend fun setPendingValidation(value: LibraryPendingValidation): Unit = withContext(Dispatchers.IO) {
        val encoded = json.encodeToString(
            StoredPendingValidation(
                email = value.email,
                requestUuid = value.requestUuid,
                createdAt = value.createdAt.toString(),
            ),
        )
        prefs.get().edit { putString(KEY_PENDING_VALIDATION, encoded) }
    }

    suspend fun clearPendingValidation(): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit { remove(KEY_PENDING_VALIDATION) }
    }

    suspend fun bookingDraft(): LibraryBookingDraft? = withContext(Dispatchers.IO) {
        val raw = prefs.get().getString(KEY_BOOKING_DRAFT, null) ?: return@withContext null
        runCatching {
            val stored = json.decodeFromString<StoredBookingDraft>(raw)
            LibraryBookingDraft(
                libraryId = stored.libraryId,
                zoneId = stored.zoneId,
                date = LocalDate.parse(stored.date),
                durationMinutes = stored.durationMinutes,
                startTime = LocalTime.parse(stored.startTime),
                seatResourceId = stored.seatResourceId,
                note = stored.note,
                consentAccepted = stored.consentAccepted,
                savedAt = Instant.parse(stored.savedAt),
            )
        }.getOrNull()
    }

    suspend fun setBookingDraft(value: LibraryBookingDraft): Unit = withContext(Dispatchers.IO) {
        val encoded = json.encodeToString(
            StoredBookingDraft(
                libraryId = value.libraryId,
                zoneId = value.zoneId,
                date = value.date.toString(),
                durationMinutes = value.durationMinutes,
                startTime = value.startTime.toString(),
                seatResourceId = value.seatResourceId,
                note = value.note,
                consentAccepted = value.consentAccepted,
                savedAt = value.savedAt.toString(),
            ),
        )
        prefs.get().edit { putString(KEY_BOOKING_DRAFT, encoded) }
    }

    suspend fun clearBookingDraft(): Unit = withContext(Dispatchers.IO) {
        prefs.get().edit { remove(KEY_BOOKING_DRAFT) }
    }

    @Serializable
    private data class StoredPendingValidation(
        val email: String,
        val requestUuid: String,
        val createdAt: String,
    )

    @Serializable
    private data class StoredBookingDraft(
        val libraryId: String,
        val zoneId: Int,
        val date: String,
        val durationMinutes: Int,
        val startTime: String,
        val seatResourceId: Int,
        val note: String,
        val consentAccepted: Boolean,
        val savedAt: String,
    )

    private companion object {
        const val KEY_DEVICE_ID = "affluences:device_id"
        const val KEY_API_KEY = "affluences:api_key"
        const val KEY_AUTH_TOKEN = "affluences:auth_token"
        const val KEY_EMAIL = "affluences:email"
        const val KEY_PENDING_VALIDATION = "affluences:pending_validation"
        const val KEY_BOOKING_DRAFT = "affluences:booking_draft"
        val json = Json { ignoreUnknownKeys = true }
    }
}
