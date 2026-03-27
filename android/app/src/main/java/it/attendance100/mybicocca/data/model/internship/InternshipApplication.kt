package it.attendance100.mybicocca.data.model.internship

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "internship_applications")
data class InternshipApplication(
    @PrimaryKey val id: Long,
    val studentId: Long,
    val companyName: String? = null,
    val status: String? = null,
    val title: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
)
