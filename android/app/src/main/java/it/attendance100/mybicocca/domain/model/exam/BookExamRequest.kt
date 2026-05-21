package it.attendance100.mybicocca.domain.model.exam

data class BookExamRequest(
    val activityChoiceId: Long,
    val studentNote: String? = null,
)
