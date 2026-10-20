package it.attendance100.mybicocca.data.remote.affluences.api

import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailTokenRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesDeviceAuth
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesCheckinRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAccountAuth
import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
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
        val apiKey = api.account.checkIn(AffluencesCheckinRequest(deviceId = TEST_DEVICE_ID, deviceLang = "it")).apiKey
        assertNotNull(
            apiKey,
            "Check-in should mint a user-identifier api key",
        )
        assertTrue(apiKey.isNotBlank(), "The minted api key should not be blank")
        return apiKey
    }

    @Test
    suspend fun checkInMintsApiKey() {
        val checkin = api.account.checkIn(AffluencesCheckinRequest(deviceId = TEST_DEVICE_ID, deviceLang = "it"))
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
            api.account.requestEmailValidation(AffluencesEmailTokenRequest("not-an-email"), AffluencesDeviceAuth(apiKey))
        }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(
            error,
            "Invalid emails should be reported as AffluencesException",
        )
        assertTrue(affluencesError.errorMessage.isNotBlank(), "Error message should not be blank")
    }

    /**
     * Polling a request that never existed is rejected with an error code rather than a
     * session token: `invalid_token` today, `does_not_exist` (the answer for a request the user
     * hasn't confirmed yet) before.
     */
    @Test
    suspend fun pollEmailValidationWithUnknownRequestIsRejected() {
        val apiKey = freshApiKey()
        val error = runCatching {
            api.account.pollEmailValidation(UUID.randomUUID().toString(), AffluencesDeviceAuth(apiKey))
        }.exceptionOrNull()
        val affluencesError = assertInstanceOf<AffluencesException>(
            error,
            "An unconfirmed request should be reported as AffluencesException",
        )
        assertTrue(
            affluencesError.errorCode in setOf(AffluencesException.NOT_VALIDATED_YET, "invalid_token"),
            "Error code should reject the request, was ${affluencesError.errorCode}",
        )
    }

    @Test
    suspend fun getMyReservationsWithBogusTokenIsRejected() {
        val apiKey = freshApiKey()
        val error = runCatching {
            api.account.getMyReservations(AffluencesAccountAuth(apiKey, "bogus-token"))
        }.exceptionOrNull()
        assertInstanceOf<AffluencesException>(error, "A bogus session token should be rejected")
    }
}
