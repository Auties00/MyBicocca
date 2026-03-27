package it.attendance100.mybicocca.data.model.teacher

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey val code: String,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val facultyCode: String? = null,
)
