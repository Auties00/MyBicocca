package it.attendance100.mybicocca.domain.model.elearning.quiz

data class AttemptPage(
    val attemptId: AttemptId,
    val pageIndex: Int,
    val nextPage: Int?,
    val questions: List<AttemptQuestion>,
)
