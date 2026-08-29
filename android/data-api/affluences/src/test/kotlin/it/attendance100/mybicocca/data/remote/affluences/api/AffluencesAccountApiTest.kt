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
        /** A stable, opaque device id for the integration runs — check-in just registers it. */
        private const val TEST_DEVICE_ID = "mybicocca-integration-test"
    }

    /**
     * Registers the device and returns the minted user-identifier, the api key the rest of
     * the account flow gates on. The remaining account calls are exercised through their
     * error paths, so no validation email is ever sent.
     */
    private suspend fun freshApiKey(): String {
        val apiKey = api.account.checkIn(deviceId = TEST_DEVICE_ID).apiKey
        assertNotNull(
            apiKey,
            "Check-in should mint a user-identifier api key",
        )
        assertTrue(apiKey.isNotBlank(), "The minted api key should not be blank")
        return apiKey
    }

    @Test
    suspend fun checkInMintsApiKey() {
        val checkin = api.account.checkIn(deviceId = TEST_DEVICE_ID)
        val apiKey = checkin.apiKey
        assertNotNull(apiKey, "Check-in should mint a user-identifier api key")
        assertTrue(apiKey.isNotBlank(), "The minted api key should not be blank")
    }

    /**
     * A malformed email is rejected before any mail is queued, so the test never triggers a
     * real validation email.
     */
    @Test
    suspend fun requestEmailValidationWithInvalidEmailThrows() {
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

    /**
     * Polling a request the user never confirmed (here, one that never existed) answers with
     * the documented `does_not_exist` error rather than a session token.
     */
    @Test
    suspend fun pollEmailValidationWithUnknownRequestReportsDoesNotExist() {
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
        assertNotNull(error, "A bogus session token should be rejected")
        assertTrue(
            error is ApiRequestException || error is AffluencesException,
            "A bogus session token should surface as an API error, was ${error.let { it::class.simpleName }}",
        )
    }
}
