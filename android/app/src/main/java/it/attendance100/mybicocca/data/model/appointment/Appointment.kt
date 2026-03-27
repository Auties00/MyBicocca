package it.attendance100.mybicocca.data.model.appointment

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey val id: Long,
    val personId: Long,
    val calendarContext: String? = null,
    val description: String? = null,
    val date: String? = null,
    val status: String? = null,
)
