package it.attendance100.mybicocca.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey val email: String,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "receives_on") val receivesOn: String?,
    @ColumnInfo(name = "rooms") val buildings: List<TeacherBuilding> = emptyList(),
)
