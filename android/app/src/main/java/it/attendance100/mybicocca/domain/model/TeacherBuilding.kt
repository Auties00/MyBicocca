package it.attendance100.mybicocca.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("teacher_room")
data class TeacherBuilding(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "description")
    val description: String?
)