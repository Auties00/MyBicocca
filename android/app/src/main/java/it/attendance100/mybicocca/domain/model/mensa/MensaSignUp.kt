package it.attendance100.mybicocca.domain.model.mensa

/** What the user typed in the sign-up form. */
data class MensaSignUpInput(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val captcha: String,
)

sealed interface MensaEmailCheck {
    data object Usable : MensaEmailCheck

    /** Already used, or refused for another reason; [message] is Compass's own. */
    data class Refused(val message: String) : MensaEmailCheck

    /** Compass offered no check: the submission will tell. */
    data object Unknown : MensaEmailCheck
}

sealed interface MensaSignUpResult {
    /** Accepted: a code was emailed to [email]. */
    data class NeedsCode(val email: String) : MensaSignUpResult

    /** Refused, with Compass's reasons (may be empty). */
    data class Refused(val reasons: List<String>) : MensaSignUpResult
}

sealed interface MensaActivationResult {
    data object Activated : MensaActivationResult
    data class Failed(val message: String) : MensaActivationResult
}

/** Compass's sign-up pages no longer look as expected: the app falls back to showing them. */
class MensaSignUpUnavailableException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** The password rules Compass's sign-up enforces, checked as the user types. */
enum class MensaPasswordRule {
    LENGTH,
    UPPER_AND_LOWER,
    DIGIT,
    SPECIAL,

    /** Only letters, digits, spaces and the allowed specials: no accents or other symbols. */
    ALLOWED_CHARACTERS,
    ;

    companion object {
        /** The specials Compass accepts, and requires at least one of. */
        const val SPECIALS = "!*(),.?"

        private val Allowed = Regex("""^[A-Za-z0-9_\s!*(),.?]*$""")

        /** The rules [password] still breaks; empty when Compass will take it. */
        fun broken(password: String): Set<MensaPasswordRule> = buildSet {
            if (password.length < 8) add(LENGTH)
            if (!(password.any { it in 'a'..'z' } && password.any { it in 'A'..'Z' })) add(UPPER_AND_LOWER)
            if (password.none { it in '0'..'9' }) add(DIGIT)
            if (password.none { it in SPECIALS }) add(SPECIAL)
            if (!Allowed.matches(password)) add(ALLOWED_CHARACTERS)
        }
    }
}
