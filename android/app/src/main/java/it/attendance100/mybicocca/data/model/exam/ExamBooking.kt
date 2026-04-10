package it.attendance100.mybicocca.data.model.exam

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_bookings")
data class ExamBooking(
    @PrimaryKey val id: Long,
    val courseOfStudyId: Long,
    val activityId: Long,
    val callId: Long,
    val studentId: Long,
    val activityChoiceId: Long,
    val matricolaId: Long,
    val activityCode: String? = null,
    val activityName: String,
    val examDate: String? = null,
    val bookingDate: String? = null,
    val position: Int? = null,
    val callDescription: String? = null,
    val building: String? = null,
    val room: String? = null,
)
