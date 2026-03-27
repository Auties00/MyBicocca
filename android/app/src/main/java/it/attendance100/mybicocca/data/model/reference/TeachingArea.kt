package it.attendance100.mybicocca.data.model.reference

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teaching_areas")
data class TeachingArea(
    @PrimaryKey val code: String,
    val name: String,
)
