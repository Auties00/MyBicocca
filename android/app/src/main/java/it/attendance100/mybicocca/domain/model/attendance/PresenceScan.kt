package it.attendance100.mybicocca.domain.model.attendance

sealed interface PresenceScan {

    data class SessionLink(val sessionId: String, val password: String?) : PresenceScan

    data class LessonCode(val code: String) : PresenceScan

    data class Unrecognized(val raw: String) : PresenceScan
}
