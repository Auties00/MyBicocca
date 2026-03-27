package it.attendance100.mybicocca.data.model.reference

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_programs")
data class StudyProgram(
    @PrimaryKey val code: String,
    val name: String,
    val degreeType: String? = null,
    val teachingAreaCode: String? = null,
    val yearCount: Int? = null,
)
