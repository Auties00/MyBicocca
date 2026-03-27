package it.attendance100.mybicocca.data.model.isee

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "isee_declarations")
data class IseeDeclaration(
    @PrimaryKey val id: Long,
    val personId: Long,
    val academicYear: Long,
    val iseeValue: Double? = null,
    val ispeValue: Double? = null,
    val bandDescription: String? = null,
    val presentationDate: String? = null,
    val familyMemberCount: Int = 0,
)
