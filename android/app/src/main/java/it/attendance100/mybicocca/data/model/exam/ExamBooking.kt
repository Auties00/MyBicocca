package it.attendance100.mybicocca.data.model.exam

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_bookings")
data class ExamBooking(
    @PrimaryKey val id: Long,
    val careerId: Long,
    val activityName: String,
    val examDate: String? = null,
    val bookingDate: String? = null,
    val position: Int? = null,
)
