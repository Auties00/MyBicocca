package it.attendance100.mybicocca.data.remote.esse3.api

data class Esse3StudentProfile(
    val personId: Long,
    val studentId: Long,
    val enrollmentId: Long,
    val matricola: String,
    val matId: Long,
    val degreeCourseId: Long,
    val userId: String
)
