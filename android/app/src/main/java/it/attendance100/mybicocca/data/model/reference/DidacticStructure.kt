package it.attendance100.mybicocca.data.model.reference

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "didactic_structures")
data class DidacticStructure(
    @PrimaryKey val facultyId: Long,
    val code: String? = null,
    val description: String? = null,
)
