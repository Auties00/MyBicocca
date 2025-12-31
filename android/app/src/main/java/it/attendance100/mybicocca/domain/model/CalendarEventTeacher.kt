package it.attendance100.mybicocca.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_event_teacher")
data class CalendarEventTeacher(
    @PrimaryKey
    val code: String,

    @ColumnInfo(name = "fullName")
    val fullName: String,

    @ColumnInfo(name = "email")
    val email: String
)