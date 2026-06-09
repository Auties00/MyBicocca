package it.attendance100.mybicocca.domain.model.attendance

sealed interface PresenceMarkOutcome {

    val message: String

    data class Recorded(
        override val message: String,
        val statusDescription: String? = null,
    ) : PresenceMarkOutcome

    data class AlreadyRecorded(override val message: String) : PresenceMarkOutcome

    data class WrongCredential(override val message: String) : PresenceMarkOutcome

    data class NotOpen(override val message: String) : PresenceMarkOutcome

    data class Failed(override val message: String) : PresenceMarkOutcome
}
