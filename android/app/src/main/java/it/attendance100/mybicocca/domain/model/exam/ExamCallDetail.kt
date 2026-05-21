package it.attendance100.mybicocca.domain.model.exam

data class ExamCallDetail(
    val call: ExamCall,
    val notes: String?,
    val president: ExamExaminer?,
    val bookingTypeDescription: String?,
)

data class ExamExaminer(
    val id: Long?,
    val name: String?,
    val surname: String?,
)
