package it.attendance100.mybicocca.data.model.attendance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseCode: String,
    val courseName: String,
    val attendancePercentage: Float,
    val lessonsAttended: Int,
    val totalHours: Float,
    val status: String,
)
