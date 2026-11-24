package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.remote.compass.signup.CompassActivationReply
import it.attendance100.mybicocca.data.remote.compass.signup.CompassEmailCheck
import it.attendance100.mybicocca.data.remote.compass.signup.CompassSignUpClient
import it.attendance100.mybicocca.data.remote.compass.signup.CompassSignUpInput
import it.attendance100.mybicocca.data.remote.compass.signup.CompassSignUpReply
import it.attendance100.mybicocca.data.remote.compass.signup.CompassSignUpUnavailable
import it.attendance100.mybicocca.domain.model.mensa.MensaActivationResult
import it.attendance100.mybicocca.domain.model.mensa.MensaEmailCheck
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpInput
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpResult
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpUnavailableException
import it.attendance100.mybicocca.domain.repository.MensaSignUpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** [MensaSignUpRepository] over Compass's sign-up pages, through a [CompassSignUpClient] per sign-up. */
class MensaSignUpRepositoryImpl @Inject constructor() : MensaSignUpRepository {

    private var client: CompassSignUpClient? = null

    override suspend fun start() = call {
        client?.close()
        client = CompassSignUpClient().also { it.start() }
    }

    override suspend fun captcha(): ByteArray = call { requireClient().captcha() }

    override suspend fun checkEmail(email: String): MensaEmailCheck = call {
        when (val check = requireClient().checkEmail(email)) {
            null -> MensaEmailCheck.Unknown
            CompassEmailCheck.Usable -> MensaEmailCheck.Usable
            is CompassEmailCheck.Refused -> MensaEmailCheck.Refused(check.message)
        }
    }

    override suspend fun submit(input: MensaSignUpInput): MensaSignUpResult = call {
        val reply = requireClient().submit(
            CompassSignUpInput(
                firstName = input.firstName.trim(),
                lastName = input.lastName.trim(),
                email = input.email.trim(),
                password = input.password,
                passwordConfirm = input.passwordConfirm,
                captcha = input.captcha.trim(),
            ),
        )
        when (reply) {
            is CompassSignUpReply.NeedsActivation -> MensaSignUpResult.NeedsCode(reply.email.ifBlank { input.email.trim() })
            is CompassSignUpReply.Refused -> MensaSignUpResult.Refused(reply.reasons)
        }
    }

    override suspend fun activate(email: String, code: String): MensaActivationResult = call {
        // The code is not tied to the session, so a sign-up resumed later needs no new one.
        val activator = client ?: CompassSignUpClient().also { client = it }
        when (val reply = activator.activate(email.trim(), code.trim())) {
            CompassActivationReply.Activated -> MensaActivationResult.Activated
            is CompassActivationReply.Failed -> MensaActivationResult.Failed(reply.message)
        }
    }

    override fun close() {
        client?.close()
        client = null
    }

    private fun requireClient(): CompassSignUpClient =
        client ?: throw IllegalStateException("start() first")

    private suspend fun <T> call(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: CompassSignUpUnavailable) {
            throw MensaSignUpUnavailableException(e.message.orEmpty(), e)
        }
    }
}
