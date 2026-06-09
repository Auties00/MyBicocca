package it.attendance100.mybicocca.data.remote.affluences.api

import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import java.util.UUID

class AffluencesAccountApiTest : AffluencesTestBase() {
    companion object {
        // A stable, opaque device id for the integration runs — check-in just registers it.
        private const val TEST_DEVICE_ID = "mybicocca-integration-test"
    }

    // Registers the device and returns the minted user-identifier (the api key the rest of the
    // account flow gates on), like firstResourceTypeFilters() in the reservation tests. The other
    // calls are exercised through their error paths, so no validation email is ever sent.
    private suspend fun freshApiKey(): String {
        val apiKey = assertNotNull(
            api.account.checkIn(deviceId = TEST_DEVICE_ID).apiKey,
            "Check-in should mint a user-identifier api key",
        )
        assertTrue(apiKey.isNotBlank(), "The minted api key should not be blank")
        return apiKey
    }

    @Test
    suspend fun checkInMintsApiKey() {
        val checkin = api.account.checkIn(deviceId = TEST_DEVICE_ID)
        val apiKey = assertNotNull(checkin.apiKey, "Check-in should mint a user-identifier api key")
        assertTrue(apiKey.isNotBlank(), "The minted api key should not be blank")
    }

    @Test
    suspend fun requestEmailValidationWithInvalidEmailThrows() {
        // A malformed email is rejected before any mail is queued, mirroring
        // createReservationWithInvalidEmailThrows on the reservation api.
        val apiKey = freshApiKey()
        val error = runCatching {
            api.account.requestEmailValidation(apiKey = apiKey, email = "not-an-email")
        }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(
            error,
            "Invalid emails should be reported as AffluencesException",
        )
        assertTrue(affluencesError.errorMessage.isNotBlank(), "Error message should not be blank")
    }

    @Test
    suspend fun pollEmailValidationWithUnknownRequestReportsDoesNotExist() {
        // Polling a request the user never confirmed (here, one that never existed) answers with the
        // documented does_not_exist error rather than a session token.
        val apiKey = freshApiKey()
        val error = runCatching {
            api.account.pollEmailValidation(apiKey = apiKey, requestUuid = UUID.randomUUID().toString())
        }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(
            error,
            "An unconfirmed request should be reported as AffluencesException",
        )
        assertEquals(
            "does_not_exist",
            affluencesError.errorCode,
            "Error code should mark the request as not yet confirmed",
        )
    }

    @Test
    suspend fun getMyReservationsWithBogusTokenIsRejected() {
        val apiKey = freshApiKey()
        val error = runCatching {
            api.account.getMyReservations(apiKey = apiKey, authToken = "bogus-token")
        }.exceptionOrNull()
        val rejection = assertNotNull(error, "A bogus session token should be rejected")
        assertTrue(
            rejection is ApiRequestException || rejection is AffluencesException,
            "A bogus session token should surface as an API error, was ${rejection::class.simpleName}",
        )
    }
}
