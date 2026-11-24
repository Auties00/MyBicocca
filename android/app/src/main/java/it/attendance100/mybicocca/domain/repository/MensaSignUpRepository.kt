package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.mensa.MensaActivationResult
import it.attendance100.mybicocca.domain.model.mensa.MensaEmailCheck
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpInput
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpResult

/**
 * Creates a canteen account, one sign-up per instance: [start] opens the session the captcha and the
 * form belong to. Every call throws
 * [it.attendance100.mybicocca.domain.model.mensa.MensaSignUpUnavailableException] when Compass's pages
 * changed, so the caller can fall back to the web form; network failures throw as usual.
 */
interface MensaSignUpRepository {
    suspend fun start()

    /** A new captcha image (PNG) for this sign-up. */
    suspend fun captcha(): ByteArray

    suspend fun checkEmail(email: String): MensaEmailCheck

    suspend fun submit(input: MensaSignUpInput): MensaSignUpResult

    /** Sends the emailed code; works even after the app was closed in between. */
    suspend fun activate(email: String, code: String): MensaActivationResult

    fun close()
}
