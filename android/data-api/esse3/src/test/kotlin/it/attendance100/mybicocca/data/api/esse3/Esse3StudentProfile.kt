package it.attendance100.mybicocca.data.api.esse3

data class Esse3StudentProfile(
    val personId: Long,
    val studentId: Long,
    val enrollmentId: Long,
    val matricola: String,
    val degreeCourseId: Long,
    val userId: String
)
