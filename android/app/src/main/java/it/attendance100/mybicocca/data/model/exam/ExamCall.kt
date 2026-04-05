package it.attendance100.mybicocca.data.model.exam

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "exam_calls")
data class ExamCall(
    @PrimaryKey val id: Long,
    val careerId: Long,
    val courseOfStudyId: Long = 0,
    val activityId: Long = 0,
    val activityName: String,
    val activityCode: String? = null,
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val room: String? = null,
    val building: String? = null,
    val enrollmentStartDate: String? = null,
    val enrollmentEndDate: String? = null,
    val enrolledCount: Int? = null,
    val stateDescription: String? = null,
    val examinerEmails: List<String>? = null,
)
